package com.seleniumtests.ut.connectors.bugtracker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.connectors.bugtracker.FakeBugTracker;
import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;

@PrepareForTest({BugTracker.class})
public class TestBugTracker extends MockitoTest {

	@Mock
	private JiraConnector jiraConnector;
	
	private ScreenShot screenshot;
	private TestStep step1;
	private TestStep stepEnd;

	@BeforeMethod(groups= {"ut"})
	public void init() throws IOException {
		File tmpImg = File.createTempFile("img", ".png");
		File tmpHtml = File.createTempFile("html", ".html");
		
		screenshot = new ScreenShot();
		screenshot.setImagePath("screenshot/" + tmpImg.getName());
		screenshot.setHtmlSourcePath("htmls/" + tmpHtml.getName());
		FileUtils.copyFile(tmpImg, new File(screenshot.getFullImagePath()));
		FileUtils.copyFile(tmpHtml, new File(screenshot.getFullHtmlPath()));
		
		step1 = new TestStep("step 1", null, new ArrayList<>(), false);
		step1.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		
		stepEnd = new TestStep("Test end", null, new ArrayList<>(), false);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end", SnapshotCheckType.FULL), 1, null);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end2", SnapshotCheckType.FULL), 1, null);
	}

	@Test(groups={"ut"})
	public void testJiraBugtracker() throws Exception {
		PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenReturn(jiraConnector);
		
		BugTracker bugtracker = BugTracker.getInstance("jira", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		Assert.assertTrue(bugtracker instanceof JiraConnector);

	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testUnknownBugtracker() throws Exception {
		PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenReturn(jiraConnector);
		
		BugTracker.getInstance("mantis", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
	}
	
	/**
	 * Create a new issue
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssue() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
			
		bugtracker.createIssue("me", "you", "selenium", "DEV", "ngName", "myTest", "some description", 
				Arrays.asList(step1, stepEnd));
		
		// check that we check if the issue already exists
		verify(fbt).issueAlreadyExists(any(IssueBean.class));
		
		// check that issue is created
		verify(fbt).createIssue(any(IssueBean.class));
	}
	
	/**
	 * Update an existing issue as we fail on an other step
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testUpdateIssue() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		when(fbt.issueAlreadyExists(any(IssueBean.class))).thenReturn(new IssueBean("ISSUE-1", "[Selenium][app][env][ng] test Test1 KO", "Test KO"));
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
			
		bugtracker.createIssue("me", "you", "selenium", "DEV", "ngName", "myTest", "some description", 
				Arrays.asList(step1, stepEnd));
		
		// check that we check if the issue already exists
		verify(fbt).issueAlreadyExists(any(IssueBean.class));
		
		// check that issue is not created
		verify(fbt, never()).createIssue(any(IssueBean.class));
		verify(fbt).updateIssue(eq("ISSUE-1"), anyString(), anyList());
	}
	
	/**
	 * Do not update existing issue as we fail on the same step
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDoNotUpdateIssue() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		when(fbt.issueAlreadyExists(any(IssueBean.class))).thenReturn(new IssueBean("ISSUE-1", "[Selenium][app][env][ng] test Test1 KO", String.format(BugTracker.STEP_KO_PATTERN, 1)));
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
	
		bugtracker.createIssue("me", "you", "selenium", "DEV", "ngName", "myTest", "some description", 
				Arrays.asList(step1, stepEnd));
		
		// check that we check if the issue already exists
		verify(fbt).issueAlreadyExists(any(IssueBean.class));
		
		// check that issue is not created
		verify(fbt, never()).createIssue(any(IssueBean.class));
		verify(fbt, never()).updateIssue(eq("ISSUE-1"), anyString(), anyList());
	}
	

	/**
	 * Create a new issue bean
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssueBean() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
			
		
		IssueBean issueBean = bugtracker.createIssueBean("me", "you", "[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateIssueBean", "some description", 
				Arrays.asList(step1, stepEnd));
		
		Assert.assertEquals(issueBean.getAssignee(), "me");
		Assert.assertEquals(issueBean.getDescription(), "some descriptionStep 1 KO\n\nStep 'step 1' in error\n\nStep Test end\n\nFor more details, see attached .zip file");
		Assert.assertEquals(issueBean.getSummary(), "[Selenium][selenium][DEV][ngName] test myTest KO");
		Assert.assertEquals(issueBean.getReporter(), "you");
		Assert.assertEquals(issueBean.getTestName(), "testCreateIssueBean");
		Assert.assertEquals(issueBean.getScreenShots(), Arrays.asList(screenshot, screenshot)); // screenshots from the last step
		Assert.assertEquals(issueBean.getTestStep(), stepEnd); 
		Assert.assertEquals(issueBean.getDateTime().getDayOfMonth(),  ZonedDateTime.now().plusHours(3).getDayOfMonth()); 
		Assert.assertTrue(issueBean.getDetailedResult().isFile());
		Assert.assertTrue(issueBean.getDetailedResult().length() > 1000);
		Assert.assertNull(issueBean.getId()); // not inistialized by default
	}
	
	/**
	 * If no steps are available, do not create IssueBean
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssueBeanNoStep() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		
		IssueBean issueBean = bugtracker.createIssueBean("me", "you", "[Selenium][selenium][DEV][ngName] test myTest KO", "myTest", "some description", 
				new ArrayList<>());
		
		Assert.assertNull(issueBean);
	}
	
	/**
	 * If no Test end steps are available, do not create IssueBean
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssueBeanNoEndStep() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		
		IssueBean issueBean = bugtracker.createIssueBean("me", "you", "[Selenium][selenium][DEV][ngName] test myTest KO", "myTest", "some description", 
				Arrays.asList(step1));
		
		Assert.assertNull(issueBean);
	}
}
