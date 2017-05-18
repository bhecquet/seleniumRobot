package com.seleniumtests.ut.connectors.selenium;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;

import io.appium.java_client.remote.MobileCapabilityType;


@PrepareForTest({HttpClients.class})
public class TestSeleniumRobotGridConnector extends MockitoTest {

	@Mock
	private CloseableHttpClient client; 
	
	@Mock
	private CloseableHttpResponse response;
	
	@Mock
	private HttpEntity entity;
	
	@Mock
	private StatusLine statusLine;
	
	@Mock
	private RemoteWebDriver driver;
	
	private Capabilities capabilities = new DesiredCapabilities();
	
	@BeforeMethod(alwaysRun=true)
	private void init() throws ClientProtocolException, IOException {
		PowerMockito.mockStatic(HttpClients.class);
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)anyObject(), anyObject())).thenReturn(response);
		when(driver.getCapabilities()).thenReturn(capabilities);
	}
	
	@Test(groups={"ut"})
	public void testSendApp() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		File appFile = File.createTempFile("app", ".apk");
		((DesiredCapabilities)capabilities).setCapability(MobileCapabilityType.APP, appFile.getAbsolutePath());
		
		// prepare response
		InputStream is = new StringInputStream("file:app/zip");
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(driver);
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), "file:app/zip");
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
		((DesiredCapabilities)capabilities).setCapability(MobileCapabilityType.APP, "http://server:port/data/application.apk");
		
		SeleniumGridConnector connector = new SeleniumRobotGridConnector("http://localhost:6666");
		connector.uploadMobileApp(driver);
		
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
		connector.uploadMobileApp(driver);
		
		verify(client, never()).execute((HttpHost)anyObject(), anyObject());
		Assert.assertEquals(capabilities.getCapability(MobileCapabilityType.APP), null);
	}
	
}
