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
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.ImageElement;

public class TestImageElement extends MockitoTest {
	
	@Mock
	private CustomEventFiringWebDriver driver;
	
	@Mock
	private WebElement element;

	@Mock
	private TargetLocator locator;

	@Test(groups={"ut"})
	public void testImageElement() throws Exception {
		try (MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class)) {
			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			when(driver.findElement(By.id("img"))).thenReturn(element);
			when(driver.switchTo()).thenReturn(locator);
			when(element.getSize()).thenReturn(new Dimension(10, 10));
			when(element.getDomAttribute("src")).thenReturn("http://nowhere.com/jpg");

			ImageElement el = spy(new ImageElement("image", By.id("img")));

			Assert.assertEquals(el.getHeight(), 10);
			Assert.assertEquals(el.getWidth(), 10);
			Assert.assertEquals(el.getUrl(), "http://nowhere.com/jpg");

			// check we called getDriver before using it
			verify(el, times(3)).updateDriver();
		}
	}
}
