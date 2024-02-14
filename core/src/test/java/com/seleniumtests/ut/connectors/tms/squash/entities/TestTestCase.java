package com.seleniumtests.ut.connectors.tms.squash.entities;

import static org.mockito.Mockito.when;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.tms.squash.entities.Campaign;
import com.seleniumtests.connectors.tms.squash.entities.TestCase;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

public class TestTestCase extends ConnectorsTest {
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		Campaign.configureEntity("user", "pwd", SERVER_URL + "/");
	}
	
	@Test(groups={"ut"})
	public void testExists() {
		createServerMock("GET", "/test-cases/12", 200, "{\r\n" + 
				"  \"_type\" : \"test-case\",\r\n" + 
				"  \"id\" : 12,\r\n" + 
				"  \"name\" : \"walking test\",\r\n" + 
				"  \"reference\" : \"TC1\",\r\n" + 
				"  \"kind\" : \"STANDARD\",\r\n" + 
				"  \"project\" : {\r\n" + 
				"    \"_type\" : \"project\",\r\n" + 
				"    \"id\" : 14,\r\n" + 
				"    \"name\" : \"sample project\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"path\" : \"/sample project/sample folder/walking test\",\r\n" + 
				"  \"parent\" : {\r\n" + 
				"    \"_type\" : \"test-case-folder\",\r\n" + 
				"    \"id\" : 237,\r\n" + 
				"    \"name\" : \"sample folder\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/test-case-folders/237\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"created_by\" : \"User-1\",\r\n" + 
				"  \"created_on\" : \"2017-06-15T10:00:00.000+0000\",\r\n" + 
				"  \"last_modified_by\" : \"User-1\",\r\n" + 
				"  \"last_modified_on\" : \"2017-06-15T10:00:00.000+0000\",\r\n" + 
				"  \"importance\" : \"LOW\",\r\n" + 
				"  \"status\" : \"WORK_IN_PROGRESS\",\r\n" + 
				"  \"nature\" : {\r\n" + 
				"    \"code\" : \"NAT_USER_TESTING\"\r\n" + 
				"  },\r\n" + 
				"  \"type\" : {\r\n" + 
				"    \"code\" : \"TYP_EVOLUTION_TESTING\"\r\n" + 
				"  },\r\n" + 
				"  \"prerequisite\" : \"<p>You must have legs with feet attached to them (one per leg)</p>\\n\",\r\n" + 
				"  \"description\" : \"<p>check that you can walk through the API (literally)</p>\\n\",\r\n" + 
				"  \"automated_test\" : {\r\n" + 
				"    \"_type\" : \"automated-test\",\r\n" + 
				"    \"id\" : 2,\r\n" + 
				"    \"name\" : \"script_custom_field_params_all.ta\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/automated-tests/2\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  },\r\n" + 
				"  \"custom_fields\" : [ {\r\n" + 
				"    \"code\" : \"CF_TXT\",\r\n" + 
				"    \"label\" : \"test level\",\r\n" + 
				"    \"value\" : \"mandatory\"\r\n" + 
				"  }, {\r\n" + 
				"    \"code\" : \"CF_TAGS\",\r\n" + 
				"    \"label\" : \"see also\",\r\n" + 
				"    \"value\" : [ \"walking\", \"bipedal\" ]\r\n" + 
				"  } ],\r\n" + 
				"  \"steps\" : [ {\r\n" + 
				"    \"_type\" : \"action-step\",\r\n" + 
				"    \"id\" : 165,\r\n" + 
				"    \"action\" : \"<p>move ${first_foot} forward</p>\\n\",\r\n" + 
				"    \"expected_result\" : \"<p>I just advanced by one step</p>\\n\",\r\n" + 
				"    \"index\" : 0,\r\n" + 
				"    \"custom_fields\" : [ {\r\n" + 
				"      \"code\" : \"CF_TXT\",\r\n" + 
				"      \"label\" : \"test level\",\r\n" + 
				"      \"value\" : \"mandatory\"\r\n" + 
				"    }, {\r\n" + 
				"      \"code\" : \"CF_TAGS\",\r\n" + 
				"      \"label\" : \"see also\",\r\n" + 
				"      \"value\" : [ \"basic\", \"walking\" ]\r\n" + 
				"    } ],\r\n" + 
				"    \"attachments\" : [ ],\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/test-steps/165\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  }, {\r\n" + 
				"    \"_type\" : \"action-step\",\r\n" + 
				"    \"id\" : 166,\r\n" + 
				"    \"action\" : \"<p>move ${second_foot}&nbsp;forward</p>\\n\",\r\n" + 
				"    \"expected_result\" : \"<p>and another step !</p>\\n\",\r\n" + 
				"    \"index\" : 1,\r\n" + 
				"    \"custom_fields\" : [ {\r\n" + 
				"      \"code\" : \"CF_TXT\",\r\n" + 
				"      \"label\" : \"test level\",\r\n" + 
				"      \"value\" : \"mandatory\"\r\n" + 
				"    }, {\r\n" + 
				"      \"code\" : \"CF_TAGS\",\r\n" + 
				"      \"label\" : \"see also\",\r\n" + 
				"      \"value\" : [ \"basic\", \"walking\" ]\r\n" + 
				"    } ],\r\n" + 
				"    \"attachments\" : [ ],\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/test-steps/166\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  }, {\r\n" + 
				"    \"_type\" : \"call-step\",\r\n" + 
				"    \"id\" : 167,\r\n" + 
				"    \"delegate_parameter_values\" : false,\r\n" + 
				"    \"called_test_case\" : {\r\n" + 
				"      \"_type\" : \"test-case\",\r\n" + 
				"      \"id\" : 239,\r\n" + 
				"      \"name\" : \"victory dance\",\r\n" + 
				"      \"_links\" : {\r\n" + 
				"        \"self\" : {\r\n" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/239\"\r\n" + 
				"        }\r\n" + 
				"      }\r\n" + 
				"    },\r\n" + 
				"    \"called_dataset\" : null,\r\n" + 
				"    \"index\" : 2,\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/test-steps/167\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  }, {\r\n" + 
				"    \"_type\" : \"call-step\",\r\n" + 
				"    \"id\" : 168,\r\n" + 
				"    \"delegate_parameter_values\" : false,\r\n" + 
				"    \"called_test_case\" : {\r\n" + 
				"      \"_type\" : \"unauthorized-resource\",\r\n" + 
				"      \"resource_type\" : \"test-case\",\r\n" + 
				"      \"resource_id\" : 240,\r\n" + 
				"      \"_links\" : {\r\n" + 
				"        \"self\" : {\r\n" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/240\"\r\n" + 
				"        }\r\n" + 
				"      }\r\n" + 
				"    },\r\n" + 
				"    \"called_dataset\" : null,\r\n" + 
				"    \"index\" : 3,\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/test-steps/168\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"parameters\" : [ {\r\n" + 
				"    \"_type\" : \"parameter\",\r\n" + 
				"    \"id\" : 1,\r\n" + 
				"    \"name\" : \"first_foot\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/parameters/1\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  }, {\r\n" + 
				"    \"_type\" : \"parameter\",\r\n" + 
				"    \"id\" : 2,\r\n" + 
				"    \"name\" : \"second_foot\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/parameters/2\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"datasets\" : [ {\r\n" + 
				"    \"_type\" : \"dataset\",\r\n" + 
				"    \"id\" : 1,\r\n" + 
				"    \"name\" : \"right handed people\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/datasets/1\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  }, {\r\n" + 
				"    \"_type\" : \"dataset\",\r\n" + 
				"    \"id\" : 2,\r\n" + 
				"    \"name\" : \"left handed people\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/datasets/2\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"language\" : \"\",\r\n" + 
				"  \"script\" : \"\",\r\n" + 
				"  \"verified_requirements\" : [ {\r\n" + 
				"    \"_type\" : \"requirement-version\",\r\n" + 
				"    \"id\" : 255,\r\n" + 
				"    \"name\" : \"Must have legs\",\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/requirement-versions/255\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  }, {\r\n" + 
				"    \"_type\" : \"unauthorized-resource\",\r\n" + 
				"    \"resource_type\" : \"requirement-version\",\r\n" + 
				"    \"resource_id\" : 256,\r\n" + 
				"    \"_links\" : {\r\n" + 
				"      \"self\" : {\r\n" + 
				"        \"href\" : \"http://localhost:8080/api/rest/latest/requirement-versions/256\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"attachments\" : [ ],\r\n" + 
				"  \"_links\" : {\r\n" + 
				"    \"self\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/238\"\r\n" + 
				"    },\r\n" + 
				"    \"project\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/projects/14\"\r\n" + 
				"    },\r\n" + 
				"    \"steps\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/238/steps\"\r\n" + 
				"    },\r\n" + 
				"    \"parameters\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/238/parameters\"\r\n" + 
				"    },\r\n" + 
				"    \"datasets\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/238/datasets\"\r\n" + 
				"    },\r\n" + 
				"    \"attachments\" : {\r\n" + 
				"      \"href\" : \"http://localhost:8080/api/rest/latest/test-cases/238/attachments\"\r\n" + 
				"    }\r\n" + 
				"  }\r\n" + 
				"}");
		Assert.assertEquals(TestCase.get(12).getId(), 12);
	}
	
	/**
	 * Test case does not exist
	 */
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testNotExist() {
		createServerMock("GET", "/test-cases/12", 404, "{\r\n" + 
				"    \"exception\": \"javax.persistence.EntityNotFoundException\",\r\n" + 
				"    \"message\": \"Unable to find org.squashtest.tm.domain.testcase.TestCase with id 23\"\r\n" + 
				"}");
		TestCase.get(12);
	}

	/**
	 * Error retrieving test case
	 */
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testExistsInError() {
		GetRequest getRequest = (GetRequest) createServerMock("GET", "/test-cases/12", 200, "{}", "requestBodyEntity");
		when(getRequest.asJson()).thenThrow(UnirestException.class);
		
		TestCase.get(12);
	}
	
	@Test(groups={"ut"})
	public void testFromJson() {

		JSONObject json = new JSONObject();
		json.put("_type", "test-case");
		json.put("id", 1);
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/test-case/1\"" + 
				"        }}"));
	
		TestCase testCase = TestCase.fromJson(json);
		Assert.assertEquals(testCase.getId(), 1);
		Assert.assertEquals(testCase.getUrl(), "http://localhost:8080/api/rest/latest/test-case/1");
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testFromJsonWrongFormat() {
		
		JSONObject json = new JSONObject();
		json.put("_type", "test-case");
		json.put("name", "foo");
		json.put("_links", new JSONObject("{\"self\" : {" + 
				"          \"href\" : \"http://localhost:8080/api/rest/latest/test-case/1\"" + 
				"        }}"));
	
		TestCase.fromJson(json);
	}
	
}
