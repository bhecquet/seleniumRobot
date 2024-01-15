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
package com.seleniumtests.it.driver.support.perdriver.chrome;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameChrome extends TestFrame {

	public TestFrameChrome() throws Exception {
		super(BrowserType.CHROME);
	}
	
	@Test(groups={"it"})
	public void testFrameText() {
		super.testFrameText();
	}
	
	@Test(groups={"it"})
	public void testFrameRadio() {
		super.testFrameRadio();
	}
	
	@Test(groups={"it"})
	public void testFrameCheckbox() {
		super.testFrameCheckbox();
	}
	
	@Test(groups={"it"})
	public void testFrameButton() {
		super.testFrameButton();
	}
	
	@Test(groups={"it"})
	public void testFrameLabel() {
		super.testFrameLabel();
	}
	
	@Test(groups={"it"})
	public void testFrameLink() {
		super.testFrameLink();
	}
	
	@Test(groups={"it"})
	public void testFrameSelect() {
		super.testFrameSelect();
	}
	
	@Test(groups={"it"})
	public void testElementInsideOtherElementWithFrame() {
		super.testElementInsideOtherElementWithFrame();
	}
	
	@Test(groups={"it"})
	public void testFrameTable() {
		super.testFrameTable();
	}
	
	@Test(groups={"it"})
	public void testIsElementPresentInFrame() {
		super.testIsElementPresentInFrame();
	}
	
	@Test(groups={"it"})
	public void testFindElements() {
		super.testFindElements();
	}
	
	@Test(groups={"it"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}
	
	@Test(groups={"it"})
	public void testFrameInFrameText() {
		super.testFrameInFrameText();
	}
	
	@Test(groups={"it"})
	public void testFrameOtherElement() {
		super.testFrameOtherElement();
	}
	
	@Test(groups={"it"})
	public void testBackToMainFrame() {
		super.testBackToMainFrame();
	}	
	
	@Test(groups={"it"})
	public void testFrameInSecondFrameText() {
		super.testFrameInSecondFrameText();
	}
 	
}
