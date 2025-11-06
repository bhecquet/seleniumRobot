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
package com.seleniumtests.browserfactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seleniumtests.core.SeleniumTestsContext;
import org.openqa.selenium.MutableCapabilities;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverConfig;

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

	/**
	 * Returns the
	 * @param hubUrl	the URL of the Hub
	 * @return	<user>:<key>
	 */
	public static String getAuthenticationString(String hubUrl) {
		Matcher matcher = REG_USER_PASSWORD.matcher(hubUrl);
		String user;
		String key;
		if (matcher.matches()) {
			user = matcher.group(1);
			key = matcher.group(2);
		} else {
			throw new ConfigurationException("webDriverGrid variable does not have the right format for connecting to browserstack: \"https://<user>:<token>@hub.browserstack.com/wd/hub\"");
		}
		return user + ":" + key;
	}

	@Override
    public MutableCapabilities createCapabilities() {

        MutableCapabilities capabilities = createDeviceSpecificCapabilities();

        // platform must be the concatenation of 'os' and 'os_version' that browserstack understands
        String platform = webDriverConfig.getPlatform();
        String platformVersion = null;
        String platformName = null;

		if (platform == null) {
			throw new ConfigurationException("Browserstack needs platform parameter");
		}

		Map<String, String> browserStackOptions = new HashMap<>();
        
        if (SeleniumTestsContextManager.isMobileTest()) {

			// browserstack capabilities
			browserStackOptions.put("deviceName", webDriverConfig.getDeviceName()); // pour deviceName
			browserStackOptions.put("osVersion", webDriverConfig.getMobilePlatformVersion());
			browserStackOptions.put("os", webDriverConfig.getPlatform());
        	
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
				browserStackOptions.put("browserVersion", webDriverConfig.getBrowserVersion());
            }
			browserStackOptions.put("os", platformName);
			browserStackOptions.put("osVersion", platformVersion);
        }

		capabilities.setCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID, SeleniumTestsContext.getContextId().toString());
		capabilities.setCapability(SeleniumRobotCapabilityType.TEST_ID, Objects.requireNonNullElse(SeleniumTestsContextManager.getThreadContext().getTestMethodSignature(), "no-test"));
        capabilities.setCapability("browserName", webDriverConfig.getBrowserType());
		browserStackOptions.put("sessionName", (String) capabilities.getCapability(SeleniumRobotCapabilityType.TEST_ID));
		browserStackOptions.put("buildName", (String) capabilities.getCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID));
		browserStackOptions.put("projectName", SeleniumTestsContextManager.getApplicationName());
        
        // we need to upload something
		Optional<String> applicationOption = getApp(capabilities);
 		if (applicationOption.isPresent() && applicationOption.get() != null) {
 			String appUrl = uploadFile(applicationOption.get());
 			capabilities.setCapability("app", appUrl);
 		}


		browserStackOptions.put("consoleLogs", "info");
		capabilities.setCapability("bstack:options", browserStackOptions);

		 // be sure not to have appium capabilities so that further setCapabilities do not add "appium:" prefix
        return new MutableCapabilities(capabilities);
    }
	
	 /**
     * Upload application to browserstack server
     */
    private String uploadFile(String application) {

    	// extract user name and password from getWebDriverGrid
		String authenticationString = getAuthenticationString(SeleniumTestsContextManager.getThreadContext().getWebDriverGrid().get(0));
		String user = authenticationString.split(":")[0];
		String key = authenticationString.split(":")[1];

    	try (UnirestInstance unirest = Unirest.spawnInstance();){
    		
    		String proxyHost = System.getProperty("https.proxyHost");
    		String proxyPort = System.getProperty("https.proxyPort");
    		if (proxyHost != null && proxyPort != null) {
    			unirest.config().proxy(proxyHost, Integer.parseInt(proxyPort));
    		}
    		
    		HttpResponse<JsonNode> jsonResponse = unirest.post(BROWSERSTACK_UPLOAD_URL)
    					.basicAuth(user, key)
    					.field("file", new File(application))
    					.asJson()
    					.ifFailure(response -> {
    						throw new ConfigurationException(String.format("Application file upload failed: %s", response.getStatusText()));
    						})
    					.ifSuccess(response -> logger.info("Application successfuly uploaded to Saucelabs"));
    		return jsonResponse.getBody().getObject().getString("app_url");
    		

		} catch (UnirestException e) {
			throw new ConfigurationException("Application file upload failed: " + e.getMessage());
		}
    	
    	
    }

	/**
	 * Initialize capabilities for browser or device
	 * @return the capabilities
	 */
	protected MutableCapabilities createDeviceSpecificCapabilities() {
		if (SeleniumTestsContextManager.isDesktopWebTest()) {
			return switch (webDriverConfig.getBrowserType()) {
				case FIREFOX -> new FirefoxCapabilitiesFactory(webDriverConfig).createCapabilities();
				case INTERNET_EXPLORER -> new IECapabilitiesFactory(webDriverConfig).createCapabilities();
				case CHROME -> new ChromeCapabilitiesFactory(webDriverConfig).createCapabilities();
				case SAFARI -> new SafariCapabilitiesFactory(webDriverConfig).createCapabilities();
				case EDGE -> new EdgeCapabilitiesFactory(webDriverConfig).createCapabilities();
				default ->
						throw new ConfigurationException(String.format("Browser %s is unknown for desktop tests", webDriverConfig.getBrowserType()));
			};
		} else if (SeleniumTestsContextManager.isMobileTest()) {
			if ("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
				return new AndroidCapabilitiesFactory(webDriverConfig).createCapabilities();
			} else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())) {
				return new IOsCapabilitiesFactory(webDriverConfig).createCapabilities();
			} else {
				throw new ConfigurationException(String.format("Platform %s is unknown for mobile tests", webDriverConfig.getPlatform()));
			}
		} else {
			throw new ConfigurationException("Wrong test format detected. Should be either mobile or desktop");
		}
	}
}
