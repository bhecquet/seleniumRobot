package com.seleniumtests.it.connector.extools;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.extools.Lighthouse;
import com.seleniumtests.connectors.extools.Lighthouse.Category;
import com.seleniumtests.connectors.extools.LighthouseFactory;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;

public class TestLigthouse extends GenericMultiBrowserTest {

	public TestLigthouse() throws Exception {
		super(BrowserType.CHROME, "DriverTestPage");
	}
	
	@Test(groups="it")
	public  void testLighthouseExecution() {
		Lighthouse lighthouseInstance = LighthouseFactory.getInstance();
		lighthouseInstance.execute(testPageUrl, new ArrayList<>());
		Assert.assertNotNull(lighthouseInstance.getHtmlReport());
		Assert.assertNotNull(lighthouseInstance.getJsonReport());
		Assert.assertTrue(lighthouseInstance.getScore(Category.ACCESSIBILITY) > 30);
	}
	
	@Test(groups="it")
	public  void testLighthouseExecutionInError() {
		Lighthouse lighthouseInstance = LighthouseFactory.getInstance();
		lighthouseInstance.execute("badUrl", new ArrayList<>());
		Assert.assertNull(lighthouseInstance.getHtmlReport());
		Assert.assertNull(lighthouseInstance.getJsonReport());
		Assert.assertTrue(lighthouseInstance.getScore(Category.ACCESSIBILITY) < 0.1);
	}

}
