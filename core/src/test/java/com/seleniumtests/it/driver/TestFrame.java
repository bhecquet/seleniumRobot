/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

/**
 * Checks that it's possible to interact with elements in iframe
 * @author behe
 *
 */
public class TestFrame extends GenericMultiBrowserTest {

	public TestFrame(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestFrame(BrowserType browserType) throws Exception {
		super(browserType); 
	}
	
	public TestFrame() throws Exception {
//		super(BrowserType.FIREFOX); 
		super(null);
	}
	
	

	@Test(groups={"it"})
	public void testFrameText() {
		Assert.assertEquals(testPage.textElementIFrame.getValue(), "a value");
	}
	
	@Test(groups={"it"})
	public void testFrameRadio() {
		Assert.assertEquals(testPage.radioElementIFrame.getAttribute("name"), "radio");
	}
	
	@Test(groups={"it"})
	public void testFrameCheckbox() {
		Assert.assertEquals(testPage.checkElementIFrame.getAttribute("type"), "checkbox");
	}
	
	@Test(groups={"it"})
	public void testFrameButton() {
		testPage.buttonIFrame.click();
		Assert.assertEquals(testPage.buttonIFrame.getText(), "A button");
	}
	
	@Test(groups={"it"})
	public void testFrameLabel() {
		Assert.assertEquals(testPage.labelIFrame.getText(), "A label");
	}
	
	@Test(groups={"it"})
	public void testFrameLink() {
		Assert.assertTrue(testPage.linkIFrame.getUrl().startsWith("http://www.google.fr"));
	}
	
	@Test(groups={"it"})
	public void testFrameSelect() {
		Assert.assertEquals(testPage.selectListIFrame.getOptions().size(), 1);
	}
	
	@Test(groups={"it"})
	public void testElementInsideOtherElementWithFrame() {
		Assert.assertEquals(testPage.optionOfSelectListIFrame.getText(), "option1 frame");
	}
	
	@Test(groups={"it"})
	public void testFrameTable() {
		Assert.assertEquals(testPage.tableIFrame.getRowCount(), 2);
	}
	
	@Test(groups={"it"})
	public void testIsElementPresentInFrame() {
		Assert.assertTrue(testPage.tableIFrame.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testFindElements() {
		Assert.assertEquals(testPage.rows.findElements().size(), 2);
	}
	
	@Test(groups={"it"})
	public void testFindElementsBy() {
		Assert.assertEquals(testPage.tableIFrame.findElements(By.tagName("tr")).size(), 2);
	}
	
	@Test(groups={"it"})
	public void testFrameInFrameText() {
		Assert.assertEquals(testPage.textElementSubIFrame.getValue(), "an other value in iframe");
	}
	
	/**
	 * test that when working with an element inside frame, we automatically go back to default content before next element action
	 */
	@Test(groups={"it"})
	public void testBackToMainFrame() {
		System.out.println(testPage.labelIFrame.getText());
		try {
			testPage.textElement.sendKeys("youpi");
			Assert.assertEquals(testPage.textElement.getValue(), "youpi");
		} finally {
			testPage.resetButton.click();
		}
		
	}
	
 	
}
