package com.augmentedframework.utils;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Helper class to setup general browser options
 *
 * @author Sabato
 * @version 1.0
 * @since 01/01/2020
 */
public class WebDriverOptions {

    public static ChromeOptions setChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--disable-extensions");
        chromeOptions.addArguments("--always-authorize-plugins");
        chromeOptions.addArguments("--allow-running-insecure-content");
        chromeOptions.addArguments("--test-type");
        chromeOptions.addArguments("--enable-npapi");
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.plugins", 1);
        prefs.put("profile.content_settings.plugin_whitelist.adobe-flash-player", 1);
        prefs.put("profile.content_settings.exceptions.plugins.*,*.per_resource.adobe-flash-player", 1);
        return chromeOptions;
    }

    public static EdgeOptions setEdgeOptions() {
        EdgeOptions edgeOptions = new EdgeOptions();
        return edgeOptions;
    }
}
