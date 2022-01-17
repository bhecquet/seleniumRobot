package com.seleniumtests.ut.connectors.selenium.fielddetector;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.Label;

import kong.unirest.json.JSONObject;

public class TestLabel extends GenericTest {

	
	@Test(groups= {"ut"})
	public void testFromJson() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 2261,"
				+ "		\"left\": 8,"
				+ "		\"width\": 96,"
				+ "		\"height\": 14,"
				+ "		\"text\": \"My link Parent\","
				+ "		\"right\": 104,"
				+ "		\"bottom\": 2275"
				+ "	}"));
		Assert.assertEquals(label.getLeft(), 8);
		Assert.assertEquals(label.getRight(), 104);
		Assert.assertEquals(label.getTop(), 2261);
		Assert.assertEquals(label.getBottom(), 2275);
		Assert.assertEquals(label.getHeight(), 14);
		Assert.assertEquals(label.getWidth(), 96);
		Assert.assertEquals(label.getText(), "My link Parent");
	}
	
	/**
	 * Test case where text key is not present
	 */
	@Test(groups= {"ut"})
	public void testFromJsonNoText() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 2261,"
				+ "		\"left\": 8,"
				+ "		\"width\": 96,"
				+ "		\"height\": 14,"
				+ "		\"right\": 104,"
				+ "		\"bottom\": 2275"
				+ "	}"));
		Assert.assertEquals(label.getText(), null);
	}
	
	@Test(groups= {"ut"})
	public void testChangePosition() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 2261,"
				+ "		\"left\": 8,"
				+ "		\"width\": 96,"
				+ "		\"height\": 14,"
				+ "		\"text\": \"My link Parent\","
				+ "		\"right\": 104,"
				+ "		\"bottom\": 2275"
				+ "	}"));
		label.changePosition(10, 20);
		Assert.assertEquals(label.getLeft(), 18);
		Assert.assertEquals(label.getRight(), 114);
		Assert.assertEquals(label.getTop(), 2281);
		Assert.assertEquals(label.getBottom(), 2295);
		Assert.assertEquals(label.getHeight(), 14);
		Assert.assertEquals(label.getWidth(), 96);
	}
	

	/**
	 * With same coordinates, label is inside field
	 */
	@Test(groups= {"ut"})
	public void testIsInside() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 674,"
				+ "		\"left\": 20,"
				+ "		\"width\": 121,"
				+ "		\"height\": 22,"
				+ "		\"text\": \"My link Parent\","
				+ "		\"right\": 141,"
				+ "		\"bottom\": 696"
				+ "	}"));
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 674,"
				+ "		\"bottom\": 696,"
				+ "		\"left\": 20,"
				+ "		\"right\": 141,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 121,"
				+ "		\"height\": 22"
				+ "	}"));
		
		Assert.assertTrue(label.isInside(field));
		Assert.assertFalse(label.isFieldLeftOf(field));
		Assert.assertFalse(label.isFieldRightOf(field));
		Assert.assertFalse(label.isFieldAbove(field));
		Assert.assertFalse(label.isFieldBelow(field));
	}
	

	/**
	 * Label is on the top of field
	 * 
	 * 	 		------
	 * 			Label
	 * 			------
	 * 	
	 * 			-----
	 * 			Field
	 * 			-----
	 * 
	 * 
	 */
	@Test(groups= {"ut"})
	public void testIsNotInsideAbove() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 664,"
				+ "		\"left\": 20,"
				+ "		\"width\": 121,"
				+ "		\"height\": 22,"
				+ "		\"text\": \"My link Parent\","
				+ "		\"right\": 141,"
				+ "		\"bottom\": 684"
				+ "	}"));
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 674,"
				+ "		\"bottom\": 694,"
				+ "		\"left\": 20,"
				+ "		\"right\": 141,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 121,"
				+ "		\"height\": 22"
				+ "	}"));
		
		Assert.assertFalse(label.isInside(field));
		Assert.assertFalse(label.isFieldLeftOf(field));
		Assert.assertFalse(label.isFieldRightOf(field));
		Assert.assertFalse(label.isFieldAbove(field));
		Assert.assertTrue(label.isFieldBelow(field));
	}

	/**
	 * Label is on the bottom of field
	 * 	 		------
	 * 			Field
	 * 			------
	 * 	
	 * 			-----
	 * 			Label
	 * 			-----
	 * 
	 */
	@Test(groups= {"ut"})
	public void testIsNotInsideBelow() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 684,"
				+ "		\"left\": 20,"
				+ "		\"width\": 121,"
				+ "		\"height\": 22,"
				+ "		\"text\": \"My link Parent\","
				+ "		\"right\": 141,"
				+ "		\"bottom\": 704"
				+ "	}"));
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 674,"
				+ "		\"bottom\": 694,"
				+ "		\"left\": 20,"
				+ "		\"right\": 141,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 121,"
				+ "		\"height\": 22"
				+ "	}"));
		
		Assert.assertFalse(label.isInside(field));
		Assert.assertFalse(label.isFieldLeftOf(field));
		Assert.assertFalse(label.isFieldRightOf(field));
		Assert.assertTrue(label.isFieldAbove(field));
		Assert.assertFalse(label.isFieldBelow(field));
	}
	

	/**
	 * Label is on the left of field
	 * 	-----		------
	 * 	Label		Field
	 * 	-----		------	
	 * 
	 */
	@Test(groups= {"ut"})
	public void testIsNotInsideLeft() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 674,"
				+ "		\"left\": 50,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20,"
				+ "		\"text\": \"My link Parent\","
				+ "		\"right\": 150,"
				+ "		\"bottom\": 694"
				+ "	}"));
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 674,"
				+ "		\"bottom\": 694,"
				+ "		\"left\": 100,"
				+ "		\"right\": 200,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20"
				+ "	}"));
		
		Assert.assertFalse(label.isInside(field));
		Assert.assertFalse(label.isFieldLeftOf(field));
		Assert.assertTrue(label.isFieldRightOf(field));
		Assert.assertFalse(label.isFieldAbove(field));
		Assert.assertFalse(label.isFieldBelow(field));
	}
	
	/**
	 * Label is on the right of field
	 * 	-----		------
	 * 	Field		Label
	 * 	-----		------
	 * 
	 */
	@Test(groups= {"ut"})
	public void testIsNotInsideRight() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 674,"
				+ "		\"left\": 150,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20,"
				+ "		\"text\": \"My link Parent\","
				+ "		\"right\": 250,"
				+ "		\"bottom\": 694"
				+ "	}"));
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 674,"
				+ "		\"bottom\": 694,"
				+ "		\"left\": 100,"
				+ "		\"right\": 200,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20"
				+ "	}"));
		
		Assert.assertFalse(label.isInside(field));
		Assert.assertTrue(label.isFieldLeftOf(field));
		Assert.assertFalse(label.isFieldRightOf(field));
		Assert.assertFalse(label.isFieldAbove(field));
		Assert.assertFalse(label.isFieldBelow(field));
	}
	

	/**
	 * Field on the left, but not aligned horizontally
	 * 
	 * 			------
	 * 			Field
	 * 			------
	 * 	
	 * 	-----
	 * 	Label
	 * 	-----
	 * 
	 */
	@Test(groups= {"ut"})
	public void testFieldAndLabelNotAligned1() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 200,"
				+ "		\"left\": 100,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20,"
				+ "		\"right\": 200,"
				+ "		\"bottom\": 220,"
				+ "		\"text\": \"My link Parent\""
				+ "	}"));
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 100,"
				+ "		\"left\": 200,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20,"
				+ "		\"right\": 300,"
				+ "		\"bottom\": 120,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true"
				+ "	}"));
		
		Assert.assertFalse(label.isInside(field));
		Assert.assertFalse(label.isFieldLeftOf(field));
		Assert.assertFalse(label.isFieldRightOf(field));
		Assert.assertFalse(label.isFieldAbove(field));
		Assert.assertFalse(label.isFieldBelow(field));
	}
	
	/**
	 * Field on the right, but not aligned horizontally
	 * 
	 * 			------
	 * 			Field
	 * 			------
	 * 	
	 * 					-----
	 * 					Label
	 * 					-----
	 * 
	 */
	@Test(groups= {"ut"})
	public void testFieldAndLabelNotAligned2() {
		Label label = Label.fromJson(new JSONObject("{"
				+ "		\"top\": 200,"
				+ "		\"left\": 300,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20,"
				+ "		\"right\": 400,"
				+ "		\"bottom\": 220,"
				+ "		\"text\": \"My link Parent\""
				+ "	}"));
		Field field = Field.fromJson(new JSONObject("{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 100,"
				+ "		\"left\": 200,"
				+ "		\"width\": 100,"
				+ "		\"height\": 20,"
				+ "		\"right\": 300,"
				+ "		\"bottom\": 120,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true"
				+ "	}"));
		
		Assert.assertFalse(label.isInside(field));
		Assert.assertFalse(label.isFieldLeftOf(field));
		Assert.assertFalse(label.isFieldRightOf(field));
		Assert.assertFalse(label.isFieldAbove(field));
		Assert.assertFalse(label.isFieldBelow(field));
	}
	
	@Test(groups= {"ut"})
	public void testMatches() {
		Label l1 = new Label(0, 100, 0, 20, "foobar");
		Label l2 = new Label(0, 99, 0, 20, "fooba");
		Assert.assertTrue(l1.match(l2));
	}
	
	@Test(groups= {"ut"})
	public void testMatchesNull() {
		Label l1 = new Label(0, 100, 0, 20, "foobar");
		Assert.assertFalse(l1.match(null));
	}
	
	@Test(groups= {"ut"})
	public void testMatchesNullText() {
		Label l1 = new Label(0, 100, 0, 20, null);
		Label l2 = new Label(0, 99, 0, 20, "fooba");
		Assert.assertFalse(l1.match(l2));
	}
	
	@Test(groups= {"ut"})
	public void testMatchesNullText2() {
		Label l1 = new Label(0, 100, 0, 20, "foobar");
		Label l2 = new Label(0, 99, 0, 20, null);
		Assert.assertFalse(l1.match(l2));
	}
	
	/**
	 * If both texts are null, only rely on position
	 */
	@Test(groups= {"ut"})
	public void testMatchesNullText3() {
		Label l1 = new Label(0, 100, 0, 20, null);
		Label l2 = new Label(0, 99, 0, 20, null);
		Assert.assertTrue(l1.match(l2));
	}
	
	@Test(groups= {"ut"})
	public void testMatchesEmptyText() {
		Label l1 = new Label(0, 100, 0, 20, "");
		Label l2 = new Label(0, 99, 0, 20, "");
		Assert.assertTrue(l1.match(l2));
	}
	
	@Test(groups= {"ut"})
	public void testNoMatchByText() {
		Label l1 = new Label(0, 100, 0, 20, "foobar");
		Label l2 = new Label(0, 99, 0, 20, "barfoo");
		Assert.assertFalse(l1.match(l2));
	}
	
	@Test(groups= {"ut"})
	public void testNoMatchByPosition() {
		Label l1 = new Label(0, 100, 0, 20, "foobar");
		Label l2 = new Label(55, 150, 0, 20, "fooba");
		Assert.assertFalse(l1.match(l2));
	}

}
