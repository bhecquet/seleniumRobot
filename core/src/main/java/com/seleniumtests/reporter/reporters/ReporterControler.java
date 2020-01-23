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
package com.seleniumtests.reporter.reporters;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SuiteRunner;
import org.testng.TestRunner;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * This reporter controls the execution of all other reporter because TestNG
 * @author s047432
 *
 */
public class ReporterControler implements IReporter {
	

	private static final Object reporterLock = new Object();
	private static final Logger logger = SeleniumRobotLogger.getLogger(ReporterControler.class);

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		generateReport(xmlSuites, suites, outputDirectory, null);
	}

	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory, ITestResult currentTestResult) {
		
		synchronized (reporterLock) {
			Map<ITestContext, Set<ITestResult>> resultSet = updateTestSteps(suites, currentTestResult);
			try {
				new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).mkdirs();
			} catch (Exception e) {}
			cleanAttachments(resultSet);
			
			// are we at the end of a suite (suite.getResults() has the same size as the returned result map)
			boolean suiteFinished = false;
			for (ISuite suite: suites) {
				if (suite.getResults().size() == resultSet.size()) {
					suiteFinished = true;
					break;
				}
			}

			try {
				new JUnitReporter().generateReport(xmlSuites, suites, SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
			} catch (Exception e) {}
			
			for (Class<?> reporterClass: SeleniumTestsContextManager.getGlobalContext().getReporterPluginClasses()) {
				try {
					if (suiteFinished) {
						IReporter reporter = (IReporter) reporterClass.getConstructor().newInstance();
						reporter.generateReport(xmlSuites, suites, outputDirectory);
						
					// when the tests are currently running, do optimize reports (for example, html results will have their resources on CDN
					} else {
						CommonReporter reporter = (CommonReporter) reporterClass.getConstructor().newInstance();
						reporter.generateReport(resultSet, outputDirectory, true);
					}
					
				} catch (Exception e) {
					logger.error("Error generating report", e);
				}
			}

		}
		
	}
	
	/**
	 * Add configurations methods to list of test steps so that they can be used by reporters
	 * @param suites				List of test suite to parse
	 * @param currentTestResult		When we generate temp results, we get a current test result so that we do not wait test2 to be executed to get test1 displayed in report
	 */
	private Map<ITestContext, Set<ITestResult>> updateTestSteps(final List<ISuite> suites, ITestResult currentTestResult) {
		Map<ITestContext, Set<ITestResult>> allResultSet = new LinkedHashMap<>();
		
		for (ISuite suite: suites) {
			
			Field testRunnersField;
			List<TestRunner> testContexts;
			try {
				testRunnersField = SuiteRunner.class.getDeclaredField("testRunners");
				testRunnersField.setAccessible(true);
				testContexts = (List<TestRunner>) testRunnersField.get(suite);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassCastException e) {
				throw new RuntimeException("TestNG may have change");
			}
			
			// If at least one test (not a test method, but a TestNG test) is finished, suite contains its results
			for (String suiteString: suite.getResults().keySet()) {
				ISuiteResult suiteResult = suite.getResults().get(suiteString);
				
				Set<ITestResult> resultSet = new HashSet<>(); 
				resultSet.addAll(suiteResult.getTestContext().getFailedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getSkippedTests().getAllResults());
				allResultSet.put(suiteResult.getTestContext(), resultSet);
			}
			
			// complete the suite result with remaining, currently running tests
			for (TestRunner testContext: testContexts) {
				if (allResultSet.containsKey(testContext)) {
					continue;
				}
				
				Set<ITestResult> resultSet = removeUnecessaryResults(testContext, currentTestResult);

				allResultSet.put(testContext, resultSet);
			}

			for (Set<ITestResult> resultSet: allResultSet.values()) {
				for (ITestResult testResult: resultSet) {
					List<TestStep> testSteps = getAllTestSteps(testResult);
					
					Long testDuration = 0L;
					for (TestStep step: testSteps) {
						testDuration += step.getDuration();
					}
					
					testResult.setEndMillis(testResult.getStartMillis() + testDuration);
				}
			}
		}
		
		return allResultSet;
	}
	
	/**
	 * Remove duplicated results (when a test is reexecuted, we have several results for the same scenario)
	 * 
	 * TODO: see if we could remove the same method in SeleniumRobotTestListener
	 * @param context
	 * @param currentTestResult
	 * @return
	 */
	public Set<ITestResult> removeUnecessaryResults(ITestContext context, ITestResult currentTestResult) {
		
		// copy current results in context so that it does not change during processing when several threads are used
		List<ITestResult> allResults = new ArrayList<>();
		Set<ITestResult> passedTests = new TreeSet<>(context.getPassedTests().getAllResults());
		Set<ITestResult> failedTests = new TreeSet<>(context.getFailedTests().getAllResults());
		Set<ITestResult> skippedTests = new TreeSet<>(context.getSkippedTests().getAllResults());
		
		allResults.addAll(passedTests);
		allResults.addAll(failedTests);
		allResults.addAll(skippedTests);
		
		if (currentTestResult != null && currentTestResult.getTestContext().equals(context)) {
			allResults.add(currentTestResult);
		}

		// get an ordered list of test results so that we keep the last one of each test
		allResults = allResults.stream()
				.sorted((r1, r2) -> Long.compare(r1.getStartMillis(), r2.getStartMillis()))
				.collect(Collectors.toList());
		
		// contains only the results to keep, that means, the last execution of each test
		Map<String, ITestResult> uniqueResults = new HashMap<>();
		for (ITestResult result: allResults) {
			String hash = TestNGResultUtils.getHashForTest(result);
			uniqueResults.put(hash, result);
		}
		
		// remove results we do not want from context
		List<ITestResult> resultsToKeep = new ArrayList<>(uniqueResults.values());
		
		for (ITestResult result: failedTests) {
			if (!resultsToKeep.contains(result)) {
				context.getFailedTests().removeResult(result);
			}
		}
		for (ITestResult result: skippedTests) {
			if (!resultsToKeep.contains(result)) {
				context.getSkippedTests().removeResult(result);
			}
		}
		for (ITestResult result: passedTests) {
			if (!resultsToKeep.contains(result)) {
				context.getPassedTests().removeResult(result);
			}
		}
		
		Set<ITestResult> resultSet = new HashSet<>(); 
		resultSet.addAll(context.getFailedTests().getAllResults());
		resultSet.addAll(context.getPassedTests().getAllResults());
		resultSet.addAll(context.getSkippedTests().getAllResults());
		
		// it's our current result, so we want if context matches
		if (currentTestResult != null && currentTestResult.getTestContext().equals(context)) {
			resultSet.add(currentTestResult);
		}
		
		return resultSet;
	}
	
	/**
	 * Delete all files in html and screenshot folders that are not directly references by any test step
	 * @param suites
	 */
	private void cleanAttachments(Map<ITestContext, Set<ITestResult>> resultSet) {
		
		List<File> usedFiles = new ArrayList<>();
		List<File> allFiles = new ArrayList<>();
		
		Set<ITestResult> allResultsSet = new HashSet<>();
		for (Set<ITestResult> rs: resultSet.values()) {
			allResultsSet.addAll(rs);
		}
		
		for (ITestResult testResult: allResultsSet) {
			
			// without context, nothing can be done
			SeleniumTestsContext testContext = TestNGResultUtils.getSeleniumRobotTestContext(testResult);
			if (testContext == null) {
				continue;
			}
			
			// get files referenced by the steps
			for (TestStep testStep: testContext.getTestStepManager().getTestSteps()) {
				usedFiles.addAll(testStep.getAllAttachments());
			}
			
			String outputSubDirectory = new File(testContext.getOutputDirectory()).getName();
			String outputDirectoryParent = new File(testContext.getOutputDirectory()).getParent();
			File htmlDir = Paths.get(outputDirectoryParent, outputSubDirectory, "htmls").toFile();
			File htmlBeforeDir = Paths.get(outputDirectoryParent, "before-" + outputSubDirectory, "htmls").toFile();
			File screenshotDir = Paths.get(outputDirectoryParent, outputSubDirectory, "screenshots").toFile();
			File screenshotBeforeDir = Paths.get(outputDirectoryParent, "before-" + outputSubDirectory, "screenshots").toFile();
			
			// get list of existing files
			if (htmlDir.isDirectory()) {
				allFiles.addAll(Arrays.asList(htmlDir.listFiles()));
			}
			if (screenshotDir.isDirectory()) {
				allFiles.addAll(Arrays.asList(screenshotDir.listFiles()));
			}
			if (htmlBeforeDir.isDirectory()) {
				allFiles.addAll(Arrays.asList(htmlBeforeDir.listFiles()));
			}
			if (screenshotBeforeDir.isDirectory()) {
				allFiles.addAll(Arrays.asList(screenshotBeforeDir.listFiles()));
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
		return TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();

	}
}
