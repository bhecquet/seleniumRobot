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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.xml.XmlTest;

import com.seleniumtests.reporter.logger.TestLogging;

public class StubTestClassForListenerParent extends StubParentClass {

	@BeforeTest
	public void beforeTestInParent(XmlTest xmlTest) {
		TestLogging.info("beforeTest parent call");
	}
	
	@AfterClass
	public void afterClassInParent(XmlTest xmlTest) {
		TestLogging.info("afterClass parent call");
	}
}
