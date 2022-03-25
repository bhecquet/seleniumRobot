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

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.seleniumtests.util.logging.SeleniumRobotLogger;

import gherkin.ast.Examples;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.PickleEventWrapper;

/**
 * The only purpose of this class is to provide custom {@linkplain #toString()},
 * making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#feature(cucumber.api.testng.CucumberFeatureWrapper)
 */
public class CucumberScenarioWrapper implements PickleEventWrapper {


	private static final Logger logger = SeleniumRobotLogger.getLogger(CucumberScenarioWrapper.class);

    private final PickleEvent pickleEvent;
    private final ScenarioDefinition scenario;
    private String exampleValues;
    private String scenarioOutlineName = null;

    public CucumberScenarioWrapper(PickleEvent pickleEvent, ScenarioDefinition scenario) {
        this.pickleEvent = pickleEvent;
        this.scenario = scenario;
        
        if (scenario instanceof ScenarioOutline) {
        	scenarioOutlineName = scenario.getName();
        	exampleValues = getExampleValue();
        }
    }
    
    private String getExampleValue() {
    	for (Examples example: ((ScenarioOutline)scenario).getExamples()) {
    		for (TableRow row: example.getTableBody()) {
	    		for (PickleLocation pLocation: pickleEvent.pickle.getLocations()) {
	    			if (row.getLocation().getColumn() == pLocation.getColumn() && row.getLocation().getLine() == pLocation.getLine()) {
	    				return formatExampleRow(row);
	    			}
	    		}
    		}
    	}
    	return "||";
    }
    
    private String formatExampleRow(TableRow row) {
    	return "| " + StringUtils.join(row.getCells().stream().map(TableCell::getValue).collect(Collectors.toList()), " | ") + " |";
    }

    @Override
    public String toString() {
    	return toString(95);
    }
    
    public String toString(int strip) {
    	
    	String name;
    	
    	// in case of examples (Scenario Outline), search if scenario description contains placeholders. 
    	// If true, only resturns description, else, return description and visualName (which contains data) so that all execution can be distinguished in report

    	if (scenarioOutlineName != null && !Pattern.compile("<[^>]*+>").matcher(scenarioOutlineName).find()) { 
    		name = pickleEvent.pickle.getName() + "-" + exampleValues;
    		
    	} else {
    		name = pickleEvent.pickle.getName();
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
	@Override
	public PickleEvent getPickleEvent() {
		return pickleEvent;
	}

}
