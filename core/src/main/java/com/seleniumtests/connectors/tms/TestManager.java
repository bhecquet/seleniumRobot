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
package com.seleniumtests.connectors.tms;

import com.seleniumtests.uipage.htmlelements.select.ISelectList;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;

import com.seleniumtests.core.TestVariable;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public abstract class TestManager implements ITestManager {
	

	protected static final Logger logger = SeleniumRobotLogger.getLogger(TestManager.class);
	
	public static final String TMS_TYPE = "tmsType"; 
	public static final String TMS_SERVER_URL = "tmsUrl";
	public static final String TMS_PASSWORD = "tmsPassword";
	public static final String TMS_USER = "tmsUser";
	public static final String TMS_PROJECT = "tmsProject";
	public static final String TMS_DOMAIN = "tmsDomain";
	
    // variable that may be defined dynamically
    public static final String TMS_TEST_ID = "tms.testId";					// name of the variable that identifies the id of the test in TMS
    public static final String TMS_DATASET_ID = "tms.datasetId";					// name of the variable that identifies the id of the dataset in TMS

	protected boolean initialized;
	
	protected TestManager() {
		initialized = false;
	}

	 
    /**
     * Returns the ID of the test case for this test result or null if it's not defined
     * It assumes that test method has been annotated with 'testId' custom attribute {@code @Test(attributes = {@CustomAttribute(name = "testId", values = "12")})}
	 * or that testId has been set inside test
     */
    public Integer getTestCaseId(ITestResult testNGResult) {
    	
    	TestVariable testCaseIdVariable = TestNGResultUtils.getSeleniumRobotTestContext(testNGResult) != null ? TestNGResultUtils.getSeleniumRobotTestContext(testNGResult).getConfiguration().get(TMS_TEST_ID): null;
    	
    	// priority given to variables
    	if (testCaseIdVariable != null) {
    		return Integer.parseInt(testCaseIdVariable.getValue());
    	}
    	
    	for (CustomAttribute customAttribute: testNGResult.getMethod().getAttributes()) {
    		if ("testId".equals(customAttribute.name()) && customAttribute.values().length > 0) {
    			try {
    				return Integer.parseInt(customAttribute.values()[0]);
    			} catch (NumberFormatException e) {
    				logger.error(String.format("Could not parse %s as int for getting testId of test method %s", customAttribute.values()[0], testNGResult.getMethod().getMethodName()));
    			}
    		}
    	}
    	return null;
    	
    }

	/**
	 * Returns the ID of the dataset for this test result or null if it's not defined
	 * It assumes that test method has been annotated with 'datasetId' custom attribute {@code @Test(attributes = {@CustomAttribute(name = "datasetId", values = "12")})}
	 * or that datasetId has been set inside test
	 */
	public Integer getDatasetId(ITestResult testNGResult) {

    	TestVariable datasetIdVariable = TestNGResultUtils.getSeleniumRobotTestContext(testNGResult) != null ? TestNGResultUtils.getSeleniumRobotTestContext(testNGResult).getConfiguration().get(TMS_DATASET_ID): null;

    	// priority given to variables
     	if (datasetIdVariable != null) {
    		return Integer.parseInt(datasetIdVariable.getValue());
    	}

    	for (CustomAttribute customAttribute: testNGResult.getMethod().getAttributes()) {
    		if ("datasetId".equals(customAttribute.name()) && customAttribute.values().length > 0) {
    			try {
    				return Integer.parseInt(customAttribute.values()[0]);
    			} catch (NumberFormatException e) {
    				logger.error(String.format("Could not parse %s as int for getting datasetId of test method %s", customAttribute.values()[0], testNGResult.getMethod().getMethodName()));
    			}
    		}
    	}
    	return null;

    }
	
	public static ITestManager getInstance(JSONObject configString) {

		String type;
		try {
			type = configString.getString(TMS_TYPE);
		} catch (JSONException e) {
			throw new ConfigurationException("Test manager type must be provided. ex: {'tmsType': 'hp', 'run': '3'}");
		}

		ServiceLoader<ITestManager> selectLoader = ServiceLoader.load(ITestManager.class);
		Iterator<ITestManager> selectsIterator = selectLoader.iterator();


		List<String> registeredTypes = new ArrayList<>();
		while (selectsIterator.hasNext())
		{
			ITestManager testManagerClass = selectsIterator.next();
            try {
                ITestManager testManager = testManagerClass.getClass().getConstructor().newInstance();
				registeredTypes.add(testManager.getType());
				if (testManager.getType().equals(type)) {
					return testManager;
				}
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ConfigurationException(testManagerClass.toString() + " cannot be loaded");
            }
		}

		throw new ConfigurationException(String.format("TestManager type [%s] is unknown, valid values are: %s", type, String.join(",", registeredTypes)));

	}

	public boolean getInitialized() {
		return initialized;
	}

}
