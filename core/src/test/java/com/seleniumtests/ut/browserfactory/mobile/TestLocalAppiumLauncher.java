/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.ut.browserfactory.mobile;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.mobile.LocalAppiumLauncher;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.osutility.OSCommand;

@PrepareForTest({FileUtils.class, OSCommand.class, LocalAppiumLauncher.class, Paths.class})
public class TestLocalAppiumLauncher extends MockitoTest {
	
	@Mock
	Process nodeProcess;
	
	@Mock
	Path nodePath;
	
	@Mock
	File nodeFile;

	private void initValidAppiumInstallation() throws IOException {
		PowerMockito.mockStatic(FileUtils.class);
		PowerMockito.mockStatic(System.class);
		when(System.getenv("APPIUM_HOME")).thenReturn("/opt/appium/");
		when(FileUtils.readFileToString(new File("/opt/appium/node_modules/appium/.appiumconfig.json")))
					  .thenReturn("{\"git-sha\":\"c75d8adcb66a75818a542fe1891a34260c21f76a\",\"ios\":{\"version\":\"1.4.13\"},\"android\":{\"version\":\"1.4.13\"},\"selendroid\":{\"version\":\"1.4.13\"},\"firefoxos\":{\"version\":\"1.4.13\"},\"built\":\"2015-09-30T01:56:51.644Z\"}");
		
	}
	
	private void initValidNodeInstallation() {
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("node -v")).thenReturn("v6.2.1");
	}
	
	/**
	 * Test when appium home does not exist, an error is raised
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAppiumNotFound() {
		PowerMockito.mockStatic(System.class);
		when(System.getenv("APPIUM_HOME")).thenReturn(null);
		new LocalAppiumLauncher();
	}
	
	/**
	 * Test when appium_home exist, version found
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testAppiumFound() throws IOException {
		initValidAppiumInstallation();
		initValidNodeInstallation();
		LocalAppiumLauncher appium = new LocalAppiumLauncher();
		Assert.assertEquals(appium.getAppiumVersion(), "1.4.13");
	}
	
	
	/**
	 * Test when appium_home path does not contain a right appiumConfig file
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testAppiumFoundInvalid() throws IOException {
		PowerMockito.mockStatic(FileUtils.class);
		PowerMockito.mockStatic(System.class);
		PowerMockito.mockStatic(OSCommand.class);
		when(System.getenv("APPIUM_HOME")).thenReturn("/opt/appium/");
		when(FileUtils.readFileToString(new File("/opt/appium/node_modules/appium/.appiumconfig.json")))
					  .thenReturn("{\"git-sha\":\"c75d8adcb66a75818a542fe1891a34260c21f76a\"}");
		new LocalAppiumLauncher();
	}
	
	/**
	 * Test when node is found in system path
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testNodeFoundInSystemPath() throws IOException {
		initValidAppiumInstallation();
		
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("node -v")).thenReturn("v6.2.1");
		
		LocalAppiumLauncher appium = new LocalAppiumLauncher();
		Assert.assertEquals(appium.getNodeVersion(), "v6.2.1");
	}
	
	/**
	 * Test when node is not found in system path, an error is raised
	 * @throws IOException
	 */
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testNodeNotFoundInPath() throws IOException {
		initValidAppiumInstallation();
		
		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("node -v")).thenReturn("node command not found");
		new LocalAppiumLauncher();
	}
	
	/**
	 * !!! THIS METHOD is ignored as PowerMock cannot mock nio.Paths class !!! 
	 * 
	 * Test when node is found in appium path
	 * @throws IOException
	 */
	@Test(groups={"ut"}, enabled=false)
	public void testNodeFoundInAppiumPath() throws IOException {
		initValidAppiumInstallation();

		PowerMockito.mockStatic(OSCommand.class);
		when(OSCommand.executeCommandAndWait("/opt/appium/node -v")).thenReturn("v6.2.1");
		
		PowerMockito.mockStatic(Paths.class);
		
		when(Paths.get("/opt/appium/", "node")).thenReturn(nodePath);
		when(nodePath.toFile()).thenReturn(nodeFile);
		when(nodeFile.exists()).thenReturn(true);
		
		LocalAppiumLauncher appium = new LocalAppiumLauncher();
		Assert.assertEquals(appium.getNodeVersion(), "v6.2.1");
	}
	
	@Test(groups={"ut"})
	public void testAppiumStartup() throws IOException {
		initValidAppiumInstallation();
		initValidNodeInstallation();
		
		when(OSCommand.executeCommand("node /opt/appium//node_modules/appium/bin/appium.js --port 4723 ")).thenReturn(nodeProcess);
		
		LocalAppiumLauncher appium = new LocalAppiumLauncher();
		appium.setAppiumPort(4723);
		appium.startAppiumWithoutWait();
		
		Assert.assertEquals(appium.getAppiumProcess(), nodeProcess);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testAppiumStopWithoutStart() throws IOException {
		initValidAppiumInstallation();
		initValidNodeInstallation();
		
		LocalAppiumLauncher appium = new LocalAppiumLauncher();
		appium.stopAppium();
	}
	
	@Test(groups={"ut"})
	public void testAppiumStop() throws IOException {
		
		initValidAppiumInstallation();
		initValidNodeInstallation();
		
		when(OSCommand.executeCommand("node /opt/appium//node_modules/appium/bin/appium.js --port 4723 ")).thenReturn(nodeProcess);
		
		LocalAppiumLauncher appium = new LocalAppiumLauncher();
		appium.setAppiumPort(4723);
		appium.startAppiumWithoutWait();
		appium.stopAppium();
		Mockito.verify(nodeProcess).destroy();
	}
	
	@Test(groups={"ut"})
	public void testAppiumRandomPort() throws IOException {
		initValidAppiumInstallation();
		initValidNodeInstallation();
		LocalAppiumLauncher appium1 = new LocalAppiumLauncher();
		LocalAppiumLauncher appium2 = new LocalAppiumLauncher();
		Assert.assertNotEquals(appium1.getAppiumPort(), appium2.getAppiumPort());
	}
}
