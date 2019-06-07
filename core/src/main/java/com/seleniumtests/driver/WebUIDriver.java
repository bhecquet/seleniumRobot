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
package com.seleniumtests.driver;

import com.neotys.selenium.proxies.NLWebDriver;
import com.neotys.selenium.proxies.NLWebDriverFactory;
import com.seleniumtests.browserfactory.*;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.ScreenshotUtil.Target;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;
import com.seleniumtests.driver.screenshots.VideoRecorder;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides factory to create webDriver session.
 */
public class WebUIDriver {

	public static final String DEFAULT_DRIVER_NAME = "main";
	private static final Logger logger = SeleniumRobotLogger.getLogger(WebUIDriver.class);
	
    private static ThreadLocal<Map<String, WebUIDriver>> uxDriverSession = new ThreadLocal<>();
    private static ThreadLocal<VideoRecorder> videoRecorder = new ThreadLocal<>();
    private static ThreadLocal<String> currentWebUiDriverName = new ThreadLocal<>();
    private String name;
    private DriverConfig config;
    private WebDriver driver;
    private IWebDriverFactory webDriverBuilder;
    private final static Object createDriverLock = new Object();

    public WebUIDriver(String name) {
    	if (SeleniumTestsContextManager.getThreadContext() == null) {
            return;
        }
    	this.name = name;
        config = new DriverConfig(SeleniumTestsContextManager.getThreadContext());
        if (uxDriverSession.get() == null) {
            uxDriverSession.set(new HashMap<>());
        }
        uxDriverSession.get().put(name, this);
        
        switchToDriver(name);
    }

    /**
     * prepare driver:
     * - create it
     * - add listeners
     * - create and start video capture
     * - create and start network capture proxy
     * - record driver and browser pid so that they can be deleted at the end of test session
     * @return
     */
	public WebDriver createRemoteWebDriver()  {
        
        if (config.getMode() == DriverMode.GRID) {
            webDriverBuilder = new SeleniumGridDriverFactory(this.config);
        } else if (config.getMode() == DriverMode.SAUCELABS) {
        	webDriverBuilder = new SauceLabsDriverFactory(this.config);
        } else if (config.getMode() == DriverMode.BROWSERSTACK) {
        	webDriverBuilder = new BrowserStackDriverFactory(this.config);
        
        	
        // local mode
        } else {
        	if (config.getTestType().isMobile()) {
        		webDriverBuilder = new AppiumDriverFactory(this.config);
        	} else {
        		
	            if (config.getBrowserType() == BrowserType.FIREFOX) {
	                webDriverBuilder = new FirefoxDriverFactory(this.config);
	            } else if (config.getBrowserType() == BrowserType.INTERNET_EXPLORER) {
	                webDriverBuilder = new IEDriverFactory(this.config);
	            } else if (config.getBrowserType() == BrowserType.EDGE) {
	            	webDriverBuilder = new EdgeDriverFactory(this.config);
	            } else if (config.getBrowserType() == BrowserType.CHROME) {
	                webDriverBuilder = new ChromeDriverFactory(this.config);
	            } else if (config.getBrowserType() == BrowserType.HTMLUNIT) {
	                webDriverBuilder = new HtmlUnitDriverFactory(this.config);
	            } else if (config.getBrowserType() == BrowserType.SAFARI) {
	                webDriverBuilder = new SafariDriverFactory(this.config);
	            } else {
	                throw new DriverExceptions("Unsupported browser: " + config.getBrowserType().toString());
	            }
        	}
        }
        
        logger.info("driver mode: "+ config.getMode());

        synchronized (createDriverLock) {
        	
    		// get browser info used to start this driver. It will be used then for managing pids
        	BrowserInfo browserInfo = webDriverBuilder.getSelectedBrowserInfo();
        	List<Long> existingPids = new ArrayList<>();

    		// get pid pre-existing the creation of this driver. This helps filtering drivers launched by other tests or users
    		if (browserInfo != null) {
        		existingPids.addAll(browserInfo.getDriverAndBrowserPid(new ArrayList<>()));
        	}
        	
            driver = webDriverBuilder.createWebDriver();
            WaitHelper.waitForSeconds(2);
            
            List<Long> driverPids = new ArrayList<>();
            
            // get the created PIDs
            if (browserInfo != null) {
    			driverPids = browserInfo.getDriverAndBrowserPid(existingPids);
    		}
            
            driver = handleListeners(driver, browserInfo, driverPids);
    	

			if (config.getBrowserMobProxy() != null) {
				config.getBrowserMobProxy().newHar(SeleniumTestsContextManager.getThreadContext().getRelativeOutputDir());
			}
			
			if (config.getVideoCapture() != VideoCaptureMode.FALSE && videoRecorder.get() == null) {
				try {
					VideoRecorder recorder = CustomEventFiringWebDriver.startVideoCapture(SeleniumTestsContextManager.getThreadContext().getRunMode(), 
																						SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(),
																						new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()),
																						"videoCapture.avi");
					videoRecorder.set(recorder);
				} catch (ScenarioException e) {
					logger.warn("Video capture won't start: " + e.getMessage());
				}
			}
        }

        

        return driver;
    }

    /**
     * Clean all WebUIDriver for this thread
     */
    public static void cleanUp() {
    	
    	if (uxDriverSession.get() == null) {
    		return;
    	}
    	
    	for (WebUIDriver webuiDriver: uxDriverSession.get().values()) {
    		if (webuiDriver != null) {
    			webuiDriver.clean();
    		}
    	}
    	
		cleanUpWebUIDriver();

    }
    
    /**
     * logs the current state of the driver
     * Used when terminating test, before closing browser
     * extract the video file recorded by test
     */
    public static File logFinalDriversState() {
    	if (uxDriverSession.get() == null) {
    		return null;
    	}
    	
    	for (WebUIDriver webuiDriver: uxDriverSession.get().values()) {
    		if (webuiDriver != null) {
    			webuiDriver.logFinalDriverState();
    		}
    	}
    	
    	File videoFile = stopVideoCapture();
		
		return videoFile;
    }
    
    /**
     * Stop video capture
     */
    private static File stopVideoCapture() {
    	if (videoRecorder.get() != null) {
			
			try {
				return CustomEventFiringWebDriver.stopVideoCapture(SeleniumTestsContextManager.getThreadContext().getRunMode(), 
																		SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector(),
																		WebUIDriver.getVideoRecorder().get());

			} catch (IOException e) {
				logger.error("cannot attach video capture", e);
			} catch (Throwable e) {
				logger.error("Error stopping video capture: " + e.getMessage());
			} finally {
				videoRecorder.remove();
			}
		}
    	
    	return null;
    }
    
    /**
     * Logs current state of the browser
     */
    private void logFinalDriverState() {
    	if (driver != null) {
			try {
				
				// force screenshotUtil to use the driver of this WebUiDriver, not the currently selected one
				for (ScreenShot screenshot: new ScreenshotUtil(driver).capture(Target.PAGE, ScreenShot.class, true, true)) {
					TestLogging.logScreenshot(screenshot, null, name);
				}
			} catch (Exception e) {
				TestLogging.log("Error while logging: " + e.getMessage());
			}
		}
		
		try {
	    	// stop HAR capture
			if (config.getBrowserMobProxy() != null) {
				Har har = config.getBrowserMobProxy().endHar();
				TestLogging.logNetworkCapture(har, name);
			}
			
			
		} catch (Exception e) {
			TestLogging.log("Error while logging: " + e.getMessage());
		} finally {
			config.setBrowserMobProxy(null);
		}
    }
    	
    /**
     * Cleans the driver created by this class: 
     * quit  browser
     * remove pids
     * stop appium
     * dereference driver in this WebUIDriver
     */
    private void clean() {

    	if (driver != null) {
    		
    		// write logs
    		try {
        		for (String logType: driver.manage().logs().getAvailableLogTypes()) {

        			PrintWriter writer;
					try {
						writer = new PrintWriter(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), String.format("driver-log-%s.txt", logType)).toFile().getAbsolutePath(), "UTF-8");
						for (LogEntry line: driver.manage().logs().get(logType).getAll()) {
	        				writer.println(line.toString());
	        			}
	        			writer.close();
					} catch (FileNotFoundException | UnsupportedEncodingException e) {
					}
        		}
            } catch (Exception e) {
            }
    		
    		
    		try {
	            TestLogging.log("quiting webdriver " + Thread.currentThread().getId());
	            driver.quit();
        	} catch (Exception ex) {
        		TestLogging.error("Exception encountered when quiting driver:" + ex.getMessage());
        	}
    		driver = null;
        }
		
        if (webDriverBuilder != null) {
        	webDriverBuilder.cleanUp();
        } 
        
        // in case of mobile test with appium, stop appium server
        try {
	        if (webDriverBuilder instanceof AppiumDriverFactory) {
	        	((AppiumDriverFactory) webDriverBuilder).getAppiumLauncher().stopAppium();
	        }
        } catch (Exception e) {
        	logger.error("Error stopping Appium: " + e.getMessage());
        }
    
        // stop HAR capture in case it has not already been done by SeleniumRobotTestListener. This may be the case when a driver is created in @AfterMethod
		try {
	        if (config.getBrowserMobProxy() != null) {
	        	config.getBrowserMobProxy().endHar();
			}
		} catch (Throwable e) {
			logger.error("Error stopping browsermob proxy: " + e.getMessage());
		} finally {
			config.setBrowserMobProxy(null);
		}
		
		// stop video capture
		stopVideoCapture();
		
    }
    
    /**
     * dereference this WebUIDriver from the thread
     */
    private static void cleanUpWebUIDriver() {
        uxDriverSession.remove();
    }

	/**
     * Returns native WebDriver which can be converted to RemoteWebDriver.
     *
     * @return  webDriver
     */
    public static WebDriver getNativeWebDriver() {
        return ((CustomEventFiringWebDriver) getWebDriver(true)).getWebDriver();
    }

	public static BrowserMobProxy getBrowserMobProxy() {
		CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)WebUIDriver.getWebDriver(false);
		BrowserMobProxy mobProxy = null;
		if (driver != null) {
			mobProxy = driver.getMobProxy();
		}
		return mobProxy;
	}
	
	/**
	 * Returns the Neoload driver for the current running driver
	 * @return
	 */
	public static NLWebDriver getNeoloadDriver() {
		CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)WebUIDriver.getWebDriver(false);
		if (driver != null && driver.getNeoloadDriver() != null) {
			return driver.getNeoloadDriver();
		} else {
			return null;
		}
	}

    /**
     * Get EventFiringWebDriver.
     *
     * @deprecated use getWebDriver(boolean) instead
     * @return  webDriver
     */
	@Deprecated
    public static WebDriver getWebDriver() {
        return getWebDriver(true);
    }

    public IWebDriverFactory getWebDriverBuilder() {
		return webDriverBuilder;
	}

	/**
     * Returns WebDriver instance Creates a new WebDriver Instance if it is null and isCreate is true.
     *
     * @param   isCreate  create webdriver or not
     *
     * @return
     */
    public static WebDriver getWebDriver(final Boolean isCreate) {
    	return getWebDriver(isCreate, null, getCurrentWebUiDriverName(), null);
    }
    
    /**
     * Returns WebDriver instance Creates a new WebDriver Instance if it is null and isCreate is true.
     *
     * @param   isCreate  					create webdriver or not
     * @param	browserType					the new browser type to create. If null, and creation is requested, then the browser configured by user or configuration will be created
     * @param	driverName					a logical name to give to the created driver
     * @param	attachExistingDriverPort 	if we need to attach to an existing browser instead of creating one, then specify the port here
     *
     * @return
     */
    public static WebDriver getWebDriver(Boolean isCreate, BrowserType browserType, String driverName, Integer attachExistingDriverPort) {
    	
    	if (driverName == null) {
    		throw new ScenarioException("A name must be given to the driver");
    	}
    	
        if ((uxDriverSession.get() == null || uxDriverSession.get().get(driverName) == null || uxDriverSession.get().get(driverName).driver == null) && isCreate && !SeleniumTestsContextManager.isNonGuiTest()) {
        	
        	
        	WebUIDriver uiDriver = getWebUIDriver(true, driverName);
        	uiDriver.config.setAttachExistingDriverPort(attachExistingDriverPort);
        	
        	if (browserType == null) {
        		uiDriver.config.setBrowserType(SeleniumTestsContextManager.getThreadContext().getBrowser());
        	} else {
        		uiDriver.config.setBrowserType(browserType);
        	}
        	
        	// expect the new driver to run on same node as the previous ones
        	if (uxDriverSession.get() != null && uxDriverSession.get().size() > 1 && uiDriver.config.getSeleniumGridConnector() != null) {
        		uiDriver.config.setRunOnSameNode(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector().getNodeUrl());
        	}
        	
        	uiDriver.createWebDriver();
        }

        try {
        	return uxDriverSession.get().get(driverName).driver;
        } catch (NullPointerException e) {
        	return null;
        }
        
    }
    
    /**
     * Switch to one of the created driver, referenced by name
     * @param driverName	the driver name to return
     */
    public static void switchToDriver(String driverName) {
    	if (uxDriverSession.get() == null || !uxDriverSession.get().containsKey(driverName)) {
    		throw new ScenarioException(String.format("driver with name %s has not been created", driverName));
    	}
    	currentWebUiDriverName.set(driverName);
    	
    	if (uxDriverSession.get().get(driverName).driver != null && ((CustomEventFiringWebDriver)(uxDriverSession.get().get(driverName).driver)).getSessionId() == null) {
    		throw new ScenarioException("Cannot switch to a closed driver");
    	}
    	
    	TestLogging.info(String.format("Switching to driver named '%s'", driverName));
    }

    /**
     * Returns current WebUIDriver instance
     * @param create	create instance if it does not exist in this thread. Beware that this instance will have to be deleted at the end of test
     * 					(for regular seleniumRobot tests, this is done in <code>SeleniumRobotTestPlan.finishTestMethod</code>
     *
     * @return
     */
    public static WebUIDriver getWebUIDriver(boolean create) {
    	return getWebUIDriver(create, getCurrentWebUiDriverName());
    }
    
    /**
     * Returns current WebUIDriver instance for given name
     * If a new WebUIDriver instance is created, this become the current one
     * @param create	create instance if it does not exist in this thread. Beware that this instance will have to be deleted at the end of test
     * 					(for regular seleniumRobot tests, this is done in <code>SeleniumRobotTestPlan.finishTestMethod</code>
     * @param name		the name of the driver instance to retrieve (default is 'main', the first created driver)
     * @return
     */
    public static WebUIDriver getWebUIDriver(boolean create, String name) {
        if ((uxDriverSession.get() == null || uxDriverSession.get().get(name) == null) && create) {
            new WebUIDriver(name);
        }
        
        try {
        	return uxDriverSession.get().get(name);
        } catch (NullPointerException e) {
        	return null;
        }
    }

	/**
     * Lets user set their own driver This can be retrieved as WebUIDriver.getWebDriver().
     *
     * @param  driver
     */
    public static void setWebDriver(final WebDriver driver) {
        if (driver == null) {
        	if (uxDriverSession.get() != null && uxDriverSession.get().get(getCurrentWebUiDriverName()) != null) {
        		uxDriverSession.get().get(getCurrentWebUiDriverName()).driver = null;
        	}
        } else {
        	// create WebUiDriver if it does not exist
            getWebUIDriver(true);
            uxDriverSession.get().get(getCurrentWebUiDriverName()).driver = driver;
        }
    }

    protected WebDriver handleListeners(WebDriver driver, BrowserInfo browserInfo, List<Long> driverPids) {

    	// Use of Neoload
    	// we must configure the user path, and the proxy mode
    	// In this case, the read driver is "replaced" by the proxy to the driver
    	if (config.isNeoloadActive()) {
    		driver = NLWebDriverFactory.newNLWebDriver(driver, config.getNeoloadUserPath());
    	}
    	
    	EventFiringWebDriver listeningDriver = new CustomEventFiringWebDriver(driver, 
    																			driverPids, 
    																			browserInfo, 
    																			SeleniumTestsContextManager.isWebTest(), 
    																			SeleniumTestsContextManager.getThreadContext().getRunMode(),
    																			config.getBrowserMobProxy(),
    																			SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
    	
        List<WebDriverEventListener> listeners = config.getWebDriverListeners();
        if (listeners != null && !listeners.isEmpty()) {
            for (int i = 0; i < config.getWebDriverListeners().size(); i++) {
            	listeningDriver = listeningDriver.register(listeners.get(i));
            }
        }

        return listeningDriver;
    }
    
    private void checkBrowserRunnable() {
    	if (config.getMode() == DriverMode.LOCAL && !config.getTestType().isMobile()) {
    		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
    		if (!browsers.containsKey(config.getBrowserType())) {
    			throw new ConfigurationException(String.format("Browser %s is not available. Available browsers are %s", 
    					config.getBrowserType(), browsers));
    		}
    	}
    }
    
    /**
     * Get version from browser capabilities and display it
     */
    private void displayBrowserVersion() {
    	if (driver == null) {
    		return;
    	}
    	Capabilities caps = ((CustomEventFiringWebDriver) driver).getCapabilities();
        String browserName = caps.getBrowserName();
        String browserVersion = caps.getVersion(); 
        
        Integer majorVersion;
        try {
        	majorVersion = Integer.parseInt(browserVersion.split("\\.")[0]);
        } catch (NumberFormatException e) {
        	majorVersion = 1;
        }
        config.setMajorBrowserVersion(majorVersion);
        logger.info(String.format("Browser is: %s %s", browserName, browserVersion));
    }

    /**
     * create the driver
     * @return the driver
     * @throws ScenarioException in case we are not allowed to create it (if we are in @BeforeMethod and after @AfterMethod)
     */
    public WebDriver createWebDriver() {
    	
    	if (SeleniumTestsContextManager.getThreadContext().isDriverCreationBlocked()) {
    		throw new ScenarioException("Driver creation forbidden before @BeforeMethod and after @AfterMethod execution");
    	}
    	
    	if (config.getTestType().isMobile()) {
    		logger.info("Start creating appium driver");
    	} else {
    		logger.info(String.format("Start creating %s driver", config.getBrowserType().getBrowserType()));
    	}
    	checkBrowserRunnable();
        driver = createRemoteWebDriver();
        displayBrowserVersion();
 
        if (config.getTestType().isMobile()) {
    		logger.info("Finished creating appium driver");
    	} else {
    		logger.info(String.format("Finished creating %s driver", config.getBrowserType().getBrowserType()));
    	}

        return driver;
    }

    public static void main(final String[] args) {
        logger.info(DriverExceptionListener.class.getName());
    }

    public boolean isSetAcceptUntrustedCertificates() {
        return config.isSetAcceptUntrustedCertificates();
    }

    public boolean isSetAssumeUntrustedCertificateIssuer() {
        return config.isSetAssumeUntrustedCertificateIssuer();
    }

    public boolean isEnableJavascript() {
        return config.isEnableJavascript();
    }

	public DriverConfig getConfig() {
		return config;
	}

	public static ThreadLocal<VideoRecorder> getVideoRecorder() {
		return videoRecorder;
	}

	public static String getCurrentWebUiDriverName() {
		if (currentWebUiDriverName.get() == null) {
			currentWebUiDriverName.set(DEFAULT_DRIVER_NAME);
		}
		return currentWebUiDriverName.get();
	}

	/**
	 * Gets the driver created inside this WebUIDriver
	 * @return
	 */
	public WebDriver getDriver() {
		return driver;
	}

	/**
	 * FOR TEST ONLY
	 * @param driver
	 */
	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public static ThreadLocal<Map<String, WebUIDriver>> getUxDriverSession() {
		return uxDriverSession;
	}
}
