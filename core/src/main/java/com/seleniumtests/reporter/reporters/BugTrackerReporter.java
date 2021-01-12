package com.seleniumtests.reporter.reporters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;

import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.reporter.logger.HyperlinkInfo;
import com.seleniumtests.reporter.logger.StringInfo;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.ScenarioLogger;

public class BugTrackerReporter extends CommonReporter implements IReporter {

	private static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(BugTrackerReporter.class);

	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport) {
		generateReport(resultSet, outdir, optimizeReport, false);
	}
	protected void generateReport(Map<ITestContext, Set<ITestResult>> resultSet, String outdir, boolean optimizeReport, boolean finalGeneration) {

		// record only when all tests are executed so that intermediate results (a failed test which has been retried) are not present in list
		if (!finalGeneration) {
			return;
		}

		for (Map.Entry<ITestContext, Set<ITestResult>> entry: resultSet.entrySet()) {

			ITestContext context = entry.getKey();

			for (ITestResult testResult : entry.getValue()) {
				// done in case it was null (issue #81)
				SeleniumTestsContext testContext = SeleniumTestsContextManager.setThreadContextFromTestResult(context, getTestName(testResult), getClassName(testResult), testResult);

				BugTracker bugtrackerServer = testContext.getBugTrackerInstance();
				if (bugtrackerServer == null) {
					return;
				} 
				
				// get all bugtracker options
				Map<String, String> issueOptions = new HashMap<>();
				for (TestVariable variable: testContext.getConfiguration().values()) {
					if (variable.getName().startsWith("bugtracker.")) {
						issueOptions.put(variable.getName().replace("bugtracker.", ""), variable.getValue());
					}
				}
				
				// application data
				String application = testContext.getApplicationName();
				String environment = testContext.getTestEnv();
				String testNgName = testResult.getTestContext().getCurrentXmlTest().getName();
				String testName = TestNGResultUtils.getUniqueTestName(testResult);

				String description = String.format("Test '%s' failed\n", testName);

				if (testResult.getMethod().getDescription() != null && !testResult.getMethod().getDescription().trim().isEmpty()) {
					description += "Test goal: " + testResult.getMethod().getDescription();
				}

				// search the last step to get screenshots and failure reason
				List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
				if (testSteps == null) {
					return;
				}

				// create issue only for failed tests and if it has not been created before
				if (testResult.getStatus() == ITestResult.FAILURE && !TestNGResultUtils.isBugtrackerReportCreated(testResult)) {
	
					IssueBean issueBean = bugtrackerServer.createIssue(
							application,
							environment,
							testNgName,
							testName,
							description,
							testSteps,
							issueOptions
							);
					
					// log information on issue
					if (issueBean != null) {
						if (issueBean.getId() != null && issueBean.getAccessUrl() != null) {
							TestNGResultUtils.setTestInfo(testResult, "Issue", new HyperlinkInfo(issueBean.getId(), issueBean.getAccessUrl()));
						} else if (issueBean.getId() != null) {
							TestNGResultUtils.setTestInfo(testResult, "Issue", new StringInfo(issueBean.getId()));
						}
						TestNGResultUtils.setTestInfo(testResult, "Issue date", new StringInfo(issueBean.getCreationDate()));
					}
					
					TestNGResultUtils.setBugtrackerReportCreated(testResult, true);
					
				// close issue if test is now OK and a previous issue has been created
				} else if (testResult.getStatus() == ITestResult.SUCCESS && !TestNGResultUtils.isBugtrackerReportCreated(testResult)) {
					
					bugtrackerServer.closeIssue( 
							application,
							environment,
							testNgName,
							testName);
					
					TestNGResultUtils.setBugtrackerReportCreated(testResult, true);
				}
			}
		}
	}

	

}
