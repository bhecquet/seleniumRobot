/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		variable.setReservable(true);
		variable.setTimeToLive(3);
//		TestVariable variable = new TestVariable(16, "key.test", "value", false, "custom.test.variable.key.test");
		variable = connector.upsertVariable(variable, false);
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
