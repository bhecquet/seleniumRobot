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
package com.seleniumtests.it.reporter;

import static org.mockito.Mockito.spy;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.PerformanceReporter;

public class TestJsonReporter extends ReporterTest {
	
	private PerformanceReporter reporter;

	
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
	
	@SuppressWarnings("unchecked")
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {
		
		reporter = spy(new PerformanceReporter());

		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass"});
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();

		// check all files are generated with the right name
		Assert.assertTrue(Paths.get(outDir, "results.json").toFile().exists());
	}
	
	@SuppressWarnings("unchecked")
	@Test(groups={"it"})
	public void testReportContent(ITestContext testContext) throws Exception {
		
		reporter = spy(new PerformanceReporter());
		
		executeSubTest(new String[] {"com.seleniumtests.it.reporter.StubTestClass", "com.seleniumtests.it.reporter.StubTestClass2"});
		String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
		
		JSONObject jsonResult = new JSONObject(FileUtils.readFileToString(Paths.get(outDir, "results.json").toFile()));
		
		// Check content of result file
		Assert.assertEquals(jsonResult.getInt("pass"), 3);
		Assert.assertEquals(jsonResult.getInt("fail"), 4);
		Assert.assertEquals(jsonResult.getInt("skip"), 2);
		Assert.assertEquals(jsonResult.getInt("total"), 9);
		
	}

}
