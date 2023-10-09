package com.seleniumtests.it.util.ide;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.mockito.ArgumentMatchers;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.it.driver.support.server.WebServer;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.ide.SeleniumIdeLauncher;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import net.openhft.compiler.CompilerUtils;

/**
 * /!\ to make these tests work from your IDE, you MUST add a VM argument: "-javaagent:<path_to_maven_repo>\org\aspectj\aspectjweaver\1.9.1\aspectjweaver-1.9.1.jar" 
 * Test and compilation MUST also be executed by a JDK
 * When using java IDE scripts, make sure they have different names accross tests, else, we may encounter problems with class already being in classloader
 */
public class TestSeleniumIdeLauncher extends GenericTest {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(TestSeleniumIdeLauncher.class);
	private WebServer server;
	
	private  Map<String, String> getPageMapping() {
		Map<String, String> mapping = new HashMap<>();
		mapping.put("/tu/testWithoutFixedPattern.html", "/testWithoutFixedPattern.html");
		mapping.put("/tu/testIFrame.html", "/testIFrame.html");
		mapping.put("/tu/ffLogo1.png", "/ffLogo1.png");
		mapping.put("/tu/ffLogo2.png", "/ffLogo2.png"); 
		mapping.put("/tu/googleSearch.png", "/googleSearch.png");
		mapping.put("/tu/images/bouton_enregistrer.png", "/images/bouton_enregistrer.png");
		mapping.put("/tu/jquery.min.js", "/jquery.min.js");
		
		return mapping;
	}
	
	@BeforeClass(groups={"it", "ut"})
	public void exposeTestPage(final ITestContext testNGCtx) throws Exception {

		String localAddress = Inet4Address.getLocalHost().getHostAddress();
		server = new WebServer(localAddress, getPageMapping());
        server.expose(55555);
        logger.info(String.format("exposing server on http://%s:%d", localAddress, server.getServerHost().getPort()));
	}

	@AfterClass(groups={"it", "ut"}, alwaysRun=true)
	public void stop() throws Exception {
		if (server != null) {
			logger.info("stopping web server");
			server.stop();
		}
	}
	
	@Test(groups={"it"})
	public void testSeleniumExecution() throws IOException, ClassNotFoundException {
		try {
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			System.setProperty("foo", "Hello Selenium IDE");
			

			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MainPageTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTest.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()), 1);
			
			String mainReportContent = ReporterTest.readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<a href='mainPage/TestReport.html' info=\"ok\" .*?>mainPage</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = ReporterTest.readTestMethodResultFile("mainPage");
			try {
				Assert.assertTrue(detailedReportContent1.contains("Start method mainPage"));

				// check we have automatic steps corresponding to the single test method "jcommander"
				Assert.assertFalse(detailedReportContent1.contains("</button> new window link - ")); // manual step is not there
				Assert.assertTrue(detailedReportContent1.contains("><i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> mainPage  - ")); // auto step is there
				Assert.assertTrue(detailedReportContent1.contains("<i class=\"fas fa-plus\"></i></button><span class=\"step-title\"> openPage with args: (http://localhost:55555/testIFrame.html, ) - "));
				Assert.assertTrue(detailedReportContent1.contains("</span> click on HtmlElement , by={By.id: image}")); // action
				Assert.assertTrue(detailedReportContent1.contains("</span> frame")); // auto sub-step
				Assert.assertTrue(detailedReportContent1.contains("</span> click on HtmlElement , by={By.id: buttonIFrame}"));
				
				// test that user variable (set via command line in our test) is added to variabled available to script
				Assert.assertTrue(detailedReportContent1.contains("Hello Selenium IDE"));
			} catch (AssertionError e) {
				logger.error("------------------ Detailed report --------------");
				logger.error(detailedReportContent1);
				throw e;
			}
			
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			System.clearProperty("foo");
			
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	@Test(groups={"it"})
	public void testSeleniumExecutionMaskPassword() throws IOException, ClassNotFoundException {
		try {
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			System.setProperty("foo", "Hello Selenium IDE");
			
			
			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MainPageMaskPassword.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageMaskPassword.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()), 1);
			
			String mainReportContent = ReporterTest.readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<a href='mainPage/TestReport.html' info=\"ok\" .*?>mainPage</a>.*"));
			
			// check that detailed result does not display the password, it should be replaced with '******'
			String detailedReportContent1 = ReporterTest.readTestMethodResultFile("mainPage");
			Assert.assertTrue(detailedReportContent1.contains("sendKeys on HtmlElement , by={By.id: text2} with args: (true, true, [******,], )"));
			Assert.assertFalse(detailedReportContent1.contains("myPass"));
	
			
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			System.clearProperty("foo");
			
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	/**
	 * Test that with IE, test starts correctly (initial url is set)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test(groups={"it"})
	public void testSeleniumExecutionInternetExplorer() throws IOException, ClassNotFoundException {
		try {
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.BROWSER, "iexploreEdge");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			System.setProperty("foo", "Hello Selenium IDE");
			

			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MainPageTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTest.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()), 1);
			
			String mainReportContent = ReporterTest.readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<a href='mainPage/TestReport.html' info=\"ok\" .*?>mainPage</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = ReporterTest.readTestMethodResultFile("mainPage");

			Assert.assertTrue(detailedReportContent1.contains("Start method mainPage"));
			Assert.assertTrue(detailedReportContent1.contains("Test is OK"));
			Assert.assertTrue(detailedReportContent1.contains("Hello Selenium IDE"));

			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			System.clearProperty("foo");
			
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	/**
	 * Test that with IE, test starts correctly (initial url is set)
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test(groups={"it"}, enabled = false)
	public void testSeleniumIDE() throws IOException, ClassNotFoundException {
		try {
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.BROWSER, "iexploreEdge");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");

			new SeleniumIdeLauncher().executeScripts(Arrays.asList("D:\\tmp\\TestFumeur.java"), 1);
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			
		}
	}
	
	@Test(groups={"it"})
	public void testSeleniumExecutionParallel() throws IOException, ClassNotFoundException {
		try {
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			System.setProperty("foo", "Hello Selenium IDE");
			
			
			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MainPageTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTest2.java").toFile();
			File tmpSuiteFile2 = GenericTest.createFileFromResource("ti/ide/MainPageTest2.java");
			File suiteFile2 = Paths.get(tmpSuiteFile2.getParentFile().getAbsolutePath(), "MainPageTest3.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			FileUtility.copyFile(tmpSuiteFile2, suiteFile2);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath(), suiteFile2.getAbsolutePath()), 2);
			
			String mainReportContent = ReporterTest.readSummaryFile();
			
			// check that test are seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<a href='mainPage/TestReport.html' info=\"ok\" .*?>mainPage</a>.*"));
			Assert.assertTrue(mainReportContent.matches(".*<a href='mainPage-1/TestReport.html' info=\"ok\" .*?>mainPage-1</a>.*"));
			
			// check tests has been run in parallel
			String logs = ReporterTest.readSeleniumRobotLogFile();
			Assert.assertEquals(StringUtils.countMatches(logs, "Start creating *chrome driver"), 2);
			Assert.assertTrue(logs.contains("[TestNG-tests-1]")); // first thread
			Assert.assertTrue(logs.contains("[TestNG-tests-2]")); // second thread
			
			// check that driver creation is done before the end of each test, meaning they have been executed almost at the same time
			Assert.assertTrue(logs.indexOf("[TestNG-tests-2] WebUIDriver: Start creating *chrome driver") < logs.indexOf("[TestNG-tests-1] ScenarioLogger: Test is OK"));
			Assert.assertTrue(logs.indexOf("[TestNG-tests-1] WebUIDriver: Start creating *chrome driver") < logs.indexOf("[TestNG-tests-2] ScenarioLogger: Test is OK"));
			
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			System.clearProperty("foo");
			
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	/**
	 * Check that manual steps are displayed in report
	 * /!\ most of the time, this test fails during maven execution
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test(groups={"it"})
	public void testSeleniumExecutionWithManualSteps() throws IOException, ClassNotFoundException {
		try {
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "true");
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			
			// use a different file from the previous test to avoid problems with compiler cache
			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MainPageTest2.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTest4.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()), 1);
			
			String mainReportContent = ReporterTest.readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<a href='mainPage/TestReport.html' info=\"ok\" .*?>mainPage</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = ReporterTest.readTestMethodResultFile("mainPage");
			
			// manual step is present with details
			Assert.assertFalse(detailedReportContent1.contains("click on HtmlElement , by={By.id: image}")); // not there because created before the first step
			Assert.assertTrue(detailedReportContent1.contains("</button><span class=\"step-title\"> new window link - "));
			Assert.assertTrue(detailedReportContent1.contains("click on HtmlElement , by={By.id: buttonIFrame}"));
			
			// screenshot is present for the step (taken at the beginning of the step: see anchor)
			Assert.assertTrue(detailedReportContent1.matches(".*<div class\\=\"message-snapshot col\"><div class\\=\"text-center\">.*"
					+ "src\\=\"screenshots/mainPage_0-1_new_window_link.*<div class\\=\"text-center\">drv:main: Current Window: </div>"
					+ "<div class=\"text-center font-weight-lighter\"><a href='http://localhost:55555/testWithoutFixedPattern.html' target=url>URL</a>.*"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	/**
	 * Check an error is raised if browser is not specified
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test(groups={"it"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "'-Dbrowser=<browser>' option is mandatory")
	public void testSeleniumExecutionWithoutBrowser() throws IOException, ClassNotFoundException {
		try {
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "true");
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");
			
			// use a different file from the previous test to avoid problems with compiler cache
			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MainPageTest2.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTest5.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()), 1);

			
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	/**
	 * Test we can execute seleniumRobot test class and result files are correctly generated
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleExecution() throws ClassNotFoundException, IOException {
		try {
			String cls = "package covea.selenium.commons.tests;" + 
					"" + 
					"import com.seleniumtests.core.runner.SeleniumTestPlan;" + 
					"import org.testng.annotations.Test;" + 
					"" + 
					"public class Default extends SeleniumTestPlan {" + 
					"    " + 
					"    @Test" + 
					"    public void test() {" + 
					"        System.out.println(\"hello\");" + 
					"    }" + 
					"}";
			
			Map<String, String> clss = new HashMap<String, String>();
			clss.put("covea.selenium.commons.tests.Default", cls);
			
			new SeleniumIdeLauncher().executeGeneratedClasses(clss, 1);
			
			String mainReportContent = ReporterTest.readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<a href='test/TestReport.html' info=\"ok\" .*?>test</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = ReporterTest.readTestMethodResultFile("test");
			Assert.assertTrue(detailedReportContent1.contains("hello"));
		} finally {
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Test(groups={"it"})
	public void testParseIssue() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		try {		    
		    SeleniumIdeLauncher seleniumIde = new SeleniumIdeLauncher();
		    
		    Logger logger = spy(SeleniumRobotLogger.getLogger(SeleniumIdeLauncher.class));
		    Field loggerField = SeleniumIdeLauncher.class.getDeclaredField("logger");
		    loggerField.setAccessible(true);
		    loggerField.set(seleniumIde, logger);
		    
		
			CompilerUtils.addClassPath("target/test-classes");
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "true");
			System.setProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED, "false");

			// use a different file from the previous test to avoid problems with compiler cache
			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MainPageTestError.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTestError.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			try {
				seleniumIde.executeScripts(Arrays.asList(suiteFile.getAbsolutePath()), 1);
				Assert.assertFalse(true, "Exception should have been raised");
			} catch (ScenarioException e) {
				
			}
			
			verify(logger).error(ArgumentMatchers.contains("invalid code, one element is missing : "));


		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		}
	}
}
