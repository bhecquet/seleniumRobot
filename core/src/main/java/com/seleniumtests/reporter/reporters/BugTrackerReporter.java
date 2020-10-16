package com.seleniumtests.reporter.reporters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.reporter.logger.TestStep;

public class BugTrackerReporter extends CommonReporter implements IReporter {

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
				
				// search all bugtracker parameters bugtracker.field.<key>=<value>
				Map<String, String> customFields = new HashMap<>();
				for (TestVariable variable: testContext.getConfiguration().values()) {
					if (variable.getName().startsWith("bugtracker.field.")) {
						customFields.put(variable.getName().replace("bugtracker.field.", ""), variable.getValue());
					}
				}
				TestVariable assignee = testContext.getConfiguration().get("bugtracker.assignee");
				TestVariable reporter = testContext.getConfiguration().get("bugtracker.reporter");
				TestVariable priority = testContext.getConfiguration().get("bugtracker.priority");
				TestVariable issueType = testContext.getConfiguration().get("bugtracker.issueType");
				TestVariable components = testContext.getConfiguration().get("bugtracker.components");
				
				// application data
				String application = testContext.getApplicationName();
				String environment = testContext.getTestEnv();
				String testNgName = testResult.getTestContext().getCurrentXmlTest().getName();
				String testName = TestNGResultUtils.getUniqueTestName(testResult);

				String description = String.format("Test %s failed\n", testName);

				if (testResult.getMethod().getDescription() != null) {
					description += "Test goal: " + testResult.getMethod().getDescription();
				}

				// search the last step to get screenshots and failure reason
				List<TestStep> testSteps = TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();
				if (testSteps == null) {
					return;
				}

				// create issue only for failed tests and if it has not been created before
				if (testResult.getStatus() == ITestResult.FAILURE && !TestNGResultUtils.isBugtrackerReportCreated(testResult)) {
	
					bugtrackerServer.createIssue(
							assignee == null ? null: assignee.getValue(), 
							priority == null ? null: priority.getValue(), 
							issueType == null ? null: issueType.getValue(), 
							reporter == null ? null: reporter.getValue(), 							
							customFields, 
							components == null ? new ArrayList<>(): Arrays.asList(components.getValue().split(",")),
							application,
							environment,
							testNgName,
							testName,
							description,
							testSteps
							);
					
					TestNGResultUtils.setBugtrackerReportCreated(testResult, true);
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
