package com.seleniumtests.it.connector.extools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.Uft;
import com.seleniumtests.reporter.logger.TestStep;

public class TestUft extends GenericTest {

	@Test(groups = { "it" }, enabled = true)
	public void testExecute() throws Exception {
		
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("<server>", "<user>", "<pwd>", "DEFAULT", "TEST_UFT", "[QualityCenter]Subject\\test\\GUITest1");
		uft.loadScript(false);
		List<TestStep> testSteps = uft.executeScript(120, args);
		
		// check a step is returned
		Assert.assertNotNull(testSteps);

		
	}
}
