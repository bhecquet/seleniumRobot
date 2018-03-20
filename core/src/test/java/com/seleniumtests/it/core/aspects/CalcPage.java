/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

import cucumber.api.java.en.When;

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
	
	public CalcPage add(int a) {
		add(result, a);
		return this;
	}
	
	public CalcPage connect(String login, String password) {
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

}
