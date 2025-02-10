package com.augmentedframework.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.testng.Assert;
import org.testng.Reporter;

import java.text.SimpleDateFormat;

/**
 * Log class captures and prints screenshots and test case info and messages with actions for writing log
 * status as pass/fail
 *
 * @author YSabato
 * @version 1.0
 * @since 01/01/2020
 */
public class Log {
    public static boolean consoleOutPut;
    private static String screenShotPath;

    static final String MESSAGE_HTML_BEGIN = "";
    static final String MESSAGE_HTML_END = "";

    static final String FAIL_HTML_BEGIN = "";
    static final String FAIL_HTML_END1 = "";
    static final String FAIL_HTML_END2 = "";

    static final String EVENT_HTML_BEGIN = "";
    static final String EVENT_HTML_END = "";

    static {
        try {
            Properties props = new Properties();
            InputStream cpr = Log.class.getResourceAsStream("/log4j2.properties");
            props.load(cpr);
            Configurator.initialize(null, "log4j2.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File screenShotFolder = new File(Reporter.getCurrentTestResult().getTestContext().getOutputDirectory());
        screenShotPath = screenShotFolder.getParent() + File.separator + "ScreenShot" + File.separator;
        screenShotFolder = new File(screenShotPath);

        if (!screenShotFolder.exists()) {
            screenShotFolder.mkdir();
        }

        File[] screenShots = screenShotFolder.listFiles();
        File screens = screenShotFolder;

        // delete files if the folder has any
        if (screenShots != null && screenShots.length > 0) {
            for (File screenShot : screenShots) {
                screenShot.delete();
            }
        }

        final Map<String, String> params = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getAllParameters();

        if (params.containsKey("consoleOutPut")) {
            Log.consoleOutPut = Boolean.parseBoolean(params.get("consoleOutPut"));
        }
    }

    /**
     * lsLog4j returns name of the logger from the current thread
     */
    public static Logger lsLog4j() {
        return LogManager.getLogger(Thread.currentThread().getName());
    }

    /**
     * callerClass method used to retrieve the Class Name
     */
    public static String callerClass() {
        return Thread.currentThread().getStackTrace()[2].getClassName();
    }

    /**
     * Method prints a custom message in the log (level=info)
     * @param description - test case custom message
     *
     */
    public static void message(String description) {
        Reporter.log(MESSAGE_HTML_BEGIN + description + MESSAGE_HTML_END);
        ExtentReporter.info(description);
        lsLog4j().log(Level.INFO, callerClass(), description, null);
    }

    /**
     * Method prints a custom message during debugging (level=info)
     * @param description - test case custom message
     *
     */
    public static void event(String description) {
        String currDate = new SimpleDateFormat("dd MMM HH:mm:ss SSS").format(Calendar.getInstance().getTime());
        Reporter.log(EVENT_HTML_BEGIN.replace("%s", getHashCode()) + currDate + " - " + description + EVENT_HTML_END);
        ExtentReporter.debug(description);
        lsLog4j().log(Level.DEBUG, callerClass(), description, null);
    }

    /**
     * Method prints a custom message during debugging (level=info)
     * @param description - test case custom message
     * @param duration - time taken
     *
     */
    public static void event(String description, long duration) {
        String currDate = new SimpleDateFormat("dd MMM HH:mm:ss SSS").format(Calendar.getInstance().getTime());
        Reporter.log(EVENT_HTML_BEGIN.replace("%s", getHashCode()) + currDate + " - <b>" + duration + "</b> - " + description + " - "
                + Thread.currentThread().getStackTrace()[2].toString() + EVENT_HTML_END);
        ExtentReporter.debug(currDate + " - <b>" + duration + "</b> - " + description + " - "
                + Thread.currentThread().getStackTrace()[2].toString());
        lsLog4j().log(Level.DEBUG, callerClass(), description, null);
    }

    /**
     * Method prints a custom message in the fail level (level=error)
     * @param description - test case custom message
     *
     */
    public static void fail(String description) {
        Reporter.log("<!--FAIL-->");
        Reporter.log(FAIL_HTML_BEGIN + description + FAIL_HTML_END1 + FAIL_HTML_END2);
        ExtentReporter.fail(description);
        ExtentReporter.logStackTrace(new AssertionError(description));
        lsLog4j().log(Level.ERROR, callerClass(), description, null);
        Assert.fail(description);
    }

    /**
     * Returns the hashcode based on the test case name and parameters
     * @return
     */
    public static String getHashCode() {
        StringBuilder parameters = new StringBuilder();
        for(Object param : Reporter.getCurrentTestResult().getParameters()) {
            parameters.append(param);
        }
        return String.valueOf((Reporter.getCurrentTestResult().getMethod().getMethodName() + parameters).hashCode());
    }
}
