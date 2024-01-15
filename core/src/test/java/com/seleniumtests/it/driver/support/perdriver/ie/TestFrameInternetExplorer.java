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
package com.seleniumtests.it.driver.support.perdriver.ie;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameInternetExplorer extends TestFrame {

	public TestFrameInternetExplorer() throws Exception {
		super(BrowserType.INTERNET_EXPLORER);
	}

	@Test(groups= {"ie"})
	public void testFrameText() {
		super.testFrameText();
	}
	
	@Test(groups= {"ie"})
	public void testFrameRadio() {
		super.testFrameRadio();
	}
	
	@Test(groups= {"ie"})
	public void testFrameCheckbox() {
		super.testFrameCheckbox();
	}
	
	@Test(groups= {"ie"})
	public void testFrameButton() {
		super.testFrameButton();
	}
	
	@Test(groups= {"ie"})
	public void testFrameLabel() {
		super.testFrameLabel();
	}
	
	@Test(groups= {"ie"})
	public void testFrameLink() {
		super.testFrameLink();
	}
	
	@Test(groups= {"ie"})
	public void testFrameSelect() {
		super.testFrameSelect();
	}
	
	@Test(groups= {"ie"})
	public void testElementInsideOtherElementWithFrame() {
		super.testElementInsideOtherElementWithFrame();
	}
	
	@Test(groups= {"ie"})
	public void testFrameTable() {
		super.testFrameTable();
	}
	
	@Test(groups= {"ie"})
	public void testIsElementPresentInFrame() {
		super.testIsElementPresentInFrame();
	}
	
	@Test(groups= {"ie"})
	public void testFindElements() {
		super.testFindElements();
	}
	
	@Test(groups= {"ie"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}
	
	@Test(groups= {"ie"})
	public void testFrameInFrameText() {
		super.testFrameInFrameText();
	}
	
	@Test(groups= {"ie"})
	public void testBackToMainFrame() {
		super.testBackToMainFrame();
	}	

	@Test(groups= {"ie"})
	public void testFrameInSecondFrameText() {
		super.testFrameInSecondFrameText();
	}
}
