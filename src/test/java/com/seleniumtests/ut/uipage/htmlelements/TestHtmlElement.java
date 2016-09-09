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
package com.seleniumtests.ut.uipage.htmlelements;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.remote.RemoteWebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

/**
 * Test class for checking calls to a standard HTMLElement without using any driver
 * Here, we'll concentrate on verifying that refresh calls are done before any action on element
 * @author behe
 *
 */
@PrepareForTest(WebUIDriver.class)
public class TestHtmlElement extends MockitoTest {
	
	@Mock
	private CustomEventFiringWebDriver driver;
	
	@Mock
	private RemoteWebElement element;
	@Mock
	private RemoteWebElement subElement1;
	@Mock
	private RemoteWebElement subElement2;
	
	@Mock
	private Mouse mouse;
	
	@Mock
	private Keyboard keyboard;
	
	@Spy
	private HtmlElement el = new HtmlElement("element", By.id("el"));
	
	@BeforeMethod(alwaysRun=true)
	private void init() {
		// mimic sub elements of the HtmlElement
		List<WebElement> subElList = new ArrayList<WebElement>();
		subElList.add(subElement1);
		subElList.add(subElement2);
		
		// list of elements correspond
		List<WebElement> elList = new ArrayList<WebElement>();
		elList.add(element);
		
		PowerMockito.mockStatic(WebUIDriver.class);
		Mockito.when(WebUIDriver.getWebDriver()).thenReturn(driver);
		Mockito.when(driver.findElement(By.id("el"))).thenReturn(element);
		Mockito.when(driver.findElements(By.name("subEl"))).thenReturn(subElList);
		Mockito.when(driver.findElement(By.name("subEl"))).thenReturn(subElement1);
		Mockito.when(driver.findElements(By.id("el"))).thenReturn(elList);
		Mockito.when(driver.getKeyboard()).thenReturn(keyboard);
		Mockito.when(driver.getMouse()).thenReturn(mouse);
		
		Mockito.when(element.findElement(By.name("subEl"))).thenReturn(subElement1);
		Mockito.when(element.findElements(By.name("subEl"))).thenReturn(subElList);
		Mockito.when(element.getAttribute(Mockito.anyString())).thenReturn("attribute");
		Mockito.when(element.getSize()).thenReturn(new Dimension(10, 10));
		Mockito.when(element.getLocation()).thenReturn(new Point(5, 5));
		Mockito.when(element.getTagName()).thenReturn("h1");
		Mockito.when(element.getText()).thenReturn("text");
		Mockito.when(element.isDisplayed()).thenReturn(true);
		Mockito.when(element.isEnabled()).thenReturn(true);
	}
	
	@AfterMethod(alwaysRun=true) 
	private void finish(Method method) throws Exception {
		// check we called getDriver before using it
		PowerMockito.verifyPrivate(el).invoke("getDriver");
		
		// isElementPresent does not call findElement as we use WebDriverWait
		if (!method.getName().contains("ElementPresent")) {
			PowerMockito.verifyPrivate(el).invoke("findElement");
		}
	}

	@Test(groups={"ut"})
	public void testClick() {
		el.click();
	}
	
	@Test(groups={"ut"})
	public void testSimulateClick() {
		el.simulateClick();
	}
	
	@Test(groups={"ut"})
	public void testSimulateSendKeys() {
		el.simulateSendKeys();
	}

	@Test(groups={"ut"})
	public void testSimulateMoveToElement() {
		el.simulateMoveToElement(1, 1);
	}
	
	@Test(groups={"ut"})
	public void testFindElementsBy() {
		Assert.assertEquals(el.findElements(By.name("subEl")).size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testFindSubElement() {
		HtmlElement subEl = el.findElement(By.name("subEl"));
		Assert.assertEquals(subEl.getElement(), subElement1);
	}
	
	@Test(groups={"ut"})
	public void testFindNthSubElement() {
		HtmlElement subEl = el.findElement(By.name("subEl"), 1);
		Assert.assertEquals(subEl.getElement(), subElement2);
	}
	
	@Test(groups={"ut"})
	public void testFindElements() {
		Assert.assertEquals(el.findElements().size(), 1);
	}
	
	@Test(groups={"ut"})
	public void testGetAttribute() {
		Assert.assertEquals(el.getAttribute("attr"), "attribute");
	}
	
	@Test(groups={"ut"})
	public void testGetHeight() {
		Assert.assertEquals(el.getHeight(), 10);
	}
	
	@Test(groups={"ut"})
	public void testGetWidth() {
		Assert.assertEquals(el.getWidth(), 10);
	}
	
	@Test(groups={"ut"})
	public void testLocation() {
		Assert.assertEquals(el.getLocation().getX(), 5);
	}
	
	@Test(groups={"ut"})
	public void testTagName() {
		Assert.assertEquals(el.getTagName(), "h1");
	}
	
	@Test(groups={"ut"})
	public void testText() {
		Assert.assertEquals(el.getText(), "text");
	}
	
	@Test(groups={"ut"})
	public void testValue() {
		Assert.assertEquals(el.getValue(), "attribute");
	}
	
	@Test(groups={"ut"})
	public void testIsDisplayed() {
		Assert.assertEquals(el.isDisplayed(), true);
	}
	
	@Test(groups={"ut"})
	public void testIsDisplayedException() {
		Mockito.when(element.isDisplayed()).thenThrow(WebDriverException.class);
		Assert.assertEquals(el.isDisplayed(), false);
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresent() {
		Assert.assertEquals(el.isElementPresent(1), true);
	}
	
	@Test(groups={"ut"})
	public void testIsEnabled() {
		Assert.assertEquals(el.isEnabled(), true);
	}
	
	@Test(groups={"ut"})
	public void testIsTextPresentPattern() {
		Assert.assertEquals(el.isTextPresent("\\w+xt"), true);
	}
	
	@Test(groups={"ut"})
	public void testIsTextPresentText() {
		Assert.assertEquals(el.isTextPresent("text"), true);
	}
	
	@Test(groups={"ut"})
	public void testMouseDown() {
		el.mouseDown();
	}
	
	@Test(groups={"ut"})
	public void testMouseOver() {
		el.mouseOver();
	}
	
	@Test(groups={"ut"})
	public void testMouseUp() {
		el.mouseUp();
	}
	
	@Test(groups={"ut"})
	public void testSimulateMouseOver() {
		el.simulateMouseOver();
	}
	
	@Test(groups={"ut"})
	public void testSendKeys() {
		el.sendKeys("someText");
	}
	
	@Test(groups={"ut"})
	public void testSendKeysWithException() {
		Mockito.when(element.getAttribute("type")).thenThrow(WebDriverException.class);
		el.sendKeys("someText");
		Mockito.verify(element).clear();
	}
	
	@Test(groups={"ut"})
	public void testFindPatternInText() {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\w+)\\w"), "text"), "ex");
	}
	
	@Test(groups={"ut"})
	public void testFindPatternInAttr() {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\w+)\\w"), "attr"), "ttribut");
	}
	
	@Test(groups={"ut"})
	public void testFindPatternInAttrNoMatch() {
		Assert.assertEquals(el.findPattern(Pattern.compile("\\w(\\d+)\\w"), "attr"), "");
	}
	
	
	
}
