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
package com.seleniumtests.core.runner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.helper.CSVHelper;
import com.seleniumtests.util.helper.ExcelHelper;

/**
 * This class initializes context, sets up and tears down and clean up drivers An STF test should extend this class.
 */

public  class SeleniumTestPlan extends SeleniumRobotTestPlan {

	@BeforeClass(alwaysRun=true)
	public void configure() {
		setCucumberTest(false);
	}
	

	private File getDatasetFile(Method testMethod) {
		File csvDatasetFile = Paths.get(robotConfig().getApplicationDataPath(), "dataset", robotConfig().getTestEnv(), testMethod.getName() + ".csv").toFile();
		File xlsxDatasetFile = Paths.get(robotConfig().getApplicationDataPath(), "dataset", robotConfig().getTestEnv(), testMethod.getName() + ".xlsx").toFile();
    	
		if (csvDatasetFile.exists()) {
			return csvDatasetFile;
		} else if (xlsxDatasetFile.exists()) {
			return xlsxDatasetFile;
		} else {
			throw new ConfigurationException(String.format("Dataset file %s or %s does not exist", csvDatasetFile, xlsxDatasetFile));
		}
	}
	
	/**
	 * This data provider can be used to read data from a CSV file (NO HEADER) in /data/<app>/dataset/<environment>/<testmethodname>.csv folder
	 * CSV file MUST use ',' as separator. For (semicolon) ';', use datasetSemicolon
	 * @param testMethod
	 * @return
	 * @throws IOException
	 */
    @DataProvider(name = "dataset")
    public Object[][] dataset(Method testMethod) throws IOException {	
    	File dataset = getDatasetFile(testMethod);
    	if (dataset.getName().toLowerCase().endsWith("csv")) {
    		return CSVHelper.read(dataset, ",");
    	} else {
    		List<Map<String, String>> data = new ExcelHelper(dataset).readSheet(0, false);
    		return reformatData(data);
    	}
    }
    
    private Object[][] reformatData(List<Map<String, String>> data) {
    	List<String[]> formattedData = new ArrayList<>();
    	for (Map<String, String> dataLine: data) {
    		formattedData.add((String[]) dataLine.values().toArray(new String[] {}));
    	}
    	String[][] result = new String[formattedData.size()][];
    	return formattedData.toArray(result);
    }
    
    /**
	 * This data provider can be used to read data from a CSV file (WITH HEADER) in /data/<app>/dataset/<environment>/<testmethodname>.csv folder
	 * CSV file MUST use ',' as separator. For (semicolon) ';', use datasetSemicolonWithHeader
	 * @param testMethod
	 * @return
	 * @throws IOException
	 */
    @DataProvider(name = "datasetWithHeader")
    public Object[][] datasetWithHeader(Method testMethod) throws IOException {
    	File dataset = getDatasetFile(testMethod);
    	if (dataset.getName().toLowerCase().endsWith("csv")) {
    		return CSVHelper.readWithHeader(getDatasetFile(testMethod), ",");
    	} else {
    		List<Map<String, String>> data = new ExcelHelper(dataset).readSheet(0, true);
    		return reformatData(data);
    	}
    }
    
    @DataProvider(name = "datasetSemicolon")
    public Object[][] datasetSemicolon(Method testMethod) throws IOException {	
    	return CSVHelper.read(getDatasetFile(testMethod), ";");
    }
    
    @DataProvider(name = "datasetSemicolonWithHeader")
    public Object[][] datasetSemicolonWithHeader(Method testMethod) throws IOException {
    	return CSVHelper.readWithHeader(getDatasetFile(testMethod), ";");
    }
}
