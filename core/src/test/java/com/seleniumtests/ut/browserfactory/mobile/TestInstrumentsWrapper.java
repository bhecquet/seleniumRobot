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
package com.seleniumtests.ut.browserfactory.mobile;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.Platform;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.mobile.InstrumentsWrapper;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

//@PrepareForTest({OSCommand.class, InstrumentsWrapper.class, OSUtility.class})
public class TestInstrumentsWrapper extends MockitoTest {
	

	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInstrumentsNotFound() {
//		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("instruments")).thenReturn("instruments: command not found");
		new InstrumentsWrapper();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testNotOnMac() {
//		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		new InstrumentsWrapper();
	}
	

	@Test(groups={"ut"})
	public void testOnMac() {
//		PowerMockito.mockStatic(OSUtility.class);
//		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("xcrun")).thenReturn("Usage: xcrun [options] <tool name> ... arguments ...");
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.MAC);
		
		new InstrumentsWrapper();
	}
	
	
	@Test(groups={"ut"})
	public void testiOSDeviceRetrieving() throws IOException {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.mockStatic(OSUtility.class);
		when(OSCommand.executeCommandAndWait("xcrun")).thenReturn("Usage: xcrun [options] <tool name> ... arguments ...");
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.MAC);
		
		String deviceList = GenericTest.readResourceToString("tu/devices.json");
		
		when(OSCommand.executeCommandAndWait("xcrun simctl list devices available --json")).thenReturn(deviceList);
		
		InstrumentsWrapper wrapper = new InstrumentsWrapper();
		List<MobileDevice> devs = wrapper.parseIosDevices();
		
		Assert.assertEquals(devs.size(), 10);
		Assert.assertEquals(devs.get(1).getName(), "iPhone 14");
		Assert.assertEquals(devs.get(8).getName(), "iPad Pro");
		Assert.assertEquals(devs.get(1).getVersion(), "16.2");
		Assert.assertEquals(devs.get(1).getId(), "F588AFB8-5DF5-475C-B01C-28707D0CBD19");
		Assert.assertEquals(devs.get(1).getPlatform(), "iOS");
		Assert.assertEquals(devs.get(1).getBrowsers().get(0).getBrowser(), BrowserType.SAFARI);
	}
	
}
