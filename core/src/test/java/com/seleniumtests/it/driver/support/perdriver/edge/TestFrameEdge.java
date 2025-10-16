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
package com.seleniumtests.it.driver.support.perdriver.edge;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameEdge extends TestFrame {

	public TestFrameEdge() throws Exception {
		super(BrowserType.EDGE);
	}

	@Override
    @Test(groups={"it"})
	public void testFrameText() {
		super.testFrameText();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameRadio() {
		super.testFrameRadio();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameCheckbox() {
		super.testFrameCheckbox();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameButton() {
		super.testFrameButton();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameLabel() {
		super.testFrameLabel();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameLink() {
		super.testFrameLink();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameSelect() {
		super.testFrameSelect();
	}
	
	@Override
    @Test(groups={"it"})
	public void testElementInsideOtherElementWithFrame() {
		super.testElementInsideOtherElementWithFrame();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameTable() {
		super.testFrameTable();
	}
	
	@Override
    @Test(groups={"it"})
	public void testIsElementPresentInFrame() {
		super.testIsElementPresentInFrame();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindElements() {
		super.testFindElements();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFindElementsBy() {
		super.testFindElementsBy();
	}
	
	@Override
    @Test(groups={"it"})
	public void testFrameInFrameText() {
		super.testFrameInFrameText();
	}
	
	@Override
    @Test(groups={"it"})
	public void testBackToMainFrame() {
		super.testBackToMainFrame();
	}

	@Override
    @Test(groups={"it"})
	public void testFrameInSecondFrameText() {
		super.testFrameInSecondFrameText();
	}
}
