package com.seleniumtests.ut.uipage.htmlelements;

import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.uipage.htmlelements.PictureElement;

public class TestPictureElement extends MockitoTest {

	@Test(groups={"ut"})
	public void testClick() {
		PictureElement pictureEl = new PictureElement("source", "tu/images/logo_text_field.png", null);
		pictureEl.clickAt(1, 1);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testClickOutsidePicture() {
		PictureElement pictureEl = new PictureElement("source", "tu/images/logo_text_field.png", null);
		pictureEl.clickAt(1000, 1000);
	}
}
