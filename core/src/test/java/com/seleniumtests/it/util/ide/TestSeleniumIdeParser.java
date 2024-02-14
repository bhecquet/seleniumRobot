package com.seleniumtests.it.util.ide;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MysuiteTest", "MysuiteTest")
				.replace("https://initialurl.com", "https://www.seleniumhq.org/") +
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
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MysuiteTest", "MysuiteTest")
				.replace("https://initialurl.com", "https://www.seleniumhq.org/") +
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
	
	/**
	 * Check that WebDriverWait time unit is replaced
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeGenerationWebDriverWait() throws IOException {
		
		String testClassCode = "package com.infotel.selenium.ide;\n" + 
				"\n" + 
				"import java.io.IOException;\n" + 
				"import com.seleniumtests.core.runner.SeleniumTestPlan;\n" + 
				"import org.testng.annotations.Test;\n" + 
				"\n" + 
				"public class MysuiteTestWait extends SeleniumTestPlan {\n" + 
				"\n" + 
				"    @Test\n" + 
				"    public void jcommander() throws IOException {\n" + 
				"        new MysuiteTestWaitPage().jcommander();\n" + 
				"    }\n" + 
				"\n" + 
				"}";
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MysuiteTestWait", "MysuiteTestWait")
				.replace("https://initialurl.com", "https://www.seleniumhq.org/") +
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
				"    {\n" +
				"        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));\n" +
				"        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(\"bla\")));\n" +
				"    }\n" +
				"    driver.findElement(By.linkText(\"21. Parameter delegates\")).click();\n" + 
				"    assertThat(driver.findElement(By.linkText(\"2.1. Boolean\")).getText(), is(\"2.1. Boolean\"));\n" +
				"    seleniumhq();\n" + 
				"}\n" + 
				"\n" + 
				"\n" + 
				"}";
		
			File tmpSuiteFile = createFileFromResource("ti/ide/MysuiteTestWait.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MysuiteTestWait.java").toFile();
			FileUtility.copyFile(tmpSuiteFile, suiteFile);
			
			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();
			
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTestWait"));
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTestWaitPage"));
			
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTestWait"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTestWaitPage"), pageClassCode);
	}
	
	/**
	 * Test it's possible to call java code from Selenium IDE test, using an echo command 
	 * System.out.println("CALL:driver.findElement(By.linkText(vars.get("user").toString()));");
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeGenerationJavaCall() throws IOException {
		
		String testClassCode = "package com.infotel.selenium.ide;\n"
				+ "\n"
				+ "import java.io.IOException;\n"
				+ "import com.seleniumtests.core.runner.SeleniumTestPlan;\n"
				+ "import org.testng.annotations.Test;\n"
				+ "\n"
				+ "public class MainPageExternalCall extends SeleniumTestPlan {\n"
				+ "\n"
				+ "    @Test\n"
				+ "    public void mainPage() throws IOException {\n"
				+ "        new MainPageExternalCallPage().mainPage();\n"
				+ "    }\n"
				+ "\n"
				+ "}";
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MainPageExternalCall", "MainPageExternalCall")
				.replace("https://initialurl.com", "https://docs.python.org/3/library/operator.html") +
				"public void mainPage(){\n"
				+ "    driver.get(\"https://docs.python.org/3/library/operator.html\");\n"
				+ "    driver.manage().window().setSize(new Dimension(1150, 825));\n"
				+ "    vars.put(\"user\", \"myUser\");\n"
				+ "    driver.findElement(By.linkText(\"Lib/operator.py\")).click();\n"
				+ "    driver.findElement(By.linkText(vars.get(\"user\").toString()));\n"
				+ "}\n" 
				+ "\n" 
				+ "\n" 
				+ "}";
		
		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			
			File tmpSuiteFile = createFileFromResource("ti/ide/MainPageExternalCall.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageExternalCall.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();
	
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MainPageExternalCall"));
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MainPageExternalCallPage"));
			
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageExternalCall"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageExternalCallPage"), pageClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}
	
	/**
	 * Test it's possible to call java code from Selenium IDE test, using an echo command 
	 * Here, call parameters has escaped quotes
	 * System.out.println("CALL:driver.findElement(By.linkText(vars.get(\"user\").toString()));");
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeGenerationJavaCall2() throws IOException {
		
		String testClassCode = "package com.infotel.selenium.ide;\n"
				+ "\n"
				+ "import java.io.IOException;\n"
				+ "import com.seleniumtests.core.runner.SeleniumTestPlan;\n"
				+ "import org.testng.annotations.Test;\n"
				+ "\n"
				+ "public class MainPageExternalCall2 extends SeleniumTestPlan {\n"
				+ "\n"
				+ "    @Test\n"
				+ "    public void mainPage() throws IOException {\n"
				+ "        new MainPageExternalCall2Page().mainPage();\n"
				+ "    }\n"
				+ "\n"
				+ "}";
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MainPageExternalCall2", "MainPageExternalCall2")
				.replace("https://initialurl.com", "https://docs.python.org/3/library/operator.html") +
				"public void mainPage(){\n"
				+ "    driver.get(\"https://docs.python.org/3/library/operator.html\");\n"
				+ "    driver.manage().window().setSize(new Dimension(1150, 825));\n"
				+ "    vars.put(\"user\", \"myUser\");\n"
				+ "    driver.findElement(By.linkText(\"Lib/operator.py\")).click();\n"
				+ "    driver.findElement(By.linkText(vars.get(\"user\").toString()));\n"
				+ "}\n" 
				+ "\n" 
				+ "\n" 
				+ "}";
		
		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			
			File tmpSuiteFile = createFileFromResource("ti/ide/MainPageExternalCall2.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageExternalCall2.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();
			
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageExternalCall2"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageExternalCall2Page"), pageClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}
	
	
	/**
	 * Selenium IDE may generate this code:
	 * <code>
	 *     assertEquals(vars.get("dateAujourdhui").toString(), "vars.get("dateFin").toString()");
	 * </code>
	 * or
	 * <code>
	 *     assertThat(value, is("vars.get("dateDemain").toString()"));
	 * </code>
	 * or
	 * <code>
	 *     assertThat(driver.findElement(By.xpath("//div/input")).getText(), is("vars.get(\"stringVide\").toString()"));
	 * </code>
	 * Check we unquote the last "vars.get" and unescape escaped quotes
	 */
	@Test(groups={"it"})
	public void testCodeGenerationunquoteAssertVariables() throws IOException {
		
		String testClassCode = "package com.infotel.selenium.ide;\n"
				+ "\n"
				+ "import java.io.IOException;\n"
				+ "import com.seleniumtests.core.runner.SeleniumTestPlan;\n"
				+ "import org.testng.annotations.Test;\n"
				+ "\n"
				+ "public class MainPageVariableQuote extends SeleniumTestPlan {\n"
				+ "\n"
				+ "    @Test\n"
				+ "    public void mainPage() throws IOException {\n"
				+ "        new MainPageVariableQuotePage().mainPage();\n"
				+ "    }\n"
				+ "\n"
				+ "}";
		
		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MainPageVariableQuote", "MainPageVariableQuote")
				.replace("https://initialurl.com", "https://docs.python.org/3/library/operator.html") +
				"public void mainPage(){\n"
				+ "    driver.get(\"https://docs.python.org/3/library/operator.html\");\n"
				+ "    driver.manage().window().setSize(new Dimension(1150, 825));\n"
				+ "    vars.put(\"user\", \"myUser\");\n"
				+ "    driver.findElement(By.linkText(\"Lib/operator.py\")).click();\n"
				+ "    vars.put(\"dateFin\", driver.findElement(By.xpath(\"//td[8]/div/lds-datepicker/div/input\")).getAttribute(\"value\"));\n"
				+ "    vars.put(\"dateAujourdhui\", js.executeScript(\"return new Date().toLocaleDateString('fr-FR');\"));\n"
				+ "    assertEquals(vars.get(\"dateAujourdhui\").toString(), vars.get(\"dateFin\").toString());\n"
				+ "    assertThat(\"\", is(vars.get(\"dateDemain\").toString()));\n"
				+ "    assertThat(\"\", is(vars.get(\"immatriculation_1\").toString()));\n"
				+ "    assertThat(driver.findElement(By.xpath(\"//div/input\")).getText(), is(vars.get(\"stringVide\").toString()));\n"
				+ "}\n"
				+ "\n"
				+ "\n"
				+ "}";
		
		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");
			
			File tmpSuiteFile = createFileFromResource("ti/ide/MainPageVariableQuote.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageVariableQuote.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);
			
			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();
			
			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MainPageVariableQuotePage"));
			
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageVariableQuote"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageVariableQuotePage"), pageClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}

	/**
	 * Check that if code is parsed ok but does not compile, a stub test class is generated
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeParseButCannotCompile() throws IOException {

		String testClassCode = "package com.infotel.selenium.ide;\n" +
				"\n" +
				"import java.io.IOException;\n" +
				"import com.seleniumtests.core.runner.SeleniumTestPlan;\n" +
				"import org.testng.annotations.Test;\n" +
				"import org.testng.Assert;\n" +
				"\n" +
				"public class MainPageTestCannotCompile extends SeleniumTestPlan {\n" +
				"\n" +
				"\t @Test\n" +
				"    public void testMainPageTestCannotCompile() {\n" +
				"        Assert.assertFalse(true, \"com.infotel.selenium.ide.MainPageTestCannotCompilePage class cannot be compiled, code may be invalid. See generation logs for details\");\n" +
				"    }\n" +
				"}\n";

		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");

			File tmpSuiteFile = createFileFromResource("ti/ide/MainPageTestCannotCompile.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTestCannotCompile.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);

			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();

			// when code cannot compile, page class is not provided
			Assert.assertFalse(classInfo.containsKey("com.infotel.selenium.ide.MainPageTestCannotCompilePage"));

			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageTestCannotCompile"), testClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}

	/**
	 * If code cannot be parsed, a stub class is generated
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeCannotParse() throws IOException {

		String testClassCode = "package com.infotel.selenium.ide;\n" +
				"\n" +
				"import java.io.IOException;\n" +
				"import com.seleniumtests.core.runner.SeleniumTestPlan;\n" +
				"import org.testng.annotations.Test;\n" +
				"import org.testng.Assert;\n" +
				"\n" +
				"public class MainPageTestError extends SeleniumTestPlan {\n" +
				"\n" +
				"\t @Test\n" +
				"    public void testMainPageTestError() {\n" +
				"        Assert.assertFalse(true, \"(line 68,col 61) Parse error. Found  \\\"driver\\\" <IDENTIFIER>, expected one of  \\\"%=\\\" \\\"&=\\\" \\\"*=\\\" \\\"++\\\" \\\"+=\\\" \\\"--\\\" \\\"-=\\\" \\\"/=\\\" \\\";\\\" \\\"<<=\\\" \\\"=\\\" \\\">>=\\\" \\\">>>=\\\" \\\"^=\\\" \\\"|=\\\"\");\n" +
				"    }\n" +
				"}\n";

		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");

			File tmpSuiteFile = createFileFromResource("ti/ide/MainPageTestError.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageTestError.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);

			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();

			// when code cannot compile, page class is not provided
			Assert.assertFalse(classInfo.containsKey("com.infotel.selenium.ide.MainPageTestErrorPage"));

			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageTestError"), testClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}

	/**
	 * assertThat(driver.findElement(By.xpath("//h2[@class=\"slds-text-heading_small\"]")).getText(), is("Veuillez renseigner l\\\'exhaustivitÃ© "));
	 * becomes
	 * assertThat(driver.findElement(By.xpath("//h2[@class=\"slds-text-heading_small\"]")).getText(), is("Veuillez renseigner l'exhaustivitÃ© "));
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeGenerationUnescapeSingleQuote() throws IOException {

		String testClassCode = "package com.infotel.selenium.ide;\n"
				+ "\n"
				+ "import java.io.IOException;\n"
				+ "import com.seleniumtests.core.runner.SeleniumTestPlan;\n"
				+ "import org.testng.annotations.Test;\n"
				+ "\n"
				+ "public class MainPageSimpleQuoteEscape extends SeleniumTestPlan {\n"
				+ "\n"
				+ "    @Test\n"
				+ "    public void mainPage() throws IOException {\n"
				+ "        new MainPageSimpleQuoteEscapePage().mainPage();\n"
				+ "    }\n"
				+ "\n"
				+ "}";

		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MainPageSimpleQuoteEscape", "MainPageSimpleQuoteEscape")
				.replace("https://initialurl.com", "https://docs.python.org/3/library/operator.html") +
				"public void mainPage(){\n"
				+ "    driver.get(\"https://docs.python.org/3/library/operator.html\");\n"
				+ "    driver.manage().window().setSize(new Dimension(1150, 825));\n"
				+ "    vars.put(\"user\", \"myUser\");\n"
				+ "    driver.findElement(By.linkText(\"Lib/operator.py\")).click();\n"
				+ "    vars.put(\"dateFin\", driver.findElement(By.xpath(\"//td[8]/div/lds-datepicker/div/input\")).getAttribute(\"value\"));\n"
				+ "    vars.put(\"dateAujourdhui\", js.executeScript(\"return new Date().toLocaleDateString('fr-FR');\"));\n"
				+ "    assertThat(driver.findElement(By.xpath(\"//h2[@class=\\\"slds-text-heading_small\\\"]\")).getText(), is(\"Veuillez renseigner l'exhaustivitÃ© \"));\n"
				+ "}\n"
				+ "\n"
				+ "\n"
				+ "}";

		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");

			File tmpSuiteFile = createFileFromResource("ti/ide/MainPageSimpleQuoteEscape.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageSimpleQuoteEscape.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);

			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();

			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MainPageSimpleQuoteEscape"));

			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageSimpleQuoteEscape"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageSimpleQuoteEscapePage"), pageClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}

	@Test(groups={"it"})
	public void testCodeGenerationVariableInXPath() throws IOException {

		String testClassCode = "package com.infotel.selenium.ide;\n"
				+ "\n"
				+ "import java.io.IOException;\n"
				+ "import com.seleniumtests.core.runner.SeleniumTestPlan;\n"
				+ "import org.testng.annotations.Test;\n"
				+ "\n"
				+ "public class MainPageSimpleXPathContent extends SeleniumTestPlan {\n"
				+ "\n"
				+ "    @Test\n"
				+ "    public void mainPage() throws IOException {\n"
				+ "        new MainPageSimpleXPathContentPage().mainPage();\n"
				+ "    }\n"
				+ "\n"
				+ "}";

		String pageClassCode = String.format(SeleniumIdeParser.PAGE_OBJECT_HEADER, "MainPageSimpleXPathContent", "MainPageSimpleXPathContent")
				.replace("https://initialurl.com", "https://docs.python.org/3/library/operator.html") +
				"public void mainPage(){\n"
				+ "    driver.get(\"https://docs.python.org/3/library/operator.html\");\n"
				+ "    driver.manage().window().setSize(new Dimension(1150, 825));\n"
				+ "    vars.put(\"user\", \"myUser\");\n"
				+ "    driver.findElement(By.linkText(\"Lib/operator.py\")).click();\n"
				+ "    vars.put(\"dateFin\", driver.findElement(By.xpath(\"//td[8]/div/lds-datepicker/div/input\")).getAttribute(\"value\"));\n"
				+ "    vars.put(\"dateAujourdhui\", js.executeScript(\"return new Date().toLocaleDateString('fr-FR');\"));\n"
				+ "    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));\n"
				+ "    // vars.get in xpath expression\n"
				+ "    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(\"//table//tr[contains(td[1], '\" + vars.get(\"immatriculation\").toString() + \"') and contains(td[2], 'AUDI') and contains(td[3], 'AFRem') and contains(td[4], 'SITE') and contains(td[5], 'FR') and contains(td[6], 'OUI')]\")));\n"
				+ "    vars.put(\"nbVHOK\", driver.findElements(By.xpath(\"//tr/td[8][(translate(concat(substring(., 8, 4), '-', substring(., 5, 2), '-',  substring(., 2, 2)),  '-', '') >= translate(concat(substring('\" + vars.get(\"dateFinGarantie\").toString() + \"', 7, 4), '-',  substring('\" + vars.get(\"firstVariable\").toString() + \"', 4, 2), '-',  substring('\" + vars.get(\"secondVariable\").toString() + \"', 1, 2)), '-', '')) or not(normalize-space()) or normalize-space()]\")).size());\n"
				+ "}\n"
				+ "\n"
				+ "\n"
				+ "}";

		try {
			System.setProperty(SeleniumTestsContext.MANUAL_TEST_STEPS, "false");

			File tmpSuiteFile = createFileFromResource("ti/ide/MainPageSimpleXPathContent.java");
			File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MainPageSimpleXPathContent.java").toFile();
			FileUtils.copyFile(tmpSuiteFile, suiteFile);

			Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();

			Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MainPageSimpleXPathContent"));

			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageSimpleXPathContent"), testClassCode);
			Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MainPageSimpleXPathContentPage"), pageClassCode);
		} finally {
			System.clearProperty(SeleniumTestsContext.MANUAL_TEST_STEPS);
		}
	}
}
