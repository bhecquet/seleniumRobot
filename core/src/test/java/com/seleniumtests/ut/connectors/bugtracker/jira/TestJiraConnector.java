package com.seleniumtests.ut.connectors.bugtracker.jira;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.testng.annotations.BeforeMethod;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.seleniumtests.MockitoTest;

import io.atlassian.util.concurrent.Promise;

public class TestJiraConnector extends MockitoTest {

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
	private Promise<Iterable<Priority>> promisePriorities;
	
	@Mock
	private Promise<Iterable<Field>> promiseFields;
	

	@BeforeMethod(groups={"ut"})
	public void initJira() throws Exception {
		PowerMockito.whenNew(AsynchronousJiraRestClientFactory.class).withNoArguments().thenReturn(restClientFactory);
		when(restClientFactory.createWithBasicHttpAuthentication(any(URI.class), eq("user"), eq("password"))).thenReturn(restClient);
		
		when(restClient.getProjectClient()).thenReturn(projectRestClient);
		when(projectRestClient.getProject(anyString())).thenReturn(promiseProject);
		when(promiseProject.claim()).thenReturn(project);
		
		when(restClient.getMetadataClient()).thenReturn(metadataRestClient);
		when(metadataRestClient.getPriorities()).thenReturn(promisePriorities);
		when(metadataRestClient.getFields()).thenReturn(promiseFields);	
	}
}
