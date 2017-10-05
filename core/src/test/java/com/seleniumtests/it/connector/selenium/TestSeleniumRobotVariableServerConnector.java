package com.seleniumtests.it.connector.selenium;

import java.util.Map;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;

public class TestSeleniumRobotVariableServerConnector extends GenericTest {

	SeleniumRobotVariableServerConnector connector;
	
	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		initThreadContext(ctx);
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerUrl("http://localhost:8002");
		SeleniumTestsContextManager.getThreadContext().setSeleniumRobotServerActive(true);
		connector = new SeleniumRobotVariableServerConnector("Test 1");
		if (!connector.getActive()) {
			throw new SkipException("no seleniumrobot server available");
		}
	}
	
	@Test(groups={"it"}, enabled=false)
	public void testCreateApplication() {
		Map<String, String> variables = connector.getVariables();
		Assert.assertTrue(variables.size() > 0);
	}
	
	
	
}
