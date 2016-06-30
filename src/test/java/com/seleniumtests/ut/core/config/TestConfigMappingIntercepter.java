package com.seleniumtests.ut.core.config;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.uipage.Locator;
import com.seleniumtests.uipage.aspects.InterceptBy;


public class TestConfigMappingIntercepter {
	@BeforeMethod(enabled=true, alwaysRun = true)
	public void initContext(final ITestContext testNGCtx, final XmlTest xmlTest) {
		InterceptBy.setPage("TestConfigMobileIntercepter");
		SeleniumTestsContextManager.initThreadContext(testNGCtx, xmlTest);
	}
	
	@Test(groups={"ut"})
	public void interceptBy() {
		Assert.assertEquals(By.id("map:id").toString(), "By.id: login", "intercept by with map doesn't work");
	}

	@Test(groups={"ut"})
	public void interceptByLocator() {
		Assert.assertEquals(Locator.locateById("map:id").toString(), "By.id: login", "intercept by Locator doesn't work");
	}
	
	@Test(groups={"ut"})
	public void noChangeBy() {
		Assert.assertEquals(By.id("id").toString(), "By.id: id", "no change when no key word doesn't work");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void exceptionWhenNoPresence() {
		By.id("map:name");
	}

}
