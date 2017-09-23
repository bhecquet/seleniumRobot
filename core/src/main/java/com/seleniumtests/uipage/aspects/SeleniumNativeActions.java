package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.WebElement;

@Aspect
public class SeleniumNativeActions {

	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver+.findElement (..))"
			+ ")"			
			)
	public Object findHtmlElement(ProceedingJoinPoint joinPoint) throws Throwable {
		Object element = joinPoint.proceed(joinPoint.getArgs());
		return element;
	}
}
