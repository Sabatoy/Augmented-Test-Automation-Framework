package com.augmentedframework.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.augmentedframework.ui.config.grid.BrowserStackCapabilitiesConfiguration;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * WebdriverFactory class create a web driver instance
 * based on user needs like browser type, host, and port.
 * It sets up desired capabilities and initializes actions.
 * It also includes methods to get Hub and node details,
 * and handles page loading for images, frames, and documents.
 *
 * @author Sabato
 * @version 1.0
 * @since 01/01/2020
 */
public class WebDriverFactory {
    private static Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);

    private static String userName;
    private static String accessKey;

    public static ExpectedCondition<Boolean> documentLoad;
    public static ExpectedCondition<Boolean> framesLoad;
    public static ExpectedCondition<Boolean> imagesLoad;
    public static Duration maxPageLoadWait = Duration.ofSeconds(120);

    static {
        documentLoad = driver -> {
            final JavascriptExecutor js = (JavascriptExecutor) driver;
            boolean docReadyState = false;
            try {
                docReadyState = (Boolean) js.executeScript("return (function() { if (document.readyState != 'complete') {  return false; } if (window.jQuery != null && window.jQuery != undefined && window.jQuery.active) { return false;} if (window.jQuery != null && window.jQuery != undefined && window.jQuery.ajax != null && window.jQuery.ajax != undefined && window.jQuery.ajax.active) {return false;}  if (window.angular != null && angular.element(document).injector() != null && angular.element(document).injector().get('$http').pendingRequests.length) return false; return true;})();");
            } catch (WebDriverException e) {
                docReadyState = true;
            }
            return docReadyState;
        };

        imagesLoad = driver -> {
            boolean docReadyState = true;
            try {
                JavascriptExecutor js;
                List<WebElement> images = driver.findElements(By.cssSelector("img[src]"));
                for (int i = 0; i < images.size(); i++) {
                    try {
                        js = (JavascriptExecutor) driver;
                        docReadyState = docReadyState && (Boolean) js.executeScript("return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0", images.get(i));
                        if (!docReadyState) {
                            break;
                        }
                    } catch (StaleElementReferenceException e) {
                        images = driver.findElements(By.cssSelector("img[src]"));
                        i--;
                    } catch (WebDriverException e) {
                        docReadyState = true;
                    }
                }
            } catch (WebDriverException e) {
                docReadyState = true;
            }
            return docReadyState;
        };

        framesLoad = driver -> {
            boolean docReadyState = true;
            try {
                JavascriptExecutor js;
                List<WebElement> frames = driver.findElements(By.cssSelector("iframe[style*='hidden']"));
                for (WebElement frame : frames) {
                    try {
                        driver.switchTo().defaultContent();
                        driver.switchTo().frame(frame);
                        js = (JavascriptExecutor) driver;
                        docReadyState = (Boolean) js.executeScript("return (document.readyState==\"complete\")");
                        driver.switchTo().defaultContent();
                        if (!docReadyState) {
                            break;
                        }
                    } catch (WebDriverException e) {
                        docReadyState = true;
                    }
                }
            } catch (WebDriverException e) {
                docReadyState = true;
            } finally {
                driver.switchTo().defaultContent();
            }
            return docReadyState;
        };
    }

    /**
     *
     * @return
     * @throws MalformedURLException
     */
    private static URL getHubUrl() throws MalformedURLException {
        return new URL(String.format("<browserstack url>", userName, accessKey));
    }

    /**
     *
     * @return
     */
    public static WebDriver get() {
        BrowserStackCapabilitiesConfiguration caps = setCapabilityBasedOnEnvironment(true, null);
        Map<String, String> capsToOverride = new HashMap<>();
        String testName = new Exception().getStackTrace()[1].getMethodName();
        capsToOverride.put("name", testName);
        caps.override(capsToOverride);
        return getNewSession(setBrowserOptions(caps), testName);
    }

    /**
     *
     * @param caps
     * @param testName
     * @return
     */
    private synchronized static WebDriver getNewSession(DesiredCapabilities caps, String testName) {
        Log.event("Requesting browser instance...");
        synchronized (System.class) {
            logger.debug("Capabilities setup is complete. Creating a Remote Web Driver...");
            RemoteWebDriver driver;
            try {
                driver = new RemoteWebDriver(getHubUrl(), caps);
                logger.debug("Remote Web driver has been successfully created");
                String bsLink = getPublicBSUrl(driver);
                logger.debug("BrowserStack link for " + testName + ":: " + bsLink);
                maximize(driver, caps);
                return driver;
            } catch(MalformedURLException e) {
                logger.error("This is not a valid URL to be passed to the Remote web driver");
            }
        }
        return null;
    }

    /**
     * Maximize browser
     *
     * @param driver
     * @param capabilities
     */
    public static void maximize(WebDriver driver, DesiredCapabilities capabilities) {
        try {
            if(capabilities.getCapability("os").toString().toLowerCase().contains("os x")){
                driver.manage().window().fullscreen();
            } else {
                driver.manage().window().maximize();
            }
        } catch(UnsupportedCommandException e) {
            long width = (Long) ((JavascriptExecutor)driver).executeScript("return window.screen.width");
            long height = (Long) ((JavascriptExecutor)driver).executeScript("return window.screen.height");
            driver.manage().window().setSize(new Dimension((int)width, (int)height));
        }
    }

    private static BrowserStackCapabilitiesConfiguration setCapabilityBasedOnEnvironment(boolean isJenkinsRun, String platform) {
        BrowserStackCapabilitiesConfiguration caps = new BrowserStackCapabilitiesConfiguration();
        Map<String, String> capsToOverride = new HashMap<>();
        try {
            if(isJenkinsRun) {
                overrideWithSysProp(caps);
                capsToOverride.put("build", System.getenv("BUILD_ID"));
            } else {
                overrideWithPlatformParam(platform, caps);
                capsToOverride.put("build", "Local Build");
            }
            caps.override(capsToOverride);
        } catch(Exception e) {
            Log.fail(e.getMessage());
        }

        return caps;
    }

    /**
     *
     * @param caps
     * @throws Exception
     */
    private static void overrideWithSysProp(BrowserStackCapabilitiesConfiguration caps) throws Exception {
        Properties sysProp = System.getProperties();
        userName = System.getenv().containsKey("userName") ? System.getenv().get("userName") : userName;
        accessKey = System.getenv().containsKey("accessKey") ? System.getenv().get("accessKey") : accessKey;
        if (sysProp.containsKey("os_v_browser_v")) {
            overrideWithPlatformParam(sysProp.getProperty("os_v_browser_v"), caps);
        } else if (System.getenv().containsKey("os_v_browser_v")) {
            overrideWithPlatformParam(System.getenv("os_v_browser_v"), caps);
        }
    }

    /**
     *
     * @param platform
     * @param caps
     * @throws Exception
     */
    private static void overrideWithPlatformParam(String platform, BrowserStackCapabilitiesConfiguration caps) throws Exception {
        String[] combination = platform.split("_");
        if (combination.length != 4) {
            throw new Exception(String.format("Malformed platform %s.", platform));
        }
        Map<String, String> platformCaps = new HashMap<>();
        platformCaps.put("os", combination[0]);
        platformCaps.put("os_version", combination[1]);
        platformCaps.put("browser", combination[2]);
        platformCaps.put("browser_version", combination[3]);
        caps.override(platformCaps);
    }

    /**
     *
     * @param driver
     * @return
     */
    public static String getPublicBSUrl(WebDriver driver) {
        try {
            String sessionUrl = String.format("<browserstack url>", userName, accessKey,
                    ((RemoteWebDriver)driver).getSessionId());
            String sessionBody = RestAssured.get(sessionUrl).body().asString();
            return new JSONObject(sessionBody).getJSONObject("automation_session").getString("public_url");
        } catch(Exception e) {
            e.printStackTrace();
            return "Issue getting public URL of BrowserStack";
        }
    }

    /**
     *
     * @param caps
     * @return
     */
    private static DesiredCapabilities setBrowserOptions(BrowserStackCapabilitiesConfiguration caps) {
        DesiredCapabilities desiredCapabilities = caps.getDesiredCapabilities();
        desiredCapabilities.setCapability("<browserstack url", false);
        switch (caps.getBrowser()) {
            case "Chrome":
                desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, WebDriverOptions.setChromeOptions());
                break;
            case "Firefox":
                // FirefoxOptions To-Do
                break;
            case "Edge":
                //desiredCapabilities.setCapability(EdgeOptions.CAPABILITY, WebDriverOptions.setEdgeOptions());
                break;
        }
        return desiredCapabilities;
    }
}

