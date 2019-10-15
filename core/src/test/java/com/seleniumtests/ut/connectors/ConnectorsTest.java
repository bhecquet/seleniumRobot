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
package com.seleniumtests.ut.connectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.WebUIDriverFactory;

@PrepareForTest({Unirest.class, WebUIDriverFactory.class, SeleniumGridDriverFactory.class})
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

	/**
	 * Method for creating server reply mock
	 * @throws UnirestException 
	 */
	protected HttpRequest createServerMock(String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		
		@SuppressWarnings("unchecked")
		HttpResponse<String> response = mock(HttpResponse.class);
		HttpResponse<JsonNode> jsonResponse = mock(HttpResponse.class);
		HttpRequest request = mock(HttpRequest.class);
		JsonNode json = mock(JsonNode.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);
		HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
		
		when(request.getUrl()).thenReturn(SERVER_URL);
		when(response.getStatus()).thenReturn(statusCode);
		when(response.getBody()).thenReturn(replyData);
		when(jsonResponse.getStatus()).thenReturn(statusCode);
		when(jsonResponse.getBody()).thenReturn(json);
		try {
			JSONObject jsonReply = new JSONObject(replyData);
			when(json.getObject()).thenReturn(jsonReply);
		} catch (JSONException e) {}
		
		switch(requestType) {
			case "GET":
				GetRequest getRequest = mock(GetRequest.class); 
				
				when(Unirest.get(SERVER_URL + apiPath)).thenReturn(getRequest);

				when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.asString()).thenReturn(response);
				when(getRequest.asJson()).thenReturn(jsonResponse);
				when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyInt())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyBoolean())).thenReturn(getRequest);
				when(getRequest.getHttpRequest()).thenReturn(request);
				return getRequest;
			case "POST":
				when(Unirest.post(SERVER_URL + apiPath)).thenReturn(postRequest);
			case "PATCH":
				when(Unirest.patch(SERVER_URL + apiPath)).thenReturn(postRequest);
				when(postRequest.field(anyString(), anyString())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
				when(postRequest.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
				when(postRequest.queryString(anyString(), anyString())).thenReturn(postRequest);
				when(postRequest.queryString(anyString(), anyInt())).thenReturn(postRequest);
				when(postRequest.queryString(anyString(), anyBoolean())).thenReturn(postRequest);
				when(postRequest.header(anyString(), anyString())).thenReturn(postRequest);
				when(requestMultipartBody.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyBoolean())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyString())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
				when(requestMultipartBody.asString()).thenReturn(response);
				return postRequest;
			
		}
		return null;	
	}

	protected OngoingStubbing<JsonNode> createJsonServerMock(String requestType, String apiPath, int statusCode, String ... replyData) throws UnirestException {
		
		@SuppressWarnings("unchecked")
		HttpResponse<JsonNode> jsonResponse = mock(HttpResponse.class);
		HttpRequest request = mock(HttpRequest.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);
		HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
		
		when(request.getUrl()).thenReturn(SERVER_URL);
		when(jsonResponse.getStatus()).thenReturn(statusCode);
		
		OngoingStubbing<JsonNode> stub = when(jsonResponse.getBody()).thenReturn(new JsonNode(replyData[0]));

		for (String reply: Arrays.asList(replyData).subList(1, replyData.length)) {
			stub = stub.thenReturn(new JsonNode(reply));
		}

		
		switch(requestType) {
		case "GET":
			GetRequest getRequest = mock(GetRequest.class); 
			
			when(Unirest.get(SERVER_URL + apiPath)).thenReturn(getRequest);
			
			when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
			when(getRequest.asJson()).thenReturn(jsonResponse);
			when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
			when(getRequest.queryString(anyString(), anyInt())).thenReturn(getRequest);
			when(getRequest.queryString(anyString(), anyBoolean())).thenReturn(getRequest);
			when(getRequest.getHttpRequest()).thenReturn(request);
			return stub;
		case "POST":
			when(Unirest.post(SERVER_URL + apiPath)).thenReturn(postRequest);
		case "PATCH":
			when(Unirest.patch(SERVER_URL + apiPath)).thenReturn(postRequest);
			when(postRequest.field(anyString(), anyString())).thenReturn(requestMultipartBody);
			when(postRequest.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
			when(postRequest.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
			when(postRequest.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
			when(postRequest.queryString(anyString(), anyString())).thenReturn(postRequest);
			when(postRequest.queryString(anyString(), anyInt())).thenReturn(postRequest);
			when(postRequest.queryString(anyString(), anyBoolean())).thenReturn(postRequest);
			when(postRequest.header(anyString(), anyString())).thenReturn(postRequest);
			when(requestMultipartBody.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
			when(requestMultipartBody.field(anyString(), anyBoolean())).thenReturn(requestMultipartBody);
			when(requestMultipartBody.field(anyString(), anyString())).thenReturn(requestMultipartBody);
			when(requestMultipartBody.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
			when(requestMultipartBody.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
			return stub;
			
		}
		return null;	
	}
	
	protected WebUIDriver createMockedWebDriver() throws Exception {

		WebUIDriver uiDriver = spy(new WebUIDriver("main"));
		
		PowerMockito.whenNew(WebUIDriver.class).withArguments(any()).thenReturn(uiDriver);
		PowerMockito.whenNew(RemoteWebDriver.class).withAnyArguments().thenReturn(driver);
		when(driver.manage()).thenReturn(options);
		when(options.timeouts()).thenReturn(timeouts);
		when(driver.getSessionId()).thenReturn(new SessionId("abcdef"));
		when(driver.navigate()).thenReturn(navigation);
		when(driver.switchTo()).thenReturn(targetLocator);
		when(driver.getCapabilities()).thenReturn(new DesiredCapabilities("chrome", "75.0", Platform.WINDOWS));
		when(driver.findElement(By.id("text2"))).thenReturn(element);
		when(element.getAttribute(anyString())).thenReturn("attribute");
		when(element.getSize()).thenReturn(new Dimension(10, 10));
		when(element.getLocation()).thenReturn(new Point(5, 5));
		when(element.getTagName()).thenReturn("h1");
		when(element.getText()).thenReturn("text");
		when(element.isDisplayed()).thenReturn(true);
		when(element.isEnabled()).thenReturn(true);
		
		return uiDriver;
	}
	
	/**
	 * Creates a mock for grid on http://localhost:4321
	 * 
	 * PowerMockito.mockStatic(Unirest.class); should be called first
	 * @throws Exception 
	 */
	protected WebUIDriver createGridHubMockWithNodeOK() throws Exception {
		
		WebUIDriver uiDriver = createMockedWebDriver();
		
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, "Console");
		createServerMock("GET", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "ABC");
		createServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, "Gui");
		createServerMock("GET", SeleniumRobotGridConnector.STATUS_SERVLET, 200, "{\"http://localhost:4321\": {" + 
				"    \"busy\": false," + 
				"    \"lastSessionStart\": \"never\"," + 
				"    \"version\": \"4.6.0\"," + 
				"    \"usedTestSlots\": 0,\n" + 
				"    \"testSlots\": 1," + 
				"    \"status\": \"ACTIVE\"" + 
				"  }," + 
				"  \"hub\": {" + 
				"    \"version\": \"4.6.1\"," + 
				"    \"status\": \"ACTIVE\"" + 
				"  }," + 
				"  \"success\": true" + 
				"}");

		
		
		createJsonServerMock("GET", SeleniumRobotGridConnector.API_TEST_SESSSION, 200, 
				// session not found
				"{" + 
				"  \"msg\": \"Cannot find test slot running session 7ef50edc-ce51-40dd-98b6-0a369bff38b in the registry.\"," + 
				"  \"success\": false" + 
				"}", 
				// session found
				"{" + 
				"  \"inactivityTime\": 409," + 
				"  \"internalKey\": \"fef800fc-941d-4f76-9590-711da6443e00\"," + 
				"  \"msg\": \"slot found !\"," + 
				"  \"proxyId\": \"http://localhost:4321\"," + 
				"  \"session\": \"7ef50edc-ce51-40dd-98b6-0a369bff38b1\"," + 
				"  \"success\": true" + 
				"}");
		
		return uiDriver;
	}
}
