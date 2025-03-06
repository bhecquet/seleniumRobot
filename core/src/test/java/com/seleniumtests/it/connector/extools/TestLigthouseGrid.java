package com.seleniumtests.it.connector.extools;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.extools.Lighthouse;
import com.seleniumtests.connectors.extools.Lighthouse.Category;
import com.seleniumtests.connectors.extools.LighthouseFactory;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;

public class TestLigthouseGrid extends GenericMultiBrowserTest {

	private static final String SELENIUM_GRID_URL = "http://127.0.0.1:4444/wd/hub";

	public TestLigthouseGrid() throws Exception {
		super(BrowserType.CHROME, "DriverTestPage", SELENIUM_GRID_URL, null);
	}
	

	@BeforeMethod(groups={"it"})
	public void initConnector(ITestContext ctx) {
		SeleniumGridConnector connector = new SeleniumRobotGridConnector(SELENIUM_GRID_URL);
		
		if (!connector.isGridActive()) {
			throw new SkipException("no seleniumrobot grid available");
		}
	}
	
	@Test(groups="it")
	public  void testLighthouseExecution() {
		Lighthouse lighthouseInstance = LighthouseFactory.getInstance();
		lighthouseInstance.execute(testPageUrl, new ArrayList<>());
		Assert.assertNotNull(lighthouseInstance.getHtmlReport());
		Assert.assertNotNull(lighthouseInstance.getJsonReport());
		Assert.assertNull(lighthouseInstance.getLogs());
		Assert.assertTrue(lighthouseInstance.getScore(Category.ACCESSIBILITY) > 30);
	}


}
