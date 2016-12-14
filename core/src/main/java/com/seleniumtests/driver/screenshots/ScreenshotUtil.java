/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.driver.screenshots;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class ScreenshotUtil {
	private static final Logger logger = SeleniumRobotLogger.getLogger(ScreenshotUtil.class);

    private String outputDirectory;
    private WebDriver driver;
    private String filename;
    private static final String SCREENSHOT_DIR = "screenshots/";
    private static final String HTML_DIR = "htmls/";
	
	public ScreenshotUtil() {
        outputDirectory = getOutputDirectory();
        this.driver = WebUIDriver.getWebDriver();
    }

    public ScreenshotUtil(final WebDriver driver) {
        outputDirectory = getOutputDirectory();
        this.driver = driver;
    }
	
    public static String captureEntirePageScreenshotToString(final WebDriver driver) {
        if (driver == null) {
            return "";
        }

        try {
            // Don't capture snapshot for htmlunit
            if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.HTMLUNIT) {
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
        return SeleniumTestsContextManager.getThreadContext().getOutputDirectory();
    }

    private void handleSource(String htmlSource, final ScreenShot screenShot) {
    	String _htmlSource = htmlSource;
        if (_htmlSource == null) {
        	_htmlSource = driver.getPageSource();
        }

        if (_htmlSource != null) {
            try {
                FileUtils.writeStringToFile(new File(outputDirectory + "/" + HTML_DIR + filename + ".html"), _htmlSource);
                screenShot.setHtmlSourcePath(HTML_DIR + filename + ".html");
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
                FileUtility.writeImage(outputDirectory + "/" + SCREENSHOT_DIR + filename + ".png", byteArray);
                screenShot.setImagePath(SCREENSHOT_DIR + filename + ".png");

            }
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    private void handleTitle(String title, final ScreenShot screenShot) {
    	String _title = title;
        if (_title == null) {
            _title = driver.getTitle();
        }

        if (_title == null) {
            _title = "";
        }

        screenShot.setTitle(_title);
    }
    
    /**
     * Capture driver snapshot to file
     */
    public File captureWebPageToFile() {
    	ScreenShot screenShot = new ScreenShot();
    	filename = HashCodeGenerator.getRandomHashCode("web");
    	handleImage(screenShot);
    	if (screenShot.getImagePath() == null) {
    		return null;
    	}
    	return new File(outputDirectory + "/" + screenShot.getFullImagePath());
    }
    
    /**
	 * prend une capture d'écran
	 */
	public File captureDesktopToFile() {
		
		if (SeleniumTestsContextManager.isMobileTest()) {
			throw new ScenarioException("Desktop capture can only be done on Desktop tests");
		}
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultGraphicDevice = ge.getDefaultScreenDevice();
		Integer screenWidth = defaultGraphicDevice.getDisplayMode().getWidth();
		Integer screenHeight = defaultGraphicDevice.getDisplayMode().getHeight();
		
		// Capture the screen shot of the area of the screen defined by the rectangle
        BufferedImage bi;
		try {
			bi = new Robot().createScreenCapture(new Rectangle(screenWidth, screenHeight));
			filename = HashCodeGenerator.getRandomHashCode("web");
			File outputFile = new File(outputDirectory + "/" + SCREENSHOT_DIR + filename + ".png");
			ImageIO.write(bi, "png" , outputFile);
			return outputFile;
		} catch (AWTException e) {
			throw new ScenarioException("Cannot capture image", e);
		} catch (IOException e1) {
			throw new ScenarioException("Erreur while creating screenshot:  " + e1.getMessage(), e1);
		}
	}

    /**
     * Capture snapshot if seleniumContext is configured to do so
     * @return
     */
    public ScreenShot captureWebPageSnapshot() {

        ScreenShot screenShot = new ScreenShot();

        if (SeleniumTestsContextManager.getThreadContext() == null 
        		|| outputDirectory == null 
        		|| !SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot()) {
            return screenShot;
        }

        screenShot.setSuiteName(getSuiteName());

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
                
                try {
                	pageSource = driver.getPageSource();
                } catch (WebDriverException e) {
                	pageSource = "";
                }
            } 
            
            this.filename = HashCodeGenerator.getRandomHashCode("web");
            screenShot.setLocation(url);

            handleTitle(title, screenShot);
            handleSource(pageSource, screenShot);
            handleImage(screenShot);
        } catch (WebSessionEndedException e) {
            throw e;
        } catch (Exception ex) {
        	logger.error(ex);
        }

        return screenShot;
    }
}
