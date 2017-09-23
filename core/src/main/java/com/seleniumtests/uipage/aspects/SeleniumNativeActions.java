package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.seleniumtests.uipage.htmlelements.HtmlElement;

@Aspect
public class SeleniumNativeActions {

	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver+.findElement (..))"
			+ ")"			
			)
	public Object findHtmlElement(ProceedingJoinPoint joinPoint) throws Throwable {
		return new HtmlElement("", (By)(joinPoint.getArgs()[0]));
	}
	
	// TODO: handle frames (do not reset state when this mode is used (see ReplayAction)
	// TODO: check Select behaviour
	// TODO: handle findElementBy... (from RemoteWebDriver) => should be useless as SeleniuRobot only expose a WebDriverInstance
}
