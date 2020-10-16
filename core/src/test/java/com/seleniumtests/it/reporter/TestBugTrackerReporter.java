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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.connectors.bugtracker.BugTracker;
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

			ArgumentCaptor<List<TestStep>> testStepsArgument = ArgumentCaptor.forClass(List.class);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector).createIssue(isNull(), isNull(), isNull(), isNull(), eq(new HashMap<>()), eq(new ArrayList<>()), eq("core"), eq("DEV"), anyString(), eq("testInError"), contains("Test testInError failed"), testStepsArgument.capture());
			Assert.assertEquals(testStepsArgument.getValue().size(), 3);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
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
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
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
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
			
			
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
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
		}
	}
	
	@Test(groups={"it"})
	public void testCustomFieldsGet() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			System.setProperty("bugtracker.field.foo", "bar");
			System.setProperty("bugtracker.field.foo2", "bar2");

			ArgumentCaptor<Map<String, String>> fieldsArgument = ArgumentCaptor.forClass(Map.class);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector).createIssue(isNull(), isNull(), isNull(), isNull(), fieldsArgument.capture(), eq(new ArrayList<>()), eq("core"), eq("DEV"), anyString(), eq("testInError"), contains("Test testInError failed"), any());
			Assert.assertEquals(((Map)fieldsArgument.getValue()).size(), 2); // the 2 fields are captured
			Assert.assertEquals(((Map)fieldsArgument.getValue()).get("foo"), "bar"); // content ok
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty("bugtracker.field.foo");
			System.clearProperty("bugtracker.field.foo2");
		}
	}
	
	@Test(groups={"it"})
	public void testIssuePropertiesGet() throws Exception {
		try {
			System.setProperty(SeleniumTestsContext.BUGTRACKER_TYPE, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_URL, "http://localhost:1234");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PROJECT, "Project");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_USER, "jira");
			System.setProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD, "jira");
			System.setProperty("bugtracker.assignee", "assign");
			System.setProperty("bugtracker.reporter", "me");
			System.setProperty("bugtracker.priority", "P1");
			System.setProperty("bugtracker.issueType", "bug");
			System.setProperty("bugtracker.components", "comp1,comp2");
			
			ArgumentCaptor<List<String>> componentsArgument = ArgumentCaptor.forClass(List.class);
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForTestManager"}, ParallelMode.METHODS, new String[] {"testInError"});
			
			// check we have only one result recording for each test method
			verify(jiraConnector).createIssue(eq("assign"), eq("P1"), eq("bug"), eq("me"), any(), componentsArgument.capture(), eq("core"), eq("DEV"), anyString(), eq("testInError"), contains("Test testInError failed"), any());
			Assert.assertEquals(((List)componentsArgument.getValue()).size(), 2); // the 2 components are captured
			Assert.assertEquals(((List)componentsArgument.getValue()).get(0), "comp1"); // content ok
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PROJECT);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
			System.clearProperty("bugtracker.assignee");
			System.clearProperty("bugtracker.reporter");
			System.clearProperty("bugtracker.priority");
			System.clearProperty("bugtracker.issueType");
			System.clearProperty("bugtracker.components");
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
			
			verify(jiraConnector, never()).createIssue(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_TYPE);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_URL);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_USER);
			System.clearProperty(SeleniumTestsContext.BUGTRACKER_PASSWORD);
		}
	}	
}
