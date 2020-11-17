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
package com.seleniumtests.ut.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.StringUtility;

public class TestStringUtility extends GenericTest {

	@Test(groups={"ut"})
	public void testOddCharWithNull() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName(null), "null");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalQuote() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("''"), "");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalSpace() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("  "), "__");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalBackslash() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("\\\\"), "__");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalSlash() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("//"), "");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalPeriod() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("::"), "..");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalStar() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("**"), "..");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalQuestion() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("??"), "..");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalPipe() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("||"), "");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalInferior() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("<<"), "--");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalSuperior() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName(">>"), "--");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalDoubleQuote() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("\""), "");
	}
	
	@Test(groups={"ut"})
	public void testOddCharRemovalAccents() {
		Assert.assertEquals(StringUtility.replaceOddCharsFromFileName("aàéç"), "aaec");
	}
	
	@Test(groups={"ut"})
	public void testKeepNewLineInHtml() {
		Assert.assertEquals(StringUtility.encodeString("foo\nbar", "html"), "foo<br/>\nbar");
	}
}
