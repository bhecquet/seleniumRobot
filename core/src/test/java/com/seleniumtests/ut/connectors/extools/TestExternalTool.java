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
package com.seleniumtests.ut.connectors.extools;

import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.ExternalTool;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.osutility.OSCommand;

//@PrepareForTest({ExternalTool.class, FileUtility.class, OSCommand.class})
public class TestExternalTool extends MockitoTest {
	
	@Mock
	Process process;
	
	@BeforeMethod(groups={"ut"})
	public void init() throws Exception {
//		PowerMockito.spy(ExternalTool.class);
//		PowerMockito.spy(FileUtility.class);
		
		Map<String, String> env = new HashMap<>();
		env.put("JAVA_HOME", "/usr/bin/java");
		env.put("SELENIUM_TOOL_MyTool", "/opt/mytool/mytool");
		env.put("SELENIUM_TOOL_MyOtherTool", "/opt/mytool/myothertool");
		
//		PowerMockito.when(ExternalTool.readEnvVariables()).thenReturn(env);
		
//		PowerMockito.when(FileUtility.fileExists("/opt/mytool/mytool")).thenReturn(true);
//		PowerMockito.when(FileUtility.fileExists("/opt/mytool/myothertool")).thenReturn(false);
		
	}
	
	@Test(groups={"ut"})
	public void testReadPrograms() {
		Map<String, String> tools = new ExternalTool().searchTools();
		Assert.assertEquals(tools.size(), 2);
		Assert.assertEquals(tools.get("MyTool"), "/opt/mytool/mytool");
	}
	
	/**
	 * Check error is raised when the program is not declared
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testProgramDoesNotExists() {
		new ExternalTool("prog_unkown");
	}
	
	/**
	 * Check error is raised when the program is declared but not installed
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testProgramNotInstalled() {
		new ExternalTool("MyOtherTool");
	}
	
	/**
	 * Check no error is raised when the program is declared and installed
	 */
	@Test(groups={"ut"})
	public void testProgramInstalled() {
		new ExternalTool("MyTool");
	}

	@Test(groups={"ut"})
	public void testStartProgram() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.when(OSCommand.executeCommand(new String[] {"/opt/mytool/mytool"})).thenReturn(process);
		
		ExternalTool tool = new ExternalTool("MyTool").start();
		Assert.assertTrue(tool.isStarted());
	}
	
	@Test(groups={"ut"})
	public void testStopProgram() {
//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.when(OSCommand.executeCommand(new String[] {"/opt/mytool/mytool"})).thenReturn(process);
		
		ExternalTool tool = new ExternalTool("MyTool").start().stop();
		Assert.assertFalse(tool.isStarted());
		
		verify(process).destroyForcibly();
	}

	/**
	 * Check we cannot start an already started program
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testStartStartedProgram() {

//		PowerMockito.mockStatic(OSCommand.class);
//		PowerMockito.when(OSCommand.executeCommand(new String[] {"/opt/mytool/mytool"})).thenReturn(process);
		
		new ExternalTool("MyTool").start().start();
	}
}
