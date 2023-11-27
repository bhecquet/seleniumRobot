package com.seleniumtests.ut.connectors.extools;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.Uft;
import com.seleniumtests.connectors.selenium.SeleniumRobotGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.reporter.logger.TestStep;

public class TestUft extends MockitoTest {

	@Mock
	SeleniumRobotGridConnector connector;

	private MockedStatic mockedTestTask;

	@BeforeMethod(groups= {"ut"})
	public void init() {
		mockedTestTask = mockStatic(TestTasks.class);
	}

	@AfterMethod(groups={"ut"}, alwaysRun = true)
	private void closeMocks() {
		mockedTestTask.close();
	}

	/**
	 * Check uft.vbs file is copied to temp folder test is stored locally
	 */
	@Test(groups = { "ut" })
	public void testPrepareArgumentsWithLocalTest() {
		Uft uft = new Uft("D:\\Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		List<String> args = uft.prepareArguments(true, true);

		Assert.assertEquals(args.size(), 4);
		Assert.assertTrue(args.get(0).startsWith(System.getProperty("java.io.tmpdir")));
		Assert.assertTrue(args.get(0).endsWith("uft.vbs"));
		Assert.assertTrue(args.get(1).equals("D:\\Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/execute"));
	}

	/**
	 * Check we add ALM parameters if they are set
	 */
	@Test(groups = { "ut" })
	public void testPrepareArgumentsWithAlmTest() {
		Uft uft = new Uft("http://almserver/qcbin", "usr", "pwd", "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		List<String> args = uft.prepareArguments(true, true);

		Assert.assertEquals(args.size(), 9);
		Assert.assertTrue(args.get(0).startsWith(System.getProperty("java.io.tmpdir")));
		Assert.assertTrue(args.get(0).endsWith("uft.vbs"));
		Assert.assertTrue(args.get(1).equals("[QualityCenter]Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/execute"));
		Assert.assertTrue(args.get(3).equals("/server:http://almserver/qcbin"));
		Assert.assertTrue(args.get(4).equals("/user:usr"));
		Assert.assertTrue(args.get(5).equals("/password:pwd"));
		Assert.assertTrue(args.get(6).equals("/domain:dom"));
		Assert.assertTrue(args.get(7).equals("/project:proj"));
		Assert.assertTrue(args.get(8).equals("/load"));;
	}
	
	/**
	 * Check we add ALM parameters if they are set on loading
	 */
	@Test(groups = { "ut" })
	public void testPrepareLoadArgumentsWithAlmTest() {
		Uft uft = new Uft("http://almserver/qcbin", "usr", "pwd", "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		List<String> args = uft.prepareArguments(true, false);
		
		Assert.assertEquals(args.size(), 8);
		Assert.assertTrue(args.get(0).startsWith(System.getProperty("java.io.tmpdir")));
		Assert.assertTrue(args.get(0).endsWith("uft.vbs"));
		Assert.assertTrue(args.get(1).equals("[QualityCenter]Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/server:http://almserver/qcbin"));
		Assert.assertTrue(args.get(3).equals("/user:usr"));
		Assert.assertTrue(args.get(4).equals("/password:pwd"));
		Assert.assertTrue(args.get(5).equals("/domain:dom"));
		Assert.assertTrue(args.get(6).equals("/project:proj"));
		Assert.assertTrue(args.get(7).equals("/load"));
	}
	
	/**
	 * If some ALM values are missing, throw a configuration exception
	 */
	@Test(groups = { "ut" }, expectedExceptions = ConfigurationException.class)
	public void testPrepareLoadArgumentsWithAlmTestMissingServerValues() {
		Uft uft = new Uft(null, "usr", "pwd", "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		uft.prepareArguments(true, false);
	}
	
	@Test(groups = { "ut" }, expectedExceptions = ConfigurationException.class)
	public void testPrepareLoadArgumentsWithAlmTestMissingUserValues() {
		Uft uft = new Uft("http://almserver/qcbin", null, "pwd", "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		uft.prepareArguments(true, false);
	}
	
	@Test(groups = { "ut" }, expectedExceptions = ConfigurationException.class)
	public void testPrepareLoadArgumentsWithAlmTestMissingPasswordValues() {
		Uft uft = new Uft("http://almserver/qcbin", "usr", null, "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		uft.prepareArguments(true, false);
	}
	@Test(groups = { "ut" }, expectedExceptions = ConfigurationException.class)
	public void testPrepareLoadArgumentsWithAlmTestMissingDomainValues() {
		Uft uft = new Uft("http://almserver/qcbin", "usr", "pwd", null, "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		uft.prepareArguments(true, false);
	}
	@Test(groups = { "ut" }, expectedExceptions = ConfigurationException.class)
	public void testPrepareLoadArgumentsWithAlmTestMissingProjectValues() {
		Uft uft = new Uft("http://almserver/qcbin", "usr", "pwd", "dom", null,
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		uft.prepareArguments(true, false);
	}
	
	@Test(groups = { "ut" })
	public void testPrepareLoadArgumentsWithKillOnStartup() {
		Uft uft = new Uft("D:\\Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(true);
		List<String> args = uft.prepareArguments(true, false);

		Assert.assertEquals(args.size(), 4);
		Assert.assertTrue(args.get(0).startsWith(System.getProperty("java.io.tmpdir")));
		Assert.assertTrue(args.get(0).endsWith("uft.vbs"));
		Assert.assertTrue(args.get(1).equals("D:\\Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/load"));
		Assert.assertTrue(args.get(3).equals("/clean"));
	}
	
	/**
	 * Check we do not add ALM parameters if they are set on execution
	 */
	@Test(groups = { "ut" })
	public void testPrepareExecutionArgumentsWithAlmTest() {
		Uft uft = new Uft("http://almserver/qcbin", "usr", "pwd", "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		List<String> args = uft.prepareArguments(true, false);
		
		Assert.assertEquals(args.size(), 8);
		Assert.assertTrue(args.get(0).startsWith(System.getProperty("java.io.tmpdir")));
		Assert.assertTrue(args.get(0).endsWith("uft.vbs"));
		Assert.assertTrue(args.get(1).equals("[QualityCenter]Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/server:http://almserver/qcbin"));
	}
	
	@Test(groups = { "ut" })
	public void testPrepareArgumentsWithAlmTestAndParam() {
		Map<String, String> params = new HashMap<>();
		params.put("User", "toto");
		Uft uft = new Uft("http://almserver/qcbin", "usr", "pwd", "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		
		uft.setParameters(params);
		List<String> args = uft.prepareArguments(true, true);

		Assert.assertEquals(args.size(), 11);
		Assert.assertTrue(args.get(0).startsWith(System.getProperty("java.io.tmpdir")));
		Assert.assertTrue(args.get(0).endsWith("uft.vbs"));
		Assert.assertTrue(args.get(1).equals("[QualityCenter]Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/execute"));
		Assert.assertTrue(args.get(3).equals("\"User=toto\""));
		Assert.assertTrue(args.get(4).equals("/server:http://almserver/qcbin"));
		Assert.assertTrue(args.get(5).equals("/user:usr"));
		Assert.assertTrue(args.get(6).equals("/password:pwd"));
		Assert.assertTrue(args.get(7).equals("/domain:dom"));
		Assert.assertTrue(args.get(8).equals("/project:proj"));
		Assert.assertTrue(args.get(9).equals("/load"));
		Assert.assertTrue(args.get(10).equals("/clean"));
		Assert.assertTrue(uft.isKillUftOnStartup());
	}
	
	@Test(groups = { "ut" })
	public void testPrepareArgumentsWithAlmTestAndParamAndClean() {
		Map<String, String> params = new HashMap<>();
		params.put("User", "toto");
		Uft uft = new Uft("http://almserver/qcbin", "usr", "pwd", "dom", "proj",
				"[QualityCenter]Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		uft.setParameters(params);
		List<String> args = uft.prepareArguments(true, true);
		
		Assert.assertEquals(args.size(), 10);
		Assert.assertTrue(args.get(0).startsWith(System.getProperty("java.io.tmpdir")));
		Assert.assertTrue(args.get(0).endsWith("uft.vbs"));
		Assert.assertTrue(args.get(1).equals("[QualityCenter]Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/execute"));
		Assert.assertTrue(args.get(3).equals("\"User=toto\""));
		Assert.assertTrue(args.get(4).equals("/server:http://almserver/qcbin"));
		Assert.assertTrue(args.get(5).equals("/user:usr"));
		Assert.assertTrue(args.get(6).equals("/password:pwd"));
		Assert.assertTrue(args.get(7).equals("/domain:dom"));
		Assert.assertTrue(args.get(8).equals("/project:proj"));
		Assert.assertTrue(args.get(9).equals("/load"));
	}
	
	/**
	 * Check that in grid mode, we load the file to grid node
	 */
	@Test(groups = { "ut" })
	public void testPrepareArgumentsForGrid() {
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(connector);
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		when(connector.uploadFileToNode(anyString(), eq(true))).thenReturn("D:\\file");
		
		Uft uft = new Uft("D:\\Subject\\Tools\\Tests\\test1");
		uft.setKillUftOnStartup(false);
		List<String> args = uft.prepareArguments(true, true);

		Assert.assertEquals(args.size(), 4);
		Assert.assertTrue(args.get(0).equals("D:\\file\\uft.vbs"));
		Assert.assertTrue(args.get(1).equals("D:\\Subject\\Tools\\Tests\\test1"));
		Assert.assertTrue(args.get(2).equals("/execute"));
		Assert.assertTrue(args.get(3).equals("/load"));
	}
	
	/**
	 * Test when grid is not present 
	 */
	@Test(groups = { "ut" }, expectedExceptions = ScenarioException.class)
	public void testPrepareArgumentsForGridNotThere() {
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(null);
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		when(connector.uploadFileToNode(anyString(), eq(true))).thenReturn("D:\\file\\uft.vbs");
		
		Uft uft = new Uft("D:\\Subject\\Tools\\Tests\\test1");
		uft.prepareArguments(true, true);
	}
	
	@Test(groups = { "ut" })
	public void testLoad() throws Exception {
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");
		report = "some comments\n_____OUTPUT_____\n" + report + "\n_____ENDOUTPUT_____\nsome other comments";
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);
		
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		uft.setParameters(args);
		uft.loadScript(false);
	
		ArgumentCaptor<String[]> argsArgument = ArgumentCaptor.forClass(String[].class);

		mockedTestTask.verify(() -> TestTasks.executeCommand(eq("cscript.exe"), eq(60), isNull(), argsArgument.capture()));
		
		// test parameters are not copied
		Assert.assertEquals(argsArgument.getAllValues().get(0).length, 3);
		Assert.assertEquals(argsArgument.getAllValues().get(0)[1], "[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		Assert.assertEquals(argsArgument.getAllValues().get(0)[2], "/load");
	}
	
	/**
	 * Check we cannot execute a test if it's not loaded
	 * @throws Exception
	 */
	@Test(groups = { "ut" }, expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Test script has not been loaded. Call 'loadScript' before")
	public void testExecuteNoLoad() throws Exception {
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");
		report = "some comments\n_____OUTPUT_____\n" + report + "\n_____ENDOUTPUT_____\nsome other comments";
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);

		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");

		uft.executeScript(5, new HashMap<>());		
	}
	
	/**
	 * Check we cannot execute the same test twice if loading has not been done before the second execution
	 * @throws Exception
	 */
	@Test(groups = { "ut" }, expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Test script has not been loaded. Call 'loadScript' before")
	public void testExecuteNoLoadBeforeSecondExecution() throws Exception {
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");
		report = "some comments\n_____OUTPUT_____\n" + report + "\n_____ENDOUTPUT_____\nsome other comments";
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);

		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		uft.loadScript(false);
		uft.executeScript(5, new HashMap<>());	
		
		// try a second execution, it's not possible
		uft.executeScript(5, new HashMap<>());		
	}
	
	@Test(groups = { "ut" })
	public void testExecute() throws Exception {
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");
		report = "some comments\n_____OUTPUT_____\n" + report + "\n_____ENDOUTPUT_____\nsome other comments";
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);
		
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		uft.loadScript(false);
		List<TestStep> testSteps = uft.executeScript(120, args);

		// check a step is returned
		Assert.assertNotNull(testSteps);
		Assert.assertEquals(testSteps.size(), 3);
		Assert.assertEquals(testSteps.get(0).getFiles().size(), 1);
		Assert.assertEquals(testSteps.get(0).getFiles().get(0).getName(), "Uft report");
		
		ArgumentCaptor<String[]> argsArgument = ArgumentCaptor.forClass(String[].class);

		mockedTestTask.verify(() -> TestTasks.executeCommand(eq("cscript.exe"), eq(120), isNull(), argsArgument.capture()));
		Assert.assertEquals(argsArgument.getAllValues().get(0).length, 4);

		Assert.assertEquals(argsArgument.getAllValues().get(0)[1], "[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		Assert.assertEquals(argsArgument.getAllValues().get(0)[2], "/execute");
		Assert.assertEquals(argsArgument.getAllValues().get(0)[3], "\"User=toto\"");
		
	}
	
	/**
	 * Test execution with an other file format
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testExecute2() throws Exception {
		String report = GenericTest.readResourceToString("tu/uftReport.xml");
		report = "some comments\n_____OUTPUT_____\n" + report + "\n_____ENDOUTPUT_____\nsome other comments";
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);
		
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		uft.loadScript(false);
		List<TestStep> testSteps = uft.executeScript(120, args);
		
		// check a step is returned
		Assert.assertNotNull(testSteps);
		
		ArgumentCaptor<String[]> argsArgument = ArgumentCaptor.forClass(String[].class);

		mockedTestTask.verify(() -> TestTasks.executeCommand(eq("cscript.exe"), eq(120), isNull(), argsArgument.capture()));
		Assert.assertEquals(argsArgument.getAllValues().get(0).length, 4);

		Assert.assertEquals(argsArgument.getAllValues().get(0)[1], "[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		Assert.assertEquals(argsArgument.getAllValues().get(0)[2], "/execute");
		Assert.assertEquals(argsArgument.getAllValues().get(0)[3], "\"User=toto\"");
		
	}
	
	/**
	 * Check the case where UFT produces an invalid file
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testExecuteWithReport2() throws Exception {
		String report = GenericTest.readResourceToString("tu/uftResult.txt");
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);
		
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		uft.loadScript(false);
		List<TestStep> testSteps = uft.executeScript(120, new HashMap<>());
		
		// check a step is returned
		Assert.assertNotNull(testSteps);
		Assert.assertEquals(testSteps.get(0).getName(), "UFT: test1");
		Assert.assertFalse(testSteps.toString().contains("<table>")); // check no HTML code is returned
		
	}
	
	/**
	 * Adding a character before XML report simulates the BOM
	 * @throws Exception
	 */
	@Test(groups = { "ut" })
	public void testExecuteWithBom() throws Exception {
		
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");
		report = "some comments\n_____OUTPUT_____B\n" + report + "\n_____ENDOUTPUT_____\nsome other comments";
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);
		
		
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		uft.loadScript(false);
		List<TestStep> testSteps = uft.executeScript(5, new HashMap<>());
		
		// check a step is returned
		Assert.assertNotNull(testSteps);
		Assert.assertEquals(testSteps.get(0).getName(), "UFT: DebutTest [DebutTest]");
	}
	
	@Test(groups = { "ut" })
	public void testExecuteNothingReturned() throws Exception {
		String report = "";
		mockedTestTask.when(() -> TestTasks.executeCommand(eq("cscript.exe"), anyInt(), nullable(Charset.class), any(String[].class))).thenReturn(report);
		
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1");
		uft.loadScript(false);
		List<TestStep> testSteps = uft.executeScript(5, args);
		
		// check a step is returned
		Assert.assertFalse(testSteps.isEmpty());
		Assert.assertEquals(testSteps.get(0).getName(), "UFT: test1");
	}

}