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
package com.seleniumtests.it.core.aspects;

import java.io.IOException;

import org.openqa.selenium.By;
import org.testng.Assert;

import com.seleniumtests.core.Step;
import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.core.StepName;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

import io.cucumber.java.en.When;

public class CalcPage extends PageObject {
	
	private static HtmlElement nonElement = new HtmlElement("none", By.id("none")); 
	
	private int result = 0;

	public CalcPage() throws IOException {
		super();
	}
	
	public CalcPage add(int a, int b) {
		result = a + b;
		doNothing();
		return this;
	}
	
	@When("add '(\\d+)' to '(\\d+)'")
	public CalcPage addC(Integer a, Integer b) {
		result = a + b;
		return this;
	}
	
	public CalcPage failAction() {
		throw new DriverExceptions("fail");
	}
	
	public CalcPage assertAction() {
		Assert.assertTrue(false, "false error");
		return this;
	}
	
	public CalcPage assertAction2() {
		Assert.assertTrue(false, "false error2");
		return this;
	}
	
	public CalcPage addWithSubStepCatchedError(int a) {
		try {
			addWithCatchedError(a);
		} catch (Exception e) {}
		return this;
	}
	
	public CalcPage addWithCatchedError(int a) {
		try {
			failAction();
		} catch (Exception e) {}
		return this;
	}
	
	@StepName("add something to total")
	public CalcPage addWithName(int a) {
		add(result, a);
		return this;
	}
	
	@StepName("add ${a} to total")
	public CalcPage addWithName2(int a) {
		add(result, a);
		return this;
	}
	
	@StepName("add ${a} and ${b} to total")
	public CalcPage addWithName3(Integer a, Integer ... b) {
		add(result, a);
		for (int c: b) {
			add(result, c);
		}
		return this;
	}
	
	@Step(name="add something to total")
	public CalcPage addWithNameBis(int a) {
		add(result, a);
		return this;
	}
	
	@Step(name="add ${a} to total")
	public CalcPage addWithName2Bis(int a) {
		add(result, a);
		return this;
	}
	
	@Step(name="add ${a} and ${b} to total")
	public CalcPage addWithName3Bis(Integer a, Integer ... b) {
		add(result, a);
		for (int c: b) {
			add(result, c);
		}
		return this;
	}
	
	@Step(name="add", errorCause = RootCause.REGRESSION, errorCauseDetails = "Check your scripts")
	public CalcPage addWithErrorCauseNoError(int a) {
		add(result, a);
		return this;
	}
	
	@Step(name="add", errorCause = RootCause.REGRESSION)
	public CalcPage addWithErrorCauseError(int a) {
		throw new DriverExceptions("fail");
	}
	
	@Step(name="add", errorCause = RootCause.REGRESSION, errorCauseDetails = "Check your scripts")
	public CalcPage addWithErrorCauseErrorAndDetails(int a) {
		throw new DriverExceptions("fail");
	}
	
	@Step(name="add")
	public CalcPage addNoCauseErrorNoDetails(int a) {
		throw new DriverExceptions("fail");
	}
	
	@Step(name="add", errorCauseDetails = "Check your scripts")
	public CalcPage addNoCauseErrorButDetails(int a) {
		throw new DriverExceptions("fail");
	}
	
	public CalcPage add(int a) {
		add(result, a);
		return this;
	}
	
	public CalcPage connect(String login, String password) {
		logger.info("login is " + login);
		logger.info("password is " + password);
		return this;
	}
	
	@StepName("Connect to calc with ${login}/${password}")
	public CalcPage connectWithName(String login, String password) {
		logger.info("login is " + login);
		logger.info("password is " + password);
		return this;
	}
	
	/**
	 * Use this method only when manualTestSteps are enabled, or an error will be raised
	 * @param a
	 * @return
	 */
	public CalcPage minus(int a) {
		addStep(String.format("minus %d", a));
		return this;
	}
	
	public CalcPage doNothing() {
		nonElement.doNothing();
		return this;
	}
	
	public ResultPage displayResult() throws IOException {
		return new ResultPage(result);
	}
	
	public CalcPage assertWithSubStep() {
		doNothing();
		return assertAction();
	}

	public int getResult() {
		return result;
	}

}
