package com.example.independent.steps;

import com.example.independent.config.TestConfig;
import com.example.independent.pages.AccountPage;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.UUID;

public final class AccountSteps {
    private WebDriver driver;
    private AccountPage page;
    private String id;

    @Given("an account workflow {string}")
    public void accountWorkflow(String value) throws Exception {
        this.id = value;
        Thread.sleep(TestConfig.latencyMillis());
        if (TestConfig.browserEnabled()) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
            String html = "<html><body><input id='reference'><button id='submit' onclick=\"document.getElementById('result').textContent=document.getElementById('reference').value\">Go</button><div id='result'></div></body></html>";
            String data = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));
            driver.get("data:text/html;base64," + data);
            page = new AccountPage(driver);
        }
    }

    @When("the workflow is submitted")
    public void submit() {
        if (page != null) {
            page.enterReference(id);
            page.submit();
        }
    }

    @Then("workflow {string} completes")
    public void completes(String expected) throws Exception {
        if (page != null && !expected.equals(page.result())) {
            throw new AssertionError("unexpected browser result");
        }
        Path dir = Path.of("target/independent-executions");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(expected + "__" + UUID.randomUUID() + ".done"), expected,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    @After
    public void close() {
        if (driver != null) driver.quit();
    }
}
