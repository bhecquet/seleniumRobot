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
package com.seleniumtests.it.reporter;

import static org.mockito.Mockito.spy;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.reporters.CustomReporter;

/**
 * Test that reporter plugins are used
 * @author s047432
 *
 */
public class TestReporterPlugin extends ReporterTest {
	
	@AfterMethod(groups={"it"})
	private void deleteGeneratedFiles() {
		File outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		for (File file: outDir.listFiles()) {
			file.delete();
		}
	}
	
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {
	
		try {
			System.setProperty(SeleniumTestsContext.REPORTER_PLUGIN_CLASSES, "com.seleniumtests.it.reporter.CustomReportPlugin");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClass"}, ParallelMode.NONE, new String[] {"testAndSubActions"});
			String outDir = new File(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory()).getAbsolutePath();
	
			// check all files are generated with the right name
			Assert.assertTrue(Paths.get(outDir, "customReport.txt").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.REPORTER_PLUGIN_CLASSES);
		}
	}
}
