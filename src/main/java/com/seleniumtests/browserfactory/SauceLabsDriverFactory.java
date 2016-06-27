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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class SauceLabsDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

	private static final String SAUCE_UPLOAD_URL = "https://saucelabs.com/rest/v1/storage/%s/%s?overwrite=true";
	private static final Pattern REG_USER_PASSWORD = Pattern.compile("http://(.*?):(.*?)@ondemand.saucelabs.com:80/wd/hub");

    public SauceLabsDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    private DesiredCapabilities cloudSpecificCapabilities() {
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	
    	if (webDriverConfig.getPlatform().equalsIgnoreCase("android")) {
	    	capabilities.setCapability("app-package", webDriverConfig.getAppPackage());
	        capabilities.setCapability("app-activity", webDriverConfig.getAppActivity());
	        capabilities.setCapability("app-wait-activity", webDriverConfig.getAppWaitActivity());
    	} else if (webDriverConfig.getPlatform().equalsIgnoreCase("ios")) {
    		capabilities = DesiredCapabilities.iphone();
    	}
        capabilities.setCapability("app", "sauce-storage:" + new File(webDriverConfig.getApp()).getName()); //  saucelabs waits for app capability a special file: sauce-storage:<filename>
        return capabilities;
    }
  
    /**
     * Upload application to saucelabs server
     * @param targetAppPath
     * @param serverURL
     * @param testdroid_apikey
     * @return
     * @throws IOException
     * @throws AuthenticationException 
     */
    protected static String uploadFile(String targetAppPath) throws IOException, AuthenticationException {

    	// extract user name and password from appiumServerURL
    	Matcher matcher = REG_USER_PASSWORD.matcher(SeleniumTestsContextManager.getThreadContext().getAppiumServerURL());
    	String user;
    	String password;
    	if (matcher.matches()) {
    		user = matcher.group(1);
    		password = matcher.group(2);
    	} else {
    		throw new ConfigurationException("appiumServerURL variable does not have the right format for connecting to sauceLabs");
    	}
    	
    	FileEntity entity = new FileEntity(new File(targetAppPath));
    	UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user, password);
    	
    	HttpPost request = new HttpPost(String.format(SAUCE_UPLOAD_URL, user, new File(targetAppPath).getName()));
        request.setEntity(entity);
        request.addHeader(new BasicScheme().authenticate(creds, request, null));
        request.addHeader("Content-Type", "application/octet-stream");

        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(request);
        
        if (response.getStatusLine().getStatusCode() != 200) {
        	client.close();
        	throw new ConfigurationException("Application file upload failed: " + response.getStatusLine().getReasonPhrase());
        }
        client.close();

        return "";

    }

    protected WebDriver createNativeDriver() throws MalformedURLException {
    	
    	DesiredCapabilities capabilities;
    	if (webDriverConfig.getTestType().family().equals(TestType.APP)) {
    		capabilities = cloudSpecificCapabilities();
    		try {
				uploadFile(webDriverConfig.getApp());
			} catch (IOException | AuthenticationException e) {
				throw new ConfigurationException("Error while uploading mobile application: " + e.getMessage());
			}
    	} else {
    		capabilities = new DesiredCapabilities();
    	}

        if(webDriverConfig.getPlatform().equalsIgnoreCase("android")){
            return new AndroidDriver(new URL(webDriverConfig.getAppiumServerURL()), new AndroidCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig));
        } else if (webDriverConfig.getPlatform().equalsIgnoreCase("ios")){
            return new IOSDriver(new URL(webDriverConfig.getAppiumServerURL()), new IOsCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig));
        }

        return new RemoteWebDriver(new URL(webDriverConfig.getAppiumServerURL()), new SauceLabsCapabilitiesFactory().createCapabilities(webDriverConfig));

    }

    @Override
    public WebDriver createWebDriver() {
        final DriverConfig cfg = this.getWebDriverConfig();

        try {
            driver = createNativeDriver();
        } catch (final MalformedURLException me){
            throw new DriverExceptions("Problem with creating driver", me);
        }

        setImplicitWaitTimeout(cfg.getImplicitWaitTimeout());
        if (cfg.getPageLoadTimeout() >= 0) {
            setPageLoadTimeout(cfg.getPageLoadTimeout());
        }

        this.setWebDriver(driver);
        return driver;
    }

    protected void setPageLoadTimeout(final long timeout) {
        try {
            driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
        } catch (WebDriverException e) {
            // chromedriver does not support pageLoadTimeout
        }
    }

}
