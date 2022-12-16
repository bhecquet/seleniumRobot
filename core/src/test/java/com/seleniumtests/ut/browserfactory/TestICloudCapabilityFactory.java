package com.seleniumtests.ut.browserfactory;

import org.mockito.Mock;
import org.openqa.selenium.MutableCapabilities;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.browserfactory.ICloudCapabilityFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.MobileCapabilityType;

public class TestICloudCapabilityFactory extends GenericTest {

	
	private class StubCloudCapabilityFactory extends ICloudCapabilityFactory {
		protected StubCloudCapabilityFactory(DriverConfig webDriverConfig) {
			super(webDriverConfig);
		}

		@Override
		public MutableCapabilities createCapabilities() {
			return null;
		}
		
		public boolean isUploadApp(MutableCapabilities capabilities) {
			return super.isUploadApp(capabilities);
		}

		
	}

	@Mock
	private DriverConfig config;

	@BeforeMethod(groups= {"ut"})
	public void init() {
	
	}
	
	@Test(groups="ut")
	public void testIsUploadApp() {
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, "/home/app.apk");
		boolean upload = new StubCloudCapabilityFactory(config).isUploadApp(caps);
		Assert.assertTrue(upload);
		Assert.assertEquals(caps.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP), "/home/app.apk");
	}
	
	@Test(groups="ut")
	public void testNoUploadApp() {
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, "NO_UPLOAD:/home/app.apk");
		boolean upload = new StubCloudCapabilityFactory(config).isUploadApp(caps);
		Assert.assertFalse(upload);
		Assert.assertEquals(caps.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP), "/home/app.apk");
	}

	
}
