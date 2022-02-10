package com.seleniumtests.ut.connectors.tms.squash.entities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.CampaignFolder;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

@PrepareForTest({Unirest.class})
public class TestCampaign extends ConnectorsTest {
	
	private Project project;
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		project = new Project("http://localhost:8080/api/rest/latest/projects/1", 1, "project");
		Campaign.configureEntity("user", "pwd", SERVER_URL + "/");
	}
	
	@Test(groups={"ut"})
	public void testGetAll() {
		createServerMock("GET", "/campaigns", 200, "{" + 
				"  \"_embedded\" : {" + 
				"    \"campaigns\" : [ {" + 
				"      \"_type\" : \"campaign\"," + 
				"      \"id\" : 41," + 
				"      \"name\" : \"sample campaign 1\"," + 
				"      \"reference\" : \"SAMP_CAMP_1\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/41\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"campaign\"," + 
				"      \"id\" : 46," + 
				"      \"name\" : \"sample campaign 2\"," + 
				"      \"reference\" : \"SAMP_CAMP_2\"," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/46\"" + 
				"        }" + 
				"      }" + 
				"    } ]" + 
				"  }," + 
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=0&size=2\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=0&size=2\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=1&size=2\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=1&size=2\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 2," + 
				"    \"totalElements\" : 4," + 
				"    \"totalPages\" : 2," + 
				"    \"number\" : 1" + 
				"  }" + 
				"}");
		List<Campaign> campaigns = Campaign.getAll();
		Assert.assertEquals(campaigns.size(), 2);
		Assert.assertEquals(campaigns.get(0).getName(), "sample campaign 1");
		Assert.assertEquals(campaigns.get(0).getId(), 41);
		Assert.assertEquals(campaigns.get(0).getUrl(), "http://localhost:8080/api/rest/latest/campaigns/41");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetAllWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/campaigns", 200, "{}", "requestBodyEntity");
		when(getRequest.asPaged(any(), (Function<HttpResponse<JsonNode>, String>) any(Function.class))).thenThrow(UnirestException.class);
		
		Campaign.getAll();
	}
	
	@Test(groups={"ut"})
	public void testGetIterations() {
		createServerMock("GET", "/campaign/7/iterations", 200, "{" + 
				"  \"_embedded\" : {" + 
				"    \"iterations\" : [ {" + 
				"      \"_type\" : \"iteration\"," + 
				"      \"id\" : 10," + 
				"      \"name\" : \"sample iteration 1\"," + 
				"      \"reference\" : \"SAMP_IT_1\"," + 
				"      \"description\" : \"<p>This iteration is a sample one...</p>\"," + 
				"      \"parent\" : {" + 
				"        \"_type\" : \"campaign\"," + 
				"        \"id\" : 36," + 
				"        \"name\" : \"sample parent campaign\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/36\"" + 
				"          }" + 
				"        }" + 
				"      }," + 
				"      \"created_by\" : \"User-1\"," + 
				"      \"created_on\" : \"2017-07-21T10:00:00.000+0000\"," + 
				"      \"last_modified_by\" : \"admin\"," + 
				"      \"last_modified_on\" : \"2017-07-22T10:00:00.000+0000\"," + 
				"      \"scheduled_start_date\" : null," + 
				"      \"scheduled_end_date\" : null," + 
				"      \"actual_start_date\" : \"2017-08-01T10:00:00.000+0000\"," + 
				"      \"actual_end_date\" : \"2017-08-30T10:00:00.000+0000\"," + 
				"      \"actual_start_auto\" : false," + 
				"      \"actual_end_auto\" : false," + 
				"      \"custom_fields\" : [ {" + 
				"        \"code\" : \"CUF_Z\"," + 
				"        \"label\" : \"Cuf Z\"," + 
				"        \"value\" : \"value of Z\"" + 
				"      }, {" + 
				"        \"code\" : \"CUF_Y\"," + 
				"        \"label\" : \"Cuf Y\"," + 
				"        \"value\" : \"value of Y\"" + 
				"      } ]," + 
				"      \"test_suites\" : [ {" + 
				"        \"_type\" : \"test-suite\"," + 
				"        \"id\" : 88," + 
				"        \"name\" : null," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/test-suites/88\"" + 
				"          }" + 
				"        }" + 
				"      }, {" + 
				"        \"_type\" : \"test-suite\"," + 
				"        \"id\" : 11," + 
				"        \"name\" : null," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/test-suites/11\"" + 
				"          }" + 
				"        }" + 
				"      }, {" + 
				"        \"_type\" : \"test-suite\"," + 
				"        \"id\" : 14," + 
				"        \"name\" : null," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/test-suites/14\"" + 
				"          }" + 
				"        }" + 
				"      } ]," + 
				"      \"attachments\" : [ ]," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/iterations/10\"" + 
				"        }" + 
				"      }" + 
				"    } ]" + 
				"  }," + 
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/36/iterations?page=0&size=1\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/36/iterations?page=0&size=1\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/36/iterations?page=1&size=1\"" + 
				"    }," + 
				"    \"next\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/36/iterations?page=2&size=1\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/36/iterations?page=2&size=1\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 1," + 
				"    \"totalElements\" : 3," + 
				"    \"totalPages\" : 3," + 
				"    \"number\" : 1" + 
				"  }" + 
				"}");
		

		Campaign campaign = new Campaign("http://localhost:4321/campaign/7",  
				7, 
				"campaign"
				);
		List<Iteration> iterations = campaign.getIterations();
		Assert.assertEquals(iterations.size(), 1);
		Assert.assertEquals(iterations.get(0).getName(), "sample iteration 1");
		Assert.assertEquals(iterations.get(0).getId(), 10);
		Assert.assertEquals(iterations.get(0).getUrl(), "http://localhost:8080/api/rest/latest/iterations/10");
		
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetAllIterationsWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/campaign/7/iterations", 200, "{}", "requestBodyEntity");
		when(getRequest.asPaged(any(), (Function<HttpResponse<JsonNode>, String>) any(Function.class))).thenThrow(UnirestException.class);
		
		Campaign campaign = new Campaign("http://localhost:4321/campaign/7",  
				7, 
				"campaign"
				);
		campaign.getIterations();
	}
	
	/**
	 * Check case where no campaigns are available
	 */

	@Test(groups={"ut"})
	public void testGetAllNoCampaigns() {
		createServerMock("GET", "/campaigns", 200, "{" + 
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=0&size=2\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=0&size=2\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=1&size=2\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns?page=1&size=2\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 2," + 
				"    \"totalElements\" : 4," + 
				"    \"totalPages\" : 2," + 
				"    \"number\" : 1" + 
				"  }" + 
				"}");
		List<Campaign> campaigns = Campaign.getAll();
		Assert.assertEquals(campaigns.size(), 0);
	}
	
	@Test(groups={"ut"})
	public void testFromJson() {

		JSONObject json = new JSONObject();
		json.put("_type", "campaign");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/41\"" + 
				"        }}"));
	
		Campaign campaign = Campaign.fromJson(json);
		Assert.assertEquals(campaign.getId(), 1);
		Assert.assertEquals(campaign.getName(), "foo");
		Assert.assertEquals(campaign.getUrl(), "http://localhost:8080/api/rest/latest/campaigns/41");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFromJsonWrongFormat() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "campaign");
		json.put("id", 1);
		json.put("name", "foo");
		
		Campaign.fromJson(json);
	}
	
	@Test(groups={"ut"})
	public void testCreateCampaignNoFolder() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/campaigns", 200, "{" + 
				"  \"_type\" : \"campaign\"," + 
				"  \"id\" : 332," + 
				"  \"name\" : \"Campaign Test\"," + 
				"  \"reference\" : \"ABCD\"," + 
				"  \"description\" : \"<p>Sed eget rhoncus sapien. Nam et pulvinar nisi. su Do</p>\"," + 
				"  \"status\" : \"PLANNED\"," + 
				"  \"project\" : {" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 44," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/44\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"path\" : \"/sample project/campaign folder/Campaign Test\"," + 
				"  \"parent\" : {" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 44," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/44\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"created_by\" : \"admin\"," + 
				"  \"created_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"last_modified_by\" : \"admin\"," + 
				"  \"last_modified_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"scheduled_start_date\" : \"2021-08-31T10:00:00.000+0000\"," + 
				"  \"scheduled_end_date\" : \"2031-09-29T10:00:00.000+0000\"," + 
				"  \"actual_start_date\" : \"2034-09-29T10:00:00.000+0000\"," + 
				"  \"actual_end_date\" : \"2035-09-29T10:00:00.000+0000\"," + 
				"  \"actual_start_auto\" : false," + 
				"  \"actual_end_auto\" : false," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"CUF_A\"," + 
				"    \"label\" : \"Cuf A\"," + 
				"    \"value\" : \"value of A\"" + 
				"  }, {" + 
				"    \"code\" : \"CUF_B\"," + 
				"    \"label\" : \"Cuf B\"," + 
				"    \"value\" : \"value of B\"" + 
				"  } ]," + 
				"  \"iterations\" : [ ]," + 
				"  \"test_plan\" : [ ]," + 
				"  \"attachments\" : [ ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/332\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		Campaign.create(project, "myCampaign", null);
		verify(postRequest).body(new JSONObject("{\"_type\":\"campaign\",\"name\":\"myCampaign\",\"status\":\"PLANNED\",\"parent\":{\"id\":1,\"_type\":\"project\"}}"));
	}
	
	@Test(groups={"ut"})
	public void testCreateCampaignWithFolder() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/campaigns", 200, "{" + 
				"  \"_type\" : \"campaign\"," + 
				"  \"id\" : 332," + 
				"  \"name\" : \"Campaign Test\"," + 
				"  \"reference\" : \"ABCD\"," + 
				"  \"description\" : \"<p>Sed eget rhoncus sapien. Nam et pulvinar nisi. su Do</p>\"," + 
				"  \"status\" : \"PLANNED\"," + 
				"  \"project\" : {" + 
				"    \"_type\" : \"project\"," + 
				"    \"id\" : 44," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/44\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"path\" : \"/sample project/campaign folder/Campaign Test\"," + 
				"  \"parent\" : {" + 
				"    \"_type\" : \"campaign-folder\"," + 
				"    \"id\" : 7," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/campaign-folders/7\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"created_by\" : \"admin\"," + 
				"  \"created_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"last_modified_by\" : \"admin\"," + 
				"  \"last_modified_on\" : \"2017-06-15T10:00:00.000+0000\"," + 
				"  \"scheduled_start_date\" : \"2021-08-31T10:00:00.000+0000\"," + 
				"  \"scheduled_end_date\" : \"2031-09-29T10:00:00.000+0000\"," + 
				"  \"actual_start_date\" : \"2034-09-29T10:00:00.000+0000\"," + 
				"  \"actual_end_date\" : \"2035-09-29T10:00:00.000+0000\"," + 
				"  \"actual_start_auto\" : false," + 
				"  \"actual_end_auto\" : false," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"CUF_A\"," + 
				"    \"label\" : \"Cuf A\"," + 
				"    \"value\" : \"value of A\"" + 
				"  }, {" + 
				"    \"code\" : \"CUF_B\"," + 
				"    \"label\" : \"Cuf B\"," + 
				"    \"value\" : \"value of B\"" + 
				"  } ]," + 
				"  \"iterations\" : [ ]," + 
				"  \"test_plan\" : [ ]," + 
				"  \"attachments\" : [ ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/332\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		
		CampaignFolder campaignFolder = new CampaignFolder(
				"http://localhost:8080/api/rest/latest/campaign-folders/7", 
				7, 
				"folder",
				project, 
				null);
		Campaign.create(project, "myCampaign", campaignFolder);
		verify(postRequest).body(new JSONObject("{\"_type\":\"campaign\",\"name\":\"myCampaign\",\"status\":\"PLANNED\",\"parent\":{\"id\":7,\"_type\":\"campaign-folder\"}}"));
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testCreateCampaignWithError() {
		RequestBodyEntity postRequest = (RequestBodyEntity) createServerMock("POST", "/campaigns", 200, "{}", "requestBodyEntity");
		when(postRequest.asJson()).thenThrow(UnirestException.class);
		
		CampaignFolder campaignFolder = new CampaignFolder(
				"http://localhost:8080/api/rest/latest/campaign-folders/7", 
				7, 
				"folder",
				project, 
				null);
		Campaign.create(project, "myCampaign", campaignFolder);
	}
}
