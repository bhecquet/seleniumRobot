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
