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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ReplayOnError;
import com.seleniumtests.uipage.htmlelements.select.ISelectList;
import com.seleniumtests.uipage.htmlelements.select.StubSelect;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

/**
 * Support both standard select tag and fake select consists of tag ul and li.
 */
public class SelectList extends HtmlElement {

	private static List<Class<? extends ISelectList>> selectImplementations;

	static {
		if (selectImplementations == null) {
    		selectImplementations = searchRelevantImplementation();
    	}
	}

    private static final String ERROR_MULTI_SELECT = "You may only deselect all options of a multi-select";
	protected ThreadLocal<List<WebElement>> options = new ThreadLocal<>();
	private StringSimilarityService service = new StringSimilarityServiceImpl(new JaroWinklerStrategy());
	private ThreadLocal<ISelectList> selectImplementation = new ThreadLocal<>();

	protected ThreadLocal<Boolean> multiple = new ThreadLocal<>();

	/**
	 * Creates a SelectList element which represents either
	 * - a standard HTML element => locator should point to the "select" element itself
	 * - a select list built with HTML lists (ul / li) => locator should point to <ul> element
	 * - an angular materials select => locator should point to the <mat-select> element
	 * @param label
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

    	// set a default type so that use of selectImplementation can be done
    	setSelectImplementation(new StubSelect());
    	
        super.findElement(true);

        // search the right select list handler
        for (Class<? extends ISelectList> selectClass: selectImplementations) {
        	try {
				ISelectList selectInstance = selectClass.getConstructor(WebElement.class, FrameElement.class).newInstance(getRealElementNoSearch(), frameElement);
				if (selectInstance.isApplicable()) {
					setSelectImplementation(selectInstance);
					selectInstance.setDriver(getDriver());
					break;
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				logger.error(String.format("Cannot use Select implementation %s: %s", selectClass.getName(), e.getMessage()));
			}
        }
        
        if (getSelectImplementation() instanceof StubSelect) {
        	throw new ScenarioException("Cannot find type of select " + getRealElementNoSearch().getTagName());
        }
        
        setOptions(getSelectImplementation().getOptions());
        setMultipleSelect(getSelectImplementation().isMultipleWithoutFind());

    }

    /**
     * Returns a new Select element (created to facilitate unit testing).
     *
     * @return
     */
    protected Select getNewSelectElement(final WebElement element) {
        return new Select(element);
    }
    
    protected void setOptions(List<WebElement> selectOptions) {
    	options.set(selectOptions);
    }
    
    protected List<WebElement> getOptionsNoSearch() {
    	return options.get();
    }


    @ReplayOnError
    public List<WebElement> getOptions() {
    	try {
	        findElement();
	        return getOptionsNoSearch();
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }
    
    /**
     * @return The first selected option in this select tag (or the currently selected option in a
     *         normal select)
     *         or null If no option is selected
     */
    @ReplayOnError
    public WebElement getFirstSelectedOption() {
    	return getTheFirstSelectedOption();
    }
    private WebElement getTheFirstSelectedOption() {
    	List<WebElement> allSelectedOptions = getAllTheSelectedOptions();
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
    	return getAllTheSelectedOptions();
    }
    
    private List<WebElement> getAllTheSelectedOptions() {

    	try {
	    	findElement();
			return getSelectImplementation().getAllSelectedOptions(); 
			
    	} finally {
    		getSelectImplementation().finalizeAction();
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
    	List<WebElement> allSelectedOptions = getAllTheSelectedOptions();
    	List<String> textList = new ArrayList<>();
    	
        for (WebElement option : allSelectedOptions) {
        	textList.add(getSelectImplementation().getOptionText(option));
        }

        String[] texts = new String[textList.size()];
        return textList.toArray(texts);
    }

    @ReplayOnError
    public String getSelectedValue() {
    	WebElement firstSelectedOption = getTheFirstSelectedOption();
    	if (firstSelectedOption != null) {
    		return getSelectImplementation().getOptionValue(firstSelectedOption);
    	} else {
    		return "";
    	}
    }

    @ReplayOnError
    public String[] getSelectedValues() {
    	List<WebElement> allSelectedOptions = getAllTheSelectedOptions();
    	List<String> valueList = new ArrayList<>();
    	
        for (WebElement option : allSelectedOptions) {
        	valueList.add(getSelectImplementation().getOptionValue(option));
        }

        String[] texts = new String[valueList.size()];
        return valueList.toArray(texts);
    }

    @ReplayOnError
    public boolean isMultiple() {
    	try {
	        findElement();
	
	        return getSelectImplementation().isMultipleWithoutFind();
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }
    
    protected boolean isMultipleSelect() {
    	return multiple.get();
    }
    
    protected void setMultipleSelect(boolean multipleSelect) {
    	multiple.set(multipleSelect);
    }
    
    /**
     * De-selects all options in a multi-select list element.
     */
    @ReplayOnError(waitAfterAction = true)
    public void deselectAll() {
    	
    	List<WebElement> allSelectedOptions = getAllTheSelectedOptions();
    	
    	if (!isMultipleSelect()) {
            throw new UnsupportedOperationException(ERROR_MULTI_SELECT);
        }
    	
        for (WebElement option : allSelectedOptions) {
        	getSelectImplementation().setDeselected(option);
        }
    }

    @ReplayOnError(waitAfterAction = true)
    public void deselectByIndex(final Integer index) {
    	
    	
    	try {
	    	findElement();
	    	
	    	if (!isMultipleSelect()) {
	            throw new UnsupportedOperationException("You may only deselect options of a multi-select");
	        }
	    	getSelectImplementation().deselectByIndex(index);
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

    @ReplayOnError(waitAfterAction = true)
    public void deselectByText(final String text) {
    
    	try {
	    	findElement();
	    	
	    	if (!isMultipleSelect()) {
	            throw new UnsupportedOperationException(ERROR_MULTI_SELECT);
	        }
	    	
	    	getSelectImplementation().deselectByText(text);
			
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

    @ReplayOnError(waitAfterAction = true)
    public void deselectByValue(final String value) {

    	try {
	    	findElement();
	    	
	    	if (!isMultipleSelect()) {
	            throw new UnsupportedOperationException(ERROR_MULTI_SELECT);
	        }
	    	
	    	getSelectImplementation().deselectByValue(value);
			
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

    @ReplayOnError(waitAfterAction = true)
    public void selectByIndex(final Integer index) {
    	try {
	        findElement();
	        
	        getSelectImplementation().selectByIndex(index);
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

    @ReplayOnError(waitAfterAction = true)
    public void selectByIndex(int ... indexs) {
    	 try {
 	    	findElement();
 	    	
 	    	for (int i = 0; i < indexs.length; i++) {
 	    		getSelectImplementation().selectByIndex(indexs[i]);
 	    	}
     	} finally {
     		getSelectImplementation().finalizeAction();
     	}
    }

  
    /**
     * Select standard select by attribute text, and select fake select with ul and li by attribute title.
     *
     * @param  text
     */
    @ReplayOnError(waitAfterAction = true)
    public void selectByText(final String text) {
    	try {
	    	findElement();
	        
	    	getSelectImplementation().selectByText(text);
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

    @ReplayOnError(waitAfterAction = true)
    public void selectByText(String ... texts) {
    	try {
	        findElement();
	        
	        for (int i = 0; i < texts.length; i++) {
	        	getSelectImplementation().selectByText(texts[i]);
	        }
	       
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }
   
    
    /**
     * Select Corresponding select by attribute text, select the most similar text
     *
     * @param  text
     */
    @ReplayOnError(waitAfterAction = true)
    public void selectByCorrespondingText(String text) {
    	try {
	    	findElement();
	    	selectCorrespondingText(text);
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }
    
    /**
     * Multiple select by attribute text, similar select
     * For each text, only one option will be selected
     * 
     * @param text
     */
    @ReplayOnError(waitAfterAction = true)
    public void selectByCorrespondingText(String ... text) {
    	try {
	    	findElement();
	    	for (int i = 0; i < text.length; i++) {
	    		selectCorrespondingText(text[i]);
	    	}
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }
    
    private void selectCorrespondingText(final String text) {
    	double score = 0;
    	WebElement optionToSelect = null;
    	for (WebElement option : getOptionsNoSearch()) {
    		String source = getSelectImplementation().getOptionText(option).trim();
    		if (service.score(source, text) > score) {
    			score = service.score(source, text);
    			optionToSelect = option;
    		}
    	}
    	if (optionToSelect != null) {
    		getSelectImplementation().setSelected(optionToSelect);
    	} else {
    		throw new NoSuchElementException("Cannot locate option with corresponding text " + text);
    	}
    }
    
    @ReplayOnError(waitAfterAction = true)
    public void deselectByCorrespondingText(final String text) {
    	
    	try {
    		findElement();
    		
        	if (!isMultipleSelect()) {
                throw new UnsupportedOperationException(ERROR_MULTI_SELECT);
            }

	    	double score = 0;
	    	WebElement optionToSelect = null;
	    	for (WebElement option : getOptionsNoSearch()) {
	    		String source = option.getText().trim();
	    		if (service.score(source, text) > score) {
	    			score = service.score(source, text);
	    			optionToSelect = option;
	    		}
	    	}
	    	if (optionToSelect != null) {
	    		getSelectImplementation().setDeselected(optionToSelect);
	    	} else {
	    		throw new NoSuchElementException("Cannot locate option with corresponding text " + text);
	    	}
    		
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

    @ReplayOnError(waitAfterAction = true)
    public void selectByValue(final String value) {
    	try {
	    	findElement();
	        
	    	getSelectImplementation().selectByValue(value);
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

    @ReplayOnError(waitAfterAction = true)
    public void selectByValue(final String ... values) {
    	try {
	        findElement();
	        for (int i = 0; i < values.length; i++) {
	        	getSelectImplementation().selectByValue(values[i]);
	        }
    	} finally {
    		getSelectImplementation().finalizeAction();
    	}
    }

	public ISelectList getSelectImplementation() {
		return selectImplementation.get();
	}

	public void setSelectImplementation(ISelectList selectImplementation) {
		this.selectImplementation.set(selectImplementation);
	}
}
