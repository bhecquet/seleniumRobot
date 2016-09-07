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
