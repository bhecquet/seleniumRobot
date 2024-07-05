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
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.SeleniumTestsContextManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

	private File getDatasetFile(InputStream inputStream, String extension) throws IOException {
		File tempFile = File.createTempFile("dataset", extension);
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(inputStream, tempFile);
		return tempFile;
	}

	private File getDatasetFile(Method testMethod) throws IOException {
		String datasetPath = SeleniumTestsContextManager.getDatasetPath();

		if (datasetPath.startsWith("classpath:")) {
			String csvResourceName = String.format("%s/%s/%s.csv",
					datasetPath.replace("classpath:", ""),
					robotConfig().getTestEnv(),
					testMethod.getName());
			InputStream csvResource = Thread.currentThread().getContextClassLoader().getResourceAsStream(csvResourceName);
			String xlsxResourceName = String.format("%s/%s/%s.xlsx",
					datasetPath.replace("classpath:", ""),
					robotConfig().getTestEnv(),
					testMethod.getName());
			InputStream xlsxResource = Thread.currentThread().getContextClassLoader().getResourceAsStream(xlsxResourceName);

			if (csvResource != null) {
				return getDatasetFile(csvResource, ".csv");
			} else if (xlsxResource != null) {
				return getDatasetFile(xlsxResource, ".xlsx");
			} else {
				throw new ConfigurationException(String.format("Dataset file %s or %s does not exist in resource", csvResourceName, xlsxResourceName));
			}

		} else {

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
	}
	
	/**
	 * This data provider can be used to read data from a CSV/XLSX file (NO HEADER) in /data/<app>/dataset/<environment>/<testmethodname>.csv folder
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
	 * This data provider can be used to read data from a CSV/XLSX file (WITH HEADER) in /data/<app>/dataset/<environment>/<testmethodname>.csv folder
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
    
    /**
	 * This data provider can be used to read data from a CSV file (NO HEADER) in /data/<app>/dataset/<environment>/<testmethodname>.csv folder
	 * CSV file MUST use ';' as separator. For (comma) ',', use 'dataset'
	 * @param testMethod
	 * @return
	 * @throws IOException
	 */
    @DataProvider(name = "datasetSemicolon")
    public Object[][] datasetSemicolon(Method testMethod) throws IOException {	
    	return CSVHelper.read(getDatasetFile(testMethod), ";");
    }
    
    
    /**
	 * This data provider can be used to read data from a CSV file (WITH HEADER) in /data/<app>/dataset/<environment>/<testmethodname>.csv folder
	 * CSV file MUST use ';' as separator. For (comma) ',', use 'datasetWithHeader'
	 * @param testMethod
	 * @return
	 * @throws IOException
	 */
    @DataProvider(name = "datasetSemicolonWithHeader")
    public Object[][] datasetSemicolonWithHeader(Method testMethod) throws IOException {
    	return CSVHelper.readWithHeader(getDatasetFile(testMethod), ";");
    }
}
