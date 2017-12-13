package com.seleniumtests.uipage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByCssSelector;
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
		return labelForward(label, "input", false);
	}
	
	/**
	 * Search first element for <code>tagName</code> after label referenced by name
	 * @param label
	 * @param tagName
	 * @return
	 */
	public static ByC labelForward(final String label, final String tagName) {
		return labelForward(label, tagName, false);
	}

	/**
	 * Search first 'input' element after label referenced by partial name
	 * @param label
	 * @return
	 */
	public static ByC partialLabelForward(final String label) {
		return labelForward(label, "input", true);
	}
	
	/**
	 * Search first element for <code>tagName</code> after label referenced by partial name
	 * @param label
	 * @param tagName
	 * @return
	 */
	public static ByC partialLabelForward(final String label, final String tagName) {
		return labelForward(label, tagName, true);
	}

	private static ByC labelForward(final String label, String tagName, boolean partial) {
		return new ByLabelForward(label, tagName, partial);
	}
	
	/**
	 * Search first 'input' element before label referenced by name
	 * @param label
	 * @return
	 */
	public static ByC labelBackward(final String label) {
		return labelBackward(label, "input", false);
	}
	
	/**
	 * Search first element for <code>tagName</code> before label referenced by name
	 * @param label
	 * @param tagName
	 * @return
	 */
	public static ByC labelBackward(final String label, final String tagName) {
		return labelBackward(label, tagName, false);
	}

	/**
	 * Search first 'input' element before label referenced by partial name
	 * @param label
	 * @return
	 */
	public static ByC partialLabelBackward(final String label) {
		return labelBackward(label, "input", true);
	}
	
	/**
	 * Search first element for <code>tagName</code> before label referenced by partial name
	 * @param label
	 * @param tagName
	 * @return
	 */
	public static ByC partialLabelBackward(final String label, final String tagName) {
		return labelBackward(label, tagName, true);
	}
	
	private static ByC labelBackward(final String label, String tagName, boolean partial) {
		return new ByLabelBackward(label, tagName, partial);
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

	public static class ByLabelForward extends ByC implements Serializable {

		private static final long serialVersionUID = 5341968046120372161L;

		private final String label;
		private final String tagName;
		private final boolean partial;

		public ByLabelForward(String label, String tagName, boolean partial) {
			
			if (label == null) {
				throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
			}
			if (tagName == null) {
				tagName = "input";
			}
			
			this.label = label;
			this.tagName = tagName;
			this.partial = partial;
		}

		@Override
		public List<WebElement> findElements(SearchContext context) {
			String escapedLabel = escapeQuotes(label);
			if (partial) {
				return ((FindsByXPath) context).findElementsByXPath(String.format("//label[contains(text(),%s)]/following::%s", escapedLabel, tagName));
			} else {
				return ((FindsByXPath) context).findElementsByXPath(String.format("//label[text() = %s]/following::%s", escapedLabel, tagName));
			}
		}

		@Override
		public WebElement findElement(SearchContext context) {
			String escapedLabel = escapeQuotes(label);
			if (partial) {
				return ((FindsByXPath) context).findElementByXPath(String.format("//label[contains(text(),%s)]/following::%s", escapedLabel, tagName));
			} else {
				return ((FindsByXPath) context).findElementByXPath(String.format("//label[text() = %s]/following::%s", escapedLabel, tagName));
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

		public ByLabelBackward(String label, String tagName, boolean partial) {
			
			if (label == null) {
				throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
			}
			if (tagName == null) {
				tagName = "input";
			}
			
			this.label = label;
			this.tagName = tagName;
			this.partial = partial;
		}
		
		@Override
		public List<WebElement> findElements(SearchContext context) {
			String escapedLabel = escapeQuotes(label);
			if (partial) {
				return ((FindsByXPath) context).findElementsByXPath(String.format("//label[contains(text(),%s)]/preceding::%s", escapedLabel, tagName));
			} else {
				return ((FindsByXPath) context).findElementsByXPath(String.format("//label[text() = %s]/preceding::%s", escapedLabel, tagName));
			}
		}
		
		@Override
		public WebElement findElement(SearchContext context) {
			String escapedLabel = escapeQuotes(label);
			List<WebElement> elements;
			if (partial) {
				elements = ((FindsByXPath) context).findElementsByXPath(String.format("//label[contains(text(),%s)]/preceding::%s", escapedLabel, tagName));
			} else {
				elements = ((FindsByXPath) context).findElementsByXPath(String.format("//label[text() = %s]/preceding::%s", escapedLabel, tagName));
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

		private final String attributeName;
		private final String attributeValue;

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
		public List<WebElement> findElements(SearchContext context) {
			String escapedAttributeValue = escapeQuotes(attributeValue);
			return ((FindsByCssSelector) context).findElementsByCssSelector(String.format("[%s=%s]", attributeName, escapedAttributeValue));
		}

		@Override
		public WebElement findElement(SearchContext context) {
			String escapedAttributeValue = escapeQuotes(attributeValue);
			return ((FindsByCssSelector) context).findElementByCssSelector(String.format("[%s=%s]", attributeName, escapedAttributeValue));
		}

		@Override
		public String toString() {
			return "By.attribute: " + attributeName;
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
