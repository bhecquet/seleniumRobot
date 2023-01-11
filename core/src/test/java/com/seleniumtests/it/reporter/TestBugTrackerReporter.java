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
package com.seleniumtests.it.reporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.connectors.bugtracker.jira.JiraBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.reporter.logger.TestStep;

@PrepareForTest({BugTracker.class})
public class TestBugTrackerReporter extends ReporterTest {

	@Mock
	private JiraConnector jiraConnector;
	
	@BeforeMethod(groups={"it"})
	public void initTestManager() throws Exception {

		PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenReturn(jiraConnector);

		BugTracker.resetBugTrackerInstances();
		
	}
	
	/**
	 * Check a failed test produces a issue creation
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIssueIsRecorded() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER, "me");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE, "you");
			System.setProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application", "app");

			ArgumentCaptor<List<TestStep>> testStepsArgument = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<Map<String, String>> issueOptionsArgument = ArgumentCaptor.forClass(Map.class);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector).createIssue(eq("core"), eq("DEV"), anyString(), eq("testInError"), contains("Test 'testInError' failed"), testStepsArgument.capture(), issueOptionsArgument.capture());
			Assert.assertEquals(testStepsArgument.getValue().size(), 5);
			
			Assert.assertEquals(issueOptionsArgument.getValue().size(), 3);
			Assert.assertEquals(issueOptionsArgument.getValue().get(BugTracker.BUGTRACKER_ISSUE_REPORTER), "me");
			Assert.assertEquals(issueOptionsArgument.getValue().get(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE), "you2"); // check we get the updated value, set directly in test method
			Assert.assertEquals(issueOptionsArgument.getValue().get(JiraConnector.BUGTRACKER_JIRA_FIELD + "application"), "app");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE);
			System.clearProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application");
		}
	}
	
	/**
	 * check that with data provider, a new issue can be created for each execution of the same method (index is present in test name, which guarantees that a different issue 
	 * will be created.
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIssueIsRecordedWithDataProvider() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			System.setProperty(SeleniumTestsContext.TEST_RETRY_COUNT, "0");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER, "me");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE, "you");
			System.setProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application", "app");
			
			ArgumentCaptor<Map<String, String>> issueOptionsArgument = ArgumentCaptor.forClass(Map.class);
			ArgumentCaptor<Map<String, String>> issueOptionsArgument2 = ArgumentCaptor.forClass(Map.class);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInErrorDataProvider"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector).createIssue(eq("core"), eq("DEV"), anyString(), eq("testInErrorDataProvider"), contains("Test 'testInErrorDataProvider' failed"), any(), issueOptionsArgument.capture());
			
			Assert.assertEquals(issueOptionsArgument.getValue().size(), 3);
			Assert.assertEquals(issueOptionsArgument.getValue().get(BugTracker.BUGTRACKER_ISSUE_REPORTER), "me");
			Assert.assertEquals(issueOptionsArgument.getValue().get(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE), "you2data1"); // check we get the updated value, set directly in test method
			Assert.assertEquals(issueOptionsArgument.getValue().get(JiraConnector.BUGTRACKER_JIRA_FIELD + "application"), "app");
			

			// check we have only one result recording for each test method
			verify(jiraConnector).createIssue(eq("core"), eq("DEV"), anyString(), eq("testInErrorDataProvider-1"), contains("Test 'testInErrorDataProvider-1' failed"), any(), issueOptionsArgument2.capture());
			
			Assert.assertEquals(issueOptionsArgument2.getValue().size(), 3);
			Assert.assertEquals(issueOptionsArgument2.getValue().get(BugTracker.BUGTRACKER_ISSUE_REPORTER), "me");
			Assert.assertEquals(issueOptionsArgument2.getValue().get(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE), "you2data2"); // check we get the updated value, set directly in test method
			Assert.assertEquals(issueOptionsArgument2.getValue().get(JiraConnector.BUGTRACKER_JIRA_FIELD + "application"), "app");
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty(SeleniumTestsContext.TEST_RETRY_COUNT);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE);
			System.clearProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application");
		}
	}
	
	
	/**
	 * Check that when Jira is created / present, a link is available in report
	 * We check in HTML report, but, it is the same for XML report (Here, we do not check that Perfreport works correctly)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIssueIsRecordedInReportsFakeBugtracker() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "fake");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "fake");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "fake");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER, "me");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE, "you");
			System.setProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application", "app");
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'");
			String creationDate = ZonedDateTime.now().format(formatter);
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check content of summary report file
			String mainReportContent = readSummaryFile();
			Assert.assertTrue(mainReportContent.matches(String.format(".*<td class=\"info\">1234</td><td class=\"info\">%s.*", creationDate)));
			
			String detailedReportContent = readTestMethodResultFile("testInError");
			Assert.assertTrue(detailedReportContent.contains("<th>Issue</th><td>1234</td>"));
			Assert.assertTrue(detailedReportContent.contains(String.format("<th>Issue date</th><td>%s", creationDate)));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE);
			System.clearProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application");
		}
	}
	
	/**
	 * Check that issue is recorded after all execution of a test method (including retries) have been performed, but before other test executions
	 */
	@Test(groups={"it"})
	public void testIssueRecordedOnceAllRetriesDoneAfterTestMethod() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "fake");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "fake");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "fake");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER, "me");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE, "you");
			System.setProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application", "app");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError", "testSkipped"});
			
			// check content of summary report file
			String logs = readSeleniumRobotLogFile();
			
			// check issue is created before the next test is started
			Assert.assertTrue(logs.matches(".*Issue 1234 created.*Start method testSkipped.*"));
			
			// check issue is created after all retries
			Assert.assertTrue(logs.matches(".*\\[NOT RETRYING\\] max retry count \\(2\\) reached.*Issue 1234 created.*"));
			
			// check issue has been recorded only once
			Assert.assertEquals(StringUtils.countMatches(logs, "Issue 1234 created"), 1);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE);
			System.clearProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application");
		}
	}
	
	/**
	 * Check that when Jira is created / present, a link is available in report
	 * We check in HTML report, but, it is the same for XML report (Here, we do not check that Perfreport works correctly)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIssueIsRecordedInReports() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER, "me");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE, "you");
			System.setProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application", "app");
			
			JiraBean jiraBean = new JiraBean("JIRA-1234", "summary", "description", "Bug", "P1");
			jiraBean.setAccessUrl("http://jira.server.com/browse/JIRA-1234");
			jiraBean.setDate("2021-01-06T15:18+01:00");
			ZonedDateTime creationDate = ZonedDateTime.now();
			jiraBean.setCreationDate(creationDate);
			
			when(jiraConnector.createIssue(eq("core"), eq("DEV"), anyString(), eq("testInError"), contains("Test 'testInError' failed"), any(), any())).thenReturn(jiraBean);
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check content of summary report file
			String mainReportContent = readSummaryFile();
			Assert.assertTrue(mainReportContent.matches(String.format(".*<td class=\"info\"><a href=\"http://jira.server.com/browse/JIRA-1234\">JIRA-1234</a></td><td class=\"info\">%s</td>.*", jiraBean.getCreationDate().replace("+", "\\+"))));

			String detailedReportContent = readTestMethodResultFile("testInError");
			Assert.assertTrue(detailedReportContent.contains("<th>Issue</th><td><a href=\"http://jira.server.com/browse/JIRA-1234\">JIRA-1234</a></td>"));
			Assert.assertTrue(detailedReportContent.contains(String.format("<th>Issue date</th><td>%s</td>", jiraBean.getCreationDate())));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE);
			System.clearProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application");
		}
	}
	
	
	/**
	 * Check that when Jira is created / present, without hyperlink, the issue name is present
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIssueIsRecordedInReportsNoURL() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER, "me");
			System.setProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE, "you");
			System.setProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application", "app");
			
			JiraBean jiraBean = new JiraBean("JIRA-1234", "summary", "description", "Bug", "P1");
			jiraBean.setDate("2021-01-06T15:18+01:00");
			ZonedDateTime creationDate = ZonedDateTime.now();
			jiraBean.setCreationDate(creationDate);
			
			when(jiraConnector.createIssue(eq("core"), eq("DEV"), anyString(), eq("testInError"), contains("Test 'testInError' failed"), any(), any())).thenReturn(jiraBean);
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check content of summary report file
			String mainReportContent = readSummaryFile();
			Assert.assertTrue(mainReportContent.matches(String.format(".*<td class=\"info\">JIRA-1234</td><td class=\"info\">%s</td>.*", jiraBean.getCreationDate().replace("+", "\\+"))));
			
			String detailedReportContent = readTestMethodResultFile("testInError");
			Assert.assertTrue(detailedReportContent.contains("<th>Issue</th><td>JIRA-1234</td>"));
			Assert.assertTrue(detailedReportContent.contains(String.format("<th>Issue date</th><td>%s</td>", jiraBean.getCreationDate())));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_REPORTER);
			System.clearProperty(BugTracker.BUGTRACKER_ISSUE_ASSIGNEE);
			System.clearProperty(JiraConnector.BUGTRACKER_JIRA_FIELD + "application");
		}
	}
	
	/**
	 * With test OK, no issue is recorded but we try to close a matching issue
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNoIssueIsRecordedWithTestSuccess() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");

			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testAndSubActions"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any());
			verify(jiraConnector).closeIssue(eq("core"), eq("DEV"), anyString(), eq("testAndSubActions"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
		}
	}
	
	/**
	 * With test Skipped, no issue is recorded
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNoIssueIsRecordedWithTestSkipped() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testSkipped"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any());
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
		}
	}
	
	/**
	 * Without bugtracker instance, no issue recorded, no error raised
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testNoIssueIsRecordeWithoutBugTracker() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "foo");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any());
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
		}
	}
	
	@Test(groups={"it"})
	public void testIssueNotRecordedMissingProject() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");

			PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenThrow(new ConfigurationException(""));
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
		}
	}	
	
	/**
	 * Check that when a test contains description, this is set in issue
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testIssueDescriptionIsInterpolated() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");

			System.setProperty("url", "http://mysite.com");

			ArgumentCaptor<List<TestStep>> testStepsArgument = ArgumentCaptor.forClass(List.class);
			ArgumentCaptor<Map<String, String>> issueOptionsArgument = ArgumentCaptor.forClass(Map.class);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassforTestDescription"}, ParallelMode.METHODS, new String[] {"testWithLineBreaksInDescription"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector).createIssue(eq("core"), eq("DEV"), anyString(), eq("testWithLineBreaksInDescription"), contains("Test 'testWithLineBreaksInDescription' failed\n" + 
					"Test goal: a test with param http://mysite.com\n" + 
					"and line breaks"), testStepsArgument.capture(), issueOptionsArgument.capture());
			Assert.assertEquals(testStepsArgument.getValue().size(), 3);

			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty("url");
		}
	}
}
