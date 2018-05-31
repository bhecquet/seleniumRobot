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
package com.seleniumtests.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.seleniumtests.browserfactory.AppiumDriverFactory;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.ChromeDriverFactory;
import com.seleniumtests.browserfactory.EdgeDriverFactory;
import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.browserfactory.HtmlUnitDriverFactory;
import com.seleniumtests.browserfactory.IEDriverFactory;
import com.seleniumtests.browserfactory.IWebDriverFactory;
import com.seleniumtests.browserfactory.SafariDriverFactory;
import com.seleniumtests.browserfactory.SauceLabsDriverFactory;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.browserfactory.TestDroidDriverFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

import net.lightbody.bmp.BrowserMobProxy;

/**
 * This class provides factory to create webDriver session.
 */
public class WebUIDriver {

	private static final Logger logger = SeleniumRobotLogger.getLogger(WebUIDriver.class);
	private static OSUtility osUtil = OSUtilityFactory.getInstance();
	
    private static ThreadLocal<WebDriver> driverSession = new ThreadLocal<>();
    private static ThreadLocal<WebUIDriver> uxDriverSession = new ThreadLocal<>();
    private String node;
    private DriverConfig config;
    private WebDriver driver;
    private IWebDriverFactory webDriverBuilder;
    private final static Object createDriverLock = new Object();

    public WebUIDriver() {
    	if (SeleniumTestsContextManager.getThreadContext() == null) {
            return;
        }
        config = new DriverConfig(SeleniumTestsContextManager.getThreadContext());
        uxDriverSession.set(this);
    }

	public WebDriver createRemoteWebDriver()  {
        
        // TODO: use grid with appium ?
        if (config.getMode() == DriverMode.GRID) {
            webDriverBuilder = new SeleniumGridDriverFactory(this.config);
        } else if (config.getMode() == DriverMode.SAUCELABS) {
        	webDriverBuilder = new SauceLabsDriverFactory(this.config);
        } else if (config.getMode() == DriverMode.TESTDROID) {
        	webDriverBuilder = new TestDroidDriverFactory(this.config);
        	
        // local mode
        } else {
        	if (config.getTestType().isMobile()) {
        		webDriverBuilder = new AppiumDriverFactory(this.config);
        	} else {
        		
	            if (config.getBrowser() == BrowserType.FIREFOX) {
	                webDriverBuilder = new FirefoxDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.INTERNET_EXPLORER) {
	                webDriverBuilder = new IEDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.EDGE) {
	            	webDriverBuilder = new EdgeDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.CHROME) {
	                webDriverBuilder = new ChromeDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.HTMLUNIT) {
	                webDriverBuilder = new HtmlUnitDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.SAFARI) {
	                webDriverBuilder = new SafariDriverFactory(this.config);
	            } else {
	                throw new DriverExceptions("Unsupported browser: " + config.getBrowser().toString());
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
        }

        

        return driver;
    }
    
    public String getNode() {
        return node;
    }

    public void setNode(final String node) {
        this.node = node;
    }

    public static void cleanUp() {
    	
        WebDriver driver = driverSession.get();
    	if (driver != null) {
    		try {
	            TestLogging.log("quiting webdriver " + Thread.currentThread().getId());
	            driver.quit();
        	} catch (Exception ex) {
        		TestLogging.error("Exception encountered when quiting driver:" + ex.getMessage());
        	}
        }
        
    	IWebDriverFactory iWebDriverFactory = getWebUIDriver().webDriverBuilder;
        if (iWebDriverFactory != null) {
            iWebDriverFactory.cleanUp();
        } 
        
        // in case of mobile test with appium, stop appium server
        if (iWebDriverFactory instanceof AppiumDriverFactory) {
        	((AppiumDriverFactory) iWebDriverFactory).getAppiumLauncher().stopAppium();
        }

        driverSession.remove();
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
     * Get EventFiringWebDriver.
     *
     * @return  webDriver
     */
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
        if (driverSession.get() == null && isCreate && !SeleniumTestsContextManager.isNonGuiTest()) {
        	getWebUIDriver().createWebDriver();
        }

        return driverSession.get();
    }

    /**
     * Returns WebUIDriver instance Creates new WebUIDriver instance if it is null.
     *
     * @return
     */
    public static WebUIDriver getWebUIDriver() {
        if (uxDriverSession.get() == null) {
//        	if (!SeleniumTestsContextManager.getThreadContext().isDevMode()){
//        		cleanWebDrivers();
//        	}
            uxDriverSession.set(new WebUIDriver());
        }

        return uxDriverSession.get();
    }

    /**
     * Close all the opened web browser processes. Not called in development mode.
     * 
     * This may not be desirable
     */
    private static void cleanWebDrivers() {
    	logger.info("Dev. mode : " + SeleniumTestsContextManager.getThreadContext().isDevMode()
    					+" , web browser running processes will terminate ! ");
		osUtil.killAllWebBrowserProcess(false); //true to force the kill
	}

	/**
     * Lets user set their own driver This can be retrieved as WebUIDriver.getWebDriver().
     *
     * @param  driver
     */
    public static void setWebDriver(final WebDriver driver) {
        if (driver == null) {
            driverSession.remove();
        } else {
        	// create WebUiDriver if it does not exist
            getWebUIDriver();
            driverSession.set(driver);
        }
    }

    protected WebDriver handleListeners(WebDriver driver, BrowserInfo browserInfo, List<Long> driverPids) {
    	EventFiringWebDriver listeningDriver = new CustomEventFiringWebDriver(driver, 
    																			driverPids, 
    																			browserInfo, 
    																			SeleniumTestsContextManager.isWebTest(), 
    																			SeleniumTestsContextManager.getThreadContext().getRunMode(),
    																			config.getBrowserMobProxy());
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
    		if (!browsers.containsKey(config.getBrowser())) {
    			throw new ConfigurationException(String.format("Browser %s is not available. Available browsers are %s", 
    					config.getBrowser(), browsers));
    		}
    	}
    }
    
    private void displayBrowserVersion() {
    	if (driver == null) {
    		return;
    	}
    	Capabilities caps = ((CustomEventFiringWebDriver) driver).getCapabilities();
        String browserName = caps.getBrowserName();
        String browserVersion = caps.getVersion(); 
        logger.info(String.format("Browser is: %s %s", browserName, browserVersion));
    }

    public WebDriver createWebDriver() {
    	
    	if (SeleniumTestsContextManager.getThreadContext().isDriverCreationBlocked()) {
    		throw new ScenarioException("Driver creation forbidden before @BeforeMethod and after @AfterMethod execution");
    	}
    	
    	if (config.getTestType().isMobile()) {
    		logger.info("Start creating appium driver");
    	} else {
    		logger.info(String.format("Start creating %s driver", config.getBrowser().getBrowserType()));
    	}
    	checkBrowserRunnable();
        driver = createRemoteWebDriver();
        displayBrowserVersion();
 
        if (config.getTestType().isMobile()) {
    		logger.info("Finished creating appium driver");
    	} else {
    		logger.info(String.format("Finished creating %s driver", config.getBrowser().getBrowserType()));
    	}

        driverSession.set(driver);
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
	
    public static ThreadLocal<WebUIDriver> getUxDriverSession() {
		return uxDriverSession;
	}
}
