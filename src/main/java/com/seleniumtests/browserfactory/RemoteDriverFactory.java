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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.screenshots.ScreenShotRemoteWebDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.helper.WaitHelper;

public class RemoteDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    public RemoteDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }

    /**
     * Create a capability depending on the browser type.
     * @param webDriverConfig
     * @return the capability for a given browser
     */
    public DesiredCapabilities createCapabilityByBrowser(DriverConfig webDriverConfig){
    	DesiredCapabilities capability = null;
    	
    	switch (webDriverConfig.getBrowser()) {

	        case FireFox :
	            capability = new FirefoxCapabilitiesFactory().createCapabilities(webDriverConfig);
	            break;
	
	        case InternetExplore :
	            capability = new IECapabilitiesFactory().createCapabilities(webDriverConfig);
	            break;
	
	        case Chrome :
	            capability = new ChromeCapabilitiesFactory().createCapabilities(webDriverConfig);
	            break;
	
	        case HtmlUnit :
	            capability = new HtmlUnitCapabilitiesFactory().createCapabilities(webDriverConfig);
	            break;
	
	        case Safari :
	            capability = new SafariCapabilitiesFactory().createCapabilities(webDriverConfig);
	            break;
	
	        case PhantomJS :
	            capability = new PhantomJSCapabilitiesFactory().createCapabilities(webDriverConfig);
	            break;
	
	        default :
	            break;
	    }
    	
    	return capability;
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

        DesiredCapabilities capability = createCapabilityByBrowser(webDriverConfig);

        if ((BrowserType.FireFox).equals(webDriverConfig.getBrowser())) {
            driver = getDriverFirefox(url, capability);
        } else {
            driver = new ScreenShotRemoteWebDriver(url, capability);
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
            logger.info("WebDriver is running on node " + node + ", " + browserName + version 
            			+ ", session " + ((RemoteWebDriver) driver).getSessionId());
            TestLogging.log("WebDriver is running on node " + node + ", " + browserName + version + ", session "
                    + ((RemoteWebDriver) driver).getSessionId());
            
        } catch (Exception ex) {
        	logger.error(ex);
        } finally {
        	client.close();
        }
    }

    protected void setPageLoadTimeout(final long timeout, final BrowserType type) {
        switch (type) {

            case Chrome :
            case FireFox :
            case InternetExplore :
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
