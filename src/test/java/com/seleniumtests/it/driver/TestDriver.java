package com.seleniumtests.it.driver;

import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.helper.WaitHelper;
import com.seleniumtests.webelements.HtmlElement;

public class TestDriver {
	
	public TestDriver() throws Exception {
		super();
	}

	private WebDriver driver;
	private static DriverTestPage testPage;
	
	@BeforeClass(dependsOnGroups={"it"})
	public void initDriver() throws Exception {
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		
		try {
			driver.manage().window().maximize();
		} catch (Exception e) {}
	}
	
	@AfterMethod
	public void cleanAlert() {
		try {
			driver.switchTo().alert().accept();
		} catch (WebDriverException e) {
			
		}
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		driver.close();
	}
	
	/**
	 * Is browser able to clic on moving elements
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testMovingElement() throws Exception {
		testPage.startButton.click();
		testPage.greenSquare.click();
		driver.switchTo().alert().accept();
		
	}
	
	@Test(groups={"it"}, expectedExceptions=UnhandledAlertException.class)
	public void testFindWithAlert() {
		testPage.startButton.click();
		testPage.greenSquare.click();
		testPage.redSquare.click();
	}
   
	
	/**
	 * Test native click
	 */
   
	@Test(groups={"it"})
	public void testClickDiv() {
		try {
			testPage.redSquare.click();
			Assert.assertEquals("coucou", testPage.textElement.getValue());
		} finally {
			testPage.resetButton.click();
			Assert.assertEquals("", testPage.textElement.getValue());
		}
	}
   
	@Test(groups={"it"})
	public void testClickRadio() {
		try {
			testPage.radioElement.click();
			Assert.assertTrue(testPage.radioElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testClickCheckBox() {
		try {
			testPage.checkElement.click();
			Assert.assertTrue(testPage.checkElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
  
	
	/**
	 * Test javascript actions
	 */
	@Test(groups={"it"})
	public void testClickJsDiv() {
		try {
			testPage.redSquare.simulateClick();
			Assert.assertEquals("coucou", testPage.textElement.getValue());
		} finally {
			testPage.resetButton.click();
			Assert.assertEquals("", testPage.textElement.getValue());
		}
	}
   
	@Test(groups={"it"})
	public void testClickJsRadio() {
		try {
			testPage.radioElement.simulateClick();
			Assert.assertTrue(testPage.radioElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testClickJsCheckbox() {
		try {
			testPage.checkElement.simulateClick();
			Assert.assertTrue(testPage.checkElement.isSelected());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testSendKeys() {
		try {
			testPage.textElement.sendKeys("youpi");
			Assert.assertEquals("youpi", testPage.textElement.getValue());
		} finally {
			testPage.resetButton.click();
		}
	}
   
	@Test(groups={"it"})
	public void testSendKeysJs() {
		try {
			testPage.textElement.simulateSendKeys("youpi");
			Assert.assertEquals("youpi", testPage.textElement.getValue());
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}

	/**
	 * Click on link that opens a new window
	 * Then search an element into this window, close it and search an element into main window
	 */  
	@Test(groups={"it"})
	public void clickLink() {

		String mainHandle = null;
		try {
			testPage.link.click();
		
			// passage sur le nouvel onglet et recherche d'un élément
			mainHandle = testPage.selectNewWindow();
			Assert.assertEquals("input", driver.findElement(By.name("q")).getTagName());
		} finally {
			// retour sur l'onglet principal
			driver.close();
			if (mainHandle != null) {
				testPage.selectWindow(mainHandle);
			}
		}
		Assert.assertEquals(testPage.link.getUrl(), "http://www.google.fr/");
	}
	
	/**
	 * Changing data in an input field should throw onBlur event
	 */  
	@Test(groups={"it"})
	public void testOnBlur() {
		testPage.onBlurField.sendKeys("onBlur done");
		Assert.assertEquals("onBlur done", testPage.onBlurFieldDest.getValue());
	}
	
	@Test(groups={"it"})
	public void testFindElements() {
		// 2 éléments à trouver
		Assert.assertEquals(2, new HtmlElement("", By.name("divFindName")).getAllElements().size());
		
		// 3 éléments dont l'un dans une branche
		Assert.assertEquals(3, new HtmlElement("", By.className("myClass")).getAllElements().size());
	}

//	@Test(groups={"it"})
//	public void testFindElement() {
//		TODO: vérifier si elle sert encore avec le rejeu
//		Assert.assertEquals("first child", driver.findElement(By.id("parent")).findElement(By.id("child1")).getText());
//	}

	/**
	 * test specific HtmlElements actions
	 */
	@Test(groups={"it"})
	public void testFindPattern1() {
		Assert.assertEquals("http://www.google.fr/", testPage.link.findLink("href"));
	}
	
	@Test(groups={"it"})
	public void testFindPattern2() {
		Assert.assertEquals("http://www.google.fr", testPage.linkPopup.findLink("onclick"));
	}
	
	@Test(groups={"it"}) 
	public void testFindPattern3() {
		Assert.assertEquals("http://www.google.fr", testPage.linkPopup2.findLink("onclick"));
	}
	
	/**
	 * text search
	 */
	@Test(groups={"it"})
	public void testFindPattern4() {
		Assert.assertEquals("other", new HtmlElement("", By.id("divFind2")).findPattern(Pattern.compile("an (\\w+) text"), "text"));
	}
	
	/**
	 * Check we wait enough for element to be displayed
	 */
	@Test(groups={"it"}) 
	public void testDelay() {
		try {
			testPage.delayButton.click();
			Assert.assertEquals("my value", new HtmlElement("", By.id("newEl")).getValue());
		} finally {
			testPage.delayButtonReset.click();
		}
		
	}
	
	/**
	 * Test that it's possible to use an hidden element. Make it appear before using it
	 */
	@Test(groups={"it"})
	public void testHiddenElement() { 
		testPage.hiddenCheckBox.click();
		Assert.assertTrue(testPage.hiddenCheckBox.isSelected());
		Assert.assertTrue(testPage.hiddenCheckBox.isDisplayed());
	}
	
	@Test(groups={"it"})
	public void testIsElementPresent1() {
		try {
			testPage.delayButton.click();
			Assert.assertFalse(new HtmlElement("", By.id("newEl")).isElementPresent());
			WaitHelper.waitForSeconds(3);
			Assert.assertTrue(new HtmlElement("", By.id("newEl")).isElementPresent());
		} finally {
			testPage.delayButtonReset.click();
		}
	}

//	@Test(groups={"it"})
//	public void testFindImageElement() {
//		
//		try {
//			driver.executeScript("window.scrollTo(0, 0);");
//			driver.findImageElement(new File(Thread.currentThread().getContextClassLoader().getResource("googleSearch.png").getFile())).click();
//			Assert.assertEquals("image", driver.findElement(By.id("text2")).getAttribute("value"));
//		} finally {
//			driver.findElement(By.id("button2")).click();
//		}
//	}
	
	/**
	 * Vérifie qu'avant d'agir sur un élément, on positionne la fenêtre du navigateur pour qu'il soit visible
	 */
	@Test(groups={"it"})
	public void testAutoScrolling() {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
		new HtmlElement("", By.id("buttonScroll")).click();
		Assert.assertFalse(((JavascriptExecutor) driver).executeScript("return $(window).scrollTop();").equals(0L));
	}
}
