/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.infotel.seleniumRobot.jpetstore.tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.infotel.seleniumRobot.jpetstore.webpage.JPetStoreHome;
import com.seleniumtests.core.runner.SeleniumTestPlan;

public class IntegrationTests extends SeleniumTestPlan {

	@Test(
	        groups = { "it" },
	        description = "check it's possible to add an animal to cart"
	    )
	public void addAnimalToCart() throws Exception {
		new JPetStoreHome(true)
			.goToFish()
			.accessAngelFish()
			.addToCart(0);
	}
	
	@Test(
	        groups = { "it" },
	        description = "Check mock access"
	    )
	public void showBirdListFromMock() throws Exception {
		List<String> langs = new JPetStoreHome(true)
			.goToMockedSonarLanguageList()
			.getLanguageList();
		
		Assert.assertTrue(langs.contains("Pionus maximilien"));
		Assert.assertEquals(langs.size(), 8);
	}
	
	@Test(
			groups = { "it" },
			description = "Check sonar access"
			)
	public void showLanguageListFromSonar() throws Exception {
		List<String> langs = new JPetStoreHome(true)
				.goToSonarLanguageList()
				.getLanguageList();
		
		Assert.assertTrue(langs.contains("Java"));
	}
}
