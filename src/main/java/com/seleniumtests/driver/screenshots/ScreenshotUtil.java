/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.driver.screenshots;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;

public class ScreenshotUtil {
	private static final Logger logger = TestLogging.getLogger(ScreenshotUtil.class);

	private String suiteName;
    private String outputDirectory;
    private WebDriver driver;
    private String filename;
    private static final String SCREENSHOT_DIR = "/screenshots/";
	
	public ScreenshotUtil() {
        suiteName = getSuiteName();
        outputDirectory = getOutputDirectory();
        this.driver = WebUIDriver.getWebDriver();
    }

    public ScreenshotUtil(final WebDriver driver) {
        suiteName = getSuiteName();
        outputDirectory = getOutputDirectory();
        this.driver = driver;
    }
	
    public static String captureEntirePageScreenshotToString(final WebDriver driver) {
        if (driver == null) {
            return "";
        }

        try {

            // Don't capture snapshot for htmlunit
            if (WebUIDriver.getWebUIDriver().getBrowser().equalsIgnoreCase(BrowserType.HTMLUNIT.getBrowserType())) {
                return null;
            }

            TakesScreenshot screenShot = (TakesScreenshot) driver;
            return screenShot.getScreenshotAs(OutputType.BASE64);
        } catch (Exception ex) {

            // Ignore all exceptions
            logger.error(ex);
        }

        return "";
    }

    private static String getSuiteName() {
    	return SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getSuite().getName();
    }

    private static String getOutputDirectory() {
        return SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getOutputDirectory();
    }

    private void handleSource(String htmlSource, final ScreenShot screenShot) {
        if (htmlSource == null) {
            htmlSource = driver.getPageSource();
        }

        if (htmlSource != null) {
            try {
                FileUtils.writeStringToFile(new File(outputDirectory + "/htmls/" + filename + ".html"), htmlSource);
                screenShot.setHtmlSourcePath(suiteName + "/htmls/" + filename + ".html");
            } catch (IOException e) {
                logger.warn("Ex", e);
            }
        }
    }

    private void handleImage(final ScreenShot screenShot) {
        try {
            String screenshotString = captureEntirePageScreenshotToString(WebUIDriver.getWebDriver());

            if (screenshotString != null && !screenshotString.isEmpty()) {
                byte[] byteArray = screenshotString.getBytes();
                FileUtility.writeImage(outputDirectory + SCREENSHOT_DIR + filename + ".png", byteArray);
                screenShot.setImagePath(suiteName + SCREENSHOT_DIR + filename + ".png");

            }
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    private void handleTitle(String title, final ScreenShot screenShot) {
        if (title == null) {
            title = driver.getTitle();
        }

        if (title == null) {
            title = "";
        }

        screenShot.setTitle(title);
    }

    public ScreenShot captureWebPageSnapshot() {

        ScreenShot screenShot = new ScreenShot();

        if (SeleniumTestsContextManager.getThreadContext() == null || outputDirectory == null) {
            return screenShot;
        }

        screenShot.setSuiteName(this.suiteName);

        try {
            String url = "app";
            String title = "app";
            String pageSource = "";
            if (SeleniumTestsContextManager.getThreadContext().getTestType().family().equals(TestType.WEB)) {
            	try {
                    url = driver.getCurrentUrl();
                } catch (org.openqa.selenium.UnhandledAlertException ex) {

                    // ignore alert customexception
                    logger.error(ex);
                    url = driver.getCurrentUrl();
                }

                title = driver.getTitle();
                pageSource = driver.getPageSource();
            } 
            
            this.filename = HashCodeGenerator.getRandomHashCode("web");
            screenShot.setLocation(url);

            handleTitle(title, screenShot);
            handleSource(pageSource, screenShot);
            if (SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot()) {
                handleImage(screenShot);
            }
        } catch (WebSessionEndedException e) {
            throw e;
        } catch (Exception ex) {
        	logger.error(ex);
        }

        if (SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot()) {
            SeleniumTestsContextManager.getThreadContext().addScreenShot(screenShot);
        }

        return screenShot;
    }

    /**
     * Used by DriverExceptionListener, don't log the customexception but put it into context.
     */
    public void capturePageSnapshotOnException() {
        Boolean capture = SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot();
        SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(true);
        captureWebPageSnapshot();
        SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(capture);

        if (!SeleniumTestsContextManager.getThreadContext().getScreenshots().isEmpty()) {
            ((LinkedList<ScreenShot>) SeleniumTestsContextManager.getThreadContext().getScreenshots()).getLast().setException(true);
        }
    }

    public static void captureSnapshot(final String messagePrefix) {
        if (SeleniumTestsContextManager.getThreadContext() != null
                && SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot()
                && getOutputDirectory() != null) {
            String filename = HashCodeGenerator.getRandomHashCode("HtmlElement");
            StringBuilder sbMessage = new StringBuilder();
            try {
                String img = ScreenshotUtil.captureEntirePageScreenshotToString(WebUIDriver.getWebDriver());
                if (img == null) {
                    return;
                }

                byte[] byteArray = img.getBytes();
                if (byteArray != null && byteArray.length > 0) {
                    String imgFile = SCREENSHOT_DIR + filename + ".png";
                    FileUtility.writeImage(getOutputDirectory() + imgFile, byteArray);

                    ScreenShot screenShot = new ScreenShot();
                    String imagePath = getSuiteName() + imgFile;
                    screenShot.setImagePath(imagePath);
                    SeleniumTestsContextManager.getThreadContext().addScreenShot(screenShot);
                    sbMessage.append(messagePrefix + ": <a href='" + imagePath
                            + "' class='lightbox'>Application Snapshot</a>");
                    TestLogging.logWebOutput(null, sbMessage.toString(), false);
                }
            } catch (WebSessionEndedException ex) {
                throw ex;
            } catch (Exception e) {
                logger.error(e);
            }

        }
    }
}
