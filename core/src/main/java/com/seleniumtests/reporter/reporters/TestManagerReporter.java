/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.reporter.reporters;

import java.util.Map;
import java.util.Set;

import com.seleniumtests.connectors.tms.ITestManager;
import com.seleniumtests.reporter.info.HyperlinkInfo;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;

/**
 * Class for sending test reports to test managers
 * @author behe
 *
 */
public class TestManagerReporter extends CommonReporter implements IReporter {

	@Override
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeResult, boolean finalGeneration) {

		// record only when all tests are executed so that intermediate results (a failed test which has been retried) are not present in list
		if (!finalGeneration) {
			return;
		}
		
		// Record test method reports for each result which has not already been recorded
		for (Map.Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {
			for (ITestResult testResult: entry.getValue()) {
				
				SeleniumTestsContext testContext = SeleniumTestsContextManager.setThreadContextFromTestResult(testResult.getTestContext(), testResult);
				ITestManager testManager = testContext.getTestManagerInstance();

				if (testManager == null) {
					return;
				} 

				testManager.login();
				
				// do not record twice the same result
				if (!TestNGResultUtils.isTestManagerReportCreated(testResult)) {

					testManager.recordResult(testResult);
					testManager.recordResultFiles(testResult);
					TestNGResultUtils.setTestManagereportCreated(testResult, true);

					// add information into report
					Integer testCaseId = testManager.getTestCaseId(testResult);
					if (testCaseId != null) {
						TestNGResultUtils.setTestInfo(testResult, "TestCase", new HyperlinkInfo(testCaseId.toString(), testManager.getTestCaseUrl(testResult)));
					}
				}

				testManager.logout();
			}
		}


	}


}
