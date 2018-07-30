/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.seleniumtests.customexception.DriverExceptions;

public class PackageUtility {
	
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
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomStream);
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

	private static String getVersionFromPom() {
		// Try to get version number from pom.xml (available in Eclipse)
		try {
			String className = DriverExceptions.class.getName();
			String classfileName = "/" + className.replace('.', '/') + ".class";
			URL classfileResource = PackageUtility.class.getResource(classfileName);
			
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
	
	private static String getVersionFromMetaInf() {
		// Try to get version number from maven properties in jar's META-INF
		try (InputStream is = PackageUtility.class.getResourceAsStream("/META-INF/maven/com.infotel.seleniumRobot/core/pom.properties")) {
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
	
	public static final synchronized String getVersion() {
		
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
}
