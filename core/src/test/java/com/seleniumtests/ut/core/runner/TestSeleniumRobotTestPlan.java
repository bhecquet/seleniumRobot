/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
