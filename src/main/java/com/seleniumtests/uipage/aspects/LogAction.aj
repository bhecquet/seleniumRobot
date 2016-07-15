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

package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

/**
 * Aspect to intercept calls to methods of HtmlElement. It allows to retry discovery and action 
 * when something goes wrong with the driver
 * 
 * @author behe
 *
 */
@Aspect
public class LogAction {

	@Before("(execution(public * com.seleniumtests.uipage.htmlelements..*Element..* (..)) "
    		+ "|| execution(public * com.seleniumtests.uipage.htmlelements.SelectList..* (..)) "
    		+ "|| execution(public * com.seleniumtests.uipage.htmlelements.Table..* (..))) "
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.toString (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.get* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.set* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.find* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.wait* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.htmlelements.HtmlElement.toHTML (..))"
    		)
	public void logStep(JoinPoint joinPoint) {
		HtmlElement element = (HtmlElement)joinPoint.getTarget();

		if (isHtmlElementDirectlyCalled(Thread.currentThread().getStackTrace())) {
			String argString = "";
			if (joinPoint.getArgs().length > 0) {
				argString = "with args: (";
				for (Object arg: joinPoint.getArgs()) {
					argString += arg.toString() + ", ";
				}
				argString += ")";
			}
			System.out.println(String.format("%s on %s %s", joinPoint.getSignature().getName(), element, argString));
		}
	}
	
	@Before("execution(public * com.seleniumtests.uipage.PageObject..* (..)) "
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.get* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.close* (..))"
    		+ "&& !execution(* com.seleniumtests.uipage.PageObject.assert* (..))"
    		)
	public void logPageObjectAction(JoinPoint joinPoint) {
		PageObject page = (PageObject)joinPoint.getTarget();
		String pageName = page == null ? "": "on page " + page.getClass().getSimpleName();
			
		String argString = "";
		if (joinPoint.getArgs().length > 0) {
			argString = "with args: (";
			for (Object arg: joinPoint.getArgs()) {
				argString += arg.toString() + ", ";
			}
			argString += ")";
		}
		System.out.println(String.format("%s %s %s", joinPoint.getSignature().getName(), pageName, argString));
		
	}
	
	/**
	 * Check whether this action has directly been performed on the HtmlElement (e.g: click)
	 * or through an other type of element (e.g: clic on LinkElement, redirected to HtmlElement)
	 * In this last case, do not log action as it has already been logged by the specific type of 
	 * element
	 * @param stack
	 * @return
	 */
	private boolean isHtmlElementDirectlyCalled(StackTraceElement[] stack) {
		String stackClass = null;
		boolean specificElementFound = false;
		boolean htmlElementFound = false;
		
		for(int i=0; i < stack.length; i++){
			 stackClass = stack[i].getClassName();
			 if (stackClass.equals("com.seleniumtests.uipage.htmlelements.HtmlElement")) {
				 htmlElementFound = true;
			 } else if (stackClass.startsWith("com.seleniumtests.uipage.htmlelements.")) {
				 specificElementFound = true;
			 }
		}
		if (htmlElementFound && specificElementFound) {
			return false;
		} else {
			return true;
		}
	}
}
