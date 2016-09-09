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
package com.seleniumtests.ut.reporter;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.ElaborateLog;

public class TestElaborateLog extends GenericTest {

	@Test(groups={"ut"})
	public void testToString() {
		ElaborateLog log = new ElaborateLog(" a string with ||TYPE=string");
		Assert.assertEquals(log.toString(), "TYPE=string||MSG= a string with ||SCREEN=||SRC=||LOCATION=||HREF=");
	}
	
	@Test(groups={"ut"})
	public void testHref() {
		ElaborateLog log = new ElaborateLog("TYPE=string||MSG= a string with ||SCREEN=||SRC=||LOCATION=||HREF=href");
		Assert.assertEquals(log.getHref(), "href");
	}
	
	@Test(groups={"ut"})
	public void testLocation() {
		ElaborateLog log = new ElaborateLog("TYPE=string||MSG= a string with ||SCREEN=||SRC=||LOCATION=location||HREF=");
		Assert.assertEquals(log.getLocation(), "location");
	}
	
	@Test(groups={"ut"})
	public void testMsg() {
		ElaborateLog log = new ElaborateLog("a new message||SCREEN=||SRC=||LOCATION=location||HREF=");
		Assert.assertEquals(log.getMsg(), "a new message");
	}
	
	@Test(groups={"ut"})
	public void testHrefEmpty() {
		ElaborateLog log = new ElaborateLog("TYPE=string||MSG= a string with ||SCREEN=||SRC=||LOCATION=");
		Assert.assertEquals(log.getHref(), null);
	}
}
