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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.seleniumtests.reporter.logger.FileContent;
import org.apache.logging.log4j.Logger;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SuiteRunner;
import org.testng.TestRunner;
import org.testng.reporters.FailedReporter;
import org.testng.xml.XmlSuite;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.testanalysis.ErrorCause;
import com.seleniumtests.core.testanalysis.ErrorCauseFinder;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;
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
	private final JUnitReporter junitReporter;
	private final FailedReporter failedReporter;

	public ReporterControler() {
		junitReporter = new JUnitReporter();
		failedReporter = new FailedReporter();
	}

	
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		generateReport(xmlSuites, suites, outputDirectory, null);
		
		// remove test logs
		SeleniumRobotLogger.cleanTestLogs();
	}

	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory, ITestResult currentTestResult) {
		
		synchronized (reporterLock) {

			Map<ITestContext, Set<ITestResult>> resultSet = updateTestSteps(suites, currentTestResult);
			try {
				new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).mkdirs();
			} catch (Exception e) {
				logger.warn(String.format("Problem creating output directory %s: %s", SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), e.getMessage()));
			}
			
			// are we at the end of all suites (suite.getResults() has the same size as the returned result map)
			boolean suiteFinished = true;
			for (ISuite suite: suites) {
				if (suite.getResults().size() != resultSet.size()) {
					suiteFinished = false;
					break;
				}
			}
			
			// change / add test result according to snapshot comparison results
			if (suiteFinished) {
				changeTestResultsWithSnapshotComparison(suites);
			}

			try {
				if (suiteFinished) {
					junitReporter.generateReport(xmlSuites, suites, SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
					failedReporter.generateReport(xmlSuites, suites, SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
				} else {
					junitReporter.generateReport(resultSet, SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), true);
				}
			} catch (Exception e) {
				logger.error("Error generating JUnit report", e);
			}
			
			for (Class<?> reporterClass: SeleniumTestsContextManager.getGlobalContext().getReporterPluginClasses()) {
				try {
					if (suiteFinished) {
						CommonReporter reporter = CommonReporter.getInstance(reporterClass);
						reporter.generateReport(xmlSuites, suites, outputDirectory);
						
					// when the tests are currently running, do optimize reports (for example, html results will have their resources on CDN)
					} else {
						
						CommonReporter reporter = CommonReporter.getInstance(reporterClass);
						reporter.generateReport(resultSet, outputDirectory, true);
					}
					
				} catch (Exception e) {
					logger.error("Error generating report", e);
				}
			}

			cleanAttachments(currentTestResult);
			logger.info("Reports generated");
		}
	}

	
	/**
	 * If snapshot comparison has been enabled, request snapshot server for each test result to know if comparison was successful
	 * /!\ This method is aimed to be called only once all test suites have been completed 
	 * @param suites	test suites
	 */
	private void changeTestResultsWithSnapshotComparison(List<ISuite> suites) {
		
		if (!(SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerActive()
				&& SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshot())) {
			return;
		}
		
		SeleniumRobotSnapshotServerConnector snapshotServer = SeleniumRobotSnapshotServerConnector.getInstance();
		
		for (ISuite suite: suites) {
			for (String suiteString: suite.getResults().keySet()) {
				ISuiteResult suiteResult = suite.getResults().get(suiteString);
				
				Set<ITestResult> resultSet = new HashSet<>(); 
				resultSet.addAll(suiteResult.getTestContext().getFailedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
				resultSet.addAll(suiteResult.getTestContext().getSkippedTests().getAllResults());
				
				for (ITestResult testResult: resultSet) {
					
					// check if we have an id from snapshot server
					Integer testCaseInSessionId = TestNGResultUtils.getSnapshotTestCaseInSessionId(testResult);
					if (testCaseInSessionId == null) {
						continue;
					}
					
					StringBuilder errorMessage = new StringBuilder();
					int snapshotComparisonResult = snapshotServer.getTestCaseInSessionComparisonResult(testCaseInSessionId, errorMessage);
					
					// update snapshot comparison result of the run test.
					TestNGResultUtils.setSnapshotComparisonResult(testResult, snapshotComparisonResult);

					changeTestResultWithSnapshotComparison(suiteResult, testResult, snapshotComparisonResult);
				}
			}
		}
	}

	/**
	 * Change the test result based on snapshot comparison result if required by test configuration
	 * If test is OK but comparison fails, then test will be set to "KO" if 'changeTestResult' is set
	 * @param suiteResult				the TestNG suite
	 * @param testResult				the test result to update
	 * @param snapshotComparisonResult	snapshot comparison result which may update the test result
	 */
	private void changeTestResultWithSnapshotComparison(ISuiteResult suiteResult, ITestResult testResult, int snapshotComparisonResult) {
		// based on snapshot comparison flag, change test result only if comparison is KO
		if (SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour() == SnapshotComparisonBehaviour.CHANGE_TEST_RESULT && snapshotComparisonResult == ITestResult.FAILURE ) {
			testResult.setStatus(ITestResult.FAILURE);
			testResult.setThrowable(new ScenarioException("Snapshot comparison failed"));
			
		} else if (SeleniumTestsContextManager.getGlobalContext().seleniumServer().getSeleniumRobotServerCompareSnapshotBehaviour() == SnapshotComparisonBehaviour.ADD_TEST_RESULT) {
			
			ITestResult newTestResult;
			try {
				newTestResult = TestNGResultUtils.copy(testResult, "snapshots-" +testResult.getName(), TestNGResultUtils.getTestDescription(testResult) + " FOR SNAPSHOT COMPARISON");
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new ScenarioException(e.getMessage(), e);
			}
			
			// add the test result
			newTestResult.setStatus(snapshotComparisonResult);
			if (snapshotComparisonResult == ITestResult.SUCCESS) {
				suiteResult.getTestContext().getPassedTests().addResult(newTestResult);
			} else if (snapshotComparisonResult == ITestResult.FAILURE) {
				newTestResult.setThrowable(new ScenarioException("Snapshot comparison failed"));
				suiteResult.getTestContext().getFailedTests().addResult(newTestResult);
			} else if (snapshotComparisonResult == ITestResult.SKIP) {
				suiteResult.getTestContext().getSkippedTests().addResult(newTestResult);
			}
			
			// add a snapshot comparison result for the newly created test result (which correspond to snapshot comparison)
			TestNGResultUtils.setSnapshotComparisonResult(newTestResult, snapshotComparisonResult);
		}
	}
	
	/**
	 * Add configurations methods to list of test steps so that they can be used by reporters
	 * @param suites				List of test suite to parse
	 * @param currentTestResult		When we generate temp results, we get a current test result so that we do not wait test2 to be executed to get test1 displayed in report
	 */
	private Map<ITestContext, Set<ITestResult>> updateTestSteps(List<ISuite> suites, ITestResult currentTestResult) {
		Map<ITestContext, Set<ITestResult>> allResultSet = new LinkedHashMap<>();
		
		for (ISuite suite: suites) {
			
			Field testRunnersField;
			List<TestRunner> testContexts;
			try {
				testRunnersField = SuiteRunner.class.getDeclaredField("testRunners");
				testRunnersField.setAccessible(true);
				testContexts = (List<TestRunner>) testRunnersField.get(suite);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | ClassCastException e) {
				throw new CustomSeleniumTestsException("TestNG may have changed");
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
					synchronized (testSteps) {
						for (TestStep step: testSteps) {
							testDuration += step.getDuration();
						}
					}
					
					testResult.setEndMillis(testResult.getStartMillis() + testDuration);
				}
			}
		}
		
		return allResultSet;
	}
	
	/**
	 * Remove duplicated results (when a test is reexecuted, we have several results for the same scenario)
	 * TODO: see if we could remove the same method in SeleniumRobotTestListener
	 * @param context				TestNG context from which we extract list of tests
	 * @param currentTestResult
	 * @return new tests results
	 */
	public Set<ITestResult> removeUnecessaryResults(ITestContext context, ITestResult currentTestResult) {
		
		// copy current results in context so that it does not change during processing when several threads are used
		// only keep finished tests (issue #654)
		List<ITestResult> allResults = new ArrayList<>();
		Set<ITestResult> passedTests = context.getPassedTests().getAllResults().stream().filter(TestNGResultUtils::isFinished).collect(Collectors.toCollection(TreeSet::new));
		Set<ITestResult> failedTests = context.getFailedTests().getAllResults().stream().filter(TestNGResultUtils::isFinished).collect(Collectors.toCollection(TreeSet::new));
		Set<ITestResult> skippedTests = context.getSkippedTests().getAllResults().stream().filter(TestNGResultUtils::isFinished).collect(Collectors.toCollection(TreeSet::new));
		
		allResults.addAll(passedTests);
		allResults.addAll(failedTests);
		allResults.addAll(skippedTests);
		
		if (currentTestResult != null && currentTestResult.getTestContext() != null && currentTestResult.getTestContext().equals(context)) {
			allResults.add(currentTestResult);
		}

		// get an ordered list of test results so that we keep the last one of each test
		allResults = allResults.stream()
				.sorted(Comparator.comparingLong(ITestResult::getStartMillis))
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
		resultSet.addAll(context.getFailedTests().getAllResults().stream().filter(TestNGResultUtils::isFinished).collect(Collectors.toSet()));
		resultSet.addAll(context.getPassedTests().getAllResults().stream().filter(TestNGResultUtils::isFinished).collect(Collectors.toSet()));
		resultSet.addAll(context.getSkippedTests().getAllResults().stream().filter(TestNGResultUtils::isFinished).collect(Collectors.toSet()));
		
		// it's our current result, so we want if context matches
		if (currentTestResult != null && currentTestResult.getTestContext() != null && currentTestResult.getTestContext().equals(context)) {
			resultSet.add(currentTestResult);
		}
		
		return resultSet;
	}
	
	/**
	 * Delete all files in html and screenshot folders that are not directly references by test steps in current result
	 * @param currentResult	the test result to parse
	 */
	private void cleanAttachments(ITestResult currentResult) {

		if (currentResult == null) {
			return;
		}
		
		List<File> usedFiles = new ArrayList<>();

        // without context, nothing can be done
		SeleniumTestsContext testContext = TestNGResultUtils.getSeleniumRobotTestContext(currentResult);
		if (testContext == null) {
			return;
		}
			
		// get files referenced by the steps
		for (TestStep testStep: testContext.getTestStepManager().getTestSteps()) {
			try {
				testStep.moveAttachments(testContext.getOutputDirectory());
			} catch (IOException e) {
				logger.error("Cannot move attachment {}", e.getMessage());
			}
			usedFiles.addAll(testStep.getAllAttachments().stream().map(FileContent::getFile).toList());
			
		}

        List<File> allFiles = new ArrayList<>(listAttachments(testContext));
		
		for (File file: allFiles) {
			if (!usedFiles.contains(file)) {
				try {
					Files.deleteIfExists(file.toPath());
				} catch (IOException e)  {
					logger.info("File {} not deleted: {}", file.getAbsolutePath(), e.getMessage());
				}
			}
		}
	}
	
	/**
	 * List all attachments in output directory folder
	 * @param testContext	the test context
	 */
	private List<File> listAttachments(SeleniumTestsContext testContext) {
		
		List<File> allFiles = new ArrayList<>();

		String outputSubDirectory = new File(testContext.getOutputDirectory()).getName();
		String outputDirectoryParent = new File(testContext.getOutputDirectory()).getParent();
		File htmlDir = Paths.get(outputDirectoryParent, outputSubDirectory, SeleniumTestsContext.HTML_DIRECTORY).toFile();
		File htmlBeforeDir = Paths.get(outputDirectoryParent, "before-" + outputSubDirectory, SeleniumTestsContext.HTML_DIRECTORY).toFile();
		File screenshotDir = Paths.get(outputDirectoryParent, outputSubDirectory, SeleniumTestsContext.SCREENSHOT_DIRECTORY).toFile();
		File screenshotBeforeDir = Paths.get(outputDirectoryParent, "before-" + outputSubDirectory, SeleniumTestsContext.SCREENSHOT_DIRECTORY).toFile();
		File videoDir = Paths.get(outputDirectoryParent, outputSubDirectory, SeleniumTestsContext.VIDEO_DIRECTORY).toFile();
		
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
		if (videoDir.isDirectory()) {
			allFiles.addAll(Arrays.asList(videoDir.listFiles()));
		}
		
		return allFiles;
	}

	/**
	 * Returns the list of all test steps, including configuration method calls
	 * Use TestStep created in LogAction.java
	 */
	protected List<TestStep> getAllTestSteps(final ITestResult testResult) {
		if (TestNGResultUtils.getSeleniumRobotTestContext(testResult) == null) {
			return new ArrayList<>();
		}
		return TestNGResultUtils.getSeleniumRobotTestContext(testResult).getTestStepManager().getTestSteps();

	}
}
