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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.ButtonElement;
import com.seleniumtests.uipage.htmlelements.CheckBoxElement;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.ImageElement;
import com.seleniumtests.uipage.htmlelements.LabelElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.uipage.htmlelements.RadioButtonElement;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;


/**
 * Test class for checking calls to a standard HTMLElement without using any driver
 * Here, we'll concentrate on verifying that refresh calls are done before any action on element
 * @author behe
 *
 */
@PrepareForTest(WebUIDriver.class)
public class TestFrameElement extends MockitoTest {
	
	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private RemoteWebElement element;
	@Mock
	private RemoteWebElement link;
	@Mock
	private RemoteWebElement frameEl;
	@Mock
	private RemoteWebElement frameEl2;

	@Mock
	private TargetLocator locator;

	
	@BeforeMethod(groups={"ut"})
	private void init() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		
		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getWebDriver()).thenReturn(driver);
		when(driver.findElement(By.id("el"))).thenReturn(element);
		when(element.findElement(By.id("link"))).thenReturn(link);
		when(driver.findElement(By.id("frameId"))).thenReturn(frameEl);
		when(driver.findElement(By.id("frameId2"))).thenReturn(frameEl2);
		when(driver.switchTo()).thenReturn(locator);
		
		when(element.getSize()).thenReturn(new Dimension(1, 1));
		when(element.isDisplayed()).thenReturn(true);
		when(link.isDisplayed()).thenReturn(true);
	}

	@Test(groups={"ut"})
	public void testUseElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		el.click();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testUseElementInsideFrameRetryOnError() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		
		Mockito.doThrow(new WebDriverException("fake exception")).doNothing().when(element).click();
		el.click();
		
		// 2 invocations because first call to click raises an error
		verify(locator, times(2)).frame(frameEl);
		verify(locator, times(2)).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testUseElementInside2Frames() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		FrameElement frame2 = new FrameElement("", By.id("frameId2"), frame);
		HtmlElement el = new HtmlElement("", By.id("el"), frame2);
		el.click();
		
		verify(locator).frame(frameEl);
		verify(locator).frame(frameEl2);
		verify(locator).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testUseElementOutsideFrame() throws Exception {
		HtmlElement el = new HtmlElement("", By.id("el"));
		el.click();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	/* tests for each element defined in framework */
	@Test(groups={"ut"})
	public void testButtonElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		ButtonElement el = new ButtonElement("", By.id("el"), frame);
		el.submit();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testButtonElementOutsideFrame() throws Exception {
		ButtonElement el = new ButtonElement("", By.id("el"));
		el.submit();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testCheckBoxElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		CheckBoxElement el = new CheckBoxElement("", By.id("el"), frame);
		el.check();
		
		verify(locator, times(2)).frame(frameEl);
		verify(locator, times(2)).defaultContent();
	}
	@Test(groups={"ut"})
	public void testCheckBoxElementOutsideFrame() throws Exception {
		CheckBoxElement el = new CheckBoxElement("", By.id("el"));
		el.uncheck();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testImageElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		ImageElement el = new ImageElement("", By.id("el"), frame);
		el.getWidth();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testImageElementOutsideFrame() throws Exception {
		ImageElement el = new ImageElement("", By.id("el"));
		el.getWidth();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testLabelElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		LabelElement el = new LabelElement("", By.id("el"), frame);
		el.click();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testLabelElementOutsideFrame() throws Exception {
		LabelElement el = new LabelElement("", By.id("el"));
		el.click();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testLinkElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		LinkElement el = new LinkElement("", By.id("el"), frame);
		el.click();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testLinkElementOutsideFrame() throws Exception {
		LinkElement el = new LinkElement("", By.id("el"));
		el.click();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testRadioButtonElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		RadioButtonElement el = new RadioButtonElement("", By.id("el"), frame);
		el.check();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testRadioButtonElementOutsideFrame() throws Exception {
		RadioButtonElement el = new RadioButtonElement("", By.id("el"));
		el.check();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testSelectListInsideFrame() throws Exception {
		when(element.getTagName()).thenReturn("select");
		
		FrameElement frame = new FrameElement("", By.id("frameId"));
		SelectList el = new SelectList("", By.id("el"), frame);
		el.getSelectedText();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testSelectListOutsideFrame() throws Exception {
		when(element.getTagName()).thenReturn("select");
		
		SelectList el = new SelectList("", By.id("el"));
		el.getSelectedText();
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testTableInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		Table el = new Table("", By.id("el"), frame);
		el.getColumns();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testTableOutsideFrame() throws Exception {
		Table el = new Table("", By.id("el"));
		el.getColumns();
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	@Test(groups={"ut"})
	public void testTextFieldElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		TextFieldElement el = new TextFieldElement("", By.id("el"), frame);
		el.sendKeys("toto");
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testTextFieldElementOutsideFrame() throws Exception {
		TextFieldElement el = new TextFieldElement("", By.id("el"));
		el.sendKeys("toto");
		
		verify(locator, times(0)).frame(frameEl);
	}
	
	/**
	 * Test that we enter the iframe of the parent element when searching looking a sub-element
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testUseElementInsideElementInsideFrame() throws Exception {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		LinkElement link = el.findLinkElement(By.id("link"));
		link.click();
		
		verify(locator).frame(frameEl);
		verify(locator).defaultContent();
	}
	
}
