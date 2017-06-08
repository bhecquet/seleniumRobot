package com.seleniumtests.ut.connectors.selenium;


import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.customexception.ConfigurationException;

@PrepareForTest({HttpClients.class})
public class TestSeleniumGridConnectorFactory extends MockitoTest {

	@Mock
	private CloseableHttpClient client; 
	
	@Mock
	private CloseableHttpResponse response;
	
	@Mock
	private HttpEntity entity;
	
	@Mock
	private StatusLine statusLine;
	
	@BeforeMethod(groups={"ut"})
	private void init() throws ClientProtocolException, IOException {
		PowerMockito.mockStatic(HttpClients.class);
		when(HttpClients.createDefault()).thenReturn(client);
		when(response.getEntity()).thenReturn(entity);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(client.execute((HttpHost)anyObject(), anyObject())).thenReturn(response);
	}
	
	/**
	 * If servlet GuiServlet is available, we get a SeleniumRobotGridConnector
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testWithSeleniumRobotGrid() throws UnsupportedOperationException, IOException {
		
		InputStream is = new StringInputStream("Active sessions");
		
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);
		
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstance("http://localhost:6666") instanceof SeleniumRobotGridConnector);
	}
	
	/**
	 * If servlet GuiServlet is not available, we get a SeleniumGridConnector
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testWithSeleniumGrid() throws UnsupportedOperationException, IOException {
		
		InputStream is = new StringInputStream("default monitoring page");
		
		when(statusLine.getStatusCode()).thenReturn(200);
		when(entity.getContent()).thenReturn(is);
		
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstance("http://localhost:6666") instanceof SeleniumGridConnector);
	}
	
	/**
	 * If status code is not 200, throw an error
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithErrorCodeHttp() throws UnsupportedOperationException, IOException {
		
		InputStream is = new StringInputStream("default monitoring page");
		
		when(statusLine.getStatusCode()).thenReturn(404);
		when(entity.getContent()).thenReturn(is);
		
		SeleniumGridConnectorFactory.getInstance("http://localhost:6666");
	}
	
	/**
	 * If any error occurs when getting servlet, throw an error, grid cannot be contacted
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithError() throws UnsupportedOperationException, IOException {
		
		when(client.execute((HttpHost)anyObject(), anyObject())).thenThrow(IOException.class);
		
		SeleniumGridConnectorFactory.getInstance("http://localhost:6666");
	}
}
