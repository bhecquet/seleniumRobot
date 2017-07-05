package com.seleniumtests.it.connector.selenium;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;

public class TestSeleniumRobotSnapshotServerConnector extends GenericTest {

	SeleniumRobotSnapshotServerConnector connector;
	
	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		initThreadContext(ctx);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put(SeleniumRobotServerConnector.SELENIUM_SERVER_URL, "http://localhost:8000");
		connector = new SeleniumRobotSnapshotServerConnector();
	}
	
	@Test(groups={"it"})
	public void testCreateApplication() {
		connector.createApplication();
		Assert.assertNotNull(connector.getApplicationId());
	}
	
	@Test(groups={"it"})
	public void testCreateEnvironment() {
		connector.createEnvironment();
		Assert.assertNotNull(connector.getEnvironmentId());
	}
	
	@Test(groups={"it"})
	public void testCreateVersion() {
		connector.createVersion();
		Assert.assertNotNull(connector.getApplicationId());
		Assert.assertNotNull(connector.getVersionId());
	}
	
	@Test(groups={"it"})
	public void testCreateSession() {
		connector.createSession();
		Assert.assertNotNull(connector.getEnvironmentId());
		Assert.assertNotNull(connector.getSessionId());
	}
}
