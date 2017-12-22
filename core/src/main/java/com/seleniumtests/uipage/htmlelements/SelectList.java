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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import com.seleniumtests.uipage.ReplayOnError;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

/**
 * Support both standard select tag and fake select consists of tag ul and li.
 */
public class SelectList extends HtmlElement {

    protected Select select = null;
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
        } catch (UnexpectedTagNameException e) {
            if ("ul".equalsIgnoreCase(element.getTagName())) {
                options = element.findElements(By.tagName("li"));
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
    	return option.getAttribute("value");
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
        for (WebElement option : options) {
        	if (option.isSelected()) {
        		return option;
        	}
        }

        return null;
    }
    
    /**
     * @return All selected options belonging to this select tag
     */
    @ReplayOnError
    public List<WebElement> getAllSelectedOptions() {
      List<WebElement> toReturn = new ArrayList<>();

      for (WebElement option : options) {
          if (option.isSelected()) {
        	  toReturn.add(option);
          }
      }

      return toReturn;
    }

    @ReplayOnError
    public String getSelectedText() {
        findElement();
        for (WebElement option : options) {
            if (option.isSelected()) {
                return option.getText();
            }
        }
        return null;
    }

    @ReplayOnError
    public String[] getSelectedTexts() {
        findElement();

        List<String> textList = new ArrayList<>();
        for (WebElement option : options) {
            if (option.isSelected()) {
                textList.add(option.getText());
            }
        }

        String[] texts = new String[textList.size()];
        return textList.toArray(texts);
    }

    @ReplayOnError
    public String getSelectedValue() {
        findElement();
        for (WebElement option : options) {
            if (option.isSelected()) {
                return getOptionValue(option);
            }
        }
        return null;
    }

    @ReplayOnError
    public String[] getSelectedValues() {
        findElement();

        List<String> valueList = new ArrayList<>();
        for (WebElement option : options) {
            if (option.isSelected()) {
                valueList.add(getOptionValue(option));
            }
        }

        String[] values = new String[valueList.size()];
        return valueList.toArray(values);
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
        findElement();
        if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }

        for (WebElement option : options) {
        	setDeselected(option);
        }
    }

    @ReplayOnError
    public void deselectByIndex(final Integer index) {
        findElement();
        if (select != null) {
        	select.deselectByIndex(index);
        } else {
	        WebElement option = options.get(index);
	        setDeselected(option);
        }
    }

    @ReplayOnError
    public void deselectByText(final String text) {
        findElement();
        
        if (select != null) {
        	select.deselectByVisibleText(text);
        } else {
	        for (WebElement option : options) {
	            if (option.getText().equals(text)) {
	            	setDeselected(option);
	                break;
	            }
	        }
        }
    }

    @ReplayOnError
    public void deselectByValue(final String value) {
        findElement();
        
        if (select != null) {
        	select.deselectByValue(value);
        } else {
	        for (WebElement option : options) {
	            if (getOptionValue(option).equals(value)) {
	            	setDeselected(option);
	                break;
	            }
	        }
        }
    }

    @ReplayOnError
    public void selectByIndex(final Integer index) {
        findElement();

        if (select != null) {
        	select.selectByIndex(index);
        } else {
	        WebElement option = options.get(index);
	        setSelected(option);
        }
    }

    @ReplayOnError
    public void selectByIndex(final int[] indexs) {
        findElement();
        
        for (int i = 0; i < indexs.length; i++) {
            WebElement option = options.get(indexs[i]);
            setSelected(option);
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
        if (options == null) {
            driver.findElement(By.xpath("//li[text()='" + text + "']")).click();
            return;
        }
        
        if (select != null) {
        	select.selectByVisibleText(text);
        } else {
	        for (WebElement option : options) {
	            String selectedText;
	            if ("li".equalsIgnoreCase(option.getTagName()) && !option.getAttribute("title").isEmpty()) {
	                selectedText = option.getAttribute("title");
	            } else {
	                selectedText = option.getText();
	            }
	
	            if (selectedText.equals(text)) {
	                setSelected(option);
	                break;
	            }
	        }
        }
    }

    @ReplayOnError
    public void selectByText(final String[] texts) {
        findElement();
        for (int i = 0; i < texts.length; i++) {
            for (WebElement option : options) {
                if (option.getText().equals(texts[i])) {
                    setSelected(option);
                    break;
                }
            }
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
        
        if (select != null) {
        	select.selectByValue(value);
        } else {
	        for (WebElement option : options) {
	            if (getOptionValue(option).equals(value)) {
	                setSelected(option);
	                break;
	            }
	        }
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
    	if(select != null){
    		if(!select.getAllSelectedOptions().contains(option)){
    			select.selectByVisibleText(option.getText());
    		}
    	}else{
	        if (!option.isSelected()) {
	            option.click();
	        }
    	}
    }
    
    private void setDeselected(final WebElement option) {
    	if(select != null){
    		if(select.getAllSelectedOptions().contains(option)){
    			select.deselectByVisibleText(option.getText());
    		}
    	}else{
	        if (option.isSelected()) {
	            option.click();
	        }
    	}
    }
}
