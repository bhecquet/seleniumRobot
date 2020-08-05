package com.seleniumtests.uipage;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.seleniumtests.uipage.htmlelements.HtmlElement;

public class ExpectedConditionsC {

	private ExpectedConditionsC() {
	}

	/**
	   * An expectation for checking that an element is present on the DOM of a page. This does not
	   * necessarily mean that the element is visible.
	   *
	   * @param element		the HtmlElement to locate
	   * @return the WebElement once it is located
	   */
	  public static ExpectedCondition<WebElement> presenceOfElementLocated(final HtmlElement element) {
	    return new ExpectedCondition<WebElement>() {
	      @Override
	      public WebElement apply(WebDriver driver) {
	    	  element.findElement(false,  false);
	    	  return element.getRealElement();
	      }

	      @Override
	      public String toString() {
	        return "presence of element: " + element;
	      }
	    };
	  }
	  
	  public static ExpectedCondition<Boolean> absenceOfElementLocated(final HtmlElement element) {
		  return new ExpectedCondition<Boolean>() {
			  @Override
			  public Boolean apply(WebDriver driver) {
				  try {
					  element.findElement(false,  false);
					  element.getRealElement();
					  
					  // element is there, this is not what we want
					  return null;
				  } catch (Exception e) {
					  return true;
				  }
			  }
			  
			  @Override
			  public String toString() {
				  return "absence of element: " + element;
			  }
		  };
	  }
	
}
