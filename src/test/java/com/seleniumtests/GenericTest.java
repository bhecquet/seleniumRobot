package com.seleniumtests;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;

import com.seleniumtests.driver.WebUIDriver;

public class GenericTest {
	
	public WebDriver driver = null;

	
	/**
	 * destroys the driver if one has been created
	 */
	@AfterMethod(alwaysRun = true)
	public void destroyDriver() {
		if (driver != null) {
			driver.close();
			WebUIDriver.cleanUp();
		}
	}
}
