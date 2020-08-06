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
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.Element;
import com.seleniumtests.util.logging.ScenarioLogger;

public class Fixture {


	private static final Pattern PARAM_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
	private static final Map<String, Field> allElements = scanForElements(SeleniumTestsContextManager.getGlobalContext().getCucmberPkg());
	protected static ThreadLocal<PageObject> currentPage = new ThreadLocal<>();
	protected static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(Fixture.class);  // with this logger, information will be added in test step + logs
	
	
	/**
	 * Search for all Elements in test code
	 * @return
	 */
	public static Map<String, Field> scanForElements(String cucumberPkg) {
	
		try {
	    	if (cucumberPkg == null) {
	    		throw new ConfigurationException("'cucumberPackage' parameter is not set in test NG XML file (inside <suite> tag), "
	    				+ "set it to the root package where cucumber implementation resides");
	    	}
			
			Map<String, Field> allFields = new HashMap<>();
			ImmutableSet<ClassInfo> infos = ClassPath.from(Fixture.class.getClassLoader()).getTopLevelClassesRecursive(cucumberPkg);
			
			for (ClassInfo info: infos) {
//				System.out.println("--" + info.getName());
				
				for (Field field: Class.forName(info.getName()).getDeclaredFields()) {
					if (Element.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
						field.setAccessible(true);
						allFields.put(String.format("%s.%s", info.getSimpleName(), field.getName()), field);
						allFields.put(field.getName(), field);
//						System.out.println(field.getName());
						
			
					}
				}
			}
			
			return allFields;
			
		} catch (IOException | SecurityException | ClassNotFoundException | IllegalArgumentException e) {
			throw new ConfigurationException(String.format("Cannot search elements in %s", cucumberPkg), e);
		}
		
	}
	
	/**
	 * Get element from its name
	 * @param name
	 * @return	the found element
	 * @throws ScenarioException when element is not found
	 */
	public String getElement(String name) {
		
		Field elementField = allElements.get(name);
		if (elementField == null) {
			throw new ScenarioException(String.format("Element '%s' cannot be found among all classes. It may not have been defined", name));
		}
		
		Class<?> pageClass = elementField.getDeclaringClass(); 
		
		// create new page if we are not on it
		if (pageClass != currentPage.get().getClass()) {
			try {
				currentPage.set((PageObject)pageClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ScenarioException(String.format("Page '%s' don't have default constructor, add it to avoid this error", pageClass.getSimpleName()));
			}
			logger.info("switching to page " + pageClass.getSimpleName());
		}
		
		if (name.split(".").length == 1) {
			return name;
		} else {
			return name.split(".")[1];
		}
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

	public static PageObject getCurrentPage() {
		return currentPage.get();
	}
	
}
