package com.seleniumtests.ut.connectors.extools;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.Lighthouse;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ScenarioException;

@PrepareForTest({TestTasks.class})
public class TestLighthouse extends MockitoTest {

	@Captor
	private ArgumentCaptor<String[]> lighthouseArguments;
	
	@Mock
	private SeleniumGridConnector connector;

	@BeforeMethod(groups="ut", alwaysRun = true)
	public void init() {
		PowerMockito.mockStatic(TestTasks.class);
		
		// by default, say that lighthouse in installed
		PowerMockito.when(TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(5), isNull(), eq("--help"))).thenReturn("      --chrome-flags                 Custom flags to pass to Chrome (space-delimited). For a full list of flags, see https://bit.ly/chrome-flags\r\n"
				+ "\r\n"
				+ "                                          Additionally, use the CHROME_PATH environment variable to use a specific Chrome binary. Requires Chromium vers\r\n"
				+ "                                     ion 66.0 or later. If omitted, any detected Chrome Canary or Chrome stable will be used.\r\n"
				+ "                                                                                                                    [chaîne de caractères] [défaut : \"\"]\r\n"
				+ "      --port                         The port to use for the debugging protocol. Use 0 for a random port                           [nombre] [défaut : 0]\r\n"
				+ "      --hostname                     The hostname to use for the debugging protocol.");
		
	}
	
	/**
	 * If we detect '--port', it means lighthouse is installed
	 */
	@Test(groups="ut")
	public void testLighthouseInstalled() {
		
		
		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		Assert.assertTrue(lighthouse.isAvailable());
	}
	
	@Test(groups="ut")
	public void testLighthouseNotInstalled() {
		
		PowerMockito.when(TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(5), isNull(), eq("--help"))).thenReturn("Unknown program");
		
		Lighthouse lighthouse = new Lighthouse(1234, "/home/selenium/out");
		Assert.assertFalse(lighthouse.isAvailable());
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
	
	@Test(groups="ut")
	public void testLighthouseExecute() throws IOException {
		
		PowerMockito.when(TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), any())).thenReturn("json output written to file");
		
		File resultFolder = createFilesForExecution();
		
		Lighthouse lighthouse = new Lighthouse(1234, resultFolder.toPath().resolve("out").toString());
		lighthouse.execute("http://myurl.com", new ArrayList<>());
		
		PowerMockito.verifyStatic(TestTasks.class);
		TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), lighthouseArguments.capture());
		Assert.assertEquals(lighthouseArguments.getAllValues().size(), 4);
		Assert.assertEquals(lighthouseArguments.getAllValues().get(0), "http://myurl.com");
		Assert.assertEquals(lighthouseArguments.getAllValues().get(1), "--port=1234");
		Assert.assertEquals(lighthouseArguments.getAllValues().get(2), "--output=html,json");
		
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
	

	@Test(groups="ut")
	public void testLighthouseExecuteOnGrid() throws IOException {
		
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(connector);
		
		when(connector.downloadFileFromNode(anyString())).thenReturn(File.createTempFile("light", ".dat"), File.createTempFile("light", ".dat"));
		
		PowerMockito.when(TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), any())).thenReturn("json output written to file");
		
		File resultFolder = createFilesForExecution();
		
		Lighthouse lighthouse = new Lighthouse(1234, "upload/lighthouseOut");
		lighthouse.execute("http://myurl.com", new ArrayList<>());
		
		verify(connector).downloadFileFromNode("upload/lighthouseOut.report.json");
		verify(connector).downloadFileFromNode("upload/lighthouseOut.report.html");
	}
	
	@Test(groups="ut")
	public void testLighthouseExecuteFailed() throws IOException {
		
		PowerMockito.when(TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), any())).thenReturn("LH:CriConnection:error sendRawMessage() was called without an established connection");
		
		File resultFolder = createFilesForExecution();
		
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
	 * @throws IOException
	 */
	@Test(groups="ut", expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Lighthouse not available")
	public void testLighthouseExecuteNotAvailable() throws IOException {
		
		PowerMockito.when(TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(5), isNull(), eq("--help"))).thenReturn("Unknown program");
		PowerMockito.when(TestTasks.executeCommand(eq("_USE_PATH_lighthouse"), eq(90), isNull(), any())).thenReturn("json output written to file");
		
		File resultFolder = createFilesForExecution();
		
		Lighthouse lighthouse = new Lighthouse(1234, resultFolder.toPath().resolve("out").toString());
		lighthouse.execute("http://myurl.com", new ArrayList<>());
		
	}
}
