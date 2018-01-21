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
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
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
import com.seleniumtests.core.proxy.ProxyConfig;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

/**
 * This class provides factory to create webDriver session.
 */
public class WebUIDriver {

	private static final Logger logger = SeleniumRobotLogger.getLogger(WebUIDriver.class);
	private static OSUtility osUtil = OSUtilityFactory.getInstance();
	
    private static ThreadLocal<WebDriver> driverSession = new ThreadLocal<>();
    private static ThreadLocal<WebUIDriver> uxDriverSession = new ThreadLocal<>();
    private String node;
    private DriverConfig config = new DriverConfig();
    private WebDriver driver;
    private IWebDriverFactory webDriverBuilder;
    private final static Object createDriverLock = new Object();

    public WebUIDriver() {
        init();
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
        
        logger.info("driver mode: "+config.getMode());

        synchronized (createDriverLock) {
        	
    		// get browser info used to start this driver. It will be used then for 
        	BrowserInfo browserInfo = OSUtility.getInstalledBrowsersWithVersion().get(config.getBrowser());
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
        IWebDriverFactory iWebDriverFactory = getWebUIDriver().webDriverBuilder;
        if (iWebDriverFactory != null) {
            iWebDriverFactory.cleanUp();
        } else {
            WebDriver driver = driverSession.get();
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
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
    	EventFiringWebDriver listeningDriver = new CustomEventFiringWebDriver(driver, driverPids, browserInfo, SeleniumTestsContextManager.isWebTest(), SeleniumTestsContextManager.getThreadContext().getRunMode());
        List<WebDriverEventListener> listeners = config.getWebDriverListeners();
        if (listeners != null && !listeners.isEmpty()) {
            for (int i = 0; i < config.getWebDriverListeners().size(); i++) {
            	listeningDriver = listeningDriver.register(listeners.get(i));
            }
        }

        return listeningDriver;
    }
    
    /**
     * In local mode only, display the browser version
     * For remote, will use either the default one, or version specified in test context
     */
    private void checkBrowserAvailable() {
    	if (config.getMode() == DriverMode.LOCAL && !config.getTestType().isMobile()) {
    		Map<BrowserType, BrowserInfo> browsers = OSUtility.getInstalledBrowsersWithVersion();
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
    	
    	if (config.getTestType().isMobile()) {
    		logger.info("Start creating appium driver");
    	} else {
    		logger.info(String.format("Start creating %s driver", config.getBrowser().getBrowserType()));
    	}
	
	
        
    	checkBrowserAvailable();
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

    private void init() {
        if (SeleniumTestsContextManager.getThreadContext() == null) {
            return;
        }

        BrowserType browser = SeleniumTestsContextManager.getThreadContext().getBrowser();
        config.setBrowser(browser);

        DriverMode mode = SeleniumTestsContextManager.getThreadContext().getRunMode();
        config.setMode(mode);

        String hubUrl = SeleniumTestsContextManager.getThreadContext().getWebDriverGrid();
        config.setHubUrl(hubUrl);

        String ffProfilePath = SeleniumTestsContextManager.getThreadContext().getFirefoxUserProfilePath();
        config.setFfProfilePath(ffProfilePath);

        String operaProfilePath = SeleniumTestsContextManager.getThreadContext().getOperaUserProfilePath();
        config.setOperaProfilePath(operaProfilePath);

        String ffBinPath = SeleniumTestsContextManager.getThreadContext().getFirefoxBinPath();
        config.setFfBinPath(ffBinPath);

        String chromeBinPath = SeleniumTestsContextManager.getThreadContext().getChromeBinPath();
        config.setChromeBinPath(chromeBinPath);

        String chromeDriverPath = SeleniumTestsContextManager.getThreadContext().getChromeDriverPath();
        config.setChromeDriverPath(chromeDriverPath);
        
        
        String geckoDriverPath = SeleniumTestsContextManager.getThreadContext().getGeckoDriverPath();
        config.setGeckoDriverPath(geckoDriverPath);
        
        String edgeDriverPath = SeleniumTestsContextManager.getThreadContext().getEdgeDriverPath();
        config.setEdgeDriverPath(edgeDriverPath);

        String ieDriverPath = SeleniumTestsContextManager.getThreadContext().getIEDriverPath();
        config.setIeDriverPath(ieDriverPath);

        int webSessionTimeout = SeleniumTestsContextManager.getThreadContext().getWebSessionTimeout();
        config.setWebSessionTimeout(webSessionTimeout);

        double implicitWaitTimeout = SeleniumTestsContextManager.getThreadContext().getImplicitWaitTimeout();
        config.setImplicitWaitTimeout(implicitWaitTimeout);

        int explicitWaitTimeout = SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout();
        config.setExplicitWaitTimeout(explicitWaitTimeout);
        config.setPageLoadTimeout(SeleniumTestsContextManager.getThreadContext().getPageLoadTimeout());

        String outputDirectory = SeleniumTestsContextManager.getGlobalContext().getOutputDirectory();
        config.setOutputDirectory(outputDirectory);
        
        // set proxy config
        ProxyConfig proxyConfig = new ProxyConfig();
        proxyConfig.setType(SeleniumTestsContextManager.getThreadContext().getWebProxyType());
        proxyConfig.setAddress(SeleniumTestsContextManager.getThreadContext().getWebProxyAddress());
        proxyConfig.setPort(SeleniumTestsContextManager.getThreadContext().getWebProxyPort());
        proxyConfig.setLogin(SeleniumTestsContextManager.getThreadContext().getWebProxyLogin());
        proxyConfig.setPassword(SeleniumTestsContextManager.getThreadContext().getWebProxyPassword());
        proxyConfig.setExclude(SeleniumTestsContextManager.getThreadContext().getWebProxyExclude());
        proxyConfig.setPac(SeleniumTestsContextManager.getThreadContext().getWebProxyPac());
        config.setProxyConfig(proxyConfig);


        String browserVersion = SeleniumTestsContextManager.getThreadContext().getWebBrowserVersion();
        config.setBrowserVersion(browserVersion);

        String webPlatform = SeleniumTestsContextManager.getThreadContext().getPlatform();
        
        // this configuration is only used for web tests
        if (webPlatform != null && !webPlatform.toLowerCase().contains("ios")) {
            config.setWebPlatform(Platform.fromString(webPlatform));
        }

        config.setSetAssumeUntrustedCertificateIssuer(SeleniumTestsContextManager.getThreadContext().getAssumeUntrustedCertificateIssuer());

        config.setSetAcceptUntrustedCertificates(SeleniumTestsContextManager.getThreadContext().getAcceptUntrustedCertificates());

        config.setEnableJavascript(SeleniumTestsContextManager.getThreadContext().getJavascriptEnabled());

        if (SeleniumTestsContextManager.getThreadContext().getNtlmAuthTrustedUris() != null) {
            config.setNtlmAuthTrustedUris(SeleniumTestsContextManager.getThreadContext().getNtlmAuthTrustedUris());
        }

        if (SeleniumTestsContextManager.getThreadContext().getBrowserDownloadDir() != null) {
            config.setBrowserDownloadDir(SeleniumTestsContextManager.getThreadContext().getBrowserDownloadDir());
        }

        config.setAddJSErrorCollectorExtension(SeleniumTestsContextManager.getThreadContext().getAddJSErrorCollectorExtension());

        String ua;
        if (SeleniumTestsContextManager.getThreadContext().getUserAgent() != null) {
            ua = SeleniumTestsContextManager.getThreadContext().getUserAgent();
        } else {
            ua = null;
        }

        config.setUserAgentOverride(ua);

        String listeners = SeleniumTestsContextManager.getThreadContext().getWebDriverListener();
        if (SeleniumTestsContextManager.getThreadContext().getEnableExceptionListener()) {
            if (listeners != null) {
                listeners = listeners + ",";
            } else {
                listeners = "";
            }

            listeners = listeners + DriverExceptionListener.class.getName();
        }

        if (listeners != null && !"".equals(listeners)) {
            config.setWebDriverListeners(listeners);
        } else {
            config.setWebDriverListeners("");
        }

        config.setUseFirefoxDefaultProfile(SeleniumTestsContextManager.getThreadContext().isUseFirefoxDefaultProfile());

        String appiumServerURL = SeleniumTestsContextManager.getThreadContext().getAppiumServerURL();
        config.setAppiumServerURL(appiumServerURL);
        
        String mobilePlatformVersion = SeleniumTestsContextManager.getThreadContext().getMobilePlatformVersion();
        config.setMobilePlatformVersion(mobilePlatformVersion);

        String deviceName = SeleniumTestsContextManager.getThreadContext().getDeviceName();
        config.setDeviceName(deviceName);

        String app = SeleniumTestsContextManager.getThreadContext().getApp();
        config.setApp(app);

        String appPackage = SeleniumTestsContextManager.getThreadContext().getAppPackage();
        config.setAppPackage(appPackage);

        String appActivity = SeleniumTestsContextManager.getThreadContext().getAppActivity();
        config.setAppActivity(appActivity);
        
        String appWaitActivity = SeleniumTestsContextManager.getThreadContext().getAppWaitActivity();
        config.setAppWaitActivity(appWaitActivity);

        Integer newCommandTimeOut = SeleniumTestsContextManager.getThreadContext().getNewCommandTimeout();
        config.setNewCommandTimeout(newCommandTimeOut);
        
        config.setFullReset(SeleniumTestsContextManager.getThreadContext().getFullReset());

        config.setVersion(SeleniumTestsContextManager.getThreadContext().getVersion());
        config.setPlatform(SeleniumTestsContextManager.getThreadContext().getPlatform());
        config.setCloudApiKey(SeleniumTestsContextManager.getThreadContext().getCloudApiKey());
        config.setProjectName(SeleniumTestsContextManager.getThreadContext().getProjectName());
        config.setTestType(SeleniumTestsContextManager.getThreadContext().getTestType());
    }

    public static void main(final String[] args) {
        logger.info(DriverExceptionListener.class.getName());
    }

    public boolean isSetAcceptUntrustedCertificates() {
        return config.isSetAcceptUntrustedCertificates();
    }

    public boolean isAddJSErrorCollectorExtension() {
        return config.isAddJSErrorCollectorExtension();
    }

    public void setAddJSErrorCollectorExtension(final Boolean addJSErrorCollectorExtension) {
        config.setAddJSErrorCollectorExtension(addJSErrorCollectorExtension);
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
