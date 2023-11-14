package com.seleniumtests.ut.connectors.tms.squash.entities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class TestProject extends ConnectorsTest {
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		Campaign.configureEntity("user", "pwd", SERVER_URL + "/");
	}
	
	@Test(groups={"ut"})
	public void testGetAll() {
		createServerMock("GET", "/projects", 200, "{" + 
				"  \"_embedded\" : {" + 
				"    \"projects\" : [ {" + 
				"      \"_type\" : \"project\"," + 
				"      \"id\" : 367," + 
				"      \"name\" : \"sample project 1\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/projects/367\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"project\"," + 
				"      \"id\" : 456," + 
				"      \"name\" : \"sample project 2\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/projects/456\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"project\"," + 
				"      \"id\" : 789," + 
				"      \"name\" : \"sample project 3\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/projects/789\"" + 
				"        }" + 
				"      }" + 
				"    } ]" + 
				"  }," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects?page=0&size=3\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 3," + 
				"    \"totalElements\" : 3," + 
				"    \"totalPages\" : 1," + 
				"    \"number\" : 0" + 
				"  }" + 
				"}");
		List<Project> projects = Project.getAll();
		Assert.assertEquals(projects.size(), 3);
		Assert.assertEquals(projects.get(0).getName(), "sample project 1");
		Assert.assertEquals(projects.get(0).getId(), 367);
		Assert.assertEquals(projects.get(0).getUrl(), "http://localhost:8080/api/rest/latest/projects/367");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetAllWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/projects", 200, "{}", "requestBodyEntity");
		when(getRequest.asPaged(any(), (Function<HttpResponse<JsonNode>, String>) any(Function.class))).thenThrow(UnirestException.class);
		
		Project.getAll();
	}
	
	@Test(groups={"ut"})
	public void testGet() {
		createServerMock("GET", "/projects?projectName=sample project", 200, 
				"{"
				+ "  \"_type\" : \"project\","
				+ "  \"id\" : 367,"
				+ "  \"description\" : \"<p>This project is the main sample project</p>\","
				+ "  \"label\" : \"Main Sample Project\","
				+ "  \"name\" : \"sample project\","
				+ "  \"active\" : true,"
				+ "  \"attachments\" : [ ],"
				+ "  \"_links\" : {"
				+ "    \"self\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/367\""
				+ "    },"
				+ "    \"requirements\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/367/requirements-library/content\""
				+ "    },"
				+ "    \"test-cases\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/367/test-cases-library/content\""
				+ "    },"
				+ "    \"campaigns\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/367/campaigns-library/content\""
				+ "    },"
				+ "    \"permissions\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/367/permissions\""
				+ "    },"
				+ "    \"attachments\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/attachments\""
				+ "    }"
				+ "  }"
				+ "}");
		Project project = Project.get("sample project");
		Assert.assertEquals(project.getName(), "sample project");
		Assert.assertEquals(project.getId(), 367);
		Assert.assertEquals(project.getUrl(), "http://localhost:8080/api/rest/latest/projects/367");
	}

	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class)
	public void testGetWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/projects?projectName=sample project", 200, "{}", "requestBodyEntity");
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
		Project.get("sample project");
	}
	

	@Test(groups={"ut"})
	public void testAsJson() {
		Project project = new Project("http://localhost:8080/api/rest/latest/projects/1", 1, "project");
		JSONObject json = project.asJson();
		Assert.assertEquals(json.getInt("id"), 1);
		Assert.assertEquals(json.getString("_type"), "project");
	}
	
	
	@Test(groups={"ut"})
	public void testFromJson() {

		JSONObject json = new JSONObject();
		json.put("_type", "project");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/projects/1\"" + 
				"        }}"));
	
		Project project = Project.fromJson(json);
		Assert.assertEquals(project.getId(), 1);
		Assert.assertEquals(project.getName(), "foo");
		Assert.assertEquals(project.getUrl(), "http://localhost:8080/api/rest/latest/projects/1");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFromJsonWrongFormat() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "project");
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/projects/1\"" + 
				"        }}"));
		
		Project.fromJson(json);
	}
	
	@Test(groups={"ut"})
	public void testGetCampaignsInProject() {
		createServerMock("GET", "/projects/44/campaigns", 200, "{" + 
				"  \"_embedded\" : {" + 
				"    \"campaigns\" : [ {" + 
				"      \"_type\" : \"campaign\"," + 
				"      \"id\" : 255," + 
				"      \"name\" : \"campaign 1\"," + 
				"      \"reference\" : \"C-1\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/255\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"campaign\"," + 
				"      \"id\" : 122," + 
				"      \"name\" : \"campaign 2\"," + 
				"      \"reference\" : \"C-2\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/122\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"campaign\"," + 
				"      \"id\" : 147," + 
				"      \"name\" : \"campaign 3\"," + 
				"      \"reference\" : \"C-3\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/147\"" + 
				"        }" + 
				"      }" + 
				"    } ]" + 
				"  }," + 
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14/campaigns?page=0&size=3&sort=name,desc\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14/campaigns?page=1&size=3&sort=name,desc\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14/campaigns?page=2&size=3&sort=name,desc\"" + 
				"    }," + 
				"    \"next\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14/campaigns?page=3&size=3&sort=name,desc\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14/campaigns?page=3&size=3&sort=name,desc\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 3," + 
				"    \"totalElements\" : 10," + 
				"    \"totalPages\" : 4," + 
				"    \"number\" : 2" + 
				"  }" + 
				"}", "request");
		
		Project project = new Project("http://localhost:4321/projects/44", 44, "project");
		List<Campaign> campaigns = project.getCampaigns();
		Assert.assertEquals(campaigns.size(), 3);
		Assert.assertEquals(campaigns.get(0).getName(), "campaign 1");
		Assert.assertEquals(campaigns.get(0).getId(), 255);
		
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetCampaignsInProjectWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/projects/44/campaigns", 200, "{}", "request");
		when(getRequest.asPaged(any(), (Function<HttpResponse<JsonNode>, String>) any(Function.class))).thenThrow(UnirestException.class);
		
		Project project = new Project("http://localhost:4321/projects/44", 44, "project");
		project.getCampaigns();
		
	}
}
