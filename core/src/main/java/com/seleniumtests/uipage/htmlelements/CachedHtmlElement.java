package com.seleniumtests.uipage.htmlelements;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.seleniumtests.customexception.ScenarioException;

public class CachedHtmlElement implements WebElement {

	private String tagName;
	private String text;
	private Rectangle rectangle;
	private Map<String, String> attributes;
	
	public CachedHtmlElement(WebElement elementToCache) {
		rectangle = elementToCache.getRect();
	}
	
	@Override
	public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
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
		return tagName;
	}

	@Override
	public String getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public boolean isSelected() {
		return (("option".equals(tagName) && "selected".equals(attributes.get("selected")))
				|| ("input".equals(tagName) && "checkbox".equals(attributes.get("type")) && "true".equals(attributes.get("checked")))
				|| ("input".equals(tagName) && "radio".equals(attributes.get("type")) && "true".equals(attributes.get("checked")))
			);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public <T extends WebElement> List<T> findElements(By by) {
		return new ArrayList<>();
	}

	@Override
	public <T extends WebElement> T findElement(By by) {
		return null;
	}

	@Override
	public boolean isDisplayed() {
		return true;
	}

	@Override
	public Point getLocation() {
		return new Point(rectangle.x, rectangle.y);
	}

	@Override
	public Dimension getSize() {
		return new Dimension(rectangle.width, rectangle.height);
	}

	@Override
	public Rectangle getRect() {
		return rectangle;
	}

	@Override
	public String getCssValue(String propertyName) {
		return "";
	}

}
