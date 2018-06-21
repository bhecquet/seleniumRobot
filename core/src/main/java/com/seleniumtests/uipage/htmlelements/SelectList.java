/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
            	options = driver.findElement(By.tagName("mat-select-content")).findElements(By.tagName("mat-option"));
            	selectType = Type.ANGULAR_MATERIAL;
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
        findElement();
        return options;
    }
    
    /**
     * @return The first selected option in this select tag (or the currently selected option in a
     *         normal select)
     * @throws NoSuchElementException If no option is selected
     */
    @ReplayOnError
    public WebElement getFirstSelectedOption() {
    	findElement();
    	switch(selectType) {
			case ANGULAR_MATERIAL:
				for (WebElement option : options) {
					if (option.getAttribute("class").contains("mat-selected")) {
						return option;
					}
				}
				return null;
			case HTML:
				for (WebElement option : options) {
					if (option.isSelected()) {
						return option;
					}
				}
				return null;
			case LIST:
				return null;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }
    
    /**
     * @return All selected options belonging to this select tag
     */
    @ReplayOnError
    public List<WebElement> getAllSelectedOptions() {
    	List<WebElement> toReturn = new ArrayList<>();
      
    	findElement();
		switch (selectType) {
		case ANGULAR_MATERIAL:
			for (WebElement option : options) {
				if (option.getAttribute("class").contains("mat-selected")) {
					toReturn.add(option);
				}
			}
			return null;
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
        findElement();

        String value = element.getAttribute("multiple");
        return value != null && !"false".equals(value);
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
    	
    	findElement();
		switch (selectType) {
			case HTML:
				select.deselectByIndex(index);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				WebElement option = options.get(index);
		        setDeselected(option);
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    @ReplayOnError
    public void deselectByText(final String text) {
    	if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }
    	
    	findElement();
		switch (selectType) {
			case HTML:
				select.deselectByVisibleText(text);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				for (WebElement option : options) {
		            if (option.getText().equals(text)) {
		            	setDeselected(option);
		                break;
		            }
		        }
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    @ReplayOnError
    public void deselectByValue(final String value) {
    	if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }
    	
    	findElement();
		switch (selectType) {

			case HTML:
				select.deselectByValue(value);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				for (WebElement option : options) {
		            if (getOptionValue(option).equals(value)) {
		            	setDeselected(option);
		                break;
		            }
		        }
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    @ReplayOnError
    public void selectByIndex(final Integer index) {
        findElement();
        
		switch (selectType) {
			case HTML:
				select.selectByIndex(index);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				WebElement option = options.get(index);
				setSelected(option);
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    @ReplayOnError
    public void selectByIndex(final int[] indexs) {
        findElement();
        
        switch (selectType) {
			case HTML:
				for (int j = 0; j < indexs.length; j++) {
					select.selectByIndex(indexs[j]);
				}
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				for (int k = 0; k < indexs.length; k++) {
		            WebElement option = options.get(indexs[k]);
		            setSelected(option);
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
    	findElement();
        
		switch (selectType) {
			case HTML:
				select.selectByVisibleText(text);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				for (WebElement option : options) {
		            String selectedText;
		            if (!option.getAttribute("title").isEmpty()) {
		                selectedText = option.getAttribute("title");
		            } else {
		                selectedText = option.getText();
		            }
		
		            if (selectedText.equals(text)) {
		                setSelected(option);
		                break;
		            }
		        }
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    @ReplayOnError
    public void selectByText(final String[] texts) {
        findElement();
        
        switch (selectType) {

			case HTML:
				for (int i = 0; i < texts.length; i++) {
					select.selectByVisibleText(texts[i]);
				}
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				for (int i = 0; i < texts.length; i++) {
		            for (WebElement option : options) {
		                if (option.getText().equals(texts[i])) {
		                    setSelected(option);
		                    break;
		                }
		            }
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
    		setSelected(optionToSelect);
    	} else {
    		logger.error("No option matches " + text);
    	}
    }
    
    /**
     * Multiple select by attribute text, similar select
     * 
     * @param text
     */
    @ReplayOnError
    public void selectByCorrespondingText(String[] text) {
    	findElement();
    	for (int i = 0; i < text.length; i++) {
    		double score = 0;
        	WebElement optionToSelect = null;
        	for (WebElement option : options) {
        		String source = option.getText();
        		if (service.score(source, text[i]) > score) {
        			score = service.score(source, text[i]);
        			optionToSelect = option;
        		}
        	}
        	if (optionToSelect != null) {
        		setSelected(optionToSelect);
        	} else {
        		logger.error("No option matches " + text);
        	}
    	}
    }

    @ReplayOnError
    public void selectByValue(final String value) {
    	findElement();
        
		switch (selectType) {
			case HTML:
				select.selectByValue(value);
				break;
			case ANGULAR_MATERIAL:
			case LIST:
				for (WebElement option : options) {
		            if (getOptionValue(option).equals(value)) {
		                setSelected(option);
		                break;
		            }
		        }
				break;
			default:
				throw new CustomSeleniumTestsException(selectType + "not recognized ");
		}
    }

    @ReplayOnError
    public void selectByValue(final String[] values) {
        findElement();
        for (int i = 0; i < values.length; i++) {
            for (WebElement option : options) {
                if (getOptionValue(option).equals(values[i])) {
                    setSelected(option);
                    break;
                }
            }
        }
    }

    private void setSelected(final WebElement option) {
    	
    	switch (selectType) {
			case ANGULAR_MATERIAL:
				if ("false".equals(option.getAttribute("aria-selected"))) {
					option.findElement(By.tagName("mat-pseudo-checkbox")).click();
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
					option.findElement(By.tagName("mat-pseudo-checkbox")).click();
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
