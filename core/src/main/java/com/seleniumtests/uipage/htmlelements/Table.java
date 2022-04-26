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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.uipage.ReplayOnError;

/**
 * Class that represents an html table. Only these tables are supported
 * @author s047432
 *
 */
public class Table extends HtmlElement {
    private static final String ERROR_NO_ROWS = "There are no rows in this table";
	private List<WebElement> rows = null;
    private List<WebElement> columns = null;

    public Table(final String label, final By by) {
        super(label, by);
    }

    public Table(final String label, final By by, final Integer index) {
    	super(label, by, index);
    }
    
    public Table(final String label, final By by, final Integer index, Integer replayTimeout) {
    	super(label, by, index, replayTimeout);
    }
    
    public Table(final String label, final By by, final HtmlElement parent) {
    	super(label, by, parent);
    }
    
    public Table(final String label, final By by, final HtmlElement parent, final Integer index) {
    	super(label, by, parent, index);
    }
    
    public Table(final String label, final By by, final HtmlElement parent, final Integer index, Integer replayTimeout) {
    	super(label, by, parent, index, replayTimeout);
    }
     
    public Table(final String label, final By by, final FrameElement frame) {
    	super(label, by, frame);
    }
    
    public Table(final String label, final By by, final FrameElement frame, final Integer index) {
    	super(label, by, frame, index);
    }
    
    public Table(final String label, final By by, final FrameElement frame, final Integer index, Integer replayTimeout) {
    	super(label, by, frame, index, replayTimeout);
    }

    /**
     * Get rows and columns
     * rows correspond to <tr> html elements
     * columns correspond to <th> elements if they exist, or the <td> elements of the first row
     */
    public void findTableElement() {
        super.findElement();
        rows = findHtmlElements(By.tagName("tr"));
        columns = getColumnsInternal();
    }

    private List<WebElement> getColumnsInternal() {
        
        // Need to check whether rows is null AND whether or not the list of rows is empty
        if (rows != null && !rows.isEmpty()) {
        	
        	columns = getRowCells(rows.get(0));
        	
            if ((columns == null || columns.isEmpty()) && rows.size() > 1) {
                columns = getRowCells(rows.get(1));
            }
            return columns;
        } else {
        	return new ArrayList<>();
        }
    }
    
    /**
     * Returns column list
     * @return
     */
    @ReplayOnError
    public List<WebElement> getColumns() {
    	findTableElement();
    	return columns;
 
    }

    /**
     * Returns the number of columns
     * @return
     */
    @ReplayOnError
    public int getColumnCount() {
    	findTableElement();
        return columns.size();
    }
    
    /**
     * Given a row WebElement (represents a <tr> html element), returns the list of elements (<tr> or <th>)
     * 
     * Tip: returned element is a list of HtmlElement, but you must cast it to use its specific methods
     * 
     * @param row	the row to analyze
     * @return
     */
    public List<WebElement> getRowCells(WebElement row) {
    	List<WebElement> cells;
    	if (row == null) {
    		return new ArrayList<>();
    	}
    	
    	By descendants = By.xpath(".//descendant::*[name()=\"th\" or name()=\"td\"]");
    	if (row instanceof HtmlElement) {
    		cells = ((HtmlElement)row).findHtmlElements(descendants);
    	} else {
    		cells = row.findElements(descendants);
    	}
    	return cells;
    }

    /**
     * Get table cell content.
     */

    public String getContent(final int row, final int column) {
    	return getCell(row, column).getText();
    }
    
    /**
     * Returns the cell from table, searching for its content by pattern
     * 
     * Tip: returned element is a HtmlElement, but you must cast it to use its specific methods 
     * 
     * @param content	pattern to search for
     * @param column	column where pattern should be searched
     * @return
     */
    @ReplayOnError
    public WebElement getCellFromContent(final Pattern content, final int column) {
    	findTableElement();
    	
    	if (rows != null && !rows.isEmpty()) {
    		for (WebElement row: rows) {
    			List<WebElement> cols = getRowCells(row);
    			
    			if (cols.isEmpty()) {
        			throw new ScenarioException("There are no columns in this row");
        		}
    			
    			WebElement cell = cols.get(column);
				Matcher matcher = content.matcher(cell.getText());
				if (matcher.matches()) {
					return cell;
				}
    		}
    		throw new ScenarioException(String.format("Pattern %s has not been found in table", content.pattern()));
    		
    	} else {
    		throw new ScenarioException(ERROR_NO_ROWS);
    	}
    }
    
    /**
     * issue #306
     * Returns the row from table, searching for its content by pattern. Then you can search for a specific cell using 'getRowCells(row);'
     *  
     * Tip: returned element is a HtmlElement, but you must cast it to use its specific methods
     * 
     * @param content	pattern to search for
     * @param column	column where pattern should be searched
     * @return
     */
    @ReplayOnError
    public WebElement getRowFromContent(final Pattern content, final int column) {
    	findTableElement();
    	
    	if (rows != null && !rows.isEmpty()) {
    		for (WebElement row: rows) {
    			List<WebElement> cols = getRowCells(row);
    			
    			if (cols.isEmpty()) {
    				throw new ScenarioException("There are no columns in this row");
    			}
    			
    			WebElement cell = cols.get(column);
    			Matcher matcher = content.matcher(cell.getText());
    			if (matcher.matches()) {
    				return row;
    			}
    		}
    		throw new ScenarioException(String.format("Pattern %s has not been found in table", content.pattern()));
    		
    	} else {
    		throw new ScenarioException(ERROR_NO_ROWS);
    	}
    }
    
    /**
     * Get a table cell at row,column coordinates
     * 
     * Tip: returned element is a HtmlElement, but you must cast it to use its specific methods
     * 
     * @param row		the row index
     * @param column	the column index
     * @param focus		if true, scroll to this cell
     * @return
     */
    public WebElement getCell(final int row, final int column, boolean focus) {
    	WebElement cell = getCell(row, column);
    	if (focus) {
    		Point loc = cell.getLocation();
    		((CustomEventFiringWebDriver)getDriver()).scrollTo(loc.x - 50, loc.y - 50);
    	}
    	return cell;
    }
    
    /**
     * Get table cell
     * 
     * Tip: returned element is a HtmlElement, but you must cast it to use its specific methods
     */
    @ReplayOnError
    public WebElement getCell(final int row, final int column) {
    	findTableElement();
    	
    	if (rows != null && !rows.isEmpty()) {
    		List<WebElement> cols = getRowCells(rows.get(row));
    		
    		if (cols.isEmpty()) {
    			throw new ScenarioException(String.format("Cell at (%d, %d) could not be found", row, column));
    		}
    		
    		return cols.get(column);
    	}
    	
    	throw new ScenarioException(ERROR_NO_ROWS);
    }

    /**
     * Returns the number of rows
     * @return
     */
    public int getRowCount() {
        getRows();
        return rows.size();
    }

    /**
     * Returns list of rows
     * 
     * Tip: returned element is a list of HtmlElement, but you must cast it to use its specific methods
     * @return
     */
    @ReplayOnError
    public List<WebElement> getRows() {
    	findTableElement();
        return rows;
    }

    public boolean isHasBody() {
        return !getRows().isEmpty();
    }
}
