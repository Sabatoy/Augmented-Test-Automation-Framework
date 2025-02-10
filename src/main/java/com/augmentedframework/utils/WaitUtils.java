package com.augmentedframework.utils;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;

/**
 * Wait class provides actions to wait for page load and page load with user defined max time
 * used globally in all classes and methods
 *
 * @author YSabato
 * @version 1.0
 * @since 01/01/2020
 */
public class WaitUtils {

    public static void waitForPageLoad(final WebDriver driver) {
        waitForPageLoad(driver, WebDriverFactory.maxPageLoadWait);
    }

    public static void waitForPageLoad(final WebDriver driver, Duration maxWait) {
        long startTime = StopWatch.startTime();
        FluentWait<WebDriver> wait = new WebDriverWait(driver, maxWait)
                .pollingEvery(Duration.ofSeconds(500))
                .ignoring(StaleElementReferenceException.class,WebDriverException.class)
                .withMessage("Page Load Timed Out");
        try {
            wait.until(WebDriverFactory.documentLoad);
            wait.until(WebDriverFactory.imagesLoad);
            wait.until(WebDriverFactory.framesLoad);
            String title = driver.getTitle().toLowerCase();
            String url = driver.getCurrentUrl().toLowerCase();
            Log.event("Page URL:: " + url);

            if ("Page cannot be found".equalsIgnoreCase(title) || title.contains("is not available") ||
                    url.contains("/error/") || url.toLowerCase().contains("/errorpage/")) {
                Assert.fail("Site is down. [Title: " + title + ", URL:" + url + "]");
            }
        } catch (TimeoutException e) {
            driver.navigate().refresh();
            wait.until(WebDriverFactory.documentLoad);
            wait.until(WebDriverFactory.imagesLoad);
            wait.until(WebDriverFactory.framesLoad);
        }

        Log.event("Page Load Wait: (Sync)", StopWatch.elapsedTime(startTime));
    }
}
