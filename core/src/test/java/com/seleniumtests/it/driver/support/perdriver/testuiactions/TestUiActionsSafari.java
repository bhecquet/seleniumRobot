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
package com.seleniumtests.it.driver.support.perdriver.testuiactions;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestUiActions;

public class TestUiActionsSafari extends TestUiActions {

	public TestUiActionsSafari() throws Exception {
		super(BrowserType.SAFARI);
	}
	
	@Test(groups={"it"})
	public void testNewAction() {
		super.testNewAction();
	}
	
	@Test(groups={"it"})
	public void testNewActionWithHtmlElement() throws Exception {
		super.testNewActionWithHtmlElement();
	}

	@Test(groups={"it"})
	public void testMoveClick() {
		super.testMoveClick();
	}
	
	@Test(groups={"it"}) 
	public void testSendKeys() {
		super.testSendKeys();
	}
	
	@Test(groups={"it"}) 
	public void testSendKeysWithHtmlElement() throws Exception {
		super.testSendKeysWithHtmlElement();
	}
}
