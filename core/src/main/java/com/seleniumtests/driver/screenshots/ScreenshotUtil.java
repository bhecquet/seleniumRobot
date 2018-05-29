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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.util.imaging.ImageProcessor;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.appium.java_client.android.AndroidDriver;

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
	
    public static String capturePageScreenshotToString(final WebDriver driver) {
        if (driver == null) {
            return "";
        }

        try {
            // Don't capture snapshot for htmlunit
            if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.HTMLUNIT) {
                return null;
            }

            TakesScreenshot screenShot = (TakesScreenshot) driver;
            
            // android does not support screenshot from webview context, switch temporarly to native_app context to take screenshot
            if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.BROWSER) {
            	((AndroidDriver<WebElement>)((CustomEventFiringWebDriver)driver).getWebDriver()).context("NATIVE_APP");
            }

            String screenshotB64 = screenShot.getScreenshotAs(OutputType.BASE64);
            if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.BROWSER) {
            	((AndroidDriver<WebElement>)((CustomEventFiringWebDriver)driver).getWebDriver()).context("WEBVIEW");
            }
            
            return screenshotB64;
        } catch (Exception ex) {
            // Ignore all exceptions
            logger.error(ex);
        }

        return "";
    }
    
    /**
     * take snapshot and write it to file
     * @param driver		driver used to capture snapshot
     * @param filePath		file where snapshot will be written
     * @throws IOException 
     */
    public static void capturePageScreenshotToFile(final WebDriver driver, String filePath, int cropTop, int cropBottom) throws IOException {
    	String screenshotString = capturePageScreenshotToString(driver);

        if (screenshotString != null && !screenshotString.isEmpty() && driver != null) {
            byte[] byteArray = screenshotString.getBytes();
            byte[] decodeBuffer = Base64.decodeBase64(byteArray);
            BufferedImage img = ImageProcessor.loadFromFile(decodeBuffer);
            FileUtility.writeImage(filePath, img);
            
            // in case driver already capture the whole content, do not crop anything
            Dimension contentDimension = ((CustomEventFiringWebDriver)driver).getContentDimension();
            if (img.getWidth() == contentDimension.width && img.getHeight() == contentDimension.height) {
            	return;
            }
            
            // crop capture by removing scrollbars
            Dimension dimensions = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
            BufferedImage croppedImg = ImageProcessor.cropImage(img, 0, cropTop, dimensions.getWidth(), dimensions.getHeight() - cropTop - cropBottom);
            
            FileUtility.writeImage(filePath, croppedImg);
        }
    }
    
    /**
     * Capture the whole content even if screenshot from selenium is partial. Scroll inside page to 
     * retrieve all the content
     * @param driver
     * @param filePath
     */
    public static void captureEntirePageToFile(final WebDriver driver, String filePath) {
    	Dimension contentDimension = ((CustomEventFiringWebDriver)driver).getContentDimension();
    	Dimension viewDimensions = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
    	int topPixelsToCrop = SeleniumTestsContextManager.getThreadContext().getSnapshotTopCropping();
    	int bottomPixelsToCrop = SeleniumTestsContextManager.getThreadContext().getSnapshotBottomCropping();
    	
    	if (SeleniumTestsContextManager.isAppTest()) {
    		try {
				capturePageScreenshotToFile(driver, filePath, 0, 0);
			} catch (IOException e) {
				logger.error("Cannot capture page", e);
			}
    		return;
    	}
    	
    	// issue #34: prevent getting image from HTMLUnit driver
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.HTMLUNIT) {
            return;
        }
    	
    	((CustomEventFiringWebDriver)driver).scrollTop();
    	String tmpCap = getOutputDirectory() + "/" + SCREENSHOT_DIR + "/" + HashCodeGenerator.getRandomHashCode("tmp") + ".png";
    	
    	int scrollY = 0;
    	int scrollX = 0;
    	int maxLoops = ((contentDimension.height / (viewDimensions.height - topPixelsToCrop - bottomPixelsToCrop)) + 1) * ((contentDimension.width / viewDimensions.width) + 1) + 3;
    	int loops = 0;
    	int currentImageHeight = 0;
    	
    	BufferedImage currentImage = null;
    	while (loops < maxLoops) {
    		try {
    			// do not crop top for the first vertical capture
    			// do not crop bottom for the last vertical capture
    			int cropTop = currentImageHeight != 0 ? topPixelsToCrop : 0;
    			int cropBottom = currentImageHeight + viewDimensions.height < contentDimension.height ? bottomPixelsToCrop : 0;
    			
    			// do not scroll to much so that we can crop fixed header without loosing content
    			scrollY = currentImageHeight - cropTop;
				((CustomEventFiringWebDriver)driver).scrollTo(scrollX, scrollY);
				
    			capturePageScreenshotToFile(driver, tmpCap, cropTop, cropBottom);
				BufferedImage image = ImageIO.read(new File(tmpCap));
				
				if (currentImage == null) {
					currentImage = new BufferedImage(contentDimension.getWidth(), contentDimension.getHeight(), BufferedImage.TYPE_INT_RGB);
					currentImage.createGraphics().drawImage(image, 0, 0, null);
					currentImageHeight = image.getHeight();
				} else {
					
					// crop top of the picture in case of the last vertical snapshot. It prevents duplication of content
					if (currentImageHeight + image.getHeight() > contentDimension.getHeight() || scrollX + image.getWidth() > contentDimension.getWidth()) {
						image = ImageProcessor.cropImage(image, 
								Math.max(0, image.getWidth() - (contentDimension.getWidth() - scrollX)), 
								Math.max(0, image.getHeight() - (contentDimension.getHeight() - currentImageHeight)), 
								Math.min(image.getWidth(), contentDimension.getWidth() - scrollX), 
								Math.min(image.getHeight(), contentDimension.getHeight() - currentImageHeight));
					}
					
					currentImage = ImageProcessor.concat(currentImage, image, scrollX, currentImageHeight);
					currentImageHeight += image.getHeight();
				}
				
				// all captures done, exit
				if ((currentImageHeight >= contentDimension.getHeight() && scrollX + image.getWidth() >= contentDimension.getWidth())
						|| SeleniumTestsContextManager.isAppTest()) {
					new File(tmpCap).delete();
					break;
					
				// we are at the bottom but something on the right has not been captured, move to the right and go on
				} else if (currentImageHeight >= contentDimension.getHeight()) {
					scrollX += image.getWidth();
					currentImageHeight = 0;
				}
			} catch (IOException e) {
				logger.error("Cannot capture page", e);
				break;
			}
    		loops += 1;
    	}

    	FileUtility.writeImage(filePath, currentImage);    	
    }

    private static String getSuiteName() {
    	return SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getSuite().getName();
    }

    private static String getOutputDirectory() {
        return SeleniumTestsContextManager.getThreadContext().getOutputDirectory();
    }

    private void handleSource(String htmlSource, final ScreenShot screenShot) {
    	String newHtmlSource = htmlSource;
        if (newHtmlSource == null) {
        	newHtmlSource = driver.getPageSource();
        }

        if (newHtmlSource != null) {
            try {
                FileUtils.writeStringToFile(new File(outputDirectory + "/" + HTML_DIR + filename + ".html"), newHtmlSource);
                screenShot.setHtmlSourcePath(HTML_DIR + filename + ".html");
            } catch (IOException e) {
                logger.warn("Ex", e);
            }
        }
    }

    private void handleImage(final ScreenShot screenShot) {
        try {
        	String imagePath = SCREENSHOT_DIR + filename + ".png";
        	captureEntirePageToFile(WebUIDriver.getWebDriver(), outputDirectory + "/" + imagePath);
        	
        	if (new File(outputDirectory + "/" + imagePath).exists()) {
        		screenShot.setImagePath(imagePath);
        	}
        } catch (Exception e) {
            logger.warn(e);
        }
        ((CustomEventFiringWebDriver)WebUIDriver.getWebDriver()).scrollTop();
    }

    private void handleTitle(String title, final ScreenShot screenShot) {
    	String newTitle = title;
        if (newTitle == null) {
            newTitle = driver.getTitle();
        }

        if (newTitle == null) {
            newTitle = "";
        }

        screenShot.setTitle(newTitle);
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
    	return new File(screenShot.getFullImagePath());
    }
	
	/**
	 * Take screenshot and put it in a file
	 */
	public File captureDesktopToFile() {
		
		if (SeleniumTestsContextManager.isMobileTest()) {
			throw new ScenarioException("Desktop capture can only be done on Desktop tests");
		}

		if (driver == null) {
			throw new ConfigurationException("driver is null, capture cannot be done");
		}
		
		// use driver because, we need remote desktop capture when using grid mode
		String screenshotString = (String)((CustomEventFiringWebDriver)driver).executeScript(CustomEventFiringWebDriver.NON_JS_CAPTURE_DESKTOP);
		 
		try {
			if (screenshotString != null && !screenshotString.isEmpty()) {
	            byte[] byteArray = screenshotString.getBytes();
	            filename = HashCodeGenerator.getRandomHashCode("web");
	            String filePath = outputDirectory + "/" + SCREENSHOT_DIR + filename + ".png";
	            byte[] decodeBuffer = Base64.decodeBase64(byteArray);
	            BufferedImage img = ImageProcessor.loadFromFile(decodeBuffer);
	            FileUtility.writeImage(filePath, img);
	            return new File(filePath);
			} else {
				throw new ScenarioException("Erreur while creating screenshot");
			}
		} catch (IOException e) {
			throw new ScenarioException("Erreur while creating screenshot:  " + e.getMessage(), e);
		}
		
		
	}
	
	/**
	 * Take screenshot and put it in a screenshot object
	 */
	public ScreenShot captureDesktopToScreenshot() {
		ScreenShot screenShot = new ScreenShot();
		screenShot.setTitle("Desktop");
		File screenshotFile = captureDesktopToFile();
		screenShot.setImagePath(SCREENSHOT_DIR + screenshotFile.getName());
		return screenShot;
	}
	
	/**
     * Capture snapshot if seleniumContext is configured to do so
     * @return
     */
	public ScreenShot captureWebPageSnapshot() {
		try {
			return captureWebPageSnapshots(false).get(0);
		} catch (IndexOutOfBoundsException e) {
			return new ScreenShot();
		}
	}

    /**
     * Capture browser windows to screenshot objects
     * Current window will be the last captured one so that it can be recorded in the current step (see <code>TestLogging.logScreenshot</code>)
     * If an error occurs when getting driver, desktop snapshot is taken
     * @param allWindows 	if true, all windows created by this browsing session will be captured
     * @return
     */
    public List<ScreenShot> captureWebPageSnapshots(boolean allWindows) {

        if (SeleniumTestsContextManager.getThreadContext() == null 
        		|| outputDirectory == null 
        		|| !SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot()) {
            return new ArrayList<>();
        }
        
        // check driver is accessible
        List<ScreenShot> screenshots = new ArrayList<>();
        Set<String> windowHandles;
        String currentWindowHandle;
        try {
	        windowHandles = driver.getWindowHandles();
	        currentWindowHandle = driver.getWindowHandle();
        } catch (Exception e) {
        	try {
        		screenshots.add(captureDesktopToScreenshot());
        	} catch (ScenarioException e1) {
        		logger.warn("could not capture desktop: " + e1.getMessage());
        	}
        	return screenshots;
        }

        // capture all but the current window
        try {
	        if (allWindows) {
	        	for (String windowHandle: windowHandles) {
	        		if (windowHandle.equals(currentWindowHandle)) {
	        			continue;
	        		}
	        		driver.switchTo().window(windowHandle);
	        		screenshots.add(captureWebPageToScreenshot(""));
	        	}
	        }
	        
	    // be sure to go back to the window we left before capture 
        } finally {
        	try {
        		driver.switchTo().window(currentWindowHandle);
        		
        		// capture current window
                screenshots.add(captureWebPageToScreenshot("Current Window: "));
        		
        	} catch (Exception e) {
            }
        }

        return screenshots;
    }
    
    /**
     * Capture current browser window to screenshot object 
     * @return
     */
    private ScreenShot captureWebPageToScreenshot(String titlePrefix) {
    	ScreenShot screenShot = new ScreenShot();
    	screenShot.setSuiteName(getSuiteName());

        try {
            String url = "app";
            String title = titlePrefix + "app";
            String pageSource = "";
            if (SeleniumTestsContextManager.getThreadContext().getTestType().family().equals(TestType.WEB)) {
            	try {
                    url = driver.getCurrentUrl();
                } catch (org.openqa.selenium.UnhandledAlertException ex) {

                    // ignore alert customexception
                    logger.error(ex);
                    url = driver.getCurrentUrl();
                }

                title = titlePrefix + driver.getTitle();
                
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
