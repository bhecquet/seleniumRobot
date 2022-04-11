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

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriverException;

import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.cucumber.java.en.When;

public class StubCucumberClass {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(StubCucumberClass.class);
	
	@When("^write (\\w+)$")
	public void writeText(String text) {
		logger.info("write " + text);
		WaitHelper.waitForSeconds(1);
	}

	@When("^write2 (\\w+)$")
	public void writeText2(String text) {
		logger.info("write " + text);
		WaitHelper.waitForSeconds(1);
	}
	
	@When("^write_error (\\w+)$")
	public void writeTextWithError(String text) {
		throw new WebDriverException("no element found");
	}
	
	@When("^write_error2 (\\w+)$")
	public void writeTextWithError2(String text) {
		throw new WebDriverException("no element found");
	}

}
