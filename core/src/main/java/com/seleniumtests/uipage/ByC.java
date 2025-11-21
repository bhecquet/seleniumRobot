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
package com.seleniumtests.uipage;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.seleniumtests.driver.CustomEventFiringWebDriver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchShadowRootException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.WebUIDriver;

public class ByC extends By {
    
    
    public String getEffectiveXPath() {
        throw new NotImplementedException("XPath not implemented");
    }
    
    private static final String ERROR_CANNOT_FIND_ELEMENT_WITH_SUCH_CRITERIA = "Cannot find element with such criteria ";
    
    /**
     * All classes that inherit this class force the use of xpath to search elements.
     * Helps to do salesforce tests when pseudo shadow DOM is used
     * @author S047432
     *
     */
    public abstract static class ByForcedXPath extends By implements Serializable {
        
        private static final long serialVersionUID = 4699295846976948351L;
        
    }
    
    public abstract static class ByHasCssSelector extends ByC implements Serializable {
        
        private static final long serialVersionUID = 4699295846976949851L;
        
        protected boolean useCssSelector;
        
        public boolean isUseCssSelector() {
            return useCssSelector;
        }
        
        public void setUseCssSelector(boolean useCssSelector) {
            this.useCssSelector = useCssSelector;
        }
        
        public abstract String getEffectiveCssSelector();
        
    }
    
    @Override
    public List<WebElement> findElements(SearchContext context) {
        return new ArrayList<>();
    }
    
    /**
     * Search first 'input' element after label referenced by name
     * @param label
     * @return
     */
    public static ByC labelForward(final String label) {
        return labelForward(label, null, false, null);
    }
    
    /**
     * Search first element for <code>tagName</code> after label referenced by name
     * @param label
     * @param tagName
     * @return
     */
    public static ByC labelForward(final String label, final String tagName) {
        return labelForward(label, tagName, false, null);
    }
    
    /**
     *  Search first element for {@code tagName} after label referenced by partial name
     * Use case is {@code <h2>some label</h2><input type="text" value="" />}
     * @param label			label to search
     * @param tagName		tag name after this label. The element we really search
     * @param labelTagName  if label is not in a {@code <label>} tag, define this tag name
     * @return
     */
    public static ByC labelForward(final String label, final String tagName, final String labelTagName) {
        return labelForward(label, tagName, false, labelTagName);
    }
    
    /**
     * Search first 'input' element after label referenced by partial name
     * @param label
     * @return
     */
    public static ByC partialLabelForward(final String label) {
        return labelForward(label, null, true, null);
    }
    
    /**
     * Search first element for {@code tagName} after label referenced by partial name
     * Use case is {@code <label>some label</label><input type="text" value="" />}
     * @param label
     * @param tagName
     * @return
     */
    public static ByC partialLabelForward(final String label, final String tagName) {
        return labelForward(label, tagName, true, null);
    }
    
    /**
     *  Search first element for {@code tagName} after label referenced by partial name
     * Use case is {@code <h2>some label</h2><input type="text" value="" />}
     * @param label			label to search
     * @param tagName		tag name after this label. The element we really search
     * @param labelTagName  if label is not in a {@code <label>} tag, define this tag name
     * @return
     */
    public static ByC partialLabelForward(final String label, final String tagName, final String labelTagName) {
        return labelForward(label, tagName, true, labelTagName);
    }
    
    private static ByC labelForward(final String label, String tagName, boolean partial, final String labelTagName) {
        return new ByLabelForward(label, tagName, partial, labelTagName);
    }
    
    /**
     * Search first 'input' element before label referenced by name
     * @param label
     * @return
     */
    public static ByC labelBackward(final String label) {
        return labelBackward(label, null, false, null);
    }
    
    /**
     * Search first element for <code>tagName</code> before label referenced by name
     * @param label
     * @param tagName
     * @return
     */
    public static ByC labelBackward(final String label, final String tagName) {
        return labelBackward(label, tagName, false, null);
    }
    public static ByC labelBackward(final String label, final String tagName, final String labelTagName) {
        return labelBackward(label, tagName, false, labelTagName);
    }
    
    /**
     * Search first 'input' element before label referenced by partial name
     * @param label
     * @return
     */
    public static ByC partialLabelBackward(final String label) {
        return labelBackward(label, null, true, null);
    }
    
    /**
     * Search first element for <code>tagName</code> before label referenced by partial name
     * Use case is {@code <input type="text" value="" /><label>some label<label>}
     * @param label
     * @param tagName
     * @return
     */
    public static ByC partialLabelBackward(final String label, final String tagName) {
        return labelBackward(label, tagName, true, null);
    }
    
    /**
     *  Search first element for {@code tagName} before label referenced by partial name
     * Use case is {@code <input type="text" value="" /><h2>some label</h2>}
     * @param label			label to search
     * @param tagName		tag name after this label. The element we really search
     * @param labelTagName  if label is not in a {@code <label>} tag, define this tag name
     * @return
     */
    public static ByC partialLabelBackward(final String label, final String tagName, final String labelTagName) {
        return labelBackward(label, tagName, true, labelTagName);
    }
    
    private static ByC labelBackward(final String label, String tagName, boolean partial, final String labelTagName) {
        return new ByLabelBackward(label, tagName, partial, labelTagName);
    }
    
    /**
     * Search element by attribute name and attribute value
     * Name and value can have some value accepted for CSS selector: <a>https://developer.mozilla.org/en-US/docs/Web/CSS/Attribute_selectors</a>
     * 'attributeName*' => attribute value contains the provided value
     * 'attributeName^' => attribute value starts with the provided value
     * 'attributeName$' => attribute value ends with the provided value
     * 'attributeName' => attribute value equals the provided value
     *
     * @param attributeName
     * @param attributeValue
     * @return
     */
    public static ByC attribute(final String attributeName, final String attributeValue) {
        return new ByAttribute(attributeName, attributeValue);
    }
    
    /**
     * Search first element of <code>tagName</code> with text. TagName may be '*' if you want to search text among all types of elements
     * text can have some value accepted for CSS selector: <a>https://developer.mozilla.org/en-US/docs/Web/CSS/Attribute_selectors</a>
     * 'text*' => text value contains the provided value
     * 'text^' => text value starts with the provided value
     * 'text$' => text value ends with the provided value
     * 'text' => text value equals the provided value
     * @param textToSearch
     * @param tagName
     * @return
     */
    public static ByC partialText(final String textToSearch, final String tagName) {
        return text(textToSearch, tagName, true, false);
    }
    public static ByC text(final String textToSearch, final String tagName) {
        return text(textToSearch, tagName, false, false);
    }

    /**
     * Search for an element with 'tagName' whose text is found inside a sub-element
     * @param textToSearch      The text to search in sub elements
     * @param tagName           The tagname of the element we want to find
     * @return
     */
    public static ByC textInside(final String textToSearch, final String tagName) {
        return text(textToSearch, tagName, false, true);
    }
    
    private static ByC text(final String textToSearch, String tagName, boolean partial, boolean textInSubElement) {
        return new ByText(textToSearch, tagName, partial, textInSubElement);
    }
    
    /**
     * Search an element with several criteria
     * Only returns element(s) which matches all criteria
     * @param bies
     * @return
     */
    public static ByC and(By ... bies) {
        return new And(bies);
    }
    
    /**
     * Searches for a <label> element with the specified label name and returns only the associated <input> element due to the connection between the label's for attribute and the input's id.
     * Element label need to have an attribute "for".
     * Use Case: {@code <label for="input_id">labelName</label><input id="input_id" value="" />}
     * @param labelName The name of the label to search for.
     * @return A By instance that can be used to locate the <input> element associated with the specified label.
     */
    public static By label(String labelName) {
        return new ByLabel(labelName);
    }
    
    /**
     * Search a shadow-root element, when web site uses shadow DOM. You may specify multiple locators to walk through the tree until the last searched shadow-root
     * <pre>{@code
     * <host1>
     * 		#shadow-root
     * 		<host2>
     * 			#shadow-root
     * 			<div id="el" />
     * 		</host2>
     * </host1>
     * </code>}
     * </pre>
     *
     * You would write
     * <pre>{@code
     * HtmlElement shadowRoot = new HtmlElement("", ByC.shadow(By.tagName("host1"), By.tagName("host2")));
     * HtmlElement myElement = new HtmlElement("", By.id("el"), shadowRoot);
     * }</pre>
     *
     * @param bies
     * @return
     */
    public static ByC shadow(By ... bies) {
        return new Shadow(bies);
    }
    
    /**
     * method for searching an element by a locator or an other.
     * It also checks if the locator is relevant to the tested platform (in case of mobile), which allow to write
     *
     * <code>ByC.or(android(By.tagName("input")), ios(By.xpath(""), web(By.id("myId"))</code>
     
     * @author S047432
     *
     */
    public static ByC or(By ... bies) {
        return new Or(bies);
    }

    /**
     * Says that the locator in parameter is for Web only. It doesn't do anything else and should be used with ByC.or() to have an effect
     * @param by
     * @return
     */
    public static ByC web(By by) {
        return new Web(by);
    }

    /**
     * Says that the locator in parameter is for android only. It doesn't do anything else and should be used with ByC.or() to have an effect
     * @param by
     * @return
     */
    public static ByC android(By by) {
        return new Android(by);
    }
    
    /**
     * Says that the locator in parameter is for iOS only. It doesn't do anything else and should be used with ByC.or() to have an effect
     * @param by
     * @return
     */
    public static ByC ios(By by) {
        return new Ios(by);
    }
    
    /**
     * Search an element by id using xpath
     * @param id
     *            The value of the "id" attribute to search for.
     * @return A By which locates elements by the value of the "id" attribute.
     */
    public static By xId(String id) {
        return new ByAttribute("id", id);
    }
    
    /**
     * Search an element by link text using xpath
     * @param linkText
     *            The exact text to match against.
     * @return A By which locates A elements by the exact text it displays.
     */
    public static By xLinkText(String linkText) {
        return new ByText(linkText, "*", false, false);
    }
    
    /**
     * Search an element by partial link text using xpath
     * @param partialLinkText
     *            The partial text to match against
     * @return a By which locates elements that contain the given link text.
     */
    public static By xPartialLinkText(String partialLinkText) {
        return new ByText(partialLinkText, "*", true, false);
    }
    
    /**
     * Search an element by name using xpath
     * @param name
     *            The value of the "name" attribute to search for.
     * @return A By which locates elements by the value of the "name" attribute.
     */
    public static By xName(String name) {
        return new ByAttribute("name", name);
    }
    
    /**
     * Search an element by tag name using xpath
     * @param tagName
     *            The element's tag name.
     * @return A By which locates elements by their tag name.
     */
    public static By xTagName(String tagName) {
        return new ByXTagName(tagName);
    }
    
    /**
     * Find elements based on the value of the "class" attribute. If an element has
     * multiple classes, then this will match against each of them. For example, if
     * the value is "one two onone", then the class names "one" and "two" will
     * match.
     *
     * @param className
     *            The value of the "class" attribute to search for.
     * @return A By which locates elements by the value of the "class" attribute.
     */
    public static By xClassName(String className) {
        return new ByXClassName(className);
    }
    
    protected static String buildSelectorForText(String text) {
        String escapedText;
        if (text.endsWith("*") || text.endsWith("^") || text.endsWith("$")) {
            escapedText = escapeQuotes(text.substring(0, text.length() - 1));
        } else {
            escapedText = escapeQuotes(text);
        }
        
        if (text.endsWith("*")) {
            return String.format("[contains(text(),%s)]", escapedText);
        } else if (text.endsWith("^")) {
            return String.format("[starts-with(text(),%s)]", escapedText);
        } else if (text.endsWith("$")) {
//			return String.format("[ends-with(text(),%s)]", escapedText); //not valid for xpath 1.0
            return String.format("[substring(text(), string-length(text()) - string-length(%s) +1) = %s]", escapedText, escapedText);
        } else {
            return String.format("[text() = %s]", escapedText);
        }
    }
    
    
    public static class ByLabelForward extends ByC implements Serializable {
        
        private static final long serialVersionUID = 5341968046120372161L;
        
        private String label;
        private String tagName;
        private String labelTagName; // tag of the label we are searching. default is label
        private boolean partial;
        
        /**
         *
         * @param label			Content of the label to search
         * @param tagName		Tag name of the element following label, we want to get. Default is "input"
         * @param partial		do we search for partial of full label name
         * @param labelTagName	tag name of the label element. Default is "label"
         */
        public ByLabelForward(String label, String tagName, boolean partial, String labelTagName) {
            
            if (label == null) {
                throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
            }
            
            this.label = label;
            this.tagName = tagName == null ? "input": tagName;
            this.partial = partial;
            this.labelTagName = labelTagName == null ? "label": labelTagName;
        }
        
        @Override
        public String getEffectiveXPath() {
            if (partial && !label.endsWith("*")) {
                label += "*";
            }
            return String.format(".//%s%s/following::%s", labelTagName, buildSelectorForText(label), tagName);
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            return  context.findElements(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            return context.findElement(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public String toString() {
            return String.format("By.label %s:'%s' forward on element %s", labelTagName, label, tagName);
        }
    }
    
    public static class ByLabelBackward extends ByC implements Serializable {
        
        private static final long serialVersionUID = 5341968046120372162L;
        
        private String label;
        private final String tagName;
        private final boolean partial;
        private final String labelTagName; // tag of the label we are searching. default is label
        
        /**
         *
         * @param label			Content of the label to search
         * @param tagName		Tag name of the element following label, we want to get. Default is "input"
         * @param partial		do we search for partial of full label name
         * @param labelTagName	tag name of the label element. Default is "label"
         */
        public ByLabelBackward(String label, String tagName, boolean partial, String labelTagName) {
            
            if (label == null) {
                throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
            }
            
            this.label = label;
            this.tagName = tagName == null ? "input": tagName;
            this.partial = partial;
            this.labelTagName = labelTagName == null ? "label": labelTagName;
        }
        
        @Override
        public String getEffectiveXPath() {
            if (partial && !label.endsWith("*")) {
                label += "*";
            }
            return String.format(".//%s%s/preceding::%s", labelTagName, buildSelectorForText(label), tagName);
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            return context.findElements(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            List<WebElement> elements;
            elements = context.findElements(By.xpath(getEffectiveXPath()));
            List<WebElement> elementsReverse = elements.subList(0, elements.size());
            Collections.reverse(elementsReverse);
            return elementsReverse.get(0);
        }
        
        @Override
        public String toString() {
            return String.format("By.label %s:'%s' backward on element %s", labelTagName, label, tagName);
        }
    }
    
    
    public static class ByAttribute extends ByHasCssSelector implements Serializable {
        
        private static final long serialVersionUID = 5341968046120372161L;
        
        private String attributeName;
        private String attributeValue;
        
        public ByAttribute(String attributeName, String attributeValue) {
            
            if (attributeName == null) {
                throw new IllegalArgumentException("Cannot find elements with a null attribute.");
            }
            if (attributeValue == null) {
                throw new IllegalArgumentException("Cannot find elements with a null attribute value.");
            }
            
            this.attributeName = attributeName;
            this.attributeValue = attributeValue;
        }
        
        @Override
        public String getEffectiveXPath() {
            return String.format(".//*%s", buildSelector());
        }
        
        /**
         *Build a xpath selector so that we understand the CSS syntax: https://www.w3schools.com/cssref/css_selectors.asp
         * '*' => contains
         * '^' => starts with
         * '$' => ends with
         * @return
         */
        private String buildSelector() {
            String escapedAttributeValue = escapeQuotes(attributeValue);
            
            if (attributeName.endsWith("*")) {
                String tmpAttributeName = attributeName.substring(0, attributeName.length() - 1);
                return String.format("[contains(@%s,%s)]", tmpAttributeName, escapedAttributeValue);
            } else if (attributeName.endsWith("^")) {
                String tmpAttributeName = attributeName.substring(0, attributeName.length() - 1);
                return String.format("[starts-with(@%s,%s)]", tmpAttributeName, escapedAttributeValue);
            } else if (attributeName.endsWith("$")) {
                String tmpAttributeName = attributeName.substring(0, attributeName.length() - 1);
                //return String.format("[ends-with(@%s,%s)]", attributeName, escapedAttributeValue); // would by valid with xpath 2.0
                return String.format("[substring(@%s, string-length(@%s) - string-length(%s) +1) = %s]", tmpAttributeName, tmpAttributeName, escapedAttributeValue, escapedAttributeValue);
            } else {
                return String.format("[@%s=%s]", attributeName, escapedAttributeValue);
            }
        }

        private String buildAndroidUiSelector() {
            String selector = null;
            if (attributeName.equals("text*")) {
                selector = String.format("textContains(\"%s\")", attributeValue);
            } else if (attributeName.equals("text^")) {
                selector = String.format("textStartsWith(\"%s\")", attributeValue);
            } else if (attributeName.equals("text$")) {
                selector = String.format("textMatches(\"%s\")", attributeValue);
            } else if (attributeName.equals("text")) {
                selector = String.format("text(\"%s\")", attributeValue);
            } else if (attributeName.equals("content-desc*")) {
                selector = String.format("descriptionContains(\"%s\")", attributeValue);
            } else if (attributeName.equals("content-desc^")) {
                selector = String.format("descriptionStartsWith(\"%s\")", attributeValue);
            } else if (attributeName.equals("content-desc$")) {
                selector = String.format("descriptionMatches(\"%s\")", attributeValue);
            } else if (attributeName.equals("content-desc")) {
                selector = String.format("description(\"%s\")", attributeValue);
            } else if (attributeName.equals("resource-id$")) {
                selector = String.format("resourceIdMatches(\"%s\")", attributeValue);
            } else if (attributeName.equals("resource-id")) {
                selector = String.format("resourceId(\"%s\")", attributeValue);
            } else {
                return null;
            }
            return String.format("new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().%s.instance(0))", selector);
        }
        
        @Override
        public String getEffectiveCssSelector() {
            return String.format("[%s=%s]", attributeName, attributeValue);
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            CustomEventFiringWebDriver currentDriver = WebUIDriver.getWebDriver(false);
            String androidSelector = null;
            if (!currentDriver.isWebTest()
                    && currentDriver.getOriginalDriver() instanceof AndroidDriver) {
                androidSelector = buildAndroidUiSelector();
            }
            if (androidSelector != null) {
                return context.findElements(AppiumBy.androidUIAutomator(androidSelector));
            } else {
                if (useCssSelector) {
                    return context.findElements(By.cssSelector(getEffectiveCssSelector()));
                } else {
                    return context.findElements(By.xpath(getEffectiveXPath()));
                }
            }
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            CustomEventFiringWebDriver currentDriver = WebUIDriver.getWebDriver(false);
            String androidSelector = null;
            if (!currentDriver.isWebTest()
                    && currentDriver.getOriginalDriver() instanceof AndroidDriver) {
                androidSelector = buildAndroidUiSelector();
            }
            if (androidSelector != null) {
                return context.findElement(AppiumBy.androidUIAutomator(androidSelector));
            } else {
                if (useCssSelector) {
                    return context.findElement(By.cssSelector(getEffectiveCssSelector()));
                } else {
                    return context.findElement(By.xpath(getEffectiveXPath()));
                }
            }
        }
        
        @Override
        public String toString() {
            return String.format("By.attribute: %s='%s'", attributeName, attributeValue);
        }
    }
    
    /**
     * Find element with the text content given
     * for text inside the searched element, it will be like "//div[contains(text(), '1min 49s')]"
     * for text inside a child element, it will be like "//*[contains(@class, 'stage-cell-0 SUCCESS') and .//*[contains(text(), '1min 49s')]]"
     */
    public static class ByText extends ByC implements Serializable {
        
        private static final long serialVersionUID = 5341968046120372161L;
        
        private String text;
        private String tagName;
        private boolean partial;
        private boolean textInSubElement;
        
        public ByText(String text, String tagName, boolean partial, boolean textInSubElement) {
            
            if (text == null) {
                throw new IllegalArgumentException("Cannot find elements with a null text content.");
            }
            if (tagName == null) {
                throw new IllegalArgumentException("Cannot find elements with a null tagName.");
            }
            
            this.text = text;
            this.tagName = tagName;
            this.partial = partial;
            this.textInSubElement = textInSubElement;
        }
        
        @Override
        public String getEffectiveXPath() {
            if (partial && !text.endsWith("*")) {
                text += "*";
            }
            if (textInSubElement) {
                return String.format(".//%s[* and .//*%s]", tagName, buildSelectorForText(text));
            } else {
                return String.format(".//%s%s", tagName, buildSelectorForText(text));
            }
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            return context.findElements(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            if (textInSubElement) {
                List<WebElement> elements = context.findElements(By.xpath(getEffectiveXPath()));
                try {
                    return elements.get(elements.size() - 1);
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException("Cannot find any element by text " + text);
                }
            } else {
                return context.findElement(By.xpath(getEffectiveXPath()));
            }
        }
        
        
        @Override
        public String toString() {
            return String.format("%s By.text: '%s'", tagName, text);
        }
    }
    
    /**
     * Allow to search elements with several criteria
     * It will create intersection between a search for each criteria
     * @author s047432
     *
     */
    public static class And extends ByC implements Serializable {
        
        /**
         *
         */
        private static final long serialVersionUID = 6341968046120372161L;
        private transient By[] bies;
        
        public And(By ... bies) {
            if (bies.length == 0) {
                throw new ScenarioException("At least on locator must be provided for And");
            }
            
            for (By by : bies) {
                if (by == null) {
                    throw new IllegalArgumentException("Cannot find elements with a null element");
                }
            }
            
            this.bies = bies;
        }
        
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            List<WebElement> elements = bies[0].findElements(context);
            for (int i = 1; i < bies.length; i++) {
                elements = ListUtils.retainAll(elements, bies[i].findElements(context));
            }
            return elements;
            
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            try {
                return findElements(context).get(0);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException(ERROR_CANNOT_FIND_ELEMENT_WITH_SUCH_CRITERIA + toString());
            }
        }
        
        
        @Override
        public String toString() {
            
            List<String> biesString = new ArrayList<>();
            for (By by: bies) {
                biesString.add(by.toString());
            }
            
            return String.join(" and ", biesString);
        }
    }
    
    public static class Shadow extends ByC implements Serializable {
        
        
        private static final long serialVersionUID = 6341668046120372161L;

        private transient By[] bies;
        
        public Shadow(By ... bies) {
            if (bies.length == 0) {
                throw new ScenarioException("At least one locator must be provided for shadow ");
            }
            this.bies = bies;
        }
        
        
        /**
         * If multiple "By" are provided
         */
        @Override
        public List<WebElement> findElements(SearchContext context) {
            
            List<WebElement> elements = new ArrayList<>();
            
            
            for (By by: bies) {
                
                List<WebElement> hosts;
                if (elements.isEmpty()) { // first iteration
                    hosts = by.findElements(context);
                } else {
                    hosts = elements.get(0).findElements(by);
                    elements = new ArrayList<>(); // reset list because we don't care parent elements
                }
                
                for (WebElement host: hosts) {
                    SearchContext root;
                    try {
                        root = host.getShadowRoot();
                    } catch (NoSuchShadowRootException e) {
                        continue;
                        // do nothing, it may happen when one of the found elements is not a shadow root (e.g: search by tagName)
                    }

                    try {
                        // https://github.com/SeleniumHQ/selenium/issues/10127 => invalid locator raised when using ShadowRoot directly
                        // so we build a remoteWebElement which works
                        Method getIdMethod = root.getClass().getMethod("getId");
                        getIdMethod.setAccessible(true);
                        String id = (String) getIdMethod.invoke(root);
                        RemoteWebElement shadowRootElement = new RemoteWebElement();
                        shadowRootElement.setParent((RemoteWebDriver) ((WrapsDriver)root).getWrappedDriver());
                        shadowRootElement.setId(id);
                        elements.add(shadowRootElement);
                        
                    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new CustomSeleniumTestsException("A change in Selenium occured, ByC.Shadow fails");
                    }
                }
                
                // stop if no element is found
                if (elements.isEmpty()) {
                    return new ArrayList<>();
                }
            }
            return elements;
            
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            try {
                return findElements(context).get(0);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException(ERROR_CANNOT_FIND_ELEMENT_WITH_SUCH_CRITERIA + toString());
            }
        }
        
        
        @Override
        public String toString() {
            
            List<String> biesString = new ArrayList<>();
            for (By by: bies) {
                biesString.add(by.toString());
            }
            
            return String.join("/", biesString);
        }

        public By[] getBies() {
            return bies;
        }

        public void setBies(By[] bies) {
            this.bies = bies;
        }
        
    }
    
    /**
     * Class for searching an element by a locator or an other.
     * It also checks if the locator is relevant to the tested platform (in case of mobile), which allow to write
     *
     * <code>ByC.or(android(By.tagName("input")), ios(By.xpath("")))</code>
     
     * @author S047432
     *
     */
    public static class Or extends ByC implements Serializable {
        
        private transient By[] bies;
        
        private static final long serialVersionUID = 6341968046167372161L;
        
        public Or(By ... bies) {
            if (bies.length == 0) {
                throw new ScenarioException("At least on locator must be provided");
            }
            
            for (By by : bies) {
                if (by == null) {
                    throw new IllegalArgumentException("Cannot find elements with a null element");
                }
            }
            
            this.bies = bies;
        }
        
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            
            String platform = SeleniumTestsContextManager.getThreadContext().getPlatform();
            boolean webTest = SeleniumTestsContextManager.isWebTest();
            List<WebElement> elements = new ArrayList<>();
            
            
            for (By by: bies) {
                // check this 'by' applies to the platform
                if ((by instanceof Android && !platform.equalsIgnoreCase("android"))
                        || (by instanceof Ios && !platform.equalsIgnoreCase("ios"))
                        || ((by instanceof Ios || by instanceof Android) && webTest)
                        || (by instanceof Web && !webTest)
                ) {
                    continue;
                } else if (by instanceof ByPlatformSpecific) {
                    by = ((ByPlatformSpecific) by).getBy();
                }
                elements = by.findElements(context);
                
                // stop once at least an element is found
                if (!elements.isEmpty()) {
                    break;
                }
            }
            return elements;
            
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            try {
                return findElements(context).get(0);
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException(ERROR_CANNOT_FIND_ELEMENT_WITH_SUCH_CRITERIA + toString());
            }
        }
        
        
        @Override
        public String toString() {
            
            List<String> biesString = new ArrayList<>();
            for (By by: bies) {
                biesString.add(by.toString());
            }
            
            return String.join(" or ", biesString);
        }
    }
    
    /**
     *
     * Selector for specifying that the underlying selector is specific to web
     * This is usefull using ByC.or, when an element is the same in web and mobile but with different selector strategies
     *
     */
    public static class Web extends ByC implements Serializable, ByPlatformSpecific {
        
        private static final long serialVersionUID = 6341968046120092151L;
        
        private transient By by;
        
        public Web(By by) {
            this.by = by;
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            throw new UnsupportedOperationException("You cannot use ByC.web directly");
            
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            throw new UnsupportedOperationException("You cannot use ByC.web directly");
        }
        
        
        @Override
        public String toString() {
            return String.format("web[%s]", by.toString());
        }
        
        @Override
        public By getBy() {
            return by;
        }
    }

    /**
     * Selector for specifying that the underlying selector is specific to android
     */
    public static class Android extends ByC implements Serializable, ByPlatformSpecific {

        private static final long serialVersionUID = 6341968046120092161L;

        private transient By by;

        public Android(By by) {
            this.by = by;
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
            throw new UnsupportedOperationException("You cannot use ByC.android directly");

        }

        @Override
        public WebElement findElement(SearchContext context) {
            throw new UnsupportedOperationException("You cannot use ByC.android directly");
        }


        @Override
        public String toString() {
            return String.format("android[%s]", by.toString());
        }

        @Override
        public By getBy() {
            return by;
        }
    }
    
    /**
     * Selector for specifying that the underlying selector is specific to Ios
     */
    public static class Ios extends ByC implements Serializable, ByPlatformSpecific {
        
        
        private static final long serialVersionUID = 6341468046120372161L;
        private transient By by;
        
        public Ios(By by) {
            this.by = by;
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            throw new UnsupportedOperationException("You cannot use ByC.ios directly");
            
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            throw new UnsupportedOperationException("You cannot use ByC.ios directly");
        }
        
        
        @Override
        public String toString() {
            return String.format("ios[%s]", by.toString());
        }
        
        @Override
        public By getBy() {
            return by;
        }
    }
    
    
    
    public static class ByXTagName extends ByForcedXPath implements Serializable {
        
        private static final long serialVersionUID = 4699295846984948351L;
        
        private final String tagName;
        
        public ByXTagName(String tagName) {
            if (tagName == null) {
                throw new IllegalArgumentException("Cannot find elements when the tag name is null.");
            }
            
            this.tagName = tagName;
        }
        
        public String getEffectiveXPath() {
            return ".//" + tagName;
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            return context.findElements(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            return context.findElement(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public String toString() {
            return "By.tagName: " + tagName;
        }
    }
    
    public static class ByXClassName extends ByForcedXPath implements Serializable {
        
        private static final long serialVersionUID = -8737882849130394673L;
        
        private final String className;
        
        public ByXClassName(String className) {
            if (className == null) {
                throw new IllegalArgumentException(
                        "Cannot find elements when the class name expression is null.");
            }
            
            this.className = className;
        }
        
        public String getEffectiveXPath() {
            return (".//*[" + containingWord("class", className) + "]");
        }
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            return context.findElements(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            return context.findElement(By.xpath(getEffectiveXPath()));
        }
        
        /**
         * Generate a partial XPath expression that matches an element whose specified attribute
         * contains the given CSS word. So to match &lt;div class='foo bar'&gt; you would say "//div[" +
         * containingWord("class", "foo") + "]".
         *
         * @param attribute name
         * @param word name
         * @return XPath fragment
         */
        private String containingWord(String attribute, String word) {
            return "contains(concat(' ',normalize-space(@" + attribute + "),' '),' " + word + " ')";
        }
        
        @Override
        public String toString() {
            return "By.className: " + className;
        }
    }
    
    
    protected static String escapeQuotes(String aString) {
        if (!aString.contains("'")) {
            return "'" + aString + "'";
        } else {
            StringBuilder newString = new StringBuilder("concat(");
            for (String part: aString.split("'")) {
                newString.append("'" + part + "',\"'\",");
            }
            return newString.substring(0, newString.length() - 5) + ")";
        }
    }
    
    /**
     * Find element with the for content given
     *
     */
    public static class ByLabel extends ByForcedXPath implements Serializable {
        
        private static final long serialVersionUID = 5341968046120372161L;
        
        private String label;
        
        /**
         * @param label Value of the "for" attribute of the label to search.
         */
        public ByLabel(String label) {
            if (label == null) {
                throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
            }
            this.label = label;
        }
        
        public String getEffectiveXPath() {
            return String.format("//input[@id=string(//label[.='%s']/@for)]", label);
        }
        
        @Override
        public WebElement findElement(SearchContext context) {
            return context.findElement(By.xpath(getEffectiveXPath()));
        }
        
        
        @Override
        public List<WebElement> findElements(SearchContext context) {
            return context.findElements(By.xpath(getEffectiveXPath()));
        }
        
        @Override
        public String toString() {
            return String.format("By.label '%s' on element", label);
        }
    }
    
}
