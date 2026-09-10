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

import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Coordinates;

import com.seleniumtests.driver.CustomEventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Element which combines several HtmlElement. It's used to check the presence of several elements at once, using
 * {@link #isElementPresent()} which returns true only if ALL underlying elements are present.
 * 
 * As this element does not target a single real element, none of the other HtmlElement methods are usable and they
 * all throw a {@link NotImplementedException}.
 */
public class CombinedElement extends HtmlElement {

	private static final String NOT_IMPLEMENTED_MESSAGE = "This method is not implemented for CombinedElement";

	private final List<HtmlElement> elements;

	public CombinedElement(HtmlElement... elements) {
		this(Arrays.asList(elements));
	}

	public CombinedElement(List<HtmlElement> elements) {
		super("combined", By.id(""));
		this.elements = new ArrayList<>(elements);
	}

	public List<HtmlElement> getElements() {
		return elements;
	}

	/**
	 * Element is considered present if and only if all combined elements are present
	 */
	@Override
	public boolean isElementPresent(int timeout) {
		for (HtmlElement element : elements) {
			if (!element.isElementPresent(timeout)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Element is considered present if and only if all combined elements are present
	 */
	@Override
	public boolean isElementPresent() {
		for (HtmlElement element : elements) {
			if (!element.isElementPresent()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void click() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void clickAction() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void rightClickAction() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void doubleClickAction() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void clickMouse() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void rightClickMouse() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void clickAt(int xOffset, int yOffset) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void simulateClick() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void simulateDoubleClick() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void simulateSendKeys(CharSequence... keysToSend) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void simulateMoveToElement(int x, int y) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public List<WebElement> findElements(@NotNull By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public List<WebElement> findHtmlElements(By childBy) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public List<WebElement> findHtmlElements() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public HtmlElement findElement(@NotNull By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public ButtonElement findButtonElement(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public CheckBoxElement findCheckBoxElement(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public ImageElement findImageElement(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public LabelElement findLabelElement(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public LinkElement findLinkElement(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public RadioButtonElement findRadioButtonElement(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public SelectList findSelectList(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Table findTable(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public TextFieldElement findTextFieldElement(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public HtmlElement findElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public ButtonElement findButtonElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public CheckBoxElement findCheckBoxElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public ImageElement findImageElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public LabelElement findLabelElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public LinkElement findLinkElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public RadioButtonElement findRadioButtonElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public SelectList findSelectList(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Table findTable(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public TextFieldElement findTextFieldElement(By by, Integer index) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void changeCssAttribute(String cssProperty, String cssPropertyValue) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Object executeScript(String javascript, Object... args) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void findElement(boolean waitForVisibility, boolean makeVisible) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void replaceSelector() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void scrollToElement(int yOffset) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public List<WebElement> findElements() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getAttribute(@NotNull String name) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getDomAttribute(@NotNull String name) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getDomProperty(@NotNull String name) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getAriaRole() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getAccessibleName() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public SearchContext getShadowRoot() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public By getBy() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getCssValue(@NotNull String propertyName) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public CustomEventFiringWebDriver updateDriver() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public CustomEventFiringWebDriver getDriver() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setDriver(CustomEventFiringWebDriver driver) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public WebElement getElement() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public int getHeight() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Point getLocation() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Rectangle getRect() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Dimension getSize() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getTagName() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getText() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getValue() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public int getWidth() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isDisplayed() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isDisplayedRetry() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isElementPresentAndDisplayed(int timeout) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isElementPresentAndDisplayed() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isEnabled() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isSelected() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isTextPresent(String pattern) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void simulateMouseOver() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void sendKeys(@NotNull CharSequence... keysToSend) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void sendKeys(boolean blurAfter, CharSequence... keysToSend) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void sendKeysAction(long duration, CharSequence... keysToSend) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void sendKeysAction(CharSequence... keysToSend) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void sendKeysKeyboard(CharSequence... keysToSend) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void sendKeys(boolean clear, boolean blurAfter, CharSequence... keysToSend) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void clear() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String findPattern(Pattern pattern, String attributeName) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String findLink(String attributeName) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String toHTML() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String toString() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void doNothing() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Point getCenter() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForPresent() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForNotPresent() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForVisibility() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForInvisibility() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setImplicitWaitTimeout(double timeout) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setImplicitWaitTimeout(int timeout, TemporalUnit unit) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForPresent(int timeout) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForPresentAndDisplayed(int timeout) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForPresentAndDisplayed() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitFor(int timeout, ExpectedCondition<?> condition) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForNotPresent(int timeout) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForVisibility(int timeout) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void waitForInvisibility(int timeout) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public FrameElement getFrameElement() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setFrameElement(FrameElement frameElement) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public <T extends HtmlElement> T changeFrame(FrameElement frameElement) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public <X> X getScreenshotAs(@NotNull OutputType<X> target) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void submit() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Coordinates getCoordinates() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Map<String, Object> toJson() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	// getParent() is NOT overridden: HtmlElement's constructor calls it internally (before construction
	// completes) to check compatibility between parent and frame. Throwing here would break object creation.
	// It simply returns null, which is correct since a CombinedElement has no parent.

	@Override
	public void setParent(HtmlElement parent) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setBy(By by) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setElement(WebElement element) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public Integer getElementIndex() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public WebElement getRealElement() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setElementIndex(Integer elementIndex) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public boolean isScrollToElementBeforeAction() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public void setScrollToElementBeforeAction(boolean scrollToElementBeforeAction) {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getName() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public String getType() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}

	@Override
	public By getSelector() {
		throw new NotImplementedException(NOT_IMPLEMENTED_MESSAGE);
	}
}
