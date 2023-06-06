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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.video.VideoCaptureMode;

import net.lightbody.bmp.BrowserMobProxy;

public class DriverConfig {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(DriverConfig.class);

    private WebDriver driver;
    private Integer attachExistingDriverPort = null;
    private String runOnSameNode = null;
    private BrowserMobProxy browserMobProxy;
    private SeleniumTestsContext testContext;
    private BrowserType browserType;
    private Integer majorBrowserVersion;
    
    public DriverConfig(SeleniumTestsContext testContext) {
    	this.testContext = testContext;
    	this.browserType = testContext.getBrowser();
    }

    public List<WebDriverEventListener> getWebDriverListeners() {
    	List<String> listeners = testContext.getWebDriverListener();
        
        ArrayList<WebDriverEventListener> listenerList = new ArrayList<>();
        for (String listenerName : listeners) {

            WebDriverEventListener listener = null;
            try {
                if (!"".equals(listenerName)) {
                    listener = (WebDriverEventListener) (Class.forName(listenerName)).newInstance();
                    listenerList.add(listener);
                }
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                logger.error(e);
            }
        }
        
        return listenerList;
    }

    public BrowserType getBrowserType() {
        return browserType;
    }

    public void setBrowserType(BrowserType browserType) {
		this.browserType = browserType;
	}

	public String getBrowserDownloadDir() {
        return testContext.getBrowserDownloadDir();
    }
	
	public Capabilities getCapabilites() {
		return testContext.getCapabilities();
	}
	
	public String getInitialUrl() {
		return testContext.getInitialUrl();
	}

    public String getBrowserVersion() {
        return testContext.getWebBrowserVersion();
    }
    
    public Capabilities getAppiumCapabilities() {
    	return testContext.getAppiumCapabilities();
    }

    public String getChromeBinPath() {
        return testContext.getChromeBinPath();
    }
    
    public String getChromeOptions() {
    	return testContext.getChromeOptions();
    }
    
    public String getEdgeOptions() {
    	return testContext.getEdgeOptions();
    }
    
    public Boolean getBetaBrowser() {
    	return testContext.getBetaBrowser();
    }
    
    public String getAutomationName() {
    	return testContext.getAutomationName();
    }

    public String getChromeDriverPath() {
        return testContext.getChromeDriverPath();
    }
    
    public Boolean getIeMode() {
    	return testContext.getEdgeIeMode();
    }
    
    public List<String> getNodeTags() {
    	return testContext.getNodeTags();
    }
    
    public List<DebugMode> getDebug() {
    	return testContext.getDebug();
    }

    public WebDriver getDriver() {
        return driver;
    }
    
    public boolean getCaptureNetwork() {
    	return testContext.getCaptureNetwork();
    }

    public int getExplicitWaitTimeout() {
        if (testContext.getExplicitWaitTimeout() < getImplicitWaitTimeout()) {
            return (int) getImplicitWaitTimeout();
        } else {
            return testContext.getExplicitWaitTimeout();
        }
    }

    public String getFirefoxBinPath() {
        return testContext.getFirefoxBinPath();
    }

    public String getFirefoxProfilePath() {
        return testContext.getFirefoxUserProfilePath();
    }
    
    public String getChromeProfilePath() {
    	return testContext.getChromeUserProfilePath();
    }
    
    public String getEdgeProfilePath() {
    	return testContext.getEdgeUserProfilePath();
    }

    public List<String> getHubUrl() {
        return testContext.getWebDriverGrid();
    }

    public String getIeDriverPath() {
        return testContext.getIEDriverPath();
    }

    public double getImplicitWaitTimeout() {
        return testContext.getImplicitWaitTimeout();
    }

    public DriverMode getMode() {
        return testContext.getRunMode();
    }

    public String getNtlmAuthTrustedUris() {
        return testContext.getNtlmAuthTrustedUris();
    }

    public String getOperaProfilePath() {
        if (testContext.getOperaUserProfilePath() == null && getClass().getResource("/profiles/operaProfile") != null) {

            try {
                return getClass().getResource("/profiles/operaProfile").toURI().getPath();
            } catch (URISyntaxException e) {
            	throw new DriverExceptions(e.getMessage());
            }
        }

        return testContext.getOperaUserProfilePath();
    }

    public String getOutputDirectory() {
        return testContext.getOutputDirectory();
    }

    public int getPageLoadTimeout() {
        return testContext.getPageLoadTimeout();
    }
    
    public PageLoadStrategy getPageLoadStrategy() {
    	return testContext.getPageLoadStrategy();
    }

    public Platform getWebPlatform() {
        return Platform.fromString(testContext.getPlatform());
    }

    public Proxy getProxy() {
    	return testContext.getProxy();
    }
    
    public ProxyType getWebProxyType() {
    	return testContext.getWebProxyType();
    }
    
    public String getWebProxyAddress() {
    	return testContext.getWebProxyAddress();
    }
    
    public Integer getWebProxyPort() {
    	return testContext.getWebProxyPort();
    }
    
    /**
     * /!\ take the grid connector from test context because this one stores the current grid connector when a driver is created
     *     This is used by issue #231 to know on which grid connector a previous driver has been created, and create the new driver on the same grid
     * @return
     */
    public SeleniumGridConnector getSeleniumGridConnector() {
    	return testContext.getSeleniumGridConnector();
    }
    
    public String getWebProxyLogin() {
    	return testContext.getWebProxyLogin();
    }
    
    public String getWebProxyPassword() {
    	return testContext.getWebProxyPassword();
    }
    
    public String getWebProxyExclude() {
    	return testContext.getWebProxyExclude();
    }
    
    public String getWebProxyPac() {
    	return testContext.getWebProxyPac();
    }

    public String getUserAgentOverride() {
        return testContext.getUserAgent();
    }

    public int getWebSessionTimeout() {
        return testContext.getWebSessionTimeout();
    }

    public boolean isEnableJavascript() {
        return testContext.getJavascriptEnabled();
    }

    public boolean isSetAcceptUntrustedCertificates() {
        return testContext.getAcceptUntrustedCertificates();
    }

    public boolean isSetAssumeUntrustedCertificateIssuer() {
        return testContext.getAssumeUntrustedCertificateIssuer();
    }

    public void setDriver(final WebDriver driver) {
        this.driver = driver;
    }

    public String getMobilePlatformVersion() {
        return testContext.getMobilePlatformVersion();
    }

    public String getDeviceName() {
        return testContext.getDeviceName();
    }
    
    public String getDeviceId() {
    	return testContext.getDeviceId();
    }

    public String getApp() {
        return testContext.getApp();
    }

    public String getAppPackage() {
        return testContext.getAppPackage();
    }

    public String getAppActivity() {
        return testContext.getAppActivity();
    }

    public String getEdgeDriverPath() {
		return testContext.getEdgeDriverPath();
	}

	public Integer getNewCommandTimeout() {
        return testContext.getNewCommandTimeout();
    }

    public TestType getTestType() {
        return testContext.getTestType();
    }

    public String getPlatform() {
        return testContext.getPlatform();
    }

    public String getVersion() {
        return testContext.getVersion();
    }

	public VideoCaptureMode getVideoCapture() {
		return testContext.getVideoCapture();
	}

	public String getAppWaitActivity() {
		return testContext.getAppWaitActivity();
	}

	public boolean isFullReset() {
		return testContext.getFullReset();
	}

	public String getGeckoDriverPath() {
		return testContext.getGeckoDriverPath();
	}

	public boolean isHeadlessBrowser() {
		return testContext.isHeadlessBrowser();
	}

	public BrowserMobProxy getBrowserMobProxy() {
		return browserMobProxy;
	}

	public void setBrowserMobProxy(BrowserMobProxy browserMobProxy) {
		this.browserMobProxy = browserMobProxy;
	}

	public SeleniumTestsContext getTestContext() {
		return testContext;
	}
	
	public String getNeoloadUserPath() {
    	return testContext.getNeoloadUserPath();
    }
	
	public boolean isNeoloadActive() {
		return getNeoloadUserPath() != null && System.getProperty("nl.selenium.proxy.mode") != null;
	}
	
	/**
	 * Returns the current value for 'attachExistingDriver'
	 * @return
	 */
	public Integer getAttachExistingDriverPort() {
		return attachExistingDriverPort;
	}

	/**
	 * Set the value for attaching an existing driver.
	 * 
	 * @param attachExistingDriverPort	In case of chrome, value is the debugger port
	 * 									In case of internet explorer, value may be anything
	 * 									If null is given, we create a new driver
	 */
	public void setAttachExistingDriverPort(Integer attachExistingDriverPort) {
		this.attachExistingDriverPort = attachExistingDriverPort;
	}

	public String getRunOnSameNode() {
		return runOnSameNode;
	}

	public void setRunOnSameNode(String runOnSameNode) {
		this.runOnSameNode = runOnSameNode;
	}

	public Integer getMajorBrowserVersion() {
		return majorBrowserVersion;
	}

	public void setMajorBrowserVersion(Integer majorBrowserVersion) {
		this.majorBrowserVersion = majorBrowserVersion;
	}
}
