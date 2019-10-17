package com.seleniumtests.it.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.util.ide.SeleniumIdeLauncher;

public class TestSeleniumIdeLauncher extends ReporterTest {

	@Test(groups={"it"})
	public void testSeleniumExecution() throws IOException, ClassNotFoundException {
		try {
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			System.setProperty("foo", "Hello Selenium IDE");
			

			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MysuiteTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MysuiteTest.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()));
			
			String mainReportContent = readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='jcommander/TestReport.html' .*?>jcommander</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = readTestMethodResultFile("jcommander");
			Assert.assertTrue(detailedReportContent1.contains("Start method jcommander"));
			
			// check we have automatic steps corresponding to the single test method "jcommander"
			Assert.assertFalse(detailedReportContent1.contains("</button> Boolean link - ")); // manual step is not there
			Assert.assertTrue(detailedReportContent1.contains("><i class=\"fa fa-plus\"></i></button> jcommander  - ")); // auto step is there
			Assert.assertTrue(detailedReportContent1.contains("<i class=\"fa fa-plus\"></i></button> openPage with args: (null, ) - "));
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.linkText: 2.1. Boolean} </li>")); // action
			Assert.assertTrue(detailedReportContent1.contains("<li>seleniumhq </li>")); // auto sub-step
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.cssSelector: td:nth-child(2) .icon} </li>"));
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.linkText: Blog} </li>"));
			
			// test that user variable (set via command line in our test) is added to variabled available to script
			Assert.assertTrue(detailedReportContent1.contains("Sys$Out: Hello Selenium IDE"));
			
			
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
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
			System.setProperty(SeleniumTestsContext.BROWSER, "chrome");
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "true");
			
			
			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MysuiteTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MysuiteTest.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()));
			
			String mainReportContent = readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='jcommander/TestReport.html' .*?>jcommander</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = readTestMethodResultFile("jcommander");
			
			// manual step is present with details
			Assert.assertFalse(detailedReportContent1.contains("<li>click on HtmlElement , by={By.linkText: 2.1. Boolean} </li>")); // not there because created before the first step
			Assert.assertTrue(detailedReportContent1.contains("</button> Boolean link - "));
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.linkText: 21. Parameter delegates} </li>"));
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.cssSelector: td:nth-child(2) .icon} </li>"));
			Assert.assertTrue(detailedReportContent1.contains("<li>click on HtmlElement , by={By.linkText: Blog} </li>"));
			
			// screenshot is present for the step (taken at the beginning of the step: see anchor)
			Assert.assertTrue(detailedReportContent1.contains("<div class=\"message-snapshot\">Output 'main' browser: Current Window: JCommander: <a href='http://www.jcommander.org//#_boolean'"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
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
