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
package com.infotel.seleniumRobot.jpetstore.webpage;

import org.openqa.selenium.By;

import com.infotel.seleniumRobot.jpetstore.webpage.catalog.fish.FishList;
import com.infotel.seleniumRobot.jpetstore.webpage.rest.LanguageList;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;

import cucumber.api.java.en.When;

public class JPetStoreHome extends HeaderAndFooter {
	
	private static final HtmlElement fishMenu = new HtmlElement("Enter", By.id("SidebarContent")).findElement(By.tagName("a"), 0);
	private static final HtmlElement sidebar = new HtmlElement("sidebar", By.id("SidebarContent"));
	private static final LinkElement sonarLanguages = new LinkElement("sonar languages", By.linkText("Sonar list langage"));
	private static final LinkElement mockSonarLanguages = new LinkElement("mocked sonar languages", By.linkText("Mocked Sonar list langage"));
	
	
	public JPetStoreHome() throws Exception {
		super(sidebar);
	}
	
	public JPetStoreHome(boolean openPageURL) throws Exception {
		super(sidebar, openPageURL ? param("appURL") : null);
	}
	
	/**
	 * Go to fish page
	 * @return
	 * @throws Exception
	 */
	@When("^Cliquer sur le lien 'FISH'$")
	public FishList goToFish() throws Exception {
		fishMenu.click();
		return new FishList();
	}
	
	/**
	 * Get list of languages
	 * @return
	 * @throws Exception
	 */
	public LanguageList goToSonarLanguageList() throws Exception {
		sonarLanguages.click();
		return new LanguageList();
	}
	
	/*
	 * Get mock list
	 */
	public LanguageList goToMockedSonarLanguageList() throws Exception {
		mockSonarLanguages.click();
		return new LanguageList();
	}
	
	public FishList goToFishFromHeader(String param) throws Exception {
		fishMenu.click();
		return new FishList();
	}
}
