/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
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
    private WebUIDriver uiDriver;
    private String filename;
    public static final String SCREENSHOT_DIR = "screenshots/";
    public static final String HTML_DIR = "htmls/";
    
    @Deprecated
    public enum Target {SCREEN, PAGE}
	
	public ScreenshotUtil() {
		uiDriver = WebUIDriver.getWebUIDriver(false);
		driver = uiDriver.getDriver();
    	if (driver == null) {
    		throw new ScenarioException("Driver has not already been created");
    	}
		
        outputDirectory = getOutputDirectory();
    }

    public ScreenshotUtil(final WebDriver driver) {
        outputDirectory = getOutputDirectory();
        this.driver = driver;
    }

    private static String getOutputDirectory() {
        return SeleniumTestsContextManager.getThreadContext().getOutputDirectory();
    }
    
    
    
    private class NamedBufferedImage {
    	public BufferedImage image;
    	public String prefix;
    	public String url = "app";
    	public String title = "app";
    	public String pageSource = "";
    	
    	/**
    	 * Creates a NamedBufferedImage based on the provided image
    	 * @param image
    	 * @param prefix
    	 */
    	public NamedBufferedImage(BufferedImage image, String prefix) {
    		this.prefix = prefix;
    		this.image = image;
    		this.url = null;
    		this.title = null;
    		this.pageSource = null;
    		
    	}
    	
    	/**
    	 * Copy the current image to an other one, changing underlying image
    	 * @return
    	 */
    	public NamedBufferedImage copy(BufferedImage image) {
    		NamedBufferedImage img = new NamedBufferedImage(image, this.prefix);
    		img.prefix = prefix;
    		img.image = image;
    		img.url = url;
    		img.title = title;
    		img.pageSource = pageSource;
    		
    		return img;
    	}
    	

        /**
         * Add information (url, source, title) to the captured image
         * Beware that these information use the current driver so driver state must reflect the provided image
         * @param bufferedImage
         */
        public NamedBufferedImage addMetaDataToImage() {

            if (SeleniumTestsContextManager.isWebTest()) {
        		try {
                    url = driver.getCurrentUrl();
                } catch (org.openqa.selenium.UnhandledAlertException ex) {
                    // ignore alert customexception
                    logger.error(ex);
                    url = driver.getCurrentUrl();
                } catch (Throwable e) {
                	// allow screenshot even if some problem occurs
                	url = "http://no/url/available";
                }

        		try {
        			title = driver.getTitle();
        		} catch (Throwable e) {
        			// allow screenshot even if some problem occurs
        			title = "No Title";
        		}
        		title = prefix == null ? title: prefix + title;
        		
        		try {
                	pageSource = driver.getPageSource();
                } catch (Throwable e) {
                	pageSource = "";
                }
        	}
        	
        	return this;
        }
        
        /**
         * When target is an element, add information relative to element
         * @return
         */
        public NamedBufferedImage addElementMetaDataToImage(WebElement element) {
        	
        	if (SeleniumTestsContextManager.isWebTest()) {
        		try {
        			url = driver.getCurrentUrl();
        		} catch (org.openqa.selenium.UnhandledAlertException ex) {
        			// ignore alert customexception
        			logger.error(ex);
        			url = driver.getCurrentUrl();
        		} catch (Throwable e) {
        			// allow screenshot even if some problem occurs
        			url = "http://no/url/available";
        		}
        		
        		try {
        			title = element.toString();
        		} catch (Throwable e) {
        			// allow screenshot even if some problem occurs
        			title = "No Title";
        		}
        		title = prefix == null ? title: prefix + title;
        		
        		try {
        			pageSource = element.getAttribute("outerHTML");
        		} catch (Throwable e) {
        			pageSource = "";
        		}
        	}
        	
        	return this;
        }
    }
    

    private SnapshotTarget targetToSnapshotTarget(Target target) {
    	if (target == Target.PAGE) {
    		return SnapshotTarget.PAGE;
    	} else {
    		return SnapshotTarget.SCREEN;
    	}
    }
    
    /**
     * Capture a picture only if SeleniumTestsContext.getCaptureSnapshot() allows it
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @return
     */
    public <T extends Object> T capture(SnapshotTarget target, Class<T> exportClass) {
    	return capture(target, exportClass, false);
    } 
    
    /**
     * @deprecated use method with SnapshotTarget signature instead
     * @param <T>
     * @param target
     * @param exportClass
     * @return
     */
    @Deprecated
    public <T extends Object> T capture(Target target, Class<T> exportClass) {
    	return capture(targetToSnapshotTarget(target), exportClass);
    }
    
    /**
     * Capture a picture
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @param force			force capture even if set to false in SeleniumTestContext. This allows PictureElement and ScreenZone to work
     * @return
     */
    
    public <T extends Object> T capture(SnapshotTarget target, Class<T> exportClass, boolean force) {
    	try {
			return capture(target, exportClass, false, force).get(0);
		} catch (IndexOutOfBoundsException e) {
			try {
				return (T)exportClass.getConstructor().newInstance();
			} catch (Exception e1) {
				return null;
			}
		}
    }
    
    /**
     * @deprecated use method with SnapshotTarget signature instead
     * @param <T>
     * @param target
     * @param exportClass
     * @param force
     * @return
     */
    @Deprecated
    public <T extends Object> T capture(Target target, Class<T> exportClass, boolean force) {
    	return capture(targetToSnapshotTarget(target), exportClass, force);
    }
    
    private void removeAlert() {
    	try {
	    	Alert alert = driver.switchTo().alert();
			alert.dismiss();
    	} catch (Exception e) {}
    }
    
    /**
     * @deprecated use method with SnapshotTarget signature instead
     */
    @Deprecated
    public <T extends Object> List<T> capture(Target target, Class<T> exportClass, boolean allWindows, boolean force) {
    	return capture(targetToSnapshotTarget(target), exportClass, allWindows, force);
    }
    
    /**
     * Capture a picture
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @param allWindows	if true, will take a screenshot for all windows (only available for browser capture)
     * @param force			force capture even if set to false in SeleniumTestContext. This allows PictureElement and ScreenZone to work
     * @return
     */
    
    public <T extends Object> List<T> capture(SnapshotTarget target, Class<T> exportClass, boolean allWindows, boolean force) {
    	
    	if (!force && (SeleniumTestsContextManager.getThreadContext() == null 
        		|| getOutputDirectory() == null 
        		|| !SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot())) {
            return new ArrayList<>();
        }
    	
    	List<NamedBufferedImage> capturedImages = new ArrayList<>();
    	LocalDateTime start = LocalDateTime.now();
    	
    	// capture desktop
    	if (target.isScreenTarget() && SeleniumTestsContextManager.isDesktopWebTest()) {
    		capturedImages.add(new NamedBufferedImage(captureDesktop(), ""));
    		
    	// capture web with scrolling
    	} else if (target.isPageTarget() && SeleniumTestsContextManager.isWebTest()) {
    		removeAlert();
    		capturedImages.addAll(captureWebPages(allWindows));
    		
    	// capture web with scrolling on the main window
    	} else if (target.isElementTarget() && SeleniumTestsContextManager.isWebTest()) {
    		removeAlert();
    		capturedImages.addAll(captureWebPages(false));
    		
	    } else if ((target.isPageTarget() || target.isElementTarget()) && SeleniumTestsContextManager.isAppTest()){
    		capturedImages.add(new NamedBufferedImage(capturePage(-1, -1), ""));
    	} else {
    		throw new ScenarioException("Capturing page is only possible for web and application tests. Capturing desktop possible for desktop web tests only");
    	}
    	
    	// if we want to capture an element only, crop the previous capture
    	if (target.isElementTarget() && target.getElement() != null && capturedImages.size() > 0) {
    		Rectangle elementPosition = target.getElement().getRect();
    		NamedBufferedImage wholeImage = capturedImages.remove(0);
    		BufferedImage elementImage = ImageProcessor.cropImage(wholeImage.image, elementPosition.x, elementPosition.y, elementPosition.width, elementPosition.height);
    		NamedBufferedImage namedElementImage = new NamedBufferedImage(elementImage, "");
    		namedElementImage.addElementMetaDataToImage(target.getElement());
    		capturedImages.add(0, namedElementImage);
    	}
    	
    	// back to page top
    	try {
    		if (target.isPageTarget()) {
    			((CustomEventFiringWebDriver)driver).scrollTop();
    		}
    	} catch (WebDriverException e) {
    		// ignore errors here.
    		// com.seleniumtests.it.reporter.TestTestLogging.testManualSteps() with HTMLUnit driver
    		// org.openqa.selenium.WebDriverException: Can't execute JavaScript before a page has been loaded!
    	}
    	
    	List<T> out = new ArrayList<>();
    	for (NamedBufferedImage capturedImage: capturedImages) {
    		if (capturedImage != null) {
		    	if (exportClass.equals(File.class)) {
		    		out.add((T)exportToFile(capturedImage.image));
		    	} else if (exportClass.equals(ScreenShot.class)) {
		    		out.add((T)exportToScreenshot(capturedImage, Duration.between(start, LocalDateTime.now()).toMillis()));
		    	} else if (exportClass.equals(String.class)) {
		    		try {
						out.add((T)ImageProcessor.toBase64(capturedImage.image));
					} catch (IOException e) {
						logger.error("ScreenshotUtil: cannot write image");
					}
		    	} else if (exportClass.equals(BufferedImage.class)) {
		    		out.add((T)capturedImage.image);
		    	}
    		}
    	}
    	
    	return out;
    }
    
    /**
     * Capture current page (either web or app page)
     * This is a wrapper around the selenium screenshot capability
     * @return
     */
    public BufferedImage capturePage(int cropTop, int cropBottom) {
        if (driver == null) {
            return null;
        }

        try {
            // Don't capture snapshot for htmlunit
            if (uiDriver != null && uiDriver.getConfig().getBrowserType() == BrowserType.HTMLUNIT) {
                return null;
            }

            TakesScreenshot screenShot = (TakesScreenshot) driver;
            
         // TEST_MOBILE
//                ((AndroidDriver<WebElement>)((CustomEventFiringWebDriver)driver).getWebDriver()).getContextHandles();
//                ((AndroidDriver<WebElement>)((CustomEventFiringWebDriver)driver).getWebDriver()).context("CHROMIUM");
         // TEST_MOBILE
            
            // android does not support screenshot from webview context, switch temporarly to native_app context to take screenshot
            if (uiDriver != null && uiDriver.getConfig().getBrowserType() == BrowserType.BROWSER) {
            	((AndroidDriver<WebElement>)((CustomEventFiringWebDriver)driver).getWebDriver()).context("NATIVE_APP");
            }

            String screenshotB64 = screenShot.getScreenshotAs(OutputType.BASE64);
            if (uiDriver != null && uiDriver.getConfig().getBrowserType() == BrowserType.BROWSER) {
            	((AndroidDriver<WebElement>)((CustomEventFiringWebDriver)driver).getWebDriver()).context("WEBVIEW");
            }
            
            BufferedImage capturedImage = ImageProcessor.loadFromB64String(screenshotB64);
            
            // crop capture by removing headers
            if (cropTop >= 0 && cropBottom >= 0) {
            	

                // in case driver already capture the whole content, do not crop anything as cropping is used to remove static headers when scrolling
                Dimension contentDimension = ((CustomEventFiringWebDriver)driver).getContentDimension();
                if (capturedImage.getWidth() == contentDimension.width && capturedImage.getHeight() == contentDimension.height) {
                	return capturedImage;
                }
            	
	            Dimension dimensions = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
	            capturedImage = ImageProcessor.cropImage(capturedImage, 0, cropTop, dimensions.getWidth(), dimensions.getHeight() - cropTop - cropBottom);
            }
            
            return capturedImage;
        } catch (Exception ex) {
            // Ignore all exceptions
            logger.error("capturePageScreenshotToString: ", ex);
        }

        return null;
    }
    
    /**
     * Capture desktop screenshot. This is not available for mobile tests
     * @return
     */
    public BufferedImage captureDesktop() {
    	
		if (SeleniumTestsContextManager.isMobileTest()) {
			throw new ScenarioException("Desktop capture can only be done on Desktop tests");
		}

		// use driver because, we need remote desktop capture when using grid mode
		String screenshotB64 =  CustomEventFiringWebDriver.captureDesktopToBase64String(SeleniumTestsContextManager.getThreadContext().getRunMode(), 
																							SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
		try {
			return ImageProcessor.loadFromB64String(screenshotB64);
		} catch (IOException e) {
			logger.error("captureDesktopToString: ", e);
		}
		return null;
    }
    
    /**
     * Captures all web pages if requested and if the browser has multiple windows / tabs opened
     * At the end, focus is on the previously selected tab/window
     * @param allWindows		if true, all tabs/windows will be returned
     * @return
     */
    private List<NamedBufferedImage> captureWebPages(boolean allWindows) {
    	 // check driver is accessible
        List<NamedBufferedImage> images = new ArrayList<>();
        
        Set<String> windowHandles;
        String currentWindowHandle;
        try {
	        windowHandles = driver.getWindowHandles();
	        currentWindowHandle = driver.getWindowHandle();
        } catch (Exception e) {
        	try {
        		images.add(new NamedBufferedImage(captureDesktop(), "Desktop")); // do not add metadata as driver may not be available
        	} catch (ScenarioException e1) {
        		logger.warn("could not capture desktop: " + e1.getMessage());
        	}
        	return images;
        }

        // capture all but the current window
        String windowWithSeleniumfocus = currentWindowHandle;
        try {
	        if (allWindows) {
	        	for (String windowHandle: windowHandles) {
	        		if (windowHandle.equals(currentWindowHandle)) {
	        			continue;
	        		}
	        		driver.switchTo().window(windowHandle);
	        		windowWithSeleniumfocus = windowHandle;
	        		images.add(new NamedBufferedImage(captureWebPage(), "").addMetaDataToImage());
	        	}
	        }
	        
	    // be sure to go back to the window we left before capture 
        } finally {
        	try {
        		// issue #228: only switch to window if we went out of it
        		if (windowWithSeleniumfocus != currentWindowHandle) {
        			driver.switchTo().window(currentWindowHandle);
        		}
        		
        		// capture current window
        		images.add(new NamedBufferedImage(captureWebPage(), "Current Window: ").addMetaDataToImage());
        		
        	} catch (Exception e) {
        		try {
            		images.add(new NamedBufferedImage(captureDesktop(), "Desktop"));
            	} catch (ScenarioException e1) {
            		logger.warn("could not capture desktop: " + e1.getMessage());
            	}
            }
        }

        return images;
    }
    
    /**
     * Captures a web page. If the browser natively returns the whole page, nothing more is done. Else (only webview is returned), we scroll down the page to get more of the page  
     * @return
     */
    private BufferedImage captureWebPage() {

    	Dimension contentDimension = ((CustomEventFiringWebDriver)driver).getContentDimension();
    	Dimension viewDimensions = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
    	Integer topPixelsToCrop = SeleniumTestsContextManager.getThreadContext().getSnapshotTopCropping();
    	Integer bottomPixelsToCrop = SeleniumTestsContextManager.getThreadContext().getSnapshotBottomCropping();

    	
    	// issue #34: prevent getting image from HTMLUnit driver
    	if (uiDriver != null && uiDriver.getConfig().getBrowserType() == BrowserType.HTMLUNIT) {
            return null;
        }
    	
    	// if cropping is automatic, get fixed header size to configure cropping
    	if (topPixelsToCrop == null) {
    		topPixelsToCrop = ((CustomEventFiringWebDriver)driver).getTopFixedHeaderSize().intValue();
    	}
    	if (bottomPixelsToCrop == null) {
    		bottomPixelsToCrop = ((CustomEventFiringWebDriver)driver).getBottomFixedFooterSize().intValue();
    	}
    	
    	int scrollY = 0;
    	int scrollX = 0;
    	
    	// when cropping, we do not crop the first header and last footer => loops computing must take it into account (contentDimension.height - topPixelsToCrop - bottomPixelsToCrop)
    	int maxLoops = (((contentDimension.height - topPixelsToCrop - bottomPixelsToCrop) / 
    			(viewDimensions.height - topPixelsToCrop - bottomPixelsToCrop)) + 1) * ((contentDimension.width / viewDimensions.width) + 1) + 3;
    	int loops = 0;
    	int currentImageHeight = 0;
    	
    	try {
    		((CustomEventFiringWebDriver)driver).scrollTop();
    	} catch (JavascriptException e) {
    		maxLoops = 1;
		}
    	
    	BufferedImage currentImage = null;
    	while (loops < maxLoops) {
			// do not crop top for the first vertical capture
			// do not crop bottom for the last vertical capture
			int cropTop = currentImageHeight != 0 ? topPixelsToCrop : 0;
			int cropBottom = currentImageHeight + (viewDimensions.height - cropTop) < contentDimension.height ? bottomPixelsToCrop : 0;
			
			// do not scroll to much so that we can crop fixed header without loosing content
			scrollY = currentImageHeight - cropTop;
			
			try {
				((CustomEventFiringWebDriver)driver).scrollTo(scrollX, scrollY);
			} catch (JavascriptException e) {}
			
			BufferedImage image = capturePage(cropTop, cropBottom);
			if (image == null) {
				logger.error("Cannot capture page");
				break;
			}
			
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
				break;
				
			// we are at the bottom but something on the right has not been captured, move to the right and go on
			} else if (currentImageHeight >= contentDimension.getHeight()) {
				scrollX += image.getWidth();
				currentImageHeight = 0;
			}

    		loops += 1;
    	}

    	return currentImage;
    	
    }
    
    /**
     * Export buffered image to file
     * @param image
     * @return
     */
    private File exportToFile(BufferedImage image) {
    	filename = HashCodeGenerator.getRandomHashCode("web");
        String filePath = getOutputDirectory() + "/" + SCREENSHOT_DIR + filename + ".png";
        FileUtility.writeImage(filePath, image);
        return new File(filePath);
    }
    
    /**
     * Export buffered image to screenshot object, adding HTML source, title, ...
     * @param image
     * @param prefix
     * @param duration
     * @return
     */
    private ScreenShot exportToScreenshot(NamedBufferedImage namedImage, long duration) {
    	ScreenShot screenShot = new ScreenShot();
    	
    	
    	File screenshotFile = exportToFile(namedImage.image);
    	
        screenShot.setLocation(namedImage.url);
        screenShot.setTitle(namedImage.title);
        
        String outputSubDirectory = new File(outputDirectory).getName();
        try {
            FileUtils.writeStringToFile(new File(outputDirectory + "/" + HTML_DIR + filename + ".html"), namedImage.pageSource);
            screenShot.setHtmlSourcePath(String.format("../%s/%s%s.html", outputSubDirectory, HTML_DIR, filename));
        } catch (IOException e) {
            logger.warn("Ex", e);
        }

    	// record duration of screenshot
    	screenShot.setDuration(duration);
    	if (screenshotFile.exists()) {
    		Path pathAbsolute = Paths.get(screenshotFile.getAbsolutePath());
	        Path pathBase = Paths.get(getOutputDirectory());

    		screenShot.setImagePath(String.format("../%s/%s", outputSubDirectory, pathBase.relativize(pathAbsolute).toString()));
    	}
		return screenShot;
    }
 
}
