package com.seleniumtests.uipage.htmlelements.select;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface ISelectList {
	

	void setDriver(WebDriver driver);

	boolean isApplicable();
 
    List<WebElement> getOptions();
    
    void finalizeAction();
    
    String getOptionValue(WebElement option);
    
    String getOptionText(WebElement option);

    /**
     * @return All selected options belonging to this select tag
     */
    List<WebElement> getAllSelectedOptions();

    void deselectByIndex(final Integer index);
    
    void deselectByText(final String text);
    
    void deselectByValue(final String value);

    void selectByIndex(int index);
    
    void selectByText(String text);
    
    void selectByValue(String value);

    void setSelected(WebElement option);
    
    void setDeselected(WebElement option);
    
    boolean isMultipleWithoutFind();

    void selectCorrespondingText(final String text);

    void deselectCorrespondingText(String text);
}
