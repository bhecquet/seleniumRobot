package com.seleniumtests.ut.core.runner;

import java.util.HashMap;
import java.util.Map;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.ExternalTool;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.util.FileUtility;

@PrepareForTest({ExternalTool.class, FileUtility.class})
public class TestSeleniumRobotTestPlan extends MockitoTest {

	@BeforeClass(groups={"ut"})
	public void init() throws Exception {
		PowerMockito.spy(ExternalTool.class);
		PowerMockito.spy(FileUtility.class);
		
		Map<String, String> env = new HashMap<>();
		env.put("SELENIUM_TOOL_MyTool", "/opt/mytool/mytool");
		
		PowerMockito.when(ExternalTool.readEnvVariables()).thenReturn(env);
		
		PowerMockito.when(FileUtility.fileExists("/opt/mytool/mytool")).thenReturn(true);
		
	}
	
	/**
	 * check that adding program from test adds it to context
	 */
	@Test(groups={"ut"})
	public void testUseProgram() {
		ExternalTool tool = new SeleniumRobotTestPlan().useProgram("MyTool");
		Assert.assertEquals(SeleniumTestsContextManager.getThreadContext().getExternalPrograms().size(), 1);
	}
}
