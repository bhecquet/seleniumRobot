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
package com.seleniumtests.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;

public class PackageUtility {
	private static final Logger logger = SeleniumRobotLogger.getLogger(PackageUtility.class);
	
	private PackageUtility() {
		// nothing
	}

	/**
	 * Read pom file to extract version
	 * If version is not available, in pom, look for parent version
	 * @param pomFile
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	public static String getVersionFromPom(InputStream pomStream) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		
		// issue #113: corrects the error when executing integration tests
		Thread.currentThread().setContextClassLoader(PackageUtility.class.getClassLoader());
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {

    		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e) {
			logger.warn(e.getMessage());
		}
		Document doc = factory.newDocumentBuilder().parse(pomStream);
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
		
		// version not found, look for parent version
		version = (String) XPathFactory.newInstance()
				.newXPath()
				.compile("/project/parent/version")
				.evaluate(doc, XPathConstants.STRING);
		if (version != null) {
			version = version.trim();
			if (!version.isEmpty()) {
				return version;
			}
		}
		return null;
	}

	private static String getVersionFromPom(Class<?> clazz) {
		// Try to get version number from pom.xml (available in Eclipse)
		try {
			String className = clazz.getName();
			String classfileName = "/" + className.replace('.', '/') + ".class";
			URL classfileResource = clazz.getResource(classfileName);
			
			if (classfileResource != null) {
				Path absolutePackagePath = Paths.get(classfileResource.toURI()).getParent();
				int packagePathSegments = className.length() - className.replace(".", "").length();
				
				// Remove package segments from path, plus two more levels for "target/classes", which is the standard location for classes in Eclipse.
				Path path = absolutePackagePath;
				for (int i = 0, segmentsToRemove = packagePathSegments + 2; i < segmentsToRemove; i++) {
					path = path.getParent();
				}
				Path pom = path.resolve("pom.xml");
				try (InputStream is = Files.newInputStream(pom)) {
					return getVersionFromPom(is);
				}
			} 
			return null;

		} catch (Exception e) {
			return null;
		}
	}
	
	private static String getCoreVersionFromPom() {
		return getVersionFromPom(PackageUtility.class);
	}
	
	private static String getDriverVersionFromPom() {
		try {
			Class<?> helloDriverClass = Class.forName(String.format("fr.covea.seleniumRobot.driversdownload.HelloDriver%s", OSUtility.getCurrentPlatorm().toString().toLowerCase()));
			return getVersionFromPom(helloDriverClass);
		} catch (ClassNotFoundException e) {
			return null;
		}
		
	}
	
	private static String getVersionFromMetaInf(String propertiesFile) {
		// Try to get version number from maven properties in jar's META-INF
		try (InputStream is = PackageUtility.class.getResourceAsStream(propertiesFile)) {
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
	
	private static String getCoreVersionFromMetaInf() {
		return getVersionFromMetaInf("/META-INF/maven/com.infotel.seleniumRobot/core/pom.properties");
	}
	
	public static String getDriverVersionFromMetaInf() {
		return getVersionFromMetaInf(String.format("/META-INF/maven/com.infotel.seleniumRobot/seleniumRobot-%s-driver/pom.properties", OSUtility.getCurrentPlatorm().toString().toLowerCase()));
	}
	
	private static String getDriverVersionFromManifest() {
		String version = null;
		Package pkg;
		try {
			pkg = Class.forName(String.format("fr.covea.seleniumRobot.driversdownload.HelloDriver%s", OSUtility.getCurrentPlatorm().toString().toLowerCase())).getPackage();
		} catch (ClassNotFoundException e) {
			return null;
		}
		if (pkg != null) {
			version = pkg.getImplementationVersion();
			if (version == null) {
				version = pkg.getSpecificationVersion();
			}
		}
		return version;
	}
	
	private static String getVersionFromManifest() {
		String version = null;
		Package pkg = PackageUtility.class.getPackage();
		if (pkg != null) {
			version = pkg.getImplementationVersion();
			if (version == null) {
				version = pkg.getSpecificationVersion();
			}
		}
		return version;
	}
	
	/**
	 * Get core version
	 * @return
	 */
	public static final synchronized String getVersion() {
		
		String version = getCoreVersionFromPom();
		
		if (version == null) {
			version = getCoreVersionFromMetaInf();
		}
		if (version == null) {
			version = getVersionFromManifest();
		}

		version = version == null ? "" : version.trim();
		return version.isEmpty() ? "unknown" : version;
	}
	
	/**
	 * Get version associated to seleniumRobot-<os>-driver dependency
	 * @return
	 */
	public static final synchronized String getDriverVersion() {
		
		String version = getDriverVersionFromPom();
		
		if (version == null) {
			version = getDriverVersionFromMetaInf();
		}
		if (version == null) {
			version = getDriverVersionFromManifest();
		}
		
		version = version == null ? "" : version.trim();
		return version.isEmpty() ? "unknown" : version;
	}

	public static String getSeleniumVersion() {
		// use ChromeDriver because it's one of the sole selenium classes that are not embedded in core-selenium jar file
		// probably due to weaving
		URL url = ChromeDriver.class.getProtectionDomain().getCodeSource().getLocation();

        try {
            String jarFile = Paths.get(url.toURI()).toFile().getName();
			Pattern versionPattern = Pattern.compile("^.*?(4.\\d+.\\d+).*$");
			Matcher matcher = versionPattern.matcher(jarFile);
			if (matcher.find()) {
				return matcher.group(1);
			} else {
				logger.error("Unable to find version from jar file: {}, returns 4.37.0", jarFile);
				return "4.37.0";
			}
        } catch (URISyntaxException e) {
            logger.error("Cannot get selenium version, returns 4.37.0", e);
			return "4.37.0";
        }
    }
}
