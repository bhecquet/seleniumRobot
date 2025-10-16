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
import com.seleniumtests.it.driver.TestPictureElement;

public class TestPictureElementEdge extends TestPictureElement {

	public TestPictureElementEdge() throws Exception {
		super(BrowserType.EDGE);
	}
	
	
	//copy all tests so that they can be played individually for debug
	@Override
    @Test(groups={"it"})
	public void testClickOnPicture() {
		super.testClickOnPicture();
	}

	@Override
    @Test(groups={"it"})
	public void testDoubleClickOnPicture() {
		super.testDoubleClickOnPicture();
	}
	
	@Override
    @Test(groups={"it"})
	public void testClickOnGooglePicture() {	
		super.testClickOnGooglePicture();
	}

	@Override
    @Test(groups={"it"})
	public void testActionDurationIsLogged() {
		super.testActionDurationIsLogged();
	}

	@Override
    @Test(groups={"it"})
	public void testClickOnGooglePictureFromFile() {
		super.testClickOnGooglePictureFromFile();
	}
	
	@Override
    @Test(groups={"it"})
	public void testSendKeysOnPicture() {
		super.testSendKeysOnPicture();
	}

	@Override
    @Test(groups={"it"})
	public void testIsVisible() { 
		super.testIsVisible();
	}
	
	@Override
    @Test(groups={"it"})
	public void testIsNotVisible() {
		super.testIsNotVisible();
	}
}
