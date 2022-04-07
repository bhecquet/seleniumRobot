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
package com.seleniumtests.ut.core.runner;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.runner.cucumber.CustomCucumberPropertiesProvider;

import io.cucumber.testng.CucumberPropertiesProvider;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;

public class TestCustomCucumberPropertiesProvider extends GenericTest {
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		System.setProperty("cucumberPackage", "com.seleniumtests.core.config");
	}

	@Test(groups={"ut"})
	public void testSingle(ITestContext testNGCtx) throws URISyntaxException {
		try {
			System.setProperty("cucumberTags", "@new");
			initThreadContext(testNGCtx);
			
		    CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(testNGCtx.getCurrentXmlTest());
		    TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass(), properties);
			Object[][] scenarios = runner.provideScenarios();
			Assert.assertEquals(scenarios.length, 1);
			Assert.assertEquals(((PickleWrapper)scenarios[0][0]).getPickle().getName(), "core_3");
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
	
	@Test(groups={"ut"})
	public void testAnd(ITestContext testNGCtx) throws URISyntaxException {
		try {
			System.setProperty("cucumberTags", "@new4 AND @new5");
			initThreadContext(testNGCtx);
			
			CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(testNGCtx.getCurrentXmlTest());
		    TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass(), properties);
			Object[][] scenarios = runner.provideScenarios();
			Assert.assertEquals(scenarios.length, 1);
			Assert.assertEquals(((PickleWrapper)scenarios[0][0]).getPickle().getName(), "core_6");
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
	
	@Test(groups={"ut"})
	public void testOr(ITestContext testNGCtx) throws URISyntaxException {
		try {
			System.setProperty("cucumberTags", "@new or @new2");
			initThreadContext(testNGCtx);
			
			CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(testNGCtx.getCurrentXmlTest());
		    TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass(), properties);
			Object[][] scenarios = runner.provideScenarios();
			Assert.assertEquals(scenarios.length, 2);
			Assert.assertEquals(((PickleWrapper)scenarios[0][0]).getPickle().getName(), "core_3");
			Assert.assertEquals(((PickleWrapper)scenarios[1][0]).getPickle().getName(),  "core_4");
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
	
	@Test(groups={"ut"})
	public void testAndOr(ITestContext testNGCtx) throws URISyntaxException {
		try {
			System.setProperty("cucumberTags", "@core AND (@new2 or @new)");
			initThreadContext(testNGCtx);
			CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(testNGCtx.getCurrentXmlTest());
		    TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass(), properties);
			Object[][] scenarios = runner.provideScenarios();
			Assert.assertEquals(scenarios.length, 2);
			Assert.assertEquals(((PickleWrapper)scenarios[0][0]).getPickle().getName(), "core_3");
			Assert.assertEquals(((PickleWrapper)scenarios[1][0]).getPickle().getName(),  "core_4");
		} finally {
			System.clearProperty("cucumberTags");
		}
	}
	
	/**
	 * Check that we can get test from scenario outlines with matching text
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioOutlineMatching(ITestContext testNGCtx) throws URISyntaxException {
		try {
			System.setProperty("cucumberTests", "core_ .*");
			initThreadContext(testNGCtx);
			CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(testNGCtx.getCurrentXmlTest());
		    TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass(), properties);
			Object[][] scenarios = runner.provideScenarios();
			Assert.assertEquals(scenarios.length, 2);
			Assert.assertEquals(((PickleWrapper)scenarios[0][0]).getPickle().getName(), "core_ tata");
			Assert.assertEquals(((PickleWrapper)scenarios[1][0]).getPickle().getName(), "core_ titi");
		} finally {
			System.clearProperty("cucumberTests");
		}
	}
	
	/**
	 * Test the cucumber options cucumber.filter.name taken from Test NG XML file
	 * Cucmber will take it's options 
	 * - from TestNG file (read parameters)
	 * - from System properties automatically
	 * 
	 * 
	 * @param testNGCtx
	 * @throws URISyntaxException
	 */
	@Test(groups={"ut"})
	public void testScenarioOutlineMatchingWithCucumberOptions(ITestContext testNGCtx) throws URISyntaxException {

		try {
			testNGCtx.getCurrentXmlTest().addParameter("cucumber.filter.name", "core_ .*");
			
			initThreadContext(testNGCtx);
			CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(testNGCtx.getCurrentXmlTest());
		    TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass(), properties);
			Object[][] scenarios = runner.provideScenarios();
			Assert.assertEquals(scenarios.length, 2);
			Assert.assertEquals(((PickleWrapper)scenarios[0][0]).getPickle().getName(), "core_ tata");
			Assert.assertEquals(((PickleWrapper)scenarios[1][0]).getPickle().getName(), "core_ titi");
		} finally {
			testNGCtx.getCurrentXmlTest().setParameters(new HashMap<>());
		}
	}
	
	/**
	 * Check that we can get test from scenario 
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioExactText(ITestContext testNGCtx) throws URISyntaxException {
		try {
			System.setProperty("cucumberTests", "core_3,core_4");
			initThreadContext(testNGCtx);
			CucumberPropertiesProvider properties = new CustomCucumberPropertiesProvider(testNGCtx.getCurrentXmlTest());
		    TestNGCucumberRunner runner = new TestNGCucumberRunner(this.getClass(), properties);
			Object[][] scenarios = runner.provideScenarios();
			Assert.assertEquals(scenarios.length, 2);
			Assert.assertEquals(((PickleWrapper)scenarios[0][0]).getPickle().getName(), "core_3");
			Assert.assertEquals(((PickleWrapper)scenarios[1][0]).getPickle().getName(), "core_4");
		} finally {
			System.clearProperty("cucumberTests");
		}
	}

}
