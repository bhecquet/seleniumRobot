/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.webelements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.uipage.htmlelements.ElementInfo;

public class TestElementInfo extends GenericMultiBrowserTest {

	
	public TestElementInfo() {
		super(BrowserType.CHROME, "DriverTestPage");
	}

	
	@BeforeMethod(groups={"it"})
	public void init() {
		// delete all previously created information
		ElementInfo.purgeAll();
		SeleniumTestsContextManager.getThreadContext().setAdvancedElementSearch("full");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
	}
	
	/**
	 * Test the info file is created when element is found
	 */
	@Test(groups={"it"})
	public void testNewElementInfo() {
		try {
			DriverTestPage.textElement.sendKeys("youpi");
		} finally {
			DriverTestPage.resetButton.click();
		}
		
		// check file has been created
		File elementInfoPath = ElementInfo.buildElementInfoPath(DriverTestPage.textElement);
		Assert.assertTrue(elementInfoPath.isFile());
		ElementInfo elInfo = ElementInfo.readFromJsonFile(elementInfoPath);
		Assert.assertEquals(elInfo.getTotalSearch(), 1); // element only searched once
		Assert.assertEquals(elInfo.getAttributes().size(), 2);	// check some element information have been retrieved. We do not check all as it's already done in unit tests
	}
	
	/**
	 * Element info is not recreated, but updated when element has already been successfully searched
	 */
	@Test(groups={"it"})
	public void testElementInfoAlreadyExists() {
		
		// element information will be created with the following command
		DriverTestPage.textElement.getTagName();
		File elementInfoPath = ElementInfo.buildElementInfoPath(DriverTestPage.textElement);
		ElementInfo elInfo = ElementInfo.readFromJsonFile(elementInfoPath);
		Assert.assertEquals(elInfo.getTotalSearch(), 1); // element only searched once
		
		DriverTestPage.textElement.getTagName();
		ElementInfo newElInfo = ElementInfo.readFromJsonFile(elementInfoPath);
		Assert.assertEquals(newElInfo.getTotalSearch(), 2); // test search done twice
		Assert.assertEquals(newElInfo.getTagStability(), 1); // test search done twice
	}
	
	/**
	 * When no label is provided, there should be no error but info file is not created
	 */
	@Test(groups={"it"})
	public void testNoLabel() {
		DriverTestPage.textSelectedId.getTagName();
		File elementInfoPath = ElementInfo.buildElementInfoPath(DriverTestPage.textElement);
		Assert.assertFalse(elementInfoPath.isFile());
	}
	
	/**
	 * Info file recreated when search criteria has been modified
	 */
	@Test(groups={"it"})
	public void testElementModified() {
		// element information will be created with the following command
		DriverTestPage.textElement.getTagName();
		File elementInfoPath = ElementInfo.buildElementInfoPath(DriverTestPage.textElement);
		ElementInfo elInfo = ElementInfo.readFromJsonFile(elementInfoPath);
		Assert.assertEquals(elInfo.getTotalSearch(), 1); // element only searched once
		
		// change locator and check file has been regenerated (totalsearch == 1)
		try {
			DriverTestPage.textElement.setBy(By.name("textField"));
			DriverTestPage.textElement.getTagName();
			ElementInfo newElInfo = ElementInfo.readFromJsonFile(elementInfoPath);
			Assert.assertEquals(newElInfo.getTotalSearch(), 1); // test search done once because locator has been changed
			Assert.assertEquals(newElInfo.getTagStability(), 0); // test search done twice
		} finally {
			DriverTestPage.textElement.setBy(By.id("text2"));
		}
	}
	
	/**
	 * Check scrolling is done to take screenshot
	 * No error should be raised here, meaning that the capture has correctly been done
	 * Check a screenshot has been done
	 */
	@Test(groups={"it"})
	public void testScrollingForScreenshot() {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
		DriverTestPage.scrollButton.click();
		File elementInfoPath = ElementInfo.buildElementInfoPath(DriverTestPage.scrollButton);
		ElementInfo elInfo = ElementInfo.readFromJsonFile(elementInfoPath);
        Assert.assertFalse(elInfo.getB64Image().isEmpty());
	}
	
	/**
	 * With selenium native code, event with overriding, no ElementInfo should be created because we have no label and no way to create a unique one
	 */
	@Test(groups={"it"})
	public void testWithSeleniumNativeSearch() throws IOException {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
		driver.findElement(By.id("text2"));
		List<Path> infoFilePaths = Files.walk(ElementInfo.getElementInfoLocation())
									.filter(Files::isRegularFile)
									.toList();
		Assert.assertEquals(infoFilePaths.size(), 0);
	}
	
	
 
}
