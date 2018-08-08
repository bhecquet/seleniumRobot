/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByXPath;

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
	 * Name and value can have value accepted for CSS selector: <a>https://developer.mozilla.org/en-US/docs/Web/CSS/Attribute_selectors</a>
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	public static ByC attribute(final String attributeName, final String attributeValue) {
		return new ByAttribute(attributeName, attributeValue);
	}
	
	/**
	 * Search first element of <code>tagName</code> with text
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

	public static class ByLabelForward extends ByC implements Serializable {

		private static final long serialVersionUID = 5341968046120372161L;

		private final String label;
		private final String tagName;
		private final String labelTagName; // tag of the label we are searching. default is label
		private final boolean partial;

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
			String escapedLabel = escapeQuotes(label);
			if (partial) {
				return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[contains(text(),%s)]/following::%s", labelTagName, escapedLabel, tagName));
			} else {
				return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[text() = %s]/following::%s", labelTagName, escapedLabel, tagName));
			}
		}

		@Override
		public WebElement findElement(SearchContext context) {
			String escapedLabel = escapeQuotes(label);
			if (partial) {
				return ((FindsByXPath) context).findElementByXPath(String.format(".//%s[contains(text(),%s)]/following::%s", labelTagName, escapedLabel, tagName));
			} else {
				return ((FindsByXPath) context).findElementByXPath(String.format(".//%s[text() = %s]/following::%s", labelTagName, escapedLabel, tagName));
			}
		}

		@Override
		public String toString() {
			return "By.label forward: " + label;
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
			String escapedLabel = escapeQuotes(label);
			if (partial) {
				return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[contains(text(),%s)]/preceding::%s", labelTagName, escapedLabel, tagName));
			} else {
				return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[text() = %s]/preceding::%s", labelTagName, escapedLabel, tagName));
			}
		}
		
		@Override
		public WebElement findElement(SearchContext context) {
			String escapedLabel = escapeQuotes(label);
			List<WebElement> elements;
			if (partial) {
				elements = ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[contains(text(),%s)]/preceding::%s", labelTagName, escapedLabel, tagName));
			} else {
				elements = ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[text() = %s]/preceding::%s", labelTagName, escapedLabel, tagName));
			}
			List<WebElement> elementsReverse = elements.subList(0, elements.size());
			Collections.reverse(elementsReverse);
			return elementsReverse.get(0);
		}
		
		@Override
		public String toString() {
			return "By.label backward: " + label;
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
				attributeName = attributeName.substring(0, attributeName.length() - 1);
				return String.format("[contains(@%s,%s)]", attributeName, escapedAttributeValue);
			} else if (attributeName.endsWith("^")) {
				attributeName = attributeName.substring(0, attributeName.length() - 1);
				return String.format("[starts-with(@%s,%s)]", attributeName, escapedAttributeValue);
			} else if (attributeName.endsWith("$")) {
				attributeName = attributeName.substring(0, attributeName.length() - 1);
				//return String.format("[ends-with(@%s,%s)]", attributeName, escapedAttributeValue); // would by valid with xpath 2.0
				return String.format("[substring(@%s, string-length(@%s) - string-length(%s) +1) = %s]", attributeName, attributeName, escapedAttributeValue, escapedAttributeValue);
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
			return "By.attribute: " + attributeName;
		}
	}
	
	/**
	 * Find element with the text content given
	 * @author s047432
	 *
	 */
	public static class ByText extends ByC implements Serializable {

		private static final long serialVersionUID = 5341968046120372161L;

		private final String text;
		private final String tagName;
		private final boolean partial;

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
			String escapedText = escapeQuotes(text);
			if (partial) {
				return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[contains(text(),%s)]", tagName, escapedText));
			} else {
				return ((FindsByXPath) context).findElementsByXPath(String.format(".//%s[text() = %s]", tagName, escapedText));
			}
		}

		@Override
		public WebElement findElement(SearchContext context) {
			String escapedText = escapeQuotes(text);
			if (partial) {
				return ((FindsByXPath) context).findElementByXPath(String.format(".//%s[contains(text(),%s)]", tagName, escapedText));
			} else {
				return ((FindsByXPath) context).findElementByXPath(String.format(".//%s[text() = %s]", tagName, escapedText));
			}
		}

		@Override
		public String toString() {
			return String.format("%s By.text: %s", tagName, text);
		}
	}
	
	protected String escapeQuotes(String aString) {
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
