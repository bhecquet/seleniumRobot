package com.seleniumtests.ut.connectors.extools;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.openqa.selenium.Platform;

import static org.mockito.ArgumentMatchers.any;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.Files;
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
		File tmp = Files.createTempDir();
		File runner = Paths.get(tmp.getAbsolutePath(), "ban", "testrunner.bat").toFile();
		FileUtils.write(runner, "rem");
		
		PowerMockito.mockStatic(System.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		new SoapUi();	
	}
	
	@Test(groups={"ut"})
	public void testSoapUiInstallOk() throws IOException {
		File tmp = Files.createTempDir();
		File runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.bat").toFile();
		FileUtils.write(runner, "rem");
		
		PowerMockito.mockStatic(System.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		new SoapUi();	
	}
	
	@Test(groups={"ut"})
	public void testRunWithString() throws IOException {
		File tmp = Files.createTempDir();
		File runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.bat").toFile();
		FileUtils.write(runner, "rem");
		
		PowerMockito.mockStatic(System.class);
		PowerMockito.mockStatic(OSCommand.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		SoapUi soapui = new SoapUi();	
		when(OSCommand.executeCommandAndWait(any(String[].class))).thenReturn("ok");
		Assert.assertEquals(soapui.executeWithProjectString("<xml></xml>", "myproject"), "ok");
	}
	
	@Test(groups={"ut"})
	public void testRunWithProjectFile() throws IOException {
		File tmp = Files.createTempDir();
		File runner;
		if (OSUtility.getCurrentPlatorm() == Platform.WINDOWS) {
			runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.bat").toFile();
		} else {
			runner = Paths.get(tmp.getAbsolutePath(), "bin", "testrunner.sh").toFile();
		}
		FileUtils.write(runner, "rem");
		File project = Paths.get(tmp.getAbsolutePath(), "project.xml").toFile();
		FileUtils.write(project, "<xml></xml>");
		
		PowerMockito.mockStatic(System.class);
		PowerMockito.mockStatic(OSCommand.class);
		when(System.getenv("SOAPUI_HOME")).thenReturn(tmp.getAbsolutePath());
		SoapUi soapui = new SoapUi();	
		when(OSCommand.executeCommandAndWait(new String[] {runner.getAbsolutePath(), project.getAbsolutePath()})).thenReturn("ok");
		Assert.assertEquals(soapui.executeWithProjectFile(project), "ok");
	}
}
