package com.seleniumtests.it.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.ide.SeleniumIdeParser;

public class TestSeleniumIdeParser extends GenericTest {

	/**
	 * Check we generate 2 classes
	 * - test class which only contains a call to WebPage
	 * - page class which contains selenium code
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testCodeGeneration() throws IOException {
		
		String testClassCode = "package com.infotel.selenium.ide;\n" + 
				"\n" + 
				"import java.io.IOException;\n" + 
				"import com.seleniumtests.core.runner.SeleniumTestPlan;\n" + 
				"import org.testng.annotations.Test;\n" + 
				"\n" + 
				"public class MysuiteTest extends SeleniumTestPlan {\n" + 
				"\n" + 
				"	@Test\n" + 
				"	public void jcommander() throws IOException {\n" + 
				"		new MysuiteTestPage().jcommander();\n" + 
				"	}\n" + 
				"\n" + 
				"}";
		
		String pageClassCode = "package com.infotel.selenium.ide;\n" + 
				"\n" + 
				"import java.io.IOException;\n" + 
				"\n" + 
				"import com.seleniumtests.uipage.PageObject;\n" + 
				"import org.openqa.selenium.JavascriptExecutor;\n" + 
				"import static org.testng.Assert.*;\n" + 
				"import static org.hamcrest.MatcherAssert.*;\n" + 
				"import static org.hamcrest.CoreMatchers.is;\n" + 
				"import static org.hamcrest.core.IsNot.not;\n" + 
				"import org.openqa.selenium.By;\n" + 
				"import org.openqa.selenium.Dimension;\n" + 
				"import org.openqa.selenium.WebElement;\n" + 
				"import org.openqa.selenium.interactions.Actions;\n" + 
				"import org.openqa.selenium.support.ui.ExpectedConditions;\n" + 
				"import org.openqa.selenium.support.ui.WebDriverWait;\n" + 
				"import org.openqa.selenium.JavascriptExecutor;\n" + 
				"import org.openqa.selenium.Alert;\n" + 
				"import org.openqa.selenium.Keys;\n" + 
				"import java.util.*;\n" + 
				"\n" + 
				"public class MysuiteTestPage extends PageObject {\n" + 
				"\n" + 
				"	private Map<String, Object> vars;\n" + 
				"	private JavascriptExecutor js;\n" + 
				"\n" + 
				"	public MysuiteTestPage() throws IOException {\n" + 
				"		super();\n" + 
				"		js = (JavascriptExecutor) driver;\n" + 
				"		vars = new HashMap<String, Object>();\n" + 
				"	}\n" + 
				"public void seleniumhq(){\n" + 
				"    driver.get(\"https://www.seleniumhq.org/\");\n" + 
				"    driver.manage().window().setSize(new Dimension(768, 683));\n" + 
				"    driver.findElement(By.cssSelector(\"td:nth-child(2) .icon\")).click();\n" + 
				"    driver.findElement(By.linkText(\"Blog\")).click();\n" + 
				"}\n" + 
				"\n" + 
				"public void jcommander(){\n" + 
				"    vars.put(\"toto\", \"coucou\");\n" + 
				"    driver.get(\"http://www.jcommander.org//\");\n" + 
				"    driver.manage().window().setSize(new Dimension(768, 683));\n" + 
				"    driver.findElement(By.linkText(\"2.1. Boolean\")).click();\n" + 
				"    driver.findElement(By.linkText(\"21. Parameter delegates\")).click();\n" + 
				"    seleniumhq();\n" + 
				"}\n" + 
				"\n" + 
				"}";
		
		
		File tmpSuiteFile = createFileFromResource("ti/ide/MysuiteTest.java");
		File suiteFile = Paths.get(tmpSuiteFile.getParentFile().getAbsolutePath(), "MysuiteTest.java").toFile();
		FileUtils.copyFile(tmpSuiteFile, suiteFile);
		
		Map<String, String> classInfo = new SeleniumIdeParser(suiteFile.getAbsolutePath()).parseSeleniumIdeFile();

		Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTest"));
		Assert.assertTrue(classInfo.containsKey("com.infotel.selenium.ide.MysuiteTestPage"));
		
		Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTest"), testClassCode);
		Assert.assertEquals(classInfo.get("com.infotel.selenium.ide.MysuiteTestPage"), pageClassCode);
	}
}
