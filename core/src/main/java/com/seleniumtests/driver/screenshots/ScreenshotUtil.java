/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.util.StringUtility;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.bidi.browsingcontext.BrowsingContext;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.DevToolsException;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v142.page.Page;
import org.openqa.selenium.devtools.v142.page.Page.CaptureScreenshotFormat;
import org.openqa.selenium.devtools.v142.page.Page.GetLayoutMetricsResponse;
import org.openqa.selenium.devtools.v142.page.model.Viewport;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.HashCodeGenerator;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.imaging.ImageProcessor;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import org.openqa.selenium.firefox.HasFullPageScreenshot;
import org.openqa.selenium.remote.RemoteWebElement;

import javax.imageio.ImageIO;

public class ScreenshotUtil {

	
	private static final Logger logger = SeleniumRobotLogger.getLogger(ScreenshotUtil.class);

    private final String outputDirectory;
    private final CustomEventFiringWebDriver driver;
    private WebUIDriver uiDriver;

	public ScreenshotUtil() {
		uiDriver = WebUIDriver.getWebUIDriver(false);
		driver = uiDriver.getDriver();
    	if (driver == null) {
    		throw new ScenarioException("Driver has not already been created");
    	}
		
        outputDirectory = getOutputDirectory();
    }

    public ScreenshotUtil(final CustomEventFiringWebDriver driver) {
        outputDirectory = getOutputDirectory();
        this.driver = driver;
    }

    private static String getOutputDirectory() {
        return SeleniumTestsContextManager.getThreadContext().getOutputDirectory();
    }
    
    
    
    private class NamedBufferedImage {
    	private final BufferedImage image;
    	private final String prefix;
    	private String url;
    	private String title;
    	private String pageSource;
    	
    	/**
    	 * Creates a NamedBufferedImage based on the provided image
    	 * @param image		the buffured image
    	 * @param prefix	prefix that will be set on name
    	 */
    	public NamedBufferedImage(BufferedImage image, String prefix) {
    		this.prefix = prefix;
    		this.image = image;
    		this.url = null;
    		this.title = null;
    		this.pageSource = null;
    		
    	}
    	

        /**
         * Add information (url, source, title) to the captured image
         * Beware that this information use the current driver so driver state must reflect the provided image
         */
        public NamedBufferedImage addMetaDataToImage() {

            if (driver.isWebTest()) {
        		try {
                    url = driver.getCurrentUrl();
                } catch (org.openqa.selenium.UnhandledAlertException ex) {
                    // ignore alert customexception
                    logger.error(ex);
                    url = driver.getCurrentUrl();
                } catch (Exception e) {
                	// allow screenshot even if some problem occurs
                	url = "http://no/url/available";
                }

        		try {
        			title = driver.getTitle();
        		} catch (Exception e) {
        			// allow screenshot even if some problem occurs
        			title = "No Title";
        		}
        		title = prefix == null ? title: prefix + title;
        		
        		try {
                	pageSource = getFullPageSource(0, new HashMap<>());
                } catch (Exception e) {
                	pageSource = "";
                }
        	}
        	
        	return this;
        }

		/**
		 * Get the page source
		 * it will go through frame and replace the frame "src" attribute with srcdoc so that whole content is inside a single file
		 * @param depth		depth of frames
		 * @param frameMap	id of the frame, related to it's index in the document, so that we can restore the context at the end of the method
		 * @return	the full DOM
		 */
		private String getFullPageSource(int depth, Map<String, Integer> frameMap) {

			List<String> contextPath = new ArrayList<>();
			if (depth == 0) {
				RemoteWebElement currentContext = (RemoteWebElement) driver.getOriginalDriver().switchTo().activeElement();
				contextPath.add(currentContext.getId());
				RemoteWebElement currentFrame = currentContext;
				RemoteWebElement parentFrame = null;
				while (!currentFrame.equals(parentFrame)) {
					if (parentFrame != null) {
						currentFrame = parentFrame;
					}
					driver.switchTo().parentFrame();
					parentFrame = (RemoteWebElement) driver.getOriginalDriver().switchTo().activeElement();
					contextPath.add(parentFrame.getId());
				}
				// remove the root document from list
				contextPath.removeAll(List.of(parentFrame.getId()));
				contextPath = contextPath.reversed(); // to go top -> down
			}

			// go through frames to extract content
			String mainDom = (String) driver.executeScript("return document.documentElement.outerHTML;");
			if (mainDom != null && mainDom.contains("<iframe")) {
				List<WebElement> iframes = driver.getOriginalDriver().findElements(By.tagName("iframe"));

				for (WebElement iframe : iframes) {

					try {
						String iframeHtml = iframe.getAttribute("outerHTML");
						driver.switchTo().frame(iframe);
						frameMap.put(((RemoteWebElement) driver.getOriginalDriver().switchTo().activeElement()).getId(), iframes.indexOf(iframe));
						String frameSource = getFullPageSource(depth + 1, frameMap);
						driver.switchTo().parentFrame();
						mainDom = mainDom.replace(iframeHtml, iframeHtml.replaceAll("src=.*? ", "srcdoc=\"" + StringUtility.encodeString(frameSource, "xml") + "\" "));
					} catch (Exception e) {
						logger.error("Could not get frame source", e);
					}
				}
			}

			// restore frame context
			if (depth == 0) {
				for (String elementId : contextPath) {
					try {
						driver.switchTo().frame(frameMap.get(elementId));
					} catch (Exception e) {
						logger.error("Could not switch to frame '{}' while restoring context, go back to default content", elementId, e);
						driver.switchTo().defaultContent();
					}
				}
			}

			return mainDom;
		}
        
        /**
         * When target is an element, add information relative to element
         * @return this
         */
        public NamedBufferedImage addElementMetaDataToImage(WebElement element) {
        	
        	if (driver.isWebTest()) {
        		try {
        			url = driver.getCurrentUrl();
        		} catch (org.openqa.selenium.UnhandledAlertException ex) {
        			// ignore alert customexception
        			logger.error(ex);
        			url = driver.getCurrentUrl();
        		} catch (Exception e) {
        			// allow screenshot even if some problem occurs
        			url = "http://no/url/available";
        		}
        		
        		try {
        			title = element.toString();
        		} catch (Exception e) {
        			// allow screenshot even if some problem occurs
        			title = "No Title";
        		}
        		title = prefix == null ? title: prefix + title;
        		
        		try {
        			pageSource = element.getDomProperty("outerHTML");
        		} catch (Exception e) {
        			pageSource = "";
        		}
        	}
        	
        	return this;
        }
    }
    
    /**
     * Capture a picture only if SeleniumTestsContext.getCaptureSnapshot() allows it
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @return	the screenshot or null if user requested not to take screenshots
     */
    public <T> T capture(SnapshotTarget target, Class<T> exportClass) {
    	return capture(target, exportClass, false);
    } 
    
    public <T> T capture(SnapshotTarget target, Class<T> exportClass, int scrollDelay) {
    	return capture(target, exportClass, false, scrollDelay);
    } 
    
    /**
     * Capture a picture
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @param force			force capture even if set to false in SeleniumTestContext. This allows PictureElement and ScreenZone to work
     * @return the screenshot or null if user requested not to take screenshots and force is "false"
     */
    public <T> T capture(SnapshotTarget target, Class<T> exportClass, boolean force) {
    	return capture(target, exportClass, force, 0);
    }
    
    /**
     * Capture a picture
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @param force			force capture even if set to false in SeleniumTestContext. This allows PictureElement and ScreenZone to work
     * @param scrollDelay	time in ms between the scrolling (when it's needed) and effective capture. A higher value means we have chance all picture have been loaded (with progressive loading)
     * 						but capture take more time
     * @return the screenshot or null if user requested not to take screenshots and force is "false"
     */
    public <T> T capture(SnapshotTarget target, Class<T> exportClass, boolean force, int scrollDelay) {
    	try {
			return capture(target, exportClass, false, force, scrollDelay).get(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
    }

    
    private void removeAlert() {
    	try {
	    	Alert alert = driver.switchTo().alert();
			alert.dismiss();
		} catch (WebSessionEndedException e) {
			throw e;
    	} catch (Exception e) {
    		// nothing to do
    	}
    }
    
    /**
     * Capture a picture
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @param allWindows	if true, will take a screenshot for all windows (only available for browser capture)
     * @param force			force capture even if set to false in SeleniumTestContext. This allows PictureElement and ScreenZone to work
     * @return				The image in the requested format
     */
    
    public <T> List<T> capture(SnapshotTarget target, Class<T> exportClass, boolean allWindows, boolean force) {
    	return capture(target, exportClass, allWindows, force, 0);
    }
    
    /**
     * Capture a picture
     * @param target		which picture to take, screen or page.
     * @param exportClass	The type of export to perform (File, ScreenShot, String, BufferedImage)
     * @param allWindows	if true, will take a screenshot for all windows (only available for browser capture)
     * @param force			force capture even if set to false in SeleniumTestContext. This allows PictureElement and ScreenZone to work
     * @param scrollDelay	time in ms between the scrolling (when it's needed) and effective capture. A higher value means we have chance all picture have been loaded (with progressive loading)
     * 						but capture take more time
     * @return				The image in the requested format
     */
    public <T> List<T> capture(SnapshotTarget target, Class<T> exportClass, boolean allWindows, boolean force, int scrollDelay) {
    	
    	if (!force && (SeleniumTestsContextManager.getThreadContext() == null 
        		|| outputDirectory == null 
        		|| !SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot())) {
            return new ArrayList<>();
        }

    	Instant start = Instant.now();
    	List<NamedBufferedImage> capturedImages = captureAllImages(target, allWindows, scrollDelay);
    	
    	return exportBufferedImages(exportClass, start, capturedImages);

    }

	/**
	 * Export the captured images to the given class
	 * @param exportClass		class to export to: File, Screenshot, B64 String, BuffuredImage
	 * @param start				instant when robot started taking capture
	 * @param capturedImages	list of already captured images
	 * @return the image in the new format
	 */
	private <T> List<T> exportBufferedImages(Class<T> exportClass, Instant start, List<NamedBufferedImage> capturedImages) {
		List<T> out = new ArrayList<>();
    	for (NamedBufferedImage capturedImage: capturedImages) {
    		if (capturedImage != null) {
		    	if (exportClass.equals(File.class)) {
		    		out.add((T)exportToFile(capturedImage.image));
		    	} else if (exportClass.equals(ScreenShot.class)) {
		    		out.add((T)exportToScreenshot(capturedImage, Duration.between(start, Instant.now()).toMillis()));
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
	 * Capture all images and returns them as BufferedImages
	 * @param target		Target of the snapshot: screen, webview, page, element
	 * @param allWindows	whether to capture all windows
	 * @return the captures
	 */
	private List<NamedBufferedImage> captureAllImages(SnapshotTarget target, boolean allWindows, int scrollDelay) {
		List<NamedBufferedImage> capturedImages = new ArrayList<>();
    	
    	// capture desktop
    	if (target.isScreenTarget() && SeleniumTestsContextManager.isDesktopWebTest()) {
    		capturedImages.add(new NamedBufferedImage(captureDesktop(), ""));
    		
    	// capture desktop
    	} else if (target.isMainScreenTarget() && SeleniumTestsContextManager.isDesktopWebTest()) {
    		capturedImages.add(new NamedBufferedImage(captureDesktop(true), ""));
    			
    	// capture web with scrolling
    	} else if (target.isPageTarget() && driver != null && driver.isWebTest()) {
    		removeAlert();
    		capturedImages.addAll(captureWebPages(allWindows, scrollDelay));
    		
    	// capture web without scrolling on the main window
    	} else if (target.isViewportTarget() && driver != null && driver.isWebTest()) {
    		removeAlert();
    		target.setSnapshotRectangle(new Rectangle(driver.getScrollPosition(), driver.getViewPortDimensionWithoutScrollbar()));
    		capturedImages.add(new NamedBufferedImage(capturePage(0, 0), "")); // allow removing of scrollbar (a negative value would not remove it)
    		
    	// capture web with scrolling on the main window
    	} else if (target.isElementTarget() && driver != null && driver.isWebTest()) {
    		removeAlert();
    		try {
				double aspectRatio = driver.getDeviceAspectRatio();
    			target.setSnapshotRectangle(getElementRectangleWithAR(target.getElement(), aspectRatio));
    		} catch (WebDriverException e) {
				throw new ScenarioException(String.format("Cannot check element %s snapshot as it is not available", target.getElement()));
			}
    		capturedImages.addAll(captureWebPages(false, scrollDelay));
    		
	    } else if ((target.isPageTarget() || target.isElementTarget() || target.isViewportTarget()) && SeleniumTestsContextManager.isAppTest()){
    		capturedImages.add(new NamedBufferedImage(capturePage(-1, -1), ""));
    		
    	} else {
    		throw new ScenarioException("Capturing page is only possible for web and application tests. Capturing desktop possible for desktop web tests only");
    	}
    	
    	// if we want to capture an element only, crop the previous capture
    	if (target.isElementTarget() && target.getElement() != null && !capturedImages.isEmpty()) {
    		Rectangle elementPosition = target.getSnapshotRectangle();
    		
    		NamedBufferedImage wholeImage = capturedImages.remove(0);

    		BufferedImage elementImage = ImageProcessor.cropImage(wholeImage.image,
                    elementPosition.x,
					elementPosition.y,
					elementPosition.width,
					elementPosition.height);
    		NamedBufferedImage namedElementImage = new NamedBufferedImage(elementImage, "");
    		namedElementImage.addElementMetaDataToImage(target.getElement());
    		capturedImages.add(0, namedElementImage);
    	}
		return capturedImages;
	}
    
    /**
     * Capture current page (either web or app page)
     * This is a wrapper around the selenium screenshot capability
     * @return the image
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

            String screenshotB64 = driver.getScreenshotAs(OutputType.BASE64);
            if (screenshotB64 == null) {
            	logger.warn("capture cannot be done");
            	return null;
            }
            
            BufferedImage capturedImage = ImageProcessor.loadFromB64String(screenshotB64);
            
            // crop capture by removing headers
            if (cropTop >= 0 && cropBottom >= 0) {
            	

                // in case driver already capture the whole content, do not crop anything as cropping is used to remove static headers when scrolling
                Dimension contentDimension = driver.getContentDimension();
                if (capturedImage.getWidth() == contentDimension.width && capturedImage.getHeight() == contentDimension.height) {
                	return capturedImage;
                }
            	
	            Dimension dimensions = driver.getViewPortDimensionWithoutScrollbar();
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
     * @return the image
     */
    public BufferedImage captureDesktop() {
    	return captureDesktop(false);
    }
    
    /**
     * Capture desktop screenshot. This is not available for mobile tests
     * @param onlyMainScreen	only capture the default (or 'main') screen
     * @return the image
     */
    public BufferedImage captureDesktop(boolean onlyMainScreen) {
    	
		if (SeleniumTestsContextManager.isMobileTest()) {
			throw new ScenarioException("Desktop capture can only be done on Desktop tests");
		}

		// use driver because, we need remote desktop capture when using grid mode
		String screenshotB64 =  CustomEventFiringWebDriver.captureDesktopToBase64String(onlyMainScreen,
																					SeleniumTestsContextManager.getThreadContext().getRunMode(),
																					SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector()
																					);
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
     * @return list of images
     */
    private List<NamedBufferedImage> captureWebPages(boolean allWindows, int scrollDelay) {
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
        		logger.warn("could not capture desktop: {}", e1.getMessage());
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
	        		images.add(new NamedBufferedImage(captureWebPage(scrollDelay, windowHandle), "").addMetaDataToImage());
	        	}
	        }
	        
	    // be sure to go back to the window we left before capture 
        } finally {
        	try {
        		// issue #228: only switch to window if we went out of it
        		if (!windowWithSeleniumfocus.equals(currentWindowHandle)) {
        			driver.switchTo().window(currentWindowHandle);
        		}
        		
        		// capture current window
        		images.add(new NamedBufferedImage(captureWebPage(scrollDelay, currentWindowHandle), "Current Window: ").addMetaDataToImage());
        		
        	} catch (Exception e) {
        		try {
            		images.add(new NamedBufferedImage(captureDesktop(), "Desktop"));
            	} catch (ScenarioException e1) {
            		logger.warn("could not capture desktop: {}", e1.getMessage());
            	}
            }
        }

        return images;
    }

	public BufferedImage captureWebPageFullPage() throws IOException {
		if (!(driver.getOriginalDriver() instanceof HasFullPageScreenshot)) {
			throw new CustomSeleniumTestsException("Full page capture can only be done on Firefox tests");
		}

		File screenshot = ((FirefoxDriver) driver.getOriginalDriver()).getFullPageScreenshotAs(OutputType.FILE);
		return ImageIO.read(screenshot);

	}

    /**
     * Capture web page using the Chrome DevTools Protocol
     * @return the image
     */
    public BufferedImage captureWebPageUsingCDP(String windowHandle) throws IOException {
    	if (!(driver.getWebDriver() instanceof HasDevTools)) {
    		throw new DevToolsException("CDP not implemented for " + driver.getClass().toString());
    	}
    	
    	DevTools devTools = ((HasDevTools) driver.getWebDriver()).getDevTools();
    	
		devTools.createSession(windowHandle);
		try {
			GetLayoutMetricsResponse layout = devTools.send(Page.getLayoutMetrics());
			String b64Image = devTools.send(Page.captureScreenshot(Optional.of(CaptureScreenshotFormat.PNG), Optional.of(100), Optional.of(new Viewport(0, 0, layout.getCssContentSize().getWidth(), layout.getCssContentSize().getHeight(), 1)), Optional.of(true), Optional.of(true), Optional.of(true)));
			return ImageProcessor.loadFromB64String(b64Image);
		} finally {
			devTools.disconnectSession();
		}
    }

	public BufferedImage captureWebPageUsingBidi(String windowHandle) throws IOException {
		if (!(driver.getWebDriver() instanceof HasBiDi))  {
			throw new DevToolsException("Bidi not implemented for " + driver.getClass().toString());
		}

		BrowsingContext browsingContext = new BrowsingContext(driver.getWebDriver(), windowHandle);
		String screenshot = browsingContext.captureScreenshot();

		return ImageProcessor.loadFromB64String(screenshot);
	}
    
    /**
     * Captures a web page. If the browser natively returns the whole page, nothing more is done. Else (only webview is returned), we scroll down the page to get more of the page
     * On chromium browsers, CDP will be used if possible
     * If you do not want to use CDP, then, set a scrollDelay to a positive value
     *   
     * @param scrollDelay	time in ms to wait between scrolling and snapshot. 
     * @param windowHandle	the window handle of the page to capture
     */
    public BufferedImage captureWebPage(int scrollDelay, String windowHandle) {

		// BiDi screenshot only takes the viewport
//    	if (driver.getWebDriver() instanceof HasBiDi && scrollDelay == 0) {
//    		try {
//    			return captureWebPageUsingBidi(windowHandle);
//    		} catch (IOException e) {
//    			logger.warn("Error getting screenshot with CDP, using standard method: " + e.getMessage());
//    		} catch (DevToolsException e) {
//    			// ignore and use the standard method
//    		}
//    	}
		try {
			return captureWebPageFullPage();
		} catch (CustomSeleniumTestsException e) {
			//
		} catch (Exception e) {
			logger.warn("Error getting screenshot with Firefox, using standard method: {}", e.getMessage());
		}

    	if (driver.getWebDriver() instanceof HasDevTools && scrollDelay == 0) {
    		try {
    			return captureWebPageUsingCDP(windowHandle);
    		} catch (IOException e) {
    			logger.warn("Error getting screenshot with CDP, using standard method: {}", e.getMessage());
    		} catch (DevToolsException e) {
    			// ignore and use the standard method
    		}
    	}

    	Dimension contentDimension = driver.getContentDimension();
    	Dimension viewDimensions = driver.getViewPortDimensionWithoutScrollbar();
    	Integer topPixelsToCrop = SeleniumTestsContextManager.getThreadContext().getSnapshotTopCropping();
    	Integer bottomPixelsToCrop = SeleniumTestsContextManager.getThreadContext().getSnapshotBottomCropping();
    	double devicePixelRatio = driver.getDeviceAspectRatio();

    	
    	// issue #34: prevent getting image from HTMLUnit driver
    	if (uiDriver != null && uiDriver.getConfig().getBrowserType() == BrowserType.HTMLUNIT) {
            return null;
        }
    	
    	// if cropping is automatic, get fixed header size to configure cropping
    	if (topPixelsToCrop == null) {
    		topPixelsToCrop = driver.getTopFixedHeaderSize().intValue();
    	}
    	if (bottomPixelsToCrop == null) {
    		bottomPixelsToCrop = driver.getBottomFixedFooterSize().intValue();
    	}
    	
    	int scrollY = 0;
    	int scrollX = 0;
    	
    	// when cropping, we do not crop the first header and last footer => loops computing must take it into account (contentDimension.height - topPixelsToCrop - bottomPixelsToCrop)
    	int maxLoops = (((contentDimension.height - topPixelsToCrop - bottomPixelsToCrop) / 
    			(viewDimensions.height - topPixelsToCrop - bottomPixelsToCrop)) + 1) * ((contentDimension.width / viewDimensions.width) + 1) + 3;
    	
    	// if a modal is displayed, do not capture more than the viewport
    	if (driver.isModalDisplayed()) {
    		maxLoops = 1;
    	}
    	
    	int loops = 0;
    	int currentImageHeight = 0;
  
    	// issue #435: be sure maxLoops is positive (could be negative in presence of fixed modal on long page)
    	maxLoops = Math.max(1, maxLoops);
    	
    	try {
    		driver.scrollTop();
    	} catch (JavascriptException e) {
    		maxLoops = 1;
		}

    	BufferedImage currentImage = null;
    	while (loops < maxLoops) {
			// do not crop top for the first vertical capture
			// do not crop bottom for the last vertical capture of if maxLoops == 1 (only a single capture)
			int cropTop = currentImageHeight != 0 ? topPixelsToCrop : 0;
			int cropBottom = currentImageHeight + (viewDimensions.height - cropTop) < contentDimension.height && maxLoops != 1 ? bottomPixelsToCrop : 0;
			
			// do not scroll to much so that we can crop fixed header without loosing content
			scrollY = currentImageHeight - cropTop;
			
			try {
				driver.scrollTo((int)(scrollX / devicePixelRatio), (int)(scrollY / devicePixelRatio));
			} catch (JavascriptException e) {
				// ignore javascript errors
			}
			
			// wait some time (if > 0) to let picture loading
			WaitHelper.waitForMilliSeconds(scrollDelay);
			
			BufferedImage image = capturePage(cropTop, cropBottom);
			if (image == null) {
				logger.error("Cannot capture page");
				break;
			}
			
			if (currentImage == null) {
				// issue #435: in case of a single capture, contentDimension may be different from viewDimension (with a displayed modal), create an image with viewDimension
				if (maxLoops == 1) {
					currentImage = new BufferedImage(viewDimensions.getWidth(), viewDimensions.getHeight(), BufferedImage.TYPE_INT_RGB);
				} else {
					currentImage = new BufferedImage(contentDimension.getWidth(), contentDimension.getHeight(), BufferedImage.TYPE_INT_RGB);
				}
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

		// back to top
		try {
			driver.scrollTop();
		} catch (JavascriptException e) {
			// ignore
		}

    	return currentImage;
    	
    }
    
    /**
     * Export buffered image to file
     * @param image	the image to export
     * @return produced file
     */
    private File exportToFile(BufferedImage image) {
    	String filename = HashCodeGenerator.getRandomHashCode("web");
        String filePath = Paths.get(SeleniumTestsContextManager.getThreadContext().getScreenshotOutputDirectory(), filename + ".png").toString();
        FileUtility.writeImage(filePath, image);
        logger.debug("Captured image copied to {}", filePath);
        return new File(filePath);
    }
    
    /**
     * Export buffered image to screenshot object, adding HTML source, title, ...
     * @param namedImage	image to export
     * @param duration		duration of the capture
     * @return	the screenshot object
     */
    private ScreenShot exportToScreenshot(NamedBufferedImage namedImage, long duration) {
    	ScreenShot screenShot = new ScreenShot(namedImage.image, namedImage.pageSource);

        screenShot.setLocation(namedImage.url);
        screenShot.setTitle(namedImage.title);
        
    	// record duration of screenshot
    	screenShot.setDuration(duration);
		return screenShot;
    }

	/**
	 * Returns the element rectangle applying aspect ratio, in case screen / browser is not at 100%
	 * @param element		element to capture
	 * @param aspectRatio	aspect ratio of screen
	 * @return	the rectangle updated with AR
	 */
	public static Rectangle getElementRectangleWithAR(WebElement element, double aspectRatio) {
		Rectangle rectangle = element.getRect();

		return new Rectangle(
				(int) Math.round(rectangle.x * aspectRatio),
				(int) Math.round(rectangle.y * aspectRatio),
				(int) Math.round(rectangle.height * aspectRatio),
				(int) Math.round(rectangle.width * aspectRatio)
		);
	}
 
}
