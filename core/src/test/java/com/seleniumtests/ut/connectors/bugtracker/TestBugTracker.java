package com.seleniumtests.ut.connectors.bugtracker;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.customexception.ConfigurationException;

@PrepareForTest({BugTracker.class})
public class TestBugTracker extends MockitoTest {

	@Mock
	private JiraConnector jiraConnector;

	@Test(groups={"ut"})
	public void testJiraBugtracker() throws Exception {
		PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenReturn(jiraConnector);
		
		BugTracker bugtracker = BugTracker.getInstance("jira", "http://foo/bar", "selenium", "user", "password");
		Assert.assertTrue(bugtracker instanceof JiraConnector);
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testUnknownBugtracker() throws Exception {
		PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenReturn(jiraConnector);
		
		BugTracker.getInstance("mantis", "http://foo/bar", "selenium", "user", "password");
	}
}
