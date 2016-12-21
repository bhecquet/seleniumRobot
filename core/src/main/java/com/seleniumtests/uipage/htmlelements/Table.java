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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.ReplayOnError;

public class Table extends HtmlElement {
    private List<WebElement> rows = null;
    private List<WebElement> columns = null;

    public Table(final String label, final By by) {
        super(label, by);
    }
    
    public Table(final String label, final By by, final HtmlElement parent) {
    	super(label, by, parent);
    }

    public Table(final String label, final By by, final HtmlElement parent, final int index) {
    	super(label, by, parent, index);
    }
    
    public Table(final String label, final By by, final FrameElement frame) {
    	super(label, by, frame);
    }

    public void findTableElement() {
        super.findElement();
        rows = element.findElements(By.tagName("tr"));
        columns = getColumnsInternal();
    }

    private List<WebElement> getColumnsInternal() {
        
        // Need to check whether rows is null AND whether or not the list of rows is empty
        if (rows != null && !rows.isEmpty()) {
            columns = rows.get(0).findElements(By.tagName("td"));
            if (columns == null || columns.isEmpty()) {

                if (rows.size() > 1) {
                    columns = rows.get(1).findElements(By.tagName("td"));
                } else {
                    columns = rows.get(0).findElements(By.tagName("th"));
                }
            }
            return columns;
        } else {
        	return new ArrayList<>();
        }
    }
    
    @ReplayOnError
    public List<WebElement> getColumns() {
    	findTableElement();
    	return columns;
 
    }

    @ReplayOnError
    public int getColumnCount() {
    	findTableElement();
        return columns.size();
    }
    
    public List<WebElement> getRowCells(WebElement row) {
    	List<WebElement> cells;
    	if (row == null) {
    		return new ArrayList<>();
    	}
    	cells = row.findElements(By.tagName("td"));
    	if (cells.isEmpty()) {
    		cells = row.findElements(By.tagName("th"));
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
     * @param content	pattern to search for
     * @param column	column where pattern should be searched
     * @return
     */
    public WebElement getCellFromContent(final Pattern content, final int column) {
    	findTableElement();
    	
    	if (rows != null && !rows.isEmpty()) {
    		for (WebElement row: rows) {
    			columns = row.findElements(By.tagName("td"));
    			
    			if (columns == null || columns.isEmpty()) {
        			columns = row.findElements(By.tagName("th"));
        		}
    			
    			if (columns.isEmpty()) {
        			throw new ScenarioException("There are no columns in this table");
        		}
    			
    			WebElement cell = columns.get(column);
				Matcher matcher = content.matcher(cell.getText());
				if (matcher.matches()) {
					return cell;
				}
    		}
    		throw new ScenarioException(String.format("Pattern %s has not been found in table", content.pattern()));
    		
    	} else {
    		throw new ScenarioException("There are no rows in this table");
    	}
    }
    
    /**
     * Get table cell.
     */
    @ReplayOnError
    public WebElement getCell(final int row, final int column) {
    	findTableElement();
    	
    	if (rows != null && !rows.isEmpty()) {
    		columns = rows.get(row).findElements(By.tagName("td"));
    		
    		if (columns == null || columns.isEmpty()) {
    			columns = rows.get(row).findElements(By.tagName("th"));
    		}
    		
    		if (columns.isEmpty()) {
    			throw new ScenarioException(String.format("Cell at (%d, %d) could not be found", row, column));
    		}
    		
    		return columns.get(column);
    	}
    	
    	throw new ScenarioException("There are no rows in this table");
    }

    public int getRowCount() {
        getRows();
        return rows.size();
    }

    @ReplayOnError
    public List<WebElement> getRows() {
    	findTableElement();
        return rows;
    }

    public boolean isHasBody() {
        return !getRows().isEmpty();
    }
}
