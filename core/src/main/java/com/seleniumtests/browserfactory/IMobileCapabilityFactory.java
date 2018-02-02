package com.seleniumtests.browserfactory;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.MobileCapabilityType;

public abstract class IMobileCapabilityFactory extends ICapabilitiesFactory {

	public IMobileCapabilityFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	protected abstract String getAutomationName();
	
	protected abstract MutableCapabilities getSystemSpecificCapabilities();
	
	protected abstract MutableCapabilities getBrowserSpecificCapabilities();
	  
	@Override
    public MutableCapabilities createCapabilities() {

    	String app = webDriverConfig.getApp();
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, getAutomationName());
    
    	if (app != null && !"".equals(app.trim())) {
	    	if (webDriverConfig.isFullReset()) {
	    		capabilities.setCapability(MobileCapabilityType.FULL_RESET, "true");
	    	} else {
	    		capabilities.setCapability(MobileCapabilityType.FULL_RESET, "false");
	    	}
    	}
    	
    	// Set up version and device name else appium server would pick the only available emulator/device
        // Both of these are ignored for android for now
    	capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, webDriverConfig.getPlatform());
    	capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, webDriverConfig.getMobilePlatformVersion());
    	capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, webDriverConfig.getDeviceName());
    	capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, webDriverConfig.getNewCommandTimeout());
    	
    	// in case app has not been specified for cloud provider
        if (capabilities.getCapability(MobileCapabilityType.APP) == null && app != null) {
        	capabilities.setCapability(MobileCapabilityType.APP, app.replace("\\", "/"));
        }
    	
    	// do not configure application and browser as they are mutualy exclusive
        if (app == null || "".equals(app.trim()) && webDriverConfig.getBrowser() != BrowserType.NONE) {
        	capabilities.setCapability(CapabilityType.BROWSER_NAME, webDriverConfig.getBrowser().toString().toLowerCase());
        	capabilities.merge(getBrowserSpecificCapabilities());
        } else {
        	capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
        }
        
        // add OS specific capabilities
        capabilities.merge(getSystemSpecificCapabilities());
        
        return capabilities;
    }

}
