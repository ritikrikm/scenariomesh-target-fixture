package io.scenariomesh.fixture;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public final class FixtureSteps {
    private WebDriver driver;
    private String scenarioId;

    @Given("fixture scenario {string} opens an isolated page")
    public void opensIsolatedPage(String id) {
        scenarioId = id;
        crashWorkerOnceIfRequested(id);
        if (!browserEnabled()) {
            return;
        }
        driver = createDriver();

        String html = "<html><body><input id='value'/><button id='submit' " +
                "onclick=\"document.getElementById('result').textContent=document.getElementById('value').value\">" +
                "Submit</button><div id='result'></div></body></html>";
        String encoded = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));
        driver.get("data:text/html;base64," + encoded);
    }

    @When("the scenario writes its unique id")
    public void writesUniqueId() {
        if (!browserEnabled()) {
            return;
        }
        driver.findElement(By.id("value")).sendKeys(scenarioId);
        driver.findElement(By.id("submit")).click();
    }

    @Then("only id {string} is visible")
    public void onlyExpectedIdIsVisible(String expectedId) throws IOException {
        if (browserEnabled()) {
            requireEqual(expectedId, driver.findElement(By.id("result")).getText());
        } else {
            requireEqual(expectedId, scenarioId);
        }
        recordExecution(expectedId);
    }

    private static void recordExecution(String id) throws IOException {
        Path directory = Path.of(System.getProperty("fixture.executionDir", "target/fixture-executions"));
        Files.createDirectories(directory);
        Path marker = directory.resolve(id + "__" + UUID.randomUUID() + ".done");
        Files.writeString(marker, id + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private static void crashWorkerOnceIfRequested(String id) {
        String requested = System.getProperty("fixture.crash.once.id", "").trim();
        if (!id.equals(requested)) {
            return;
        }
        Path directory = Path.of(System.getProperty("fixture.crashDir", "target/fixture-crash-sentinels"));
        Path sentinel = directory.resolve(id + ".crashed-once");
        try {
            Files.createDirectories(directory);
            Files.createFile(sentinel);
            System.err.println("FIXTURE: intentionally terminating worker for " + id);
            Runtime.getRuntime().halt(86);
        } catch (java.nio.file.FileAlreadyExistsException alreadyCrashed) {
            System.out.println("FIXTURE: retry observed for " + id + "; continuing normally");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create crash sentinel for " + id, exception);
        }
    }

    private static void requireEqual(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected [" + expected + "] but found [" + actual + "]");
        }
    }

    private static boolean browserEnabled() {
        return browserMode() != BrowserMode.NONE;
    }

    private WebDriver createDriver() {
        BrowserMode mode = browserMode();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1280,720");
        switch (mode) {
            case NONE -> throw new IllegalStateException("Browser driver requested while fixture.browser.mode=none");
            case CHROME_HEADLESS -> {
                options.addArguments("--headless=new");
                return new ChromeDriver(options);
            }
            case CHROME_HEADED -> {
                return new ChromeDriver(options);
            }
            case REMOTE -> {
                String remote = System.getProperty("fixture.webdriver.remote.url", "").trim();
                if (remote.isEmpty()) {
                    throw new IllegalStateException("fixture.webdriver.remote.url must be set when fixture.browser.mode=remote");
                }
                try {
                    URL url = URI.create(remote).toURL();
                    return new RemoteWebDriver(url, options);
                } catch (Exception exception) {
                    throw new IllegalStateException("Could not create remote WebDriver for " + remote, exception);
                }
            }
            default -> throw new IllegalStateException("Unsupported browser mode: " + mode);
        }
    }

    private static BrowserMode browserMode() {
        String explicit = System.getProperty("fixture.browser.mode", "").trim().toLowerCase(java.util.Locale.ROOT);
        if (!explicit.isEmpty()) {
            return switch (explicit) {
                case "none", "off", "disabled" -> BrowserMode.NONE;
                case "chrome-headless", "headless", "chrome" -> BrowserMode.CHROME_HEADLESS;
                case "chrome-headed", "headed" -> BrowserMode.CHROME_HEADED;
                case "remote", "remote-webdriver", "grid" -> BrowserMode.REMOTE;
                default -> throw new IllegalArgumentException("Unknown fixture.browser.mode: " + explicit);
            };
        }
        if (!Boolean.parseBoolean(System.getProperty("fixture.browser.enabled", "true"))) {
            return BrowserMode.NONE;
        }
        return BrowserMode.CHROME_HEADLESS;
    }

    private enum BrowserMode {
        NONE,
        CHROME_HEADLESS,
        CHROME_HEADED,
        REMOTE
    }

    @After
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}
