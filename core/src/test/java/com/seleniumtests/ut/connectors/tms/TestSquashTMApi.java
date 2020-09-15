package com.seleniumtests.ut.connectors.tms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.function.Function;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.SquashTMApi;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.customexception.ConfigurationException;

import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

@PrepareForTest({Project.class, Unirest.class})
public class TestSquashTMApi extends ConnectorsTest {
	
	private Project project1 = new Project("http://localhost:4321/projects/1", 1, "project1");
	private Project project2 = new Project("http://localhost:4321/projects/2", 2, "project2");

	@BeforeMethod(groups={"ut"})
	public void init() {
		PowerMockito.mockStatic(Project.class);
		
		// server is present by default
		createServerMock("GET", "/api/rest/latest/projects", 200, "{}");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testServerInError() {
		createServerMock("GET", "/api/rest/latest/projects", 500, "{}");
		PowerMockito.when(Project.getAll()).thenReturn(Arrays.asList(project1, project2));
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testServerInError2() {
		GetRequest getRequest = (GetRequest)createServerMock("GET", "/api/rest/latest/projects", 200, "{}");
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
		PowerMockito.when(Project.getAll()).thenReturn(Arrays.asList(project1, project2));
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
	}
	
	@Test(groups={"ut"})
	public void testGetExistingProject() {
		PowerMockito.when(Project.getAll()).thenReturn(Arrays.asList(project1, project2));
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project1");
		Assert.assertEquals(api.getCurrentProject(), project1);
	}
	
	/**
	 * Project does not exist on Squash TM => raise error
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testGetNoExistingProject() {
		PowerMockito.when(Project.getAll()).thenReturn(Arrays.asList(project1, project2));
		SquashTMApi api = new SquashTMApi("http://localhost:4321", "user", "password", "project3");
		Assert.assertEquals(api.getCurrentProject(), project1);
	}
}
