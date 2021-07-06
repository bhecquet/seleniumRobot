package com.seleniumtests.ut.uipage.uielements;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.uipage.uielements.ByUI;
import com.seleniumtests.uipage.uielements.ElementType;

public class TestByUI extends GenericTest {

	@Test(groups= {"ut"})
	public void testToStringNoInfo() {
		Assert.assertEquals(new ByUI().toString(), "ByUI(type='null')");
	}
	@Test(groups= {"ut"})
	public void testToStringFullInfo() {
		Assert.assertEquals(ByUI.type(ElementType.CHECKBOX)
				.toTheLeftOfLabel("leftL")
				.toTheRightOfLabel("rightL")
				.aboveLabel("aboveL")
				.belowLabel("belowL")
				.textMatching("textL").toString(), "ByUI(type='CHECKBOX', leftOf='leftL', rightOf='rightL', above='aboveL', below='belowL', text='textL')");
	}
}
