package com.example.independent.config;

public final class TestConfig {
    private TestConfig() {}
    public static boolean browserEnabled() {
        return Boolean.parseBoolean(System.getProperty("ui.browser.enabled", "false"));
    }
    public static long latencyMillis() {
        return Long.parseLong(System.getProperty("fixture.latency.ms", "2"));
    }
}
