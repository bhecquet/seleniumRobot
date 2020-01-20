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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByXPath;

import com.seleniumtests.customexception.ScenarioException;

public class ByC extends By {

	@Override
	public List<WebElement> findElements(SearchContext context) {
		return null;
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
	 * @param label
	 * @param tagName
	 * @return
	 */
	public static ByC partialText(final String textToSearch, final String tagName) {
		return text(textToSearch, tagName, true);
	}
	public static ByC text(final String textToSearch, final String tagName) {
		return text(textToSearch, tagName, false);
	}

	private static ByC text(final String textToSearch, String tagName, boolean partial) {
		return new ByText(textToSearch, tagName, partial);
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
		return new ByText(linkText, "*", false);
	}

	/**
	 * Search an element by partial link text using xpath
	 * @param partialLinkText
	 *            The partial text to match against
	 * @return a By which locates elements that contain the given link text.
	 */
	public static By xPartialLinkText(String partialLinkText) {
		return new ByText(partialLinkText, "*", true);
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
		public List<WebElement> findElements(SearchContext context) {
			if (partial && !label.endsWith("*")) {
				label += "*";
			}
			return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s%s/following::%s", labelTagName, buildSelectorForText(label), tagName));
		}

		@Override
		public WebElement findElement(SearchContext context) {
			if (partial && !label.endsWith("*")) {
				label += "*";
			}
			return ((FindsByXPath) context).findElementByXPath(String.format(".//%s%s/following::%s", labelTagName, buildSelectorForText(label), tagName));
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
		public List<WebElement> findElements(SearchContext context) {
			if (partial && !label.endsWith("*")) {
				label += "*";
			}
			return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s%s/preceding::%s", labelTagName, buildSelectorForText(label), tagName));
		}
		
		@Override
		public WebElement findElement(SearchContext context) {
			List<WebElement> elements;
			if (partial && !label.endsWith("*")) {
				label += "*";
			}
			elements = ((FindsByXPath) context).findElementsByXPath(String.format(".//%s%s/preceding::%s", labelTagName, buildSelectorForText(label), tagName));
			List<WebElement> elementsReverse = elements.subList(0, elements.size());
			Collections.reverse(elementsReverse);
			return elementsReverse.get(0);
		}
		
		@Override
		public String toString() {
			return String.format("By.label %s:'%s' backward on element %s", labelTagName, label, tagName);
		}
	}
	

	public static class ByAttribute extends ByC implements Serializable {

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

		@Override
		public List<WebElement> findElements(SearchContext context) {
			return ((FindsByXPath) context).findElementsByXPath(String.format(".//*%s", buildSelector()));
		}

		@Override
		public WebElement findElement(SearchContext context) {
			return ((FindsByXPath) context).findElementByXPath(String.format(".//*%s", buildSelector()));
		}

		@Override
		public String toString() {
			return String.format("By.attribute: %s='%s'", attributeName, attributeValue);
		}
	}
	
	/**
	 * Find element with the text content given
	 * @author s047432
	 *
	 */
	public static class ByText extends ByC implements Serializable {

		private static final long serialVersionUID = 5341968046120372161L;

		private String text;
		private String tagName;
		private boolean partial;

		public ByText(String text, String tagName, boolean partial) {
			
			if (text == null) {
				throw new IllegalArgumentException("Cannot find elements with a null text content.");
			}
			if (tagName == null) {
				throw new IllegalArgumentException("Cannot find elements with a null tagName.");
			}
			
			this.text = text;
			this.tagName = tagName;
			this.partial = partial;
		}

		@Override
		public List<WebElement> findElements(SearchContext context) {
			if (partial && !text.endsWith("*")) {
				text += "*";
			}
			return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s%s", tagName, buildSelectorForText(text)));
		}

		@Override
		public WebElement findElement(SearchContext context) {
			if (partial && !text.endsWith("*")) {
				text += "*";
			}
			return ((FindsByXPath) context).findElementByXPath(String.format(".//%s%s", tagName, buildSelectorForText(text)));
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
		
		private By[] bies;
		
		public And(By ... bies) {
			if (bies.length == 0) {
				throw new ScenarioException("At least on locator must be provided");
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
				throw new NoSuchElementException("Cannot find element with such criteria " + toString());
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

	  public static class ByXTagName extends By implements Serializable {

	    private static final long serialVersionUID = 4699295846984948351L;

	    private final String tagName;

	    public ByXTagName(String tagName) {
	      if (tagName == null) {
	        throw new IllegalArgumentException("Cannot find elements when the tag name is null.");
	      }

	      this.tagName = tagName;
	    }

	    @Override
	    public List<WebElement> findElements(SearchContext context) {
	      return ((FindsByXPath) context).findElementsByXPath(".//" + tagName);
	    }

	    @Override
	    public WebElement findElement(SearchContext context) {
	      return ((FindsByXPath) context).findElementByXPath(".//" + tagName);
	    }

	    @Override
	    public String toString() {
	      return "By.tagName: " + tagName;
	    }
	  }

	  public static class ByXClassName extends By implements Serializable {

	    private static final long serialVersionUID = -8737882849130394673L;

	    private final String className;

	    public ByXClassName(String className) {
	      if (className == null) {
	        throw new IllegalArgumentException(
	            "Cannot find elements when the class name expression is null.");
	      }

	      this.className = className;
	    }

	    @Override
	    public List<WebElement> findElements(SearchContext context) {
	      return ((FindsByXPath) context).findElementsByXPath(".//*[" + containingWord("class", className) + "]");
	    }

	    @Override
	    public WebElement findElement(SearchContext context) {
	      return ((FindsByXPath) context).findElementByXPath(".//*[" + containingWord("class", className) + "]");
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
			String newString = "concat(";
			for (String part: aString.split("'")) {
				newString += "'" + part + "',\"'\",";
			}
			return newString.substring(0, newString.length() - 5) + ")";
		}
	}

}
