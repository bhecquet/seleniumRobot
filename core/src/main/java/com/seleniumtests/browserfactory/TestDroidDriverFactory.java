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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.testdroid.api.http.MultipartFormDataContent;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class TestDroidDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public TestDroidDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    protected static String uploadFile(String targetAppPath, String serverURL, String testDroidApiKey) throws IOException {
        final HttpHeaders headers = new HttpHeaders().setBasicAuthentication(testDroidApiKey, "");
        
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(request -> {
        		request.setParser(new JsonObjectParser(JSON_FACTORY));
        		request.setHeaders(headers);
        		});
        
        MultipartFormDataContent multipartContent = new MultipartFormDataContent();
        FileContent fileContent = new FileContent("application/octet-stream", new File(targetAppPath));

        MultipartFormDataContent.Part filePart = new MultipartFormDataContent.Part("file", fileContent);
        multipartContent.addPart(filePart);

        HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(serverURL+"/upload"), multipartContent);

        AppiumResponse appiumResponse = request.execute().parseAs(AppiumResponse.class);
        logger.info("File id:" + appiumResponse.uploadStatus.fileInfo.file);

        return appiumResponse.uploadStatus.fileInfo.file;

    }
    
    public static class AppiumResponse {
        Integer status;
        @Key("sessionId")
        String sessionId;

        @Key("value")
        TestDroidDriverFactory.UploadStatus uploadStatus;
    }
    
    public static class UploadedFile {
        @Key("file")
        String file;
    }

    public static class UploadStatus {
        @Key("message")
        String message;
        @Key("uploadCount")
        Integer uploadCount;
        @Key("expiresIn")
        Integer expiresIn;
        @Key("uploads")
        TestDroidDriverFactory.UploadedFile fileInfo;
    }
    
    private DesiredCapabilities cloudSpecificCapabilities(String fileUUID) {
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	
        capabilities.setCapability("testdroid_apiKey", webDriverConfig.getCloudApiKey());
        capabilities.setCapability("testdroid_project", webDriverConfig.getProjectName());
        capabilities.setCapability("testdroid_testrun", "STF Run" + Calendar.getInstance().getTimeInMillis());
        
        // See available devices at: https://cloud.testdroid.com/#public/devices
        capabilities.setCapability("testdroid_device", webDriverConfig.getDeviceName());
        capabilities.setCapability("testdroid_app", fileUUID);
    	
        return capabilities;
    }

    @Override
    protected WebDriver createNativeDriver() {

    	MutableCapabilities capabilities = new MutableCapabilities();
    	
    	// updload application on TestDroid cloud
    	String fileUUID;
    	if (webDriverConfig.getTestType().family().equals(TestType.APP)) {
    		try {
				fileUUID = uploadFile(SeleniumTestsContextManager.getThreadContext().getApp(), 
										webDriverConfig.getAppiumServerURL().split("/wd/hub")[0], 
										webDriverConfig.getCloudApiKey());
				capabilities = cloudSpecificCapabilities(fileUUID);
			} catch (IOException e) {
				logger.warn("application may not have been uploaded", e);
			}
    	} 
    	capabilities.merge(driverOptions);

    	try {
	        if("android".equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	capabilities.setCapability("testdroid_target", "android");
	        	return new AndroidDriver<WebElement>(new URL(webDriverConfig.getAppiumServerURL()), capabilities);
	            
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())) {
	        	capabilities.setCapability("testdroid_target", "ios");
	        	return new IOSDriver<WebElement>(new URL(webDriverConfig.getAppiumServerURL()), capabilities);  
	            
	        } else {
	        	return new RemoteWebDriver(new URL(webDriverConfig.getAppiumServerURL()), capabilities);
	        }
	        
    	} catch (MalformedURLException e) {
    		throw new DriverExceptions("Error creating driver: " + e.getMessage());
    	}
    }

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		if("android".equalsIgnoreCase(webDriverConfig.getPlatform())){
        	return new AndroidCapabilitiesFactory(webDriverConfig);
            
        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
        	return new IOsCapabilitiesFactory(webDriverConfig);
            
        } else {
        	return new TestDroidCapabilitiesFactory(webDriverConfig);
        }
	}
}
