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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.appium.java_client.remote.options.SupportsAppOption;
import org.apache.http.auth.AuthenticationException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverConfig;

import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;

public class SauceLabsCapabilitiesFactory extends ICloudCapabilityFactory {

	private static final Pattern REG_USER_PASSWORD = Pattern.compile("https://([^:/]++):([^:@]++)@ondemand.(.*?).saucelabs.com:443/wd/hub");
	private static final String SAUCE_UPLOAD_URL = "https://api.%s.saucelabs.com/v1/storage/upload";
	
    public SauceLabsCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

    /**
     * https://docs.saucelabs.com/dev/test-configuration-options/
     */
	@Override
    public DesiredCapabilities createCapabilities() {

        final DesiredCapabilities capabilities = new DesiredCapabilities();

        
        MutableCapabilities sauceOptions = new MutableCapabilities();
        capabilities.setCapability("sauce:options", sauceOptions);
		
		if(ANDROID_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
	        
        	Capabilities androidCaps = new AndroidCapabilitiesFactory(webDriverConfig).createCapabilities();
        	capabilities.merge(androidCaps);
            
        } else if (IOS_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
        	Capabilities iosCaps = new IOsCapabilitiesFactory(webDriverConfig).createCapabilities();
        	capabilities.merge(iosCaps);
        	
        	
        } else {
        	capabilities.setCapability("browserName", webDriverConfig.getBrowserType());
            capabilities.setCapability("platform", webDriverConfig.getPlatform());
            capabilities.setCapability("version", webDriverConfig.getVersion());
            capabilities.setCapability("name", SeleniumTestsContextManager.getThreadContext().getTestMethodSignature());

        }
		
		// we need to upload something
		Optional<String> applicationCapability = ((SupportsAppOption)capabilities).getApp();
		if (applicationCapability.isPresent() && applicationCapability.get() != null) {

			boolean uploadApp = isUploadApp(capabilities);
			
			if (uploadApp) {
				uploadFile(applicationCapability.get());
			}
			((SupportsAppOption)capabilities).setApp("storage:filename=" + new File(applicationCapability.get()).getName()); //  saucelabs waits for app capability a special file: sauce-storage:<filename>

		}
        
        return capabilities;
    }
	

    /**
     * Upload application to saucelabs server
     * @return
     * @throws IOException
     * @throws AuthenticationException 
     */
    private void uploadFile(String application) {

    	// extract user name and password from getWebDriverGrid
    	Matcher matcher = REG_USER_PASSWORD.matcher(SeleniumTestsContextManager.getThreadContext().getWebDriverGrid().get(0));
    	String user;
    	String password;
    	String datacenter;
    	if (matcher.matches()) {
    		user = matcher.group(1);
    		password = matcher.group(2);
    		datacenter = matcher.group(3);
    	} else {
    		throw new ConfigurationException("webDriverGrid variable does not have the right format for connecting to sauceLabs: \"https://<user>:<token>@ondemand.<datacenter>.saucelabs.com:443/wd/hub\"");
    	}
    	
    	try (UnirestInstance unirest = Unirest.spawnInstance();){
    		
    		String proxyHost = System.getProperty("https.proxyHost");
    		String proxyPort = System.getProperty("https.proxyPort");
    		if (proxyHost != null && proxyPort != null) {
    			unirest.config().proxy(proxyHost, Integer.valueOf(proxyPort));
    		}
    		unirest.post(String.format(SAUCE_UPLOAD_URL, datacenter))
    					.basicAuth(user, password)
    					.field("payload", new File(application))
    					.field("name", new File(application).getName())
    					.asString()
    					.ifFailure(response -> {
    						throw new ConfigurationException(String.format("Application file upload failed: %s", response.getStatusText()));
    						})
    					.ifSuccess(response -> {
    						logger.info("Application successfuly uploaded to Saucelabs");
    					});

		} catch (UnirestException e) {
			throw new ConfigurationException("Application file upload failed: " + e.getMessage());
		}
    }

}
