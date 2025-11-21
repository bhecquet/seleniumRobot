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
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;

public class TestCompositeActions extends MockitoTest {

	
	@Mock
	private RemoteWebElement element;

	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private BrowserInfo browserInfo;
	
	private CustomEventFiringWebDriver eventDriver;

	private MockedStatic<WebUIDriver> mockedWebUIDriver;

	@BeforeMethod(groups={"ut"})
	private void init() {

		mockedWebUIDriver = mockStatic(WebUIDriver.class);
		when(driver.getCapabilities()).thenReturn(new ChromeOptions()); // add capabilities to allow augmenting driver
		doCallRealMethod().when(driver).perform(ArgumentMatchers.anyCollection());
		eventDriver = spy(new CustomEventFiringWebDriver(driver));
		mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(eventDriver.getBrowserInfo()).thenReturn(browserInfo);
		when(browserInfo.getBrowser()).thenReturn(BrowserType.CHROME);
		
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	private void closeMocks() {
		mockedWebUIDriver.close();
	}
	
	/**
	 * Checks that CompositeAction.updateHandles() aspect is called when 
	 * a click is done in a composite action
	 */
	@Test(groups={"ut"})
	public void testUpdateHandles() {
		new Actions(eventDriver).click().perform();

		// check handled are updated on click
		verify(eventDriver).updateWindowsHandles();
	}
	
	@Test(groups={"ut"})
	public void testHandlesNotUpdated() {
		new Actions(eventDriver).clickAndHold().perform();
		
		// check handled are not updated when no click is done
		verify(eventDriver, never()).updateWindowsHandles();
	}
	
	/**
	 * Checks that CompositeAction.updateHandles() aspect is called when 
	 * a click is done in a composite action with a driver supporting new actions (the real driver)
	 */
	@Test(groups={"ut"})
	public void testUpdateHandlesNewActions() {
		new Actions(eventDriver.getWebDriver()).click().perform();
		
		// check handles are updated on click
		verify(eventDriver).updateWindowsHandles();
	}
	
	/**
	 * No update when only down or up action is done
	 */
	@Test(groups={"ut"})
	public void testUpdateHandlesNotUpdatedNewActions() {
		new Actions(eventDriver.getWebDriver()).clickAndHold().perform();
		
		// check handled are updated on click
		verify(eventDriver, never()).updateWindowsHandles();
	}
	
	/**
	 * Test replay when error occurs in any part of the action
	 * This mode is used by firefox
	 */
	@Test(groups={"ut"})
	public void testReplayOnPerformWithNewActions() {
		doThrow(new WebDriverException("error clicking")).doNothing().when(eventDriver).perform(anyCollection());
		
		new Actions(eventDriver).click(element).perform();

		verify(eventDriver, times(2)).perform(anyCollection());
	}
}
