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
import org.openqa.selenium.remote.CapabilityType;
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
import com.seleniumtests.reporter.TestLogging;

import io.appium.java_client.remote.MobileCapabilityType;


@PrepareForTest({HttpClients.class, TestLogging.class})
public class TestSeleniumGridConnector extends MockitoTest {

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
		PowerMockito.mockStatic(TestLogging.class);
		
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)anyObject(), anyObject())).thenReturn(response);
		when(driver.getCapabilities()).thenReturn(capabilities);
	}
	
	@Test(groups={"ut"})
	public void testRunTest() throws UnsupportedOperationException, IOException {
		
		// prepare app file
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.BROWSER_NAME, "firefox");
		((DesiredCapabilities)capabilities).setCapability(CapabilityType.VERSION, "50.0");
		
		// prepare response
		InputStream is = new StringInputStream("{'proxyId':'proxy//node:0'}");
		when(entity.getContent()).thenReturn(is);
		
		SeleniumGridConnector connector = new SeleniumGridConnector("http://localhost:6666");
		connector.runTest(driver);
		
		PowerMockito.verifyStatic();
		TestLogging.info("WebDriver is running on node node, firefox 50.0, session null");
	}
	
	
	/**
	 * With simple selenium grid, upload is not available
	 * @throws ClientProtocolException 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testDoNothing() throws ClientProtocolException, IOException {
		
		SeleniumGridConnector connector = new SeleniumGridConnector("http://localhost:6666");
		connector.uploadMobileApp(driver);
		
		verify(client, never()).execute((HttpHost)anyObject(), anyObject());
	}
}
