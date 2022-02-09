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

import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Stub class for correction of issue #389: check that is Method parameter is missing, error is raised
 * @author s047432
 *
 */
public class StubTestClassForIssue382e2 extends StubParentClass {
	

	@Test(groups="stub", dataProvider = "data")
	public void testOk1(String data) throws Exception {
		
	}

	@BeforeMethod(groups={"stub"})
	public void reset() {
		
	}

	@DataProvider
	public Object[][] data(ITestContext testContext) {
		return new String[][] {new String[] {"data1"}, new String[] {"data2"}};
	}
}
