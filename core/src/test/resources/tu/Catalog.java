package com.infotel.seleniumRobot.jpetstore.cucumber;

import org.testng.Assert;

import com.infotel.seleniumRobot.jpetstore.webpage.JPetStoreHome;
import com.infotel.seleniumRobot.jpetstore.webpage.catalog.ProductItem;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class Catalog {
	
    
    @Given("Ouvrir le jPetStore")
    public void openSite() throws Exception {
    	new JPetStoreHome(true);
    }

	@Then("^Le nom du produit est '(.*?)'$")
	public void testProductName(String expectedProductName) throws Exception {
		Assert.assertEquals(new ProductItem().getProductDetails().name, expectedProductName);
	}
}
