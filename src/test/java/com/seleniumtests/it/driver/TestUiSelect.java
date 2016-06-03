/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.it.driver;

import com.seleniumtests.GenericTest;

public class TestUiSelect extends GenericTest {
	
//	private static ICustomWebDriver driver;
//	
//	@BeforeClass
//	public static void initDriver() throws Exception {
//		driver = Launcher.getInstance().getDriver();
//		driver.setTestAttachment(new TestAttachment());
//		driver.getTestAttachment().resetTestAttachments("unknown");
//		
//		try {
//			driver.manage().window().maximize();
//		} catch (Exception e) {}
//		driver.get("file:///" + Thread.currentThread().getContextClassLoader().getResource("test.html").getFile());
//	}
//	
//	@AfterClass
//	public static void closeBrowser() {
//		driver.close();
//		DriverDesktopLocator.getHostInstance().cleanBrowsers();
//		
//		// attente pour ne pas impacter le lancement d'un autre test unitaire
//		Tools.waitMs(2000);
//	}
//	
//	/**
//	 * Test le Select
//	 * - teste les multiples méthodes
//	 * TODO on ne teste pas le cas où le contenu du select est mis à jour suite à une communication réseau
//	 */
//	@Test  
//	public void testSelectByIndex() {
//		Select select = new Select(driver.findElement(By.id("select")));
//		select.selectByIndex(1);
//		Assert.assertTrue(select.getFirstSelectedOption().getText().equals("option2"));
//		Assert.assertTrue(driver.findElement(By.id("textSelectedId")).getAttribute("value").equals("1"));
//		Assert.assertTrue(driver.findElement(By.id("textSelectedValue")).getAttribute("value").equals("opt2"));
//		Assert.assertTrue(driver.findElement(By.id("textSelectedText")).getAttribute("value").equals("option2"));
//	}
//	
//	@Test  
//	public void testSelectByMatchingVisibleText() {
//		Select select = new Select(driver.findElement(By.id("select")));
//		select.selectByMatchingVisibleText("numero");
//		Assert.assertTrue(select.getFirstSelectedOption().getText().equals("option numero 3"));
//	}
//	
//	@Test  
//	public void testSelectByVisibleText() {
//		Select select = new Select(driver.findElement(By.id("select")));
//		select.selectByVisibleText("option2");
//		Assert.assertTrue(select.getFirstSelectedOption().getText().equals("option2"));
//	}
//	
//	@Test  
//	public void testSelectByValue() {
//		Select select = new Select(driver.findElement(By.id("select")));
//		select.selectByValue("opt3");
//		Assert.assertTrue(select.getFirstSelectedOption().getText().equals("option numero 3"));
//	}
//	
//	@Test  
//	public void testOptions() {
//		Select select = new Select(driver.findElement(By.id("select")));
//		Assert.assertTrue(select.getOptions().size() == 3);
//		Assert.assertTrue(select.getAllSelectedOptions().size() == 1);
//		Assert.assertFalse(select.isMultiple());
//	}
//	
//	/**
//	 * Le select disparait après sélection, on ne doit pas planter
//	 */
//	@Test  
//	public void testSelectHiddenAfterChoice() {
//		Select select = new Select(driver.findElement(By.id("select")));
//		select.selectByIndex(1);
//	}
//	

}
