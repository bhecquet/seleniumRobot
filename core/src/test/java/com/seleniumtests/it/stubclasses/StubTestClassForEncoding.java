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
import org.testng.annotations.Test;

import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;

public class StubTestClassForEncoding extends StubParentClass {
	
	@Test(groups="stub", description="a test with steps")
	public void testAndSubActions() throws IOException {
		TestStep step1 = new TestStep("step 1 <>\"'&/", "step 1 <>\"'&/", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.addAction(new TestAction("click button  <>\"'&", false, new ArrayList<>()));
		step1.addMessage(new TestMessage("a message <>\"'&", MessageType.LOG));
		TestStepManager.logTestStep(step1);
	}
	
	@Test(groups="stub")
	public void testWithException() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		throw new DriverExceptions("& some exception \"with \" <strong><a href='http://someurl/link' style='background-color: red;'>HTML to encode</a></strong>");
	}
	
	@Test(groups="stub")
	public void testWithChainedException() {
		TestStep step1 = new TestStep("step 1", "step 1", this.getClass(), Reporter.getCurrentTestResult(), new ArrayList<>(), true);
		step1.addAction(new TestAction("click button", false, new ArrayList<>()));
		TestStepManager.logTestStep(step1);
		throw new DriverExceptions("& some exception \"with \" <strong><a href='http://someurl/link' style='background-color: red;'>HTML to encode</a></strong>", new DriverExceptions("root <error>"));
	}

}
