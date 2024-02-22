package com.seleniumtests.ut.core.runner.cucumber;

import java.util.List;

import org.mockito.Mockito;
import org.openqa.selenium.By;

import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.CheckBoxElement;
import com.seleniumtests.uipage.htmlelements.PictureElement;
import com.seleniumtests.uipage.htmlelements.RadioButtonElement;
import com.seleniumtests.uipage.htmlelements.ScreenZone;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class PageForActions extends PageObject {
	
	public static TextFieldElement textField = new TextFieldElement("text", By.id("text"));
	public static ScreenZone screenZone = Mockito.spy(new ScreenZone());
	public static SelectList select = new SelectList("select", By.id("select"));
	public static Table table = new Table("table", By.id("table"));
	public static CheckBoxElement checkbox = new CheckBoxElement("checkbox", By.id("checkbox"));
	public static RadioButtonElement radio = new RadioButtonElement("radio", By.id("radio"));
	public static PictureElement picture = new PictureElement("picture", "tu/googleSearch.png", null);
	public static ScreenZone zoneNotPresent = new ScreenZone("picture", "tu/images/vosAlertes.png");

	public PageForActions() {
		super();
	}
	
	public PictureElement getPicture() {
		return picture;
	}
	
	public ScreenZone getScreenZone() {
		return zoneNotPresent;
	}
	
	public TextFieldElement getTextField() {
		return textField;
	}

}
