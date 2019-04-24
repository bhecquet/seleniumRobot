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

import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.remote.RemoteWebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.BasePage;

@PrepareForTest(WebUIDriver.class)
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
		
		PowerMockito.mockStatic(WebUIDriver.class);
		Mockito.when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
		Mockito.when(WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(webUiDriver);
		
		// use this to test abstract class
		page = Mockito.mock(BasePage.class, Mockito.CALLS_REAL_METHODS);
		
		Mockito.when(driver.switchTo()).thenReturn(targetLocator);
		Mockito.when(driver.findElement(Mockito.any())).thenReturn(element);
		Mockito.when(targetLocator.alert()).thenReturn(alert);
		Mockito.when(alert.getText()).thenReturn("alert text");
		Mockito.when(element.getText()).thenReturn("element text");

	}

	/**
	 * check we return to default content
	 */
	@Test(groups={"ut"})
	public void testAcceptAlert() {
		page.acceptAlert();
		Mockito.verify(alert).accept();
		Mockito.verify(targetLocator).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testDismissAlert() {
		page.cancelConfirmation();
		Mockito.verify(alert).dismiss();
		Mockito.verify(targetLocator).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testAlertText() {
		Assert.assertEquals(page.getAlertText(), "alert text");
	}
	
	@Test(groups={"ut"}, expectedExceptions=AssertionError.class)
	public void testAssertHTML() throws Exception {
		try {
			page.assertHTML(false, "error");
		} finally {
			PowerMockito.verifyPrivate(page).invoke("capturePageSnapshot");
		}
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
