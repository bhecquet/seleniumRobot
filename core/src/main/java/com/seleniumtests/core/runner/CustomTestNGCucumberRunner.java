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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.testng.SkipException;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;

import cucumber.api.Result;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.BackendModuleBackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.FeaturePathFeatureSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;
import io.cucumber.core.model.FeatureWithLines;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.testng.CucumberFeatureWrapper;
import io.cucumber.testng.PickleEventWrapper;

public class CustomTestNGCucumberRunner  {
	
	
	private EventBus bus;
    private Filters filters;
    private FeaturePathFeatureSupplier featureSupplier;
    private ThreadLocalRunnerSupplier runnerSupplier;
    private RuntimeOptions runtimeOptions;
    private Plugins plugins;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     * @throws URISyntaxException 
     */
    public CustomTestNGCucumberRunner(Class<?> clazz) throws URISyntaxException {
        classLoader = clazz.getClassLoader();
        resourceLoader = new MultiLoader(classLoader);

		initCucumberOptions(clazz);

    }
    
    private void initCucumberOptions(Class<?> clazz) throws URISyntaxException {
    	String cucumberPkg = SeleniumTestsContextManager.getGlobalContext().getCucmberPkg();
    	if (cucumberPkg == null) {
    		throw new CustomSeleniumTestsException("'cucumberPackage' parameter is not set in test NG XML file (inside <suite> tag), "
    				+ "set it to the root package where cucumber implementation resides");
    	}
    	
    	RuntimeOptionsBuilder builder = new RuntimeOptionsBuilder();
    	// add cucumber implementation classes
    	builder.addGlue(new URI("classpath:" + cucumberPkg.replace(".", "/")));
    	if (!cucumberPkg.startsWith("com.seleniumtests")) {
    		builder.addGlue(new URI("classpath:com/seleniumtests/core/runner/cucumber"));
        }

        // correct issue #243: as we write to log files, colors cannot be rendered in text files
    	builder.setMonochrome()
    		.addFeature(FeatureWithLines.parse(SeleniumTestsContextManager.getFeaturePath()))
    		.addTagFilter(SeleniumTestsContextManager
    			.getThreadContext()
    			.getCucumberTags()
    			.replace(" AND ", " and ") // for compatibility with old format
    			);
    	
    	for (String cucumberTest: SeleniumTestsContextManager.getThreadContext().getCucumberTests()) {
    		builder.addNameFilter(Pattern.compile(cucumberTest
    				.replace("??", "\\?\\?") // Handle special regex character '?' as we
    				));
    	}
    		
    	runtimeOptions = builder.build();
        runtimeOptions.addUndefinedStepsPrinterIfSummaryNotDefined();

        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
    

        BackendModuleBackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        bus = new TimeServiceEventBus(TimeService.SYSTEM);
        plugins = new Plugins(classLoader, new PluginFactory(), runtimeOptions);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        filters = new Filters(runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);

    }
    
    /**
     * Get list of features given their name or their file name
     */
    /*private List<CucumberFeature> getFeaturesFromRequestedTests(Class<?> clazz, ResourceLoader resourceLoader) {

        RuntimeOptions runtimeOptionsB = new RuntimeOptionsFactory(clazz).create();
        
        final List<CucumberFeature> allFeatures = runtimeOptionsB.cucumberFeatures(resourceLoader);
        List<CucumberFeature> selectedFeatures = new ArrayList<>();
        
        // filter features requested for execution
        List<String> testList = SeleniumTestsContextManager.getThreadContext().getCucumberTests();
        
        selectFeatures(allFeatures, selectedFeatures, testList);
        
        
        if (selectedFeatures.isEmpty()) {
        	// select scenarios in features
        	
            // get only scenarios whose name is in the list of tests
        	for (CucumberFeature feature: allFeatures) {
        		
        		List<CucumberTagStatement> selectedScenarios = new ArrayList<>();
        		
        		selectSenario(feature, testList, selectedScenarios);
            				
        		feature.getFeatureElements().removeAll(feature.getFeatureElements());
        		feature.getFeatureElements().addAll(selectedScenarios);
        		
        		if (!selectedScenarios.isEmpty()) {
        			selectedFeatures.add(feature);
        		}
        	}
        }
        
        return selectedFeatures;
    }*/
    
    /**
     * 
     * @param allFeatures
     * @param selectedFeatures
     * @param testList
     * @return Cucumber feature list of selected tests
     */
    /*private List<CucumberFeature> selectFeatures(final List<CucumberFeature> allFeatures, List<CucumberFeature> selectedFeatures, 
    									List<String> testList){
    	for (CucumberFeature feature: allFeatures) {
        	String featureName = feature.getGherkinFeature().getFeature().getName();
        	String featureFileName = FilenameUtils.getBaseName(feature.getPath());
        	
        	for (String test: testList) {
    			if (featureName.matches(test) || test.equals(featureFileName)) {
    				selectedFeatures.add(feature);
    				break;
    			}
    		}  
        }
    	return selectedFeatures;
    }*/
    
    /**
     * 
     * @param feature
     * @param testList
     * @param selectedScenarios
     * @return Cucumber statement list of selected tests
     */
    /*private List<CucumberTagStatement> selectSenario(CucumberFeature feature, List<String> testList, 
    													List<CucumberTagStatement> selectedScenarios){
    	for (CucumberTagStatement stmt: feature.getFeatureElements()) {
			
			for (String test: testList) {
    			if (stmt.getGherkinModel().getName().equals(test) || stmt.getGherkinModel().getName().matches(test)) {
    				selectedScenarios.add(stmt);
    			}
			}
		}
    	return selectedScenarios;
    }*/
    
    public void runScenario(PickleEvent pickle) {
        //Possibly invoked in a multi-threaded context
        Runner runner = runnerSupplier.get();
        TestCaseResultListener testCaseResultListener = new TestCaseResultListener(runner.getBus(), runtimeOptions.isStrict());
        runner.runPickle(pickle);
        testCaseResultListener.finishExecutionUnit();

        if (!testCaseResultListener.isPassed()) {
        	throw new CucumberException(testCaseResultListener.getError());
        }
    }
    
    /*public void runScenario(CucumberScenarioWrapper cucumberScenarioWrapper)   {
	
		resultListener.startFeature();
		
		Formatter formatter = runtimeOptions.formatter(classLoader);
		CucumberScenario cucumberScenario = cucumberScenarioWrapper.getCucumberScenario();
		
		try {
	    	Field field = StepContainer.class.getDeclaredField("cucumberFeature");
			field.setAccessible(true);
			CucumberFeature cucumberFeature = (CucumberFeature)field.get(cucumberScenario);
	    	formatter.uri(cucumberFeature.getPath());
	        formatter.feature(cucumberFeature.getGherkinFeature());
	    	
	    	cucumberScenario.run(runtimeOptions.formatter(classLoader), resultListener, runtime);
	    	formatter.eof();
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new CucumberException("Could not run scenario: " + e.getMessage());
		}
		
		if (!resultListener.isPassed()) {
	        throw new CucumberException(resultListener.getFirstError());
	    }
	}*/
    

    public void finish() {
        bus.send(new TestRunFinished(bus.getTime(), bus.getTimeMillis()));
    }
    
    /**
     * Returns the list of scenario contained in features whose name (or file name) matches expected tests
     */
    private List<Object[]> provideScenariosFromFeatureFiles(List<CucumberFeature> features) {
    	List<Object[]> scenarios = new ArrayList<>();
    	for (CucumberFeature feature : features) {
        	for (String cucumberTest: SeleniumTestsContextManager.getThreadContext().getCucumberTests()) {
        		// get scenarios from feature name. If feature matches, all scenarios are added
        		if (feature.getName().matches(cucumberTest) || feature.getUri().getSchemeSpecificPart().replace(".feature", "").matches(cucumberTest)) {
        			for (PickleEvent pickle : feature.getPickles()) {
                    	ScenarioDefinition scenario = getScenarioDefinitionFromPickle(feature.getGherkinFeature().getFeature(), pickle);
                        scenarios.add(new Object[]{new CucumberScenarioWrapper(pickle, scenario),
                                new CucumberFeatureWrapperImpl(feature)});
        			}
        		} 
        	}
        }
    	return scenarios;
    }
    
    private ScenarioDefinition getScenarioDefinitionFromPickle(Feature feature, PickleEvent pickle) {
    	
    	for (ScenarioDefinition scenario: feature.getChildren()) {
    		for (PickleLocation pLocation: pickle.pickle.getLocations()) {
    			if (pLocation.getColumn() == scenario.getLocation().getColumn() && pLocation.getLine() == scenario.getLocation().getLine()) {
        			return scenario;
    			}
    		}
    	}
    	return null;
    	
    }
    
    /**
     * @return returns the cucumber scenarios as a two dimensional array of {@link PickleEventWrapper}
     * scenarios combined with their {@link CucumberFeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        try {
            List<Object[]> scenarios = new ArrayList<>();
            List<CucumberFeature> features = getFeatures();
            for (CucumberFeature feature : features) {
                for (PickleEvent pickle : feature.getPickles()) {
                    if (filters.matchesFilters(pickle)) {
                    	ScenarioDefinition scenario = getScenarioDefinitionFromPickle(feature.getGherkinFeature().getFeature(), pickle);
                        scenarios.add(new Object[]{new CucumberScenarioWrapper(pickle, scenario)});
                    }
                }
            }
            
            // no scenario found, search among feature name and feature file name
            if (scenarios.isEmpty()) {
	            scenarios.addAll(provideScenariosFromFeatureFiles(features));
            }
            
            return scenarios.toArray(new Object[][]{});
        } catch (CucumberException e) {
            throw new ConfigurationException("Error while providing cucumber scenario", e);
        }
    }
    

    private List<CucumberFeature> getFeatures() {
        plugins.setSerialEventBusOnEventListenerPlugins(bus);

        List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime(), bus.getTimeMillis()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }
        return features;
    }

   

    /*public void finish() {
        Formatter formatter = runtimeOptions.formatter(classLoader);
        formatter.done();
        formatter.close();
        runtime.printSummary();
        if (!runtime.getSnippets().isEmpty()) {
        	throw new CucumberException("Some steps could not be found");
        }
    }*/

    /**
     * @return List of detected cucumber features
     */
    /*public List<CucumberFeature> getFeatures() {
        return cucumberFeatures;
    }*/
	
	/*public Object[][] provideScenarios() {
		List<CucumberScenarioWrapper> scenarioList = new ArrayList<>();
        for (CucumberFeature feature : getFeatures()) {
        	
        	// get scenario / scenario outline
        	for (CucumberTagStatement cucumberTagStatement : feature.getFeatureElements()) {
                
        		getScenarioWrapper(cucumberTagStatement, scenarioList);
            }
       }
        
       List<Object[]> newScenarioList = new ArrayList<>();
       for (CucumberScenarioWrapper scenarioWrapper: scenarioList) {
    	   newScenarioList.add(new Object[]{scenarioWrapper});
       }
        
       return newScenarioList.toArray(new Object[][]{});
    }*/
	
	/**
	 * 
	 * @param cucumberTagStatement
	 * @param scenarioList
	 * @return list of cucumber scenario wrapper
	 */
	/*private List<CucumberScenarioWrapper> getScenarioWrapper(CucumberTagStatement cucumberTagStatement, 
															List<CucumberScenarioWrapper> scenarioList){
		if (cucumberTagStatement instanceof CucumberScenarioOutline) {
			for (CucumberExamples cucumberExamples : ((CucumberScenarioOutline)cucumberTagStatement).getCucumberExamplesList()) {
	            for (CucumberScenario exampleScenario : cucumberExamples.createExampleScenarios()) {
        			CucumberScenarioWrapper scenarioWrapper = new CucumberScenarioWrapper(exampleScenario, cucumberTagStatement.getGherkinModel().getName());
	            	scenarioList.add(scenarioWrapper);
	            }
	        }
		} else {
			CucumberScenarioWrapper scenarioWrapper = new CucumberScenarioWrapper((CucumberScenario)cucumberTagStatement);
			if (!scenarioList.contains(scenarioWrapper)) {
				scenarioList.add(scenarioWrapper);
			}
		}
		return scenarioList;
	}*/
	
	class TestCaseResultListener {
	    private static final String UNDEFINED_MESSAGE = "There are undefined steps";
	    private static final String SKIPPED_MESSAGE = "This scenario is skipped";
	    private final EventBus bus;
	    private boolean strict;
	    private Result result;
	    private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
	        @Override
	        public void receive(TestCaseFinished event) {
	            receiveResult(event.result);
	        }
	    };

	    TestCaseResultListener(EventBus bus, boolean strict) {
	        this.strict = strict;
	        this.bus = bus;
	        bus.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
	    }

	    void finishExecutionUnit() {
	        bus.removeHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
	    }


	    void receiveResult(Result result) {
	        this.result = result;
	    }

	    boolean isPassed() {
	        return result == null || result.is(Result.Type.PASSED);
	    }

	    Throwable getError() {
	        if (result == null) {
	            return null;
	        }
	        switch (result.getStatus()) {
	        case FAILED:
	        case AMBIGUOUS:
	            return result.getError();
	        case PENDING:
	            if (strict) {
	                return result.getError();
	            } else {
	                return new SkipException(result.getErrorMessage(), result.getError());
	            }
	        case UNDEFINED:
	            if (strict) {
	                return new CucumberException(UNDEFINED_MESSAGE);
	            } else {
	                return new SkipException(UNDEFINED_MESSAGE);
	            }
	        case SKIPPED:
	            Throwable error = result.getError();
	            if (error != null) {
	                if (error instanceof SkipException) {
	                    return error;
	                } else {
	                    return new SkipException(result.getErrorMessage(), error);
	                }
	            } else {
	                return new SkipException(SKIPPED_MESSAGE);
	            }
	        case PASSED:
	            return null;
	        default:
	            throw new IllegalStateException("Unexpected result status: " + result.getStatus());
	        }
	    }

	}
	
	class CucumberFeatureWrapperImpl implements CucumberFeatureWrapper {
	    private final CucumberFeature cucumberFeature;

	    CucumberFeatureWrapperImpl(CucumberFeature cucumberFeature) {
	        this.cucumberFeature = cucumberFeature;
	    }

	    @Override
	    public String toString() {
	        return "\"" + cucumberFeature.getGherkinFeature().getFeature().getName() + "\"";
	    }
	}


}
