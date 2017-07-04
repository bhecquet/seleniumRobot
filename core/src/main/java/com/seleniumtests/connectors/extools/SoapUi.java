package com.seleniumtests.connectors.extools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

public class SoapUi {
	
	private String soapUiPath;
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(SoapUi.class);
	
	public SoapUi() {
		checkInstallation();
	}
	
	private void checkInstallation() {
		soapUiPath = System.getenv("SOAPUI_HOME");
		if (soapUiPath == null) {
			throw new ConfigurationException("To use SoapUI, install it and create env var SOAPUI_HOME pointing to root installation path");
		}
		if (!Paths.get(soapUiPath, "bin").toFile().exists()) {
			throw new ConfigurationException(String.format("Path %s does not contain SOAP UI installation", soapUiPath));
		}
		
		if (OSUtility.getCurrentPlatorm() == Platform.WINDOWS) {
			soapUiPath = Paths.get(soapUiPath, "bin", "testrunner.bat").toString();
		} else {
			soapUiPath = Paths.get(soapUiPath, "bin", "testrunner.sh").toString();
		}
	}
	
	/**
	 * Start SOAP UI with a project file
	 * @param projectPath	path to project file
	 * @return				reply of service
	 */
	public String executeWithProjectFile(File projectFile) {
		logger.info("Running Soap UI with project file: " + projectFile.getAbsolutePath());
		return OSCommand.executeCommandAndWait(new String[] {soapUiPath, projectFile.getAbsolutePath()});
	}
	
	/**
	 * Start SOAP UI with a project file as string. Useful when project file is stored in test resource and we only access the stream
	 * @param projectString		project string
	 * @param projectName		project name
	 * @return
	 */
	public String executeWithProjectString(String projectString, String projectName) {
		File tmpFile;
		try {
			tmpFile = File.createTempFile("project-" + projectName, ".xml");
			FileUtils.writeStringToFile(tmpFile, projectString);
		} catch (IOException e) {
			throw new ScenarioException("Cannot write project file", e);
		}
		
		return executeWithProjectFile(tmpFile);
	}
}
