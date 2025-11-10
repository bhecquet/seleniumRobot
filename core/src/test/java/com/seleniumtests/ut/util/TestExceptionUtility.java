package com.seleniumtests.ut.util;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.ExceptionUtility;

public class TestExceptionUtility extends GenericTest {

	@Test(groups= {"ut"})
	public void testGetExceptionMessage() {
		Assert.assertEquals(ExceptionUtility.getExceptionMessage(new Exception("toto")), "class java.lang.Exception: toto");
	}
	
	@Test(groups= {"ut"})
	public void testGetExceptionMessageWebDriverException() {
		Assert.assertEquals(ExceptionUtility.getExceptionMessage(new NoSuchElementException("toto")), 
				"class org.openqa.selenium.NoSuchElementException: toto\n");
	}
	
	@Test(groups= {"ut"})
	public void testGetExceptionMessageNull() {
		Assert.assertEquals(ExceptionUtility.getExceptionMessage(null), 
				"");
	}
	
	@Test(groups= {"ut"})
	public void testFilterStackTrace() {
		Assert.assertEquals(ExceptionUtility.filterStackTrace("foo"), "foo");
	}
	
	@Test(groups= {"ut"})
	public void testFilterStackTraceJavaBase() {
		Assert.assertNull(ExceptionUtility.filterStackTrace("java.base/java.util.ArrayList.forEach"));
	}

	@Test(groups= {"ut"})
	public void testFilterStackTraceSun() {
		Assert.assertNull(ExceptionUtility.filterStackTrace("sun.reflect.Method(132)"));
	}
	
	@Test(groups= {"ut"})
	public void testFilterStackTraceReflect() {
		Assert.assertNull(ExceptionUtility.filterStackTrace("java.lang.reflect.Method(132)"));
	}
	
	@Test(groups= {"ut"})
	public void testFilterStackTraceTestNG() {
		Assert.assertNull(ExceptionUtility.filterStackTrace("org.testng.Assert(132)"));
	}
	
	@Test(groups= {"ut"})
	public void testFilterStackTraceThread() {
		Assert.assertNull(ExceptionUtility.filterStackTrace("java.lang.Thread(132)"));
	}
	
	@Test(groups= {"ut"})
	public void testFilterStackTraceConcurrent() {
		Assert.assertNull(ExceptionUtility.filterStackTrace("java.util.concurrent.foo(132)"));
	}
	
	@Test(groups= {"ut"})
	public void testFilterStackTraceAspectj() {
		Assert.assertNull(ExceptionUtility.filterStackTrace("org.aspectj.runtime.reflect(132)"));
	}
	
	@Test(groups= {"ut"})
	public void testGenerateStackTraceNoCause() {
		Exception ex = new WebDriverException("foo");
		StringBuilder content = new StringBuilder();
		ExceptionUtility.generateTheStackTrace(ex, "Failure", content, "html");
		System.out.println(content.toString());
		Assert.assertTrue(content.toString().matches("(?s)class org.openqa.selenium.WebDriverException: Failure\n"
				+ "\n"
				+ "at com.seleniumtests.ut.util.TestExceptionUtility.testGenerateStackTraceNoCause\\(TestExceptionUtility.java:\\d+\\).*"));
	}
	
	@Test(groups= {"ut"})
	public void testGenerateStackTraceWithCause() {
		Exception ex = new WebDriverException("foo");
		Exception ex2 = new WebDriverException("foo1", ex);
		StringBuilder content = new StringBuilder();
		ExceptionUtility.generateTheStackTrace(ex2, "Failure", content, "html");
		System.out.println(content.toString());
		Assert.assertTrue(content.toString().matches("(?s)class org.openqa.selenium.WebDriverException: Failure\n"
				+ "\n"
				+ "at com.seleniumtests.ut.util.TestExceptionUtility.testGenerateStackTraceWithCause\\(TestExceptionUtility.java:\\d+\\)"
				+ ".*class org.openqa.selenium.WebDriverException: Caused by foo<br/>\n"
				+ "Build info: version: '.*?', revision: '.*?'<br/>\n"
				+ "System info: os.name: '.*?', os.arch: '.*?', os.version: '.*?', java.version: '.*?'<br/>\n"
				+ "Driver info: driver.version: unknown\n"
				+ "\n"
				+ "at com.seleniumtests.ut.util.TestExceptionUtility.testGenerateStackTraceWithCause\\(TestExceptionUtility.java:\\d+\\).*"));
		
	}
}
