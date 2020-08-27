package com.seleniumtests.uipage.htmlelements.select;

import java.util.List;

import org.openqa.selenium.WebElement;

public interface ISelectList {

	public boolean isApplicable();
 
    public List<WebElement> getOptions();
    
    public void finalizeAction();
    
    public String getOptionValue(WebElement option);
    
    public String getOptionText(WebElement option);

    /**
     * @return All selected options belonging to this select tag
     */
    public List<WebElement> getAllSelectedOptions();

    public void deselectByIndex(final Integer index);
    
    public void deselectByText(final String text);
    
    public void deselectByValue(final String value);

    public void selectByIndex(int index);
    
    public void selectByText(String text);
    
    public void selectByValue(String value);

    public void setSelected(WebElement option);
    
    public void setDeselected(WebElement option);
    
    public boolean isMultipleWithoutFind();
}
