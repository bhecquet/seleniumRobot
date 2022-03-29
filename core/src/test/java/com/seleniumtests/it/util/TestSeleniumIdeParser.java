package com.seleniumtests.it.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.ide.SeleniumIdeParser;

public class TestSeleniumIdeParser extends GenericTest {

	/**
	 * Check we generate 2 classes
	 * - test class which only contains a call to WebPage
	 * - page class which contains selenium code
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeGenerationAutomaticSteps() throws IOException {
		
		String testClassCode = "package com.infotel.selenium.ide;\n" + 
				"\n" + 
				"import java.io.IOException;\n" + 
				"import com.seleniumtests.core.runner.SeleniumTestPlan;\n" + 
				"import org.testng.annotations.Test;\n" + 
				"\n" + 
				"public class MysuiteTest extends SeleniumTestPlan {\n" + 
				"\n" + 
				"    @Test\n" + 
				"    public void jcommander() throws IOException {\n" + 
				"        new MysuiteTestPage().jcommander();\n" + 
				"    }\n" + 
				"\n" + 
				"}";
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MysuiteTest", "MysuiteTest") + 
				"public void seleniumhq(){\n" + 
				"    driver.get(\"https://www.seleniumhq.org/\");\n" + 
				"    driver.manage().window().setSize(new Dimension(1000, 683));\n" + 
				"    driver.findElement(By.linkText(\"Blog\")).click();\n" + 
				"}\n" + 
				"\n" + 
				"\n" + 
				"public void jcommander(){\n" + 
				"    vars.put(\"toto\", \"coucou\");\n" + 
			    "    logger.info(vars.get(\"foo\"));\n" +
				"    driver.get(\"http://www.jcommander.org//\");\n" + 
				"    driver.manage().window().setSize(new Dimension(768, 683));\n" + 
				"    driver.findElement(By.linkText(\"2.1. Boolean\")).click();\n" + 
				"    logger.info(\"STEP:Boolean link\");\n" +
				"    driver.findElement(By.linkText(\"21. Parameter delegates\")).click();\n" + 
				"    assertThat(driver.findElement(By.linkText(\"2.1. Boolean\")).getText(), is(\"2.1. Boolean\"));\n" +
				"    seleniumhq();\n" + 
				"}\n" + 
				"\n" + 
				"\n" + 
				"}";
		
		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			
			File tmpSuiteFile = createFileFromResource("ti/ide/MysuiteTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MysuiteTest.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			
			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();
	
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTest"));
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTestPage"));
			
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTest"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTestPage"), pageClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}
	
	/**
	 * Check echo "STEP:<step_name>" is replace is this case to generate manual step
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeGenerationManualSteps() throws IOException {
		
		String testClassCode = "package com.infotel.selenium.ide;\n" + 
				"\n" + 
				"import java.io.IOException;\n" + 
				"import com.seleniumtests.core.runner.SeleniumTestPlan;\n" + 
				"import org.testng.annotations.Test;\n" + 
				"\n" + 
				"public class MysuiteTest extends SeleniumTestPlan {\n" + 
				"\n" + 
				"    @Test\n" + 
				"    public void jcommander() throws IOException {\n" + 
				"        new MysuiteTestPage().jcommander();\n" + 
				"    }\n" + 
				"\n" + 
				"}";
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MysuiteTest", "MysuiteTest") + 
				"public void seleniumhq(){\n" + 
				"    driver.get(\"https://www.seleniumhq.org/\");\n" + 
				"    driver.manage().window().setSize(new Dimension(1000, 683));\n" + 
				"    driver.findElement(By.linkText(\"Blog\")).click();\n" + 
				"}\n" + 
				"\n" + 
				"\n" + 
				"public void jcommander(){\n" + 
				"    vars.put(\"toto\", \"coucou\");\n" + 
			    "    logger.info(vars.get(\"foo\"));\n" +
				"    driver.get(\"http://www.jcommander.org//\");\n" + 
				"    driver.manage().window().setSize(new Dimension(768, 683));\n" + 
				"    driver.findElement(By.linkText(\"2.1. Boolean\")).click();\n" + 
				"    addStep(\"Boolean link\");\n" +
				"    driver.findElement(By.linkText(\"21. Parameter delegates\")).click();\n" + 
				"    assertThat(driver.findElement(By.linkText(\"2.1. Boolean\")).getText(), is(\"2.1. Boolean\"));\n" +
				"    seleniumhq();\n" + 
				"}\n" + 
				"\n" + 
				"\n" + 
				"}";
		
		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "true");
			
			File tmpSuiteFile = createFileFromResource("ti/ide/MysuiteTest.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MysuiteTest.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			
			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();
			
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTest"));
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTestPage"));
			
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTest"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTestPage"), pageClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}
}
