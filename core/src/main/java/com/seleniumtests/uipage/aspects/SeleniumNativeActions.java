/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.FrameElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;

@Aspect
public class SeleniumNativeActions {

	private Map<WebUIDriver, FrameElement> currentFrame = new HashMap<>();

	private Boolean doOverride() {
		return SeleniumTestsContextManager.getThreadContext().getOverrideSeleniumNativeAction();
	}
	
	private FrameElement getCurrentFrame() {
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		if (uiDriver == null) {
			return null;
		}
		if (!currentFrame.containsKey(uiDriver)) {
			currentFrame.put(uiDriver, null);
		}
		
		return currentFrame.get(uiDriver);
	}
	
	private void setCurrentFrame(FrameElement frame) {
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		if (uiDriver == null) {
			return;
		}
		currentFrame.put(uiDriver, frame);
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
		System.out.println("coucou");
		if (doOverride()) {
			return new HtmlElement("", (By)(joinPoint.getArgs()[0]), getCurrentFrame());
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
			return new HtmlElement("", (By)(joinPoint.getArgs()[0]), getCurrentFrame()).findElements();
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
			"(call(public * org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt (..))"
			+ ")"			
			)
	public Object recordFrameSwitch(ProceedingJoinPoint joinPoint) throws Throwable {
		if (doOverride()) {
			Object frameArg = joinPoint.getArgs()[0];
			FrameElement frameEl = getFrameElement(frameArg);
			
			if (frameEl == null) {
				return joinPoint.proceed(joinPoint.getArgs());
			}
			
			if (getCurrentFrame() == null) {
				setCurrentFrame(frameEl);
			} else {
				frameEl.setFrameElement(getCurrentFrame());
				setCurrentFrame(frameEl);
			}

			return new ExpectedCondition<WebDriver>() {
			      @Override
			      public WebDriver apply(WebDriver driver) {
			        try {
			          return driver;
			        } catch (NoSuchFrameException e) {
			          return null;
			        }
			      }

			      @Override
			      public String toString() {
			        return "frame to be available: " + frameArg;
			      }
			    };
			
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
			FrameElement frameEl = getFrameElement(frameArg);
			
			if (frameEl == null) {
				return joinPoint.proceed(joinPoint.getArgs());
			}

			if (getCurrentFrame() == null) {
				setCurrentFrame(frameEl);
			} else {
				frameEl.setFrameElement(getCurrentFrame());
				setCurrentFrame(frameEl);
			}
			return null;
		} else {
			return joinPoint.proceed(joinPoint.getArgs());
		}
	}
	
	/**
	 * Returns a FrameElement based on the object passed in argument
	 * @param frameArg
	 * @return
	 */
	private FrameElement getFrameElement(Object frameArg) {
		FrameElement frameEl = null;
		
		if (frameArg instanceof HtmlElement) {
			frameEl = new FrameElement("", ((HtmlElement)frameArg).getBy());
		} else if (frameArg instanceof By) {
			frameEl = new FrameElement("", (By)frameArg);
		} else if (frameArg instanceof Integer) {
			frameEl = new FrameElement("", By.tagName("iframe"), (Integer) frameArg);
		} else if (frameArg instanceof String) {
			String name = ((String)frameArg).replaceAll("(['\"\\\\#.:;,!?+<>=~*^$|%&@`{}\\-/\\[\\]\\(\\)])", "\\\\$1");
			frameEl = new FrameElement("", By.cssSelector("frame[name='" + name + "'],iframe[name='" + name + "'],frame#" + name + ",iframe#" + name));
		}
		return frameEl;
	}
	
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver.TargetLocator+.defaultContent (..))"
			+ ")"			
			)
	public Object recordSwitchDefaultContext(ProceedingJoinPoint joinPoint) throws Throwable {
		setCurrentFrame(null);
		return joinPoint.proceed(joinPoint.getArgs());
	}
	
	
	@Around("this(com.seleniumtests.uipage.PageObject) && " +					// caller is a PageObject
			"(call(public * org.openqa.selenium.WebDriver.TargetLocator+.parentFrame (..))"
			+ ")"			
			)
	public Object recordSwitchParentFrame(ProceedingJoinPoint joinPoint) throws Throwable {
		if (getCurrentFrame() == null || !doOverride()) {
			return joinPoint.proceed(joinPoint.getArgs());
		} else {
			setCurrentFrame(getCurrentFrame().getFrameElement());
		}
		return null;
		
	}
	
	
	
	// TODO: handle findElementBy... (from RemoteWebDriver) => should be useless as SeleniuRobot only expose a WebDriverInstance
	// TODO: check behavior with WebDriverWait & CompositeActions
}
