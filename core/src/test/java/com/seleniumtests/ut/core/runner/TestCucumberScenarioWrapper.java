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

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;

import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.model.TagStatement;

public class TestCucumberScenarioWrapper extends MockitoTest {
	
	@Mock
	CucumberScenario cucumberScenario;
	
	@Mock
	TagStatement gherkinModel;
	
	@Mock
	CucumberScenario cucumberScenario2;
	
	@Mock
	TagStatement gherkinModel2;
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		when(cucumberScenario.getGherkinModel()).thenReturn(gherkinModel);
		when(cucumberScenario2.getGherkinModel()).thenReturn(gherkinModel2);
		when(cucumberScenario.getVisualName()).thenReturn("| foo | bar |");
	}

	@Test(groups={"ut"})
	public void testScenarioToStringNoStripShort(ITestContext testNGCtx) {
		when(gherkinModel.getName()).thenReturn("short");
		
		Assert.assertEquals(new CucumberScenarioWrapper(cucumberScenario).toString(-1), "short");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripShort(ITestContext testNGCtx) {
		when(gherkinModel.getName()).thenReturn("short");
		
		Assert.assertEquals(new CucumberScenarioWrapper(cucumberScenario).toString(-1), "short");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringNoStripLong(ITestContext testNGCtx) {
		when(gherkinModel.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		
		Assert.assertEquals(new CucumberScenarioWrapper(cucumberScenario).toString(-1), "a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripLong(ITestContext testNGCtx) {
		when(gherkinModel.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		
		Assert.assertEquals(new CucumberScenarioWrapper(cucumberScenario).toString(150), "a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too lo");
	}
	
	/**
	 * With placeholder on scenario outline, we use the cucumber scenario name as placeholders have been replaced
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioToStringShortPlaceholder(ITestContext testNGCtx) {
		when(gherkinModel.getName()).thenReturn("short");
		
		Assert.assertEquals(new CucumberScenarioWrapper(cucumberScenario, "short <foo>").toString(150), "short");
	}

	/**
	 * Without placeholder on scenario outline, we use the scenario outline description plus example value
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioToStringShortNoPlaceholder(ITestContext testNGCtx) {
		when(gherkinModel.getName()).thenReturn("short");
		
		Assert.assertEquals(new CucumberScenarioWrapper(cucumberScenario, "short").toString(-1), "short-| foo | bar |");
	}
	

	/**
	 * Check the unstripped version is used
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioEqualLong(ITestContext testNGCtx) {
		when(gherkinModel.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		when(gherkinModel2.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.2");
		
		Assert.assertNotEquals(new CucumberScenarioWrapper(cucumberScenario), new CucumberScenarioWrapper(cucumberScenario2));
	}
	
}
