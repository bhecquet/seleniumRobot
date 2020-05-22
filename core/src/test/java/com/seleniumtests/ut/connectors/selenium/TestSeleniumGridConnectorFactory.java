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
package com.seleniumtests.ut.connectors.selenium;


import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.BrowserStackGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.customexception.ConfigurationException;

import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

@PrepareForTest({Unirest.class})
public class TestSeleniumGridConnectorFactory extends ConnectorsTest {
	
	@Mock
	private GetRequest gRequest;

	private final String guiServletContent = "<html>\r\n" + 
			"	<head>\r\n" + 
			"			<link href='/grid/resources/templates/css/report.css' rel='stylesheet' type='text/css' />\r\n" + 
			"			<script src=\"/grid/resources/templates/js/status.js\"></script>\r\n" + 
			"			<link href=\"/grid/resources/templates/css/hubCss.css\" rel=\"stylesheet\" type=\"text/css\">\r\n" + 
			"			<link href=\"/grid/resources/templates/css/bootstrap.min.css\" rel=\"stylesheet\">\r\n" + 
			"			<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js\"></script>\r\n" + 
			"			<script src=\"/grid/resources/templates/css/bootstrap.min.js\"></script>\r\n" + 
			"			<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n" + 
			"			<link rel=\"icon\" href=\"https://d30y9cdsu7xlg0.cloudfront.net/png/1248-200.png\">\r\n" + 
			"			<title>Selenium Robot</title>\r\n" + 
			"	</head>\r\n" + 
			"	\r\n" + 
			"	<body>\r\n" + 
			"			<header>\r\n" + 
			"					<a ><img src=\"/grid/resources/templates/img/seleniumlogo_low.png\" alt=\"selenium\" id=\"selenium\"></a>\r\n" + 
			"					<p id=\"titre\" >Infotel</p>\r\n" + 
			"			</header>\r\n" + 
			"		\r\n" + 
			"		\r\n" + 
			"	<article>\r\n" + 
			"\r\n" + 
			"			<h1 id=\"hub\">Hub Status</h1>"
			+ "</article>\r\n" + 
			"		\r\n" + 
			"			\r\n" + 
			"<footer>\r\n" + 
			"		<a href=\"#\" class=\"haut\"><img src=\"/grid/resources/templates/img/up.png\" alt=\"haut\" id=\"haut\"></a>\r\n" + 
			"</footer>\r\n" + 
			"		\r\n" + 
			"	</body>\r\n" + 
			"\r\n" + 
			"</html>\r\n" + 
			"";
	
	private final String consoleServletContent = "<html>"
			+ "<head></head>"
			+ "<body>"
			+ "		<div id='main-content'>"
			+ "			<div id='header'>"
			+ "				<h1><a href='/grid/console'>Selenium</a></h1>"
			+ "				<h2>Grid Console v.3.8.1</h2>"
			+ "			</div>"
			+ "		</div>"
			+ "</body>"
			+ "</html>";
	
	/**
	 * If servlet GuiServlet is available, we get a SeleniumRobotGridConnector
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException 
	 */
	@Test(groups={"ut"})
	public void testWithSeleniumRobotGrid() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, guiServletContent);		
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);		
		
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstances(Arrays.asList(SERVER_URL + "/wd/hub")).get(0) instanceof SeleniumRobotGridConnector);
	}
	
	/**
	 * Check that with several selenium grid URL (seleniumRobot or pure selenium), several grid connectors are created, one for each URL
	 * if they are all accessible 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testWithSeveralSeleniumRobotGrid() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, guiServletContent);		
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);	

		createServerMock("GET", "5" + SeleniumRobotGridConnector.GUI_SERVLET, 404, "default monitoring page");
		createServerMock("GET", "5" + SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);		
		
		List<SeleniumGridConnector> gridConnectors = SeleniumGridConnectorFactory.getInstances(Arrays.asList(SERVER_URL + "/wd/hub", SERVER_URL + "5/wd/hub"));
		
		Assert.assertEquals(gridConnectors.size(), 2);
		Assert.assertTrue(gridConnectors.get(0) instanceof SeleniumRobotGridConnector);
		Assert.assertTrue(gridConnectors.get(1) instanceof SeleniumGridConnector);
	}
	
	/**
	 * Check that grid connectors are only created if the selenium grid is accessible (replies before timeout) 
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException
	 */
	@Test(groups={"ut"})
	public void testWithSeveralSeleniumGridNotAllThere() throws UnsupportedOperationException, IOException, UnirestException {
				
		// only the second grid replies
		createServerMock("GET", "5" + SeleniumRobotGridConnector.GUI_SERVLET, 404, "default monitoring page");
		createServerMock("GET", "5" + SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);		
		
		// 2 grid URL given
		List<SeleniumGridConnector> gridConnectors = SeleniumGridConnectorFactory.getInstances(Arrays.asList(SERVER_URL + "/wd/hub", SERVER_URL + "5/wd/hub"));
		
		Assert.assertEquals(gridConnectors.size(), 1);
		Assert.assertTrue(gridConnectors.get(0) instanceof SeleniumGridConnector);
	}
	
	/**
	 * If servlet GuiServlet is not available, we get a SeleniumGridConnector
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException 
	 */
	@Test(groups={"ut"})
	public void testWithSeleniumGrid() throws UnsupportedOperationException, IOException, UnirestException {
		
		createServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 404, "default monitoring page");
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);			
		
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstances(Arrays.asList(SERVER_URL + "/wd/hub")).get(0) instanceof SeleniumGridConnector);
	}
	
	/**
	 * If status code is not 200, throw an error
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithErrorCodeHttp() throws UnsupportedOperationException, IOException, UnirestException {
		SeleniumGridConnectorFactory.setRetryTimeout(1);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 404, "default monitoring page");	
		
		SeleniumGridConnectorFactory.getInstances(Arrays.asList(SERVER_URL + "/wd/hub"));
	}
	
	/**
	 * If any error occurs when getting servlet, throw an error, grid cannot be contacted
	 * Check also that we retry connection during N seconds (defined by retryTimeout)
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithError() throws UnsupportedOperationException, IOException, UnirestException {
		
		when(Unirest.get(SERVER_URL + SeleniumGridConnector.CONSOLE_SERVLET)).thenReturn(gRequest);
		when(gRequest.asString()).thenThrow(UnirestException.class);

		LocalDateTime start = LocalDateTime.now();
		try {
			SeleniumGridConnectorFactory.setRetryTimeout(5);
			SeleniumGridConnectorFactory.getInstances(Arrays.asList(SERVER_URL + "/wd/hub"));
		} catch (ConfigurationException e) {
			
			// check connection duration
			Assert.assertTrue(LocalDateTime.now().minusSeconds(5).isAfter(start));
			throw e;
		} finally {
			SeleniumGridConnectorFactory.setRetryTimeout(SeleniumGridConnectorFactory.DEFAULT_RETRY_TIMEOUT);
		}
	}
	

	/**
	 * If URL contains browserstack, returns a browserstack connector
	 * @throws UnsupportedOperationException
	 * @throws IOException
	 * @throws UnirestException 
	 */
	@Test(groups={"ut"})
	public void testWithBrowserStack() throws UnsupportedOperationException, IOException, UnirestException {
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstances(Arrays.asList("http://<user>:<key>@hub-cloud.browserstack.com/wd/hub")).get(0) instanceof BrowserStackGridConnector);
	}
}
