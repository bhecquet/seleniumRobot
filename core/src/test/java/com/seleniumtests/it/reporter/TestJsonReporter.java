/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.reporter;

import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.reporters.CustomReporter;

/**
 * Test that default reporting contains results.json file (CustomReporter.java) with default summary reports defined in SeleniumTestsContext.DEFAULT_CUSTOM_SUMMARY_REPORTS
 * @author s047432
 *
 */
public class TestJsonReporter extends ReporterTest {
	
	private CustomReporter reporter;

	
	@AfterMethod(groups={"it"})
	private void deleteGeneratedFiles() {
		if (reporter == null) {
			return;
		}
		File outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		for (File file: outDir.listFiles()) {
			if ("results.json".equals(file.getName())) {
				file.delete();
			}
		}
		
	}
	
	@Test(groups={"it"})
	public void testReportGeneration() {
		
		reporter = spy(new CustomReporter());

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.METHODS, new String[] {"testAndSubActions", "testInError", "testWithException"});
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();

		// check all files are generated with the right name
		Assert.assertTrue(Paths.get(outDir, "results.json").toFile().exists());
	}
	
	@Test(groups={"it"})
	public void testReportContent() throws Exception {
		
		reporter = spy(new CustomReporter());
		
		executeSubTest(new String[] {"com.seleniumtests.it.stubclasses.StubTestClass", "com.seleniumtests.it.stubclasses.StubTestClass2"});
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));
		
		// Check content of result file
		Assert.assertEquals(jsonResult.getInt("pass"), 36);
		Assert.assertEquals(jsonResult.getInt("fail"), 10);
		Assert.assertEquals(jsonResult.getInt("skip"), 5);
		Assert.assertEquals(jsonResult.getInt("total"), 51);
		
	}

	@Test(groups={"it"})
	public void testReportContainsDriverInformation() throws IOException {

		reporter = spy(new CustomReporter());

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverShort"});
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();

		JSONObject json = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile(), StandardCharsets.UTF_8));

		Assert.assertEquals(json.getJSONArray("drivers").length(), 1);
		Assert.assertEquals(json.getJSONArray("drivers").getJSONObject(0).getString("browserName"), "chrome");
		Assert.assertTrue(json.getJSONArray("drivers").getJSONObject(0).getInt("duration") > 0);
		Assert.assertTrue(json.getJSONArray("drivers").getJSONObject(0).getInt("startupDuration") > 0);
		Assert.assertNotEquals(json.getJSONArray("drivers").getJSONObject(0).getString("browserVersion"), "N/A");
		Assert.assertEquals(json.getJSONArray("drivers").getJSONObject(0).getString("browserName"), "chrome");
		Assert.assertEquals(json.getJSONArray("drivers").getJSONObject(0).getString("testName"), "testDriverShort");
	}


}
