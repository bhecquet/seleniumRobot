package com.seleniumtests;

import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;

import java.io.File;
import java.io.IOException;

public class ParentTest {

    protected static final Logger logger = SeleniumRobotLogger.getLogger(ParentTest.class);

    @AfterMethod(groups={"ut", "it", "ut context2", "ie"}, alwaysRun=true)
    public void resetTest(ITestContext testContext) {
        System.clearProperty("applicationName");
        resetTestNGResultAndLogger();
        OSUtility.resetInstalledBrowsersWithVersion();

        File outputDirectory = new File(testContext.getOutputDirectory()).getParentFile();
        if ("true".equals(System.getProperty(SeleniumRobotLogger.MAVEN_EXECUTION))) {
            try {
                logger.info("delete output directory: {}", outputDirectory);
                FileUtils.deleteDirectory(outputDirectory);
            } catch (IOException e) {
                logger.error("Cannot delete output directory: {}: {}", outputDirectory, e.getMessage());
            }
        }
    }

    public static void resetTestNGResultAndLogger() {
        resetCurrentTestResult();

        try {
            SeleniumRobotLogger.reset();
        } catch (IOException e) {
            logger.error("Cannot delete log file {}", e.getMessage());
        }
    }


    public static void resetCurrentTestResult() {
        //Reporter.setCurrentTestResult(null); // do not reset, TestNG do this for us
    }
}
