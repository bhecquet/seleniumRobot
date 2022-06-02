package com.seleniumtests.ut.connectors.selenium.fielddetector;


import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.FieldDetectorConnector;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector.FieldType;
import com.seleniumtests.connectors.selenium.fielddetector.Label;
import com.seleniumtests.core.SeleniumTestsContextManager;

import kong.unirest.json.JSONObject;

public class TestImageFieldDetector extends MockitoTest {

	@Mock
	FieldDetectorConnector fieldDetectorConnector;
	
	@Test(groups= {"ut"})
	public void testDetectFields() throws IOException {
		JSONObject obj = new JSONObject("{"
				+ "	\"fields\": [{"
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
				+ "	},"
				+ "	{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 975,"
				+ "		\"bottom\": 996,"
				+ "		\"left\": 4,"
				+ "		\"right\": 90,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"an other text\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 86,"
				+ "		\"height\": 21"
				+ "	}],"
				+ "	\"labels\": [{"
				+ "		\"top\": 24,"
				+ "		\"left\": 8,"
				+ "		\"width\": 244,"
				+ "		\"height\": 16,"
				+ "		\"text\": \"Test clicking a moving element\","
				+ "		\"right\": 252,"
				+ "		\"bottom\": 40"
				+ "	},"
				+ "	{"
				+ "		\"top\": 63,"
				+ "		\"left\": 16,"
				+ "		\"width\": 89,"
				+ "		\"height\": 11,"
				+ "		\"text\": \"Start Animation\","
				+ "		\"right\": 105,"
				+ "		\"bottom\": 74"
				+ "	}]"
				+ "} ");
		
		
		
		
		File image = createImageFromResource("ti/form_picture.png");
		
		when(fieldDetectorConnector.detect(image, 1)).thenReturn(obj);
		
		SeleniumTestsContextManager.getGlobalContext().setFieldDetectorInstance(fieldDetectorConnector);
		List<Field> fields = new ImageFieldDetector(image).detectFields();
		Assert.assertEquals(fields.size(), 2);
	}
	
	@Test(groups= {"ut"})
	public void testDetectErrorMessageAndFields() throws IOException {
		JSONObject obj = new JSONObject("{"
				+ "	\"fields\": [{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 674,"
				+ "		\"bottom\": 696,"
				+ "		\"left\": 20,"
				+ "		\"right\": 141,"
				+ "		\"class_name\": \"error_field\","
				+ "		\"text\": \"= Value 4\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 121,"
				+ "		\"height\": 22"
				+ "	},"
				+ "	{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 975,"
				+ "		\"bottom\": 996,"
				+ "		\"left\": 4,"
				+ "		\"right\": 90,"
				+ "		\"class_name\": \"error_message\","
				+ "		\"text\": \"an other text\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 86,"
				+ "		\"height\": 21"
				+ "	}],"
				+ "	\"labels\": [{"
				+ "		\"top\": 24,"
				+ "		\"left\": 8,"
				+ "		\"width\": 244,"
				+ "		\"height\": 16,"
				+ "		\"text\": \"Test clicking a moving element\","
				+ "		\"right\": 252,"
				+ "		\"bottom\": 40"
				+ "	},"
				+ "	{"
				+ "		\"top\": 63,"
				+ "		\"left\": 16,"
				+ "		\"width\": 89,"
				+ "		\"height\": 11,"
				+ "		\"text\": \"Start Animation\","
				+ "		\"right\": 105,"
				+ "		\"bottom\": 74"
				+ "	}]"
				+ "} ");
		
		
		
		
		File image = createImageFromResource("ti/form_picture.png");
		
		when(fieldDetectorConnector.detectError(image, 1)).thenReturn(obj);
		
		SeleniumTestsContextManager.getGlobalContext().setFieldDetectorInstance(fieldDetectorConnector);
		List<Field> fields = new ImageFieldDetector(image, 1, FieldType.ERROR_MESSAGES_AND_FIELDS).detectFields();
		Assert.assertEquals(fields.size(), 2);
	}
	
	@Test(groups= {"ut"})
	public void testDetectFieldsWrongFormat() throws IOException {
		JSONObject obj = new JSONObject("{"
				+ "	\"labels\": [{"
				+ "		\"top\": 24,"
				+ "		\"left\": 8,"
				+ "		\"width\": 244,"
				+ "		\"height\": 16,"
				+ "		\"text\": \"Test clicking a moving element\","
				+ "		\"right\": 252,"
				+ "		\"bottom\": 40"
				+ "	},"
				+ "	{"
				+ "		\"top\": 63,"
				+ "		\"left\": 16,"
				+ "		\"width\": 89,"
				+ "		\"height\": 11,"
				+ "		\"text\": \"Start Animation\","
				+ "		\"right\": 105,"
				+ "		\"bottom\": 74"
				+ "	}]"
				+ "} ");
		
		
		
		
		File image = createImageFromResource("ti/form_picture.png");
		
		when(fieldDetectorConnector.detect(image, 1)).thenReturn(obj);
		
		SeleniumTestsContextManager.getGlobalContext().setFieldDetectorInstance(fieldDetectorConnector);
		List<Field> fields = new ImageFieldDetector(image).detectFields();
		Assert.assertEquals(fields.size(), 0);
	}
	
	@Test(groups= {"ut"})
	public void testDetectLabels() throws IOException {
		JSONObject obj = new JSONObject("{"
				+ "	\"fields\": [{"
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
				+ "	},"
				+ "	{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 975,"
				+ "		\"bottom\": 996,"
				+ "		\"left\": 4,"
				+ "		\"right\": 90,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"an other text\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 86,"
				+ "		\"height\": 21"
				+ "	}],"
				+ "	\"labels\": [{"
				+ "		\"top\": 24,"
				+ "		\"left\": 8,"
				+ "		\"width\": 244,"
				+ "		\"height\": 16,"
				+ "		\"text\": \"Test clicking a moving element\","
				+ "		\"right\": 252,"
				+ "		\"bottom\": 40"
				+ "	},"
				+ "	{"
				+ "		\"top\": 63,"
				+ "		\"left\": 16,"
				+ "		\"width\": 89,"
				+ "		\"height\": 11,"
				+ "		\"text\": \"Start Animation\","
				+ "		\"right\": 105,"
				+ "		\"bottom\": 74"
				+ "	}]"
				+ "} ");
		
		
		
		
		File image = createImageFromResource("ti/form_picture.png");
		
		when(fieldDetectorConnector.detect(image, 1)).thenReturn(obj);
		
		SeleniumTestsContextManager.getGlobalContext().setFieldDetectorInstance(fieldDetectorConnector);
		List<Label> labels = new ImageFieldDetector(image, 1, FieldType.ALL_FORM_FIELDS).detectLabels();
		Assert.assertEquals(labels.size(), 2);
	}
	
	@Test(groups= {"ut"})
	public void testDetectLabelsWrongFormat() throws IOException {
		JSONObject obj = new JSONObject("{"
				+ "	\"fields\": [{"
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
				+ "	},"
				+ "	{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 975,"
				+ "		\"bottom\": 996,"
				+ "		\"left\": 4,"
				+ "		\"right\": 90,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"an other text\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 86,"
				+ "		\"height\": 21"
				+ "	}]"
				+ "} ");
		
		
		
		
		File image = createImageFromResource("ti/form_picture.png");
		
		when(fieldDetectorConnector.detect(image, 2)).thenReturn(obj);
		
		SeleniumTestsContextManager.getGlobalContext().setFieldDetectorInstance(fieldDetectorConnector);
		List<Label> labels = new ImageFieldDetector(image, 2).detectLabels();
		Assert.assertEquals(labels.size(), 0);
	}
	
	/**
	 * Check detector is called only once event if we detect fields and labeld
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testDetectFieldsAndLabels() throws IOException {
		JSONObject obj = new JSONObject("{"
				+ "	\"fields\": [{"
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
				+ "	},"
				+ "	{"
				+ "		\"class_id\": 3,"
				+ "		\"top\": 975,"
				+ "		\"bottom\": 996,"
				+ "		\"left\": 4,"
				+ "		\"right\": 90,"
				+ "		\"class_name\": \"radio_with_label\","
				+ "		\"text\": \"an other text\","
				+ "		\"related_field\": null,"
				+ "		\"with_label\": true,"
				+ "		\"width\": 86,"
				+ "		\"height\": 21"
				+ "	}],"
				+ "	\"labels\": [{"
				+ "		\"top\": 24,"
				+ "		\"left\": 8,"
				+ "		\"width\": 244,"
				+ "		\"height\": 16,"
				+ "		\"text\": \"Test clicking a moving element\","
				+ "		\"right\": 252,"
				+ "		\"bottom\": 40"
				+ "	},"
				+ "	{"
				+ "		\"top\": 63,"
				+ "		\"left\": 16,"
				+ "		\"width\": 89,"
				+ "		\"height\": 11,"
				+ "		\"text\": \"Start Animation\","
				+ "		\"right\": 105,"
				+ "		\"bottom\": 74"
				+ "	}]"
				+ "} ");
		
		
		
		
		File image = createImageFromResource("ti/form_picture.png");
		
		when(fieldDetectorConnector.detect(image, 1)).thenReturn(obj);
		
		SeleniumTestsContextManager.getGlobalContext().setFieldDetectorInstance(fieldDetectorConnector);
		ImageFieldDetector detector = new ImageFieldDetector(image);
		List<Field> fields = detector.detectFields();
		List<Label> labels = detector.detectLabels();
		
		// detector is called only once
		verify(fieldDetectorConnector).detect(image, 1);
	}
}
