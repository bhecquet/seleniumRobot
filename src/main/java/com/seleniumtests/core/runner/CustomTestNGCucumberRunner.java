/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.core.runner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestPlan;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestLogging;
import com.seleniumtests.customexception.CustomSeleniumTestsException;

import cucumber.api.testng.CucumberFeatureWrapper;
import cucumber.api.testng.FeatureResultListener;
import cucumber.api.testng.TestNGCucumberRunner;
import cucumber.api.testng.TestNgReporter;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import cucumber.runtime.model.StepContainer;
import gherkin.formatter.Formatter;

public class CustomTestNGCucumberRunner {

	private Runtime runtime;
	private String testName;
	private List<CucumberFeature> cucumberFeatures;
    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private FeatureResultListener resultListener;
    private ClassLoader classLoader;
    private static final Logger logger = TestLogging.getLogger(SeleniumTestPlan.class);

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     */
    public CustomTestNGCucumberRunner(Class clazz, String testName) {
        classLoader = clazz.getClassLoader();
        this.testName = testName; 
        resourceLoader = new MultiLoader(classLoader);
        
        cucumberFeatures = initCucumberOptions(clazz);

        TestNgReporter reporter = new TestNgReporter(System.out);
        resultListener = new FeatureResultListener(runtimeOptions.reporter(classLoader), runtimeOptions.isStrict());
    }
    
    private List<CucumberFeature> initCucumberOptions(Class clazz) {
    	String cucumberPkg = SeleniumTestsContextManager.getThreadContext().getCucmberPkg();
    	if (cucumberPkg == null) {
    		throw new CustomSeleniumTestsException("'cucumberPackage' parameter is not set in test NG XML file (inside <suite> tag), "
    				+ "set it to the root package where cucumber implementation resides");
    	}
    	
    	// get all features, filtered by test name
    	System.setProperty("cucumber.options", SeleniumTestsContext.FEATURES_PATH);
        List<CucumberFeature> testSelectedFeatures = getFeaturesFromRequestedTests(clazz, classLoader, resourceLoader);

    	// build cucumber option list
        // take into account tag options
        String cucumberOptions = "";
        String tagList = SeleniumTestsContextManager.getThreadContext().getCucumberTags();
        if (tagList != null && !tagList.equals("")) {
        	String tagsOptions = " ";
        	for (String tags: tagList.split("AND")) {
        		tagsOptions += String.format("--tags %s ", tags.trim());
        	}
        	
        	cucumberOptions += tagsOptions;    	
        }
        
        // add cucumber implementation classes
        cucumberOptions += " --glue classpath:" + cucumberPkg;
        
        // add feature path
        cucumberOptions += " " + SeleniumTestsContext.FEATURES_PATH;

        // get filtered features, based on tags
        runtimeOptions = new RuntimeOptions(cucumberOptions);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);

        // add list of tag filtered features to features selected by tests 
        if (tagList != null && !tagList.equals("")) {
        	testSelectedFeatures.addAll(runtimeOptions.cucumberFeatures(resourceLoader));
        }
         
        if (testSelectedFeatures.size() == 0) {
        	throw new CustomSeleniumTestsException("No test has been selected");
        }
        
        return testSelectedFeatures;
    }
    
    /**
     * Get list of features given their name or their file name
     */
    private List<CucumberFeature> getFeaturesFromRequestedTests(Class clazz, ClassLoader classLoader, ResourceLoader resourceLoader) {

        RuntimeOptions runtimeOptions = new RuntimeOptionsFactory(clazz).create();
        
        final List<CucumberFeature> allFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        List<CucumberFeature> selectedFeatures = new ArrayList<CucumberFeature>();
        
        // filter features requested for execution
        List<String> testList = SeleniumTestsContextManager.getThreadContext().getCucmberTests();
        
        for (CucumberFeature feature: allFeatures) {
        	String featureName = feature.getGherkinFeature().getName();
        	String featureFileName = FilenameUtils.getBaseName(feature.getPath());
        	
        	for (String test: testList) {
    			if (featureName.matches(test) || test.equals(featureFileName)) {
    				selectedFeatures.add(feature);
    				break;
    			}
    		}  
        }
        
        // select scenarios in features if feature list is empty
        // remove scenarios whose name is not in test list
        if (selectedFeatures.isEmpty()) {
        	for (CucumberFeature feature: allFeatures) {
        		List<CucumberTagStatement> selectedScenarios = new ArrayList<CucumberTagStatement>();
        		for (CucumberTagStatement stmt: feature.getFeatureElements()) {
        			for (String test: testList) {
            			if (stmt.getGherkinModel().getName().equals(test)) {
            				selectedScenarios.add(stmt);
            			}
        			}
        		}
        		feature.getFeatureElements().removeAll(feature.getFeatureElements());
        		feature.getFeatureElements().addAll(selectedScenarios);
        		
        		if (!selectedScenarios.isEmpty()) {
        			selectedFeatures.add(feature);
        		}
        	}
        }
        
        return selectedFeatures;
    }

    public void runScenario(CucumberScenarioWrapper cucumberScenarioWrapper) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	resultListener.startFeature();
    	
    	Formatter formatter = runtimeOptions.formatter(classLoader);
    	CucumberScenario cucumberScenario = cucumberScenarioWrapper.getCucumberScenario();
    	
    	Field field = StepContainer.class.getDeclaredField("cucumberFeature");
		field.setAccessible(true);
		CucumberFeature cucumberFeature = (CucumberFeature)field.get(cucumberScenario);
    	formatter.uri(cucumberFeature.getPath());
        formatter.feature(cucumberFeature.getGherkinFeature());
    	
    	cucumberScenario.run(runtimeOptions.formatter(classLoader), resultListener, runtime);
    	formatter.eof();
    	
    	if (!resultListener.isPassed()) {
            throw new CucumberException(resultListener.getFirstError());
        }
    }

    public void finish() {
        Formatter formatter = runtimeOptions.formatter(classLoader);

        formatter.done();
        formatter.close();
        runtime.printSummary();
    }

    /**
     * @return List of detected cucumber features
     */
    public List<CucumberFeature> getFeatures() {
        return cucumberFeatures;
    }
	
	public Object[][] provideScenarios() {
		List<CucumberScenarioWrapper> scenarioList = new ArrayList<CucumberScenarioWrapper>();
        for (CucumberFeature feature : getFeatures()) {
        	
        	// get scenario / scenario outline
        	for (CucumberTagStatement cucumberTagStatement : feature.getFeatureElements()) {
                
        		if (cucumberTagStatement instanceof CucumberScenarioOutline) {
        			for (CucumberExamples cucumberExamples : ((CucumberScenarioOutline)cucumberTagStatement).getCucumberExamplesList()) {
        	            for (CucumberScenario exampleScenario : cucumberExamples.createExampleScenarios()) {
                			CucumberScenarioWrapper scenarioWrapper = new CucumberScenarioWrapper(exampleScenario);
        	            	scenarioList.add(scenarioWrapper);
        	            }
        	        }
        		} else {
        			CucumberScenarioWrapper scenarioWrapper = new CucumberScenarioWrapper((CucumberScenario)cucumberTagStatement);
        			if (!scenarioList.contains(scenarioWrapper)) {
        				scenarioList.add(scenarioWrapper);
        			}
        		}
            }
       }
        
       List<Object[]> newScenarioList = new ArrayList<Object[]>();
       for (CucumberScenarioWrapper scenarioWrapper: scenarioList) {
    	   newScenarioList.add(new Object[]{scenarioWrapper});
       }
        
       return newScenarioList.toArray(new Object[][]{});
    }

}
