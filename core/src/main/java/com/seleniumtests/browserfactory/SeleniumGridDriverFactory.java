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
package com.seleniumtests.browserfactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.screenshots.ScreenShotRemoteWebDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.helper.WaitHelper;

public class SeleniumGridDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    public SeleniumGridDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }

    /**
     * Create a capability depending on the browser type.
     * @param webDriverConfig
     * @return the capability for a given browser
     */
    public DesiredCapabilities createCapabilityByBrowser(DriverConfig webDriverConfig, DesiredCapabilities capabilities){

    	switch (webDriverConfig.getBrowser()) {

	        case FIREFOX :
	            capabilities.merge(new FirefoxCapabilitiesFactory().createCapabilities(webDriverConfig));
	            break;
	
	        case INTERNET_EXPLORER :
	        	capabilities.merge(new IECapabilitiesFactory().createCapabilities(webDriverConfig));
	            break;
	
	        case CHROME :
	        	capabilities.merge(new ChromeCapabilitiesFactory().createCapabilities(webDriverConfig));
	            break;
	
	        case HTMLUNIT :
	        	capabilities.merge(new HtmlUnitCapabilitiesFactory().createCapabilities(webDriverConfig));
	            break;
	
	        case SAFARI :
	        	capabilities.merge(new SafariCapabilitiesFactory().createCapabilities(webDriverConfig));
	            break;
	
	        case PHANTOMJS :
	        	capabilities.merge(new PhantomJSCapabilitiesFactory().createCapabilities(webDriverConfig));
	            break;
	
	        default :
	            break;
	    }
    	
    	return capabilities;
    }
    
    /**
     * Creates capabilities specific to seleniumGrid
     * For example, Appium needs PLATFORM_NAME and PLATFORM_VERSION capabilities, but seleniumGrid matcher
     * looks at PLATFORM and VERSION capabilities. This method adds them
     * OS version is only updated for mobile. It has no real sense on desktop
     * @return
     */
    private DesiredCapabilities createSpecificGridCapabilities(DriverConfig webDriverConfig) {
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	capabilities.setCapability(CapabilityType.PLATFORM, webDriverConfig.getPlatform().toLowerCase());
    	
    	if (SeleniumTestsContextManager.isMobileTest()) {
    		capabilities.setCapability(CapabilityType.VERSION, webDriverConfig.getMobilePlatformVersion());
    	}
    	
    	return capabilities;
    }
    
    @Override
    public WebDriver createWebDriver() {
        DriverConfig webDriverConfig = this.getWebDriverConfig();
        URL url;

        try {
			url = new URL(webDriverConfig.getHubUrl());
		} catch (MalformedURLException e1) {
			throw new ConfigurationException(String.format("Hub url '%s' is invalid: %s", webDriverConfig.getHubUrl(), e1.getMessage()));
		}

        DesiredCapabilities capabilities = createSpecificGridCapabilities(webDriverConfig);
        if (SeleniumTestsContextManager.isDesktopWebTest()) {
        	capabilities = createCapabilityByBrowser(webDriverConfig, capabilities);
        } else if (SeleniumTestsContextManager.isMobileTest()) {
        	if("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
        		capabilities = new AndroidCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig);
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	capabilities = new IOsCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig);
	        } else {
	        	throw new ConfigurationException(String.format("Platform %s is unknown for mobile tests", webDriverConfig.getPlatform()));
	        }
        } else {
        	throw new ConfigurationException("Remote driver is supported for mobile and desktop web tests");
        }
        

        if ((BrowserType.FIREFOX).equals(webDriverConfig.getBrowser())) {
            driver = getDriverFirefox(url, capabilities);
        } else {
            driver = new ScreenShotRemoteWebDriver(url, capabilities);
        }

        setImplicitWaitTimeout(webDriverConfig.getImplicitWaitTimeout());
        if (webDriverConfig.getPageLoadTimeout() >= 0) {
            setPageLoadTimeout(webDriverConfig.getPageLoadTimeout(), webDriverConfig.getBrowser());
        }

        this.setWebDriver(driver);

        runWebDriver(url);

        return driver;
    }
    
    private WebDriver getDriverFirefox(URL url, DesiredCapabilities capability){
    	driver = null;
    	try {
            driver = new ScreenShotRemoteWebDriver(url, capability);
        } catch (RuntimeException e) {
            if (e.getMessage().contains(
                        "Unable to connect to host localhost on port 7062 after 45000 ms. Firefox console output")) {
                TestLogging.log("Firefox Driver creation got port customexception, retry after 5 seconds");
                WaitHelper.waitForSeconds(5);
                driver = new ScreenShotRemoteWebDriver(url, capability);
            } else {
                throw e;
            }
        }
    	return driver;
    }
    
    private void runWebDriver(URL url){
    	String hub = url.getHost();
        int port = url.getPort();

        // logging node ip address:
        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpHost host = new HttpHost(hub, port);
            
            String sessionUrl = "http://" + hub + ":" + port + "/grid/api/testsession?session=";
            URL session = new URL(sessionUrl + ((RemoteWebDriver) driver).getSessionId());
            BasicHttpEntityEnclosingRequest req;
            req = new BasicHttpEntityEnclosingRequest("POST", session.toExternalForm());

            org.apache.http.HttpResponse response = client.execute(host, req);
            String responseContent = EntityUtils.toString(response.getEntity());
            
            JSONObject object = new JSONObject(responseContent);
            String proxyId = (String) object.get("proxyId");
            String node = proxyId.split("//")[1].split(":")[0];
            String browserName = ((RemoteWebDriver) driver).getCapabilities().getBrowserName();
            String version = ((RemoteWebDriver) driver).getCapabilities().getVersion();
            TestLogging.info("WebDriver is running on node " + node + ", " + browserName + version + ", session "
                    + ((RemoteWebDriver) driver).getSessionId());
            
        } catch (Exception ex) {
        	logger.error(ex);
        } finally {
        	client.close();
        }
    }

    protected void setPageLoadTimeout(final long timeout, final BrowserType type) {
        switch (type) {

            case CHROME :
            case FIREFOX :
            case INTERNET_EXPLORER :
            	setPageLoadTimeoutCommonBrowser(timeout);
                break;

            default :
        }
    }
    
    protected void setPageLoadTimeoutCommonBrowser(final long timeout) {
        try {
            driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
        } catch (UnsupportedCommandException e) {
        	logger.error(e);
        }
    }

	@Override
	protected WebDriver createNativeDriver() {
		return null;
	}
}
