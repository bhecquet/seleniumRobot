package com.seleniumtests.it.connector.selenium;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;

/**
 * This class is for testing with a local variable server
 * Check is manual. That's why tests are not enabled by default
 * @author s047432
 *
 */
public class TestSeleniumRobotVariableServerConnector extends GenericTest {

	SeleniumRobotVariableServerConnector connector;
	
	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		initThreadContext(ctx);

		connector = new SeleniumRobotVariableServerConnector(true, "http://localhost:8002", "Test 1");
		if (!connector.getActive()) {
			throw new SkipException("no seleniumrobot server available");
		}
	}
	
	@Test(groups={"it"}, enabled=false)
	public void testCreateApplication() {
		Map<String, TestVariable> variables = connector.getVariables();
		Assert.assertTrue(variables.size() > 0);
	}
	
	@Test(groups={"it"}, enabled=false)
	public void testReserveVariable() {
		Map<String, TestVariable> variables = connector.getVariables();
		connector.getVariables();
		Assert.assertTrue(variables.size() > 0);
	}
	
	@Test(groups={"it"}, enabled=false)
	public void testUpsert() {
		TestVariable variable = new TestVariable("key.test", "value");
//		TestVariable variable = new TestVariable(16, "key.test", "value", false, "custom.test.variable.key.test");
		variable = connector.upsertVariable(variable);
//		variable.setValue("newValue");
//		variable = connector.upsertVariable(variable);
	}
	
	/**
	 * Check manually with the locally started server that variable is correctly released at the end of test
	 */
	@Test(groups={"it"}, enabled=false)
	public void testDeReserveVariable() {
		Map<String, TestVariable> variables = connector.getVariables();
		List<TestVariable> vars = new ArrayList<>(variables.values());
		Assert.assertTrue(vars.size() > 0);
		connector.unreserveVariables(vars);
	}
	
}
