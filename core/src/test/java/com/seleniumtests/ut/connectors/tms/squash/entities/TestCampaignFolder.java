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
		createServerMock("GET", "/campaign-folders", 200, "{" + 
				"  \"_embedded\" : {" + 
				"    \"campaign-folders\" : [ {" + 
				"      \"_type\" : \"campaign-folder\"," + 
				"      \"id\" : 100," + 
				"      \"name\" : \"qualification\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/100\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"campaign-folder\"," + 
				"      \"id\" : 101," + 
				"      \"name\" : \"CP-18.01\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/101\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"campaign-folder\"," + 
				"      \"id\" : 102," + 
				"      \"name\" : \"DX-U17\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/102\"" + 
				"        }" + 
				"      }" + 
				"    } ]" + 
				"  }," + 
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=1&size=3\"" + 
				"    }," + 
				"    \"next\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=2&size=3\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=3&size=3\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 3," + 
				"    \"totalElements\" : 10," + 
				"    \"totalPages\" : 4," + 
				"    \"number\" : 1" + 
				"  }" + 
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
		createServerMock("GET", "/campaign-folders", 200, "{" + 
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=0&size=3\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=1&size=3\"" + 
				"    }," + 
				"    \"next\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=2&size=3\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders?page=3&size=3\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 3," + 
				"    \"totalElements\" : 10," + 
				"    \"totalPages\" : 4," + 
				"    \"number\" : 1" + 
				"  }" + 
				"}");
		List<CampaignFolder> campaignFolders = CampaignFolder.getAll();
		Assert.assertEquals(campaignFolders.size(), 0);
	}
	

	@Test(groups={"ut"})
	public void testGetAllByProject() {
		
		createServerMock("GET", "/campaign-folders/tree/14", 200, "[ {"
				+ "  \"_type\" : \"project\","
				+ "  \"id\" : 10,"
				+ "  \"name\" : \"project-1\","
				+ "  \"folders\" : [ {"
				+ "    \"_type\" : \"campaign-folder\","
				+ "    \"id\" : 100,"
				+ "    \"name\" : \"folder1\","
				+ "    \"url\" : \"http://localhost:8080/api/rest/latest/campaign-folders/100\","
				+ "    \"children\" : [ ]"
				+ "  }, {"
				+ "    \"_type\" : \"campaign-folder\","
				+ "    \"id\" : 101,"
				+ "    \"name\" : \"folder2\","
				+ "    \"url\" : \"http://localhost:8080/api/rest/latest/campaign-folders/101\","
				+ "    \"children\" : [ ]"
				+ "  } ]"
				+ "}]");
		
		createServerMock("GET", "/campaign-folders/100", 200, "{"
				+ "  \"_type\" : \"campaign-folder\","
				+ "  \"id\" : 100,"
				+ "  \"name\" : \"folder1\","
				+ "  \"project\" : {"
				+ "    \"_type\" : \"project\","
				+ "    \"id\" : 14,"
				+ "    \"name\" : \"Mangrove\","
				+ "    \"_links\" : {"
				+ "      \"self\" : {"
				+ "        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\""
				+ "      }"
				+ "    }"
				+ "  },"
				+ "  \"path\" : \"/Mangrove/old\","
				+ "  \"parent\" : {"
				+ "    \"_type\" : \"project\","
				+ "    \"id\" : 14,"
				+ "    \"name\" : \"Mangrove\","
				+ "    \"_links\" : {"
				+ "      \"self\" : {"
				+ "        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\""
				+ "      }"
				+ "    }"
				+ "  },"
				+ "  \"created_by\" : \"User-1\","
				+ "  \"created_on\" : \"2011-09-30T10:00:00.000+00:00\","
				+ "  \"last_modified_by\" : \"admin\","
				+ "  \"last_modified_on\" : \"2017-06-16T10:00:00.000+00:00\","
				+ "  \"description\" : \"<p>where all the old campaigns go</p>\","
				+ "  \"custom_fields\" : [ {"
				+ "    \"code\" : \"CF_TXT\","
				+ "    \"label\" : \"test level\","
				+ "    \"value\" : \"mandatory\""
				+ "  }, {"
				+ "    \"code\" : \"CF_TAGS\","
				+ "    \"label\" : \"see also\","
				+ "    \"value\" : [ \"walking\", \"bipedal\" ]"
				+ "  } ],"
				+ "  \"attachments\" : [ ],"
				+ "  \"_links\" : {"
				+ "    \"self\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/100\""
				+ "    },"
				+ "    \"project\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\""
				+ "    },"
				+ "    \"content\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/100/content\""
				+ "    },"
				+ "    \"attachments\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/100/attachments\""
				+ "    }"
				+ "  }"
				+ "}");
		createServerMock("GET", "/campaign-folders/101", 200, "{"
				+ "  \"_type\" : \"campaign-folder\","
				+ "  \"id\" : 101,"
				+ "  \"name\" : \"folder2\","
				+ "  \"project\" : {"
				+ "    \"_type\" : \"project\","
				+ "    \"id\" : 14,"
				+ "    \"name\" : \"Mangrove\","
				+ "    \"_links\" : {"
				+ "      \"self\" : {"
				+ "        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\""
				+ "      }"
				+ "    }"
				+ "  },"
				+ "  \"path\" : \"/Mangrove/old\","
				+ "  \"parent\" : {"
				+ "    \"_type\" : \"project\","
				+ "    \"id\" : 14,"
				+ "    \"name\" : \"Mangrove\","
				+ "    \"_links\" : {"
				+ "      \"self\" : {"
				+ "        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\""
				+ "      }"
				+ "    }"
				+ "  },"
				+ "  \"created_by\" : \"User-1\","
				+ "  \"created_on\" : \"2011-09-30T10:00:00.000+00:00\","
				+ "  \"last_modified_by\" : \"admin\","
				+ "  \"last_modified_on\" : \"2017-06-16T10:00:00.000+00:00\","
				+ "  \"description\" : \"<p>where all the old campaigns go</p>\","
				+ "  \"custom_fields\" : [ {"
				+ "    \"code\" : \"CF_TXT\","
				+ "    \"label\" : \"test level\","
				+ "    \"value\" : \"mandatory\""
				+ "  }, {"
				+ "    \"code\" : \"CF_TAGS\","
				+ "    \"label\" : \"see also\","
				+ "    \"value\" : [ \"walking\", \"bipedal\" ]"
				+ "  } ],"
				+ "  \"attachments\" : [ ],"
				+ "  \"_links\" : {"
				+ "    \"self\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/101\""
				+ "    },"
				+ "    \"project\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\""
				+ "    },"
				+ "    \"content\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/101/content\""
				+ "    },"
				+ "    \"attachments\" : {"
				+ "      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/101/attachments\""
				+ "    }"
				+ "  }"
				+ "}");
	
		
		List<CampaignFolder> campaignFolders = CampaignFolder.getAll(project);
		Assert.assertEquals(campaignFolders.size(), 2);
		Assert.assertEquals(campaignFolders.get(0).getName(), "folder1");
		Assert.assertEquals(campaignFolders.get(0).getId(), 100);
		Assert.assertEquals(campaignFolders.get(0).getUrl(), "http://localhost:8080/api/rest/latest/campaign-folders/100");
	}
	

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetAllByProjectWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/campaign-folders/tree/14", 200, "{}", "requestBodyEntity");
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
		CampaignFolder.getAll(project);
	}
	
	@Test(groups={"ut"})
	public void testGetAllByProjectNoFolder() {
		
		createServerMock("GET", "/campaign-folders/tree/14", 200, "[ {"
				+ "  \"_type\" : \"project\","
				+ "  \"id\" : 10,"
				+ "  \"name\" : \"project-1\","
				+ "  \"folders\" : []"
				+ "}]");
		
		
		List<CampaignFolder> campaignFolders = CampaignFolder.getAll(project);
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
		json.put("project", new JSONObject("{" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 10," + 
				"    \"name\" : \"Mangrove\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/project/10\"" + 
				"      }" + 
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
		json.put("parent", new JSONObject("{" + 
				"    \"_type\" : \"campaign-folder\"," + 
				"    \"id\" : 10," + 
				"    \"name\" : \"Mangrove\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folder/10\"" + 
				"      }" + 
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
		json.put("parent", new JSONObject("{" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 10," + 
				"    \"name\" : \"Mangrove\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/project/10\"" + 
				"      }" + 
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
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/campaign-folders", 200, "{" + 
				"  \"_type\" : \"campaign-folder\"," + 
				"  \"id\" : 33," + 
				"  \"name\" : \"Campaign folder 1\"," + 
				"  \"project\" : {" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 14," + 
				"    \"name\" : \"Test Project 1\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"path\" : \"/Test Project 1/Campaign folder 1\"," + 
				"  \"parent\" : {" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 14," + 
				"    \"name\" : \"Test Project 1\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"created_by\" : \"admin\"," + 
				"  \"created_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"last_modified_by\" : \"admin\"," + 
				"  \"last_modified_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"description\" : null," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"cuf1\"," + 
				"    \"label\" : \"Lib Cuf1\"," + 
				"    \"value\" : \"Cuf1 Value\"" + 
				"  }, {" + 
				"    \"code\" : \"cuf2\"," + 
				"    \"label\" : \"Lib Cuf2\"," + 
				"    \"value\" : \"true\"" + 
				"  } ]," + 
				"  \"attachments\" : [ ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33\"" + 
				"    }," + 
				"    \"project\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"    }," + 
				"    \"content\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/content\"" + 
				"    }," + 
				"    \"attachments\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/attachments\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		CampaignFolder campaignFolder = CampaignFolder.create(project, null, "Campaign folder 1");
		verify(postRequest).body(new JSONObject(" {\"_type\":\"campaign-folder\",\"name\":\"Campaign folder 1\",\"parent\":{\"_type\":\"project\",\"id\":14,\"name\":\"Test Project 1\"}}"));
		Assert.assertEquals(campaignFolder.getId(), 33);
		Assert.assertEquals(campaignFolder.getName(), "Campaign folder 1");
		Assert.assertEquals(campaignFolder.getParent().getId(), 14);
	}
	
	@Test(groups={"ut"})
	public void testCreateCampaignFolderWithParent() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/campaign-folders", 200, "{" + 
				"  \"_type\" : \"campaign-folder\"," + 
				"  \"id\" : 33," + 
				"  \"name\" : \"Campaign folder 1\"," + 
				"  \"project\" : {" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 14," + 
				"    \"name\" : \"Test Project 1\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"path\" : \"/Test Project 1/Campaign folder 1\"," + 
				"  \"parent\" : {" + 
				"    \"_type\" : \"campaign-folder\"," + 
				"    \"id\" : 10," + 
				"    \"name\" : \"Campaign folder parent\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/10\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"created_by\" : \"admin\"," + 
				"  \"created_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"last_modified_by\" : \"admin\"," + 
				"  \"last_modified_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"description\" : null," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"cuf1\"," + 
				"    \"label\" : \"Lib Cuf1\"," + 
				"    \"value\" : \"Cuf1 Value\"" + 
				"  }, {" + 
				"    \"code\" : \"cuf2\"," + 
				"    \"label\" : \"Lib Cuf2\"," + 
				"    \"value\" : \"true\"" + 
				"  } ]," + 
				"  \"attachments\" : [ ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33\"" + 
				"    }," + 
				"    \"project\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"    }," + 
				"    \"content\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/content\"" + 
				"    }," + 
				"    \"attachments\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/33/attachments\"" + 
				"    }" + 
				"  }" + 
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
