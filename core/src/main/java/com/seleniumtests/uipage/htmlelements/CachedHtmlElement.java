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
package com.seleniumtests.uipage.htmlelements;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;

public class CachedHtmlElement implements WebElement {

	private static final String ERROR_PROBLEM_SEARCHING_BY_FIELD = "problem searching By field during reflection: core should be checked";
	private Rectangle rectangle;
	private Point location;
	private Dimension size;
	private Element cachedElement;
	private WebElement realElement;
	private boolean selected;
	
	public CachedHtmlElement(Element jsoupElement) {
		location = new Point(0, 0);
		size = new Dimension(0, 0);
		rectangle = new Rectangle(location, size);
		selected = false;
		cachedElement = jsoupElement;
		realElement = null;
	}
	
	public CachedHtmlElement(WebElement elementToCache) {

		// position and size take some time for methods that are never used 
		location = new Point(0, 0);
		size = new Dimension(0, 0);
		rectangle = new Rectangle(location, size);
		
		cachedElement = Jsoup.parseBodyFragment(elementToCache.getAttribute("outerHTML")).body().child(0);	
		if ("option".equals(cachedElement.tagName())
				|| ("input".equals(cachedElement.tagName()) && "checkbox".equals(cachedElement.attributes().getIgnoreCase("type")))
				|| ("input".equals(cachedElement.tagName()) && "radio".equals(cachedElement.attributes().getIgnoreCase("type")))
			) {
			selected = elementToCache.isSelected();
		} else {
			selected = false;
		}
		realElement = elementToCache;
		
	}
	
	@Override
	public <X> X getScreenshotAs(OutputType<X> target) {
		throw new ScenarioException("getScreenshotAs cannot be done on a CachedHtmlElement");
	}

	@Override
	public void click() {
		throw new ScenarioException("Click cannot be done on a CachedHtmlElement");
	}

	@Override
	public void submit() {
		throw new ScenarioException("Submit cannot be done on a CachedHtmlElement");
	}

	@Override
	public void sendKeys(CharSequence... keysToSend) {
		throw new ScenarioException("Sendkeys cannot be done on a CachedHtmlElement");
	}

	@Override
	public void clear() {
		throw new ScenarioException("Clear cannot be done on a CachedHtmlElement");
	}

	@Override
	public String getTagName() {
		return cachedElement.tagName();
	}

	@Override
	public String getAttribute(String name) {
		return cachedElement.attributes().getIgnoreCase(name);
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getText() {
		return cachedElement.text();
	}

	@SuppressWarnings("unchecked")
	@Override
	public WebElement findElement(By by) {
		try {
			return findElements(by).get(0);
		} catch (IndexOutOfBoundsException e) {
			throw new NoSuchElementException("Cannot find in cache element located by: " + by);
		}
	}

	@Override
	public List<WebElement> findElements(By by) {
		List<WebElement> foundElements = new ArrayList<>();
		if (by instanceof By.ById) {
			Field field;
			try {
				field = By.ById.class.getDeclaredField("id");
				field.setAccessible(true);
				foundElements.add(new CachedHtmlElement(cachedElement.getElementById((String)field.get(by))));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new CustomSeleniumTestsException(ERROR_PROBLEM_SEARCHING_BY_FIELD, e);
			}	
		} else if (by instanceof By.ByTagName) {
			Field field;
			try {
				field = By.ByTagName.class.getDeclaredField("tagName");
				field.setAccessible(true);
				foundElements.addAll(cachedElement.getElementsByTag((String)field.get(by))
									.stream()
									.map(CachedHtmlElement::new)
									.collect(Collectors.toList()));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new CustomSeleniumTestsException(ERROR_PROBLEM_SEARCHING_BY_FIELD, e);
			} 
		} else if (by instanceof By.ByClassName) {
			Field field;
			try {
				field = By.ByClassName.class.getDeclaredField("className");
				field.setAccessible(true);
				foundElements.addAll(cachedElement.getElementsByClass((String)field.get(by))
									.stream()
									.map(CachedHtmlElement::new)
									.collect(Collectors.toList()));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new CustomSeleniumTestsException(ERROR_PROBLEM_SEARCHING_BY_FIELD, e);
			} 
		} else if (by instanceof By.ByName) {
			Field field;
			try {
				field = By.ByName.class.getDeclaredField("name");
				field.setAccessible(true);
				foundElements.addAll(cachedElement.getElementsByAttributeValue("name", (String)field.get(by))
									.stream()
									.map(CachedHtmlElement::new)
									.collect(Collectors.toList()));
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new CustomSeleniumTestsException(ERROR_PROBLEM_SEARCHING_BY_FIELD, e);
			} 
		} else if (by instanceof By.ByLinkText || by instanceof By.ByPartialLinkText) {
			Field field;
			try {
				field = By.ByLinkText.class.getDeclaredField("linkText");
				field.setAccessible(true);
				for (Element el: cachedElement.getElementsByTag("a")) {
					try {
						Element element = el.getElementsContainingOwnText((String) field.get(by)).get(0);
						foundElements.add(new CachedHtmlElement(el));
					} catch (IndexOutOfBoundsException e) {
						// nothing to do
					}
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new CustomSeleniumTestsException(ERROR_PROBLEM_SEARCHING_BY_FIELD, e);
			} 
		} else {
			throw new NotImplementedException(String.format("%s is not implemented in cached element", by.getClass()));
		}

		return foundElements;
	}
	
	@Override
	public boolean isDisplayed() {
		return true;
	}

	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public Rectangle getRect() {
		return rectangle;
	}

	@Override
	public String getCssValue(String propertyName) {
		return "";
	}

	public WebElement getRealElement() {
		return realElement;
	}
	
	@Override
	public String toString() {
		return realElement.toString() + "HTML: " + cachedElement.outerHtml();
	}


}
