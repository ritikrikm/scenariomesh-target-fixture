package com.example.independent.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class AccountPage {
    private final WebDriver driver;
    public AccountPage(WebDriver driver) { this.driver = driver; }
    public void enterReference(String value) { driver.findElement(By.id("reference")).sendKeys(value); }
    public void submit() { driver.findElement(By.id("submit")).click(); }
    public String result() { return driver.findElement(By.id("result")).getText(); }
}
