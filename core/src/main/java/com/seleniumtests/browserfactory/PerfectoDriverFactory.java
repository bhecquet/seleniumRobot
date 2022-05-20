package com.seleniumtests.browserfactory;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class PerfectoDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {


	
	public PerfectoDriverFactory(DriverConfig cfg) {
		super(cfg);
	}
	
	   
    @Override
    protected WebDriver createNativeDriver() {
    	

    	try {
	        if(ICloudCapabilityFactory.ANDROID_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
	            return new AndroidDriver<WebElement>(new URL(webDriverConfig.getHubUrl().get(0)), driverOptions);
	            
	        } else if (ICloudCapabilityFactory.IOS_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	return new IOSDriver<WebElement>(new URL(webDriverConfig.getHubUrl().get(0)), driverOptions);
	            
	        } else {
	        	return new RemoteWebDriver(new URL(webDriverConfig.getHubUrl().get(0)), driverOptions);
	        }
	
    	} catch (MalformedURLException e) {
    		throw new DriverExceptions("Error creating driver: " + e.getMessage());
    	}
    }
	

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		return new PerfectoCapabilitiesFactory(webDriverConfig);
		
	}

}
