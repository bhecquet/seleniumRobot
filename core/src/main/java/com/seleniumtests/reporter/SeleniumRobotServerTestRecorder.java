package com.seleniumtests.reporter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumRobotServerTestRecorder extends CommonReporter implements IReporter {

	public SeleniumRobotSnapshotServerConnector getServerConnector() {
		return new SeleniumRobotSnapshotServerConnector();
	}
	
	/**
	 * Generate result for a single test method
	 * @param ve			velocity engine used to generate file
	 * @param testResult	result for this test method
	 */
	public String generateExecutionLogs(final ITestResult testResult) {
		
		// add logs
		String logs = SeleniumRobotLogger.getTestLogs().get(testResult.getName());
		if (logs == null) {
			return "";
		}
		
		// exception handling
		StringBuilder stackString = new StringBuilder();
		if (testResult.getThrowable() != null) {
			generateTheStackTrace(testResult.getThrowable(), testResult.getThrowable().getMessage(), stackString);
		}
		
		logs += "\\nStacktrace\\n" + stackString.toString();

		return logs;
	}

	/**
	 * Generate all test reports
	 */
	@Override
	public void generateReport(final List<XmlSuite> xml, final List<ISuite> suites, final String outdir) {
		ITestContext testCtx = SeleniumTestsContextManager.getGlobalContext().getTestNGContext();
		if (testCtx == null) {
			logger.error("Looks like your class does not extend from SeleniumTestPlan!");
			return;
		}
		
		// check that seleniumRobot server is alive
		SeleniumRobotSnapshotServerConnector serverConnector = getServerConnector();
		if (!serverConnector.getActive()) {
			logger.info("selenium-robot-server not found or down");
			return;
		} else {
			try {
				serverConnector.createApplication();
				serverConnector.createVersion();
				serverConnector.createEnvironment();
				serverConnector.createSession();
			} catch (SeleniumRobotServerException | ConfigurationException e) {
				logger.error("Error contacting selenium robot serveur", e);
				return;
			}
		}
		
		try {
			for (ISuite suite : suites) {
				Map<String, ISuiteResult> tests = suite.getResults();
				for (ISuiteResult r : tests.values()) {
					ITestContext context = r.getTestContext();
					
					// test case in seleniumRobot naming
					for (ITestNGMethod method: context.getAllTestMethods()) {
		
						Collection<ITestResult> methodResults = getResultSet(context.getFailedTests(), method);
						methodResults.addAll(getResultSet(context.getPassedTests(), method));
						methodResults.addAll(getResultSet(context.getSkippedTests(), method));
	
						if (!methodResults.isEmpty()) {
							
							// record test case
							serverConnector.createTestCase(method.getMethodName());
							serverConnector.createTestCaseInSession();
							
							List<TestStep> testSteps = TestLogging.getTestsSteps().get(methodResults.toArray(new ITestResult[] {})[0]);
							if (testSteps == null) {
								continue;
							}
							
							for (TestStep testStep: testSteps) {
								
								// record test step
								serverConnector.createTestStep(testStep.getName());
								serverConnector.recordStepResult(!testStep.getFailed(), testStep.toString());
							}
						}
					}
				}
			}
		} catch (SeleniumRobotServerException | ConfigurationException e) {
			logger.error("Error contacting selenium robot serveur", e);
			return;
		}
	
	}


	/**
	 * @param   tests
	 *
	 * @return
	 */
	protected Collection<ITestResult> getResultSet(final IResultMap tests, final ITestNGMethod method) {
		Set<ITestResult> r = new TreeSet<>();
		for (ITestResult result : tests.getAllResults()) {
			if (result.getMethod().getMethodName().equals(method.getMethodName())) {
				r.add(result);
			}
		}

		return r;
	}


}
