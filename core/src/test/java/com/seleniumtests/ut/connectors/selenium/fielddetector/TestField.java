package com.seleniumtests.ut.connectors.selenium.fielddetector;

import java.awt.Rectangle;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.Label;

import kong.unirest.json.JSONObject;


public class TestField extends GenericTest {

	
	@Test(groups= {"ut"})
	public void testChangePosition() {
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"top\": 2261,"
				+ "		\"left\": 8,"
				+ "		\"width\": 96,"
				+ "		\"height\": 14,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"class_id\": 3,"
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"right\": 104,"
				+ "		\"bottom\": 2275"
				+ "	}"));


		field.changePosition(10, 20);
		Assert.assertEquals(field.getLabel().getLeft(), 18);
		Assert.assertEquals(field.getLabel().getRight(), 114);
		Assert.assertEquals(field.getLabel().getTop(), 2281);
		Assert.assertEquals(field.getLabel().getBottom(), 2295);
		Assert.assertEquals(field.getLabel().getHeight(), 14);
		Assert.assertEquals(field.getLabel().getWidth(), 96);
		
	}
	
	@Test(groups= {"ut"})
	public void testFromJson() {
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"top\": 2261,"
				+ "		\"left\": 8,"
				+ "		\"width\": 96,"
				+ "		\"height\": 14,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"class_id\": 3,"
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"right\": 104,"
				+ "		\"bottom\": 2275"
				+ "	}"));
		Assert.assertEquals(field.getLabel().getLeft(), 8);
		Assert.assertEquals(field.getLabel().getRight(), 104);
		Assert.assertEquals(field.getLabel().getTop(), 2261);
		Assert.assertEquals(field.getLabel().getBottom(), 2275);
		Assert.assertEquals(field.getLabel().getHeight(), 14);
		Assert.assertEquals(field.getLabel().getWidth(), 96);
		
		Assert.assertEquals(field.getRectangle(), new Rectangle(8, 2261, 96, 14));
	}
	
	@Test(groups= {"ut"})
	public void testFromJsonRelatedField() {
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 4,"
				+ "		\"top\": 203,"
				+ "		\"bottom\": 225,"
				+ "		\"left\": 21,"
				+ "		\"right\": 351,"
				+ "		\"class_name\": \"field_with_label\","
				+ "		\"text\": \"Pr\\u00e9nom\","
				+ "		\"related_field\": {"
				+ "			\"class_id\": 0,"
				+ "			\"top\": 205,"
				+ "			\"bottom\": 224,"
				+ "			\"left\": 76,"
				+ "			\"right\": 354,"
				+ "			\"class_name\": \"field\","
				+ "			\"text\": null,"
				+ "			\"related_field\": null,"
				+ "			\"with_label\": false,"
				+ "			\"width\": 278,"
				+ "			\"height\": 19"
				+ "		},"
				+ "		\"with_label\": true,"
				+ "		\"width\": 330,"
				+ "		\"height\": 22"
				+ "	}"));

		
		Assert.assertEquals(field.getInnerFieldRectangle(), new Rectangle(76, 205, 278, 19));
	}
}