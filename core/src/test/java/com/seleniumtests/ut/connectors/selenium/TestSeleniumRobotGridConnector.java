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
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;

import io.appium.java_client.remote.MobileCapabilityType;


@PrepareForTest({HttpClients.class, Unirest.class})
public class TestSeleniumRobotGridConnector extends MockitoTest {

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
