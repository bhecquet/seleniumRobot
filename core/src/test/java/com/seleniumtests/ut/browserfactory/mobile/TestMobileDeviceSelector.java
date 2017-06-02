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
package com.seleniumtests.ut.browserfactory.mobile;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.mobile.AdbWrapper;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.browserfactory.mobile.MobileDeviceSelector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.osutility.OSCommand;

import io.appium.java_client.remote.MobileCapabilityType;

@PrepareForTest({AdbWrapper.class, OSCommand.class})
public class TestMobileDeviceSelector extends MockitoTest {
	
	@Mock
	private AdbWrapper adbWrapper;
	
	@InjectMocks
	private MobileDeviceSelector deviceSelector;
	
	/**
	 * Test that an error is raised when no matching device is found
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectNonExistingAndroidDevice() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		DesiredCapabilities requestedCaps = new DesiredCapabilities();
		requestedCaps.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");

		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(true);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	/**
	 * Test that an error is raised when no mobile capability is found (DEVICE_NAME, PLATFORM_NAME, PLATFORM_VERSION)
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectWithoutMobileCapabilities() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		DesiredCapabilities requestedCaps = new DesiredCapabilities();
		requestedCaps.setCapability(CapabilityType.BROWSER_NAME, "firefox");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(true);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectAndroidNotReady() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("Nexus 5", "1234", "android", "5.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		DesiredCapabilities requestedCaps = new DesiredCapabilities();
		requestedCaps.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
		
		deviceSelector.setAndroidReady(false);
		deviceSelector.setIosReady(true);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testSelectiOSNotReady() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
//		deviceList.add(new MobileDevice("IPhone 6", "0000", "ios", "10.2"));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		DesiredCapabilities requestedCaps = new DesiredCapabilities();
		requestedCaps.setCapability(MobileCapabilityType.PLATFORM_NAME, "ios");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(false);
		
		deviceSelector.getRelevantMobileDevice(requestedCaps);
	}
	
	/**
	 * Test that when several devices exists with the same capabilities, the first one is selected
	 * Test platform name with different cases
	 */
	@Test(groups={"ut"})
	public void testSelectFirstMatchingAndroidDevice() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("IPhone 6", "0000", "ios", "10.2", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 5", "1234", "Android", "5.0", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 7", "1235", "Android", "6.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		DesiredCapabilities requestedCaps = new DesiredCapabilities();
		requestedCaps.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(true);
		
		Assert.assertEquals(deviceSelector.getRelevantMobileDevice(requestedCaps), deviceList.get(1));
	}
	
	/**
	 * Test that only the device that matches all capabilities is selected
	 */
	@Test(groups={"ut"})
	public void testSelectMostMatchingAndroidDevice() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("IPhone 6", "0000", "ios", "10.2", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 5", "1234", "Android", "5.0", new ArrayList<>()));
		deviceList.add(new MobileDevice("Nexus 7", "1235", "android", "6.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		// requested caps
		DesiredCapabilities requestedCaps = new DesiredCapabilities();
		requestedCaps.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
		requestedCaps.setCapability(MobileCapabilityType.PLATFORM_VERSION, "6.0");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(true);
		
		Assert.assertEquals(deviceSelector.getRelevantMobileDevice(requestedCaps), deviceList.get(2));
	}
	
	/**
	 * Test the update of capabilities with device properties
	 * input capabilities only know the name, and from that data, we expect to get the platform and version
	 * plus an updated version of device name with the id instead as it's the property expected by ADB to 
	 * communicate with any device
	 */
	@Test(groups={"ut"})
	public void testCapabilitiesUpdate() {
		// available devices
		List<MobileDevice> deviceList = new ArrayList<>();
		deviceList.add(new MobileDevice("nexus 5", "1234", "android", "5.0", new ArrayList<>()));
		when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		
		DesiredCapabilities requestedCaps = new DesiredCapabilities();
		requestedCaps.setCapability(MobileCapabilityType.DEVICE_NAME, "Nexus 5");
		
		deviceSelector.setAndroidReady(true);
		deviceSelector.setIosReady(true);
		
		DesiredCapabilities updatedCaps = deviceSelector.updateCapabilitiesWithSelectedDevice(requestedCaps);
		Assert.assertEquals(updatedCaps.getCapability(MobileCapabilityType.PLATFORM_NAME), "android");
		Assert.assertEquals(updatedCaps.getCapability(MobileCapabilityType.DEVICE_NAME), "1234");
		Assert.assertEquals(updatedCaps.getCapability(MobileCapabilityType.PLATFORM_VERSION), "5.0");
		
	}
	
	@Test(groups={"ut"})
	public void testiOSDeviceRetrieving() {
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("instruments -s devices")).thenReturn("Mac mini de Thoraval [CBFA063D-2535-5FD8-BA05-CE8D3683D6BA]\n" 
 + "Apple TV 1080p (10.2) [6444F65D-DA15-4505-8307-4520FD346ACE] (Simulator)\n" 
 + "iPad Air (10.3) [77FCE24A-EC11-490B-AFA6-D5950EACD33D] (Simulator)\n"
 + "iPad Air 2 (10.3) [EF9D4D32-285D-4D08-B145-1B704A6E1B14] (Simulator)\n"
 + "iPad Pro (12.9 inch) (10.3) [D723D123-C176-4CDD-937E-34060F9AC31A] (Simulator)\n"
 + "iPhone 5 (10.3) [5621105C-180C-438D-9AC4-1361F9BFA553] (Simulator)\n"
 + "iPhone 6 (10.3) [8CAD959E-4AD2-4CA1-9072-300E1A738027] (Simulator)\n"
 + "iPhone 6 Plus (10.3) [FEB56FF6-5705-45F6-8D0F-4958ACA91FF5] (Simulator)\n"
 + "iPhone 7 (10.3) [D11D74FE-A620-403C-BAAA-1E0FF4486238] (Simulator)\n"
 + "iPhone 7 (10.3) + Apple Watch Series 2 - 38mm (3.2) [84DA8FFA-F743-4EA6-8E98-DC38165B9ACB] (Simulator)\n"
 + "iPhone SE (10.3.1) [2FD40F1E-45A2-4580-95D4-5B850E438953] (Simulator)");
		
		deviceSelector.setAndroidReady(false);
		deviceSelector.setIosReady(true);
		List<MobileDevice> devs = deviceSelector.parseIosDevices();
		
		Assert.assertEquals(devs.size(), 10);
		Assert.assertEquals(devs.get(8).getName(), "iPhone 7");
		Assert.assertEquals(devs.get(3).getName(), "iPad Pro");
		Assert.assertEquals(devs.get(8).getVersion(), "10.3");
		Assert.assertEquals(devs.get(8).getId(), "84DA8FFA-F743-4EA6-8E98-DC38165B9ACB");
		Assert.assertEquals(devs.get(8).getPlatform(), "iOS");
		Assert.assertEquals(devs.get(8).getBrowsers().get(0), "safari");
		Assert.assertEquals(devs.get(9).getVersion(), "10.3.1");
	}
}
