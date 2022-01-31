package com.seleniumtests.it.connector.selenium.fielddetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.FieldDetectorConnector;
import com.seleniumtests.connectors.selenium.fielddetector.ImageFieldDetector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

import kong.unirest.json.JSONObject;

public class TestFieldDetectorConnector extends GenericTest {


	private File createImageFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("img", "." + FilenameUtils.getExtension(resource));
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
	}

	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		System.setProperty(SeleniumTestsContext.IMAGE_FIELD_DETECTOR_SERVER_URL, "http://localhost:5000");
		initThreadContext(ctx);
	}
	
	@AfterMethod(groups= {"it"}, alwaysRun = true)
	public void reinit() {
		System.clearProperty(SeleniumTestsContext.IMAGE_FIELD_DETECTOR_SERVER_URL);
	}
	
	@Test(groups="it", enabled = false)
	public void testFieldDetection() throws IOException {
		ImageFieldDetector imageFieldDetector;
		try {
//			imageFieldDetector = new ImageFieldDetector(createImageFromResource("tu/imageFieldDetection/browserCapture.png"), 0.75);
			imageFieldDetector = new ImageFieldDetector(new File("D:\\Dev\\seleniumRobot\\seleniumRobot-core\\core\\test-output\\testSendKeysWithLabelOnTheLeft\\screenshots\\f5972501f7e893bfa3aa0281e4f647e1.png"), 0.75);
//			imageFieldDetector = new ImageFieldDetector(createImageFromResource("ti/form_picture.png"), 0.75);
		} catch (ConfigurationException e) {
			throw new SkipException("no field detector server available");
		}
		List<Field> fields = imageFieldDetector.detectFields();
		Assert.assertTrue(fields.size() > 10);
	}
}
