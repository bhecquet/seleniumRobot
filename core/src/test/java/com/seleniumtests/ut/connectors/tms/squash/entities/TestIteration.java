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
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
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
public class TestIteration extends ConnectorsTest {
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		new Project("http://localhost:8080/api/rest/latest/projects/1", 1, "project");
		Campaign.configureEntity("user", "pwd", SERVER_URL + "/");
	}
	
	@Test(groups={"ut"})
	public void testGetAllTestCases() {
		createServerMock("GET", "/iterations/1/test-plan", 200, "{" + 
				"  \"_embedded\" : {" + 
				"    \"test-plan\" : [ {" + 
				"      \"_type\" : \"iteration-test-plan-item\"," + 
				"      \"id\" : 4," + 
				"      \"execution_status\" : \"READY\"," + 
				"      \"referenced_test_case\" : {" + 
				"        \"_type\" : \"test-case\"," + 
				"        \"id\" : 8," + 
				"        \"name\" : \"sample test case 8\"," + 
				"        \"reference\" : \"TC-8\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/8\"" + 
				"          }" + 
				"        }" + 
				"      }," + 
				"      \"referenced_dataset\" : {" + 
				"        \"_type\" : \"dataset\"," + 
				"        \"id\" : 90," + 
				"        \"name\" : \"sample dataset 90\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/datasets/90\"" + 
				"          }" + 
				"        }" + 
				"      }," + 
				"      \"last_executed_by\" : \"User-1\"," + 
				"      \"last_executed_on\" : \"2017-06-25T10:00:00.000+0000\"," + 
				"      \"assigned_to\" : \"User-1\"," + 
				"      \"executions\" : [ {" + 
				"        \"_type\" : \"execution\"," + 
				"        \"id\" : 2," + 
				"        \"execution_status\" : \"BLOCKED\"," + 
				"        \"last_executed_by\" : \"User-1\"," + 
				"        \"last_executed_on\" : \"2017-06-24T10:00:00.000+0000\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/executions/2\"" + 
				"          }" + 
				"        }" + 
				"      }, {" + 
				"        \"_type\" : \"execution\"," + 
				"        \"id\" : 3," + 
				"        \"execution_status\" : \"SUCCESS\"," + 
				"        \"last_executed_by\" : \"User-1\"," + 
				"        \"last_executed_on\" : \"2017-06-25T10:00:00.000+0000\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/executions/3\"" + 
				"          }" + 
				"        }" + 
				"      } ]," + 
				"      \"iteration\" : {" + 
				"        \"_type\" : \"iteration\"," + 
				"        \"id\" : 1," + 
				"        \"name\" : \"sample iteration\"," + 
				"        \"reference\" : \"IT1\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1\"" + 
				"          }" + 
				"        }" + 
				"      }," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/4\"" + 
				"        }" + 
				"      }" + 
				"    }, {" + 
				"      \"_type\" : \"iteration-test-plan-item\"," + 
				"      \"id\" : 12," + 
				"      \"execution_status\" : \"READY\"," + 
				"      \"referenced_test_case\" : {" + 
				"        \"_type\" : \"test-case\"," + 
				"        \"id\" : 16," + 
				"        \"name\" : \"sample test case 16\"," + 
				"        \"reference\" : \"TC-16\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/16\"" + 
				"          }" + 
				"        }" + 
				"      }," + 
				"      \"referenced_dataset\" : {" + 
				"        \"_type\" : \"dataset\"," + 
				"        \"id\" : 12," + 
				"        \"name\" : \"sample dataset 12\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/datasets/12\"" + 
				"          }" + 
				"        }" + 
				"      }," + 
				"      \"last_executed_by\" : \"User-1\"," + 
				"      \"last_executed_on\" : \"2017-06-28T10:00:00.000+0000\"," + 
				"      \"assigned_to\" : \"User-1\"," + 
				"      \"executions\" : [ {" + 
				"        \"_type\" : \"execution\"," + 
				"        \"id\" : 9," + 
				"        \"execution_status\" : \"FAILURE\"," + 
				"        \"last_executed_by\" : \"User-1\"," + 
				"        \"last_executed_on\" : \"2017-06-26T10:00:00.000+0000\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/executions/9\"" + 
				"          }" + 
				"        }" + 
				"      }, {" + 
				"        \"_type\" : \"execution\"," + 
				"        \"id\" : 35," + 
				"        \"execution_status\" : \"SUCCESS\"," + 
				"        \"last_executed_by\" : \"User-1\"," + 
				"        \"last_executed_on\" : \"2017-06-28T10:00:00.000+0000\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/executions/35\"" + 
				"          }" + 
				"        }" + 
				"      } ]," + 
				"      \"iteration\" : {" + 
				"        \"_type\" : \"iteration\"," + 
				"        \"id\" : 1," + 
				"        \"name\" : \"sample iteration\"," + 
				"        \"reference\" : \"IT1\"," + 
				"        \"_links\" : {" + 
				"          \"self\" : {" + 
				"            \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1\"" + 
				"          }" + 
				"        }" + 
				"      }," + 
				"      \"_links\" : {" + 
				"        \"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/12\"" + 
				"        }" + 
				"      }" + 
				"    } ]" + 
				"  }," + 
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=0&size=2\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=0&size=2\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=1&size=2\"" + 
				"    }," + 
				"    \"next\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=2&size=2\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=2&size=2\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 2," + 
				"    \"totalElements\" : 6," + 
				"    \"totalPages\" : 3," + 
				"    \"number\" : 1" + 
				"  }" + 
				"}");
		Iteration iteration = new Iteration("http://localhost:8080/api/rest/latest/iterations/1", 1, "my_iteration");
		List<IterationTestPlanItem> testCases = iteration.getAllTestCases();
		Assert.assertEquals(testCases.size(), 2);
		Assert.assertEquals(testCases.get(0).getTestCase().getId(), 8);
		Assert.assertEquals(testCases.get(0).getId(), 4);
		Assert.assertEquals(testCases.get(0).getUrl(), "http://localhost:8080/api/rest/latest/iteration-test-plan-items/4");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetAllTestCaseWithError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/iterations/1/test-plan", 200, "{}", "requestBodyEntity");
		when(getRequest.asPaged(any(), (Function<HttpResponse<JsonNode>, String>) any(Function.class))).thenThrow(UnirestException.class);
		
		Iteration iteration = new Iteration("http://localhost:8080/api/rest/latest/iterations/1", 1, "my_iteration");
		iteration.getAllTestCases();
	}
	
	/**
	 * Check case where no campaigns are available
	 */

	@Test(groups={"ut"})
	public void testGetAllNoTestCases() {
		createServerMock("GET", "/iterations/1/test-plan", 200, "{" +
				"  \"_links\" : {" + 
				"    \"first\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=0&size=2\"" + 
				"    }," + 
				"    \"prev\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=0&size=2\"" + 
				"    }," + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=1&size=2\"" + 
				"    }," + 
				"    \"next\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=2&size=2\"" + 
				"    }," + 
				"    \"last\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1/test-plan?page=2&size=2\"" + 
				"    }" + 
				"  }," + 
				"  \"page\" : {" + 
				"    \"size\" : 2," + 
				"    \"totalElements\" : 6," + 
				"    \"totalPages\" : 3," + 
				"    \"number\" : 1" + 
				"  }" + 
				"}");

		Iteration iteration = new Iteration("http://localhost:8080/api/rest/latest/iterations/1", 1, "my_iteration");
		List<IterationTestPlanItem> testCases = iteration.getAllTestCases();
		Assert.assertEquals(testCases.size(), 0);
	}
	
	@Test(groups={"ut"})
	public void testFromJson() {

		JSONObject json = new JSONObject();
		json.put("_type", "iteration");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1\"" + 
				"        }}"));
	
		Iteration iteration = Iteration.fromJson(json);
		Assert.assertEquals(iteration.getId(), 1);
		Assert.assertEquals(iteration.getName(), "foo");
		Assert.assertEquals(iteration.getUrl(), "http://localhost:8080/api/rest/latest/iterations/1");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFromJsonWrongFormat() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "iteration");
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/iterations/1\"" + 
				"        }}"));
		
		Iteration.fromJson(json);
	}
	
	@Test(groups={"ut"})
	public void testCreateIteration() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/campaigns/2/iterations", 200, "{" + 
				"  \"_type\" : \"iteration\"," + 
				"  \"id\" : 22," + 
				"  \"name\" : \"new iteration\"," + 
				"  \"reference\" : \"NEW_IT\"," + 
				"  \"description\" : \"<p>A new iteration</p>\"," + 
				"  \"parent\" : {" + 
				"    \"_type\" : \"campaign\"," + 
				"    \"id\" : 2," + 
				"    \"name\" : \"parent campaign\"," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/2\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"created_by\" : \"User-A\"," + 
				"  \"created_on\" : \"2017-04-07T10:00:00.000+0000\"," + 
				"  \"last_modified_by\" : \"User-B\"," + 
				"  \"last_modified_on\" : \"2017-04-15T10:00:00.000+0000\"," + 
				"  \"scheduled_start_date\" : \"2017-04-09T10:00:00.000+0000\"," + 
				"  \"scheduled_end_date\" : \"2017-04-14T10:00:00.000+0000\"," + 
				"  \"actual_start_date\" : \"2017-04-10T10:00:00.000+0000\"," + 
				"  \"actual_end_date\" : \"2017-04-15T10:00:00.000+0000\"," + 
				"  \"actual_start_auto\" : false," + 
				"  \"actual_end_auto\" : true," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"CUF\"," + 
				"    \"label\" : \"cuf\"," + 
				"    \"value\" : \"value\"" + 
				"  } ]," + 
				"  \"test_suites\" : [ ]," + 
				"  \"attachments\" : [ ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/22\"" + 
				"    }," + 
				"    \"project\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/4\"" + 
				"    }," + 
				"    \"campaign\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/campaigns/2\"" + 
				"    }," + 
				"    \"test-suites\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/22/test-suites\"" + 
				"    }," + 
				"    \"test-plan\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/22/test-plan\"" + 
				"    }," + 
				"    \"attachments\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/22/attachments\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		
		Campaign campaign = new Campaign("http://localhost:8080/api/rest/latest/campaigns/2", 2, "campaign");
		Iteration.create(campaign, "new iteration");
		verify(postRequest).body(new JSONObject("{\"_type\":\"iteration\",\"name\":\"new iteration\",\"parent\":{\"id\":2,\"_type\":\"campaign\"}}"));
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testCreateIterationWithError() {
		RequestBodyEntity postRequest = (RequestBodyEntity) createServerMock("POST", "/campaigns/2/iterations", 200, "{}", "requestBodyEntity");
		when(postRequest.asJson()).thenThrow(UnirestException.class);
		
		Campaign campaign = new Campaign("http://localhost:8080/api/rest/latest/campaigns/2", 2, "campaign");
		Iteration.create(campaign, "new iteration");
	}
}
