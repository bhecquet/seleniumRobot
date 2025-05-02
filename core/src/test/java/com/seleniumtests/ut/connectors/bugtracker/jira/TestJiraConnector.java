package com.seleniumtests.ut.connectors.bugtracker.jira;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Page;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.collect.ImmutableList;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.Step.RootCause;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;

import io.atlassian.util.concurrent.Promise;

public class TestJiraConnector extends MockitoTest {
	
	public TestJiraConnector() throws Exception {
		// nothing to do
	}
	
	@Mock
    private JiraRestClient restClient;
	
	@Mock
	private ProjectRestClient projectRestClient;
	
	@Mock
	private Promise<Project> promiseProject;
	
	@Mock
	private Promise<Iterable<BasicProject>> promiseAllProjects;
	
	@Mock
	private Project project;
	
	@Mock
	private MetadataRestClient metadataRestClient;
	
	@Mock
	private SearchRestClient searchRestClient;
	
	@Mock
	private UserRestClient userRestClient;

	@Mock
	private IssueRestClient issueRestClient;
	
	@Mock
	private FieldSchema fieldSchema;
	
	@Mock
	private Promise<Iterable<Priority>> promisePriorities;
	
	@Mock
	private Issue issue1;
	
	@Mock
	private Issue issue2;
	
	@Mock
	private Promise<Iterable<Field>> promiseFields;
	
	@Mock
	private Promise<Iterable<User>> promiseUsers;
	
	@Mock
	private Promise<Iterable<Transition>> promiseTransitions;
	
	@Mock
	private Promise<Page<CimFieldInfo>> promiseFieldInfo;
	
	@Mock
	private Page<CimFieldInfo> fieldInfos;
	
	@Mock
	private Promise<SearchResult> promiseSearch;
	
	@Mock
	private Promise<BasicIssue> promiseBasicIssue;
	
	@Mock
	private Promise<Issue> promiseIssue;
	
	@Mock
	private Promise<Void> promiseVoid;
	
	@Mock
	private Promise<Issue> promiseIssueEmpty;
	

	private FieldSchema stringSchema = new FieldSchema("string", "", null, null, null);
	private FieldSchema optionSchema = new FieldSchema("option", "", null, null, null);
	
	private Priority priority1 = new Priority(new URI("http://foo/bar/p"), 1L, "P1", "1", "1", new URI("http://foo/bar/i"));
	private Priority priority2 = new Priority(new URI("http://foo/bar/p"), 2L, "P2", "2", "2", new URI("http://foo/bar/i"));
	
	private Field fieldApplication = new Field("13", "application", FieldType.CUSTOM, false, false, true, stringSchema);
	private Field fieldEnvironment = new Field("23", "environment", FieldType.CUSTOM, false, false, true, stringSchema);
	private Field fieldStep = new Field("33", "step", FieldType.CUSTOM, false, false, true, optionSchema);
	
	private BasicComponent component1 = new BasicComponent(new URI("http://foo/bar/c"), 1L, "comp1", "comp1");
	private BasicComponent component2 = new BasicComponent(new URI("http://foo/bar/c"), 2L, "comp2", "comp2");
	
	private IssueType issueType1 = new IssueType(new URI("http://foo/bar/i"), 1L, "Bug", false, "bug", new URI("http://foo/bar/i"));
	private IssueType issueType2 = new IssueType(new URI("http://foo/bar/i"), 2L, "Enhancement", false, "enhancement", new URI("http://foo/bar/i"));
	
	private Version version1 = new Version(new URI("http://foo/bar/v"), 1L, "v1", "v1", false, true, DateTime.now());
	private Version version2 = new Version(new URI("http://foo/bar/v"), 2L, "v2", "v2", false, true, DateTime.now());
	
	private Transition transition1 = new Transition("close", 1, new ArrayList<>());
	private Transition transition2 = new Transition("reopen", 2, new ArrayList<>());
	private Transition transition3 = new Transition("review", 3, new ArrayList<>());

	private CustomFieldOption optionStep1 = new CustomFieldOption(1L, new URI("http://foo/bar/o"), "step1", null, null);
	private CustomFieldOption optionStep2 = new CustomFieldOption(2L, new URI("http://foo/bar/o"), "step2", null, null);
	
	private CimFieldInfo fieldInfo1 = new CimFieldInfo("13", false, "application", stringSchema, new HashSet<>(), Arrays.asList(), null);
	private CimFieldInfo fieldInfo2 = new CimFieldInfo("23", false, "environment", stringSchema, new HashSet<>(), Arrays.asList(), null);
	private CimFieldInfo fieldInfo3 = new CimFieldInfo("33", false, "step", optionSchema, new HashSet<>(), Arrays.asList(optionStep1, optionStep2), null);
	
	private BasicProject project1 = new BasicProject(new URI("http://foo/bar/pr"), "PROJECT-1", 1L, "Project 1");
	private BasicProject project2 = new BasicProject(new URI("http://foo/bar/pr"), "PROJECT-2", 2L, "Project 2");
	
	private User user;
	
	private ScreenShot screenshot;
	private File detailedResult;
	
	private static final String PROJECT_KEY = "CORE";
	Map<String, String> jiraOptions = new HashMap<>();

	private TestStep step1;
	private TestStep step2;
	private TestStep stepWithErrorCauseAndDetails;
	private TestStep stepWithErrorCause;
	private TestStep stepEnd;

	private MockedConstruction mockedRestClient;

	@BeforeMethod(groups={"ut"})
	public void initJira() throws Exception {
		Map<String, URI> avatars = new HashMap<>();
		avatars.put("48x48", new URI("http://foo/bar/a"));
		user = new User(new URI("http://foo/bar/u"), "user1", "user 1", "1", "user1@company.com", true, null, avatars, "UTC");
		
		// create test steps
		File tmpImg = File.createTempFile("img", "123456.png");
		tmpImg.deleteOnExit();
		File tmpHtml = File.createTempFile("html", "123456.html");
		tmpHtml.deleteOnExit();
		
		screenshot = new ScreenShot(tmpImg, tmpHtml);
		
		step1 = new TestStep("step 1", "step 1", this.getClass(), null, new ArrayList<>(), false);
		step1.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		step1.setPosition(0);
		
		step2 = new TestStep("step 2", "step 2", this.getClass(), null, new ArrayList<>(), false);
		step2.setFailed(true);
		step2.setActionException(new NullPointerException("Error clicking"));
		step2.addAction(new TestAction("action1", false, new ArrayList<>()));
		step2.addAction(new TestAction("action2", false, new ArrayList<>()));
		step2.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		step2.setPosition(1);
		

		stepWithErrorCauseAndDetails = new TestStep("step 3", "step 3", this.getClass(), null, new ArrayList<>(), false, RootCause.REGRESSION, "Check  your script", false);
		stepWithErrorCauseAndDetails.setFailed(true);
		stepWithErrorCauseAndDetails.setActionException(new NullPointerException("Error clicking"));
		stepWithErrorCauseAndDetails.addAction(new TestAction("action1", false, new ArrayList<>()));
		stepWithErrorCauseAndDetails.addAction(new TestAction("action2", false, new ArrayList<>()));
		stepWithErrorCauseAndDetails.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		stepWithErrorCauseAndDetails.setPosition(1);
		
		stepWithErrorCause = new TestStep("step 4", "step 4", this.getClass(), null, new ArrayList<>(), false, RootCause.REGRESSION, "", false);
		stepWithErrorCause.setFailed(true);
		stepWithErrorCause.setActionException(new NullPointerException("Error clicking"));
		stepWithErrorCause.addAction(new TestAction("action1", false, new ArrayList<>()));
		stepWithErrorCause.addAction(new TestAction("action2", false, new ArrayList<>()));
		stepWithErrorCause.addSnapshot(new Snapshot(screenshot, "main", SnapshotCheckType.FULL), 1, null);
		stepWithErrorCause.setPosition(1);
		
		stepEnd = new TestStep("Test end", "Test end", this.getClass(), null, new ArrayList<>(), false);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end", SnapshotCheckType.FULL), 1, null);
		stepEnd.addSnapshot(new Snapshot(screenshot, "end2", SnapshotCheckType.FULL), 1, null);
		stepEnd.setPosition(2);
		
		// mock all clients
		mockedRestClient = mockConstruction(AsynchronousJiraRestClientFactory.class, (restClientFactory, context) -> {
			when(restClientFactory.createWithBasicHttpAuthentication(any(URI.class), eq("user"), eq("password"))).thenReturn(restClient);
		});

		when(restClient.getProjectClient()).thenReturn(projectRestClient);
		when(projectRestClient.getProject(anyString())).thenReturn(promiseProject);
		when(promiseProject.claim()).thenReturn(project);
		when(project.getComponents()).thenReturn(Arrays.asList(component1, component2));
		when(project.getIssueTypes()).thenReturn(new OptionalIterable(Arrays.asList(issueType1, issueType2)));
		when(project.getVersions()).thenReturn(Arrays.asList(version1, version2));
		when(project.getKey()).thenReturn(PROJECT_KEY);
		
		when(projectRestClient.getAllProjects()).thenReturn(promiseAllProjects);
		when(promiseAllProjects.claim()).thenReturn(Arrays.asList(project1, project2));
		
		when(restClient.getMetadataClient()).thenReturn(metadataRestClient);
		when(metadataRestClient.getPriorities()).thenReturn(promisePriorities);
		when(promisePriorities.claim()).thenReturn(Arrays.asList(priority1, priority2));
		when(metadataRestClient.getFields()).thenReturn(promiseFields);	
		when(promiseFields.claim()).thenReturn(Arrays.asList(fieldApplication, fieldEnvironment, fieldStep));
		
		when(restClient.getSearchClient()).thenReturn(searchRestClient);
		when(searchRestClient.searchJql(anyString())).thenReturn(promiseSearch);
		
		when(restClient.getUserClient()).thenReturn(userRestClient);
		doThrow(RestClientException.class).when(userRestClient).findUsers(anyString());
		doReturn(promiseUsers).when(userRestClient).findUsers("me");
		when(promiseUsers.claim()).thenReturn(Arrays.asList(user));
		
		
		when(restClient.getIssueClient()).thenReturn(issueRestClient);
		when(issueRestClient.createIssue(any(IssueInput.class))).thenReturn(promiseBasicIssue);
		when(promiseBasicIssue.claim()).thenReturn(new BasicIssue(new URI("http://foo/bar/i"), "ISSUE-1", 1L));
		when(issueRestClient.getIssue(anyString())).thenReturn(promiseIssueEmpty);
		when(issueRestClient.getIssue("ISSUE-1")).thenReturn(promiseIssue);
		when(promiseIssue.claim()).thenReturn(issue1);
		when(promiseIssueEmpty.claim()).thenThrow(RestClientException.class);
		
		when(issueRestClient.getCreateIssueMetaFields(anyString(), anyString(), any(), any())).thenReturn(promiseFieldInfo);
		when(promiseFieldInfo.claim()).thenReturn(fieldInfos);
		when(fieldInfos.getValues()).thenReturn(Arrays.asList(fieldInfo1, fieldInfo2, fieldInfo3));
		
		when(issueRestClient.getTransitions(issue1)).thenReturn(promiseTransitions);
		when(promiseTransitions.claim()).thenReturn(Arrays.asList(transition1, transition2));
		
		when(issueRestClient.addAttachments(any(), any(File.class))).thenReturn(promiseVoid);
		
		when(issue1.getKey()).thenReturn("ISSUE-1");
		when(issue1.getDescription()).thenReturn("jira issue 1");
		when(issue1.getAttachmentsUri()).thenReturn(new URI("http://foo/bar/i/1/attachments"));
		when(issue1.getCommentsUri()).thenReturn(new URI("http://foo/bar/i/1/comments"));
		when(issue2.getKey()).thenReturn("ISSUE-2");
		when(issue2.getDescription()).thenReturn("jira issue 2");
		when(issue2.getAttachmentsUri()).thenReturn(new URI("http://foo/bar/i/2/attachments"));
		when(issue2.getCommentsUri()).thenReturn(new URI("http://foo/bar/i/2/comments"));
		
		
		detailedResult = File.createTempFile("detailed", ".zip");
		detailedResult.deleteOnExit();
		
		jiraOptions.put("bugtracker.jira.openStates", "Open,To Do");
		jiraOptions.put("bugtracker.jira.closeTransition", "close");
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	private void closeMocks() {
		mockedRestClient.close();
	}

	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingCloseTransition() {
		jiraOptions.remove("bugtracker.jira.closeTransition");
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingOpenStates() {
		jiraOptions.remove("bugtracker.jira.openStates");
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"})
	public void testOpenStates() {
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		Assert.assertEquals(jiraConnector.getOpenStates().size(), 2);
		Assert.assertEquals(jiraConnector.getOpenStates().get(0), "Open");
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingServer() {
		new JiraConnector(null, PROJECT_KEY, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testEmptyServer() {
		new JiraConnector("", PROJECT_KEY, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testUnkownProject() {
		when(promiseProject.claim()).thenThrow(RestClientException.class).thenReturn(project);
		new JiraConnector("http://foo/bar", "PRO", "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingProject() {
		new JiraConnector("http://foo/bar", null, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testEmptyProject() {
		new JiraConnector("http://foo/bar", "", "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingUser() {
		new JiraConnector("http://foo/bar", PROJECT_KEY, null, "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testEmptyUser() {
		new JiraConnector("http://foo/bar", PROJECT_KEY, "", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingPassword() {
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", null, jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testEmptyPassword() {
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "", jiraOptions);	
	}
	
	
	/**
	 * The issue already exists on Jira
	 */
	@Test(groups= {"ut"})
	public void testIssueAlreadyExists() {
		
		when(promiseSearch.claim()).thenReturn(new SearchResult(0, 2, 2, Arrays.asList(issue1, issue2)));
		DateTime issueDate = DateTime.now().minusDays(1);
		when(issue1.getCreationDate()).thenReturn(issueDate);
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1", "Bug", "P1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		IssueBean newJiraBean = jiraConnector.issueAlreadyExists(jiraBean);
		
		// check issue exists and has been updated with the first found issue
		Assert.assertNotNull(newJiraBean);
		Assert.assertEquals(newJiraBean.getId(), "ISSUE-1");
		Assert.assertEquals(newJiraBean.getSummary(), jiraBean.getSummary());
		Assert.assertEquals(newJiraBean.getDescription(), "jira issue 1");
		Assert.assertEquals(newJiraBean.getDate(), issueDate.toString("yyyy-MM-dd'T'HH:mmZZ"));
	}
	
	/**
	 * Test JiraConnector only accepts JiraBean instances
	 */
	@Test(groups= {"ut"}, expectedExceptions = ClassCastException.class)
	public void testIssueAlreadyExistsIssueBean() {
		
		when(promiseSearch.claim()).thenReturn(new SearchResult(0, 2, 2, Arrays.asList(issue1, issue2)));
		IssueBean jiraBean = new IssueBean(null, "issue 1", "issue 1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.issueAlreadyExists(jiraBean);
		
	}
	
	/**
	 * The issue does not exist on jira, we return null
	 */
	@Test(groups= {"ut"})
	public void testIssueDoesNotExist() {
		
		when(promiseSearch.claim()).thenReturn(new SearchResult(0, 2, 2, Arrays.asList()));
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1", "Bug", "P1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		IssueBean newJiraBean = jiraConnector.issueAlreadyExists(jiraBean);
		
		// check issue has not been found
		Assert.assertNull(newJiraBean);
	}
	
	/**
	 * Test issue creation with all fields
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssue() throws URISyntaxException {
		ArgumentCaptor<IssueInput> issueArgument = ArgumentCaptor.forClass(IssueInput.class);
		ArgumentCaptor<File> screenshotCaptor = ArgumentCaptor.forClass(File.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		Map<String, String> fields = new HashMap<>();
		fields.put("application", "myApp"); // field allowed by jira
		fields.put("step", "step2"); // field allowed by jira
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "P1", "Bug", "myTest", null, "me", "you", Arrays.asList(screenshot), detailedResult, fields, Arrays.asList("comp1"));
		jiraConnector.createIssue(jiraBean);

		Assert.assertEquals(jiraBean.getId(), "ISSUE-1");
		
		verify(issueRestClient).createIssue(issueArgument.capture());
		
		// check issue has all data defined in jiraBean
		IssueInput issueInput = ((IssueInput)issueArgument.getValue());
		Assert.assertEquals(issueInput.getField("summary").getValue(), "issue 1");
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("issuetype").getValue())).getValuesMap().get("id"), "1");
		Assert.assertEquals(ImmutableList.copyOf(((Iterable<ComplexIssueInputFieldValue>)(issueInput.getField("components").getValue()))).size(), 1);
		Assert.assertEquals(ImmutableList.copyOf(((Iterable<ComplexIssueInputFieldValue>)(issueInput.getField("components").getValue()))).get(0).getValuesMap().get("name"), "comp1");
//		Assert.assertEquals(issueInput.getField("duedate").getValue().toString().split("-")[0], Integer.toString(LocalDateTime.now().getYear()));
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("project").getValue())).getValuesMap().get("key"), PROJECT_KEY);
		Assert.assertEquals(issueInput.getField("description").getValue(), "issue 1 descr");
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("reporter").getValue())).getValuesMap().get("name"), "you");
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("assignee").getValue())).getValuesMap().get("name"), "user1");
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("priority").getValue())).getValuesMap().get("id"), "1");
		Assert.assertEquals(issueInput.getField("13").getValue(), "myApp"); // custom field
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("33").getValue())).getValuesMap().get("value"), "step2"); // custom field

		// check attachments have been added (screenshot + detailed results)
		verify(issueRestClient, times(2)).addAttachments(eq(new URI("http://foo/bar/i/1/attachments")), screenshotCaptor.capture());
		Assert.assertTrue(((File)screenshotCaptor.getAllValues().get(0)).getName().endsWith(".png"));
		Assert.assertTrue(((File)screenshotCaptor.getAllValues().get(1)).getName().endsWith(".zip"));
	}
	
	/**
	 * Test issue creation without fields, components, screenshots, ...
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssueWithMinimalFields() throws URISyntaxException {
		ArgumentCaptor<IssueInput> issueArgument = ArgumentCaptor.forClass(IssueInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "P1", "Bug", null, null, null, null, new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
		jiraConnector.createIssue(jiraBean);		
		
		verify(issueRestClient).createIssue(issueArgument.capture());
		
		// check issue has only mandatory fields
		IssueInput issueInput = ((IssueInput)issueArgument.getValue());
		Assert.assertEquals(issueInput.getField("summary").getValue(), "issue 1");
		Assert.assertEquals(issueInput.getField("description").getValue(), "issue 1 descr");
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("issuetype").getValue())).getValuesMap().get("id"), "1");
		Assert.assertEquals(ImmutableList.copyOf(((Iterable<ComplexIssueInputFieldValue>)(issueInput.getField("components").getValue()))).size(), 0);
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("project").getValue())).getValuesMap().get("key"), PROJECT_KEY);
		
		Assert.assertNull(issueInput.getField("reporter"));
		Assert.assertNull(issueInput.getField("assignee"));
		
		// check attachments have been added (screenshot + detailed results)
		verify(issueRestClient, never()).addAttachments(eq(new URI("http://foo/bar/i/1/attachments")), any(File.class));
	}
	
	/**
	 * Test the case where the attachment file (file of snapshot is not present)
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssueMissingScreenshotFile() throws URISyntaxException {

		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		// simulate screenshot file not present
		screenshot.getImage().getFile().delete();
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "P1", "Bug", null, null, null, null, Arrays.asList(screenshot), null, new HashMap<>(), new ArrayList<>());
		jiraConnector.createIssue(jiraBean);		
		
		// check attachments (screenshots) have not been added  because screenshot file was not available
		verify(issueRestClient, never()).addAttachments(eq(new URI("http://foo/bar/i/1/attachments")), any(File.class));
	}
	
	/**
	 * Test the case where the detailed result file (.zip) is not present
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssueMissingDetailedResultFile() throws URISyntaxException {

		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		// simulate screenshot file not present
		detailedResult.delete();
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "P1", "Bug", null, null, null, null, Arrays.asList(), detailedResult, new HashMap<>(), new ArrayList<>());
		
		
		jiraConnector.createIssue(jiraBean);		
		
		// check attachments (screenshots) have not been added  because screenshot file was not available
		verify(issueRestClient, never()).addAttachments(eq(new URI("http://foo/bar/i/1/attachments")), any(File.class));
	}
	
	/**
	 * Test issue creation without fields, components, screenshots, ...
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssueWithMinimalFieldsOtherEmpty() throws URISyntaxException {
		ArgumentCaptor<IssueInput> issueArgument = ArgumentCaptor.forClass(IssueInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "", "Bug", "", null, "", "", new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
		jiraConnector.createIssue(jiraBean);
		
		
		verify(issueRestClient).createIssue(issueArgument.capture());
		
		// check issue has only mandatory fields
		IssueInput issueInput = ((IssueInput)issueArgument.getValue());
		Assert.assertEquals(issueInput.getField("summary").getValue(), "issue 1");
		Assert.assertEquals(issueInput.getField("description").getValue(), "issue 1 descr");
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("issuetype").getValue())).getValuesMap().get("id"), "1");
		Assert.assertEquals(ImmutableList.copyOf(((Iterable<ComplexIssueInputFieldValue>)(issueInput.getField("components").getValue()))).size(), 0);
		Assert.assertEquals(((ComplexIssueInputFieldValue)(issueInput.getField("project").getValue())).getValuesMap().get("key"), PROJECT_KEY);
		
		Assert.assertNull(issueInput.getField("reporter"));
		Assert.assertNull(issueInput.getField("assignee"));
		Assert.assertNull(issueInput.getField("priority"));
		
		// check attachments have been added (screenshot + detailed results)
		verify(issueRestClient, never()).addAttachments(eq(new URI("http://foo/bar/i/1/attachments")), any(File.class));
	}
	
	/**
	 * Test issue creation with bad component name. No error raised
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssueWithWrongComponent() throws URISyntaxException {
		ArgumentCaptor<IssueInput> issueArgument = ArgumentCaptor.forClass(IssueInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "", "Bug", "", null, "", "", new ArrayList<>(), null, new HashMap<>(), Arrays.asList("unknown"));
		jiraConnector.createIssue(jiraBean);
		
		
		verify(issueRestClient).createIssue(issueArgument.capture());
		
		// check unknown component has not been added, but no error
		IssueInput issueInput = ((IssueInput)issueArgument.getValue());
		Assert.assertEquals(ImmutableList.copyOf(((Iterable<ComplexIssueInputFieldValue>)(issueInput.getField("components").getValue()))).size(), 0);
		
	}
	
	/**
	 * Test issue creation with wrong user
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testCreateIssueWrongUser() {
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", null, "Bug", null, null, "notme", null, new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
		jiraConnector.createIssue(jiraBean);
	
	}
	
	/**
	 * Test issue creation with wrong issue type, unknown from jira
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testCreateIssueWrongIssueType() {
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", null, "BigBug", null, null, "", null, new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
		jiraConnector.createIssue(jiraBean);
		
	}
	
	/**
	 * Test issue creation without issue type
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testCreateIssueNoIssueType() {
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", null, null, null, null, "", null, new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
		jiraConnector.createIssue(jiraBean);
		
	}

	/**
	 * Test issue creation with wrong priority
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testCreateIssueWrongPriority() {
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "P10", "Bug", null, null, null, null, new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
		jiraConnector.createIssue(jiraBean);
	
	}
	
	/**
	 * Test issue creation with wrong field. No error should be raised (but issue may not be created if field is mandatory)
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssueUnknownField() {
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		Map<String, String> fields = new HashMap<>();
		fields.put("myfield", "myApp"); // field not allowed by jira
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "P1", "Bug", null, null, null, null, new ArrayList<>(), null, fields, new ArrayList<>());
		jiraConnector.createIssue(jiraBean);
		
	}
	/**
	 * Test issue creation with field value. No error should be raised (but issue may not be created if field is mandatory)
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"})
	public void testCreateIssueUnknownFieldValue() {
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		Map<String, String> fields = new HashMap<>();
		fields.put("step", "step3"); // field allowed by jira
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", "P1", "Bug", null, null, null, null, new ArrayList<>(), null, fields, new ArrayList<>());
		jiraConnector.createIssue(jiraBean);
		
	}
	
	@Test(groups= {"ut"})
	public void testCloseIssue() {
		ArgumentCaptor<TransitionInput> transitionArgument = ArgumentCaptor.forClass(TransitionInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.closeIssue("ISSUE-1", "closed");
		
		verify(issueRestClient).transition(eq(issue1), transitionArgument.capture());
		Assert.assertEquals(transitionArgument.getValue().getId(), 1);
	}
	
	@Test(groups= {"ut"})
	public void testCloseIssueMultipleTransitions() {

		when(promiseTransitions.claim()).thenReturn(Arrays.asList(transition3)).thenReturn(Arrays.asList(transition1, transition2));
		jiraOptions.put("bugtracker.jira.closeTransition", "review/close");
		
		ArgumentCaptor<TransitionInput> transitionArgument = ArgumentCaptor.forClass(TransitionInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.closeIssue("ISSUE-1", "closed");
		
		verify(issueRestClient, times(2)).transition(eq(issue1), transitionArgument.capture());
		Assert.assertEquals(transitionArgument.getAllValues().get(0).getId(), 3); // transition to review state
		Assert.assertEquals(transitionArgument.getAllValues().get(1).getId(), 1); // transition to closed state
	}
	
	/**
	 * issue #470: When closing issue with multiple transition, check the case where jira do not update the transitions of the issue immediately
	 * We need to retry
	 */
	@Test(groups= {"ut"})
	public void testCloseIssueMultipleTransitionsSlow() {
		
		when(promiseTransitions.claim()).thenReturn(Arrays.asList(transition3)) // initial state
										.thenReturn(Arrays.asList(transition3)) // state not updated
										.thenReturn(Arrays.asList(transition1, transition2)); // state updated
		jiraOptions.put("bugtracker.jira.closeTransition", "review/close");
		
		ArgumentCaptor<TransitionInput> transitionArgument = ArgumentCaptor.forClass(TransitionInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.closeIssue("ISSUE-1", "closed");
		
		verify(issueRestClient, times(2)).transition(eq(issue1), transitionArgument.capture());
		Assert.assertEquals(transitionArgument.getAllValues().get(0).getId(), 3); // transition to review state
		Assert.assertEquals(transitionArgument.getAllValues().get(1).getId(), 1); // transition to closed state
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "'bugtracker.jira.closeTransition' values \\[closed\\] are unknown for this issue, allowed transitions are \\[reopen, close\\]")
	public void testCloseIssueInvalidTransition() {

		jiraOptions.put("bugtracker.jira.closeTransition", "closed");
		ArgumentCaptor<TransitionInput> transitionArgument = ArgumentCaptor.forClass(TransitionInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.closeIssue("ISSUE-1", "closed");
		
		verify(issueRestClient).transition(eq(issue1), transitionArgument.capture());
		Assert.assertEquals(transitionArgument.getValue().getId(), 1);
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "'bugtracker.jira.closeTransition': value \\[closeQuickly\\] is invalid for this issue in its current state, allowed transitions are \\[reopen, close\\]")
	public void testCloseIssueInvalidTransitionInSecondPosition() {
		when(promiseTransitions.claim()).thenReturn(Arrays.asList(transition3)).thenReturn(Arrays.asList(transition1, transition2));
		jiraOptions.put("bugtracker.jira.closeTransition", "review/closeQuickly");
		
		ArgumentCaptor<TransitionInput> transitionArgument = ArgumentCaptor.forClass(TransitionInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.closeIssue("ISSUE-1", "closed");
		
		verify(issueRestClient).transition(eq(issue1), transitionArgument.capture());
		Assert.assertEquals(transitionArgument.getValue().getId(), 1);
	}
	
	/**
	 * Error raised when not 
	 * @throws URISyntaxException
	 */
	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class)
	public void testCloseUnknownIssue() {
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.closeIssue("ISSUE-2", "closed");
	}
	

	@Test(groups= {"ut"})
	public void testUpdateIssue() throws URISyntaxException {
		ArgumentCaptor<Comment> commentArgument = ArgumentCaptor.forClass(Comment.class);
		ArgumentCaptor<File> screenshotCaptor = ArgumentCaptor.forClass(File.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.updateIssue("ISSUE-1", "update", Arrays.asList(screenshot));
		
		verify(issueRestClient).addComment(any(URI.class), commentArgument.capture());
		Assert.assertEquals(commentArgument.getValue().getBody(), "update\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n");
		
		// check attachments have been added (screenshot)
		verify(issueRestClient).addAttachments(eq(new URI("http://foo/bar/i/1/attachments")), screenshotCaptor.capture());
		Assert.assertTrue(((File)screenshotCaptor.getAllValues().get(0)).getName().endsWith(".png"));
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class)
	public void testUpdateUnknownIssue() throws URISyntaxException {
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.updateIssue("ISSUE-2", "update", Arrays.asList(screenshot));
	
	}
	

	/**
	 * Create a new jira bean with all parameters
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateJiraBean() throws Exception {
		
		jiraOptions.put("bugtracker.priority", "P1");
		jiraOptions.put("bugtracker.assignee", "me");
		jiraOptions.put("bugtracker.reporter", "you");
		jiraOptions.put("bugtracker.jira.issueType", "Bug");
		jiraOptions.put("bugtracker.jira.components", "comp1,comp2");
		jiraOptions.put("bugtracker.jira.field.foo", "bar");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
	
		IssueBean issueBean = jiraConnector.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateJiraBean", "some description", 
				Arrays.asList(step1, step2, stepEnd), jiraOptions);
		
		Assert.assertTrue(issueBean instanceof JiraBean);
		JiraBean jiraBean = (JiraBean)issueBean;
		
		Assert.assertEquals(jiraBean.getAssignee(), "me");
		
		// check only step2 is seen as a failed step
		Assert.assertEquals(jiraBean.getDescription(), "*Test:* testCreateJiraBean\n" + 
				"*Description:* some description\n" +
				"*Error step #1 (step 2):* *{color:#de350b}java.lang.NullPointerException: Error clicking{color}*\n" +
				"h2. Steps in error\n" + 
				"* *Step 1: step 2*\n" +
				"{code:java}Step step 2\n" +
				"  - action1\n" +
				"  - action2{code}\n" +
				"\n" + 
				"h2. Last logs\n" + 
				"{code:java}Step Test end{code}\n" + 
				"\n" + 
				"h2. Associated screenshots\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n" + 
				"\n" + 
				"\n" + 
				"For more details, see attached .zip file");
		Assert.assertEquals(jiraBean.getSummary(), "[Selenium][selenium][DEV][ngName] test myTest KO");
		Assert.assertEquals(jiraBean.getReporter(), "you");
		Assert.assertEquals(jiraBean.getTestName(), "testCreateJiraBean");
		Assert.assertEquals(jiraBean.getScreenShots(), Arrays.asList(screenshot, screenshot)); // screenshots from the last step
		Assert.assertEquals(jiraBean.getTestStep(), step2); 
		Assert.assertEquals(jiraBean.getDateTime().getDayOfMonth(),  ZonedDateTime.now().plusHours(3).getDayOfMonth()); 
		Assert.assertEquals(jiraBean.getComponents(), Arrays.asList("comp1", "comp2")); 
		Assert.assertEquals(jiraBean.getIssueType(), "Bug"); 
		Assert.assertEquals(jiraBean.getPriority(), "P1"); 
		Assert.assertEquals(jiraBean.getCustomFields().get("foo"), "bar"); 
		Assert.assertTrue(jiraBean.getDetailedResult().isFile());
		Assert.assertTrue(jiraBean.getDetailedResult().length() > 1000);
		Assert.assertNull(jiraBean.getId()); // not initialized by default
	}
	
	/**
	 * Create a new jira bean with all parameters
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCreateJiraBeanWithOrigin() throws Exception {
		
		jiraOptions.put("bugtracker.priority", "P1");
		jiraOptions.put("bugtracker.assignee", "me");
		jiraOptions.put("bugtracker.reporter", "you");
		jiraOptions.put("bugtracker.jira.issueType", "Bug");
		jiraOptions.put("bugtracker.jira.components", "comp1,comp2");
		jiraOptions.put("bugtracker.jira.field.foo", "bar");
		SeleniumTestsContextManager.getThreadContext().setStartedBy("http://foo/bar/job/1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		IssueBean issueBean = jiraConnector.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateJiraBean", "some description", 
				Arrays.asList(step1, step2, stepEnd), jiraOptions);
		
		Assert.assertTrue(issueBean instanceof JiraBean);
		JiraBean jiraBean = (JiraBean)issueBean;
		
		Assert.assertEquals(jiraBean.getAssignee(), "me");
		
		// check only step2 is seen as a failed step
		// detailed result is not provided because 'startedBy' is set
		Assert.assertEquals(jiraBean.getDescription(), "*Test:* testCreateJiraBean\n" + 
				"*Description:* some description\n" +
				"*Started by:* http://foo/bar/job/1\n" +
				"*Error step #1 (step 2):* *{color:#de350b}java.lang.NullPointerException: Error clicking{color}*\n" +
				"h2. Steps in error\n" + 
				"* *Step 1: step 2*\n" +
				"{code:java}Step step 2\n" +
				"  - action1\n" +
				"  - action2{code}\n" +
				"\n" + 
				"h2. Last logs\n" + 
				"{code:java}Step Test end{code}\n" + 
				"\n" + 
				"h2. Associated screenshots\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n"
				);
		Assert.assertEquals(jiraBean.getSummary(), "[Selenium][selenium][DEV][ngName] test myTest KO");
		Assert.assertEquals(jiraBean.getReporter(), "you");
		Assert.assertEquals(jiraBean.getTestName(), "testCreateJiraBean");
		Assert.assertEquals(jiraBean.getScreenShots(), Arrays.asList(screenshot, screenshot)); // screenshots from the last step
		Assert.assertEquals(jiraBean.getTestStep(), step2); 
		Assert.assertEquals(jiraBean.getDateTime().getDayOfMonth(),  ZonedDateTime.now().plusHours(3).getDayOfMonth()); 
		Assert.assertEquals(jiraBean.getComponents(), Arrays.asList("comp1", "comp2")); 
		Assert.assertEquals(jiraBean.getIssueType(), "Bug"); 
		Assert.assertEquals(jiraBean.getPriority(), "P1"); 
		Assert.assertEquals(jiraBean.getCustomFields().get("foo"), "bar"); 
		Assert.assertNull(jiraBean.getDetailedResult()); // detailed result is not provided because 'startedBy' is set
		Assert.assertNull(jiraBean.getId()); // not inistialized by default
	}
	
	@Test(groups={"ut"})
	public void testCreateJiraBeanWithErrorCauseAndDetails() throws Exception {
		
		jiraOptions.put("bugtracker.priority", "P1");
		jiraOptions.put("bugtracker.assignee", "me");
		jiraOptions.put("bugtracker.reporter", "you");
		jiraOptions.put("bugtracker.jira.issueType", "Bug");
		jiraOptions.put("bugtracker.jira.components", "comp1,comp2");
		jiraOptions.put("bugtracker.jira.field.foo", "bar");
		SeleniumTestsContextManager.getThreadContext().setStartedBy("http://foo/bar/job/1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		IssueBean issueBean = jiraConnector.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateJiraBean", "some description", 
				Arrays.asList(step1, stepWithErrorCauseAndDetails, stepEnd), jiraOptions);
		
		Assert.assertTrue(issueBean instanceof JiraBean);
		JiraBean jiraBean = (JiraBean)issueBean;

		// check only step2 is seen as a failed step
		// detailed result is not provided because 'startedBy' is set
		Assert.assertEquals(jiraBean.getDescription(), "*Test:* testCreateJiraBean\n" + 
				"*Description:* some description\n" +
				"*Started by:* http://foo/bar/job/1\n" +
				"*Error step #1 (step 3):* *{color:#de350b}java.lang.NullPointerException: Error clicking{color}*\n" +
				"h2. Steps in error\n" +
				"* *Step 1: step 3*\n" +
				"+Possible cause:+ REGRESSION => Check  your script\n" +
				"{code:java}Step step 3\n" +
				"  - action1\n" +
				"  - action2{code}\n" +
				"\n" + 
				"h2. Last logs\n" + 
				"{code:java}Step Test end{code}\n" + 
				"\n" + 
				"h2. Associated screenshots\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n"
				);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateJiraBeanWithErrorCauseNoDetails() throws Exception {
		
		jiraOptions.put("bugtracker.priority", "P1");
		jiraOptions.put("bugtracker.assignee", "me");
		jiraOptions.put("bugtracker.reporter", "you");
		jiraOptions.put("bugtracker.jira.issueType", "Bug");
		jiraOptions.put("bugtracker.jira.components", "comp1,comp2");
		jiraOptions.put("bugtracker.jira.field.foo", "bar");
		SeleniumTestsContextManager.getThreadContext().setStartedBy("http://foo/bar/job/1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		IssueBean issueBean = jiraConnector.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateJiraBean", "some description", 
				Arrays.asList(step1, stepWithErrorCause, stepEnd), jiraOptions);
		
		Assert.assertTrue(issueBean instanceof JiraBean);
		JiraBean jiraBean = (JiraBean)issueBean;
		
		// check only step2 is seen as a failed step
		// detailed result is not provided because 'startedBy' is set
		Assert.assertEquals(jiraBean.getDescription(), "*Test:* testCreateJiraBean\n" + 
				"*Description:* some description\n" +
				"*Started by:* http://foo/bar/job/1\n" +
				"*Error step #1 (step 4):* *{color:#de350b}java.lang.NullPointerException: Error clicking{color}*\n" +
				"h2. Steps in error\n" +
				"* *Step 1: step 4*\n" +
				"+Possible cause:+ REGRESSION\n" +
				"{code:java}Step step 4\n" +
				"  - action1\n" +
				"  - action2{code}\n" +
				"\n" + 
				"h2. Last logs\n" + 
				"{code:java}Step Test end{code}\n" + 
				"\n" + 
				"h2. Associated screenshots\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n" + 
				"!N-A_0-2_Test_end--123456.png|thumbnail!\n"
				);
		
	}
	
	@Test(groups={"ut"})
	public void testCreateJiraBeanNoFailedSteps() throws Exception {
		
		jiraOptions.put("bugtracker.priority", "P1");
		jiraOptions.put("bugtracker.assignee", "me");
		jiraOptions.put("bugtracker.reporter", "you");
		jiraOptions.put("bugtracker.jira.issueType", "Bug");
		jiraOptions.put("bugtracker.jira.components", "comp1,comp2");
		jiraOptions.put("bugtracker.jira.field.foo", "bar");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
	
		IssueBean issueBean = jiraConnector.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateJiraBean", "some description", 
				Arrays.asList(step1, stepEnd), jiraOptions);
		
		// that when no failed step exist, (this should not happen), this is not a problem for connector
		Assert.assertNull(issueBean);

	}
	
	@Test(groups={"ut"})
	public void testCreateJiraBeanNoFinalStep() throws Exception {
		
		jiraOptions.put("bugtracker.priority", "P1");
		jiraOptions.put("bugtracker.assignee", "me");
		jiraOptions.put("bugtracker.reporter", "you");
		jiraOptions.put("bugtracker.jira.issueType", "Bug");
		jiraOptions.put("bugtracker.jira.components", "comp1,comp2");
		jiraOptions.put("bugtracker.jira.field.foo", "bar");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		
		IssueBean issueBean = jiraConnector.createIssueBean("[Selenium][selenium][DEV][ngName] test myTest KO", "testCreateJiraBean", "some description", 
				Arrays.asList(step1), jiraOptions);
		
		// issue bean is not created when "Test end" is not present
		Assert.assertNull(issueBean);
		
	}
}
