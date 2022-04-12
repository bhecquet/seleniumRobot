package com.seleniumtests.core.runner.cucumber;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;

import io.cucumber.core.options.Constants;
import io.cucumber.testng.CucumberPropertiesProvider;

public class CustomCucumberPropertiesProvider implements CucumberPropertiesProvider {

	Map<String, String> parameters; 
	
	public CustomCucumberPropertiesProvider(XmlTest xmlTest) {
		
		String cucumberPkg = SeleniumTestsContextManager.getGlobalContext().getCucmberPkg();
    	if (cucumberPkg == null) {
    		throw new CustomSeleniumTestsException("'cucumberPackage' parameter is not set in test NG XML file (inside <suite> tag), "
    				+ "set it to the root package where cucumber implementation resides");
    	}
		
		// use parameters from XML
		parameters = xmlTest.getAllParameters();
		
		String glue = "classpath:" + cucumberPkg.replace(".", "/");
		if (!cucumberPkg.startsWith("com.seleniumtests")) {
			glue += ",classpath:com/seleniumtests/core/runner/cucumber";
        }
		
		parameters.put(Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, "true");
		parameters.put(Constants.GLUE_PROPERTY_NAME, glue);
		parameters.put(Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME, "true"); // force monpochrome
		
		String tags = SeleniumTestsContextManager.getThreadContext().getCucumberTags().replace(" AND ", " and "); // for compatibility with old format
		if (!tags.trim().isEmpty()) {
			parameters.put(Constants.FILTER_TAGS_PROPERTY_NAME, tags);
		}
		
		if (!SeleniumTestsContextManager.getThreadContext().getCucumberTests().isEmpty()) {
			parameters.put(Constants.FILTER_NAME_PROPERTY_NAME, StringUtils.join(SeleniumTestsContextManager.getThreadContext().getCucumberTests(), "|"));
		}
		// Handle special regex character '?'
		parameters.put(Constants.FILTER_NAME_PROPERTY_NAME, parameters.get(Constants.FILTER_NAME_PROPERTY_NAME).replace("?", "\\?"));
		
		parameters.put(Constants.FEATURES_PROPERTY_NAME, SeleniumTestsContextManager.getFeaturePath());
	}
	
	@Override
	public String get(String key) {
		return parameters.get(key);
	}

}
