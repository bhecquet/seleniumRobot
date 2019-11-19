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

import org.testng.SkipException;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.uipage.PageObject;

public class StubTestPage extends PageObject {

	public StubTestPage() throws IOException {
		super();
	}
	
	public StubTestPage(BrowserType browser) throws IOException {
		super(null , null, browser, "app", 0);
	}
	
	public StubTestPage(BrowserType browser, String driverName) throws IOException {
		super(null , null, browser, driverName, 12);
	}
	
	public StubTestPage doSomething() {
		TestLogging.log("tell me why");
		TestLogging.info("an info message");
		return this;
	}
	
	public StubTestPage doSomethingElse() {
		TestLogging.log("Hello");
		return this;
	}
	
	public StubTestPage skipStep() {
		throw new SkipException("skip this");
	}

}
