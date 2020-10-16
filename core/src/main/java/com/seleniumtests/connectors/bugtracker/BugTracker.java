package com.seleniumtests.connectors.bugtracker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;
import org.testng.ITestResult;

import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class BugTracker {

	private static final String STEP_KO_PATTERN = "Step %d KO\n\n";
	protected static Logger logger = SeleniumRobotLogger.getLogger(BugTracker.class);

	public void createIssue(ITestResult testResult, 
			String assignee, 
			String priority, 
			Map<String, String> customFields, 
			List<String> components,
			String application,
			String environment,
			String testNgName,
			String testName,
			String description,
			List<ScreenShot> screenShots,
			List<TestStep> testSteps) {


		
		StringBuilder fullDescription = new StringBuilder(description);
		int stepIdx = 0;
		for (TestStep testStep: testSteps) {
			if (testStep.getName().startsWith("Test end")) {
				
				fullDescription.append(String.format(STEP_KO_PATTERN, stepIdx));

				// on ne va pas créer une jira si le test ne s'est même pas exécuté
				if (stepIdx == 0) {
					return;
				}
				fullDescription.append(String.format("Step '%s' in error\n\n", testSteps.get(stepIdx - 1).getName()));
				fullDescription.append(testStep.toString() + "\n\n");
				screenShots = testStep.getSnapshots().stream()
										.map(s -> s.getScreenshot())
										.collect(Collectors.toList());
				break;
			}
			stepIdx += 1;
		}

		fullDescription.append("For more details, see attached .zip file");
		String summary = String.format("[Selenium][%s][%s][%s] test %s KO", application, environment, testNgName, testName);

		// get HTML report generated by SeleniumTestsReporter2 class
		File zipFile = null;
		Path outRoot = null;
		try {
			File resourcesFolder = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "resources").toFile().getAbsoluteFile();
			File testResultFolder = Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), testName).toFile().getAbsoluteFile();
			outRoot = Files.createTempDirectory("result");
			Path tempResourcesFolder = Files.createDirectory(Paths.get(outRoot.toString(), "resources"));
			Path tempResultFolder = Files.createDirectory(Paths.get(outRoot.toString(), testName));

			IOFileFilter aviFiles = FileFilterUtils.notFileFilter(
					FileFilterUtils.or(
							FileFilterUtils.suffixFileFilter(".avi", null), // exclude video
							FileFilterUtils.suffixFileFilter(".zip", null)  // exclude previous reports
					)
			);

			// copy test results
			FileUtils.copyDirectory(testResultFolder, tempResultFolder.toFile(), aviFiles);

			// copy resources
			try {
				FileUtils.copyDirectory(resourcesFolder, tempResourcesFolder.toFile(), aviFiles);
			} catch (IOException e) {}

			// create zip
			zipFile = File.createTempFile("result", ".zip");
			zipFile.deleteOnExit();
			FileUtility.zipFolder(outRoot.toFile(), zipFile);
		} catch (IOException e) {
		} finally {
			if (outRoot != null) {
				try {
					FileUtils.deleteDirectory(outRoot.toFile());
				} catch (IOException e) {}
			}
		}

		IssueBean issueBean = new IssueBean(summary,
				fullDescription.toString(),
				priority,
				testName,
				testSteps.get(stepIdx),
				assignee,
				"",
				screenShots,
				zipFile,
				customFields,
				components);

		// check that a Jira does not already exist for the same test / appication / version. Else, complete it if the step is error is not the same
		IssueBean currentIssue = issueAlreadyExists(issueBean);
		if (currentIssue != null) {
			if (currentIssue.getDescription().contains(String.format(STEP_KO_PATTERN, stepIdx))) {
				logger.info(String.format("Jira %s already exists", currentIssue.getId()));
			} else {
				updateIssue(currentIssue.getId(), "Scenario fails on another step " + issueBean.getTestStep().getName(), issueBean.getScreenShots());
			}
		} else {
			createIssue(issueBean);
		}
	}
	
	  /**
     * Check if issue already exists, and if so, returns an updated IssueBean
     *
     * @return
     */
	public abstract IssueBean issueAlreadyExists(IssueBean issue);
	
	/**
	 * Update an existing issue with a new message and new screenshots.
	 * Used when an issue has already been raised, we complete it
	 * @param issueId			Id of the issue
	 * @param messageUpdate		message to add to description
	 * @param screenShots		New screenshots
	 */
	public abstract void updateIssue(String issueId, String messageUpdate, List<ScreenShot> screenShots);
	
	/**
	 * Create an issue
	 * @param issueBean
	 * @return
	 */
	public abstract void createIssue(IssueBean issueBean);

	public static BugTracker getInstance(String type, String url, String project, String user, String password) {

		if ("jira".equals(type)) {
			return new JiraConnector(url, project, user, password);
		} else {
			throw new ConfigurationException(String.format("BugTracker type [%s] is unknown, valid values are: ['jira']", type));
		}
	}
}
