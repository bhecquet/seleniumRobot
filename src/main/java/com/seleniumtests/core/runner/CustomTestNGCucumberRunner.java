package com.seleniumtests.core.runner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private FeatureResultListener resultListener;
    private ClassLoader classLoader;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     */
    public CustomTestNGCucumberRunner(Class clazz) {
        classLoader = clazz.getClassLoader();
        resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        runtimeOptions = runtimeOptionsFactory.create();

        TestNgReporter reporter = new TestNgReporter(System.out);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        resultListener = new FeatureResultListener(runtimeOptions.reporter(classLoader), runtimeOptions.isStrict());
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
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
        return runtimeOptions.cucumberFeatures(resourceLoader);
    }
	
	public Object[][] provideScenarios() {
		List<Object[]> scenarioList = new ArrayList<Object[]>();
        for (CucumberFeature feature : getFeatures()) {
        	
        	// get scenario / scenario outline
        	for (CucumberTagStatement cucumberTagStatement : feature.getFeatureElements()) {
                
        		if (cucumberTagStatement instanceof CucumberScenarioOutline) {
        			for (CucumberExamples cucumberExamples : ((CucumberScenarioOutline)cucumberTagStatement).getCucumberExamplesList()) {
        	            for (CucumberScenario exampleScenario : cucumberExamples.createExampleScenarios()) {
        	            	scenarioList.add(new Object[]{new CucumberScenarioWrapper(exampleScenario)});
        	            }
        	        }
        		} else {
        			scenarioList.add(new Object[]{new CucumberScenarioWrapper((CucumberScenario)cucumberTagStatement)});
        		}
            }
        }
        
       return scenarioList.toArray(new Object[][]{});
    }

}
