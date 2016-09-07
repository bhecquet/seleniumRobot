package com.seleniumtests.it.core.aspects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.runner.SeleniumRobotRunner;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.WebUIDriver;

public class TestStepLogging extends GenericTest {
	
	@BeforeClass(groups={"it"})
	public void init() {
		System.setProperty("browser", "none");
	}
	
	@AfterClass(groups={"it"})
	public void teardown() {
		System.clearProperty("browser");
		WebUIDriver.cleanUp();
	}
	
	@BeforeMethod(groups={"it"})
	public void reset() {
		Reporter.clear();
		SeleniumRobotRunner.setCucumberTest(false);
	}
	
	/**
	 * get reporter output and replace 
	 * @@lt@@ by '<'
	 * ^^greaterThan^^ by '>'
	 * for better readability
	 * @return
	 */
	private List<String> getFormattedOutput() {
		List<String> output = new ArrayList<>();
		for (String s: Reporter.getOutput()) {
			output.add(s.replaceAll("@@lt@@", "<").replace("^^greaterThan^^", ">"));
		}
		return output;
	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - add
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleNonCucumberStepLogging() throws IOException {
		new CalcPage()
				.add(1, 1);
		List<String> outputs = getFormattedOutput();
		Assert.assertEquals(outputs.get(0), "<li> <b>openPage with args: (null, )</b>");
		Assert.assertEquals(outputs.get(1), "</li>");
		Assert.assertEquals(outputs.get(2), "<li> <b>add with args: (1, 1, )</b>");
	}
	
	/**
	 * Only test presence of steps with cucumber annotations
	 * - page opening
	 * 		- addC		=> first interception by calling addC: never happens in real cucumber test
	 * 			- addC  => cucumber annotation interception
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSimpleCucumberStepLogging() throws IOException {
		SeleniumRobotRunner.setCucumberTest(true);
		new CalcPage()
			.addC(1, 1);
		List<String> outputs = getFormattedOutput();
		Assert.assertEquals(outputs.get(0), "<li> <b>openPage with args: (null, )</b>");
		Assert.assertEquals(outputs.get(1), "</li>");
		
		// check step name replacement (first interception by calling addC
		Assert.assertEquals(outputs.get(2), "<li> <b>add '(\\d+)' to '(\\d+)' with args: (1, 1, )</b>"); 
		
		// second interception because this method is annotated via cucumber. First interception should
		// not occur in real cucumber test because addC would never be directly called
		Assert.assertEquals(outputs.get(4), "<li> <b>add '(\\d+)' to '(\\d+)' with args: (1, 1, )</b>"); 
	}
	
	/**
	 * Only test presence of root steps. there should be:
	 * - page opening
	 * - add
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testFailedStep() throws IOException {
		try {
			new CalcPage()
				.failAction();
		} catch (DriverExceptions e) {
			// continue;
		}
		List<String> outputs = getFormattedOutput();
		Assert.assertEquals(outputs.get(0), "<li> <b>openPage with args: (null, )</b>");
		Assert.assertEquals(outputs.get(2), "<span style=\"font-weight:bold;color:#cc0052;\"><li><b>FailedStep</b>: <b>failAction </b></span>");
	}
	
	/**
	 * Check presence of sub steps. These are methods defined in Page object but not directly called from main test. We should get
	 * - page opening
	 * - add(1, 1)
	 * 		- nothing
	 * 			- doNothing on HtmlElement none
	 * - add(2)											=> step
	 * 		- add (2, 2)								=> sub-step
	 * 			- donothing								=> sub-step
	 * 				- doNothing on HtmlElement none		=> action on element
	 * Checks that root steps are correctly intercepted
	 * @throws IOException
	 */
	@Test(groups={"it"})
	public void testSubStepsNonCucumberStepLogging() throws IOException {
		new CalcPage()
					.add(1, 1)
					.add(2);
		List<String> outputs = getFormattedOutput();
		Assert.assertEquals(outputs.get(2), "<li> <b>add with args: (1, 1, )</b>");
		Assert.assertEquals(outputs.get(11), "<li> <b>add with args: (2, )</b>");
		Assert.assertEquals(outputs.get(12), "<ul>");		
		Assert.assertEquals(outputs.get(13), "<li> <b>add with args: (2, 2, )</b>");
		Assert.assertEquals(outputs.get(14), "<ul>");
		Assert.assertEquals(outputs.get(15), "<li> <b>doNothing </b>");
		Assert.assertEquals(outputs.get(16), "<ul>");
		Assert.assertEquals(outputs.get(17), "<li> doNothing on HtmlElement none, by={By.id: none} </li>");
		
		// closing of lists
		Assert.assertEquals(outputs.get(18), "</ul>");
		Assert.assertEquals(outputs.get(19), "</li>");
		Assert.assertEquals(outputs.get(20), "</ul>");
		Assert.assertEquals(outputs.get(21), "</li>");
	}
}
