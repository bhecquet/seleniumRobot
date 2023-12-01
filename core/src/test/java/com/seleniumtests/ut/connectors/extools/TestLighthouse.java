package com.seleniumtests.ut.connectors.extools;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.Lighthouse;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.SystemUtility;
import org.apache.commons.io.FileUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TestLighthouse extends MockitoTest {

	@Captor
	private ArgumentCaptor<String[]> lighthouseArguments;

	@Mock
	private SeleniumGridConnector connector;

	private MockedStatic mockedTestTasks;
	private MockedStatic mockedSystem;

	@BeforeMethod(groups = "ut", alwaysRun = true)
	public void init() {

		mockedTestTasks = mockStatic(TestTasks.class);
		mockedSystem = mockStatic(SystemUtility.class);

		// by default, say that lighthouse in installed
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq("--help"))).thenReturn("      --chrome-flags                 Custom flags to pass to Chrome (space-delimited). For a full list of flags, see https://bit.ly/chrome-flags\r\n"
				+ "\r\n"
				+ "                                          Additionally, use the CHROME_PATH environment variable to use a specific Chrome binary. Requires Chromium vers\r\n"
				+ "                                     ion 66.0 or later. If omitted, any detected Chrome Canary or Chrome stable will be used.\r\n"
				+ "                                                                                                                    [chaîne de caractères] [défaut : \"\"]\r\n"
				+ "      --port                         The port to use for the debugging protocol. Use 0 for a random port                           [nombre] [défaut : 0]\r\n"
				+ "      --hostname                     The hostname to use for the debugging protocol.");

		mockedSystem.when(() -> SystemUtility.getenv("LIGHTHOUSE_HOME")).thenReturn(null);
	}

	@AfterMethod(groups = "ut", alwaysRun = true)
	private void closeMocks() {
		mockedTestTasks.close();
		mockedSystem.close();
	}


	/**
	 * If we detect '--port', it means lighthouse is installed
	 */
	@Test(groups = "ut")
	public void testLighthouseInstalled() {
		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		Assert.assertTrue(lighthouse.isAvailable());
	}

	@Test(groups = "ut")
	public void testLighthouseNotInstalled() {
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq("--help"))).thenReturn("Unknown program");

		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		Assert.assertFalse(lighthouse.isAvailable());
	}


	@Test(groups = "ut")
	public void testLighthouseNotFound() {
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq(OSCommand.USE_PATH + "lighthouse"), eq(90), isNull(), eq("--help")))
				.thenReturn("No program found");

		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		boolean available = lighthouse.isAvailable();
		Assert.assertFalse(available);

	}

	@Test(groups = "ut")
	public void testCheckLighthouseHomeIsntOnPath() throws IOException {
		mockedSystem.when(() -> SystemUtility.getenv("LIGHTHOUSE_HOME")).thenReturn("/test/lighthouse/");

		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		Assert.assertFalse(lighthouse.isLighthouseInPath());
	}

	@Test(groups = "ut")
	public void testCheckLighthouseHomeIsOnPath() throws IOException {
		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		Assert.assertTrue(lighthouse.isLighthouseInPath());
	}

	@Test(groups = "ut")
	public void testExecuteLighthouseIndexWithPath() throws IOException {
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq(new String[]{})))
				.thenReturn("mocked output with path");

		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		String out = lighthouse.executeLighthouse(new ArrayList<>());

		Assert.assertEquals(out, "mocked output with path");
	}

	@Test(groups = "ut")
	public void testExecuteLighthouseIndexWithoutPath() throws IOException {
		mockedSystem.when(() -> SystemUtility.getenv("LIGHTHOUSE_HOME")).thenReturn("/test/lighthouse/");

		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_node"), eq(90), isNull(), eq("/test/lighthouse/index.js")))
				.thenReturn("mocked output without path");

		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		String out = lighthouse.executeLighthouse(new ArrayList<>());

		Assert.assertEquals(out, "mocked output without path");
	}

	private File createFilesForExecution() throws IOException {
		// simulate execution
		File resultFolder = Files.createTempDirectory("lighthouse").toFile();
		resultFolder.deleteOnExit();
		File jsonReport = resultFolder.toPath().resolve("out.report.json").toFile();
		FileUtils.write(jsonReport, "{}", StandardCharsets.UTF_8);
		jsonReport.deleteOnExit();
		File htmlReport = resultFolder.toPath().resolve("out.report.html").toFile();
		FileUtils.write(htmlReport, "<html>", StandardCharsets.UTF_8);
		htmlReport.deleteOnExit();
		return resultFolder;
	}

	@Test(groups = "ut")
	public void testLighthouseExecute() throws IOException {
		File resultFolder = createFilesForExecution();

		String[] args = new String[4];
		args[0] = "http://myurl.com";
		args[1] = "--port=1234";
		args[2] = "--output=html,json";
		args[3] = "--output-path=" + resultFolder.toPath().resolve("out").toString();

		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq("--help"))).thenReturn("--port");
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq(args[0]), eq(args[1]), eq(args[2]), eq(args[3]))).thenReturn("json output written to");

		Lighthouse lighthouse = new Lighthouse(1234, resultFolder.toPath().resolve("out").toString());
		lighthouse.execute("http://myurl.com", new ArrayList<>());

		mockedTestTasks.verify(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), any()));

		// execution successful => no logs
		Assert.assertNull(lighthouse.getLogs());

		// check files has been moved
		Assert.assertNotNull(lighthouse.getJsonReport());
		Assert.assertTrue(lighthouse.getJsonReport().canRead());
		Assert.assertEquals(FileUtils.readFileToString(lighthouse.getJsonReport(), StandardCharsets.UTF_8), "{}");
		logger.info("chemin fichier: " + lighthouse.getJsonReport().toString());
		Assert.assertTrue(lighthouse.getJsonReport().toString().replace(File.separator, "/").contains("testLighthouseExecute/lighthouse/http.myurl.com"));
		Assert.assertNotNull(lighthouse.getHtmlReport());
		Assert.assertTrue(lighthouse.getHtmlReport().canRead());
		Assert.assertEquals(FileUtils.readFileToString(lighthouse.getHtmlReport(), StandardCharsets.UTF_8), "<html>");
		Assert.assertTrue(lighthouse.getHtmlReport().toString().replace(File.separator, "/").contains("testLighthouseExecute/lighthouse/http.myurl.com"));
	}


	@Test(groups = "ut")
	public void testLighthouseExecuteOnGrid() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(connector);

		when(connector.downloadFileFromNode(anyString())).thenReturn(File.createTempFile("light", ".dat"), File.createTempFile("light", ".dat"));

		String[] args = new String[4];
		args[0] = "http://myurl.com";
		args[1] = "--port=1234";
		args[2] = "--output=html,json";
		args[3] = "--output-path=upload/lighthouseOut";

		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq("--help"))).thenReturn("--port");
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq(args[0]), eq(args[1]), eq(args[2]), eq(args[3]))).thenReturn("json output written to");

		Lighthouse lighthouse = new Lighthouse(1234, "upload/lighthouseOut");
		lighthouse.execute("http://myurl.com", new ArrayList<>());

		verify(connector).downloadFileFromNode("upload/lighthouseOut.report.json");
		verify(connector).downloadFileFromNode("upload/lighthouseOut.report.html");
	}

	@Test(groups = "ut")
	public void testLighthouseExecuteFailed() throws IOException {
		File resultFolder = createFilesForExecution();

		String[] args = new String[4];
		args[0] = "http://myurl.com";
		args[1] = "--port=1234";
		args[2] = "--output=html,json";
		args[3] = "--output-path=" +  resultFolder.toPath().resolve("out").toString();

		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq("--help"))).thenReturn("--port");
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), eq(args[0]), eq(args[1]), eq(args[2]), eq(args[3]))).thenReturn("LH:CriConnection:error sendRawMessage() was called without an established connection");


		Lighthouse lighthouse = new Lighthouse(1234, resultFolder.toPath().resolve("out").toString());
		lighthouse.execute("http://myurl.com", new ArrayList<>());

		// execution failed => logs
		Assert.assertNotNull(lighthouse.getLogs());
		Assert.assertTrue(lighthouse.getLogs().canRead());
		Assert.assertEquals(FileUtils.readFileToString(lighthouse.getLogs(), StandardCharsets.UTF_8), "LH:CriConnection:error sendRawMessage() was called without an established connection");

		// check files has been moved
		Assert.assertNull(lighthouse.getJsonReport());
		Assert.assertNull(lighthouse.getHtmlReport());
	}

	/**
	 * Error raised when Lighthouse is not installed
	 *
	 * @throws IOException
	 */
	@Test(groups = "ut", expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Lighthouse not available")
	public void testLighthouseExecuteNotAvailable() throws IOException {

		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(5), isNull(), eq("--help"))).thenReturn("Unknown program");
		mockedTestTasks.when(() -> TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), any())).thenReturn("json output written to file");

		File resultFolder = createFilesForExecution();

		Lighthouse lighthouse = new Lighthouse(1234, resultFolder.toPath().resolve("out").toString());
		lighthouse.execute("http://myurl.com", new ArrayList<>());
	}
}
