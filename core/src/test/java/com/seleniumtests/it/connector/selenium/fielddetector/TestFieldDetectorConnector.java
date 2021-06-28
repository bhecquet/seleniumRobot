package com.seleniumtests.it.connector.selenium.fielddetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.selenium.fielddetector.Field;
import com.seleniumtests.connectors.selenium.fielddetector.FieldDetectorConnector;
import com.seleniumtests.customexception.ConfigurationException;

public class TestFieldDetectorConnector extends GenericTest {

	FieldDetectorConnector connector;
	

	private File createImageFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("img", "." + FilenameUtils.getExtension(resource));
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
	}

	@BeforeMethod(groups={"it"})
	public void init(ITestContext ctx) {
		initThreadContext(ctx);
		
		try {
			connector = new FieldDetectorConnector("http://127.0.0.1:5000");
		} catch (ConfigurationException e) {
			throw new SkipException("no field detector server available");
		}
	}
	
	@Test(groups="it")
	public void testFieldDetection() throws IOException {
		List<Field> fields = connector.detect(createImageFromResource("ti/form_picture.png"));
		Assert.assertTrue(fields.size() > 10);
	}
}
