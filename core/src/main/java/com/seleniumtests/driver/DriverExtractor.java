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
package com.seleniumtests.driver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Platform;
import org.w3c.dom.Document;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtilityFactory;

/**
 * Extract drivers from resources so that they are available from IDE execution
 * without having need to extract them manually
 * 
 * @author behe
 *
 */
public class DriverExtractor {
	
	private static final String DRIVER_VERSION_FILE = "version_%s.txt";
	private static final String DRIVER_FOLDER = "drivers";
	private static final Logger logger = SeleniumRobotLogger.getLogger(DriverExtractor.class);
	private String rootPath;
	
	public DriverExtractor() {
		rootPath = SeleniumTestsContextManager.getRootPath();
	}
	
	public DriverExtractor(String rootPath) {
		this.rootPath = rootPath;
	}

	private String getVersionFromPom() {
		// Try to get version number from pom.xml (available in Eclipse)
		try {
			String className = getClass().getName();
			String classfileName = "/" + className.replace('.', '/') + ".class";
			URL classfileResource = getClass().getResource(classfileName);
			if (classfileResource != null) {
				Path absolutePackagePath = Paths.get(classfileResource.toURI()).getParent();
				int packagePathSegments = className.length() - className.replace(".", "").length();
				// Remove package segments from path, plus two more levels
				// for "target/classes", which is the standard location for
				// classes in Eclipse.
				Path path = absolutePackagePath;
				for (int i = 0, segmentsToRemove = packagePathSegments + 2; i < segmentsToRemove; i++) {
					path = path.getParent();
				}
				Path pom = path.resolve("pom.xml");
				try (InputStream is = Files.newInputStream(pom)) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
					doc.getDocumentElement().normalize();
					String version = (String) XPathFactory.newInstance()
														.newXPath()
														.compile("/project/version")
														.evaluate(doc, XPathConstants.STRING);
					if (version != null) {
						version = version.trim();
						if (!version.isEmpty()) {
							return version;
						}
					}
				}
			} 
			return null;

		} catch (Exception e) {
			return null;
		}
	}
	
	private String getVersionFromMetaInf() {
		// Try to get version number from maven properties in jar's META-INF
		try (InputStream is = getClass()
				.getResourceAsStream("/META-INF/maven/com.infotel.seleniumRobot/core/pom.properties")) {
			if (is != null) {
				Properties p = new Properties();
				p.load(is);
				String version = p.getProperty("version", "").trim();
				if (!version.isEmpty()) {
					return version;
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	private String getVersionFromManifest() {
		String version = null;
		Package pkg = getClass().getPackage();
		if (pkg != null) {
			version = pkg.getImplementationVersion();
			if (version == null) {
				version = pkg.getSpecificationVersion();
			}
		}
		return version;
	}
	
	public final synchronized String getVersion() {
		
		String version = getVersionFromPom();
		
		if (version == null) {
			version = getVersionFromMetaInf();
		}
		if (version == null) {
			version = getVersionFromManifest();
		}

		version = version == null ? "" : version.trim();
		return version.isEmpty() ? "unknown" : version;
	}
	
	private String getDriverVersion(String driverName) {
		try {
			return FileUtils.readFileToString(Paths.get(getDriverPath().toFile().getAbsolutePath(), getDriverVersionFileName(driverName)).toFile());
		} catch (IOException e) {
			return null;
		}
	}
	
	private String getDriverVersionFileName(String driverName) {
		return String.format(DRIVER_VERSION_FILE, driverName);
	}
	
	/**
	 * Extract drivers from JAR
	 * first, check if a driver directory is available in the same directory as the jar
	 * If not, extract
	 * If yes, check version of drivers and compare it to seleniumRobot version
	 * In case of difference, extract
	 * @throws IOException 
	 */
	public String extractDriver(String driverName) throws IOException {
		
		String driverVersion = getDriverVersion(driverName);
		String robotVersion = getVersion();
		Path driverPath = getDriverPath(driverName);
		
		if (driverPath.toFile().exists() && (driverVersion == null || !driverVersion.equals(robotVersion))
				|| !driverPath.toFile().exists()) {
			copyDriver(driverName);
		} 
		
		// write version file
		FileUtils.writeStringToFile(Paths.get(getDriverPath().toFile().getAbsolutePath(), getDriverVersionFileName(driverName)).toFile(), robotVersion);
		
		return driverPath.toString();
	}
	
	public void copyDriver(String driverName) {
		InputStream driver = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format("drivers/%s/%s%s", 
						Platform.getCurrent().family().toString().toLowerCase(), 
						driverName,
						OSUtilityFactory.getInstance().getProgramExtension()));
		
		if (driver == null) {
			throw new DriverExceptions(String.format("Driver %s does not exist in resources", driverName));
		}
		Path driverPath = getDriverPath(driverName);
		
		driverPath.toFile().getParentFile().mkdirs();
		
		try {
			Files.copy(driver, driverPath, StandardCopyOption.REPLACE_EXISTING);
			logger.info(String.format("Driver %s copied to %s", driverName, driverPath));
		} catch (IOException e) {
			logger.info(String.format("Driver not copied: %s - it may be in use", driverName));
		}
		
	}
	
	public Path getDriverPath() {
		return Paths.get(rootPath, DRIVER_FOLDER);
	}
	
	public Path getDriverPath(String driverName) {
		return Paths.get(rootPath, DRIVER_FOLDER, driverName + OSUtilityFactory.getInstance().getProgramExtension());
	}
}
