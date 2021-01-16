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
package com.seleniumtests.util.squashta;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;

import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TaScriptGenerator {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(TaScriptGenerator.class);
	
	private String srcPath;
	private String dstPath;
	private String application;
	
	// patterns for exclusion from .ta script generation
	private static final String XML_EXCLUDE = "EXCLUDE_FROM_SQUASH_TA";
	private static final String FEATURE_EXCLUDE = "@EXCLUDE_FROM_SQUASH_TA";	

	/**
	 * 
	 * @param srcPath		Path to root of the test application. it's the location of the "data" folder
	 * @param dstPath		Path to root where files will be written
	 * @param application	name of the application
	 */
	public TaScriptGenerator(String application, String srcPath, String dstPath) {
		this.srcPath = srcPath.replace("\\", "/");
		this.dstPath = dstPath.replace("\\", "/");
		this.application = application;
	}
	
	/**
	 * Search for scenarios in feature files
	 * @param path
	 * @param application
	 * @return
	 */
	public List<String> parseFeatures() {
		
		Pattern scenarioPattern = Pattern.compile("^\\s*Scenario:(.*)");
		Pattern scenarioOutlinePattern = Pattern.compile("^\\s*Scenario Outline:(.*)");
		
		// look for feature file into data folder
		File dir = Paths.get(srcPath, "data", application, "features").toFile();
		if (!dir.exists()) {
			return new ArrayList<>();
		}
		
		File[] featureFiles = dir.listFiles((d, filename) -> filename.endsWith(".feature"));
    	
    	List<String> scenarios = new ArrayList<>();
    	for (File featureFile: featureFiles) {
    		try {
    			boolean exclude = false;
				for (String line: FileUtils.readLines(featureFile, "UTF-8")) {
					
					// exclude scenarios using tag @EXCLUDE_FROM_SQUASH_TA 
					if (line.contains(FEATURE_EXCLUDE)) {
						exclude = true;
					}
					
					Matcher matcher = scenarioPattern.matcher(line);
					if (matcher.matches()) {
						if (!exclude) {
							scenarios.add(matcher.group(1).trim());
						}
						exclude = false;
					}
					Matcher matcherOutline = scenarioOutlinePattern.matcher(line);
					if (matcherOutline.matches()) {
						if (!exclude) {
							scenarios.add(matcherOutline.group(1).trim());
						}
						exclude = false;
					}
				}
			} catch (IOException e) {
				// ignore
			}
    	}
    	return scenarios;
    	
	}
	
	/**
	 * Read a test element in an XML testNG file
	 * @param test			the test element to read
	 * @param testDefs		list of test definitions to update
	 * @param testngFile	testNgFile read
	 */
	private void readTestTag(Element test, List<SquashTaTestDef> testDefs, File testngFile) {
		boolean cucumberTest = false;
    	String cucumberNamedTest = "";
    	boolean exclude = false;
    	
    	// search cucumber parameters among test parameters
    	// does test specifies precise cucumber properties (cucumberTests / cucumberTags)
    	for (Element param: test.getChildren("parameter")) {
    		if ("cucumberTests".equals(param.getAttributeValue("name")) || "cucumberTags".equals(param.getAttributeValue("name"))) {
    			cucumberTest = true;
    			cucumberNamedTest = param.getAttributeValue("value");
    			
    			if (!cucumberNamedTest.isEmpty()) {
    				break;
    			}
    		} 
    	}
    	
    	for (Element param: test.getChildren("parameter")) {
    		if (XML_EXCLUDE.equals(param.getAttributeValue("name"))) {
    			exclude = true;
    		}
    	}
    	
    	// is this test a cucumber test ? (calling specific runner)
    	for (Element pack: test.getDescendants(new ElementFilter("package"))) {
    		if (pack.getAttributeValue("name").contains("com.seleniumtests.core.runner")) {
    			cucumberTest = true;
    		}
    	}
    	
    	if (!exclude) {
	    	if (cucumberTest) {
	    		testDefs.add(new SquashTaTestDef(testngFile, test.getAttributeValue("name"), true, cucumberNamedTest));
	    	} else {
	    		testDefs.add(new SquashTaTestDef(testngFile, test.getAttributeValue("name"), false, ""));
	    	}
    	}
	}
	
	/**
	 * Search for tests in TestNG files
	 * @param path
	 * @param application
	 * @return
	 */
	public List<SquashTaTestDef> parseTestNgXml() {
		
		// look for feature file into data folder
		File dir = Paths.get(srcPath, "data", application, "testng").toFile();
		if (!dir.exists()) {
			return new ArrayList<>();
		}

		File[] testngFiles = dir.listFiles((d, filename) -> filename.endsWith(".xml"));
    	
    	List<SquashTaTestDef> testDefs = new ArrayList<>();
    	
    	for (File testngFile: testngFiles) {
    		
    		Document doc;
    		SAXBuilder sxb = new SAXBuilder();

    		sxb.setFeature(XMLConstants.ACCESS_EXTERNAL_DTD, false);
    		sxb.setFeature(XMLConstants.ACCESS_EXTERNAL_SCHEMA, false);
    		sxb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    	    try {
    	        
    	        doc = sxb.build(testngFile);
    	    }
    	    catch(Exception e){
    	    	logger.error(String.format("Fichier %s illisible: %s", testngFile, e.getMessage()));
    	    	return testDefs;
    	    }
    	    
    	    Element suite = doc.getRootElement();
    	    if (!"suite".equalsIgnoreCase(suite.getName())) {
    	    	continue;
    	    }
    	    
    	    for (Element test: suite.getChildren("test")) {
    	    	readTestTag(test, testDefs, testngFile);
    	    	
    	    }
    	}	
    	
    	return testDefs;
	}
	
	/**
	 * Remove all .ta files which are present in folder but have not been generated by this run
	 * e.g: xml files which may have disapear or some test that have been excluded from Squash TA generation
	 */
	public void cleanGeneratedFile(String taScriptDir, List<String> generatedTaFileList) {
		
		// get .ta files
		File dir = new File(taScriptDir);
		File[] taFiles = dir.listFiles((d, filename) -> filename.endsWith(".ta"));
		
		List<String> taFileNames = new ArrayList<>();
		for (String generatedTaFile: generatedTaFileList) {
			taFileNames.add(new File(generatedTaFile).getName());
		}

		for (File file: taFiles) {
			if (!taFileNames.contains(file.getName()) && file.getName().startsWith("g__")) {
				logger.info("deleting " + file);
				if(!file.delete()) {
					logger.warn(String.format("File %s not deleted", file.getAbsolutePath()));
				}
			}
		}
	}
	
	/**
	 * Generate .ta scripts based on the generic one
	 * $cucumberTest$ pattern replaced by -DcucumberTest=<cucumberTestName>
	 * $testngFile$ pattern replaced by the test ng file path. It's a path beginning with %STF_HOME% so that 
	 * 			it uses env variables
	 * $testngName$ pattern replaced by name of the testng test
	 * @throws IOException
	 */
	public void generateTaScripts() throws IOException {
		String taScriptDir = Paths.get(dstPath, "src", "squashTA", "tests").toString();
		
		File srcTaFile = Paths.get(srcPath, "data", application, "squash-ta", "src", "squashTA", "tests", application + "_generic.ta").toFile();
		File genericFile = new File(taScriptDir + File.separator + application + "_generic.ta");
		
		if (srcTaFile.exists()) {
			try {
				FileUtils.copyFile(srcTaFile, genericFile);
			} catch (IOException e) {
				logger.warn("Cannot copy file: " + e.getMessage());
			}
		}
		
		// if base script does not exist, skip script generation
		if (!genericFile.exists()) {
			return;
		}
		
		String content = FileUtils.readFileToString(genericFile);
		
		// generate .ta files
		List<String> cucumberScenarios = parseFeatures();
		List<SquashTaTestDef> testNgTests = parseTestNgXml();
		List<String> taFileList = new ArrayList<>();
		
		for (SquashTaTestDef testDef: testNgTests) {
			if (testDef.isCucumber && testDef.isCucumberGeneric) {
				for (String cucumberScenario: cucumberScenarios) {
					String newContent = content
							.replace("$cucumberTest$", String.format("-DcucumberTests\"=%s\"", cucumberScenario.replace(" ", "&nbsp;")))
							.replace("$testngFile$", testDef.file.getAbsolutePath()
									.replace("\\", "/")
									.replace(srcPath, "%STF_HOME%/"))
							.replace("$testngName$", testDef.name)
							.replace("$app$", application);;
					String fileName = String.format("g__%s_%s_%s.ta", testDef.file.getName(), testDef.name, cucumberScenario
																			.replace(" ", "_")
																			.replace("<", "")
																			.replace(">",  ""));
					FileUtils.writeStringToFile(new File(taScriptDir + File.separator + fileName), newContent, "UTF-8");
					taFileList.add(fileName);
					logger.info("generating " + taScriptDir + File.separator + fileName);
				}
			} else {
				String newContent = content
						.replace("$cucumberTest$", "")
						.replace("$testngFile$", testDef.file.getAbsolutePath()
								.replace("\\", "/")
								.replace(srcPath, "%STF_HOME%/"))
						.replace("$testngName$", testDef.name)
						.replace("$app$", application);;
				String fileName = String.format("g__%s_%s.ta", testDef.file.getName(), testDef.name);
				FileUtils.writeStringToFile(new File(taScriptDir + File.separator + fileName), newContent, "UTF-8");
				taFileList.add(fileName);
				logger.info("generating " + taScriptDir + File.separator + fileName);
			}
		}
		
		cleanGeneratedFile(taScriptDir, taFileList);
		
	}
}
