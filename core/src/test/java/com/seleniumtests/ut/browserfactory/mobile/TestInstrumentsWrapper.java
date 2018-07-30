/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import java.util.List;

import org.openqa.selenium.Platform;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.mobile.InstrumentsWrapper;
import com.seleniumtests.browserfactory.mobile.MobileDevice;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

@PrepareForTest({OSCommand.class, InstrumentsWrapper.class, OSUtility.class})
public class TestInstrumentsWrapper extends MockitoTest {
	

	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testInstrumentsNotFound() {
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("instruments")).thenReturn("instruments: command not found");
		new InstrumentsWrapper();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testNotOnMac() {
		PowerMockito.mockStatic(OSUtility.class);
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.WINDOWS);
		
		new InstrumentsWrapper();
	}
	

	@Test(groups={"ut"})
	public void testOnMac() {
		PowerMockito.mockStatic(OSUtility.class);
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("instruments")).thenReturn("instruments, version 8.3.2 (62124)");
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.MAC);
		
		new InstrumentsWrapper();
	}
	
	
	@Test(groups={"ut"})
	public void testiOSDeviceRetrieving() {
		PowerMockito.mockStatic(OSCommand.class);
		PowerMockito.mockStatic(OSUtility.class);
		when(OSCommand.executeCommandAndWait("instruments")).thenReturn("instruments, version 8.3.2 (62124)");
		when(OSUtility.getCurrentPlatorm()).thenReturn(Platform.MAC);
		
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
		
		InstrumentsWrapper wrapper = new InstrumentsWrapper();
		List<MobileDevice> devs = wrapper.parseIosDevices();
		
		Assert.assertEquals(devs.size(), 10);
		Assert.assertEquals(devs.get(8).getName(), "iPhone 7");
		Assert.assertEquals(devs.get(3).getName(), "iPad Pro");
		Assert.assertEquals(devs.get(8).getVersion(), "10.3");
		Assert.assertEquals(devs.get(8).getId(), "84DA8FFA-F743-4EA6-8E98-DC38165B9ACB");
		Assert.assertEquals(devs.get(8).getPlatform(), "iOS");
		Assert.assertEquals(devs.get(8).getBrowsers().get(0).getBrowser(), BrowserType.SAFARI);
		Assert.assertEquals(devs.get(9).getVersion(), "10.3.1");
	}
	
}
