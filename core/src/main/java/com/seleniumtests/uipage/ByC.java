package com.seleniumtests.uipage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByXPath;

public class ByC extends By {

	@Override
	public List<WebElement> findElements(SearchContext context) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static ByC labelForward(final String label) {
		return labelForward(label, "input");
	}

	public static ByC labelForward(final String label, String tagName) {
		if (label == null) {
			throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
		}
		if (tagName == null) {
			tagName = "input";
		}
	
		return new ByLabelForward(label, tagName);
	}
	
	public static ByC partialLabelForward(final String label) {
		return labelForward(label, "input");
	}
	
	public static ByC partialLabelForward(final String label, String tagName) {
		if (label == null) {
			throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
		}
		if (tagName == null) {
			tagName = "input";
		}
		
		return new ByLabelForward(label, tagName);
	}
	
	public static ByC labelBackward(final String label) {
		return labelBackward(label, "input");
	}
	
	public static ByC labelBackward(final String label, String tagName) {
		if (label == null) {
			throw new IllegalArgumentException("Cannot find elements with a null label attribute.");
		}
		if (tagName == null) {
			tagName = "input";
		}
		
		return new ByLabelBackward(label, tagName);
	}

	public static class ByLabelForward extends ByC implements Serializable {

		private static final long serialVersionUID = 5341968046120372161L;

		private final String label;
		private final String tagName;
		private final boolean partial;

		public ByLabelForward(String label, String tagName, boolean partial) {
			this.label = label;
			this.tagName = tagName;
			this.partial = partial;
		}
		
		public ByLabelForward(String label, boolean partial) {
			this(label, "input", partial);
		}
		
		public ByLabelForward(String label, String tagName) {
			this(label, tagName, false);
		}
		
		public ByLabelForward(String label) {
			this(label, "input", false);
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
			this.label = label;
			this.tagName = tagName;
			this.partial = partial;
		}
		
		public ByLabelBackward(String label, boolean partial) {
			this(label, "input", partial);
		}
		
		public ByLabelBackward(String label, String tagName) {
			this(label, tagName, false);
		}
		
		public ByLabelBackward(String label) {
			this(label, "input", false);
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
