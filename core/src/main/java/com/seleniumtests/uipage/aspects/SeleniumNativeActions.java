package com.seleniumtests.uipage.aspects;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

@Aspect
public class SeleniumNativeActions {

	private FrameElement currentFrame;
	
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
		return new HtmlElement("", (By)(joinPoint.getArgs()[0]));
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

	}
	
	
	
	// TODO: handle frames (do not reset state when this mode is used (see ReplayAction)
	// TODO: handle findElementBy... (from RemoteWebDriver) => should be useless as SeleniuRobot only expose a WebDriverInstance
	// TODO: check behavior with WebDriverWait & CompositeActions
}
