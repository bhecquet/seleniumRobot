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
package com.seleniumtests.ut.util.helper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.Filter;
import com.seleniumtests.customexception.DatasetException;
import com.seleniumtests.util.helper.CSVHelper;
import com.seleniumtests.util.helper.SpreadSheetHelper;
import com.seleniumtests.util.internal.entity.TestEntity;

public class testSpreadSheetHelper extends GenericTest {
	Map<String, Object> classMap = new LinkedHashMap<String, Object>();
	String test = "violet";


	/**
	 * Test Exception for a non .csv file
	 */
	@Test(groups={"ut"},  expectedExceptions = DatasetException.class)
	public void testExceptionNoCSV(){
		SpreadSheetHelper.getDataFromSpreadsheet(null, "src/test/resources/ti/excel/test.xlsx", null, true);
	}
	
	/**
	 * Test read headers from a CSV file (first line, result with ; separator between rows)
	 */
	@Test(groups={"ut"})
	public void testgetHeadersFromCSV(){
		List<String> data = CSVHelper.getHeaderFromCSVFile(null, "src/test/resources/ti/excel/test.csv", null);
		Assert.assertTrue(data.get(0).equals("test;test2"));
		Assert.assertEquals(data.size(), 1);
	}
	
	/**
	 * Test read CSV file, all lines and no more, with the headers, without conditions.
	 */
	@Test(groups={"ut"})
	public void testgetDataFromCSV(){
		Iterator<Object[]> data = CSVHelper.getDataFromCSVFile(null, "src/test/resources/ti/excel/test.csv", null, true, true);
		Assert.assertTrue(data.next()[0].equals("test;test2"));
		Assert.assertTrue(data.next()[0].equals("jaune;vert"));
		Assert.assertTrue(data.next()[0].equals("bleu;rouge"));
		Assert.assertFalse(data.hasNext());
	}
	
	/**
	 * Test read Entities from CSV file (no headers), without conditions.
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testgetEntitiesFromSpreadSheet() throws Exception{
	        Iterator<Object[]> dataIterator = SpreadSheetHelper.getEntitiesFromSpreadsheet(null, null, "src/test/resources/ti/excel/test.csv", null);
	        Assert.assertTrue(dataIterator.next()[0].equals("jaune;vert"));
			Assert.assertTrue(dataIterator.next()[0].equals("bleu;rouge")); 
			Assert.assertFalse(dataIterator.hasNext());
	}
	
	/**
	 * Test read Entities from CSV file (no headers), with filter: select line containing "jaune".
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testgetEntitiesWithFilterFromSpreadSheet() throws Exception{
			Filter filter = Filter.contains("test;test2", "jaune");
	        Iterator<Object[]> dataIterator = SpreadSheetHelper.getEntitiesFromSpreadsheet(null, null, "src/test/resources/ti/excel/test.csv", filter);
	        Assert.assertTrue(dataIterator.next()[0].equals("jaune;vert"));
			Assert.assertFalse(dataIterator.hasNext());
	}
	
	/**
	 * Test get Value from a Map<String, Object>.
	 */
	@Test(groups={"ut"})
	public void testgetValue() {
		classMap.clear();
	 	classMap.put("Test", true);
	 	classMap.put("NoTest", false);
		Object test = SpreadSheetHelper.getValue(classMap, "Test");
		Assert.assertEquals(test, true);	
	}
	
	/**
	 * Test getFieldsDataNeedToBeSet work as expected
	 */
	@Test(groups={"ut"})
	public void testgetFieldsDataNeedToBeSet() {
		classMap.clear();
	 	classMap.put("Test", true);
	 	classMap.put("Test.No.Blurp", false);
		Map<String, Object> testMap = SpreadSheetHelper.getFieldsDataNeedToBeSet(classMap, "test");
		Assert.assertEquals(testMap.get("Test"), "true");
		Assert.assertEquals(testMap.get("No.Blurp"), "false");
		Assert.assertEquals(testMap.size(), 2);
	}
	
	/**
	 * Test getFieldsNeedToBeSet work as expected
	 */
	@Test(groups={"ut"})
	public void testgetFieldsNeedToBeSet() {
		classMap.clear();
	 	classMap.put("Test", true);
	 	classMap.put("Test.Blurp", "coucou");
	 	classMap.put("Test.No.Blurp", false);
		Map<String, Object> testMap = SpreadSheetHelper.getFieldsNeedToBeSet(classMap, "test");
		Assert.assertEquals(testMap.get("Test"), true);
		Assert.assertEquals(testMap.get("Blurp"), "coucou");
		Assert.assertEquals(testMap.get("No"), false);
		Assert.assertEquals(testMap.size(), 3);
	}
	
	/**
	 * Test read Object
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testreadObject() throws Exception {
		classMap.clear();
	 	classMap.put("Test", "violet");
		Object test = SpreadSheetHelper.readObject(this.getClass(), "test", classMap);
		Assert.assertEquals(test.toString().split("@")[0], this.getClass().getName());
	}
	
	/**
	 * Test getArraySize work as expected.
	 */
	@Test(groups={"ut"})
	public void testgetArraySize() {
		classMap.clear();
		String num = Integer.toHexString(5);
	 	classMap.put("Test."+num, "coucou");
	 	int size = SpreadSheetHelper.getArraySize(classMap, "test");
	 	Assert.assertEquals(size,  6);
	
	}
	
	/**
	 * Test read Entities from CSV file (no headers), with HashMap.
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testgetEntitiesWithHashMapFromSpreadSheet() throws Exception{
		 	LinkedHashMap<String, Class<?>> classMap2 = new LinkedHashMap<String, Class<?>>();
		 	classMap2.put("TestEntity", TestEntity.class);
	        Iterator<Object[]> dataIterator = SpreadSheetHelper.getEntitiesFromSpreadsheet(null, classMap2, "src/test/resources/ti/excel/test.csv", null);
	        Assert.assertTrue(dataIterator.next()[0].equals("jaune;vert"));
	        Assert.assertTrue(dataIterator.next()[0].equals("bleu;rouge"));
			Assert.assertFalse(dataIterator.hasNext());
	}
	
}
