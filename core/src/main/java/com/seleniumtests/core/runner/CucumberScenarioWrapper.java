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
package com.seleniumtests.core.runner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;

/**
 * The only purpose of this class is to provide custom {@linkplain #toString()},
 * making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#feature(cucumber.api.testng.CucumberFeatureWrapper)
 */
public class CucumberScenarioWrapper {


	private static final Logger logger = SeleniumRobotLogger.getLogger(CucumberScenarioWrapper.class);

    private final PickleWrapper pickleWrapper;
    private final FeatureWrapper cucumberFeature;
    private String exampleValues;
    private String scenarioOutlineName = null;

    public CucumberScenarioWrapper(PickleWrapper pickleWrapper, FeatureWrapper cucumberFeature) throws IOException {
        this.pickleWrapper = pickleWrapper;
        this.cucumberFeature = cucumberFeature;
        
        int scenarioLineNumber = pickleWrapper.getPickle().getScenarioLine() - 1;
        File featureFile = new File(pickleWrapper.getPickle().getUri());

    	List<String> featureLines = Files.readLines(featureFile, StandardCharsets.UTF_8);
		String scenarioLine = featureLines.get(scenarioLineNumber);
		
		if (scenarioLine.contains("Scenario Outline")) {
	        int exampleLineNumber = pickleWrapper.getPickle().getLine() - 1;
			exampleValues = featureLines.get(exampleLineNumber).replaceAll("\\s{2,99}", " ").trim();
			scenarioOutlineName = scenarioLine;
		}
	

    }

    @Override
    public String toString() {
    	return toString(95);
    }
    
    public String toString(int strip) {
    	
    	String name;
    	
    	// in case of examples (Scenario Outline), search if scenario description contains placeholders. 
    	// If true, only return description, else, return description and visualName (which contains data) so that all execution can be distinguished in report

    	if (scenarioOutlineName != null && !Pattern.compile("<[^>]*+>").matcher(scenarioOutlineName).find()) { 
    		name = pickleWrapper.getPickle().getName() + "-" + exampleValues;
    		
    	} else {
    		name = pickleWrapper.getPickle().getName();
    	}
    	
    	if (strip > 50 && name.length() > strip) {
    		logger.warn("-------------------------------------------------------------------------------------");
			logger.warn(String.format("Cucumber scenario name [%s] is too long. This may cause problems in reporters and accessing reports on Windows", name));
			logger.warn("Reduce size, and if it's a 'Scenario Outline' whose name does not contain placeholders, add some. Ex: 'My long name' => 'My long name <col1>, <col2>'");
			logger.warn("-------------------------------------------------------------------------------------");
			name = name.substring(0, strip);
		}

		return name;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
        	return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        CucumberScenarioWrapper other;
        try {
        	other = (CucumberScenarioWrapper) obj;
        } catch (Exception e) {
        	return false;
        }

        return this.toString(-1) !=null ? this.toString(-1).equals(other.toString(-1)) : this.toString(-1) == other.toString(-1);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public Pickle getPickle() {
    	return pickleWrapper.getPickle();
    }

}
