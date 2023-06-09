package com.seleniumtests.ut.connectors.tms.squash.entities;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Arrays;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.ExecutionStep;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution;
import com.seleniumtests.connectors.tms.squash.entities.TestPlanItemExecution.ExecutionStatus;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.HttpRequestWithBody;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

@PrepareForTest({Unirest.class})
public class TestTestPlanItemExecution extends ConnectorsTest {

	@BeforeMethod(groups={"ut"})
	public void init() {
		Campaign.configureEntity("user", "pwd", SERVER_URL + "/");
	}
	
	
	@Test(groups={"ut"})
	public void testFromJson() {

		JSONObject json = new JSONObject();
		json.put("_type", "execution");
		json.put("id", 83);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/executions/83\"" + 
				"        }}"));
		json.put("execution_steps", new JSONArray("[ {"
				+ "    \"_type\" : \"execution-step\","
				+ "    \"id\" : 22,"
				+ "    \"execution_status\" : \"SUCCESS\","
				+ "    \"action\" : \"<p>First action</p>\","
				+ "    \"expected_result\" : \"<p>First result</p>\","
				+ "    \"_links\" : {"
				+ "      \"self\" : {"
				+ "        \"href\" : \"http://localhost:8080/api/rest/latest/execution-steps/22\""
				+ "      }"
				+ "    }"
				+ "  }, {"
				+ "    \"_type\" : \"execution-step\","
				+ "    \"id\" : 23,"
				+ "    \"execution_status\" : \"BLOCKED\","
				+ "    \"action\" : \"<p>Second action</p>\","
				+ "    \"expected_result\" : \"<p>Second result</p>\","
				+ "    \"_links\" : {"
				+ "      \"self\" : {"
				+ "        \"href\" : \"http://localhost:8080/api/rest/latest/execution-steps/23\""
				+ "      }"
				+ "    }"
				+ "  }, {"
				+ "    \"_type\" : \"execution-step\","
				+ "    \"id\" : 27,"
				+ "    \"execution_status\" : \"SUCCESS\","
				+ "    \"action\" : \"<p>The Action</p>\","
				+ "    \"expected_result\" : \"<p>The Result</p>\","
				+ "    \"_links\" : {"
				+ "      \"self\" : {"
				+ "        \"href\" : \"http://localhost:8080/api/rest/latest/execution-steps/27\""
				+ "      }"
				+ "    }"
				+ "  } ]"));
	
		TestPlanItemExecution iteration = TestPlanItemExecution.fromJson(json);
		Assert.assertEquals(iteration.getId(), 83);
		Assert.assertEquals(iteration.getName(), "foo");
		Assert.assertEquals(iteration.getUrl(), "http://localhost:8080/api/rest/latest/executions/83");
		Assert.assertEquals(iteration.getSteps().size(), 3);
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFromJsonWrongformat() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "execution");
		json.put("id", 83);
		json.put("name", "foo");
		
		TestPlanItemExecution.fromJson(json);
	}
	
	@Test(groups={"ut"})
	public void testSetResult() {
		createServerMock("PATCH", "/execution-steps/1/execution-status/SUCCESS", 200, "{}");
		HttpRequestWithBody patchRequest = (HttpRequestWithBody) createServerMock("PATCH", "/executions/83?fields=execution_status", 200, "{" + 
				"  \"_type\" : \"execution\"," + 
				"  \"id\" : 83," + 
				"  \"execution_status\" : \"SUCCESS\"," + 
				"  \"comment\" : \"<p>the comment was modified...</p>\"," + 
				"  \"prerequisite\" : \"<p>... but the prerequisite was not</p>\"," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"TXT_STATUS\"," + 
				"    \"label\" : \"text\"," + 
				"    \"value\" : \"allright\"" + 
				"  }, {" + 
				"    \"code\" : \"TAGS_RELATED\"," + 
				"    \"label\" : \"see also\"," + 
				"    \"value\" : [ \"see this\", \"also that\" ]" + 
				"  } ]," + 
				"  \"test_case_custom_fields\" : [ {" + 
				"    \"code\" : \"TC_TEXT\"," + 
				"    \"label\" : \"test case cuf\"," + 
				"    \"value\" : \"I'm from the test case\"" + 
				"  }, {" + 
				"    \"code\" : \"TC_LABELS\"," + 
				"    \"label\" : \"labels\"," + 
				"    \"value\" : [ \"was\", \"not\", \"updated\" ]" + 
				"  } ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83\"" + 
				"    }," + 
				"    \"project\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"    }," + 
				"    \"test_plan_item\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/1\"" + 
				"    }," + 
				"    \"execution-steps\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83/execution-steps\"" + 
				"    }," + 
				"    \"attachments\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83/attachments\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		
		TestPlanItemExecution execution = new TestPlanItemExecution("http://localhost:4321/executions/83", 83, "execution");
		ExecutionStep step1 = spy(new ExecutionStep("http://localhost:4321/execution-steps/1", 1, "action1"));
		ExecutionStep step2 = spy(new ExecutionStep("http://localhost:4321/execution-steps/2", 2, "action2"));
		execution.setSteps(Arrays.asList(step1, step2));
		execution.setResult(ExecutionStatus.SUCCESS, null);
		
		verify(patchRequest).body(new JSONObject("{\"_type\":\"execution\",\"execution_status\":\"SUCCESS\"}"));
		verify(step1).setStatus(ExecutionStatus.SUCCESS);
		verify(step1, never()).setComment(anyString());
		verify(step2, never()).setStatus(ExecutionStatus.SUCCESS);
	}
	
	@Test(groups={"ut"})
	public void testSetResultWithComment() {
		createServerMock("PATCH", "/execution-steps/1/execution-status/SUCCESS", 200, "{}"); // set status
		createServerMock("PATCH", "/execution-steps/1", 200, "{}"); // set comment 
		HttpRequestWithBody patchRequest = (HttpRequestWithBody) createServerMock("PATCH", "/executions/83?fields=execution_status", 200, "{" + 
				"  \"_type\" : \"execution\"," + 
				"  \"id\" : 83," + 
				"  \"execution_status\" : \"SUCCESS\"," + 
				"  \"comment\" : \"<p>the comment was modified...</p>\"," + 
				"  \"prerequisite\" : \"<p>... but the prerequisite was not</p>\"," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"TXT_STATUS\"," + 
				"    \"label\" : \"text\"," + 
				"    \"value\" : \"allright\"" + 
				"  }, {" + 
				"    \"code\" : \"TAGS_RELATED\"," + 
				"    \"label\" : \"see also\"," + 
				"    \"value\" : [ \"see this\", \"also that\" ]" + 
				"  } ]," + 
				"  \"test_case_custom_fields\" : [ {" + 
				"    \"code\" : \"TC_TEXT\"," + 
				"    \"label\" : \"test case cuf\"," + 
				"    \"value\" : \"I'm from the test case\"" + 
				"  }, {" + 
				"    \"code\" : \"TC_LABELS\"," + 
				"    \"label\" : \"labels\"," + 
				"    \"value\" : [ \"was\", \"not\", \"updated\" ]" + 
				"  } ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83\"" + 
				"    }," + 
				"    \"project\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"    }," + 
				"    \"test_plan_item\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/1\"" + 
				"    }," + 
				"    \"execution-steps\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83/execution-steps\"" + 
				"    }," + 
				"    \"attachments\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83/attachments\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		
		TestPlanItemExecution execution = new TestPlanItemExecution("http://localhost:4321/executions/83", 83, "execution");
		ExecutionStep step1 = spy(new ExecutionStep("http://localhost:4321/execution-steps/1", 1, "action1"));
		ExecutionStep step2 = spy(new ExecutionStep("http://localhost:4321/execution-steps/2", 2, "action2"));
		execution.setSteps(Arrays.asList(step1, step2));
		execution.setResult(ExecutionStatus.SUCCESS, "an error occured");
		
		verify(patchRequest).body(new JSONObject("{\"_type\":\"execution\",\"execution_status\":\"SUCCESS\"}"));
		verify(step1).setStatus(ExecutionStatus.SUCCESS);
		verify(step1).setComment("an error occured");
		verify(step2, never()).setStatus(ExecutionStatus.SUCCESS);
	}
	
	/**
	 * Check there is no error if no step defined
	 */
	@Test(groups={"ut"})
	public void testSetResultNoStep() {
		createServerMock("PATCH", "/execution-steps/1/execution-status/SUCCESS", 200, "{}");
		HttpRequestWithBody patchRequest = (HttpRequestWithBody) createServerMock("PATCH", "/executions/83?fields=execution_status", 200, "{" + 
				"  \"_type\" : \"execution\"," + 
				"  \"id\" : 83," + 
				"  \"execution_status\" : \"SUCCESS\"," + 
				"  \"comment\" : \"<p>the comment was modified...</p>\"," + 
				"  \"prerequisite\" : \"<p>... but the prerequisite was not</p>\"," + 
				"  \"custom_fields\" : [ {" + 
				"    \"code\" : \"TXT_STATUS\"," + 
				"    \"label\" : \"text\"," + 
				"    \"value\" : \"allright\"" + 
				"  }, {" + 
				"    \"code\" : \"TAGS_RELATED\"," + 
				"    \"label\" : \"see also\"," + 
				"    \"value\" : [ \"see this\", \"also that\" ]" + 
				"  } ]," + 
				"  \"test_case_custom_fields\" : [ {" + 
				"    \"code\" : \"TC_TEXT\"," + 
				"    \"label\" : \"test case cuf\"," + 
				"    \"value\" : \"I'm from the test case\"" + 
				"  }, {" + 
				"    \"code\" : \"TC_LABELS\"," + 
				"    \"label\" : \"labels\"," + 
				"    \"value\" : [ \"was\", \"not\", \"updated\" ]" + 
				"  } ]," + 
				"  \"_links\" : {" + 
				"    \"self\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83\"" + 
				"    }," + 
				"    \"project\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"" + 
				"    }," + 
				"    \"test_plan_item\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/iteration-test-plan-items/1\"" + 
				"    }," + 
				"    \"execution-steps\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83/execution-steps\"" + 
				"    }," + 
				"    \"attachments\" : {" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/executions/83/attachments\"" + 
				"    }" + 
				"  }" + 
				"}", "request");
		
		TestPlanItemExecution execution = new TestPlanItemExecution("http://localhost:4321/executions/83", 83, "execution");
		execution.setResult(ExecutionStatus.SUCCESS, null);
		
		verify(patchRequest).body(new JSONObject("{\"_type\":\"execution\",\"execution_status\":\"SUCCESS\"}"));
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testSetResultWithError() {
		RequestBodyEntity patchRequest = (RequestBodyEntity) createServerMock("PATCH", "/executions/83?fields=execution_status", 200, "{}", "requestBodyEntity");
		when(patchRequest.asJson()).thenThrow(UnirestException.class);
		
		TestPlanItemExecution execution = new TestPlanItemExecution("http://localhost:4321/executions/83", 83, "execution");
		execution.setResult(ExecutionStatus.SUCCESS, null);
	}
}
