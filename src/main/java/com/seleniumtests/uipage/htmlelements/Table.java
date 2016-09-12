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
import org.openqa.selenium.WebElement;

public class Table extends HtmlElement {
    private List<WebElement> rows = null;
    private List<WebElement> columns = null;

    public Table(final String label, final By by) {
        super(label, by);
    }

    @Override
    public void findElement() {
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
        	return new ArrayList<WebElement>();
        }
    }
    
    public List<WebElement> getColumns() {
    	findElement();
    	return columns;
    }

    public int getColumnCount() {
    	findElement();
        return columns.size();
    }

    /**
     * Get table cell content.
     *
     * @param  row     Starts from 1
     * @param  column  Starts from 1
     */
    public String getContent(final int row, final int column) {
        findElement();

        if (rows != null && !rows.isEmpty()) {
            columns = rows.get(row - 1).findElements(By.tagName("td"));

            if (columns == null || columns.isEmpty()) {
                columns = rows.get(row - 1).findElements(By.tagName("th"));
            }
            
            if (columns.isEmpty()) {
            	return null;
            }

            return columns.get(column - 1).getText();
        }

        return null;
    }

    public int getRowCount() {
        getRows();
        return rows.size();
    }

    public List<WebElement> getRows() {
        findElement();
        return rows;
    }

    public boolean isHasBody() {
        return !getRows().isEmpty();
    }
}
