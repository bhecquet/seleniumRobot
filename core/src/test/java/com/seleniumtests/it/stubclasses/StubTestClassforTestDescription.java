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
package com.seleniumtests.it.stubclasses;

import java.io.IOException;
import java.util.ArrayList;

import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;

public class StubTestClassforTestDescription extends StubParentClass {

	
	@Test(groups="stub")
	public void testNoDescription() throws IOException {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
	}
	
	@Test(groups="stub", description="a test with param ${url}")
	public void testWithDescription() throws IOException {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		TestStepManager.logTestStep(step1);

	}
	
	@Test(groups="stub", description = "This test is always <OK> & \"green\"")
	public void testWithDescriptionAndSpecialCharacters() throws IOException {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		
	}
	
	@Test(groups="stub", description="a test with param ${url}\nand line breaks")
	public void testWithLineBreaksInDescription() throws IOException {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.setFailed(true);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		throw new IndexOutOfBoundsException();
		
	}
	
	@DataProvider(name = "data")
	public Object[][] data() {
		return new String[][] {new String[] {"data2", "data1"}, new String[] {"data3", "data4"}};
	}

	
	@Test(groups="stub", dataProvider = "data", description="a test with param ${arg0} and ${arg1} from dataprovider")
	public void testDataProvider(String col1, String col2) {
		logger.info(String.format("%s,%s", col1, col2));
	}
	

	@Test(groups="stub", description="a test on ${client} account ${account}")
	public void testWithParamCreatedInTest() throws IOException {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		step1.addAction(new TestAction("sendKeys to text field", true, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		createOrUpdateLocalParam("account", "account-12345");
		createOrUpdateParam("client", "Bob");

	}
}
