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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
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
public class TestFrameElement extends MockitoTest {
	
	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private RemoteWebElement element;
	@Mock
	private RemoteWebElement link;
	@Mock
	private RemoteWebElement row;
	@Mock
	private RemoteWebElement cell;
	@Mock
	private RemoteWebElement frameEl;
	@Mock
	private RemoteWebElement frameEl2;
	@Mock
	private RemoteWebElement subFrameEl;

    @Mock
	private TargetLocator locator;

	private MockedStatic<WebUIDriver> mockedWebUIDriver;
	
	@BeforeMethod(groups={"ut"})
	private void init() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		when(driver.getCapabilities()).thenReturn(new FirefoxOptions()); // add capabilities to allow augmenting driver

        CustomEventFiringWebDriver eventDriver = spy(new CustomEventFiringWebDriver(driver));

		mockedWebUIDriver = mockStatic(WebUIDriver.class);
		mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(driver.findElement(By.id("el"))).thenReturn(element);
		when(element.findElement(By.id("link"))).thenReturn(link);
		when(element.findElements(By.tagName("tr"))).thenReturn(List.of(row));
		when(row.findElements(any(By.class))).thenReturn(List.of(cell));
		when(driver.findElements(By.id("frameId"))).thenReturn(List.of(frameEl));
		when(driver.findElements(By.id("frameId2"))).thenReturn(List.of(subFrameEl));
		when(driver.findElements(By.tagName("iframe"))).thenReturn(List.of(frameEl, frameEl2));
		when(driver.switchTo()).thenReturn(locator);
		doNothing().when(eventDriver).scrollToElement(any(WebElement.class),  anyInt());
		
		when(frameEl.getText()).thenReturn("111");
		when(frameEl2.getText()).thenReturn("222");
		when(frameEl.isDisplayed()).thenReturn(false);
		when(frameEl2.isDisplayed()).thenReturn(true);

		when(element.getSize()).thenReturn(new Dimension(1, 1));
		when(element.isDisplayed()).thenReturn(true);
		when(link.isDisplayed()).thenReturn(true);
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	private void closeMocks() {
		mockedWebUIDriver.close();
	}

	@Test(groups={"ut"})
	public void testUseElementInsideFrame() {

		ArgumentCaptor<WebElement> frameArgument = ArgumentCaptor.forClass(WebElement.class);
		FrameElement frame = new FrameElement("", By.id("frameId"));
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		el.click();

		verify(locator).frame(frameArgument.capture());
		Assert.assertEquals(frameArgument.getValue().getText(), "111"); // check the frame used
		verify(locator).defaultContent();
	}

	/**
	 * issue #276: Check that we can switch to a frame by index
	 */
	@Test(groups={"ut"})
	public void testUseElementInsideFrameWithIndex() {
		ArgumentCaptor<WebElement> frameArgument = ArgumentCaptor.forClass(WebElement.class);
		
		FrameElement frame = new FrameElement("", By.tagName("iframe"), 1);
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		el.click();
		
		verify(locator).frame(frameArgument.capture());
		Assert.assertEquals(frameArgument.getValue().getText(), "222"); // check the frame used
		verify(locator).defaultContent();
	}

	@Test(groups={"ut"})
	public void testUseElementInsideFrameWithNegativeIndex() {
		ArgumentCaptor<WebElement> frameArgument = ArgumentCaptor.forClass(WebElement.class);

		FrameElement frame = new FrameElement("", By.tagName("iframe"), -1);
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		el.click();

		verify(locator).frame(frameArgument.capture());
		Assert.assertEquals(frameArgument.getValue().getText(), "222"); // check the frame used
		verify(locator).defaultContent();
	}

	@Test(groups={"ut"})
	public void testUseElementInsideFrameWithFirstVisibleIndex() {
		ArgumentCaptor<WebElement> frameArgument = ArgumentCaptor.forClass(WebElement.class);

		FrameElement frame = new FrameElement("", By.tagName("iframe"), HtmlElement.FIRST_VISIBLE);
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		el.click();

		verify(locator).frame(frameArgument.capture());
		Assert.assertEquals(frameArgument.getValue().getText(), "222"); // check the frame used
		verify(locator).defaultContent();
	}
	/**
	 * issue #276: Check a clear error is raised when an invalid index is given for finding a frame
	 */
	@Test(groups={"ut"}, expectedExceptions=NoSuchFrameException.class)
	public void testUseElementInsideFrameWithWrongIndex() {
		FrameElement frame = new FrameElement("", By.tagName("iframe"), 2);
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		el.click();
	}
	
	@Test(groups={"ut"})
	public void testUseElementInsideFrameRetryOnError() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		
		Mockito.doThrow(new WebDriverException("fake exception")).doNothing().when(element).click();
		el.click();
		
		// 2 invocations because first call to click raises an error
		verify(locator, times(2)).frame(any(WebElement.class));
		verify(locator, times(2)).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testUseElementInside2Frames() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		FrameElement frame2 = new FrameElement("", By.id("frameId2"), frame);
		HtmlElement el = new HtmlElement("", By.id("el"), frame2);
		el.click();
		
		// switch to each frame
		verify(locator, times(2)).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	
	@Test(groups={"ut"})
	public void testUseElementOutsideFrame() {
		HtmlElement el = new HtmlElement("", By.id("el"));
		el.click();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	/* tests for each element defined in framework */
	/**
	 * check we switched to default content and frame
	 */
	@Test(groups={"ut"})
	public void testButtonElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		ButtonElement el = new ButtonElement("", By.id("el"), frame);
		el.submit();
		
		verify(locator).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testButtonElementOutsideFrame() {
		ButtonElement el = new ButtonElement("", By.id("el"));
		el.submit();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testCheckBoxElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		CheckBoxElement el = new CheckBoxElement("", By.id("el"), frame);
		el.check();
		
		verify(locator, times(2)).frame(any(WebElement.class));
		verify(locator, times(2)).defaultContent();
	}
	@Test(groups={"ut"})
	public void testCheckBoxElementOutsideFrame() {
		CheckBoxElement el = new CheckBoxElement("", By.id("el"));
		el.uncheck();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testImageElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		ImageElement el = new ImageElement("", By.id("el"), frame);
		el.getWidth();
		
		verify(locator).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testImageElementOutsideFrame() {
		ImageElement el = new ImageElement("", By.id("el"));
		el.getWidth();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testLabelElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		LabelElement el = new LabelElement("", By.id("el"), frame);
		el.click();
		
		verify(locator).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testLabelElementOutsideFrame() {
		LabelElement el = new LabelElement("", By.id("el"));
		el.click();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testLinkElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		LinkElement el = new LinkElement("", By.id("el"), frame);
		el.click();
		
		verify(locator).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testLinkElementOutsideFrame() {
		LinkElement el = new LinkElement("", By.id("el"));
		el.click();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testRadioButtonElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		RadioButtonElement el = new RadioButtonElement("", By.id("el"), frame);
		el.check();
		
		verify(locator).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testRadioButtonElementOutsideFrame() {
		RadioButtonElement el = new RadioButtonElement("", By.id("el"));
		el.check();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testSelectListInsideFrame() {
		when(element.getTagName()).thenReturn("select");
		
		FrameElement frame = new FrameElement("", By.id("frameId"));
		SelectList el = new SelectList("", By.id("el"), frame);
		el.getSelectedText();
		
		verify(locator).frame(any(WebElement.class));
		verify(locator, times(1)).defaultContent();
	}
	@Test(groups={"ut"})
	public void testSelectListOutsideFrame() {
		when(element.getTagName()).thenReturn("select");
		
		SelectList el = new SelectList("", By.id("el"));
		el.getSelectedText();
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testTableInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		Table el = new Table("", By.id("el"), frame);
		el.getColumns();
		
		// issue #320: as we return HtmlElement instead of WebElement, we need to search for root element (the table) each time we search for columns and cells
		verify(locator, times(3)).frame(any(WebElement.class));
		verify(locator, times(3)).defaultContent();
	}
	@Test(groups={"ut"})
	public void testTableOutsideFrame() {
		Table el = new Table("", By.id("el"));
		el.getColumns();
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	@Test(groups={"ut"})
	public void testTextFieldElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		TextFieldElement el = new TextFieldElement("", By.id("el"), frame);
		el.sendKeys("toto");
		
		verify(locator).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	@Test(groups={"ut"})
	public void testTextFieldElementOutsideFrame() {
		TextFieldElement el = new TextFieldElement("", By.id("el"));
		el.sendKeys("toto");
		
		verify(locator, times(0)).frame(any(WebElement.class));
	}
	
	/**
	 * Test that we enter the iframe of the parent element when searching looking a sub-element
	 */
	@Test(groups={"ut"})
	public void testUseElementInsideElementInsideFrame() {
		FrameElement frame = new FrameElement("", By.id("frameId"));
		HtmlElement el = new HtmlElement("", By.id("el"), frame);
		LinkElement link2 = el.findLinkElement(By.id("link"));
		link2.click();
		
		verify(locator).frame(any(WebElement.class));
		verify(locator).defaultContent();
	}
	
}
