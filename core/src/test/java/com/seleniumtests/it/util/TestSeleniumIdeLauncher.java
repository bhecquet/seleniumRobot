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
			

			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/MysuiteTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MysuiteTest.java").toFile();
//			File tmpSuiteFile = GenericTest.createFileFromResource("ti/ide/HelloWorld.java");
//			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "HelloWorld.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			new SeleniumIdeLauncher().executeScripts(Arrays.asList(suiteFile.getAbsolutePath()));
			
			String mainReportContent = readSummaryFile();
			
			// check that test is seen and OK
			Assert.assertTrue(mainReportContent.matches(".*<i class=\"fa fa-circle circleSuccess\"></i><a href='jcommander/TestReport.html' .*?>jcommander</a>.*"));
			
			// check that detailed result contains the "hello" written in test
			String detailedReportContent1 = readTestMethodResultFile("jcommander");
			Assert.assertTrue(detailedReportContent1.contains("Start method jcommander"));
			
		} finally {
			System.clearProperty(SeleniumTestsContext.BROWSER);
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
