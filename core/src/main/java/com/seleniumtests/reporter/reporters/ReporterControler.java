package com.seleniumtests.reporter.reporters;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestListener;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;

/**
 * This reporter controls the execution of all other reporter because TestNG
 * @author s047432
 *
 */
public class ReporterControler implements IReporter {

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

		Set<ITestResult> resultSet = updateTestSteps(suites);
		try {
			new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).mkdirs();
		} catch (Exception e) {}
		cleanAttachments(resultSet);
		
		try {
			new SeleniumTestsReporter2().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
		
		try {
			new CustomReporter().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
		
		try {
			new SeleniumRobotServerTestRecorder().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
		
		try {
			new TestManagerReporter().generateReport(xmlSuites, suites, outputDirectory);
		} catch (Exception e) {}
	}
	
	/**
	 * Add configurations methods to list of test steps so that they can be used by reporters
	 * @param suites
	 */
	private Set<ITestResult> updateTestSteps(final List<ISuite> suites) {
		Set<ITestResult> allResultSet = new HashSet<>();
		
		for (ISuite suite: suites) {
			for (String suiteString: suite.getResults().keySet()) {
				ISuiteResult suiteResult = suite.getResults().get(suiteString);

				Set<ITestResult> resultSet = new HashSet<>(); 
				resultSet.addAll(suiteResult.getTestContext().getFailedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getSkippedTests().getAllResults());
				allResultSet.addAll(resultSet);
				
				for (ITestResult testResult: resultSet) {
					TestLogging.getTestsSteps().put(testResult, getAllTestSteps(testResult));			
				}
			}
		}
		
		return allResultSet;
	}
	
	/**
	 * Delete all files in html and screenshot folders that are not directly references by any test step
	 * @param suites
	 */
	private void cleanAttachments(Set<ITestResult> resultSet) {
		
		List<File> usedFiles = new ArrayList<>();
		List<File> allFiles = new ArrayList<>();
		
		// retrieve list of all files used by test steps
		for (Entry<ITestResult, List<TestStep>> testSteps: TestLogging.getTestsSteps().entrySet()) {
			
			// do not keep results of tests that has been retried
			if (!resultSet.contains(testSteps.getKey())) {
				continue;
			}
			
			for (TestStep testStep: testSteps.getValue()) {
				usedFiles.addAll(testStep.getAllAttachments());
			}

			SeleniumTestsContext testContext = (SeleniumTestsContext)testSteps.getKey().getAttribute(SeleniumRobotTestListener.TEST_CONTEXT);
			
			if (testContext == null) {
				continue;
			}
			
			File htmlDir = Paths.get(testContext.getOutputDirectory(), "htmls").toFile();
			File screenshotDir = Paths.get(testContext.getOutputDirectory(), "screenshots").toFile();
			
			// get list of existing files
			if (htmlDir.isDirectory()) {
				allFiles.addAll(Arrays.asList(htmlDir.listFiles()));
			}
			if (screenshotDir.isDirectory()) {
				allFiles.addAll(Arrays.asList(screenshotDir.listFiles()));
			}
		}
		
		for (File file: allFiles) {
			if (!usedFiles.contains(file)) {
				file.delete();
			}
		}
	}
		
	/**
	 * Returns the first TestStep which has the same name
	 * @param testSteps
	 * @param thisStep
	 * @return
	 */
	private TestStep getTestStepWithSameName(List<TestStep> testSteps, TestStep thisStep) {
		for (TestStep step: testSteps) {
			if (thisStep.getName().equals(step.getName())) {
				return step;
			}
		}
		return null;
	}

	/**
	 * Returns the list of all test steps, including configuration method calls
	 * Use TestStep creatd in LogAction.java
	 */
	protected List<TestStep> getAllTestSteps(final ITestResult testResult) {
		
		List<TestStep> testSteps = new ArrayList<>();
		
		// reorder all configuration methods
		List<ITestResult> allConfigMethods = new ArrayList<>();
		allConfigMethods.addAll(testResult.getTestContext().getFailedConfigurations().getAllResults());
		allConfigMethods.addAll(testResult.getTestContext().getPassedConfigurations().getAllResults());
		allConfigMethods.addAll(testResult.getTestContext().getSkippedConfigurations().getAllResults());
		allConfigMethods = allConfigMethods.stream()
				.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
				.collect(Collectors.toList());
		
		// add before configuration step
		// select only configuration methods that match this method / class / test name as allConfigMethods contains all configuration methods of this test context
		for (ITestResult config: allConfigMethods) {
			if (config.getMethod().isBeforeClassConfiguration()) {
				if (!config.getMethod().getTestClass().equals(testResult.getTestClass())) {
					continue;
				}
			} else if (config.getMethod().isBeforeTestConfiguration()) {
				if (!config.getTestContext().equals(testResult.getTestContext())) {
					continue;
				}
			} else if (config.getMethod().isBeforeMethodConfiguration()) {
				try {
					String methodName = ((Method)(config.getParameters()[0])).getName();
					if (!methodName.equals(testResult.getName())) {
						continue;
					}
					
					// if this step is already present for this test method, remove it as we want to take the last one
					TestStep stepAlreadyPresent = getTestStepWithSameName(testSteps, TestLogging.getTestsSteps().get(config).get(0));
					if (stepAlreadyPresent != null) {
						testSteps.remove(stepAlreadyPresent);
					}
				} catch (Exception e) {}
			} else {
				continue;
			}
			
			// matching, add step
			try {
				testSteps.addAll(TestLogging.getTestsSteps().get(config));
			} catch (NullPointerException e) {}
		}
		
		// add regular steps
		try {
			testSteps.addAll(TestLogging.getTestsSteps().get(testResult));
		} catch (NullPointerException e) {}
		
		// add after configuration step
		for (ITestResult config: allConfigMethods) {
			if (config.getMethod().isAfterClassConfiguration()) {
				if (!config.getMethod().getTestClass().equals(testResult.getTestClass())) {
					continue;
				}
			} else if (config.getMethod().isAfterTestConfiguration()) {
				if (!config.getTestContext().equals(testResult.getTestContext())) {
					continue;
				}
			} else if (config.getMethod().isAfterMethodConfiguration()) {
				
				try {
					String methodName = ((Method)(config.getParameters()[0])).getName();
					if (!methodName.equals(testResult.getName())) {
						continue;
					}
					
					// if this step is already present for this test method, remove it as we want to take the last one
					TestStep stepAlreadyPresent = getTestStepWithSameName(testSteps, TestLogging.getTestsSteps().get(config).get(0));
					if (stepAlreadyPresent != null) {
						testSteps.remove(stepAlreadyPresent);
					}
				} catch (Exception e) {}
			} else {
				continue;
			}
			
			// matching, add step
			try {
				testSteps.addAll(TestLogging.getTestsSteps().get(config));
			} catch (NullPointerException e) {}
		}
		
		return testSteps;
	}
}
