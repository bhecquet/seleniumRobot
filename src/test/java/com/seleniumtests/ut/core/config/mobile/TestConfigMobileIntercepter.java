package com.seleniumtests.ut.core.config.mobile;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.Locator;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.webelements.InterceptBy;


public class TestConfigMobileIntercepter {
	@BeforeMethod(enabled=true, alwaysRun = true)
	public void initContext(final ITestContext testNGCtx, final XmlTest xmlTest) {
		InterceptBy.setPage("TestConfigMobileIntercepter");
		SeleniumTestsContextManager.initThreadContext(testNGCtx, xmlTest);
	}
	
	@Test(groups={"ut"})
	public void interceptBy() {
		Assert.assertEquals(By.id("prop:id").toString(), "By.id: login", "intercept by with prop doesn't work");
	}

	@Test(groups={"ut"})
	public void interceptByLocator() {
		Assert.assertEquals(Locator.locateById("prop:id").toString(), "By.id: login", "intercept by Locator doesn't work");
	}
	
	@Test(groups={"ut"})
	public void noChangeBy() {
		Assert.assertEquals(By.id("id").toString(), "By.id: id", "no change when no key word doesn't work");
	}
	
	@Test(groups={"ut"})
	public void noModifWhenNoPresence() {
		Assert.assertEquals(By.id("prop:name").toString(), "By.id: prop:name", "no change when not present doesn't work");
	}

}
