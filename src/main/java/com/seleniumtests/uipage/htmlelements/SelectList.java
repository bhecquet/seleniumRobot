/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.uipage.htmlelements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.UnexpectedTagNameException;

import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;

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
    
    public SelectList(final String label, final By by, final int index) {
    	super(label, by, index);
    }

    /**
     * De-selects all options in a multi-select list element.
     */
    public void deselectAll() {
        TestLogging.logWebStep(null, "deselect all options on " + toHTML(), false);
        findElement();
        if (!isMultiple()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }

        for (WebElement option : options) {
        	setDeselected(option);
        }
    }

    public void deselectByIndex(final int index) {
        TestLogging.logWebStep(null, "deselect index\"" + index + "\" on " + toHTML(), false);
        findElement();

        WebElement option = options.get(index);
        setDeselected(option);
    }

    public void deselectByText(final String text) {
        TestLogging.logWebStep(null, "deselect text\"" + text + "\" on " + toHTML(), false);
        findElement();
        for (WebElement option : options) {
            if (option.getText().equals(text)) {
            	setDeselected(option);
                break;
            }
        }
    }

    public void deselectByValue(final String value) {
        TestLogging.logWebStep(null, "deselect value\"" + value + "\" on " + toHTML(), false);
        findElement();
        for (WebElement option : options) {
            if (option.getAttribute("value").equals(value)) {
            	setDeselected(option);
                break;
            }
        }

    }

    @Override
    protected void findElement() {
        driver = WebUIDriver.getWebDriver();
        element = driver.findElement(this.getBy());
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

    public List<WebElement> getOptions() {
        findElement();
        return options;
    }

    public String getSelectedText() {
        findElement();
        for (WebElement option : options) {
            if (option.isSelected()) {
                return option.getText();
            }
        }
        return null;
    }

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

    public String getSelectedValue() {
        findElement();
        for (WebElement option : options) {
            if (option.isSelected()) {
                return option.getAttribute("value");
            }
        }
        return null;
    }

    public String[] getSelectedValues() {
        findElement();

        List<String> valueList = new ArrayList<>();
        for (WebElement option : options) {
            if (option.isSelected()) {
                valueList.add(option.getAttribute("value"));
            }
        }

        String[] values = new String[valueList.size()];
        return valueList.toArray(values);
    }

    @Override
    public void init() {
        super.init();
        try {
            select = getNewSelectElement(element);
            options = select.getOptions();
        } catch (UnexpectedTagNameException e) {
            if ("ul".equalsIgnoreCase(element.getTagName())) {
                options = element.findElements(By.tagName("li"));
            }
        }
    }

    public boolean isMultiple() {
        findElement();

        String value = element.getAttribute("multiple");
        return value != null && !"false".equals(value);
    }

    public void selectByIndex(final int index) {
        TestLogging.logWebStep(null, "make selection using index\"" + index + "\" on " + toHTML(), false);
        findElement();

        WebElement option = options.get(index);
        setSelected(option);
    }

    public void selectByIndex(final int[] indexs) {
        TestLogging.logWebStep(null, "make selection using indexs\"" + Arrays.toString(indexs) + "\" on " + toHTML(), false);
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
    public void selectByText(final String text) {
        TestLogging.logWebStep(null, "make selection using text\"" + text + "\" on " + toHTML(), false);
        findElement();
        if (options == null) {
            driver.findElement(By.xpath("//li[text()='" + text + "']")).click();
            return;
        }

        for (WebElement option : options) {
            String selectedText;
            if ("li".equalsIgnoreCase(option.getTagName())) {
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

    public void selectByText(final String[] texts) {
        TestLogging.logWebStep(null, "make selection using texts\"" + texts + "\" on " + toHTML(), false);
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
    public void selectByCorrespondingText(String text) {
    	TestLogging.logWebStep(null, "make corresponding selection using texts\"" + text + "\" on " + toHTML(), false);
        findElement();
    	 double score = 0;
    	 WebElement optionToSelect = null;
    	 for (WebElement option : options) {
    		String source = option.getText();
    		if(service.score(source, text)>score){
    			score = service.score(source, text);
    			optionToSelect = option;
    		}
    	 }
    	 setSelected(optionToSelect);
    }
    
    /**
     * Multiple select by attribute text, similar select
     * 
     * @param text
     */
    public void selectByCorrespondingText(String[] text) {
    	TestLogging.logWebStep(null, "make corresponding selection using texts\"" + text + "\" on " + toHTML(), false);
    	 findElement();
    	 for (int i = 0; i < text.length; i++) {
    		 double score = 0;
        	 WebElement optionToSelect = null;
        	 for (WebElement option : options) {
        		String source = option.getText();
        		if(service.score(source, text[i])>score){
        			score = service.score(source, text[i]);
        			optionToSelect = option;
        		}
        	 }
        	 setSelected(optionToSelect);
    	 }
    }

    public void selectByValue(final String value) {
        TestLogging.logWebStep(null, "make selection using value\"" + value + "\" on " + toHTML(), false);
        findElement();
        for (WebElement option : options) {
            if (option.getAttribute("value").equals(value)) {
                setSelected(option);
                break;
            }
        }
    }

    public void selectByValue(final String[] values) {
        TestLogging.logWebStep(null, "make selection using values\"" + values + "\" on " + toHTML(), false);
        findElement();
        for (int i = 0; i < values.length; i++) {
            for (WebElement option : options) {
                if (option.getAttribute("value").equals(values[i])) {
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
