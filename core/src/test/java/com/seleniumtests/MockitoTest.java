/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Redefine calls to PowerMockTestCase methods as they are not called when using TestNG groups
 * we MUST mark them as "alwaysRun"
 * @author behe
 *
 */
@PrepareForTest({Unirest.class})
public class MockitoTest  extends PowerMockTestCase {

	protected static final String SERVER_URL = "http://localhost:4321";

	@BeforeMethod(groups={"ut", "it"})  
	public void beforeMethod(final ITestContext testNGCtx) throws Exception {
		beforePowerMockTestMethod();
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
		MockitoAnnotations.initMocks(this); 
	}
	
	public void initThreadContext(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx, null);
	}
	
	public void initThreadContext(final ITestContext testNGCtx,  final String testName) {
		SeleniumTestsContextManager.initGlobalContext(testNGCtx);
		SeleniumTestsContextManager.initThreadContext(testNGCtx, testName);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
	}
	
	@BeforeClass(groups={"ut", "it"})  
	public void beforeClass() throws Exception {
		beforePowerMockTestClass();
	}
	
	@AfterMethod(groups={"ut", "it"})
	public void afterMethod() throws Exception {
		afterPowerMockTestMethod();
	}
	
	@AfterClass(groups={"ut", "it"})
	public void afterClass() throws Exception {
		afterPowerMockTestClass();
	}
	

	/**
	 * Method for creating server reply mock
	 * @throws UnirestException 
	 */
	protected void createServerMock(String requestType, String apiPath, int statusCode, String replyData) throws UnirestException {
		
		HttpResponse<String> response = mock(HttpResponse.class);
		HttpRequest request = mock(HttpRequest.class);
		MultipartBody requestMultipartBody = mock(MultipartBody.class);
		HttpRequestWithBody postRequest = mock(HttpRequestWithBody.class);
		
		when(request.getUrl()).thenReturn(SERVER_URL);
		
		switch(requestType) {
			case "GET":
				GetRequest getRequest = mock(GetRequest.class); 
				when(Unirest.get(SERVER_URL + apiPath)).thenReturn(getRequest);
				when(getRequest.asString()).thenReturn(response);
				when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
				when(getRequest.queryString(anyString(), anyInt())).thenReturn(getRequest);
				when(response.getStatus()).thenReturn(statusCode);
				when(getRequest.getHttpRequest()).thenReturn(request);
				when(response.getBody()).thenReturn(replyData);
				break;
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
				when(requestMultipartBody.field(anyString(), anyInt())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyBoolean())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyString())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), anyLong())).thenReturn(requestMultipartBody);
				when(requestMultipartBody.field(anyString(), any(File.class))).thenReturn(requestMultipartBody);
				when(requestMultipartBody.asString()).thenReturn(response);
				when(response.getStatus()).thenReturn(statusCode);
				when(response.getBody()).thenReturn(replyData);
				break;
			
		}
	}
	
}
