/*
 * Copyright 2015 www.seleniumtests.com
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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
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

    }

    public int getColumnCount() {
        if (rows == null) {
            findElement();
        }

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
        }

        if (columns != null) {
            return columns.size();
        }

        return 0;
    }

    public List<WebElement> getColumns() {
        return columns;
    }

    /**
     * Get table cell content.
     *
     * @param  row     Starts from 1
     * @param  column  Starts from 1
     */
    public String getContent(final int row, final int column) {
        if (rows == null) {
            findElement();
        }

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
        if (rows == null) {
            findElement();
        } else {
            return rows.size();
        }

        int count = element.findElements(By.xpath("tbody/tr")).size();
        if (count == 0) {
            count = element.findElements(By.xpath("tr")).size();
        }

        return count;
    }

    public List<WebElement> getRows() {
    	if (rows == null) {
            findElement();
        }
        return rows;
    }

    public boolean isHasBody() {
        return !getRows().isEmpty();
    }
}
