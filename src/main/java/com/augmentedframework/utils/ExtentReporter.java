package com.augmentedframework.utils;

import java.io.File;

import java.util.HashMap;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

/**
 * ExtentReport provides an interactive and detailed test execution report.
 *
 * @author YSabato
 * @version 1.0
 * @since 01/01/2020
 */
public class ExtentReporter {
    private static ExtentReports report = null;
    private static HashMap<Integer, ExtentTest> tests = new HashMap<Integer, ExtentTest>();
    private static final File configFile = new File(System.getProperty("user.dir") + File.separator + "ReportConfig.xml-Review");

    /**
     * Returns test instance of started test otherwise
     * creates new with empty description
     *
     * @return {@link ExtentTest} - ExtentTest Instance
     */
    private static ExtentTest getTest() {
        return startTest("");
    }

    /**
     * Method logs the given message to the reporter at INFO level
     *
     * @param message
     */
    public static void info(String message) {
        getTest().log(LogStatus.INFO, message);
    }

    /**
     * Method logs the given message to the reporter at DEBUG level
     *
     * @param event
     */
    public static void debug(String event) {
        getTest().log(LogStatus.UNKNOWN, event);
    }

    /**
     * To log the given message to the reporter at PASS level
     *
     * @param passMessage
     */
    public static void pass(String passMessage) {
        getTest().log(LogStatus.PASS, "<font color=\"green\">" + passMessage + "</font>");
    }

    /**
     * Method logs the given message to the reporter at FAIL level
     *
     * @param failMessage
     */
    public static void fail(String failMessage) {
        getTest().log(LogStatus.FAIL, "<font color=\"red\">" + failMessage + "</font>");
    }

    /**
     * Forms a unique test name in the format
     * "PackageName.ClassName<>MethodName"
     *
     * @param iTestResult - iTestResult
     * @return String - test name and method
     */
    private static String getTestName(ITestResult iTestResult) {
        String className = iTestResult.getTestClass().getRealClass().getName();
        String methodName = iTestResult.getName();
        return className + "<>" + methodName;
    }

    /**
     * Method starts the test execution annotated with @BeforeClass, which initializes the Extent Reports object.
     * This method sets up the report by specifying the output file location and initializing the test case.
     * Returns the test instance if the test has already been started
     *
     * @param description - test case description
     * @return {@link ExtentTest} - ExtentTest Instance
     */
    private static ExtentTest startTest(String description) {
        ExtentTest test = null;
        ITestResult iTestResult = Reporter.getCurrentTestResult();
        String testName = iTestResult != null ? getTestName(iTestResult) : Thread.currentThread().getName();
        Integer hashCode = iTestResult != null ? iTestResult.hashCode() : Thread.currentThread().hashCode();
        if (tests.containsKey(hashCode)) {
            test = tests.get(hashCode);
            if (description != null && !description.isEmpty()) {
                test.setDescription(description);
            }
        } else {
            if (iTestResult == null || !iTestResult.getMethod().isTest()) {
                test = new ExtentTest(testName, description);
            } else {
                test = getReportInstance(iTestResult).startTest(testName, description).assignCategory(iTestResult.getMethod().getGroups());
                tests.put(hashCode, test);
            }
        }
        return test;
    }

    /**
     * Returns an ExtentReports instance if already exists otherwise
     * creates and returns new
     *
     * @param iTestResult - iTestResult
     * @return {@link ExtentReports} - Extent report instance
     */
    private static synchronized ExtentReports getReportInstance(ITestResult iTestResult) {
        if (report == null) {
            String reportFilePath = new File(iTestResult.getTestContext().getOutputDirectory()).getParent() + File.separator + "ExtentReport.html";
            report = new ExtentReports(reportFilePath, true);
            if (configFile.exists()) {
                report.loadConfig(configFile);
            }
        }
        return report;
    }

    /**
     * Print stack trace of the given error/exception
     *
     * @param trace - trace
     */
    public static void logStackTrace(Throwable trace) {
        if (trace instanceof SkipException) {
            getTest().log(LogStatus.SKIP, "<div class=\"stacktrace\">" + ExceptionUtils.getStackTrace(trace) + "</div>");
        } else {
            getTest().log(LogStatus.FAIL, "<div class=\"stacktrace\">" + ExceptionUtils.getStackTrace(trace) + "</div>");
        }
    }
}
