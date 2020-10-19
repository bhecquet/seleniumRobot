package com.seleniumtests.ut.connectors.bugtracker.jira;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
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
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
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
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.screenshots.ScreenShot;

import io.atlassian.util.concurrent.Promise;

@PrepareForTest({JiraConnector.class})
public class TestJiraConnector extends MockitoTest {
	
	public TestJiraConnector() throws Exception {
		// nothing to do
	}

	@Mock
	private AsynchronousJiraRestClientFactory restClientFactory;
	
	@Mock
    private JiraRestClient restClient;
	
	@Mock
	private ProjectRestClient projectRestClient;
	
	@Mock
	private Promise<Project> promiseProject;
	
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
	private Promise<SearchResult> promiseSearch;
	
	@Mock
	private Promise<BasicIssue> promiseBasicIssue;
	
	@Mock
	private Promise<Issue> promiseIssue;
	
	@Mock
	private Promise<Issue> promiseIssueEmpty;
	
	private Priority priority1 = new Priority(new URI("http://foo/bar/p"), 1L, "P1", "1", "1", new URI("http://foo/bar/i"));
	private Priority priority2 = new Priority(new URI("http://foo/bar/p"), 2L, "P2", "2", "2", new URI("http://foo/bar/i"));
	
	private Field fieldApplication = new Field("13", "application", FieldType.CUSTOM, false, false, true, fieldSchema);
	private Field fieldEnvironment = new Field("23", "environment", FieldType.CUSTOM, false, false, true, fieldSchema);
	
	private BasicComponent component1 = new BasicComponent(new URI("http://foo/bar/c"), 1L, "comp1", "comp1");
	private BasicComponent component2 = new BasicComponent(new URI("http://foo/bar/c"), 2L, "comp2", "comp2");
	
	private IssueType issueType1 = new IssueType(new URI("http://foo/bar/i"), 1L, "Bug", false, "bug", new URI("http://foo/bar/i"));
	private IssueType issueType2 = new IssueType(new URI("http://foo/bar/i"), 2L, "Enhancement", false, "enhancement", new URI("http://foo/bar/i"));
	
	private Version version1 = new Version(new URI("http://foo/bar/v"), 1L, "v1", "v1", false, true, DateTime.now());
	private Version version2 = new Version(new URI("http://foo/bar/v"), 2L, "v2", "v2", false, true, DateTime.now());
	
	private Transition transition1 = new Transition("close", 1, new ArrayList<>());
	private Transition transition2 = new Transition("reopen", 2, new ArrayList<>());
	private Transition transition3 = new Transition("review", 3, new ArrayList<>());
	
	private User user;
	
	private ScreenShot screenshot;
	private File detailedResult;
	
	private static final String PROJECT_KEY = "CORE";
	Map<String, String> jiraOptions = new HashMap<>();

	@BeforeMethod(groups={"ut"})
	public void initJira() throws Exception {
		Map<String, URI> avatars = new HashMap<>();
		avatars.put("48x48", new URI("http://foo/bar/a"));
		user = new User(new URI("http://foo/bar/u"), "user1", "user 1", "1", "user1@company.com", true, null, avatars, "UTC");
		
		PowerMockito.whenNew(AsynchronousJiraRestClientFactory.class).withNoArguments().thenReturn(restClientFactory);
		when(restClientFactory.createWithBasicHttpAuthentication(any(URI.class), eq("user"), eq("password"))).thenReturn(restClient);
		
		when(restClient.getProjectClient()).thenReturn(projectRestClient);
		when(projectRestClient.getProject(anyString())).thenReturn(promiseProject);
		when(promiseProject.claim()).thenReturn(project);
		when(project.getComponents()).thenReturn(Arrays.asList(component1, component2));
		when(project.getIssueTypes()).thenReturn(new OptionalIterable(Arrays.asList(issueType1, issueType2)));
		when(project.getVersions()).thenReturn(Arrays.asList(version1, version2));
		when(project.getKey()).thenReturn(PROJECT_KEY);
		
		when(restClient.getMetadataClient()).thenReturn(metadataRestClient);
		when(metadataRestClient.getPriorities()).thenReturn(promisePriorities);
		when(promisePriorities.claim()).thenReturn(Arrays.asList(priority1, priority2));
		when(metadataRestClient.getFields()).thenReturn(promiseFields);	
		when(promiseFields.claim()).thenReturn(Arrays.asList(fieldApplication, fieldEnvironment));
		
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
		when(issueRestClient.getIssue(eq("ISSUE-1"))).thenReturn(promiseIssue);
		when(promiseIssue.claim()).thenReturn(issue1);
		when(promiseIssueEmpty.claim()).thenThrow(RestClientException.class);
		
		when(issueRestClient.getTransitions(issue1)).thenReturn(promiseTransitions);
		when(promiseTransitions.claim()).thenReturn(Arrays.asList(transition1, transition2));
		
		when(issue1.getKey()).thenReturn("ISSUE-1");
		when(issue1.getDescription()).thenReturn("jira issue 1");
		when(issue1.getAttachmentsUri()).thenReturn(new URI("http://foo/bar/i/1/attachments"));
		when(issue1.getCommentsUri()).thenReturn(new URI("http://foo/bar/i/1/comments"));
		when(issue2.getKey()).thenReturn("ISSUE-2");
		when(issue2.getDescription()).thenReturn("jira issue 2");
		when(issue2.getAttachmentsUri()).thenReturn(new URI("http://foo/bar/i/2/attachments"));
		when(issue2.getCommentsUri()).thenReturn(new URI("http://foo/bar/i/2/comments"));
		
		
		File tmpImg = File.createTempFile("img", ".png");
		tmpImg.deleteOnExit();
		screenshot = new ScreenShot();
		screenshot.setImagePath("screenshot/" + tmpImg.getName());
		detailedResult = File.createTempFile("detailed", ".zip");
		detailedResult.deleteOnExit();
		
		jiraOptions.put("jira.openStates", "Open,To Do");
		jiraOptions.put("jira.closeTransition", "close");
		jiraOptions.put("priority", "P1");
		jiraOptions.put("jira.issueType", "Bug");
		jiraOptions.put("jira.components", "comp1,comp2");
	}
	
	/**
	 * Test JiraConnector only accepts JiraBean instances
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingIssueType() {
		jiraOptions.remove("jira.issueType");
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingCloseTransition() {
		jiraOptions.remove("jira.closeTransition");
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testMissingOpenStates() {
		jiraOptions.remove("jira.openStates");
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);	
	}
	@Test(groups= {"ut"})
	public void testOpenStates() {
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		Assert.assertEquals(jiraConnector.getOpenStates().size(), 2);
		Assert.assertEquals(jiraConnector.getOpenStates().get(0), "Open");
	}
	/**
	 * No error when components is not set
	 */
	@Test(groups= {"ut"})
	public void testMissingComponents() {
		jiraOptions.remove("jira.components");
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		Assert.assertTrue(jiraConnector.getComponentsToSet().isEmpty());
	}
	/**
	 * No error when priority is not set
	 */
	@Test(groups= {"ut"})
	public void testMissingPriority() {
		jiraOptions.remove("priority");
		new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);	
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
		when(promiseProject.claim()).thenThrow(RestClientException.class);
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
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1", "Bug");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		IssueBean newJiraBean = jiraConnector.issueAlreadyExists(jiraBean);
		
		// check issue exists and has been updated with the first found issue
		Assert.assertNotNull(newJiraBean);
		Assert.assertEquals(newJiraBean.getId(), "ISSUE-1");
		Assert.assertEquals(newJiraBean.getSummary(), jiraBean.getSummary());
		Assert.assertEquals(newJiraBean.getDescription(), "jira issue 1");
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
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1", "Bug");
		
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
		
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", null, "Bug", null, null, null, null, new ArrayList<>(), null, new HashMap<>(), new ArrayList<>());
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
		fields.put("myfield", "myApp"); // field allowed by jira
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1 descr", null, "Bug", null, null, null, null, new ArrayList<>(), null, fields, new ArrayList<>());
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
		jiraOptions.put("jira.closeTransition", "review/close");
		
		ArgumentCaptor<TransitionInput> transitionArgument = ArgumentCaptor.forClass(TransitionInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.closeIssue("ISSUE-1", "closed");
		
		verify(issueRestClient, times(2)).transition(eq(issue1), transitionArgument.capture());
		Assert.assertEquals(transitionArgument.getAllValues().get(0).getId(), 3); // transition to review state
		Assert.assertEquals(transitionArgument.getAllValues().get(1).getId(), 1); // transition to closed state
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class)
	public void testCloseIssueInvalidTransition() {

		jiraOptions.put("jira.closeTransition", "closed");
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
		Assert.assertEquals(commentArgument.getValue().getBody(), "update");
		
		// check attachments have been added (screenshot)
		verify(issueRestClient).addAttachments(eq(new URI("http://foo/bar/i/1/attachments")), screenshotCaptor.capture());
		Assert.assertTrue(((File)screenshotCaptor.getAllValues().get(0)).getName().endsWith(".png"));
	}
	
	@Test(groups= {"ut"}, expectedExceptions = ScenarioException.class)
	public void testUpdateUnknownIssue() throws URISyntaxException {
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", PROJECT_KEY, "user", "password", jiraOptions);
		jiraConnector.updateIssue("ISSUE-2", "update", Arrays.asList(screenshot));
	
	}
}
