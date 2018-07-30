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
