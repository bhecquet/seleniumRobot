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
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Spy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.uipage.htmlelements.Table;


@PrepareForTest(WebUIDriver.class)
public class TestTable extends MockitoTest {
	
	@Mock
	private CustomEventFiringWebDriver driver;
	
	@Mock
	private WebElement tableEl;
	@Mock
	private WebElement row1;
	@Mock
	private WebElement row2;
	@Mock
	private WebElement col1;
	@Mock
	private WebElement col2;
	
	@Mock
	private TargetLocator locator;
	
	@Spy
	private Table table = new Table("", By.id("table"));
	
	private List<WebElement> rowEl;
	
	@BeforeMethod(groups={"ut"})
	private void init() {
		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
		when(driver.findElement(By.id("table"))).thenReturn(tableEl);
		when(driver.switchTo()).thenReturn(locator);
		
		rowEl = new ArrayList<>();
		rowEl.add(row1);
		rowEl.add(row2);
		
		when(tableEl.findElements(By.tagName("tr"))).thenReturn(rowEl);
	}
	
	/**
	 * Always check that findElement is called when accessing table
	 */
	@AfterMethod(groups={"ut"}) 
	public void checkTableRefresh() {
		verify(table).findTableElement();
	}

	@Test(groups={"ut"})
	public void testGetRows() throws Exception {
		Assert.assertEquals(table.getRows().size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testGetColumns() throws Exception {
		
		List<WebElement> colEl = new ArrayList<>();
		colEl.add(col1);
		colEl.add(col2);
		
		when(table.getRowCells(row1)).thenReturn(colEl);
		
		Assert.assertEquals(table.getColumns().size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testGetColumnsWithoutRows() throws Exception {
		when(tableEl.findElements(By.tagName("tr"))).thenReturn(new ArrayList<>());
		
		Assert.assertEquals(table.getColumns().size(), 0);
	}
	
	@Test(groups={"ut"})
	public void testGetColumnsWithFirstEmptyRow() throws Exception {
		
		List<WebElement> colEl = new ArrayList<>();
		colEl.add(col1);
		colEl.add(col2);
		

		when(table.getRowCells(row2)).thenReturn(colEl);
		
		Assert.assertEquals(table.getColumns().size(), 2);
	}

}
