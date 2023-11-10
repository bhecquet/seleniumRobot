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
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.connectors.bugtracker.FakeBugTracker;
import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.reporters.SeleniumTestsReporter2;

//@PrepareForTest({BugTracker.class})
public class TestBugTracker extends MockitoTest {

	@Mock
	private JiraConnector jiraConnector;
	
	private ScreenShot screenshot;
	private TestStep step1;
	private TestStep step2;
	private TestStep stepFailedWithDisabledBugtracker;
	private TestStep stepEnd;
	
	Map<String, String> issueOptions = new HashMap<>();

	@BeforeMethod(groups= {"ut"})
	public void init() throws IOException {
		File tmpImg = File.createTempFile("img", ".png");
		tmpImg.deleteOnExit();
		File tmpHtml = File.createTempFile("html", ".html");
		tmpHtml.deleteOnExit();
		
		screenshot = new ScreenShot(tmpImg, tmpHtml);
		
		step1 = new TestStep("step 1", null, new ArrayList<>(), false);
		step1.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		step1.setPosition(0);

		step2 = new TestStep("step 2", null, new ArrayList<>(), false);
		step2.setFailed(true);
		step2.setActionException(new NullPointerException("Error clicking"));
		step2.addAction(new TestAction("action1", false, new ArrayList<>()));
		step2.addAction(new TestAction("action2", false, new ArrayList<>()));
		step2.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		step2.setPosition(1);
		
		stepFailedWithDisabledBugtracker = new TestStep("step 2", null, new ArrayList<>(), false, RootCause.NONE, "", true);
		stepFailedWithDisabledBugtracker.setFailed(true);
		stepFailedWithDisabledBugtracker.setActionException(new NullPointerException("Error clicking"));
		stepFailedWithDisabledBugtracker.addAction(new TestAction("action1", false, new ArrayList<>()));
		stepFailedWithDisabledBugtracker.addAction(new TestAction("action2", false, new ArrayList<>()));
		stepFailedWithDisabledBugtracker.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		stepFailedWithDisabledBugtracker.setPosition(1);
		
		stepEnd = new TestStep("Test end", null, new ArrayList<>(), false);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end", SnapshotCheckType.FULL), 1, null);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end2", SnapshotCheckType.FULL), 1, null);
		stepEnd.setPosition(2);
		
		issueOptions.put("bugtracker.reporter", "you");
		issueOptions.put("bugtracker.assignee", "me");
		
		BugTracker.resetBugTrackerInstances();
		
	}

	@Test(groups={"ut"})
	public void testJiraBugtracker() throws Exception {
//		PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenReturn(jiraConnector);
		
		BugTracker bugtracker = BugTracker.getInstance("jira", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		Assert.assertTrue(bugtracker instanceof JiraConnector);

	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testUnknownBugtracker() throws Exception {
//		PowerMockito.whenNew(JiraConnector.class).withAnyArguments().thenReturn(jiraConnector);
		
		BugTracker.getInstance("mantis", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
	}
	
	/**
	 * Create a new issue when there is a failed step
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssue() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
			
		bugtracker.createIssue("selenium", "DEV", "ngName", "testCreateIssue", "some description", 
				Arrays.asList(step2, stepEnd),
				issueOptions);
		
		// check that we check if the issue already exists
		verify(fbt).issueAlreadyExists(any(IssueBean.class));
		
		// check that issue is created
		verify(fbt).createIssue(any(IssueBean.class));
	}
	
	/**
	 * Test issue is not created if no failed step is present during execution
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDoNotCreateIssue() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		
		bugtracker.createIssue("selenium", "DEV", "ngName", "testDoNotCreateIssue", "some description", 
				Arrays.asList(step1, stepEnd),
				issueOptions);
		
		// check that we check if the issue already exists
		verify(fbt, never()).issueAlreadyExists(any(IssueBean.class));
		
		// check that issue is created
		verify(fbt, never()).createIssue(any(IssueBean.class));
	}
	
	/**
	 * Check that if we request the same bugtracker N times (for same URL), it's always the same instance returned
	 * This is to avoid staled connections
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testOnlyOneInstanceIsCreatedForSameUrl() throws Exception {
		BugTracker bugtracker1 = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		BugTracker bugtracker2 = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
	
	
		Assert.assertEquals(bugtracker1, bugtracker2);
	}
	
	@Test(groups={"ut"})
	public void testOneInstanceIsCreatedForEachUrl() throws Exception {
		BugTracker bugtracker1 = BugTracker.getInstance("fake", "http://foo/bar2", "selenium", "user", "password", new HashMap<>());
		BugTracker bugtracker2 = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		
		
		Assert.assertNotEquals(bugtracker1, bugtracker2);
	}
	
	/**
	 * Update an existing issue as we fail on an other step
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testUpdateIssue() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		when(fbt.issueAlreadyExists(any(IssueBean.class))).thenReturn(new IssueBean("ISSUE-1", "[Selenium][app][env][ng] test Test1 KO", "Test KO"));
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
			
		bugtracker.createIssue("selenium", "DEV", "ngName", "testUpdateIssue", "some description", 
				Arrays.asList(step1, step2, stepEnd), issueOptions);
		
		// check that we check if the issue already exists
		verify(fbt).issueAlreadyExists(any(IssueBean.class));
		
		// check that issue is not created
		verify(fbt, never()).createIssue(any(IssueBean.class));
		verify(fbt).updateIssue(eq("ISSUE-1"), anyString(), anyList(), eq(step2));
	}
	
	/**
	 * Do not update existing issue as we fail on the same step
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDoNotUpdateIssue() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		when(fbt.issueAlreadyExists(any(IssueBean.class))).thenReturn(new IssueBean("ISSUE-1", "[Selenium][app][env][ng] test Test1 KO", String.format(BugTracker.STEP_KO_PATTERN, 1)));
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
	
		bugtracker.createIssue("selenium", "DEV", "ngName", "testDoNotUpdateIssue", "some description", 
				Arrays.asList(step1, step2, stepEnd), issueOptions);
		
		// check that we check if the issue already exists
		verify(fbt).issueAlreadyExists(any(IssueBean.class));
		
		// check that issue is not created
		verify(fbt, never()).createIssue(any(IssueBean.class));
		verify(fbt, never()).updateIssue(eq("ISSUE-1"), anyString(), anyList(), eq(step2));
	}
	

	/**
	 * Create a new issue bean
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssueBean() throws Exception {
		// copy resources so that we can be sure something has been copied to zip file
		new SeleniumTestsReporter2().copyResources();
		
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
			
		
		IssueBean issueBean = bugtracker.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateIssueBean", "some description", 
				Arrays.asList(step1, step2, stepEnd), issueOptions);
		
		Assert.assertEquals(issueBean.getAssignee(), "me");
		Assert.assertEquals(issueBean.getDescription(), "Test: testCreateIssueBean\n" + 
				"Description: some description\n" +
				"Error step 1 (step 2): java.lang.NullPointerException: Error clicking\n" + 
				"\n" + 
				"Steps in error\n" +
				"Step 1: step 2\n" + 
				"------------------------------------\n" + 
				"Step step 2\n" + 
				"  - action1\n" + 
				"  - action2\n" + 
				"\n" + 
				"Last logs\n" + 
				"Step Test end\n" + 
				"\n" + 
				"For more details, see attached .zip file");
		Assert.assertEquals(issueBean.getSummary(), "[Selenium][selenium][DEV][ngName] test myTest KO");
		Assert.assertEquals(issueBean.getReporter(), "you");
		Assert.assertEquals(issueBean.getTestName(), "testCreateIssueBean");
		Assert.assertEquals(issueBean.getScreenShots(), Arrays.asList(screenshot, screenshot)); // screenshots from the last step
		Assert.assertEquals(issueBean.getTestStep(), step2); // we take the last failing step (not Test end)
		Assert.assertEquals(issueBean.getDateTime().getDayOfMonth(),  ZonedDateTime.now().plusHours(3).getDayOfMonth()); 
		Assert.assertTrue(issueBean.getDetailedResult().isFile());
		Assert.assertEquals(issueBean.getDetailedResult().getName(), "detailedResult.zip");
		Assert.assertTrue(issueBean.getDetailedResult().length() > 900000);
		Assert.assertNull(issueBean.getId()); // not initialized by default
	}
	
	/**
	 * Test that if the failed step disables bugtracker, the issue bean is not created
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testDoNotCreateIssueBeanWithStepDisabled() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
			
		
		IssueBean issueBean = bugtracker.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testDoNotCreateIssueBeanWithStepDisabled", "some description", 
				Arrays.asList(step1, stepFailedWithDisabledBugtracker, stepEnd), issueOptions);
		Assert.assertNull(issueBean);
	}
	
	/**
	 * Test that if the failed step disables bugtracker, but an other keeps it enabled, the issue bean is created
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssueBeanWithStepDisabled() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		
		
		IssueBean issueBean = bugtracker.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateIssueBeanWithStepDisabled", "some description", 
				Arrays.asList(step1, step2, stepFailedWithDisabledBugtracker, stepEnd), issueOptions);
		Assert.assertNotNull(issueBean);
		
		// check description only points step2 as the failed step
		Assert.assertEquals(issueBean.getDescription(), "Test: testCreateIssueBeanWithStepDisabled\n" + 
				"Description: some description\n" +
				"Error step 1 (step 2): java.lang.NullPointerException: Error clicking\n" + 
				"\n" + 
				"Steps in error\n" +
				"Step 1: step 2\n" + 
				"------------------------------------\n" + 
				"Step step 2\n" + 
				"  - action1\n" + 
				"  - action2\n" + 
				"\n" + 
				"Last logs\n" + 
				"Step Test end\n" + 
				"\n" + 
				"For more details, see attached .zip file");
	}
	
	/**
	 * If no steps are available, do not create IssueBean
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssueBeanNoStep() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		
		IssueBean issueBean = bugtracker.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "myTest", "some description", 
				new ArrayList<>(), issueOptions);
		
		Assert.assertNull(issueBean);
	}
	
	/**
	 * If no Test end steps are available, do not create IssueBean
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateIssueBeanNoEndStep() throws Exception {
		FakeBugTracker fbt = spy(new FakeBugTracker());
//		PowerMockito.whenNew(FakeBugTracker.class).withAnyArguments().thenReturn(fbt);
		BugTracker bugtracker = BugTracker.getInstance("fake", "http://foo/bar", "selenium", "user", "password", new HashMap<>());
		
		IssueBean issueBean = bugtracker.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "myTest", "some description", 
				Arrays.asList(step1), issueOptions);
		
		Assert.assertNull(issueBean);
	}
}
