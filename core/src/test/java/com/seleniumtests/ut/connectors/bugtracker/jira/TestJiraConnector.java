package com.seleniumtests.ut.connectors.bugtracker.jira;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
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
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.bugtracker.IssueBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraBean;
import com.seleniumtests.connectors.bugtracker.jira.JiraConnector;
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
	private Promise<SearchResult> promiseSearch;
	
	@Mock
	private Promise<BasicIssue> promiseBasicIssue;
	
	@Mock
	private Promise<Issue> promiseIssue;
	
	private Priority priority1 = new Priority(new URI("http://foo/bar/p"), 1L, "P1", "1", "1", new URI("http://foo/bar/i"));
	private Priority priority2 = new Priority(new URI("http://foo/bar/p"), 2L, "P2", "2", "2", new URI("http://foo/bar/i"));
	
	private Field fieldApplication = new Field("1", "application", FieldType.CUSTOM, false, false, true, fieldSchema);
	private Field fieldEnvironment = new Field("2", "environment", FieldType.CUSTOM, false, false, true, fieldSchema);
	
	private BasicComponent component1 = new BasicComponent(new URI("http://foo/bar/c"), 1L, "comp1", "comp1");
	private BasicComponent component2 = new BasicComponent(new URI("http://foo/bar/c"), 2L, "comp2", "comp2");
	
	private IssueType issueType1 = new IssueType(new URI("http://foo/bar/i"), 1L, "Bug", false, "bug", new URI("http://foo/bar/i"));
	private IssueType issueType2 = new IssueType(new URI("http://foo/bar/i"), 2L, "Enhancement", false, "enhancement", new URI("http://foo/bar/i"));
	
	private Version version1 = new Version(new URI("http://foo/bar/v"), 1L, "v1", "v1", false, true, DateTime.now());
	private Version version2 = new Version(new URI("http://foo/bar/v"), 2L, "v2", "v2", false, true, DateTime.now());
	
	private ScreenShot screenshot;
	private File detailedResult;

	@BeforeMethod(groups={"ut"})
	public void initJira() throws Exception {
		PowerMockito.whenNew(AsynchronousJiraRestClientFactory.class).withNoArguments().thenReturn(restClientFactory);
		when(restClientFactory.createWithBasicHttpAuthentication(any(URI.class), eq("user"), eq("password"))).thenReturn(restClient);
		
		when(restClient.getProjectClient()).thenReturn(projectRestClient);
		when(projectRestClient.getProject(anyString())).thenReturn(promiseProject);
		when(promiseProject.claim()).thenReturn(project);
		when(project.getComponents()).thenReturn(Arrays.asList(component1, component2));
		when(project.getIssueTypes()).thenReturn(new OptionalIterable(Arrays.asList(issueType1, issueType2)));
		when(project.getVersions()).thenReturn(Arrays.asList(version1, version2));
		
		when(restClient.getMetadataClient()).thenReturn(metadataRestClient);
		when(metadataRestClient.getPriorities()).thenReturn(promisePriorities);
		when(promisePriorities.claim()).thenReturn(Arrays.asList(priority1, priority2));
		when(metadataRestClient.getFields()).thenReturn(promiseFields);	
		when(promiseFields.claim()).thenReturn(Arrays.asList(fieldApplication, fieldEnvironment));
		
		when(restClient.getSearchClient()).thenReturn(searchRestClient);
		when(searchRestClient.searchJql(anyString())).thenReturn(promiseSearch);
		
		when(restClient.getUserClient()).thenReturn(userRestClient);
		
		when(restClient.getIssueClient()).thenReturn(issueRestClient);
		when(issueRestClient.createIssue(any(IssueInput.class))).thenReturn(promiseBasicIssue);
		when(promiseBasicIssue.claim()).thenReturn(new BasicIssue(new URI("http://foo/bar/i"), "ISSUE-1", 1L));
		when(issueRestClient.getIssue(eq("ISSUE-1"))).thenReturn(promiseIssue);
		when(promiseIssue.claim()).thenReturn(issue1);
		
		when(issue1.getKey()).thenReturn("ISSUE-1");
		when(issue1.getDescription()).thenReturn("jira issue 1");
		when(issue1.getAttachmentsUri()).thenReturn(new URI("http://foo/bar/i/1/attachments"));
		when(issue2.getKey()).thenReturn("ISSUE-2");
		when(issue2.getDescription()).thenReturn("jira issue 2");
		when(issue2.getAttachmentsUri()).thenReturn(new URI("http://foo/bar/i/2/attachments"));
		
		
		File tmpImg = File.createTempFile("img", ".png");
		tmpImg.deleteOnExit();
		screenshot = new ScreenShot();
		screenshot.setImagePath("screenshot/" + tmpImg.getName());
		detailedResult = File.createTempFile("detailed", ".zip");
		detailedResult.deleteOnExit();
	}
	
	/**
	 * The issue already exists on Jira
	 */
	@Test(groups= {"ut"})
	public void testIssueAlreadyExists() {
		
		when(promiseSearch.claim()).thenReturn(new SearchResult(0, 2, 2, Arrays.asList(issue1, issue2)));
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", "CORE", "user", "password");
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
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", "CORE", "user", "password");
		jiraConnector.issueAlreadyExists(jiraBean);
		
	}
	
	/**
	 * The issue does not exist on jira, we return null
	 */
	@Test(groups= {"ut"})
	public void testIssueDoesNotExist() {
		
		when(promiseSearch.claim()).thenReturn(new SearchResult(0, 2, 2, Arrays.asList()));
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1");
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", "CORE", "user", "password");
		IssueBean newJiraBean = jiraConnector.issueAlreadyExists(jiraBean);
		
		// check issue has not been found
		Assert.assertNull(newJiraBean);
	}
	
	@Test(groups= {"ut"})
	public void testCreateIssue() {
		ArgumentCaptor<IssueInput> issueArgument = ArgumentCaptor.forClass(IssueInput.class);
		
		JiraConnector jiraConnector = new JiraConnector("http://foo/bar", "CORE", "user", "password");
		
		Map<String, String> fields = new HashMap<>();
		fields.put("application", "myApp"); // field allowed by jira
		JiraBean jiraBean = new JiraBean(null, "issue 1", "issue 1", "P1", "Bug", "myTest", null, "me", "you", Arrays.asList(screenshot), detailedResult, fields, Arrays.asList("comp1"));
		jiraConnector.createIssue(jiraBean);
		
		
		verify(issueRestClient).createIssue(issueArgument.capture());
		
		// check issue has default data
		Assert.assertEquals(((IssueInput)issueArgument.getValue()).getField("summary").getValue(), "issue 1");
	}
}
