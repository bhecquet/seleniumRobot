/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.seleniumtests.core.runner.cucumber.Fixture;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.uipage.htmlelements.select.AngularMaterialSelect;
import com.seleniumtests.uipage.htmlelements.select.AngularSelect;
import com.seleniumtests.uipage.htmlelements.select.ISelectList;
import com.seleniumtests.uipage.htmlelements.select.ListSelect;
import com.seleniumtests.uipage.htmlelements.select.NativeSelect;
import com.seleniumtests.uipage.htmlelements.select.SalesforceLigntningSelect;
import com.seleniumtests.uipage.htmlelements.select.StubSelect;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

/**
 * Support both standard select tag and fake select consists of tag ul and li.
 */
public class SelectList extends HtmlElement {

    protected List<WebElement> options = null;
	private SimilarityStrategy strategy = new JaroWinklerStrategy();
	private StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
	private ISelectList selectImplementation;
	private static Object lock = new Object();
	private static List<Class<? extends ISelectList>> selectImplementations;
	protected boolean multiple;

	/**
	 * Creates a SelectList element which represents either
	 * - a standard HTML element => locator should point to the "select" element itself
	 * - a select list built with HTML lists (ul / li) => locator should point to <ul> element
	 * - an angular materials select => locator should point to the <mat-select> element
	 * @param text
	 * @param by
	 */
	public SelectList(final String label, final By by) {
        super(label, by);
    }

    public SelectList(final String label, final By by, final Integer index) {
    	super(label, by, index);
    }
    
    public SelectList(final String label, final By by, final Integer index, Integer replayTimeout) {
    	super(label, by, index, replayTimeout);
    }
    
    public SelectList(final String label, final By by, final HtmlElement parent) {
    	super(label, by, parent);
    }
    
    public SelectList(final String label, final By by, final HtmlElement parent, final Integer index) {
    	super(label, by, parent, index);
    }
    
    public SelectList(final String label, final By by, final HtmlElement parent, final Integer index, Integer replayTimeout) {
    	super(label, by, parent, index, replayTimeout);
    }
     
    public SelectList(final String label, final By by, final FrameElement frame) {
    	super(label, by, frame);
    }
    
    public SelectList(final String label, final By by, final FrameElement frame, final Integer index) {
    	super(label, by, frame, index);
    }
    
    public SelectList(final String label, final By by, final FrameElement frame, final Integer index, Integer replayTimeout) {
    	super(label, by, frame, index, replayTimeout);
    }
    
    private static List<Class<? extends ISelectList>> searchRelevantImplementation() {

    	List<Class<? extends ISelectList>> selectImplementations = new ArrayList<>();
		
		// load via SPI other Select implementations
		ServiceLoader<ISelectList> selectLoader = ServiceLoader.load(ISelectList.class);
		Iterator<ISelectList> selectsIterator = selectLoader.iterator();
		while (selectsIterator.hasNext())
		{
			ISelectList selectClass = selectsIterator.next();
			selectImplementations.add(selectClass.getClass());
		}
		
		if (selectImplementations.isEmpty()) {
			throw new CustomSeleniumTestsException("Could not get list of Select classes");
		}
		
		return selectImplementations;
    }

    @Override
    protected void findElement() {
    	synchronized (lock) {
    		if (selectImplementations == null) {
        		selectImplementations = searchRelevantImplementation();
        	}
		}

    	// set a default type so that use of selectImplementation can be done
    	selectImplementation = new StubSelect();
    	
        super.findElement(true);
        
        // search the right select list handler
        for (Class<? extends ISelectList> selectClass: selectImplementations) {
        	try {
				ISelectList selectInstance = selectClass.getConstructor(WebElement.class, FrameElement.class).newInstance(element, frameElement);
				if (selectInstance.isApplicable()) {
					selectImplementation = selectInstance;
					break;
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			}
        }
        
        if (selectImplementation instanceof StubSelect) {
        	throw new ScenarioException("Cannot find type of select " + element.getTagName());
        }
        
        options = selectImplementation.getOptions();
        multiple = selectImplementation.isMultipleWithoutFind();
    }

    /**
     * Returns a new Select element (created to facilitate unit testing).
     *
     * @return
     */
    protected Select getNewSelectElement(final WebElement element) {
        return new Select(element);
    }


    @ReplayOnError
    public List<WebElement> getOptions() {
    	try {
	        findElement();
	        return options;
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }
    
    /**
     * @return The first selected option in this select tag (or the currently selected option in a
     *         normal select)
     *         or null If no option is selected
     */
    @ReplayOnError
    public WebElement getFirstSelectedOption() {
    	return _getFirstSelectedOption();
    }
    private WebElement _getFirstSelectedOption() {
    	List<WebElement> allSelectedOptions = _getAllSelectedOptions();
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
    	return _getAllSelectedOptions();
    }
    
    private List<WebElement> _getAllSelectedOptions() {

    	try {
	    	findElement();
			return selectImplementation.getAllSelectedOptions(); 
			
    	} finally {
    		selectImplementation.finalizeAction();
    	}
	}

    /**
     * Do not replayOnError as the called method already does it
     * @return
     */
    public String getSelectedText() {
    	try {
    		return getSelectedTexts()[0];
    	} catch (IndexOutOfBoundsException e) {
    		return "";
    	}
    }

    @ReplayOnError
    public String[] getSelectedTexts() {
    	List<WebElement> allSelectedOptions = _getAllSelectedOptions();
    	List<String> textList = new ArrayList<>();
    	
        for (WebElement option : allSelectedOptions) {
        	textList.add(selectImplementation.getOptionText(option));
        }

        String[] texts = new String[textList.size()];
        return textList.toArray(texts);
    }

    @ReplayOnError
    public String getSelectedValue() {
    	WebElement firstSelectedOption = _getFirstSelectedOption();
    	if (firstSelectedOption != null) {
    		return selectImplementation.getOptionValue(firstSelectedOption);
    	} else {
    		return "";
    	}
    }

    @ReplayOnError
    public String[] getSelectedValues() {
    	List<WebElement> allSelectedOptions = _getAllSelectedOptions();
    	List<String> valueList = new ArrayList<>();
    	
        for (WebElement option : allSelectedOptions) {
        	valueList.add(selectImplementation.getOptionValue(option));
        }

        String[] texts = new String[valueList.size()];
        return valueList.toArray(texts);
    }

    @ReplayOnError
    public boolean isMultiple() {
    	try {
	        findElement();
	
	        return selectImplementation.isMultipleWithoutFind();
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }
    
    protected boolean isMultipleSelect() {
    	return multiple;
    }
    
    /**
     * De-selects all options in a multi-select list element.
     */
    @ReplayOnError
    public void deselectAll() {
    	
    	List<WebElement> allSelectedOptions = _getAllSelectedOptions();
    	
    	if (!isMultipleSelect()) {
            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
        }
    	
        for (WebElement option : allSelectedOptions) {
        	selectImplementation.setDeselected(option);
        }
    }

    @ReplayOnError
    public void deselectByIndex(final Integer index) {
    	
    	
    	try {
	    	findElement();
	    	
	    	if (!isMultipleSelect()) {
	            throw new UnsupportedOperationException("You may only deselect options of a multi-select");
	        }
	    	selectImplementation.deselectByIndex(index);
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

    @ReplayOnError
    public void deselectByText(final String text) {
    
    	try {
	    	findElement();
	    	
	    	if (!isMultipleSelect()) {
	            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
	        }
	    	
	    	selectImplementation.deselectByText(text);
			
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

    @ReplayOnError
    public void deselectByValue(final String value) {

    	try {
	    	findElement();
	    	
	    	if (!isMultipleSelect()) {
	            throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
	        }
	    	
	    	selectImplementation.deselectByValue(value);
			
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByIndex(final Integer index) {
    	try {
	        findElement();
	        
	        selectImplementation.selectByIndex(index);
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByIndex(int ... indexs) {
    	 try {
 	    	findElement();
 	    	
 	    	for (int i = 0; i < indexs.length; i++) {
 	    		selectImplementation.selectByIndex(indexs[i]);
 	    	}
     	} finally {
     		selectImplementation.finalizeAction();
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
	        
	    	selectImplementation.selectByText(text);
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByText(String ... texts) {
    	try {
	        findElement();
	        
	        for (int i = 0; i < texts.length; i++) {
	        	selectImplementation.selectByText(texts[i]);
	        }
	       
    	} finally {
    		selectImplementation.finalizeAction();
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
    		selectImplementation.finalizeAction();
    	}
    }
    
    /**
     * Multiple select by attribute text, similar select
     * For each text, only one option will be selected
     * 
     * @param text
     */
    @ReplayOnError
    public void selectByCorrespondingText(String ... text) {
    	try {
	    	findElement();
	    	for (int i = 0; i < text.length; i++) {
	    		selectCorrespondingText(text[i]);
	    	}
    	} finally {
    		selectImplementation.finalizeAction();
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
    		selectImplementation.setSelected(optionToSelect);
    	} else {
    		throw new NoSuchElementException("Cannot locate option with corresponding text " + text);
    	}
    }
    
    @ReplayOnError
    public void deselectByCorrespondingText(final String text) {
    	
    	try {
    		findElement();
    		
        	if (!isMultipleSelect()) {
                throw new UnsupportedOperationException("You may only deselect all options of a multi-select");
            }

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
	    		selectImplementation.setDeselected(optionToSelect);
	    	} else {
	    		throw new NoSuchElementException("Cannot locate option with corresponding text " + text);
	    	}
    		
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByValue(final String value) {
    	try {
	    	findElement();
	        
	    	selectImplementation.selectByValue(value);
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

    @ReplayOnError
    public void selectByValue(final String ... values) {
    	try {
	        findElement();
	        for (int i = 0; i < values.length; i++) {
	        	selectImplementation.selectByValue(values[i]);
	        }
    	} finally {
    		selectImplementation.finalizeAction();
    	}
    }

	public ISelectList getSelectImplementation() {
		return selectImplementation;
	}
    
}
