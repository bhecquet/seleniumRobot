/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.connectors.selenium;


import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.List;

import com.seleniumtests.connectors.selenium.*;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.customexception.ConfigurationException;

import kong.unirest.GetRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import static org.mockito.Mockito.*;

public class TestSeleniumGridConnectorFactory extends ConnectorsTest {
	
	@Mock
	private GetRequest gRequest;

	private final String guiServletContent = """
			<html>
				<head>
						<link href='/grid/resources/templates/css/report.css' rel='stylesheet' type='text/css' />
						<script src="/grid/resources/templates/js/status.js"></script>
						<link href="/grid/resources/templates/css/hubCss.css" rel="stylesheet" type="text/css">
						<link href="/grid/resources/templates/css/bootstrap.min.css" rel="stylesheet">
						<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
						<script src="/grid/resources/templates/css/bootstrap.min.js"></script>
						<meta http-equiv="X-UA-Compatible" content="IE=edge">
						<link rel="icon" href="https://d30y9cdsu7xlg0.cloudfront.net/png/1248-200.png">
						<title>Selenium Robot</title>
				</head>
			
				<body>
						<header>
								<a ><img src="/grid/resources/templates/img/seleniumlogo_low.png" alt="selenium" id="selenium"></a>
								<p id="titre" >Infotel</p>
						</header>
			
			
				<article>
			
						<h1 id="hub">Hub Status</h1>"
			 "</article>
			
			
			<footer>
					<a href="#" class="haut"><img src="/grid/resources/templates/img/up.png" alt="haut" id="haut"></a>
			</footer>
			
				</body>
			
			</html>
			""";
	
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
	 */
	@Test(groups={"ut"})
	public void testWithSeleniumRobotGrid() throws UnsupportedOperationException, UnirestException {

		createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, guiServletContent);	
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);			
		
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstances(List.of(SERVER_URL + "/wd/hub")).get(0) instanceof SeleniumRobotGridConnector);
	}
	
	/**
	 * Check that with several selenium grid URL (seleniumRobot or pure selenium), several grid connectors are created, one for each URL
	 * if they are all accessible
	 */
	@Test(groups={"ut"})
	public void testWithSeveralSeleniumRobotGrid() throws UnsupportedOperationException, UnirestException {

		// SeleniumRobot grid
		createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 200, guiServletContent);		
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);	

		// Selenium grid
		GetRequest getRequest = (GetRequest)createServerMock("http://localhost:4431", "GET", SeleniumRobotGridConnector.GUI_SERVLET, 404, "default monitoring page");
		when(getRequest.asString()).thenThrow(new UnirestException(new SocketException("permission denied")));
		createServerMock("http://localhost:4421", "GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);
		
		List<SeleniumGridConnector> gridConnectors = SeleniumGridConnectorFactory.getInstances(List.of(SERVER_URL + "/wd/hub", "http://localhost:4421/wd/hub"));
		
		Assert.assertEquals(gridConnectors.size(), 2);
		Assert.assertTrue(gridConnectors.get(0) instanceof SeleniumRobotGridConnector);
		Assert.assertTrue(gridConnectors.get(1) instanceof SeleniumGridConnector);
	}
	
	/**
	 * Check that grid connectors are only created if the selenium grid is accessible (replies before timeout)
	 */
	@Test(groups={"ut"})
	public void testWithSeveralSeleniumGridNotAllThere() throws UnsupportedOperationException, UnirestException {
				
		// only the second grid replies
		GetRequest getRequest = (GetRequest)createServerMock("http://localhost:4431", "GET", SeleniumRobotGridConnector.GUI_SERVLET, 404, "default monitoring page");
		when(getRequest.asString()).thenThrow(new UnirestException(new SocketException("permission denied")));
		createServerMock("http://localhost:4421", "GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);
		
		// 2 grid URL given
		List<SeleniumGridConnector> gridConnectors = SeleniumGridConnectorFactory.getInstances(List.of(SERVER_URL + "/wd/hub", "http://localhost:4421/wd/hub"));
		
		Assert.assertEquals(gridConnectors.size(), 1);
		Assert.assertTrue(gridConnectors.get(0) instanceof SeleniumGridConnector);
	}
	
	/**
	 * If servlet GuiServlet is not available, we get a SeleniumGridConnector
	 */
	@Test(groups={"ut"})
	public void testWithSeleniumGrid() throws UnsupportedOperationException, UnirestException {


		GetRequest getRequest = (GetRequest)createGridServletServerMock("GET", SeleniumRobotGridConnector.GUI_SERVLET, 404, "default monitoring page");
		when(getRequest.asString()).thenThrow(new UnirestException(new SocketException("permission denied")));
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 200, consoleServletContent);
		
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstances(List.of(SERVER_URL + "/wd/hub")).get(0) instanceof SeleniumGridConnector);
	}
	
	/**
	 * If status code is not 200, throw an error
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithErrorCodeHttp() throws UnsupportedOperationException, UnirestException {
		SeleniumGridConnectorFactory.setRetryTimeout(1);
		createServerMock("GET", SeleniumGridConnector.CONSOLE_SERVLET, 404, "default monitoring page");	
		
		SeleniumGridConnectorFactory.getInstances(List.of(SERVER_URL + "/wd/hub"));
	}
	
	/**
	 * If any error occurs when getting servlet, throw an error, grid cannot be contacted
	 * Check also that we retry connection during N seconds (defined by retryTimeout)
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testWithError() throws UnsupportedOperationException, UnirestException {
		
		when(Unirest.get(SERVER_URL + SeleniumGridConnector.CONSOLE_SERVLET)).thenReturn(gRequest);
		when(gRequest.asString()).thenThrow(UnirestException.class);

		LocalDateTime start = LocalDateTime.now();
		try {

			SeleniumGridConnectorFactory.setRetryTimeout(5);
			SeleniumGridConnectorFactory.getInstances(List.of(SERVER_URL + "/wd/hub"));
		} catch (ConfigurationException e) {

			mockedUnirest.get().verify(() -> Unirest.get(SERVER_URL + SeleniumGridConnector.CONSOLE_SERVLET), atLeast(5));

			// check connection duration
			Assert.assertTrue(LocalDateTime.now().minusNanos(4500000).isAfter(start));
			throw e;
		}
	}
	

	/**
	 * If URL contains browserstack, returns a browserstack connector
	 */
	@Test(groups={"ut"})
	public void testWithBrowserStack() throws UnsupportedOperationException, UnirestException {
		Assert.assertTrue(SeleniumGridConnectorFactory.getInstances(List.of("http://user:key@hub-cloud.browserstack.com/wd/hub")).get(0) instanceof SeleniumBrowserstackGridConnector);
	}
}
