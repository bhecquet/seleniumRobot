package com.seleniumtests.core.runner;

import cucumber.runtime.model.CucumberScenario;

/**
 * The only purpose of this class is to provide custom {@linkplain #toString()},
 * making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#feature(cucumber.api.testng.CucumberFeatureWrapper)
 */
public class CucumberScenarioWrapper {

	
    private final CucumberScenario cucumberScenario;

    public CucumberScenarioWrapper(CucumberScenario cucumberScenario) {
        this.cucumberScenario = cucumberScenario;
    }

    public CucumberScenario getCucumberScenario() {
        return cucumberScenario;
    }

    @Override
    public String toString() {
        return cucumberScenario.getGherkinModel().getName();
    }

}
