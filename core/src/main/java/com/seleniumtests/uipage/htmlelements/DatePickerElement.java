package com.seleniumtests.uipage.htmlelements;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.ReplayOnError;

/**
 * Class representing a date picker object
 * This is first created for angular materials date pickers
 * @author s047432
 *
 */
public class DatePickerElement extends HtmlElement {

	public DatePickerElement(final String label, final By by) {
        this(label, by, null, null, null);
    }
    
    public DatePickerElement(final String label, final By by, final HtmlElement parent) {
    	this(label, by, null, parent, null);
    }
    
    public DatePickerElement(final String label, final By by, final Integer index) {
    	this(label, by, null, null, index);
    }
    
    public DatePickerElement(final String label, final By by, final HtmlElement parent, final Integer index) {
    	this(label, by, null, parent, index);
    }
    
    public DatePickerElement(final String label, final By by, final FrameElement frame) {
    	this(label, by, frame, null, null);
    }
    
    public DatePickerElement(final String label, final By by, final FrameElement frame, final Integer index) {
    	this(label, by, frame, null, index);
    }
    
    public DatePickerElement(final String label, final By by, final FrameElement frame, final HtmlElement parent, final Integer index) {
    	super(label, by, frame, parent, index);
    }

    
    @Override
    @ReplayOnError
    public void clear() {
    	findElement(true);

    	clearField();
    }
    
    /**
     * Clear text field but assumes element exists
     */
    private void clearField() {
    	
    	if (element == null) {
    		throw new ScenarioException("Element should not be null");
    	}
    	
    	BrowserType browser = WebUIDriver.getWebUIDriver(false).getConfig().getBrowser();
    	if (browser == BrowserType.INTERNET_EXPLORER) {
	    	new Actions(driver)
	    		.doubleClick(element)
		    	.sendKeys(Keys.DELETE, Keys.DELETE, Keys.DELETE, Keys.DELETE, Keys.DELETE, Keys.DELETE, Keys.DELETE, Keys.DELETE, Keys.DELETE, Keys.DELETE)
		    	.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE, Keys.BACK_SPACE)
		    	.perform();
    	} else {
    		super.clear();
    	}
    }
    
    @Override
    @ReplayOnError
    public void sendKeys(final boolean clear, final boolean blurAfter, CharSequence... keysToSend) {
        findElement(true);
        
        if (clear) {
        	clearField();
        } 
        element.click();
        element.sendKeys(keysToSend);
        
        if (blurAfter) {
        	blur(); 
        }
    }
}
