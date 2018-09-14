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
package com.seleniumtests.uipage.htmlelements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ReplayOnError;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

/**
 * Support both standard select tag and fake select consists of tag ul and li.
 */
public class SelectList extends HtmlElement {
	
	private enum Type {HTML, // the classic <select> tag
		LIST, 				// <ul><li></li></ul> select boxes with style
		ANGULAR_MATERIAL	// <mat-select>
		}

    protected Select select = null;
    private Type selectType = Type.HTML; 
    protected List<WebElement> options = null;
	private SimilarityStrategy strategy = new JaroWinklerStrategy();
	private StringSimilarityService service = new StringSimilarityServiceImpl(strategy);

	/**
	 * Creates a SelectList element which represents either
	 * - a standard HTML element => locator should point to the "select" element itself
	 * - a select list built with HTML lists (ul / li) => locator should point to <ul> element
	 * - an angular materials select => locator should point to the <mat-select> element
	 * @param text
	 * @param by
	 */
    public SelectList(final String text, final By by) {
        super(text, by);
    }
    
    public SelectList(final String label, final By by, final HtmlElement parent) {
    	super(label, by, parent);
    }

    public SelectList(final String label, final By by, final HtmlElement parent, final Integer index) {
    	super(label, by, parent, index);
    }
     
    public SelectList(final String label, final By by, final Integer index) {
    	super(label, by, index);
    }
    
    public SelectList(final String label, final By by, final FrameElement frame) {
    	super(label, by, frame);
    }
    
    public SelectList(final String label, final By by, final FrameElement frame, final Integer index) {
    	super(label, by, frame, index);
    }

    @Override
    protected void findElement() {
        super.findElement(true);
        
        try {
            select = getNewSelectElement(element);
            options = select.getOptions();
            selectType = Type.HTML;
        } catch (UnexpectedTagNameException e) {
            if ("ul".equalsIgnoreCase(element.getTagName())) {
                options = element.findElements(By.tagName("li"));
                selectType = Type.LIST;
            } else if ("mat-select".equalsIgnoreCase(element.getTagName())) {
            	
            	// click on arrow to display options
            	element.findElement(By.className("mat-select-arrow")).click();
            	options = new HtmlElement("options", By.className("mat-select-content"), frameElement)
            				.findHtmlElements(By.tagName("mat-option"))
            				.stream()
            				.map(CachedHtmlElement::new)
            				.collect(Collectors.toList());
            	selectType = Type.ANGULAR_MATERIAL;
            }
        }
    }
    
    private void finalizeAction() {
    	if (selectType == Type.ANGULAR_MATERIAL) {
    		HtmlElement selectContent = new HtmlElement("options", By.className("mat-select-content"), frameElement);
    		if (selectContent.isElementPresent()) {
    			element.sendKeys(Keys.ESCAPE);
    		}
    	}
    }

    /**
     * Returns a new Select element (created to facilitate unit testing).
     *
     * @return
     */
    protected Select getNewSelectElement(final WebElement element) {
        return new Select(element);
    }
    
    private String getOptionValue(WebElement option) {
    	switch(selectType) {
    		case ANGULAR_MATERIAL:
    		case HTML:
    			return option.getAttribute("value");
    		case LIST:
    			return option.getAttribute("id");
    		default:
    			throw new CustomSeleniumTestsException(selectType + "not recognized ");
    	}
    }

    @ReplayOnError
    public List<WebElement> getOptions() {
    	try {
	        findElement();
	        return options;
    	} finally {
    		finalizeAction();
    	}
    }
    
    /**
     * @return The first selected option in this select tag (or the currently selected option in a
     *         normal select)
     * @throws NoSuchElementException If no option is selected
     */
    @ReplayOnError
    public WebElement getFirstSelectedOption() {
    	List<WebElement> allSelectedOptions = getAllSelectedOptions();
    	try {
    		return allSelectedOptions.get(0);
    	} catch (IndexOutOfBoundsException e) {
			return null;
		}
    }
    
    /**
     * @return All selected options belonging to this select tag
     */
    @ReplayOnError
    public List<WebElement> getAllSelectedOptions() {
    	List<WebElement> toReturn = new ArrayList<>();
      
    	try {
	    	findElement();
			switch (selectType) {
			case ANGULAR_MATERIAL:
				for (WebElement option : options) {
					if (option.getAttribute("class").contains("mat-selected")) {
						toReturn.add(option);
					}
				}
			case HTML:
				for (WebElement option : options) {
					if (option.isSelected()) {
						toReturn.add(option);
					}
				}
			case LIST:
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
			}
			
			return toReturn;
    	} finally {
    		finalizeAction();
    	}
	}

    @ReplayOnError
    public String getSelectedText() {
    	WebElement firstSelectedOption = getFirstSelectedOption();
    	if (firstSelectedOption != null) {
    		return firstSelectedOption.getText();
    	} else {
    		return "";
    	}
    }

    @ReplayOnError
    public String[] getSelectedTexts() {
    	List<WebElement> allSelectedOptions = getAllSelectedOptions();
    	List<String> textList = new ArrayList<>();
    	
        for (WebElement option : allSelectedOptions) {
        	textList.add(option.getText());
        }

        String[] texts = new String[textList.size()];
        return textList.toArray(texts);
    }

    @ReplayOnError
    public String getSelectedValue() {
    	WebElement firstSelectedOption = getFirstSelectedOption();
    	if (firstSelectedOption != null) {
    		return getOptionValue(firstSelectedOption);
    	} else {
    		return "";
    	}
    }

    @ReplayOnError
    public String[] getSelectedValues() {
    	List<WebElement> allSelectedOptions = getAllSelectedOptions();
    	List<String> valueList = new ArrayList<>();
    	
        for (WebElement option : allSelectedOptions) {
        	valueList.add(getOptionValue(option));
        }

        String[] texts = new String[valueList.size()];
        return valueList.toArray(texts);
    }

    @ReplayOnError
    public boolean isMultiple() {
    	try {
	        findElement();
	
	        String value = element.getAttribute("multiple");
	        return value != null && !"false".equals(value);
    	} finally {
    		finalizeAction();
    	}
    }
    
    /**
     * De-selects all options in a multi-select list element.
     */
    @ReplayOnError
    public void deselectAll() {
    	if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }
    	
    	List<WebElement> allSelectedOptions = getAllSelectedOptions();
        for (WebElement option : allSelectedOptions) {
        	setDeselected(option);
        }
    }

    @ReplayOnError
    public void deselectByIndex(final Integer index) {
    	if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect options of a multi-select");
        }
    	
    	try {
	    	findElement();
			switch (selectType) {
				case HTML:
					select.deselectByIndex(index);
					break;
				case ANGULAR_MATERIAL:
				case LIST:
					try {
						WebElement option = options.get(index);
				        setDeselected(option);
					} catch (IndexOutOfBoundsException e) {
						throw new NoSuchElementException("Cannot locate element with index: " + index);
					}
					break;
				default:
					throw new CustomSeleniumTestsException(selectType + "not recognized ");
			}
    	} finally {
    		finalizeAction();
    	}
    }

    @ReplayOnError
    public void deselectByText(final String text) {
    	if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }
    	
    	try {
	    	findElement();
	    	boolean matched = false;
			switch (selectType) {
				case HTML:
					select.deselectByVisibleText(text);
					break;
				case ANGULAR_MATERIAL:
				case LIST:
					for (WebElement option : options) {
			            if (option.getText().equals(text)) {
			            	setDeselected(option);
			            	matched = true;
			                break;
			            }
			        }
					if (!matched) {
				      throw new NoSuchElementException("Cannot locate element with text: " + text);
				    }
					break;
				default:
					throw new CustomSeleniumTestsException(selectType + "not recognized ");
			}
    	} finally {
    		finalizeAction();
    	}
    }

    @ReplayOnError
    public void deselectByValue(final String value) {
    	if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }
    	
    	try {
	    	findElement();
	    	boolean matched = false;
			switch (selectType) {
	
				case HTML:
					select.deselectByValue(value);
					break;
				case ANGULAR_MATERIAL:
				case LIST:
					for (WebElement option : options) {
			            if (getOptionValue(option).equals(value)) {
			            	setDeselected(option);
			            	matched = true;
			                break;
			            }
			        }
					if (!matched) {
				      throw new NoSuchElementException("Cannot locate element with value: " + value);
				    }
					break;
				default:
					throw new CustomSeleniumTestsException(selectType + "not recognized ");
			}
    	} finally {
    		finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByIndex(final Integer index) {
    	try {
	        findElement();
	        
			selectIndex(index);
    	} finally {
    		finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByIndex(final int[] indexs) {
        try {
	    	findElement();
	    	
	    	for (int i = 0; i < indexs.length; i++) {
	    		selectIndex(indexs[i]);
	    	}
    	} finally {
    		finalizeAction();
    	}
    }
    
    private void selectIndex(int index) {
    	switch (selectType) {
			case HTML:
				select.selectByIndex(index);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				try {
					WebElement option = options.get(index);
					setSelected(option);
				} catch (IndexOutOfBoundsException e) {
					throw new NoSuchElementException("Cannot locate option with index: " + index);
				}
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    /**
     * Select standard select by attribute text, and select fake select with ul and li by attribute title.
     *
     * @param  text
     */
    @ReplayOnError
    public void selectByText(final String text) {
    	try {
	    	findElement();
	        
			selectText(text);
    	} finally {
    		finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByText(final String[] texts) {
    	try {
	        findElement();
	        
	        for (int i = 0; i < texts.length; i++) {
	        	selectText(texts[i]);
	        }
	       
    	} finally {
    		finalizeAction();
    	}
    }
    
    private void selectText(final String text) {
    	switch (selectType) {
			case HTML:
				select.selectByVisibleText(text);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				boolean matched = false;
				for (WebElement option : options) {
		            String selectedText;
		            if (!option.getAttribute("title").isEmpty()) {
		                selectedText = option.getAttribute("title");
		            } else {
		                selectedText = option.getText();
		            }
		
		            if (selectedText.equals(text)) {
		                setSelected(option);
		                matched = true;
		                break;
		            }
		        }
				
				if (!matched) {
			      throw new NoSuchElementException("Cannot locate element with text: " + text);
			    }
				
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
    	}
    }
    
    /**
     * Select Corresponding select by attribute text, select the most similar text
     *
     * @param  text
     */
    @ReplayOnError
    public void selectByCorrespondingText(String text) {
    	try {
	    	findElement();
	    	selectCorrespondingText(text);
    	} finally {
    		finalizeAction();
    	}
    }
    
    /**
     * Multiple select by attribute text, similar select
     * 
     * @param text
     */
    @ReplayOnError
    public void selectByCorrespondingText(String[] text) {
    	try {
	    	findElement();
	    	for (int i = 0; i < text.length; i++) {
	    		selectCorrespondingText(text[i]);
	    	}
    	} finally {
    		finalizeAction();
    	}
    }
    
    private void selectCorrespondingText(final String text) {
    	double score = 0;
    	WebElement optionToSelect = null;
    	for (WebElement option : options) {
    		String source = option.getText();
    		if (service.score(source, text) > score) {
    			score = service.score(source, text);
    			optionToSelect = option;
    		}
    	}
    	if (optionToSelect != null) {
    		setSelected(optionToSelect);
    	} else {
    		throw new NoSuchElementException("Cannot locate option with corresponding text " + text);
    	}
    }
    
    @ReplayOnError
    public void deselectByCorrespondingText(final String text) {
    	if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }
    	
    	try {
    		findElement();
	    	double score = 0;
	    	WebElement optionToSelect = null;
	    	for (WebElement option : options) {
	    		String source = option.getText();
	    		if (service.score(source, text) > score) {
	    			score = service.score(source, text);
	    			optionToSelect = option;
	    		}
	    	}
	    	if (optionToSelect != null) {
	    		setDeselected(optionToSelect);
	    	} else {
	    		throw new NoSuchElementException("Cannot locate option with corresponding text " + text);
	    	}
    		
    	} finally {
    		finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByValue(final String value) {
    	try {
	    	findElement();
	        
			selectValue(value);
    	} finally {
    		finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByValue(final String[] values) {
    	try {
	        findElement();
	        for (int i = 0; i < values.length; i++) {
	            selectValue(values[i]);
	        }
    	} finally {
    		finalizeAction();
    	}
    }
    
    private void selectValue(final String value) {
    	switch (selectType) {
			case HTML:
				select.selectByValue(value);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				boolean matched = false;
				for (WebElement option : options) {
		            if (getOptionValue(option).equals(value)) {
		                setSelected(option);
		                matched =true;
		                break;
		            }
		        }
				if (!matched) {
					throw new NoSuchElementException("Cannot locate option with value: " + value);
			    }
				
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    private void setSelected(final WebElement option) {
    	
    	switch (selectType) {
			case ANGULAR_MATERIAL:
				if ("false".equals(((HtmlElement)((CachedHtmlElement)option).getRealElement()).getAttribute("aria-selected"))) {
					// here list should still be visible
					HtmlElement checkbox = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).findElement(By.tagName("mat-pseudo-checkbox"));
					if (checkbox.isElementPresent(0)) {
						checkbox.click();
					} else {
						((CachedHtmlElement)option).getRealElement().click();
					}
				}
				break;
			case HTML:
				select.selectByVisibleText(option.getText());
				break;
			case LIST:
				option.click();
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }
    
    private void setDeselected(final WebElement option) {
    	switch (selectType) {
			case ANGULAR_MATERIAL:
				if ("true".equals(option.getAttribute("aria-selected"))) {
					HtmlElement checkbox = ((HtmlElement)((CachedHtmlElement)option).getRealElement()).findElement(By.tagName("mat-pseudo-checkbox"));
					if (checkbox.isElementPresent(0)) {
						checkbox.click();
					} else {
						((CachedHtmlElement)option).getRealElement().click();
					}
				}
				break;
			case HTML:
				select.deselectByVisibleText(option.getText());
				break;
			case LIST:
				throw new ScenarioException("Cannot deselect for list based select");
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }
}
