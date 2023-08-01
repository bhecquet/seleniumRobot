package com.seleniumtests.ut.connectors.tms.squash.entities;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.Iteration;
import com.seleniumtests.connectors.tms.squash.entities.IterationTestPlanItem;
import com.seleniumtests.connectors.tms.squash.entities.Project;
import com.seleniumtests.connectors.tms.squash.entities.TestCase;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.HttpRequestWithBody;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

@PrepareForTest({Unirest.class})
public class TestIterationTestPlanItem extends ConnectorsTest {

	@BeforeMethod(groups={"ut"})
	public void init() {
		new Project("http://localhost:8080/api/rest/latest/projects/1", 1, "project");
		Campaign.configureEntity("user", "pwd", SERVER_URL + "/");
	}
	

	@Test(groups={"ut"})
	public void testAsJson() {
		TestCase testCase = new TestCase(3, "http://localhost:8080/api/rest/latest/test-cases/3");
		IterationTestPlanItem itpi = new IterationTestPlanItem("http://localhost:8080/api/rest/latest/iteration-test-plan-items/6", 6, testCase);
		
		JSONObject json = itpi.asJson();
		Assert.assertEquals(json.getInt("id"), 6);
		Assert.assertEquals(json.getString("_type"), "iteration-test-plan-item");
	}
	
	
	@Test(groups={"ut"})
	public void testCreateExecution() {
		createServerMock("POST", "/iteration-test-plan-items/1/executions", 200, "{" + 
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
		
		TestCase testCase = new TestCase(3, "http://localhost:8080/api/rest/latest/test-cases/3");
		IterationTestPlanItem itpi = new IterationTestPlanItem("http://localhost:8080/api/rest/latest/iteration-test-plan-items/1", 1, testCase);
		
		TestPlanItemExecution execution = itpi.createExecution();
		Assert.assertEquals(execution.getId(), 22);
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testCreateExecutionWithError() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/iteration-test-plan-items/1/executions", 200, "{}", "request");
		
		when(postRequest.asJson()).thenThrow(UnirestException.class);
		
		TestCase testCase = new TestCase(3, "http://localhost:8080/api/rest/latest/test-cases/3");
		IterationTestPlanItem itpi = new IterationTestPlanItem("http://localhost:8080/api/rest/latest/iteration-test-plan-items/1", 1, testCase);
		
		itpi.createExecution();
	}
	
	@Test(groups={"ut"})
	public void testFromJson() {

		JSONObject json = new JSONObject();
		json.put("_type", "iteration-test-plan-item");
		json.put("id", 6);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/6\"" + 
				"        }}"));
		json.put("referenced_test_case", new JSONObject("{" + 
			"    \"_type\" : \"test-case\"," + 
			"    \"id\" : 25," + 
			"    \"_links\" : {" + 
			"      \"self\" : {" + 
			"        \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/25\"" + 
			"      }" + 
			"    }" + 
			"  }"));
	
		IterationTestPlanItem iteration = IterationTestPlanItem.fromJson(json);
		Assert.assertEquals(iteration.getId(), 6);
		Assert.assertEquals(iteration.getTestCase().getId(), 25);
		Assert.assertEquals(iteration.getUrl(), "http://localhost:8080/api/rest/latest/iteration-test-plan-items/6");
	}

	/**
	 * Sometimes, referenced test-case is null, handle this case
	 */
	@Test(groups={"ut"})
	public void testFromJsonNullTestCase() {

		JSONObject json = new JSONObject();
		json.put("_type", "iteration-test-plan-item");
		json.put("id", 6);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" +
				"          \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/6\"" +
				"        }}"));
		json.put("referenced_test_case", (Object)null);

		IterationTestPlanItem iteration = IterationTestPlanItem.fromJson(json);
		Assert.assertEquals(iteration.getId(), 6);
		Assert.assertNull(iteration.getTestCase());
		Assert.assertEquals(iteration.getUrl(), "http://localhost:8080/api/rest/latest/iteration-test-plan-items/6");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFromJsonWrongFormat() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "iteration-test-plan-item");
		json.put("id", 6);
		
		IterationTestPlanItem.fromJson(json);
	}
	
	@Test(groups={"ut"})
	public void testCreateIterationTestPlanItem() {
		HttpRequestWithBody postRequest = (HttpRequestWithBody) createServerMock("POST", "/iterations/4/test-plan", 200, "{" + 
				"  \"_type\" : \"iteration-test-plan-item\"," + 
				"  \"id\" : 38," + 
				"  \"execution_status\" : \"READY\"," + 
				"  \"referenced_test_case\" : {" + 
				"    \"_type\" : \"test-case\"," + 
				"    \"id\" : 25," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/25\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"referenced_dataset\" : {" + 
				"    \"_type\" : \"dataset\"," + 
				"    \"id\" : 3," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/datasets/3\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"last_executed_by\" : null," + 
				"  \"last_executed_on\" : null," + 
				"  \"assigned_to\" : \"User-1\"," + 
				"  \"executions\" : [ ]," + 
				"  \"iteration\" : {" + 
				"    \"_type\" : \"iteration\"," + 
				"    \"id\" : 4," + 
				"    \"_links\" : {" + 
				"      \"self\" : {" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/iterations/4\"" + 
				"      }" + 
				"    }" + 
				"  }," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/38\"" + 
				"    }," + 
				"    \"project\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"    }," + 
				"    \"test-case\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/25\"" + 
				"    }," + 
				"    \"dataset\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/datasets/3\"" + 
				"    }," + 
				"    \"iteration\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iterations/4\"" + 
				"    }," + 
				"    \"executions\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/38/executions\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		
		Iteration iteration = new Iteration("http://localhost:8080/api/rest/latest/iterations/4", 4, "my_iteration");
		IterationTestPlanItem itpi = IterationTestPlanItem.create(iteration, new TestCase(25, "http://localhost:8080/api/rest/latest/test-cases/25"));
		
		verify(postRequest).body(new JSONObject("{\"_type\":\"iteration-test-plan-item\",\"test_case\":{\"id\":25,\"_type\":\"test-case\"}}"));
		Assert.assertEquals(itpi.getId(), 38);
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testCreateIterationTestPlanItemWithError() {
		RequestBodyEntity postRequest = (RequestBodyEntity) createServerMock("POST", "/iterations/4/test-plan", 200, "{}", "requestBodyEntity");
		when(postRequest.asJson()).thenThrow(UnirestException.class);
		
		Iteration iteration = new Iteration("http://localhost:8080/api/rest/latest/iterations/4", 4, "my_iteration");
		 IterationTestPlanItem.create(iteration, new TestCase(25, "http://localhost:8080/api/rest/latest/test-cases/25"));
		
	}
}
