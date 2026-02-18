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

import java.io.File;
import java.nio.file.Paths;
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
	private String variableServerUrl;
    private String authToken;
    private String testName;
    private String variableName;
    private String fileName;
	
	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		initThreadContext(ctx);

		variableServerUrl = System.getProperty("variableServerUrl");
        authToken = System.getProperty("authToken");
        testName = System.getProperty("testName");
        variableName = System.getProperty("variableName");
        fileName = System.getProperty("fileName");

        if (variableServerUrl == null) {
            variableServerUrl = "https://localhost:8002";
        }

        connector = new SeleniumRobotVariableServerConnector(true, variableServerUrl, testName, authToken);
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
	
	@Test(groups={"it"}, enabled=true)
	public void testGetVariablesWithName() {
		Map<String, TestVariable> variables = connector.getVariables("custom.test.variable.key.test", null);
		Assert.assertEquals(variables.size(), 1);
	}
	
	@Test(groups={"it"}, enabled=true)
	public void testGetVariablesWithValue() {
		Map<String, TestVariable> variables = connector.getVariables(null, "value");
		Assert.assertEquals(variables.size(), 1);
	}
	
	@Test(groups={"it"}, enabled=true)
	public void testUpsert() {
		TestVariable variable = new TestVariable("key.test", "value");
		variable.setReservable(true);
//		variable.setTimeToLive(3);
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
	
	/**
     * Check manually with the locally started server that variable as file can be downloaded
     * You need, on your local server
     * - an Application with name=core
     * - a TestCase with name=testName (the class variable) and application=core
     * - a Variable with name=variableName (the class variable) and application=core and a file uploaded
     * - fileName must be this variable uploadedFile as 'example.csv'
     */
    @Test(groups = {"it"}, enabled = true)
    public void testDownloadVariableFile() {
        File checkFile = Paths.get(SeleniumTestsContextManager.getDatasetPath(), "DEV", fileName).toFile();
        if (checkFile.exists()) {
            Assert.assertTrue(checkFile.delete());
        }
        Map<String, TestVariable> variables = connector.getVariables();
        File varFile = connector.getVariableFile(variables.get(variableName));
        Assert.assertTrue(varFile.exists());
    }
	
}

