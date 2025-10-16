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
package com.seleniumtests.it.driver.support.perdriver.htmlunit;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestUiActions;

public class TestUiActionsHtmlUnit extends TestUiActions {

	public TestUiActionsHtmlUnit() throws Exception {
		super(BrowserType.HTMLUNIT);
	}
	
	@Override
    @Test(groups={"it"})
	public void testNewAction() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"})
	public void testNewActionWithHtmlElement() throws Exception {
		// skip as htmlunit does not support it
	}

	@Override
    @Test(groups={"it"})
	public void testMoveClick() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"}) 
	public void testSendKeys() {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"}) 
	public void testSendKeysWithHtmlElement() throws Exception {
		// skip as htmlunit does not support it
	}
	
	@Override
    @Test(groups={"it"}) 
	public void testSendKeysWithHtmlElementNotPresent() throws Exception {
		// skip as htmlunit does not support it
	}
}
