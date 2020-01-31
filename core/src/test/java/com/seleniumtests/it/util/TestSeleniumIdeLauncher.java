package com.seleniumtests.it.util;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.driver.support.server.WebServer;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.ide.SeleniumIdeLauncher;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import net.openhft.compiler.CompilerUtils;

/**
 * /!\ to make these tests work from your IDE, you MUST add a VM argument: "-javaagent:<path_to_maven_repo>\org\aspectj\aspectjweaver\1.9.1\aspectjweaver-1.9.1.jar" 
 * 
 *
 */
public class TestSeleniumIdeLauncher extends ReporterTest {
	
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
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()));
			
			String mainReportContent = readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='mainPage/TestReport.html' .*?>mainPage</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = readTestMethodResultFile("mainPage");
			Assert.assertTrue(detailedReportContent1.contains("Start method mainPage"));
			
			// check we have automatic steps corresponding to the single test method "jcommander"
			Assert.assertFalse(detailedReportContent1.contains("</button> new window link - ")); // manual step is not there
			Assert.assertTrue(detailedReportContent1.contains("><i class=\"fa fa-plus\"></i></button> mainPage  - ")); // auto step is there
			Assert.assertTrue(detailedReportContent1.contains("<i class=\"fa fa-plus\"></i></button> openPage with args: (null, ) - "));
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.id: image} </li>")); // action
			Assert.assertTrue(detailedReportContent1.contains("<li>frame </li>")); // auto sub-step
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.id: buttonIFrame} </li>"));
			
			// test that user variable (set via command line in our test) is added to variabled available to script
			Assert.assertTrue(detailedReportContent1.contains("Sys$Out: Hello Selenium IDE"));
			
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
			System.clearProperty("foo");
		}
	}
	
	/**
	 * Check that manual steps are displayed in report
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
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTest2.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()));
			
			String mainReportContent = readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='mainPage/TestReport.html' .*?>mainPage</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = readTestMethodResultFile("mainPage");
			
			// manual step is present with details
			Assert.assertFalse(detailedReportContent1.contains("<li>click on HtmlElement , by={By.id: image} </li>")); // not there because created before the first step
			Assert.assertTrue(detailedReportContent1.contains("</button> new window link - "));
			Assert.assertTrue(detailedReportContent1.contains("li>click on HtmlElement , by={By.id: buttonIFrame} </li>"));
			
			// screenshot is present for the step (taken at the beginning of the step: see anchor)
			Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-snapshot\">Output 'main' browser: Current Window: : <a href='http://localhost:55555/testWithoutFixedPattern.html'"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
			System.clearProperty(SeleniumTestsContext.SOFT_ASSERT_ENABLED);
		}
	}
	
	/**
	 * Test we can execute seleniumRobot test class and result files are correctly generated
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleExecution() throws ClassNotFoundException, IOException {
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
		
		new SeleniumIdeLauncher().executeGeneratedClasses(clss);
		
		String mainReportContent = readSummaryFile();
		
		// check that test is seen and OK
		Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='test/TestReport.html' .*?>test</a>.*"));
		
		// check that detailed result contains the "hello" written in test
		String detailedReportContent1 = readTestMethodResultFile("test");
		Assert.assertTrue(detailedReportContent1.contains("hello"));
		
	}
}
