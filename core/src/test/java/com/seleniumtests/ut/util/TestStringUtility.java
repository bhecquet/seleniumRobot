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

import java.net.MalformedURLException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class TestStringUtility extends GenericTest {

	@Test(groups={"ut"})
	public void testOddCharWithNull() throws MalformedURLException {
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
	
	/**
	 * Issue #402: check we have no NPE if message is null
	 */
	@Test(groups={"ut"})
	public void testKeepNewLineInHtmlWithNullMessage() {
		Assert.assertNull(StringUtility.encodeString(null, "html"));
	}

	@Test(groups={"ut"}, expectedExceptions = CustomSeleniumTestsException.class)
	public void testKeepNewLineInHtmlWithNullFormat() {
		Assert.assertNull(StringUtility.encodeString("foo\nbar", null));
	}

	@Test(groups={"ut"})
	public void testInterpolateString() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("url", new TestVariable("url", "http://mysite"));

		Assert.assertEquals(StringUtility.interpolateString("connect to ${url}", SeleniumTestsContextManager.getThreadContext()), "connect to http://mysite");
	}
	

	/**
	 * Test the case where a variable contains a reference to an other one
	 */
	@Test(groups={"ut"})
	public void testInterpolateStringVariableInVariable() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("url", new TestVariable("url", "http://mysite${path}"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("path", new TestVariable("path", "/foo/bar${param}"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("param", new TestVariable("param", "?key=value"));

		Assert.assertEquals(StringUtility.interpolateString("connect to ${url}", SeleniumTestsContextManager.getThreadContext()), "connect to http://mysite/foo/bar?key=value");
	}
	
	/**
	 * Check no looping occurs when a variable reference itself
	 */
	@Test(groups={"ut"})
	public void testInterpolateStringWithLoops() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("url", new TestVariable("url", "http://mysite${path}"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("path", new TestVariable("path", "/foo/bar${url}"));
		
		Assert.assertEquals(StringUtility.interpolateString("connect to ${url}", SeleniumTestsContextManager.getThreadContext()), "connect to http://mysite/foo/barhttp://mysite/foo/barhttp://mysite/foo/barhttp://mysite/foo/barhttp://mysite/foo/bar${url}");
	}
	
	@Test(groups={"ut"})
	public void testInterpolateNullString() {
		Assert.assertNull(StringUtility.interpolateString(null, SeleniumTestsContextManager.getThreadContext()));
	}
	
	@Test(groups={"ut"})
	public void testInterpolateVariableDoesNotExist() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("url", new TestVariable("url", "http://mysite${path}"));
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("path", new TestVariable("path", "/foo/bar${param}"));
		Assert.assertEquals(StringUtility.interpolateString("connect to ${url}", SeleniumTestsContextManager.getThreadContext()), "connect to http://mysite/foo/bar${param}");
	}
	
	/**
	 * If context is null, return the initial string
	 */
	@Test(groups={"ut"})
	public void testInterpolateStringNullContext() {
		Assert.assertEquals(StringUtility.interpolateString("connect to ${url}", null), "connect to ${url}");
	}
	@Test(groups={"ut"})
	public void testInterpolateStringNullconfiguration() {
		SeleniumTestsContextManager.getThreadContext().setConfiguration(null);
		Assert.assertEquals(StringUtility.interpolateString("connect to ${url}", SeleniumTestsContextManager.getThreadContext()), "connect to ${url}");
	}
	
	@Test(groups={"ut"})
	public void testInterpolateString2() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("url", new TestVariable("url", "http://mysite"));
		
		Assert.assertEquals(StringUtility.interpolateString("connect to ${url} ${url2} correctly", SeleniumTestsContextManager.getThreadContext()), "connect to http://mysite ${url2} correctly");
	}
	
	@Test(groups={"ut"})
	public void testInterpolateStringMaskPassword1() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("password", new TestVariable("password", "abc"));
		
		Assert.assertEquals(StringUtility.interpolateString("connect with ${password}", SeleniumTestsContextManager.getThreadContext()), "connect with ****");
	}
	
	
	@Test(groups={"ut"})
	public void testInterpolateStringDoNotMaskPassword1() {
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("password", new TestVariable("password", "abc"));
		
		Assert.assertEquals(StringUtility.interpolateString("connect with ${password}", SeleniumTestsContextManager.getThreadContext(), false), "connect with abc");
	}
	
	@Test(groups={"ut"})
	public void testInterpolateStringMaskPassword2() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("passwd", new TestVariable("passwd", "abc"));
		
		Assert.assertEquals(StringUtility.interpolateString("connect with ${passwd}", SeleniumTestsContextManager.getThreadContext()), "connect with ****");
	}
	
	@Test(groups={"ut"})
	public void testInterpolateStringMaskPassword3() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(true);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("pwd", new TestVariable("pwd", "abc"));
		
		Assert.assertEquals(StringUtility.interpolateString("connect with ${pwd}", SeleniumTestsContextManager.getThreadContext()), "connect with ****");
	}
	
	@Test(groups={"ut"})
	public void testInterpolateStringDoNotMaskPassword() {
		SeleniumTestsContextManager.getThreadContext().setMaskPassword(false);
		SeleniumTestsContextManager.getThreadContext().getConfiguration().put("pwd", new TestVariable("pwd", "abc"));
		
		Assert.assertEquals(StringUtility.interpolateString("connect with ${pwd}", SeleniumTestsContextManager.getThreadContext()), "connect with abc");
	}
	
	@Test(groups={"ut"})
	public void testInterpolateStringKeyNotFound() {
		Assert.assertEquals(StringUtility.interpolateString("connect to ${url}", SeleniumTestsContextManager.getThreadContext()), "connect to ${url}");
	}
}
