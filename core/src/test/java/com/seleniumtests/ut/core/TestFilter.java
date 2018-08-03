/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.ut.core;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.Filter;

public class TestFilter extends GenericTest {

	// parameters with Filter'name (key) and its value(s)
	final Map<String, Object> parameters1 = new HashMap<>();
		
	/**
	 * Method to set dates.
	 * @param pDate
	 * @param pStringDate
	 * @return the given date, filled with the given string
	 */
	public Date dateFromString(Date pDate, String pStringDate){
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
		try {
			pDate = df.parse(pStringDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return pDate;
	}
	
	/**
	 * Initialize the parameters
	 */
    @BeforeClass(groups={"ut"})
    public void beforeMethod() {
    	parameters1.put("isEqual", "whatever1");
    	parameters1.put("isEqualIngoreCase", "whatever1");
    	parameters1.put("lessThanNumber", 5);
    	parameters1.put("greaterThanNumber", 5);
    	Date dateEx = new Date();
    	dateEx = dateFromString(dateEx, "06-30-2016");
    	parameters1.put("lessThanDate", dateEx);
//    	parameters1.put("greaterThanDate", dateEx);
    	parameters1.put("in", "whatever2");
    	parameters1.put("contains", "what if whatever1 with whatnut");
    	parameters1.put("containsIgnoreCase", "what if whatever1 with whatnut");
//    	parameters1.put("startWith", "whatever1");
//    	parameters1.put("startWithIgnoreCase", "whatever1");
//    	parameters1.put("endWith", "whatever1");
//    	parameters1.put("endWithIgnoreCase", "whatever1");
//    	parameters1.put("or", "whatever1");
//    	parameters1.put("and", "whatever1");
    }

    /**
     * Test if the parameters do not contain a Filter's name (key)
     */
    @Test(groups={"ut"})
    public void wrongName() {
		Filter f = Filter.isEqual("equals", "whatever1"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* EQUALS ************************
     * Test if the parameters contain a Filter's name (key) 
     * EQUALS with the exact same value, then match is true.
     */
    @Test(groups={"ut"})
    public void isEqual() {
    	Filter f = Filter.isEqual("isEqual", "whatever1"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* EQUALS ************************
     * Test if the parameters contain a Filter's name (key) 
     * EQUALS with the wrong value, then match is false.
     */
    @Test(groups={"ut"})
    public void isEqualWrong() {
		Filter f = Filter.isEqual("isEqual", "whatever2"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* EQUALS ************************
     * Test if the parameters contain a Filter's name (key) 
     * EQUALS with the same value, but different case character, then match is false.
     */
    @Test(groups={"ut"})
    public void isEqualCase() {
		// with value but different case, is false
		Filter f = Filter.isEqual("isEqual", "WhatEver1"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* EQUALS_INGORE_CASE ************
     * Test if the parameters contain a Filter's name (key) 
     * EQUALS_INGORE_CASE with the same value, but different case character, then match is true.
     */
    @Test(groups={"ut"})
    public void isEqualIgnoreCase() {
		Filter f = Filter.isEqualIgnoreCase("isEqualIngoreCase", "WhatEver1"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* EQUALS_INGORE_CASE ************
     * Test if the parameters contain a Filter's name (key) 
     * EQUALS_INGORE_CASE with the wrong value, then match is false.
     */
    @Test(groups={"ut"})
    public void isEqualIgnoreCaseWrong() {
		Filter f = Filter.isEqualIgnoreCase("isEqualIngoreCase", "whatever2"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* LESS_THAN **********************
     * Test if the parameters contain a Filter's name (key) 
     * LESS_THAN with a lower Number value (than the filter), then match is true.
     */
    @Test(groups={"ut"})
    public void lessThanNumberHigherF() {
    	Filter f = Filter.lt("lessThanNumber", 6); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* LESS_THAN **********************
     * Test if the parameters contain a Filter's name (key) 
     * LESS_THAN with a lower Date value (than the filter), then match is true.
     */
    @Test(groups={"ut"})
    public void lessThanDateHigherF() {
    	Date dateF = new Date();
    	dateF = dateFromString(dateF, "07-30-2016");
    	Filter f = Filter.lt("lessThanDate", dateF); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* LESS_THAN **********************
     * Test if the parameters contain a Filter's name (key) 
     * LESS_THAN with a higher Number value (than the filter), then match is true.
     */
    @Test(groups={"ut"})
    public void lessThanNumberLowerF() {
    	Filter f = Filter.lt("lessThanNumber", 4); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* LESS_THAN **********************
     * Test if the parameters contain a Filter's name (key) 
     * LESS_THAN with a higher Date value (than the filter), then match is true.
     */
    @Test(groups={"ut"})
    public void lessThanDateLowerF() {
    	Date dateF = new Date();
    	dateF = dateFromString(dateF, "05-30-2016");
    	Filter f = Filter.lt("lessThanDate", dateF); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* LESS_THAN **********************
     * Test if the parameters contain a Filter's name (key) 
     * LESS_THAN with the same Number value (than the filter), then match is false.
     */
    @Test(groups={"ut"})
    public void lessThanNumberSameF() {
    	Filter f = Filter.lt("lessThanNumber", 5); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* LESS_THAN **********************
     * Test if the parameters contain a Filter's name (key) 
     * LESS_THAN with the same Date value (than the filter), then match is false.
     */
    @Test(groups={"ut"})
    public void lessThanDateSameF() {
    	Date dateF = new Date();
    	dateF = dateFromString(dateF, "06-30-2016");
    	Filter f = Filter.lt("lessThanDate", dateF); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* GREATER_THAN *******************
     * Test if the parameters contain a Filter's name (key) 
     * GREATER_THAN with a higher Number value (than the filter), then match is true.
     */
    @Test(groups={"ut"})
    public void greaterThanNumberLowerF() {
    	Filter f = Filter.greaterThan("greaterThanNumber", 4); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /*
     * GREATER_THAN filter (date) not created yet.
     */
    
    /********* GREATER_THAN *******************
     * Test if the parameters contain a Filter's name (key) 
     * GREATER_THAN with a lower Number value (than the filter), then match is false.
     */
    @Test(groups={"ut"})
    public void greaterThanNumberHigherF() {
    	Filter f = Filter.greaterThan("greaterThanNumber", 6); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* GREATER_THAN *******************
     * Test if the parameters contain a Filter's name (key) 
     * GREATER_THAN with the same Number value (than the filter), then match is false.
     */
    @Test(groups={"ut"})
    public void greaterThanNumber() {
    	Filter f = Filter.greaterThan("greaterThanNumber", 5); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /*
     * BETWEEN filters (number and date) not created yet.
     */
    
    /********* IN *****************************
     * Test if the parameters contain a Filter's name (key)
     * IN with a value of the filter's array, then match is true
     */
    @Test(groups={"ut"})
    public void in() {
    	String[] t = {"whatever1", "whatever2", "whatever3"};
    	Filter f = Filter.in("in", t); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* IN *****************************
     * Test if the parameters contain a Filter's name (key)
     * IN without a value of the filter's array, then match is false
     */
    @Test(groups={"ut"})
    public void inWrong() {
		// 
		String[] t = {"whatever1", "whatever3"};
    	Filter f = Filter.in("in", t); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* IN *****************************
     * Test if the parameters contain a Filter's name (key)
     * IN with a value of the filter's array, but with different case, is false
     */
    @Test(groups={"ut"})
    public void inCase() {
		String[] t = {"whatever1", "WhatEver2", "whatever3"};
    	Filter f = Filter.in("in", t); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /*
     * IS_NULL filter not created yet.
     */
    
    /********* NOT ****************************
     * Test if the match result is the opposite, 
     * NOT what the given Filter would output.
     * i.e, false when it should be true
     */
    @Test(groups={"ut"})
    public void not() {
		final Filter f1 = Filter.contains("contains", "whatever1"); 
		Filter fNot = Filter.not(f1); 
		boolean result = fNot.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* NOT ****************************
     * Test if the match result is the opposite, 
     * NOT what the given Filter would output.
     * i.e, true when it should be false
     */
    @Test(groups={"ut"})
    public void notWrong() {
		final Filter f1 = Filter.contains("contains", "whatever2"); 
		Filter fNot = Filter.not(f1); 
		boolean result = fNot.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* CONTAINS ************************
     * Test if the parameters contain a Filter's name (key)
     * CONTAINS with a value that contains the Filter's value, then match is true
     */
    @Test(groups={"ut"})
    public void contains() {
		Filter f = Filter.contains("contains", "whatever1"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* CONTAINS ************************
     * Test if the parameters contain a Filter's name (key)
     * CONTAINS with a value that does not contain the Filter's value, then match is false
     */
    @Test(groups={"ut"})
    public void containsWrong() {
		Filter f = Filter.contains("contains", "whatever2"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* CONTAINS ************************
     * Test if the parameters contain a Filter's name (key)
     * CONTAINS with a value that contains the Filter's value, 
     * but with different case, then match is false
     */
    @Test(groups={"ut"})
    public void containsCase() {
		Filter f = Filter.contains("contains", "wWhatEver1"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* CONTAINS_IGNORE_CASE *************
     * Test if the parameters contain a Filter's name (key)
     * CONTAINS_IGNORE_CASE with a value that contains the Filter's value, 
     * but with different case, then match is true
     */
    @Test(groups={"ut"})
    public void containsIgnoreCase() {
    	Filter f = Filter.containsIgnoreCase("containsIgnoreCase", "WhatEver1"); 
		boolean result = f.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* CONTAINS_IGNORE_CASE *************
     * Test if the parameters contain a Filter's name (key)
     * CONTAINS_IGNORE_CASE with a value that does not contain 
     * the Filter's value, then match is false
     */
    @Test(groups={"ut"})
    public void containsIgnoreCaseWrong() {
    	Filter f = Filter.containsIgnoreCase("containsIgnoreCase", "whatever2"); 
    	boolean result = f.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /*
     * START_WITH filter not created yet.
     */
  
    /*
     * END_WITH filter not created yet.
     */
    
    /********* AND ****************************
     * Test if the match result true, 
     * if the 2 given Filters match true
     */
    @Test(groups={"ut"})
    public void andTwo() {
		final Filter f1 = Filter.contains("contains", "whatever1"); 
		final Filter f2 = Filter.contains("contains", "whatnut"); 
		Filter fAnd = Filter.and(f1, f2); 
		boolean result = fAnd.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* AND ****************************
     * Test if the match result false, 
     * if the 1 given Filters match true
     */
    @Test(groups={"ut"})
    public void andOne() {
		final Filter f1 = Filter.contains("contains", "whatever1"); 
		final Filter f2 = Filter.contains("contains", "whatever2"); 
		Filter fAnd = Filter.and(f1, f2); 
		boolean result = fAnd.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* AND ****************************
     * Test if the match result false, 
     * if the 2 given Filters match false
     */
    @Test(groups={"ut"})
    public void andZero() {
		final Filter f1 = Filter.contains("contains", "whatever3"); 
		final Filter f2 = Filter.contains("contains", "whatever2"); 
		Filter fAnd = Filter.and(f1, f2); 
		boolean result = fAnd.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
    /********* OR *****************************
     * Test if the match result true, 
     * if the 2 given Filters match true
     */
    @Test(groups={"ut"})
    public void orTwo() {
		final Filter f1 = Filter.contains("contains", "whatever1"); 
		final Filter f2 = Filter.contains("contains", "whatnut"); 
		Filter fOr = Filter.or(f1, f2); 
		boolean result = fOr.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* OR *****************************
     * Test if the match result true, 
     * if the 1 given Filters match true
     */
    @Test(groups={"ut"})
    public void orOne() {
		final Filter f1 = Filter.contains("contains", "whatever1"); 
		final Filter f2 = Filter.contains("contains", "whatever2"); 
		Filter fOr = Filter.or(f1, f2); 
		boolean result = fOr.match(parameters1);
		Assert.assertEquals(result, true);
    }
    
    /********* OR *****************************
     * Test if the match result false, 
     * if the 2 given Filters match false
     */
    @Test(groups={"ut"})
    public void orZero() {
		final Filter f1 = Filter.contains("contains", "whatever3"); 
		final Filter f2 = Filter.contains("contains", "whatever2"); 
		Filter fOr = Filter.or(f1, f2); 
		boolean result = fOr.match(parameters1);
		Assert.assertEquals(result, false);
    }
    
}
