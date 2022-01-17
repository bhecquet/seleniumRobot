package com.seleniumtests.ut.connectors.selenium.fielddetector;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.ConnectorsTest;
import com.seleniumtests.connectors.selenium.fielddetector.FieldDetectorConnector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;

import kong.unirest.HttpRequest;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

@PrepareForTest({ Unirest.class })
public class TestFieldDetectorConnector extends ConnectorsTest {

	@BeforeMethod(alwaysRun = true)
	public void init() {
		FieldDetectorConnector.resetFieldDetectorConnector();
	}

	@Test(groups = { "ut" })
	public void testIsAlive() {

		createServerMock("GET", "/status", 200, "OK");
		FieldDetectorConnector.getInstance("http://localhost:4321");
	}

	@Test(groups = { "ut" }, expectedExceptions = ConfigurationException.class)
	public void testIsAbsent() {
		HttpRequest<HttpRequest> req = createServerMock("GET", "/status", 500, "KO", "body");
		when(req.asString()).thenThrow(UnirestException.class);

		FieldDetectorConnector.getInstance("http://localhost:4321");
	}

	@Test(groups = { "ut" }, expectedExceptions = ConfigurationException.class)
	public void testIsNotAlive() {

		createServerMock("GET", "/status", 500, "Error");
		FieldDetectorConnector.getInstance("http://localhost:4321");
	}

	@Test(groups={"ut"})
	public void testDetectOk() throws IOException  {
		File image = createImageFromResource("ti/form_picture.png");
		
		String detectionReply = String.format("{"
				+ "	\"error\": null,"
				+ "	\"%s\": {"
				+ "		\"fields\": [{"
				+ "			\"bottom\": 220,"
				+ "			\"class_id\": 4,"
				+ "			\"class_name\": \"field_with_label\","
				+ "			\"height\": 36,"
				+ "			\"left\": 491,"
				+ "			\"related_field\": {"
				+ "				\"bottom\": 228,"
				+ "				\"class_id\": 0,"
				+ "				\"class_name\": \"field\","
				+ "				\"height\": 26,"
				+ "				\"left\": 495,"
				+ "				\"related_field\": null,"
				+ "				\"right\": 642,"
				+ "				\"text\": null,"
				+ "				\"top\": 202,"
				+ "				\"width\": 147,"
				+ "				\"with_label\": false"
				+ "			},"
				+ "			\"right\": 705,"
				+ "			\"text\": \"Date de naissance\","
				+ "			\"top\": 184,"
				+ "			\"width\": 214,"
				+ "			\"with_label\": true"
				+ "		},"
				+ "		{"
				+ "			\"bottom\": 204,"
				+ "			\"class_id\": 0,"
				+ "			\"class_name\": \"field\","
				+ "			\"height\": 21,"
				+ "			\"left\": 611,"
				+ "			\"related_field\": null,"
				+ "			\"right\": 711,"
				+ "			\"text\": null,"
				+ "			\"top\": 183,"
				+ "			\"width\": 100,"
				+ "			\"with_label\": false"
				+ "		}],"
				+ "		\"labels\": [{"
				+ "			\"bottom\": 20,"
				+ "			\"height\": 15,"
				+ "			\"left\": 159,"
				+ "			\"right\": 460,"
				+ "			\"text\": \"Dossier Client S3]HOMOLOGATIO\","
				+ "			\"top\": 5,"
				+ "			\"width\": 301"
				+ "		},"
				+ "		{"
				+ "			\"bottom\": 262,"
				+ "			\"height\": 8,"
				+ "			\"left\": 939,"
				+ "			\"right\": 999,"
				+ "			\"text\": \"Rechercher\","
				+ "			\"top\": 254,"
				+ "			\"width\": 60"
				+ "		}]"
				+ "	}"
				+ "}", image.getName());
		
		createServerMock("GET", "/status", 200, "OK");	
		createServerMock("POST", "/detect", 200, detectionReply);	
		FieldDetectorConnector fieldDetectorConnector = FieldDetectorConnector.getInstance("http://localhost:4321");
		List<JSONObject> fields = fieldDetectorConnector.detect(image).getJSONArray("fields").toList();
		
		Assert.assertEquals(fields.size(), 2);
	}
	
	@Test(groups={"ut"})
	public void testDetectErrorOk() throws IOException  {
		File image = createImageFromResource("ti/form_picture.png");
		
		String detectionReply = String.format("{" 
				+ "	\"error\": null,"
				+ "	\"%s\": {"
				+ "		\"fields\": ["
				+ "		{"
				+ "			\"bottom\": 204,"
				+ "			\"class_id\": 0,"
				+ "			\"class_name\": \"error_field\","
				+ "			\"height\": 21,"
				+ "			\"left\": 611,"
				+ "			\"related_field\": null,"
				+ "			\"right\": 711,"
				+ "			\"text\": null,"
				+ "			\"top\": 183,"
				+ "			\"width\": 100,"
				+ "			\"with_label\": false"
				+ "		}],"
				+ "		\"labels\": [{"
				+ "			\"bottom\": 20,"
				+ "			\"height\": 15,"
				+ "			\"left\": 159,"
				+ "			\"right\": 460,"
				+ "			\"text\": \"Dossier Client S3]HOMOLOGATIO\","
				+ "			\"top\": 5,"
				+ "			\"width\": 301"
				+ "		},"
				+ "		{"
				+ "			\"bottom\": 262,"
				+ "			\"height\": 8,"
				+ "			\"left\": 939,"
				+ "			\"right\": 999,"
				+ "			\"text\": \"Rechercher\","
				+ "			\"top\": 254,"
				+ "			\"width\": 60"
				+ "		}]"
				+ "	}"
				+ "}", image.getName());
		
		createServerMock("GET", "/status", 200, "OK");	
		createServerMock("POST", "/detectError", 200, detectionReply);	
		FieldDetectorConnector fieldDetectorConnector = FieldDetectorConnector.getInstance("http://localhost:4321");
		List<JSONObject> fields = fieldDetectorConnector.detectError(image).getJSONArray("fields").toList();
		
		Assert.assertEquals(fields.size(), 1);
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Field detector returned error: some error occured")
	public void testDetectKoWithMessage() throws IOException  {
		File image = createImageFromResource("ti/form_picture.png");
		
		String detectionReply = "{\"error\": \"some error occured\"}";
		
		createServerMock("GET", "/status", 200, "OK");	
		createServerMock("POST", "/detect", 500, detectionReply);	
		FieldDetectorConnector fieldDetectorConnector = FieldDetectorConnector.getInstance("http://localhost:4321");
		fieldDetectorConnector.detect(image);
		
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Field detector did not return any information: some error occured")
	public void testDetectKoWithMessage2() throws IOException  {
		File image = createImageFromResource("ti/form_picture.png");
		
		String detectionReply = "{\"error\": \"some error occured\"}";
		
		createServerMock("GET", "/status", 200, "OK");	
		createServerMock("POST", "/detect", 200, detectionReply);	
		FieldDetectorConnector fieldDetectorConnector = FieldDetectorConnector.getInstance("http://localhost:4321");
		fieldDetectorConnector.detect(image);
		
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Field detector returned error")
	public void testDetectKoWithoutMessage() throws IOException  {
		File image = createImageFromResource("ti/form_picture.png");
		
		String detectionReply = "Internal server error";
		
		createServerMock("GET", "/status", 200, "OK");	
		createServerMock("POST", "/detect", 500, detectionReply);	
		FieldDetectorConnector fieldDetectorConnector = FieldDetectorConnector.getInstance("http://localhost:4321");
		fieldDetectorConnector.detect(image);
		
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Image file is null")
	public void testDetectKoImageNull() throws IOException  {	

		createServerMock("GET", "/status", 200, "OK");	
		FieldDetectorConnector fieldDetectorConnector = FieldDetectorConnector.getInstance("http://localhost:4321");
		fieldDetectorConnector.detect(null);
		
	}
	
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = ".*noPath\\.png not found")
	public void testDetectKoImageDoesNotExist() throws IOException  {	
		
		createServerMock("GET", "/status", 200, "OK");	
		FieldDetectorConnector fieldDetectorConnector = FieldDetectorConnector.getInstance("http://localhost:4321");
		fieldDetectorConnector.detect(new File("noPath.png"));
		
	}
}
