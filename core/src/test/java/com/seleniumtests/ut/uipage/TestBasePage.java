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
package com.seleniumtests.ut.uipage;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.remote.RemoteWebElement;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.BasePage;

//@PrepareForTest(WebUIDriver.class)
public class TestBasePage extends MockitoTest {
	
	@Mock
	private CustomEventFiringWebDriver driver;
	
	@Mock
	private WebUIDriver webUiDriver;
	
	@Mock
	private RemoteWebElement element;
	
	@Mock
	private TargetLocator targetLocator;
	
	@Mock
	private Alert alert;
	
	private BasePage page;
	
	@BeforeMethod(groups={"ut"})
	public void init() throws Exception{
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		
//		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
		when(WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(webUiDriver);
		
		// use this to test abstract class
		page = mock(BasePage.class, Mockito.CALLS_REAL_METHODS);
		page.setDriver(driver);
		
		when(driver.findElement(Mockito.any())).thenReturn(element);
		when(driver.switchTo()).thenReturn(targetLocator);
		when(targetLocator.alert()).thenReturn(alert);
		when(alert.getText()).thenReturn("alert text");
		
		when(element.getText()).thenReturn("element text");

	}

	
	@Test(groups={"ut"})
	public void testAlertText() {
		Assert.assertEquals(page.getAlertText(), "alert text");
	}
	
	@Test(groups={"ut"})
	public void testIsTextPresent() {
		Assert.assertTrue(page.isTextPresent("text"));
	}
	
	@Test(groups={"ut"})
	public void testIsTextNotPresent() {
		Assert.assertFalse(page.isTextPresent("toto"));
	}
}
