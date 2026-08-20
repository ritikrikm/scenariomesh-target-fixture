package com.example.independent.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;

public final class TestHooks {
    @Before
    public void beforeScenario() {
        System.setProperty("fixture.lastHook", "before");
    }
    @After
    public void afterScenario() {
        System.setProperty("fixture.lastHook", "after");
    }
}
