package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;

public class TestUiActions {
	
	private WebDriver driver;
	
	@BeforeClass(dependsOnGroups={"it"})
	public void initDriver() throws Exception {
		driver = WebUIDriver.getWebDriver(true);
		
		try {
			driver.manage().window().maximize();
		} catch (Exception e) {}
		if (SeleniumTestsContextManager.getThreadContext().getWebRunBrowser().contains("firefox")) {
			driver.get("file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile());
		} else {
			driver.get("file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile());
		}
	}

	@Test(groups={"it"})
	public void testMoveClick() {
		try {
			new Actions(driver).moveToElement(driver.findElement(By.id("carre2"))).click().build().perform();
			Assert.assertEquals("coucou", driver.findElement(By.id("text2")).getAttribute("value"));
		} finally {
			new Actions(driver).moveToElement(driver.findElement(By.id("button2"))).click().build().perform();
			Assert.assertEquals("", driver.findElement(By.id("text2")).getAttribute("value"));
		}
	}
	
	@Test(groups={"it"}) 
	public void testSendKeys() {
		try {
			new Actions(driver).moveToElement(driver.findElement(By.id("text2"))).click().sendKeys("youpi").build().perform();
			Assert.assertEquals("youpi", driver.findElement(By.id("text2")).getAttribute("value"));
		} finally {
			driver.findElement(By.id("button2")).click();
		}
	}
	
	@AfterClass(alwaysRun = true)
	public void destroyDriver() {
		if (driver != null) {
			driver.close();
			WebUIDriver.cleanUp();
		}
	}
}
