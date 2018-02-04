package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.By;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.aspects.LogAction;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

@Aspect
public class SeleniumNativeActions {

	private FrameElement currentFrame;

	private Boolean doOverride() {
		return SeleniumTestsContextManager.getThreadContext().getOverrideSeleniumNativeAction();
	}
	
	/**
	 * Intercept any call to findElement made from a PageObject subclass and returns a HtmlElement instead of a RemoteWebElement
	 * This way, every action done on this element will benefit from HtmlElement mechanism
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver+.findElement (..))"
			+ ")"			
			)
	public Object interceptFindHtmlElement(ProceedingJoinPoint joinPoint) throws Throwable {
		if (doOverride()) {
			return new HtmlElement("", (By)(joinPoint.getArgs()[0]), currentFrame);
		} else {
			return joinPoint.proceed(joinPoint.getArgs());			
		}
	}
	
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver+.findElements (..))"
			+ ")"			
			)
	public Object interceptFindsHtmlElement(ProceedingJoinPoint joinPoint) throws Throwable {
		if (doOverride()) {
			return new HtmlElement("", (By)(joinPoint.getArgs()[0]), currentFrame).findElements();
		} else {
			return joinPoint.proceed(joinPoint.getArgs());
		}
	}
	
	/**
	 * Method interceptFindHtmlElement creates an HtmlElement from findElement, but does not handle frames. 
	 * Here, we record all switchTo().frame(WebElement) call to create a FrameElement chain
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver.TargetLocator+.frame (..))"
			+ ")"			
			)
	public Object recordSwitchToFramCalls(ProceedingJoinPoint joinPoint) throws Throwable {
		if (doOverride()) {
			Object frameArg = joinPoint.getArgs()[0];
			FrameElement frameEl;
			
			if (frameArg instanceof HtmlElement) {
				frameEl = new FrameElement("", ((HtmlElement)frameArg).getBy());
			} else if (frameArg instanceof Integer) {
				frameEl = new FrameElement("", By.tagName("iframe"), 0);
			} else if (frameArg instanceof String) {
				String name = ((String)frameArg).replaceAll("(['\"\\\\#.:;,!?+<>=~*^$|%&@`{}\\-/\\[\\]\\(\\)])", "\\\\$1");
				frameEl = new FrameElement("", By.cssSelector("frame[name='" + name + "'],iframe[name='" + name + "'],frame#" + name + ",iframe#" + name));
			} else {
				return joinPoint.proceed(joinPoint.getArgs());
			}

			if (currentFrame == null) {
				currentFrame = frameEl;
			} else {
				frameEl.setFrameElement(currentFrame);
				currentFrame = frameEl;
			}
			return null;
		} else {
			return joinPoint.proceed(joinPoint.getArgs());
		}

	}
	
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver.TargetLocator+.defaultContent (..))"
			+ ")"			
			)
	public Object recordSwitchDefaultContext(ProceedingJoinPoint joinPoint) throws Throwable {
		currentFrame = null;
		return joinPoint.proceed(joinPoint.getArgs());
	}
	
	
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver.TargetLocator+.parentFrame (..))"
			+ ")"			
			)
	public Object recordSwitchParentFrame(ProceedingJoinPoint joinPoint) throws Throwable {
		if (currentFrame == null || !doOverride()) {
			return joinPoint.proceed(joinPoint.getArgs());
		} else {
			currentFrame = currentFrame.getFrameElement();
		}
		return null;
		
	}
	
	
	
	// TODO: handle findElementBy... (from RemoteWebDriver) => should be useless as SeleniuRobot only expose a WebDriverInstance
	// TODO: check behavior with WebDriverWait & CompositeActions
}
