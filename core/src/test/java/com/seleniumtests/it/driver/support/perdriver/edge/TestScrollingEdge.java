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
import com.seleniumtests.it.driver.TestAutoScrolling;

public class TestScrollingEdge extends TestAutoScrolling {

	public TestScrollingEdge() throws Exception {
		super(BrowserType.EDGE);
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToMiddleDiv1() {
		super.testScrollToMiddleDiv1();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToMiddleDiv2() {
		super.testScrollToMiddleDiv2();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToDivTop() {
		super.testScrollToDivTop();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToDivBottom() {
		super.testScrollToDivBottom();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToDivBottomWithCompositeAction() {
		super.testScrollToDivBottomWithCompositeAction();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToBottom() {
		super.testScrollToBottom();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testScrollToBottomNoHeader() {
		super.testScrollToBottomNoHeader();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testAutoScrollToMiddleDiv1() {
		super.testAutoScrollToMiddleDiv1();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testAutoScrollToMiddleDiv2() {
		super.testAutoScrollToMiddleDiv2();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testAutoScrollToDivTop() {
		super.testAutoScrollToDivTop();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testAutoScrollToDivBottom() {
		super.testAutoScrollToDivBottom();
	}

	@Override
    @Test(groups={"it"})
	public void testScrollToDivBottomClickMouse() {
		super.testScrollToDivBottomClickMouse();
	}
	
	@Override
    @Test(groups={"it", "ut"})
	public void testAutoScrollToBottom() {
		super.testAutoScrollToBottom();
	}

	@Override
    @Test(groups={"it", "ut"})
	public void testAutoScrollToMenu() {
		super.testAutoScrollToMenu();
	}
}
