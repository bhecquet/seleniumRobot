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
}
