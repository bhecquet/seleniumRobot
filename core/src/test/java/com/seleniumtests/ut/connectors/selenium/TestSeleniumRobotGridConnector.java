/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tools.ant.filters.StringInputStream;
import org.mockito.Mock;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.ut.connectors.ConnectorsTest;

import io.appium.java_client.remote.MobileCapabilityType;


@PrepareForTest({HttpClients.class, Unirest.class})
public class TestSeleniumRobotGridConnector extends ConnectorsTest {

	@Mock
	private CloseableHttpClient client; 
	
	@Mock
	private CloseableHttpResponse response;
	
	@Mock
	private HttpEntity entity;
	
	@Mock
	private StatusLine statusLine;
	
	private DesiredCapabilities capabilities = new DesiredCapabilities();
	
	@BeforeMethod(groups={"ut"})
	private void init() throws ClientProtocolException, IOException {
		PowerMockito.mockStatic(HttpClients.class);
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)anyObject(), anyObject())).thenReturn(response);
		
		PowerMockito.mockStatic(Unirest.class);
	}
	
	@Test(groups={"ut"})
	public void testSendApp() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		capabilities.setCapability(MobileCapabilityType.APP, appFile.getAbsolutePath());
		
		// prepare response
		InputStream is = new StringInputStream("file:app/zip");
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(capabilities);
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), "file:app/zip/" + appFile.getName());
	}
	
	/**
	 * When APP is an HTTP link, do nothing
	 * @throws ClientProtocolException 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDontSendAppWhenHttp() throws ClientProtocolException, IOException {
		
		// prepare app key
		capabilities.setCapability(MobileCapabilityType.APP, "http://server:port/data/application.apk");
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(capabilities);
		
		verify(client, never()).execute((HttpHost)anyObject(), anyObject());
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), "http://server:port/data/application.apk");
	}
	
	/**
	 * When APP is null, do nothing
	 * @throws ClientProtocolException 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDontSendAppWhenAppIsNull() throws ClientProtocolException, IOException {
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(new DesiredCapabilities());
		
		verify(client, never()).execute((HttpHost)anyObject(), anyObject());
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), null);
	}
	
	@Test(groups={"ut"})
	public void testKillProcess() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("POST", SeleniumRobotGridConnector.NODE_TASK_SERVLET, 200, "");	
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:4444/wd/hub");
		connector.setNodeUrl("http://localhost:4321");
		connector.killProcess("myProcess");
		
	}
}
