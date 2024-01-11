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
package com.seleniumtests.it.driver.support.perdriver.testscrolling;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestAutoScrolling;

public class TestScrollingChrome extends TestAutoScrolling {

	public TestScrollingChrome() throws Exception {
		super(BrowserType.CHROME);
	}
	
	@Test(groups="it")
	public void testScrollToMiddleDiv1() {
		super.testScrollToMiddleDiv1();
	}
	
	@Test(groups="it")
	public void testScrollToMiddleDiv2() {
		super.testScrollToMiddleDiv2();
	}
	
	@Test(groups="it")
	public void testScrollToDivTop() {
		super.testScrollToDivTop();
	}
	
	@Test(groups="it")
	public void testScrollToDivBottom() {
		super.testScrollToDivBottom();
	}
	
	@Test(groups={"it-driver"})
	public void testScrollToDivBottomClickMouse() {
		super.testScrollToDivBottomClickMouse();
	}
	
	@Test(groups="it")
	public void testScrollToDivBottomWithCompositeAction() {
		super.testScrollToDivBottomWithCompositeAction();
	}

	@Test(groups="it")
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}
	
	@Test(groups="it")
	public void testScrollToBottomNoHeader() {
		super.testScrollToBottomNoHeader();
	}
	
	@Test(groups="it")
	public void testAutoScrollToMiddleDiv1() {
		super.testAutoScrollToMiddleDiv1();
	}
	
	@Test(groups="it")
	public void testAutoScrollToMiddleDiv2() {
		super.testAutoScrollToMiddleDiv2();
	}
	
	@Test(groups="it")
	public void testAutoScrollToDivTop() {
		super.testAutoScrollToDivTop();
	}
	
	@Test(groups="it")
	public void testAutoScrollToDivBottom() {
		super.testAutoScrollToDivBottom();
	}
	
	@Test(groups="it")
	public void testAutoScrollToBottom() {
		super.testAutoScrollToBottom();
	}
	
	@Test(groups="it")
	public void testAutoScrollToMenu() {
		super.testAutoScrollToMenu();
	}
}
