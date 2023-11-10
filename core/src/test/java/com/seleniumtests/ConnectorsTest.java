/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.testng.MockitoSettings;
import org.mockito.testng.MockitoTestNGListener;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.SessionId;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.WebUIDriverFactory;
import com.seleniumtests.ut.exceptions.TestConfigurationException;

import kong.unirest.Config;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.MultipartBody;
import kong.unirest.PagedList;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;
import org.testng.annotations.Listeners;

@Listeners(MockitoTestNGListener.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConnectorsTest extends MockitoTest {
	
	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private Options options;
	
	@Mock
	private Timeouts timeouts;
	
	@Mock
	private Navigation navigation;
	
	@Mock
	private TargetLocator targetLocator;
	
	@Mock
	private RemoteWebElement element;

	@Mock
	public GetRequest getAliveRequest;
	
	@Mock
	public Config unirestConfig;
	
	@Mock
	public HttpRequestWithBody postRequest;
	
	@Mock
	public HttpResponse<String> responseAliveString;	
	
	@Mock
	public UnirestInstance unirestInstance;

	protected MockedStatic mocked;
	protected MockedStatic mockedWebUiDriverFactory;
	protected MockedConstruction mockedWebUiDriver;
	protected MockedConstruction mockedRemoteWebDriver;
	
	protected GetRequest namedApplicationRequest;
	protected GetRequest namedEnvironmentRequest;
	protected GetRequest namedTestCaseRequest;
	protected GetRequest namedVersionRequest;
	protected GetRequest variablesRequest;
	protected HttpRequestWithBody createApplicationRequest;
	protected HttpRequestWithBody createEnvironmentRequest;
	protected HttpRequestWithBody createVersionRequest;
	protected HttpRequestWithBody createTestCaseRequest;
	protected HttpRequestWithBody createVariableRequest;
	protected HttpRequestWithBody updateVariableRequest;
	protected HttpRequestWithBody updateVariableRequest2;
	
	// to call with "String.format(GRID_STATUS_WITH_SESSION, sessionId)"
	protected static final String GRID_STATUS_WITH_SESSION = "{"
			+ "  \"value\": {"
			+ "    \"ready\": true,"
			+ "    \"message\": \"Selenium Grid ready.\","
			+ "    \"nodes\": ["
			+ "      {"
			+ "        \"id\": \"9f05db23-5533-48c2-8637-0822f8a41d78\","
			+ "        \"uri\": \"http:\\u002f\\u002flocalhsot:5555\","
			+ "        \"maxSessions\": 3,"
			+ "        \"osInfo\": {"
			+ "          \"arch\": \"amd64\","
			+ "          \"name\": \"Windows 10\","
			+ "          \"version\": \"10.0\""
			+ "        },"
			+ "        \"heartbeatPeriod\": 60000,"
			+ "        \"availability\": \"UP\","
			+ "        \"version\": \"4.2.2 (revision 683ccb65d6)\","
			+ "        \"slots\": ["
			+ "          {"
			+ "            \"id\": {"
			+ "              \"hostId\": \"9f05db23-5533-48c2-8637-0822f8a41d78\","
			+ "              \"id\": \"6fd05336-ded3-4602-a1b8-03d94b3f56fa\""
			+ "            },"
			+ "            \"lastStarted\": \"1970-01-01T00:00:00Z\","
			+ "            \"session\": {"
			+ "				 \"sessionId\": \"%s\","
			+ "				 \"uri\": \"http:\\u002f\\u002flocalhost:4321\""
			+ "            },"
			+ "            \"stereotype\": {"
			+ "              \"beta\": true,"
			+ "              \"browserName\": \"MicrosoftEdge\","
			+ "              \"browserVersion\": \"103.0\","
			+ "              \"edge_binary\": \"C:\\u002fProgram Files (x86)\\u002fMicrosoft\\u002fEdge Beta\\u002fApplication\\u002fmsedge.exe\","
			+ "              \"max-sessions\": 5,"
			+ "              \"nodeTags\": ["
			+ "                \"toto\""
			+ "              ],"
			+ "              \"platform\": \"Windows 10\","
			+ "              \"platformName\": \"Windows 10\","
			+ "              \"restrictToTags\": false,"
			+ "              \"se:webDriverExecutable\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\","
			+ "              \"webdriver-executable\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\","
			+ "              \"webdriver.edge.driver\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\""
			+ "            }"
			+ "          }"
			+ "        ]"
			+ "      }"
			+ "    ]"
			+ "  }"
			+ "}";
			
			
	protected static final String GRID_STATUS_NO_SESSION = "{"
			+ "  \"value\": {"
			+ "    \"ready\": true,"
			+ "    \"message\": \"Selenium Grid ready.\","
			+ "    \"nodes\": ["
			+ "      {"
			+ "        \"id\": \"9f05db23-5533-48c2-8637-0822f8a41d78\","
			+ "        \"uri\": \"http:\\u002f\\u002flocalhsot:5555\","
			+ "        \"maxSessions\": 3,"
			+ "        \"osInfo\": {"
			+ "          \"arch\": \"amd64\","
			+ "          \"name\": \"Windows 10\","
			+ "          \"version\": \"10.0\""
			+ "        },"
			+ "        \"heartbeatPeriod\": 60000,"
			+ "        \"availability\": \"UP\","
			+ "        \"version\": \"4.2.2 (revision 683ccb65d6)\","
			+ "        \"slots\": ["
			+ "          {"
			+ "            \"id\": {"
			+ "              \"hostId\": \"9f05db23-5533-48c2-8637-0822f8a41d78\","
			+ "              \"id\": \"6fd05336-ded3-4602-a1b8-03d94b3f56fa\""
			+ "            },"
			+ "            \"lastStarted\": \"1970-01-01T00:00:00Z\","
			+ "            \"session\": null,"
			+ "            \"stereotype\": {"
			+ "              \"beta\": true,"
			+ "              \"browserName\": \"MicrosoftEdge\","
			+ "              \"browserVersion\": \"103.0\","
			+ "              \"edge_binary\": \"C:\\u002fProgram Files (x86)\\u002fMicrosoft\\u002fEdge Beta\\u002fApplication\\u002fmsedge.exe\","
			+ "              \"max-sessions\": 5,"
			+ "              \"nodeTags\": ["
			+ "                \"toto\""
			+ "              ],"
			+ "              \"platform\": \"Windows 10\","
			+ "              \"platformName\": \"Windows 10\","
			+ "              \"restrictToTags\": false,"
			+ "              \"se:webDriverExecutable\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\","
			+ "              \"webdriver-executable\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\","
			+ "              \"webdriver.edge.driver\": \"D:\\u002fDev\\u002fseleniumRobot\\u002fseleniumRobot-grid\\u002fdrivers\\u002fedgedriver_103.0_edge-103-104.exe\""
			+ "            }"
			+ "          },"
			+ "        ]"
			+ "      }"
			+ "    ]"
			+ "  }"
			+ "}";

	@BeforeMethod(groups={"ut", "it"})  
	public void initMocks(final Method method, final ITestContext testNGCtx, final ITestResult testResult) throws Exception {
		mocked = mockStatic(Unirest.class);
		mocked.when(() -> Unirest.spawnInstance()).thenReturn(unirestInstance);
		mocked.when(() -> Unirest.config()).thenReturn(unirestConfig);
	}

	@AfterMethod(groups={"ut", "it"})
	public void resetMocks() {
		mocked.close(); // as we do not use try-with-resource

		if (mockedWebUiDriver != null) {
			mockedWebUiDriver.close();
		}
		if (mockedRemoteWebDriver != null) {
			mockedRemoteWebDriver.close();
		}
		if (mockedWebUiDriverFactory != null) {
			mockedWebUiDriverFactory.close();
		}
	}
	
	/**
	 * Method for creating server reply mock
	 * @throws UnirestException 
	 */
	protected HttpRequest<?> createGridServletServerMock(String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		return createServerMock(GRID_SERVLET_URL, requestType, apiPath, statusCode, replyData, "request");
	}
	protected HttpRequest<?> createGridServletServerMock(String requestType, String apiPath, int statusCode, File replyData) throws UnirestException {
		return createServerMock(GRID_SERVLET_URL, requestType, apiPath, statusCode, replyData, "request");
	}
	protected HttpRequest<?> createGridServletServerMock(String requestType, String apiPath, int statusCode, String replyData, String responseType) throws UnirestException {
		return createServerMock(GRID_SERVLET_URL, requestType, apiPath, statusCode, replyData, responseType);
	}
	protected HttpRequest<?> createGridServletServerMock(String requestType, String apiPath, int statusCode, File replyData, String responseType) throws UnirestException {
		return createServerMock(GRID_SERVLET_URL, requestType, apiPath, statusCode, replyData, responseType);
	}
	protected HttpRequest<?> createServerMock(String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		return createServerMock(requestType, apiPath, statusCode, replyData, "request");
	}
	protected HttpRequest<?> createServerMock(String requestType, String apiPath, int statusCode, File replyData) throws UnirestException {
		return createServerMock(requestType, apiPath, statusCode, replyData, "request");
	}
	protected HttpRequest<?> createServerMock(String serverUrl, String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		return createServerMock(serverUrl, requestType, apiPath, statusCode, replyData, "request");
	}
	protected HttpRequest<?> createServerMock(String serverUrl, String requestType, String apiPath, int statusCode, File replyData) throws UnirestException {
		return createServerMock(serverUrl, requestType, apiPath, statusCode, replyData, "request");
	}
	
	/**
	 * 
	 * @param requestType
	 * @param apiPath
	 * @param statusCode
	 * @param replyData
	 * @param responseType		if "request", replies with the POST request object (HttpRequestWithBody.class). If "body", replies with the body (MultipartBody.class)
	 * @return
	 * @throws UnirestException
	 */
	protected HttpRequest<?> createServerMock(String requestType, String apiPath, int statusCode, File replyData, String responseType) throws UnirestException {
		return createServerMock(SERVER_URL, requestType, apiPath, statusCode, (Object)replyData, responseType);
	}
	protected HttpRequest<?> createServerMock(String requestType, String apiPath, int statusCode, String replyData, String responseType) throws UnirestException {
		return createServerMock(SERVER_URL, requestType, apiPath, statusCode, (Object)replyData, responseType);
	}
	
	protected HttpRequest<?> createServerMock(String serverUrl, String requestType, String apiPath, int statusCode, File replyData, String responseType) throws UnirestException {
		return createServerMock(serverUrl, requestType, apiPath, statusCode, (Object)replyData, responseType);
	}
	protected HttpRequest<?> createServerMock(String serverUrl, String requestType, String apiPath, int statusCode, String replyData, String responseType) throws UnirestException {
		return createServerMock(serverUrl, requestType, apiPath, statusCode, (Object)replyData, responseType);
	}
	
	protected HttpRequest<?> createServerMock(String serverUrl, String requestType, String apiPath, int statusCode, Object replyData, String responseType) throws UnirestException {
		return createServerMock(serverUrl, requestType, apiPath, statusCode, Arrays.asList(replyData), responseType);
	}
	protected HttpRequest<?> createServerMock(String serverUrl, String requestType, String apiPath, int statusCode, final List<Object> replyData, String responseType) throws UnirestException {


		if (replyData.isEmpty()) {
			throw new TestConfigurationException("No replyData specified");
		}

		@SuppressWarnings("unchecked")
		HttpResponse<String> response = mock(HttpResponse.class);
		HttpResponse<JsonNode> jsonResponse = mock(HttpResponse.class);
		HttpResponse<File> streamResponse = mock(HttpResponse.class);
		HttpResponse<byte[]> bytestreamResponse = mock(HttpResponse.class);
		HttpRequest<?> request = mock(HttpRequest.class);
		JsonNode json = mock(JsonNode.class);
		HttpRequestWithBody postRequest = spy(HttpRequestWithBody.class);

		PagedList<JsonNode> pageList = new PagedList<>(); // for asPaged method

		when(request.getUrl()).thenReturn(serverUrl);
		if (replyData.get(0) instanceof String) {
			when(response.getStatus()).thenReturn(statusCode);
			//when(response.getBody()).thenReturn(replyData.toArray(new String[] {}));

			when(response.getBody()).then(new Answer<String>() {
				private int count = -1;

				public String answer(InvocationOnMock invocation) {

					count++;
					if (count >= replyData.size() - 1) {
						return (String) replyData.get(replyData.size() - 1);
					} else {
						return (String) replyData.get(count);
					}
				}
			});
			when(response.getStatusText()).thenReturn("TEXT");

			when(jsonResponse.getStatus()).thenReturn(statusCode);
			when(jsonResponse.getBody()).thenReturn(json);
			when(jsonResponse.getStatusText()).thenReturn("TEXT");
			try {
				// check data is compatible with JSON
				for (Object d : replyData) {
					if (((String) d).isEmpty()) {
						d = "{}";
					}
					try {
						new JSONObject((String) d);
					} catch (JSONException e) {
						new JSONArray((String) d);
					}
				}


				//				JSONObject jsonReply = new JSONObject((String)replyData);
				//				when(json.getObject()).thenReturn(jsonReply);

				when(json.getObject()).then(new Answer<JSONObject>() {
					private int count = -1;

					public JSONObject answer(InvocationOnMock invocation) {

						count++;
						String reply;
						if (count >= replyData.size() - 1) {
							reply = (String) replyData.get(replyData.size() - 1);
						} else {
							reply = (String) replyData.get(count);
						}
						if (reply.isEmpty()) {
							reply = "{}";
						}
						return new JSONObject(reply);
					}
				});

				when(json.getArray()).then(new Answer<JSONArray>() {
					private int count = -1;

					public JSONArray answer(InvocationOnMock invocation) {

						count++;
						String reply;
						if (count >= replyData.size() - 1) {
							reply = (String) replyData.get(replyData.size() - 1);
						} else {
							reply = (String) replyData.get(count);
						}
						if (reply.isEmpty()) {
							reply = "{}";
						}
						return new JSONArray(reply);
					}
				});

				pageList = new PagedList<>();
				pageList.add(jsonResponse);

			} catch (JSONException | NullPointerException e) {
			}


		} else if (replyData.get(0) instanceof File) {
			when(streamResponse.getStatus()).thenReturn(statusCode);
			when(streamResponse.getStatusText()).thenReturn("TEXT");
			when(streamResponse.getBody()).then(new Answer<File>() {
				private int count = -1;

				public File answer(InvocationOnMock invocation) {

					count++;
					if (count >= replyData.size() - 1) {
						return (File) replyData.get(replyData.size() - 1);
					} else {
						return (File) replyData.get(count);
					}
				}
			});

			//when(bytestreamResponse.getBody()).thenReturn(FileUtils.readFileToByteArray((File)replyData));
			when(bytestreamResponse.getBody()).then(new Answer<byte[]>() {
				private int count = -1;

				public byte[] answer(InvocationOnMock invocation) throws IOException {

					count++;
					if (count >= replyData.size() - 1) {
						return (byte[]) FileUtils.readFileToByteArray((File) replyData.get(replyData.size() - 1));
					} else {
						return (byte[]) FileUtils.readFileToByteArray((File) replyData.get(count));
					}
				}
			});

			when(bytestreamResponse.getStatus()).thenReturn(statusCode);
			when(bytestreamResponse.getStatusText()).thenReturn("BYTES");

		}


		switch (requestType) {
			case "GET":
				GetRequest getRequest = mock(GetRequest.class);

				mocked.when(() -> Unirest.get(serverUrl + apiPath)).thenReturn(getRequest);
				when(getRequest.downloadMonitor(any())).thenReturn(getRequest);
				when(getRequest.socketTimeout(anyInt())).thenReturn(getRequest);
				when(unirestInstance.get(serverUrl + apiPath)).thenReturn(getRequest);

				when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.asString()).thenReturn(response);
				when(getRequest.asJson()).thenReturn(jsonResponse);
				when(getRequest.asFile(anyString())).thenReturn(streamResponse);
				when(getRequest.asFile(anyString(), any(StandardCopyOption.class))).thenReturn(streamResponse);
				when(getRequest.asBytes()).thenReturn(bytestreamResponse);
				when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyInt())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyBoolean())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), any(SessionId.class))).thenReturn(getRequest);
				when(getRequest.getUrl()).thenReturn(serverUrl);
				when(getRequest.basicAuth(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.headerReplace(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.asPaged(any(), (Function<HttpResponse<JsonNode>, String>) any(Function.class))).thenReturn(pageList);
				return getRequest;
			case "POST":
				mocked.when(() -> Unirest.post(serverUrl + apiPath)).thenReturn(postRequest);
				when(unirestInstance.post(serverUrl + apiPath)).thenReturn(postRequest);
				return preparePostRequest(serverUrl, responseType, postRequest, response, jsonResponse);
			case "PATCH":
				mocked.when(() -> Unirest.patch(serverUrl + apiPath)).thenReturn(postRequest);
				when(unirestInstance.patch(serverUrl + apiPath)).thenReturn(postRequest);
				return preparePostRequest(serverUrl, responseType, postRequest, response, jsonResponse);
			case "PUT":
				mocked.when(() -> Unirest.put(serverUrl + apiPath)).thenReturn(postRequest);
				when(unirestInstance.put(serverUrl + apiPath)).thenReturn(postRequest);
				return preparePostRequest(serverUrl, responseType, postRequest, response, jsonResponse);
			case "DELETE":
				mocked.when(() -> Unirest.delete(serverUrl + apiPath)).thenReturn(postRequest);
				when(unirestInstance.delete(serverUrl + apiPath)).thenReturn(postRequest);
				return preparePostRequest(serverUrl, responseType, postRequest, response, jsonResponse);

		}
		return null;

	}
	
	private HttpRequest<?> preparePostRequest(String serverUrl, String responseType, HttpRequestWithBody postRequest, HttpResponse<String> response, HttpResponse<JsonNode> jsonResponse) {

		RequestBodyEntity requestBodyEntity = mock(RequestBodyEntity.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);

		when(postRequest.socketTimeout(anyInt())).thenReturn(postRequest);
		when(postRequest.field(anyString(), anyString())).thenReturn(requestMultipartBody);
		when(postRequest.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
		when(postRequest.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
		when(postRequest.field(anyString(), anyDouble())).thenReturn(requestMultipartBody);
		when(postRequest.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
		when(postRequest.basicAuth(anyString(), anyString())).thenReturn(postRequest);
		when(postRequest.headerReplace(anyString(), anyString())).thenReturn(postRequest);
		when(postRequest.queryString(anyString(), anyString())).thenReturn(postRequest);
		when(postRequest.queryString(anyString(), anyInt())).thenReturn(postRequest);
		when(postRequest.queryString(anyString(), anyBoolean())).thenReturn(postRequest);
		when(postRequest.queryString(anyString(), any(SessionId.class))).thenReturn(postRequest);
		when(postRequest.header(anyString(), anyString())).thenReturn(postRequest);
		when(requestMultipartBody.field(anyString(), anyString())).thenReturn(requestMultipartBody);
		when(requestMultipartBody.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
		when(requestMultipartBody.asString()).thenReturn(response);
		doReturn(response).when(postRequest).asString();
		when(postRequest.getUrl()).thenReturn(serverUrl);
		when(postRequest.body(any(JSONObject.class))).thenReturn(requestBodyEntity);
		when(postRequest.body(any(byte[].class))).thenReturn(requestBodyEntity);
		when(postRequest.asJson()).thenReturn(jsonResponse);
		when(requestBodyEntity.asJson()).thenReturn(jsonResponse);
		when(requestBodyEntity.asString()).thenReturn(response);
		when(requestMultipartBody.getUrl()).thenReturn(serverUrl);
		when(requestMultipartBody.asJson()).thenReturn(jsonResponse);
		
		if ("request".equals(responseType)) {
			return postRequest;
		} else if ("body".equals(responseType)) {
			return requestMultipartBody;
		} else if ("requestBodyEntity".equals(responseType)) {
			return requestBodyEntity;
		} else {
			return null;
		}
	}

	protected OngoingStubbing<JsonNode> createJsonServerMock(String requestType, String apiPath, int statusCode, String ... replyData) throws UnirestException {

		@SuppressWarnings("unchecked")
		HttpResponse<JsonNode> jsonResponse = mock(HttpResponse.class);
		HttpRequest<?> request = mock(HttpRequest.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);
		HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);

		when(request.getUrl()).thenReturn(SERVER_URL);
		when(jsonResponse.getStatus()).thenReturn(statusCode);

		OngoingStubbing<JsonNode> stub = when(jsonResponse.getBody()).thenReturn(new JsonNode(replyData[0]));

		for (String reply : Arrays.asList(replyData).subList(1, replyData.length)) {
			stub = stub.thenReturn(new JsonNode(reply));
		}


		switch (requestType) {
			case "GET":
				GetRequest getRequest = mock(GetRequest.class);

				mocked.when(() -> Unirest.get(SERVER_URL + apiPath)).thenReturn(getRequest);

				when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.asJson()).thenReturn(jsonResponse);
				when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyInt())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyBoolean())).thenReturn(getRequest);
				return stub;
			case "POST":
				mocked.when(() -> Unirest.post(SERVER_URL + apiPath)).thenReturn(postRequest);
			case "PATCH":
				mocked.when(() -> Unirest.patch(SERVER_URL + apiPath)).thenReturn(postRequest);
				when(postRequest.field(anyString(), anyString())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
				when(postRequest.queryString(anyString(), anyString())).thenReturn(postRequest);
				when(postRequest.queryString(anyString(), anyInt())).thenReturn(postRequest);
				when(postRequest.queryString(anyString(), anyBoolean())).thenReturn(postRequest);
				when(postRequest.header(anyString(), anyString())).thenReturn(postRequest);
				when(requestMultipartBody.field(anyString(), anyString())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
				return stub;

		}

		return null;	
	}
	
	protected WebUIDriver createMockedWebDriver() throws Exception {

		WebUIDriver uiDriver = spy(new WebUIDriver("main"));
		mockedWebUiDriverFactory = mockStatic(WebUIDriverFactory.class);
		mockedWebUiDriverFactory.when(() -> WebUIDriverFactory.getInstance("main")).thenReturn(uiDriver);

		mockedWebUiDriver = mockConstruction(WebUIDriver.class);

		when(element.getAttribute(anyString())).thenReturn("attribute");
		when(element.getSize()).thenReturn(new Dimension(10, 10));
		when(element.getLocation()).thenReturn(new Point(5, 5));
		when(element.getTagName()).thenReturn("h1");
		when(element.getText()).thenReturn("text");
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);
		when(options.timeouts()).thenReturn(timeouts);

		mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (mock, context) -> {
			when(mock.manage()).thenReturn(options);
			when(mock.getSessionId()).thenReturn(new SessionId("abcdef"));
			when(mock.navigate()).thenReturn(navigation);
			when(mock.switchTo()).thenReturn(targetLocator);
			when(mock.getCapabilities()).thenReturn(new DesiredCapabilities("chrome", "75.0", Platform.WINDOWS));
			when(mock.findElement(By.id("text2"))).thenReturn(element);

		});


		return uiDriver;


	}
	
	/**
	 * Creates a mock for grid on http://localhost:4321
	 *
	 * @throws Exception 
	 */
	protected WebUIDriver createGridHubMockWithNodeOK() throws Exception {
		
		WebUIDriver uiDriver = createMockedWebDriver();
		
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "UI");
		createGridServletServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
		createGridServletServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
		createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
		createJsonServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200,
				// session not found (null)
				GRID_STATUS_NO_SESSION, // called when we get grid status
				GRID_STATUS_NO_SESSION,
				// session found (id provided)
				String.format(GRID_STATUS_WITH_SESSION, "abcdef"));

		return uiDriver;
	}
	

	/**
	 * simulate an alive snapshot sever responding to all requests
	 * @throws UnirestException 
	 */
	protected SeleniumRobotSnapshotServerConnector configureMockedSnapshotServerConnection() throws UnirestException {
		
		// snapshot server comes with variable server
		configureMockedVariableServerConnection();
		
		when(getAliveRequest.asString()).thenReturn(responseAliveString);
		when(responseAliveString.getStatus()).thenReturn(200);
		when(Unirest.get(SERVER_URL + "/snapshot/")).thenReturn(getAliveRequest);
		when(unirestInstance.get(SERVER_URL + "/snapshot/")).thenReturn(getAliveRequest);
		
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(true);
		
		// set default reply from server. To override this behaviour, redefine some steps in test after connector creation
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL, 200, "{'id': '9'}");	
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL, 200, "{'id': '10'}");	
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '11'}");	
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '12'}");
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL, 200, "{'id': '15'}");
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 0.0, 'tooManyDiffs': false}");
		createServerMock("PUT", SeleniumRobotSnapshotServerConnector.SNAPSHOT_API_URL, 200, "{'id': '16', 'computed': true, 'computingError': '', 'diffPixelPercentage': 0.0, 'tooManyDiffs': false}");
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.EXCLUDE_API_URL, 200, "{'id': '18'}");
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL, 200, "{'id': '17'}");
		createServerMock("PATCH", SeleniumRobotSnapshotServerConnector.STEPRESULT_API_URL + "17/", 200, "{'id': '17'}");

		createServerMock("POST", SeleniumRobotSnapshotServerConnector.FILE_API_URL, 201, "{'id': '18'}");
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.LOGS_API_URL, 201, "{'id': '19'}");
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.SESSION_API_URL, 200, "{'id': '13'}");
		createServerMock("PATCH", SeleniumRobotSnapshotServerConnector.SESSION_API_URL + "13/", 200, "{\"id\":13,\"sessionId\":\"4b2e32f4-69dc-4f05-9644-4287acc2c9ac\",\"date\":\"2017-07-24\",\"browser\":\"*none\",\"environment\":\"DEV\",\"version\":2}");		
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15", 200, "{'testSteps': [], 'computed': true, 'isOkWithSnapshots': true}");		
		createServerMock("PATCH", SeleniumRobotSnapshotServerConnector.TESTCASEINSESSION_API_URL + "15/", 200, "{\"id\":12,\"name\":\"Test 1\",\"version\":11,\"testSteps\":[14]}");		
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.TESTSTEP_API_URL, 200, "{'id': '14'}");
		createServerMock("GET", SeleniumRobotServerConnector.NAMED_APPLICATION_API_URL, 200, "{'id': 9}");		
		createServerMock("GET", SeleniumRobotServerConnector.NAMED_ENVIRONMENT_API_URL, 200, "{'id': 10}");		
		createServerMock("GET", SeleniumRobotServerConnector.NAMED_TESTCASE_API_URL, 200, "{'id': 12}");		
		createServerMock("GET", SeleniumRobotServerConnector.NAMED_VERSION_API_URL, 200, "{'id': 11}");	
		
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL, 200, "{'result': 'OK'}"); // upload reference image for step
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.STEP_REFERENCE_API_URL + "17/", 200, Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "images", "googleSearch.png").toFile()); // get reference image
		
		// field detector API
		String detectFieldsReply = "{\"fields\":[{\"class_id\":4,\"top\":142,\"bottom\":166,\"left\":174,\"right\":210,\"class_name\":\"field_with_label\",\"text\":null,\"related_field\":{\"class_id\":0,\"top\":142,\"bottom\":165,\"left\":175,\"right\":211,\"class_name\":\"field\",\"text\":null,\"related_field\":null,\"with_label\":false,\"width\":36,\"height\":23},\"with_label\":true,\"width\":36,\"height\":24},{\"class_id\":2,\"top\":216,\"bottom\":239,\"left\":277,\"right\":342,\"class_name\":\"button\",\"text\":null,\"related_field\":null,\"with_label\":false,\"width\":65,\"height\":23}],\"labels\":[{\"top\":3,\"left\":16,\"width\":72,\"height\":16,\"text\":\"Join Us\",\"right\":88,\"bottom\":19},{\"top\":46,\"left\":16,\"width\":119,\"height\":7,\"text\":\"Screen Name (min. 5 chars)\",\"right\":135,\"bottom\":53}],\"error\":null,\"version\":\"afcc45\", \"fileName\": \"img.png\"}";
		createServerMock("POST", SeleniumRobotSnapshotServerConnector.DETECT_API_URL, 200, detectFieldsReply); // detect field
		createServerMock("GET", SeleniumRobotSnapshotServerConnector.DETECT_API_URL, 200, detectFieldsReply); // get fields detected by previous POST method call
		
		SeleniumRobotSnapshotServerConnector connector = new SeleniumRobotSnapshotServerConnector(true, SERVER_URL);
		
		// reset default value to force creation
		connector.setVersionId(null);
		return connector;
	}
	
	
	/**
	 * simulate an alive variable sever responding to all requests
	 * @throws UnirestException 
	 */
	protected void configureMockedVariableServerConnection() throws UnirestException {

		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerUrl(SERVER_URL);
		SeleniumTestsContextManager.getThreadContext().seleniumServer().setSeleniumRobotServerActive(true);
		
		configureMockedVariableServerConnection(SERVER_URL);
	}
	protected void configureMockedVariableServerConnection(String serverUrl) throws UnirestException {
		when(getAliveRequest.asString()).thenReturn(responseAliveString);
		when(getAliveRequest.header(anyString(), anyString())).thenReturn(getAliveRequest);
		when(responseAliveString.getStatus()).thenReturn(200);
		when(Unirest.get(serverUrl + SeleniumRobotServerConnector.PING_API_URL)).thenReturn(getAliveRequest);
		when(unirestInstance.get(serverUrl + SeleniumRobotServerConnector.PING_API_URL)).thenReturn(getAliveRequest);
		
		// set default reply from server. To override this behaviour, redefine some steps in test after connector creation
		namedApplicationRequest = (GetRequest) createServerMock(serverUrl, "GET", SeleniumRobotVariableServerConnector.NAMED_APPLICATION_API_URL, 200, "{'id': 1}");		
		namedEnvironmentRequest = (GetRequest) createServerMock(serverUrl, "GET", SeleniumRobotVariableServerConnector.NAMED_ENVIRONMENT_API_URL, 200, "{'id': 2}");		
		namedTestCaseRequest = (GetRequest) createServerMock(serverUrl, "GET", SeleniumRobotVariableServerConnector.NAMED_TESTCASE_API_URL, 200, "{'id': 3}");		
		namedVersionRequest = (GetRequest) createServerMock(serverUrl, "GET", SeleniumRobotVariableServerConnector.NAMED_VERSION_API_URL, 200, "{'id': 4}");	
		createApplicationRequest = (HttpRequestWithBody) createServerMock(serverUrl, "POST", SeleniumRobotSnapshotServerConnector.APPLICATION_API_URL, 200, "{'id': '1'}");	
		createEnvironmentRequest = (HttpRequestWithBody) createServerMock(serverUrl, "POST", SeleniumRobotSnapshotServerConnector.ENVIRONMENT_API_URL, 200, "{'id': '2'}");	
		createVersionRequest = (HttpRequestWithBody) createServerMock(serverUrl, "POST", SeleniumRobotSnapshotServerConnector.VERSION_API_URL, 200, "{'id': '4'}");	
		createTestCaseRequest = (HttpRequestWithBody) createServerMock(serverUrl, "POST", SeleniumRobotSnapshotServerConnector.TESTCASE_API_URL, 200, "{'id': '3'}");
		createVariableRequest = (HttpRequestWithBody) createServerMock(serverUrl, "POST", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "{'id': 13, 'name': 'custom.test.variable.key', 'value': 'value', 'reservable': false}");
		updateVariableRequest = (HttpRequestWithBody) createServerMock(serverUrl, "PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 12), 200, "{'id': 12, 'name': 'custom.test.variable.key', 'value': 'value', 'reservable': false}");
		updateVariableRequest2 = (HttpRequestWithBody) createServerMock(serverUrl, "PATCH", String.format(SeleniumRobotVariableServerConnector.EXISTING_VARIABLE_API_URL, 2), 200, "{}");
		variablesRequest = (GetRequest) createServerMock(serverUrl, "GET", SeleniumRobotVariableServerConnector.VARIABLE_API_URL, 200, "[{'id': 1, 'name': 'key1', 'value': 'value1', 'reservable': false}, {'id': 2, 'name': 'key2', 'value': 'value2', 'reservable': true}]");	

	}
}
