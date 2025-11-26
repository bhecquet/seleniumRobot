package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.uipage.htmlelements.select.NativeSelect;

/**
 * Class for testing SelectList object without its implementations
 * It will be tested with a mock of HTML select
 */
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


    private MockedStatic<WebUIDriver> mockedWebUIDriver;

	@BeforeMethod(groups={"ut"})
	private void init() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(2);

		// add capabilities to allow augmenting driver
		when(driver.getCapabilities()).thenReturn(new DesiredCapabilities());

        CustomEventFiringWebDriver eventDriver = spy(new CustomEventFiringWebDriver(driver));

		when(eventDriver.switchTo()).thenReturn(target);
		
		// standard select
		when(driver.findElement(By.id("select"))).thenReturn(element);
		when(element.getTagName()).thenReturn("select");
		when(element.isEnabled()).thenReturn(true);
		when(element.isDisplayed()).thenReturn(true);
		when(element.findElements(By.tagName("option"))).thenReturn(Arrays.asList(option1, option2));
		
		// the way Select class search for text and value
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"opt1\"]"))).thenReturn(List.of(option1));
		when(element.findElements(By.xpath(".//option[normalize-space(.) = \"opt2\"]"))).thenReturn(List.of(option2));
		when(element.findElements(By.xpath(".//option[@value = \"opti1\"]"))).thenReturn(List.of(option1));
		when(element.findElements(By.xpath(".//option[@value = \"opti2\"]"))).thenReturn(List.of(option2));

		when(option1.getText()).thenReturn("opt1");
		when(option1.isEnabled()).thenReturn(true);
		when(option2.getText()).thenReturn("opt2");
		when(option2.isEnabled()).thenReturn(true);
		when(option1.getDomAttribute("value")).thenReturn("opti1");
		when(option2.getDomAttribute("value")).thenReturn("opti2");
		when(option1.getAttribute("index")).thenReturn("1");
		when(option2.getAttribute("index")).thenReturn("2");

		mockedWebUIDriver = mockStatic(WebUIDriver.class);
		mockedWebUIDriver.when(WebUIDriver::getCurrentWebUiDriverName).thenReturn("main");
		mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean(), eq(BrowserType.FIREFOX), eq("main"), isNull())).thenReturn(eventDriver);
		mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(eventDriver);
		when(eventDriver.executeScript("if (document.readyState === \"complete\") { return \"ok\"; }")).thenReturn("ok");
		when(eventDriver.getBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.FIREFOX, "78.0"));
	}

	@AfterMethod(groups= {"ut"}, alwaysRun = true)
	private void closeMocks() {
		mockedWebUIDriver.close();
	}

	@Test(groups={"ut"})
	public void testGetOptions() {

		SelectList select = new SelectList("", By.id("select"));
		List<WebElement> options = select.getOptions();
		Assert.assertEquals(options.size(), 2);
		Assert.assertTrue(select.getSelectImplementation() instanceof NativeSelect);
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFindElementWrongType() {
		when(element.getTagName()).thenReturn("something");
		SelectList select = new SelectList("", By.id("select"));
		select.getOptions();
	}
	
	@Test(groups={"ut"}, expectedExceptions = WebDriverException.class)
	public void testGetOptionsNotPresent() {
		when(driver.findElement(By.id("select"))).thenThrow(new NoSuchElementException("not found"));
		new SelectList("", By.id("select")).getOptions();
	}

	@Test(groups={"ut"})
	public void testGetFirstSelectedOption() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		WebElement option = select.getFirstSelectedOption();
		Assert.assertEquals(option.getText(), "opt1");
	}
	
	@Test(groups={"ut"})
	public void testGetFirstSelectedOptionNoneSelected() {
		SelectList select = new SelectList("", By.id("select"));
		WebElement option = select.getFirstSelectedOption();
		Assert.assertNull(option);
	}
	
	@Test(groups={"ut"})
	public void testGetAllSelectedOption() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		List<WebElement> options = select.getAllSelectedOptions();
		Assert.assertEquals(options.size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testSelectedText() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedText();
		Assert.assertEquals(txt, "opt1");
	}
	
	@Test(groups={"ut"})
	public void testSelectedTextNoSelection() {
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedText();
		Assert.assertEquals(txt, "");
	}
	
	@Test(groups={"ut"})
	public void testSelectedTexts() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String[] txt = select.getSelectedTexts();
		Assert.assertEquals(txt[0], "opt1");
		Assert.assertEquals(txt[1], "opt2");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValue() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedValue();
		Assert.assertEquals(txt, "opti1");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValueNoSelection() {
		
		SelectList select = new SelectList("", By.id("select"));
		String txt = select.getSelectedValue();
		Assert.assertEquals(txt, "");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValues() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		String[] txt = select.getSelectedValues();
		Assert.assertEquals(txt[0], "opti1");
		Assert.assertEquals(txt[1], "opti2");
	}
	
	@Test(groups={"ut"})
	public void testSelectedValuesNoSelection() {

		SelectList select = new SelectList("", By.id("select"));
		String[] txt = select.getSelectedValues();
		Assert.assertEquals(txt.length, 0);
	}
	
	@Test(groups={"ut"})
	public void testIsMultipleNoAttribute() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		
		SelectList select = new SelectList("", By.id("select"));
		Assert.assertFalse(select.isMultiple());
	}
	
	@Test(groups={"ut"})
	public void testIsMultipleNotMultiple() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("false");
		
		SelectList select = new SelectList("", By.id("select"));
		Assert.assertFalse(select.isMultiple());
	}
	
	@Test(groups={"ut"})
	public void testIsMultipleMultiple() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		Assert.assertTrue(select.isMultiple());
	}
	

	@Test(groups={"ut"})
	public void testDeselectAll() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectAll();
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"}, expectedExceptions = UnsupportedOperationException.class)
	public void testDeselectAllNotMultiple() {
		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("false");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectAll();
	}

	@Test(groups={"ut"})
	public void testDeselectByIndex() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByIndex(2);
		verify(option1, never()).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"})
	public void testDeselectByText() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByText("opt2");
		verify(option1, never()).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"})
	public void testDeselectByTextNotSelected() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByText("opt1");
		verify(option1, never()).click();
		verify(option2, never()).click();
	}

	@Test(groups={"ut"})
	public void testDeselectByValue() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByValue("opti2");
		verify(option1, never()).click();
		verify(option2).click();
	}

	@Test(groups={"ut"})
	public void testSelectByIndex() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByIndex(1);
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByIndexes() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByIndex(1, 2);
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByText() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByText("opt1");
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByTexts() {
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
	 */
	@Test(groups={"ut"})
	public void testSelectByCorrespondingText() {

		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(false);
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByCorrespondingText("Opt");
		verify(option1).click();
		verify(option2, never()).click();
	}

	@Test(groups={"ut"})
	public void testSelectByCorrespondingTexts() {

		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByCorrespondingText("PT1", "PT2");
		verify(option1).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"})
	public void testDeselectByCorrespondingText() {

		when(option1.isSelected()).thenReturn(true);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByCorrespondingText("PT2");
		verify(option1, never()).click();
		verify(option2).click();
	}
	
	@Test(groups={"ut"}, expectedExceptions = UnsupportedOperationException.class)
	public void testDeselectByCorrespondingTextNotMultiple() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		when(element.getDomAttribute("multiple")).thenReturn("false");
		
		SelectList select = new SelectList("", By.id("select"));
		select.deselectByCorrespondingText("Pt2");
	}

	@Test(groups={"ut"})
	public void testSelectByValue() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(true);
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByValue("opti1");
		verify(option1).click();
		verify(option2, never()).click();
	}
	
	@Test(groups={"ut"})
	public void testSelectByValues() {
		when(option1.isSelected()).thenReturn(false);
		when(option2.isSelected()).thenReturn(false);
		when(element.getDomAttribute("multiple")).thenReturn("true");
		
		SelectList select = new SelectList("", By.id("select"));
		select.selectByValue("opti1", "opti2");
		verify(option1).click();
		verify(option2).click();
	}
}
	