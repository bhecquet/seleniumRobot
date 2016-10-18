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

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.IReporter;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.JMeterReporter;

public class TestJMeterReporter extends MockitoTest {
	
	private JMeterReporter reporter;

	/**
	 * Execute stub tests using TestNG runner and make SeleniumTestsReporter a listener so that 
	 * a report is generated
	 */
	private XmlSuite executeSubTest() {
		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		 
		XmlTest test = new XmlTest(suite);
		test.setName("FirstTest");
		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(new XmlClass("com.seleniumtests.it.reporter.StubTestClass"));
		test.setXmlClasses(classes) ;
		
		XmlTest test2 = new XmlTest(suite);
		test2.setName("SecondTest");
		List<XmlClass> classes2 = new ArrayList<XmlClass>();
		classes2.add(new XmlClass("com.seleniumtests.it.reporter.StubTestClass2"));
		test2.setXmlClasses(classes2) ;
		
		List<XmlSuite> suites = new ArrayList<XmlSuite>();
		suites.add(suite);
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.addListener((IReporter)reporter);
		tng.setOutputDirectory(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory());
		tng.run(); 
		
		return suite;
	}
	
	@Test(groups={"it"})
	public void testReportGeneration(ITestContext testContext) throws Exception {
		
		/*reporter = spy(new JMeterReporter());

		XmlSuite suite = executeSubTest();
		
		Assert.assertTrue(reporter.getGeneratedFiles().contains("TEST-StubTestClass.test1.xml"));
		Assert.assertTrue(reporter.getGeneratedFiles().contains("TEST-StubTestClass.testInError.xml"));
		Assert.assertTrue(reporter.getGeneratedFiles().contains("TEST-StubTestClass2.test1.xml"));
		
		// check file content
		String fileContent = FileUtils.readFileToString(new File("TEST-StubTestClass.test1.xml"));
		Assert.assertTrue(fileContent.contains("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
		Assert.assertTrue(fileContent.contains("<testsuite errors="));
		Assert.assertTrue(fileContent.contains("<testcase classname=\""));
		
		// check at least one generation occured for each part of the report
		verify(reporter).generateReport(anyList(), anyList(), anyString()); // 1 time only
		*/
	}
}
