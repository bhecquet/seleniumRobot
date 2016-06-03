package com.seleniumtests.ut.core.config;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.config.ConfigReader;

public class TestConfigReader extends GenericTest {

	@Test(groups={"ut"})
	public void readConfigurationWithValueOverride() {
		HashMap<String, String> config = new ConfigReader().readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/config.ini"), "Dev");
		Assert.assertEquals(config.get("key1"), "value4", "Key override does not work");
	}
	
	@Test(groups={"ut"})
	public void readConfigurationWithoutValueOverride() {
		HashMap<String, String> config = new ConfigReader().readConfig(Thread.currentThread().getContextClassLoader().getResourceAsStream("tu/config.ini"), "VNR");
		Assert.assertEquals(config.get("key1"), "value1", "Key should not be overriden");
	}
	
	@Test(groups={"ut"})
	public void mergeIniAndXmlConfiguration(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.initTestLevelContext(testNGCtx, xmlTest);
		SeleniumTestsContext seleniumTestsCtx = SeleniumTestsContextManager.getTestLevelContext(xmlTest.getName());
		
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key6"), "value6", "Value has not been get from xml file");
		Assert.assertEquals(seleniumTestsCtx.getConfiguration().get("key2"), "value20", "Value has not been get from xml file");
	}	
}
