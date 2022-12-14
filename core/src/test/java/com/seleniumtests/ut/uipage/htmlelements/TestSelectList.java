package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mock;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.uipage.htmlelements.select.NativeSelect;
import com.seleniumtests.uipage.htmlelements.select.StubSelect;

/**
 * Class for testing SelectList object without its implementations
 * It will be tested with a mock of HTML select
 * @throws IOException
 */
@PrepareForTest({WebUIDriver.class})
public class TestSelectList extends MockitoTest {


	@Mock
	private RemoteWebDriver driver;

	@Mock
	private TargetLocator target;

	@Mock
	private WebElement element; 
	
	@Mock
	private WebElement option1;
	
	@Mock
	private WebElement option2;

	@Mock
	private Options driverOptions;
	

	private CustomEventFiringWebDriver eventDriver;

	@BeforeMethod(groups={"ut"})
	private void init() throws IOException {

		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);
		
		eventDriver = spy(new CustomEventFiringWebDriver(driver));
		

		when(eventDriver.switchTo()).thenReturn(target);
		
		// standard select
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(element.getTagName()).thenReturn("select");
		when(element.isDisplayed()).thenReturn(true);
		when(element.findElements(By.tagName("option"))).thenReturn(Arrays.asList(option1, option2));
		
		// the way Select class search for text and value
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"opt1\"]"))).thenReturn(Arrays.asList(option1));
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"opt2\"]"))).thenReturn(Arrays.asList(option2));
		when(element.findElements(By.xpath(".//option[@value = \"opti1\"]"))).thenReturn(Arrays.asList(option1));
		when(element.findElements(By.xpath(".//option[@value = \"opti2\"]"))).thenReturn(Arrays.asList(option2));
		

		when(option1.getText()).thenReturn("opt1");
		when(option2.getText()).thenReturn("opt2");
		when(option1.getDomAttribute("value")).thenReturn("opti1");
		when(option2.getDomAttribute("value")).thenReturn("opti2");
		when(option1.getAttribute("index")).thenReturn("1");
		when(option2.getAttribute("index")).thenReturn("2");

		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getCurrentWebUiDriverName()).thenReturn("main");
		when(WebUIDriver.getWebDriver(anyBoolean(), eq(BrowserType.FIREFOX), eq("main"), isNull())).thenReturn(eventDriver);
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(eventDriver.executeScript("if (document.readyState === \"complete\") { return \"ok\"; }")).thenReturn("ok");
		when(eventDriver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.FIREFOX, "78.0"));

		
	}

	@Test(groups={"ut"})
	public void testGetOptions() throws IOException {

		SelectList select = new SelectList("", By.id("select"));
		List<WebElement> options = select.getOptions();
		Assert.assertEquals(options.size(), 2);
		Assert.assertTrue(select.getSelectImplementation() instanceof NativeSelect);
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFindElementWrongType() throws IOException {
		when(element.getTagName()).thenReturn("something");
		SelectList select = new SelectList("", By.id("select"));
		select.getOptions();
	}
	
	@Test(groups={"ut"}, expectedExceptions = WebDriverException.class)
	public void testGetOptionsNotPresent() throws IOException {
		when(driver.findElement(By.id("select"))).thenThrow(new NoSuchElementException("not found"));
		new SelectList("", By.id("select")).getOptions();
	}

	@Test(groups={"ut"})
	public void testGetFirstSelectedOption() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		WebElement option = select.getFirstSelectedOption();
		Assert.assertEquals(option.getText(), "opt1");
	}
	
	@Test(groups={"ut"})
	public void testGetFirstSelectedOptionNoneSelected() throws IOException {
		SelectList select = new SelectList("", By.id("select"));
		WebElement option = select.getFirstSelectedOption();
		Assert.assertNull(option);
	}
	
	@Test(groups={"ut"})
	public void testGetAllSelectedOption() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		List<WebElement> options = select.getAllSelectedOptions();
		Assert.assertEquals(options.size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testSelectedText() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedText();
		Assert.assertEquals(txt, "opt1");
	}
	
	@Test(groups={"ut"})
	public void testSelectedTextNoSelection() throws IOException {
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedText();
		Assert.assertEquals(txt, "");
	}
	
	@Test(groups={"ut"})
	public void testSelectedTexts() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String[] txt = select.getSelectedTexts();
		Assert.assertEquals(txt[0], "opt1");
		Assert.assertEquals(txt[1], "opt2");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValue() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedValue();
		Assert.assertEquals(txt, "opti1");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValueNoSelection() throws IOException {
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedValue();
		Assert.assertEquals(txt, "");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValues() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String[] txt = select.getSelectedValues();
		Assert.assertEquals(txt[0], "opti1");
		Assert.assertEquals(txt[1], "opti2");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValuesNoSelection() throws IOException {

		SelectList select = new SelectList("", By.id("select"));
		String[] txt = select.getSelectedValues();
		Assert.assertEquals(txt.length, 0);
	}
	
	@Test(groups={"ut"})
	public void testIsMultipleNoAttribute() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		
		SelectList select = new SelectList("", By.id("select"));
		Assert.assertFalse(select.isMultiple());
	}
	
	@Test(groups={"ut"})
	public void testIsMultipleNotMultiple() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("false");
		
		SelectList select = new SelectList("", By.id("select"));
		Assert.assertFalse(select.isMultiple());
	}
	
	@Test(groups={"ut"})
	public void testIsMultipleMultiple() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		Assert.assertTrue(select.isMultiple());
	}
	

	@Test(groups={"ut"})
	public void testDeselectAll() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectAll();
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"}, expectedExceptions = UnsupportedOperationException.class)
	public void testDeselectAllNotMultiple() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("false");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectAll();
	}

	@Test(groups={"ut"})
	public void testDeselectByIndex() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByIndex(2);
		verify(option1, never()).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"})
	public void testDeselectByText() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByText("opt2");
		verify(option1, never()).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"})
	public void testDeselectByTextNotSelected() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByText("opt1");
		verify(option1, never()).click();
		verify(option2, never()).click();
	}

	@Test(groups={"ut"})
	public void testDeselectByValue() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByValue("opti2");
		verify(option1, never()).click();
		verify(option2).click();
	}

	@Test(groups={"ut"})
	public void testSelectByIndex() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByIndex(1);
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByIndexes() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByIndex(1, 2);
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByText() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByText("opt1");
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByTexts() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByText("opt1", "opt2");
		verify(option1).click();
		verify(option2).click();
	}
	
	/**
	 * Check only one option is selected when one text is given
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testSelectByCorrespondingText() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(false);
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByCorrespondingText("Opt");
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	/**
	 * Check only one option is selected when one text is given
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testSelectByCorrespondingTexts() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByCorrespondingText("PT1", "PT2");
		verify(option1).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"})
	public void testDeselectByCorrespondingText() throws IOException {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByCorrespondingText("Pt2");
		verify(option1, never()).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"}, expectedExceptions = UnsupportedOperationException.class)
	public void testDeselectByCorrespondingTextNotMultiple() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("false");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByCorrespondingText("Pt2");
	}

	@Test(groups={"ut"})
	public void testSelectByValue() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByValue("opti1");
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByValues() throws IOException {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByValue("opti1", "opti2");
		verify(option1).click();
		verify(option2).click();
	}
}
	