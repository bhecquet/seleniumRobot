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
package com.seleniumtests.browserfactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.auth.AuthenticationException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.MobileCapabilityType;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;

public class BrowserStackCapabilitiesFactory extends ICloudCapabilityFactory {
	
	private static final Pattern REG_USER_PASSWORD = Pattern.compile("https://([^:/]++):([^:@]++)@hub.browserstack.com/wd/hub");
	private static final String BROWSERSTACK_UPLOAD_URL = "https://api-cloud.browserstack.com/app-automate/upload";
	
    public BrowserStackCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
 

	@Override
    public DesiredCapabilities createCapabilities() {

        final DesiredCapabilities capabilities = new DesiredCapabilities();

        // platform must be the concatenation of 'os' and 'os_version' that browserstack undestands
        String platform = webDriverConfig.getPlatform();
        String platformVersion = null;
        String platformName = null;
        
        if (SeleniumTestsContextManager.isMobileTest()) {
	        if(ANDROID_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
		        
	        	Capabilities androidCaps = new AndroidCapabilitiesFactory(webDriverConfig).createCapabilities();
	        	capabilities.merge(androidCaps);

	        	capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UIAutomator2");
	            
	        } else if (IOS_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	Capabilities iosCaps = new IOsCapabilitiesFactory(webDriverConfig).createCapabilities();
	        	capabilities.merge(iosCaps);
	        }
	        
	        capabilities.setCapability("device", webDriverConfig.getDeviceName()); // pour deviceName
			capabilities.setCapability("realMobile", "true");
			capabilities.setCapability("os_version", webDriverConfig.getMobilePlatformVersion());
			capabilities.setCapability("os", webDriverConfig.getPlatform());
        	
        } else {
        	if (platform.toLowerCase().startsWith("windows")) {
	        	platformVersion = platform.toLowerCase().replace("windows", "").trim();
	        	platformName = "Windows";
	        	
	        } else if (platform.toLowerCase().startsWith("os x")) {
	        	platformVersion = platform.toLowerCase().replace("os x", "").trim();
	        	platformName = "OS X";
	        	
	        } else {
	        	throw new ConfigurationException("Only Windows and Mac are supported desktop platforms ('Windows xxx' or 'OS X xxx'). See https://www.browserstack.com/automate/capabilities for details. 'platform' param is " + platform);
	        }
        	
        	if (webDriverConfig.getBrowserVersion() != null) {
            	capabilities.setCapability("browser_version", webDriverConfig.getBrowserVersion());
            }
            capabilities.setCapability("os", platformName);
            capabilities.setCapability("os_version", platformVersion); 
        }
        
        capabilities.setCapability("browserName", webDriverConfig.getBrowserType());
        capabilities.setCapability("name", SeleniumTestsContextManager.getThreadContext().getTestMethodSignature());
        capabilities.setCapability("project", SeleniumTestsContextManager.getApplicationName());
        
        // we need to upload something
 		if (capabilities.getCapability(MobileCapabilityType.APP) != null) {
 			String appUrl = uploadFile((String)capabilities.getCapability(MobileCapabilityType.APP));
 			capabilities.setCapability("app", appUrl);

 		}
        
        return capabilities;
    }
	
	 /**
     * Upload application to saucelabs server
     * @param targetAppPath
     * @param serverURL
     * @return
     * @throws IOException
     * @throws AuthenticationException 
     */
    private String uploadFile(String application) {

    	// extract user name and password from getWebDriverGrid
    	Matcher matcher = REG_USER_PASSWORD.matcher(SeleniumTestsContextManager.getThreadContext().getWebDriverGrid().get(0));
    	String user;
    	String password;
    	if (matcher.matches()) {
    		user = matcher.group(1);
    		password = matcher.group(2);
    	} else {
    		throw new ConfigurationException("webDriverGrid variable does not have the right format for connecting to sauceLabs: \"https://<user>:<token>@hub.browserstack.com/wd/hub\"");
    	}
    	
    	try (UnirestInstance unirest = Unirest.spawnInstance();){
    		
    		String proxyHost = System.getProperty("https.proxyHost");
    		String proxyPort = System.getProperty("https.proxyPort");
    		if (proxyHost != null && proxyPort != null) {
    			unirest.config().proxy(proxyHost, Integer.valueOf(proxyPort));
    		}
    		
    		HttpResponse<JsonNode> jsonResponse = unirest.post(BROWSERSTACK_UPLOAD_URL)
    					.basicAuth(user, password)
    					.field("file", new File(application))
    					.asJson()
    					.ifFailure(response -> {
    						throw new ConfigurationException(String.format("Application file upload failed: %s", response.getStatusText()));
    						})
    					.ifSuccess(response -> {
    						logger.info("Application successfuly uploaded to Saucelabs");
    						
    					});
    		return jsonResponse.getBody().getObject().getString("app_url");
    		

		} catch (UnirestException e) {
			throw new ConfigurationException("Application file upload failed: " + e.getMessage());
		}
    	
    	
    }
}
