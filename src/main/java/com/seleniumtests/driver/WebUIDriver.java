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

package com.seleniumtests.driver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.seleniumtests.browserfactory.AppiumDriverFactory;
import com.seleniumtests.browserfactory.ChromeDriverFactory;
import com.seleniumtests.browserfactory.FirefoxDriverFactory;
import com.seleniumtests.browserfactory.HtmlUnitDriverFactory;
import com.seleniumtests.browserfactory.IEDriverFactory;
import com.seleniumtests.browserfactory.IWebDriverFactory;
import com.seleniumtests.browserfactory.RemoteDriverFactory;
import com.seleniumtests.browserfactory.SafariDriverFactory;
import com.seleniumtests.browserfactory.SauceLabsDriverFactory;
import com.seleniumtests.browserfactory.TestDroidDriverFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.helper.WaitHelper;

/**
 * This class provides factory to create webDriver session.
 */
public class WebUIDriver {

	private static final Logger logger = TestLogging.getLogger(WebUIDriver.class);
    private static ThreadLocal<WebDriver> driverSession = new ThreadLocal<>();
    private static ThreadLocal<WebUIDriver> uxDriverSession = new ThreadLocal<>();
    private String node;
    private DriverConfig config = new DriverConfig();
    private WebDriver driver;
    private IWebDriverFactory webDriverBuilder;

    public WebUIDriver() {
        init();
        uxDriverSession.set(this);
    }

    public WebUIDriver(final String browser, final String mode) {
        init();
        this.setBrowser(browser);
        this.setMode(mode);
        uxDriverSession.set(this);
    }

    public WebDriver createRemoteWebDriver(final String browser, final String mode) throws IOException  {
        config.setBrowser(BrowserType.getBrowserType(browser));
        config.setMode(DriverMode.valueOf(mode));

        // TODO: use grid with appium ?
        if (config.getMode() == DriverMode.EXISTING_GRID) {
            webDriverBuilder = new RemoteDriverFactory(this.config);
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
	            } else if (config.getBrowser() == BrowserType.INTERNETEXPLORER) {
	                webDriverBuilder = new IEDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.CHROME) {
	                webDriverBuilder = new ChromeDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.HTMLUNIT) {
	                webDriverBuilder = new HtmlUnitDriverFactory(this.config);
	            } else if (config.getBrowser() == BrowserType.SAFARI) {
	                webDriverBuilder = new SafariDriverFactory(this.config);
	            } else {
	                throw new DriverExceptions("Unsupported browser: " + browser);
	            }
        	}
        }

        synchronized (this.getClass()) {
            driver = webDriverBuilder.createWebDriver();
            WaitHelper.waitForSeconds(1);
        }

        driver = handleListeners(driver);

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
                } catch (WebDriverException ex) {
                    logger.error(ex);
                }
            }
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

    /**
     * Returns WebDriver instance Creates a new WebDriver Instance if it is null and isCreate is true.
     *
     * @param   isCreate  create webdriver or not
     *
     * @return
     */
    public static WebDriver getWebDriver(final Boolean isCreate) {
        if (driverSession.get() == null && isCreate) {
            try {
                getWebUIDriver().createWebDriver();
            } catch (Exception e) {
                logger.error(e);
            }
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
            uxDriverSession.set(new WebUIDriver());
        }

        return uxDriverSession.get();
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

    protected WebDriver handleListeners(WebDriver driver) {
    	WebDriver listeningDriver = driver;
        List<WebDriverEventListener> listeners = config.getWebDriverListeners();
        if (listeners != null && !listeners.isEmpty()) {
            for (int i = 0; i < config.getWebDriverListeners().size(); i++) {
            	listeningDriver = new CustomEventFiringWebDriver(listeningDriver).register(listeners.get(i));
            }
        }

        return listeningDriver;
    }

    public WebDriver createWebDriver() throws IOException  {
    	if (config.getTestType().isMobile()) {
    		logger.info("Start creating appium driver");
    	} else {
    		logger.info(String.format("Start creating %s driver", this.getBrowser()));
    	}
        
        driver = createRemoteWebDriver(config.getBrowser().getBrowserType(), config.getMode().name());

        if (config.getTestType().isMobile()) {
    		logger.info("Finished creating appium driver");
    	} else {
    		logger.info(String.format("Finished creating %s driver", this.getBrowser()));
    	}

        driverSession.set(driver);
        return driver;
    }

    public String getBrowser() {
        return config.getBrowser().getBrowserType();
    }

    public String getPlatform() {
        return config.getWebPlatform().name();
    }

    public String getBrowserVersion() {
        return config.getBrowserVersion();
    }

    public String getChromeBinPath() {
        return config.getChromeBinPath();
    }

    public String getChromeDriverPath() {
        return config.getChromeDriverPath();
    }

    public DriverConfig getConfig() {
        return config;
    }

    public int getExplicitWait() {
        return config.getExplicitWaitTimeout();
    }

    public String getFfBinPath() {
        return config.getFirefoxBinPath();
    }

    public String getFfProfilePath() throws URISyntaxException {
        return config.getFirefoxProfilePath();
    }

    public String getOperaProfilePath() throws URISyntaxException {
        return config.getOperaProfilePath();
    }

    public void setOperaProfilePath(final String operaProfilePath) {
        config.setOperaProfilePath(operaProfilePath);
    }

    public String getHubUrl() {
        return config.getHubUrl();
    }

    public String getIEDriverPath() {
        return config.getIeDriverPath();
    }

    public double getImplicitWait() {
        return config.getImplicitWaitTimeout();
    }

    public String getMode() {
        return config.getMode().name();
    }

    public String getOutputDirectory() {
        return config.getOutputDirectory();
    }

    public String getNtlmAuthTrustedUris() {
        return config.getNtlmAuthTrustedUris();
    }

    public void setNtlmAuthTrustedUris(final String url) {
        config.setNtlmAuthTrustedUris(url);
    }

    public int getPageLoadTimeout() {
        return config.getPageLoadTimeout();
    }

    public String getProxyHost() {
        return config.getProxyHost();
    }

    public void setUserAgentOverride(final String userAgentOverride) {
        config.setUserAgentOverride(userAgentOverride);
    }

    public String getUserAgentOverride() {
        return config.getUserAgentOverride();
    }

    public IWebDriverFactory getWebDriverBuilder() {
        return webDriverBuilder;
    }

    public int getWebSessionTimeout() {
        return config.getWebSessionTimeout();
    }

    private void init() {
        if (SeleniumTestsContextManager.getThreadContext() == null) {
            return;
        }

        String browser = SeleniumTestsContextManager.getThreadContext().getBrowser();
        config.setBrowser(BrowserType.getBrowserType(browser));

        String mode = SeleniumTestsContextManager.getThreadContext().getRunMode();
        config.setMode(DriverMode.fromString(mode));

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

        if (SeleniumTestsContextManager.getThreadContext().isWebProxyEnabled()) {
            String proxyHost = SeleniumTestsContextManager.getThreadContext().getWebProxyAddress();
            config.setProxyHost(proxyHost);
        }

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

    public void setEnableJavascript(final Boolean enableJavascript) {
        config.setEnableJavascript(enableJavascript);
    }

    public void setBrowser(final String browser) {
        config.setBrowser(BrowserType.getBrowserType(browser));

    }

    public void setBrowserVersion(final String browserVersion) {
        config.setBrowserVersion(browserVersion);
    }

    public void setPlatform(final String platform) {
        config.setWebPlatform(Platform.valueOf(platform));
    }

    public void setChromeBinPath(final String chromeBinPath) {
        config.setChromeBinPath(chromeBinPath);
    }

    public void setBrowserDownloadDir(final String browserDownloadDir) {
        config.setBrowserDownloadDir(browserDownloadDir);
    }

    public String getBrowserDownloadDir() {
        return config.getBrowserDownloadDir();
    }

    public void setChromeDriverPath(final String chromeDriverPath) {
        config.setChromeDriverPath(chromeDriverPath);
    }

    public void setConfig(final DriverConfig config) {
        this.config = config;
    }

    public void setExplicitTimeout(final int explicitWaitTimeout) {
        config.setExplicitWaitTimeout(explicitWaitTimeout);
    }

    public void setFfBinPath(final String ffBinPath) {
        config.setFfBinPath(ffBinPath);
    }

    public void setFfProfilePath(final String ffProfilePath) {
        config.setFfProfilePath(ffProfilePath);
    }

    public void setHubUrl(final String hubUrl) {
        config.setHubUrl(hubUrl);
    }

    public void setIEDriverPath(final String ieDriverPath) {
        config.setIeDriverPath(ieDriverPath);
    }

    public void setImplicitlyWaitTimeout(final double implicitTimeout) {
        config.setImplicitWaitTimeout(implicitTimeout);
    }

    public void setMode(final String mode) {
        config.setMode(DriverMode.valueOf(mode));
    }

    public void setOutputDirectory(final String outputDirectory) {
        config.setOutputDirectory(outputDirectory);
    }

    public void setPageLoadTimeout(final int pageLoadTimeout) {
        config.setPageLoadTimeout(pageLoadTimeout);
    }

    public void setProxyHost(final String proxyHost) {
        config.setProxyHost(proxyHost);
    }

    public void setSetAcceptUntrustedCertificates(final boolean setAcceptUntrustedCertificates) {
        config.setSetAcceptUntrustedCertificates(setAcceptUntrustedCertificates);
    }

    public void setSetAssumeUntrustedCertificateIssuer(final boolean setAssumeUntrustedCertificateIssuer) {
        config.setSetAssumeUntrustedCertificateIssuer(setAssumeUntrustedCertificateIssuer);
    }

    public void setWebDriverBuilder(final IWebDriverFactory builder) {
        this.webDriverBuilder = builder;
    }

    public void setWebSessionTimeout(final int webSessionTimeout) {
        config.setWebSessionTimeout(webSessionTimeout);
    }
}
