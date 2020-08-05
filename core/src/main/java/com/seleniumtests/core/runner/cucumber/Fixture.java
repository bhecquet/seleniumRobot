package com.seleniumtests.core.runner.cucumber;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.htmlelements.Element;

public class Fixture {


	private static final Pattern PARAM_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
	private static final Map<String, Element> allElements = scanForElements(SeleniumTestsContextManager.getGlobalContext().getCucmberPkg());
	
	/**
	 * Search for all Elements in test code
	 * @return
	 */
	public static Map<String, Element> scanForElements(String cucumberPkg) {
	
		try {
	    	if (cucumberPkg == null) {
	    		throw new ConfigurationException("'cucumberPackage' parameter is not set in test NG XML file (inside <suite> tag), "
	    				+ "set it to the root package where cucumber implementation resides");
	    	}
			
			Map<String, Element> allFields = new HashMap<>();
			ImmutableSet<ClassInfo> infos = ClassPath.from(Fixture.class.getClassLoader()).getTopLevelClassesRecursive(cucumberPkg);
			
			for (ClassInfo info: infos) {
//				System.out.println("--" + info.getName());
				
				for (Field field: Class.forName(info.getName()).getDeclaredFields()) {
					if (Element.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
						field.setAccessible(true);
						allFields.put(String.format("%s.%s", info.getSimpleName(), field.getName()), (Element)field.get(null));
						allFields.put(field.getName(), (Element)field.get(null));
//						System.out.println(field.getName());
						
			
					}
				}
			}
			
			return allFields;
			
		} catch (IOException | SecurityException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
			throw new ConfigurationException(String.format("Cannot search elements in %s", cucumberPkg), e);
		}
		
	}
	
	/**
	 * Get element from its name
	 * @param name
	 * @return	the found element
	 * @throws ScenarioException when element is not found
	 */
	public Element getElement(String name) {
		
		Element element = allElements.get(name);
		if (element == null) {
			throw new ScenarioException(String.format("Element '%s' cannot be found among all classes. It may not have been defined", name));
		}
		return element;
	}
	
	/**
	 * Get value of a parameter
	 * If value has the format {{my_value}}, search inside test params
	 * @param parameter
	 * @return
	 */
	public String getValue(String parameterValue) {
		Matcher matcher = PARAM_PATTERN.matcher(parameterValue);
		if (matcher.find()) {
			return TestTasks.param(matcher.group(1).trim());
		} else {
			return parameterValue;
		}
	}
	
}
