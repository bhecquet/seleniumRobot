package com.seleniumtests.connectors.bugtracker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

import com.seleniumtests.connectors.bugtracker.jira.JiraBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class BugTracker {

	public static final String STEP_KO_PATTERN = "Step %d: ";
	protected static Logger logger = SeleniumRobotLogger.getLogger(BugTracker.class);
	protected static Map<String, BugTracker> bugtrackerInstances = Collections.synchronizedMap(new HashMap<>());

	private String createIssueSummary(
			String application,
			String environment,
			String testNgName,
			String testName) {
		return String.format("[Selenium][%s][%s][%s] test %s KO", application, environment, testNgName, testName);
	}
	
	/**
	 * Format description
	 * For any bugtracker, description is quite simple but it can be improved depending on bug tracker 
	 * /!\ any method overriding this one MUST provide "STEP_KO_PATTERN" in the description because it's used to know if the failed step is the same 
	 * 
	 * @param testSteps
	 * @param fullDescription
	 * @param screenShots
	 * @return
	 */
	protected void formatDescription(String testName, List<TestStep> failedSteps, TestStep lastTestStep, String description, StringBuilder fullDescription) {

		fullDescription.append(String.format("Test: %s\n", testName));
		if (description != null) {
			fullDescription.append(String.format("Description: %s\n", description));
		}
		if (SeleniumTestsContextManager.getThreadContext().getStartedBy() != null) {
			fullDescription.append(String.format("Started by: %s\n", SeleniumTestsContextManager.getThreadContext().getStartedBy()));
		}
		for (TestStep failedStep: failedSteps) {
			fullDescription.append(String.format("Error step %d (%s): %s\n", failedStep.getPosition(), failedStep.getName(), failedStep.getActionException()));
		}
		fullDescription.append("\n");
		
		if (!failedSteps.isEmpty()) {
			fullDescription.append("Steps in error\n");
			for (TestStep failedStep: failedSteps) {
				fullDescription.append(String.format(STEP_KO_PATTERN + "%s\n", failedStep.getPosition(), failedStep.getName()));
				fullDescription.append("------------------------------------\n");
				fullDescription.append(failedStep.toString() + "\n\n");
			}
		}
	
		fullDescription.append("Last logs\n");
		fullDescription.append(lastTestStep.toString());
	}
	
	/**
	 * Create an issue object
	 * @param testName			method name (name of scenario)
	 * @param description		Description of the test. May be null
	 * @param testSteps			Test steps of the scenario
	 * @param issueOptions		options for the new issue 
	 * @return
	 */
	public IssueBean createIssueBean(
			String summary,
			String testName,
			String description,
			List<TestStep> testSteps, 
			Map<String, String> issueOptions) {
		
		TestStep lastTestStep = null;
		List<TestStep> failedSteps = new ArrayList<>();
		for (TestStep testStep: testSteps) {
			if (Boolean.TRUE.equals(testStep.getFailed())) {
				failedSteps.add(testStep);
			}
			if (testStep.getName().startsWith("Test end")) {
				lastTestStep = testStep;		
				break;
			}
		}

		// don't create issue if test has not been executed or not completed
		if (lastTestStep == null) {
			return null;
		}

		List<ScreenShot> screenShots = lastTestStep.getSnapshots().stream()
				.map(s -> s.getScreenshot())
				.collect(Collectors.toList());;
		StringBuilder fullDescription = new StringBuilder();
		
		formatDescription(testName, failedSteps, lastTestStep, description, fullDescription);
		
		fullDescription.append("\n\nFor more details, see attached .zip file");


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

			// copy resources (in some IT, it may not have been created
			if (resourcesFolder.exists()) {
				FileUtils.copyDirectory(resourcesFolder, tempResourcesFolder.toFile(), aviFiles);
			} else {
				logger.warn(String.format("Resource folder does not exist %s", resourcesFolder));
			}

			// create zip
			Path zipOutRoot = Files.createTempDirectory("detailedResult");
			zipOutRoot.toFile().deleteOnExit();
			zipFile = Paths.get(zipOutRoot.toString(), "detailedResult.zip").toFile();
			zipFile.deleteOnExit();
			FileUtility.zipFolder(outRoot.toFile(), zipFile);
		} catch (IOException e) {
			logger.error("Error while creating detailedResult.zip file", e);
		} finally {
			if (outRoot != null) {
				try {
					FileUtils.deleteDirectory(outRoot.toFile());
				} catch (IOException e) {
					logger.error(String.format("Error deleting temp %s", outRoot));
				}
			}
		}
		

		String assignee = issueOptions.get("assignee");
		String reporter = issueOptions.get("reporter");
		
		return createIssueBean(summary, fullDescription.toString(), testName, lastTestStep, assignee, reporter, screenShots, zipFile, issueOptions);
	}
	
	protected IssueBean createIssueBean(
			String summary,
			String fullDescription,
			String testName,
			TestStep lastStep, 
			String assignee,
			String reporter,
			List<ScreenShot> screenShots,
			File zipFile,
			Map<String, String> issueOptions) {
		return new IssueBean(summary,
				fullDescription,
				testName,
				lastStep,
				assignee,
				reporter,
				screenShots,
				zipFile);
	}
	
	/**
	 * Creates an issue if it does not already exist
	 * First we search for a similar open issue (same summary)
	 * If it exists, then we check the step where we failed. If it's the same, we do nothing, else, we update the issue, saying we failed on an other step.
	 * @param application	the tested application
	 * @param environment	the environment where we tested
	 * @param testNgName	name of the TestNG test. Helps to build the summary
	 * @param testName		method name (name of scenario)
	 * @param description	Description of the test. May be null
	 * @param testSteps		Test steps of the scenario
	 * @param issueOptions		options for the new issue 
	 */
	public IssueBean createIssue(
			String application,
			String environment,
			String testNgName,
			String testName,
			String description,
			List<TestStep> testSteps,
			Map<String, String> issueOptions) {


		String summary = createIssueSummary(application, environment, testNgName, testName);
		IssueBean issueBean = createIssueBean(summary, testName, description, testSteps, issueOptions);
		if (issueBean == null) {
			return null;
		}
		
		// get index of the last step to know where we failed
		int stepIdx = 0;
		for (TestStep testStep: testSteps) {
			if (testStep.getName().startsWith("Test end")) {
				break;
			}
			stepIdx += 1;
		}
		
		// check that an issue does not already exist for the same test / appication / version. Else, complete it if the step is error is not the same
		IssueBean currentIssue = issueAlreadyExists(issueBean);
		if (currentIssue != null) {
			if (currentIssue.getDescription().contains(String.format(STEP_KO_PATTERN, stepIdx))) {
				logger.info(String.format("Issue %s already exists", currentIssue.getId()));
			} else {
				updateIssue(currentIssue.getId(), "Scenario fails on another step " + issueBean.getTestStep().getName(), issueBean.getScreenShots());
			}
		} else {
			createIssue(issueBean);
			logger.info(String.format("Issue %s created", issueBean.getId()));
		}
		
		return issueBean;
	}
	
	/**
	 * Close issue if it exists
	 * @param application
	 * @param environment
	 * @param testNgName
	 * @param testName
	 */
	public void closeIssue( 
			String application,
			String environment,
			String testNgName,
			String testName) {
		
		String summary = createIssueSummary(application, environment, testNgName, testName);
		IssueBean issueBean;
		
		if (this instanceof JiraConnector) {
			issueBean = new JiraBean("", summary, "", "", "");
		} else {
			issueBean = new IssueBean(summary, "", "", null, null, null, null, null);
		}
		
		// Close issue if it exists
		IssueBean currentIssue = issueAlreadyExists(issueBean);
		if (currentIssue != null) {
			closeIssue(currentIssue.getId(), "Test is now OK");
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
	 * Create an issue. 
	 * This method should set the id and the accessUrl of the issue inside bean
	 * @param issueBean
	 * @return
	 */
	public abstract void createIssue(IssueBean issueBean);
	

	/**
     * Close issue
     * @param issueId           ID of the issue
     * @param closingMessage    Message of closing
     */
    public abstract void closeIssue(String issueId, String closingMessage);

	public static synchronized BugTracker getInstance(String type, String url, String project, String user, String password, Map<String, String> bugtrackerOptions) {

		if (!bugtrackerInstances.containsKey(url) || bugtrackerInstances.get(url) == null) {
		
			if ("jira".equals(type)) {
				bugtrackerInstances.put(url, new JiraConnector(url, project, user, password, bugtrackerOptions));
			} else if ("fake".equals(type)) {
				bugtrackerInstances.put(url,  new FakeBugTracker());
			} else {
				throw new ConfigurationException(String.format("BugTracker type [%s] is unknown, valid values are: ['jira']", type));
			}
		}
		return bugtrackerInstances.get(url);
	}
}
