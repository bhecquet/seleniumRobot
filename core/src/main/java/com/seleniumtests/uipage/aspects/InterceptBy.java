/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.uipage.aspects;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.config.ConfigMappingReader;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.uipage.PageObject;
/**
 * Aspect to intercept calls to methods from By. It change the argument of
 * methods when it use the keyWord : map
 * 
 * @author Sophie
 *
 */
@Aspect
public class InterceptBy {
	public static String PAGE = "";
	/**
	 * Change the argument of the By. when it's in the mapping files
	 * @param joinPoint
	 */
	 @Around("call(* org.openqa.selenium.By..* (..) )")
	public Object changeArg(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		Object reply = null;

		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				Object argument = args[i];
				if (argument != null && argument instanceof String && argument.toString().contains("map:")) {
					String[] input = ((String) argument).split(":");
					if (input[0].equals("map")) {
						String page = getCallerName(Thread.currentThread().getStackTrace());
						Map<String, HashMap<String, String>> config = SeleniumTestsContextManager.getThreadContext().getIdMapping();
						if (config == null) {
							config = new ConfigMappingReader().readConfig();
							if (config != null && !config.isEmpty()) {
								SeleniumTestsContextManager.getThreadContext().setIdMapping(config);
							} else {
								throw new ConfigurationException("There is no mapping file correspondant to this type and version");
							}
						}
						if (input[1] != null && !input[1].equals("")) {
							if (config.get(page) != null && !config.get(page).isEmpty()) {
								String toPass = config.get(page).get(input[1]);
								if (toPass != null && !toPass.equals("")) {
									args[i] = toPass;
								} else {
									throw new ConfigurationException("This id is not in the mapping files for this page");
								}
							} else {
								throw new ConfigurationException("This page doesn't have mapping configuration");
							}
						}
					}
				}

			}
		}
		reply = joinPoint.proceed(args);
		return reply;
	}
	 
	/**
	 * get the name of the PageObject that call the By.
	 * 
	 * @param stack : the stacktrace of the caller
	 */
	public String getCallerName(StackTraceElement[] stack) {
		String page = getPage();
		Class<?> stackClass = null;
		
		//find the PageObject Loader
		for(int i=0; i<stack.length;i++){
			try{
				 stackClass = Class.forName(stack[i].getClassName());
			}catch (ClassNotFoundException e){
				System.out.println(e);
			}
			if(PageObject.class.isAssignableFrom(stackClass)){
				page=last(stack[i].getClassName().split("\\."));	
			}
		}
		return page;
	}

	//return the last element of the table
	public static <Tab> Tab last(Tab[] array) {
		return array[array.length - 1];
	}
	
	//Methods for static tests with no PageObject
	public String getPage(){
		return PAGE;
	}
	public static void setPage(String page){
		PAGE = page;
	}
	
}