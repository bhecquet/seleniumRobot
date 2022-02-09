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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Platform;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.extools.SoapUi;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;


@PrepareForTest({OSCommand.class, SoapUi.class})
public class TestSoapUi extends MockitoTest {

	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testNoSoapUiEnvVar() {
		PowerMockito.mockStatic(System.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(null);
		new SoapUi();	
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testNoSoapUiInstall() throws IOException {
		File tmp = Files.createTempDirectory("tmp").toFile();
		File runner = Paths.get(tmp.getAbsolutePath(), "ban", "testrunner.bat").toFile();
		FileUtils.write(runner, "rem", StandardCharsets.UTF_8);
		
		PowerMockito.mockStatic(System.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		new SoapUi();	
	}
	
	@Test(groups={"ut"})
	public void testSoapUiInstallOk() throws IOException {
		File tmp = Files.createTempDirectory("tmp").toFile();
		File runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.bat").toFile();
		FileUtils.write(runner, "rem", StandardCharsets.UTF_8);
		
		PowerMockito.mockStatic(System.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		new SoapUi();	
	}
	
	@Test(groups={"ut"})
	public void testRunWithString() throws IOException {
		File tmp = Files.createTempDirectory("tmp").toFile();
		File runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.bat").toFile();
		FileUtils.write(runner, "rem", StandardCharsets.UTF_8);
		
		PowerMockito.mockStatic(System.class);
		PowerMockito.mockStatic(OSCommand.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		SoapUi soapui = new SoapUi();	
		when(OSCommand.executeCommandAndWait(any(String[].class))).thenReturn("ok");
		Assert.assertEquals(soapui.executeWithProjectString("<xml></xml>", "myproject"), "ok");
	}
	
	@Test(groups={"ut"})
	public void testRunWithProjectFile() throws IOException {
		File tmp = Files.createTempDirectory("tmp").toFile();
		File runner;
		if (OSUtility.getCurrentPlatorm() == Platform.WINDOWS) {
			runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.bat").toFile();
		} else {
			runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.sh").toFile();
		}
		FileUtils.write(runner, "rem", StandardCharsets.UTF_8);
		File project = Paths.get(tmp.getAbsolutePath(), "project.xml").toFile();
		FileUtils.write(project, "<xml></xml>", StandardCharsets.UTF_8);
		
		PowerMockito.mockStatic(System.class);
		PowerMockito.mockStatic(OSCommand.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		SoapUi soapui = new SoapUi();	
		when(OSCommand.executeCommandAndWait(new String[] {runner.getAbsolutePath(), project.getAbsolutePath()})).thenReturn("ok");
		Assert.assertEquals(soapui.executeWithProjectFile(project), "ok");
	}
}
