/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.it.driver.screenshots;

import java.util.List;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;

public class TestScreenshotUtil extends ReporterTest {

	/**
	 * check that duration of screenshots is logged into TestStep
	 * @param testContext
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testScreenshotDurationIsLogged(ITestContext testContext) throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});

		for (ITestResult testResult: TestLogging.getTestsSteps().keySet()) {
			if (testResult.getMethod().isTest()) {
				List<TestStep> steps = TestLogging.getTestsSteps().get(testResult);
				
				for (TestStep step: steps) {
					List<Snapshot> snapshots = step.getSnapshots();
					
					if (!snapshots.isEmpty()) {
						Assert.assertTrue(step.getDurationToExclude() > 0);
						Assert.assertEquals(snapshots.get(0).getScreenshot().getDuration(), step.getDurationToExclude());
					}
				}
			}
		}
	}
}
