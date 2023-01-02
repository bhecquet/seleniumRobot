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
package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.testng.Assert;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

/**
 * Checks that it's possible to interact with elements in iframe
 * @author behe
 *
 */
public class TestFrame extends GenericMultiBrowserTest {

	public TestFrame(BrowserType browserType) throws Exception {
		super(browserType, "DriverTestPage"); 
	}
	
	public TestFrame() throws Exception {
//		super(BrowserType.FIREFOX); 
		super(null, "DriverTestPage");
	}
	
	

	
	public void testFrameText() {
		Assert.assertEquals(DriverTestPage.textElementIFrame.getValue(), "a value");
	}
	
	
	public void testFrameRadio() {
		Assert.assertEquals(DriverTestPage.radioElementIFrame.getDomAttribute("name"), "radio");
	}
	
	
	public void testFrameCheckbox() {
		Assert.assertEquals(DriverTestPage.checkElementIFrame.getDomAttribute("type"), "checkbox");
	}
	
	
	public void testFrameButton() {
		DriverTestPage.buttonIFrame.click();
		Assert.assertEquals(DriverTestPage.buttonIFrame.getText(), "A button");
	}
	
	
	public void testFrameLabel() {
		Assert.assertEquals(DriverTestPage.labelIFrame.getText(), "A label");
	}
	
	
	public void testFrameLink() {
		Assert.assertTrue(DriverTestPage.linkIFrame.getUrl().startsWith("http://www.google.fr"));
	}
	
	
	public void testFrameSelect() {
		Assert.assertEquals(DriverTestPage.selectListIFrame.getOptions().size(), 1);
	}
	
	
	public void testElementInsideOtherElementWithFrame() {
		Assert.assertEquals(DriverTestPage.optionOfSelectListIFrame.getText(), "option1 frame");
	}
	
	
	public void testFrameTable() {
		Assert.assertEquals(DriverTestPage.tableIFrame.getRowCount(), 2);
	}
	
	
	public void testIsElementPresentInFrame() {
		Assert.assertTrue(DriverTestPage.tableIFrame.isElementPresent());
	}
	
	
	public void testFindElements() {
		Assert.assertEquals(DriverTestPage.rows.findElements().size(), 4);
	}
	
	
	public void testFindElementsBy() {
		Assert.assertEquals(DriverTestPage.tableIFrame.findElements(By.tagName("tr")).size(), 2);
	}
	
	
	/**
	 * Check it's possible to define a frame as a sub-element of an other one (e.g: a div)
	 */
	public void testFrameOtherElement() {
		Assert.assertEquals(DriverTestPage.textElementInIFrameInDiv.getValue(), "an other value in an other iframe");
	}
	
	public void testFrameInFrameText() {
		Assert.assertEquals(DriverTestPage.textElementSubIFrame.getValue(), "an other value in iframe");
	}
	
	/**
	 * issue #276: check we can get an element inside second iframe (searching by index)
	 */
	
	public void testFrameInSecondFrameText() {
		Assert.assertEquals(DriverTestPage.textElementSecondIFrame.getValue(), "an other value in iframe");
	}
	
	/**
	 * test that when working with an element inside frame, we automatically go back to default content before next element action
	 */
	
	public void testBackToMainFrame() {
		System.out.println(DriverTestPage.labelIFrame.getText());
		try {
			DriverTestPage.textElement.sendKeys("youpi");
			Assert.assertEquals(DriverTestPage.textElement.getValue(), "youpi");
		} finally {
			DriverTestPage.resetButton.click();
		}
		
	}	
 	
}
