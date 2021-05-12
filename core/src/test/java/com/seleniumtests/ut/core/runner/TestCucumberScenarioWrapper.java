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

import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;

import gherkin.ast.Examples;
import gherkin.ast.Location;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;

public class TestCucumberScenarioWrapper extends MockitoTest {
	
	@Mock
	ScenarioDefinition cucumberScenario;
	
	@Mock
	ScenarioDefinition cucumberScenario2;
	
	@Mock
	ScenarioOutline cucumberScenarioWithExample;
	
	Pickle pickle = new Pickle("short", "en", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	PickleEvent pickleEvent = new PickleEvent("uri", pickle);
	
	Pickle pickleLong = new Pickle("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.", "en", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	PickleEvent pickleEventLong = new PickleEvent("uri", pickleLong);
	
	Pickle pickleLong2 = new Pickle("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.2", "en", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	PickleEvent pickleEventLong2 = new PickleEvent("uri", pickleLong2);
	
	@Mock
	Examples examples1;
	
	@Mock
	TableRow row1;
	
	@Mock
	TableRow row2;
	
//	@Mock
//	TagStatement gherkinModel2;
	
	@BeforeMethod(groups={"ut"})
	public void init() {
//		when(cucumberScenario.getGherkinModel()).thenReturn(gherkinModel);
//		when(cucumberScenario2.getGherkinModel()).thenReturn(gherkinModel2);
//		when(cucumberScenario.getVisualName()).thenReturn("| foo | bar |");
		
		
		when(cucumberScenarioWithExample.getExamples()).thenReturn(Arrays.asList(examples1));
		when(examples1.getTableBody()).thenReturn(Arrays.asList(row1, row2));
		when(row1.getLocation()).thenReturn(new Location(5, 5));
		when(row2.getLocation()).thenReturn(new Location(6, 5));
		when(row1.getCells()).thenReturn(Arrays.asList(new TableCell(new Location(5, 7), "foo"), new TableCell(new Location(5, 12), "foo2")));
		when(row2.getCells()).thenReturn(Arrays.asList(new TableCell(new Location(6, 7), "bar"), new TableCell(new Location(6, 12), "bar2")));
		
	}

	@Test(groups={"ut"})
	public void testScenarioToStringNoStripShort(ITestContext testNGCtx) {
		when(cucumberScenario.getName()).thenReturn("short");
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleEvent, cucumberScenario).toString(-1), "short");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripShort(ITestContext testNGCtx) {
		when(cucumberScenario.getName()).thenReturn("short");
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleEvent, cucumberScenario).toString(10), "short");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringNoStripLong(ITestContext testNGCtx) {
		when(cucumberScenario.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleEventLong, cucumberScenario).toString(-1), "a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripLong(ITestContext testNGCtx) {
		when(cucumberScenario.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleEventLong, cucumberScenario).toString(150), "a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too lo");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripDefaultLong(ITestContext testNGCtx) {
		when(cucumberScenario.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleEventLong, cucumberScenario).toString(), "a very long scenario outline name with some special characters like @ and |. But we should not ");
	}
	
	/**
	 * With placeholder on scenario outline, we use the pickle name as placeholders have been replaced
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioToStringShortPlaceholder(ITestContext testNGCtx) {
		when(cucumberScenarioWithExample.getName()).thenReturn("short <foo>");
		Pickle pickle2 = new Pickle("short foobar", "en", 
				new ArrayList<>(), 
				new ArrayList<>(), 
				Arrays.asList(new PickleLocation(2, 2), // location for scenario outline
				new PickleLocation(6, 5)));
		PickleEvent pickleEvent2 = new PickleEvent("uri", pickle2);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleEvent2, cucumberScenarioWithExample).toString(150), "short foobar");
	}

	/**
	 * Without placeholder on scenario outline, we use the scenario outline description plus example value
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioToStringShortNoPlaceholder(ITestContext testNGCtx) {
		when(cucumberScenarioWithExample.getName()).thenReturn("short");
		
		Pickle pickle2 = new Pickle("short", "en", 
				new ArrayList<>(), 
				new ArrayList<>(), 
				Arrays.asList(new PickleLocation(2, 2), // location for scenario outline
						new PickleLocation(6, 5))); // location for example
		PickleEvent pickleEvent2 = new PickleEvent("uri", pickle2);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleEvent2, cucumberScenarioWithExample).toString(-1), "short-| bar | bar2 |");
	}
	

	/**
	 * Check the unstripped version is used to compare 2 cucumberscenariowrappers
	 * @param testNGCtx
	 */
	@Test(groups={"ut"})
	public void testScenarioEqualLong(ITestContext testNGCtx) {
		when(cucumberScenario.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		when(cucumberScenario2.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.2");
		
		Assert.assertNotEquals(new CucumberScenarioWrapper(pickleEventLong, cucumberScenario), new CucumberScenarioWrapper(pickleEventLong2,  cucumberScenario2));
	}
	
}
