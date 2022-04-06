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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.CucumberScenarioWrapper;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;

@PrepareForTest({Pickle.class})
public class TestCucumberScenarioWrapper extends MockitoTest {
	
	@Mock
	PickleWrapper pickleWrapper;
	
	@Mock
	PickleWrapper pickleWrapper2;
	
	@Mock
	FeatureWrapper featureWrapper;

	@Mock
	Pickle pickle;
	
	@Mock
	Pickle pickle2;

	
	@BeforeMethod(groups={"ut"})
	public void init() throws URISyntaxException {

		when(pickleWrapper.getPickle()).thenReturn(pickle);
		when(pickleWrapper2.getPickle()).thenReturn(pickle2);
		
		when(pickle.getUri()).thenReturn(new URI("file:///" + Paths.get(SeleniumTestsContextManager.getFeaturePath(), "Core.feature").toString().replace("\\", "/")));
		when(pickle2.getUri()).thenReturn(new URI("file:///" + Paths.get(SeleniumTestsContextManager.getFeaturePath(), "Core.feature").toString().replace("\\", "/")));

	}

	@Test(groups={"ut"})
	public void testScenarioToStringNoStripShort(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("core_3");
		when(pickle.getScenarioLine()).thenReturn(33);
		when(pickle.getLine()).thenReturn(33);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper).toString(-1), "core_3");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripShort(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("core_3");
		when(pickle.getScenarioLine()).thenReturn(33);
		when(pickle.getLine()).thenReturn(33);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper).toString(10), "core_3");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringNoStripLong(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		when(pickle.getScenarioLine()).thenReturn(33);
		when(pickle.getLine()).thenReturn(33);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper).toString(-1), "a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripLong(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		when(pickle.getScenarioLine()).thenReturn(33);
		when(pickle.getLine()).thenReturn(33);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper).toString(150), "a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too lo");
	}
	
	@Test(groups={"ut"})
	public void testScenarioToStringStripDefaultLong(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		when(pickle.getScenarioLine()).thenReturn(33);
		when(pickle.getLine()).thenReturn(33);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper).toString(), "a very long scenario outline name with some special characters like @ and |. But we should not ");
	}
	
	/**
	 * With placeholder on scenario outline, we use the pickle name as placeholders have been replaced
	 * @param testNGCtx
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testScenarioToStringShortPlaceholder(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("core_ tata");
		when(pickle.getScenarioLine()).thenReturn(4);
		when(pickle.getLine()).thenReturn(10);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper).toString(150), "core_ tata");
	}

	/**
	 * Without placeholder on scenario outline, we use the scenario outline description plus example value
	 * @param testNGCtx
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testScenarioToStringShortNoPlaceholder(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("core_unique_name");
		when(pickle.getScenarioLine()).thenReturn(14);
		when(pickle.getLine()).thenReturn(20);
		
		Assert.assertEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper).toString(-1), "core_unique_name-| tata |");
	}
	

	/**
	 * Check the unstripped version is used to compare 2 cucumberscenariowrappers
	 * @param testNGCtx
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void testScenarioEqualLong(ITestContext testNGCtx) throws IOException {
		when(pickle.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.");
		when(pickle.getScenarioLine()).thenReturn(14);
		when(pickle.getLine()).thenReturn(20);
		when(pickle2.getName()).thenReturn("a very long scenario outline name with some special characters like @ and |. But we should not strip it, only display a message saying its much too long.2");
		when(pickle2.getScenarioLine()).thenReturn(14);
		when(pickle2.getLine()).thenReturn(20);
		
		Assert.assertNotEquals(new CucumberScenarioWrapper(pickleWrapper, featureWrapper), 
								new CucumberScenarioWrapper(pickleWrapper2,  featureWrapper));
	}
	
}
