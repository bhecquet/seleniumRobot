package com.seleniumtests.ut.connectors.tms.squash.entities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.CampaignFolder;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.RequestBodyEntity;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class TestCampaignFolder extends ConnectorsTest {

	private Project project;
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		project = new Project("http://localhost:8080/api/rest/latest/projects/14", 14, "Test Project 1");
		Campaign.configureEntity("user", "pwd", SERVER_URL + "/");
	}
	
	@Test(groups={"ut"})
	public void testGetAll() {
		createServerMock("GET", "/campaign-folders", 200, "{\r\n" + 
				"  \"_embedded\" : {\r\n" + 
				"    \"campaign-folders\" : [ {\r\n" + 
				"      \"_type\" : \"campaign-folder\",\r\n" + 
				"      \"id\" : 100,\r\n" + 
				"      \"name\" : \"qualification\",\r\n" + 
				"      \"_links\" : {\r\n" + 
				"        \"self\" : {\r\n" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/100\"\r\n" + 
				"        }\r\n" + 
				"      }\r\n" + 
				"    }, {\r\n" + 
				"      \"_type\" : \"campaign-folder\",\r\n" + 
				"      \"id\" : 101,\r\n" + 
				"      \"name\" : \"CP-18.01\",\r\n" + 
				"      \"_links\" : {\r\n" + 
				"        \"self\" : {\r\n" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/101\"\r\n" + 
				"        }\r\n" + 
				"      }\r\n" + 
				"    }, {\r\n" + 
				"      \"_type\" : \"campaign-folder\",\r\n" + 
				"      \"id\" : 102,\r\n" + 
				"      \"name\" : \"DX-U17\",\r\n" + 
				"      \"_links\" : {\r\n" + 
				"        \"self\" : {\r\n" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/102\"\r\n" + 
				"        }\r\n" + 
				"      }\r\n" + 
				"    } ]\r\n" + 
				"  },\r\n" + 
				"  \"_links\" : {\r\n" + 
				"    \"first\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"prev\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"self\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=1&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"next\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=2&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"last\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=3&size=3\"\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"page\" : {\r\n" + 
				"    \"size\" : 3,\r\n" + 
				"    \"totalElements\" : 10,\r\n" + 
				"    \"totalPages\" : 4,\r\n" + 
				"    \"number\" : 1\r\n" + 
				"  }\r\n" + 
				"}");
		List<CampaignFolder> campaignFolders = CampaignFolder.getAll();
		Assert.assertEquals(campaignFolders.size(), 3);
		Assert.assertEquals(campaignFolders.get(0).getName(), "qualification");
		Assert.assertEquals(campaignFolders.get(0).getId(), 100);
		Assert.assertEquals(campaignFolders.get(0).getUrl(), "http://localhost:8080/api/rest/latest/campaign-folders/100");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetAllWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/campaign-folders", 200, "{}", "requestBodyEntity");
		when(getRequest.asPaged(any(), (Function<HttpResponse<JsonNode>, String>) any(Function.class))).thenThrow(UnirestException.class);
		
		CampaignFolder.getAll();
	}
	
	/**
	 * Check case where no campaigns are available
	 */

	@Test(groups={"ut"})
	public void testGetAllNoFolder() {
		createServerMock("GET", "/campaign-folders", 200, "{\r\n" + 
				"  \"_links\" : {\r\n" + 
				"    \"first\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"prev\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"self\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=1&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"next\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=2&size=3\"\r\n" + 
				"    },\r\n" + 
				"    \"last\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=3&size=3\"\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"page\" : {\r\n" + 
				"    \"size\" : 3,\r\n" + 
				"    \"totalElements\" : 10,\r\n" + 
				"    \"totalPages\" : 4,\r\n" + 
				"    \"number\" : 1\r\n" + 
				"  }\r\n" + 
				"}");
		List<CampaignFolder> campaignFolders = CampaignFolder.getAll();
		Assert.assertEquals(campaignFolders.size(), 0);
	}
	
	@Test(groups={"ut"})
	public void testFromJsonNoParent() {

		JSONObject json = new JSONObject();
		json.put("_type", "campaign-folder");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folder/1\"" + 
				"        }}"));
	
		CampaignFolder campaignFolder = CampaignFolder.fromJson(json);
		Assert.assertEquals(campaignFolder.getId(), 1);
		Assert.assertEquals(campaignFolder.getName(), "foo");
		Assert.assertEquals(campaignFolder.getUrl(), "http://localhost:8080/api/rest/latest/campaign-folder/1");
		Assert.assertNull(campaignFolder.getParent());
		Assert.assertNull(campaignFolder.getProject());
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFromJsonWrongFormat() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "campaign-folder");
		json.put("id", 1);
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folder/1\"" + 
				"        }}"));
		
		CampaignFolder.fromJson(json);
	}
	
	@Test(groups={"ut"})
	public void testFromJsonWithProject() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "campaign-folder");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folder/1\"" + 
				"        }}"));
		json.put("project", new JSONObject("{\r\n" + 
				"    \"_type\" : \"project\",\r\n" + 
				"    \"id\" : 10,\r\n" + 
				"    \"name\" : \"Mangrove\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/project/10\"\r\n" + 
				"      }\r\n" + 
				"    }}"));
		
		CampaignFolder campaignFolder = CampaignFolder.fromJson(json);
		Assert.assertEquals(campaignFolder.getId(), 1);
		Assert.assertEquals(campaignFolder.getName(), "foo");
		Assert.assertEquals(campaignFolder.getUrl(), "http://localhost:8080/api/rest/latest/campaign-folder/1");
		Assert.assertTrue(campaignFolder.getProject() instanceof Project);
		Assert.assertEquals(campaignFolder.getProject().getId(), 10);
	}
	
	@Test(groups={"ut"})
	public void testFromJsonParentFolder() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "campaign-folder");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folder/1\"" + 
				"        }}"));
		json.put("parent", new JSONObject("{\r\n" + 
				"    \"_type\" : \"campaign-folder\",\r\n" + 
				"    \"id\" : 10,\r\n" + 
				"    \"name\" : \"Mangrove\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folder/10\"\r\n" + 
				"      }\r\n" + 
				"    }}"));
		
		CampaignFolder campaignFolder = CampaignFolder.fromJson(json);
		Assert.assertEquals(campaignFolder.getId(), 1);
		Assert.assertEquals(campaignFolder.getName(), "foo");
		Assert.assertEquals(campaignFolder.getUrl(), "http://localhost:8080/api/rest/latest/campaign-folder/1");
		Assert.assertTrue(campaignFolder.getParent() instanceof CampaignFolder);
		Assert.assertEquals(campaignFolder.getParent().getId(), 10);
	}
	
	@Test(groups={"ut"})
	public void testFromJsonParentIsProject() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "campaign-folder");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folder/1\"" + 
				"        }}"));
		json.put("parent", new JSONObject("{\r\n" + 
				"    \"_type\" : \"project\",\r\n" + 
				"    \"id\" : 10,\r\n" + 
				"    \"name\" : \"Mangrove\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/project/10\"\r\n" + 
				"      }\r\n" + 
				"    }}"));
		
		CampaignFolder campaignFolder = CampaignFolder.fromJson(json);
		Assert.assertEquals(campaignFolder.getId(), 1);
		Assert.assertEquals(campaignFolder.getName(), "foo");
		Assert.assertEquals(campaignFolder.getUrl(), "http://localhost:8080/api/rest/latest/campaign-folder/1");
		Assert.assertTrue(campaignFolder.getParent() instanceof Project);
		Assert.assertEquals(campaignFolder.getParent().getId(), 10);
	}
	
	@Test(groups={"ut"})
	public void testAsJson() {
		CampaignFolder campaignFolder = new CampaignFolder(
				"http://localhost:8080/api/rest/latest/campaign-folders/7", 
				7, 
				"folder",
				project, 
				null);
		JSONObject json = campaignFolder.asJson();
		Assert.assertEquals(json.getInt("id"), 7);
		Assert.assertEquals(json.getString("name"), "folder");
		Assert.assertEquals(json.getString("_type"), "campaign-folder");
	}
	
	@Test(groups={"ut"})
	public void testCreateCampaignFolderNoParent() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/campaign-folders", 200, "{\r\n" + 
				"  \"_type\" : \"campaign-folder\",\r\n" + 
				"  \"id\" : 33,\r\n" + 
				"  \"name\" : \"Campaign folder 1\",\r\n" + 
				"  \"project\" : {\r\n" + 
				"    \"_type\" : \"project\",\r\n" + 
				"    \"id\" : 14,\r\n" + 
				"    \"name\" : \"Test Project 1\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"path\" : \"/Test Project 1/Campaign folder 1\",\r\n" + 
				"  \"parent\" : {\r\n" + 
				"    \"_type\" : \"project\",\r\n" + 
				"    \"id\" : 14,\r\n" + 
				"    \"name\" : \"Test Project 1\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"created_by\" : \"admin\",\r\n" + 
				"  \"created_on\" : \"2017-06-15T10:00:00.000+0000\",\r\n" + 
				"  \"last_modified_by\" : \"admin\",\r\n" + 
				"  \"last_modified_on\" : \"2017-06-15T10:00:00.000+0000\",\r\n" + 
				"  \"description\" : null,\r\n" + 
				"  \"custom_fields\" : [ {\r\n" + 
				"    \"code\" : \"cuf1\",\r\n" + 
				"    \"label\" : \"Lib Cuf1\",\r\n" + 
				"    \"value\" : \"Cuf1 Value\"\r\n" + 
				"  }, {\r\n" + 
				"    \"code\" : \"cuf2\",\r\n" + 
				"    \"label\" : \"Lib Cuf2\",\r\n" + 
				"    \"value\" : \"true\"\r\n" + 
				"  } ],\r\n" + 
				"  \"attachments\" : [ ],\r\n" + 
				"  \"_links\" : {\r\n" + 
				"    \"self\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33\"\r\n" + 
				"    },\r\n" + 
				"    \"project\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"\r\n" + 
				"    },\r\n" + 
				"    \"content\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/content\"\r\n" + 
				"    },\r\n" + 
				"    \"attachments\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/attachments\"\r\n" + 
				"    }\r\n" + 
				"  }\r\n" + 
				"}", "request");
		CampaignFolder campaignFolder = CampaignFolder.create(project, null, "Campaign folder 1");
		verify(postRequest).body(new JSONObject(" {\"_type\":\"campaign-folder\",\"name\":\"Campaign folder 1\",\"parent\":{\"_type\":\"project\",\"id\":14,\"name\":\"Test Project 1\"}}"));
		Assert.assertEquals(campaignFolder.getId(), 33);
		Assert.assertEquals(campaignFolder.getName(), "Campaign folder 1");
		Assert.assertEquals(campaignFolder.getParent().getId(), 14);
	}
	
	@Test(groups={"ut"})
	public void testCreateCampaignFolderWithParent() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/campaign-folders", 200, "{\r\n" + 
				"  \"_type\" : \"campaign-folder\",\r\n" + 
				"  \"id\" : 33,\r\n" + 
				"  \"name\" : \"Campaign folder 1\",\r\n" + 
				"  \"project\" : {\r\n" + 
				"    \"_type\" : \"project\",\r\n" + 
				"    \"id\" : 14,\r\n" + 
				"    \"name\" : \"Test Project 1\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"path\" : \"/Test Project 1/Campaign folder 1\",\r\n" + 
				"  \"parent\" : {\r\n" + 
				"    \"_type\" : \"campaign-folder\",\r\n" + 
				"    \"id\" : 10,\r\n" + 
				"    \"name\" : \"Campaign folder parent\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/10\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"created_by\" : \"admin\",\r\n" + 
				"  \"created_on\" : \"2017-06-15T10:00:00.000+0000\",\r\n" + 
				"  \"last_modified_by\" : \"admin\",\r\n" + 
				"  \"last_modified_on\" : \"2017-06-15T10:00:00.000+0000\",\r\n" + 
				"  \"description\" : null,\r\n" + 
				"  \"custom_fields\" : [ {\r\n" + 
				"    \"code\" : \"cuf1\",\r\n" + 
				"    \"label\" : \"Lib Cuf1\",\r\n" + 
				"    \"value\" : \"Cuf1 Value\"\r\n" + 
				"  }, {\r\n" + 
				"    \"code\" : \"cuf2\",\r\n" + 
				"    \"label\" : \"Lib Cuf2\",\r\n" + 
				"    \"value\" : \"true\"\r\n" + 
				"  } ],\r\n" + 
				"  \"attachments\" : [ ],\r\n" + 
				"  \"_links\" : {\r\n" + 
				"    \"self\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33\"\r\n" + 
				"    },\r\n" + 
				"    \"project\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"\r\n" + 
				"    },\r\n" + 
				"    \"content\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/content\"\r\n" + 
				"    },\r\n" + 
				"    \"attachments\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/attachments\"\r\n" + 
				"    }\r\n" + 
				"  }\r\n" + 
				"}", "request");
		CampaignFolder campaignFolderParent = new CampaignFolder("http://localhost:8080/api/rest/latest/campaign-folders/10", 10, "Campaign folder parent", project, null);
		CampaignFolder campaignFolder = CampaignFolder.create(project, campaignFolderParent, "Campaign folder 1");
		verify(postRequest).body(new JSONObject("{\"_type\":\"campaign-folder\",\"name\":\"Campaign folder 1\",\"parent\":{\"_type\":\"campaign-folder\",\"id\":10,\"name\":\"Campaign folder parent\"}}"));
		Assert.assertEquals(campaignFolder.getId(), 33);
		Assert.assertEquals(campaignFolder.getName(), "Campaign folder 1");
		Assert.assertEquals(campaignFolder.getParent().getId(), 10);
	}
	
	
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testCreateCampaignFolderWithError() {
		RequestBodyEntity postRequest = (RequestBodyEntity) createServerMock("POST", "/campaign-folders", 200, "{}", "requestBodyEntity");
		when(postRequest.asJson()).thenThrow(UnirestException.class);
		
		CampaignFolder.create(project, null, "Campaign folder 1");
	}
}
