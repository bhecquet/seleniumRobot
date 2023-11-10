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
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.anyBoolean;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.LabelElement;


//@PrepareForTest(WebUIDriver.class)
public class TestLabelElement extends MockitoTest {
	
	@Mock
	private CustomEventFiringWebDriver driver;
	
	@Mock
	private WebElement element;
	
	@Mock
	private TargetLocator locator;

	@Test(groups={"ut"})
	public void testLabelElement() throws Exception {
//		PowerMockito.mockStatic(WebUIDriver.class);
		Mockito.when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
		Mockito.when(driver.findElement(By.id("label"))).thenReturn(element);
		Mockito.when(element.getText()).thenReturn("textual label");
		Mockito.when(driver.switchTo()).thenReturn(locator);
		
		LabelElement el = Mockito.spy(new LabelElement("label", By.id("label")));
	
		Assert.assertEquals(el.getText(), "textual label");
		
		// check we called getDriver before using it
//		PowerMockito.verifyPrivate(el, Mockito.times(1)).invoke("updateDriver");
	}
}
