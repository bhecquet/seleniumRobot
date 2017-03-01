package com.seleniumtests.ut.util.imaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.imaging.ImageProcessor;

public class TestImageProcessor extends GenericTest {

	
	
	/**
	 * Crop a picture
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testCropImage() throws IOException {
		File ffLogo = createFileFromResource("tu/ffLogo1.png");
		BufferedImage ffLogoBuf = ImageProcessor.loadFromFile(ffLogo);
		BufferedImage croppedFfLogo = ImageProcessor.cropImage(ffLogoBuf, 10, 10, 100, 20);
		
		File croppedFfLogoOk = createFileFromResource("tu/images/croppedFfLogo.png");
		BufferedImage croppedFfLogoOkBuf = ImageProcessor.loadFromFile(croppedFfLogoOk);
		
		Assert.assertEquals(ImageProcessor.toBase64(croppedFfLogoOkBuf), ImageProcessor.toBase64(croppedFfLogo));
	}
	
	@Test(groups={"ut"})
	public void testConcatImages() throws IOException {
		File tempFile = File.createTempFile("img", null);
		File ffLogo = createFileFromResource("tu/ffLogo1.png");
		ImageProcessor.concat(ffLogo, ffLogo, tempFile, 0, 70);
		
		File concatFfLogoOk = createFileFromResource("tu/images/ffLogoConcat.png");
		BufferedImage concatFfLogoOkBuf = ImageProcessor.loadFromFile(concatFfLogoOk);
		
		Assert.assertEquals(ImageProcessor.toBase64(concatFfLogoOkBuf), ImageProcessor.toBase64(ImageProcessor.loadFromFile(tempFile)));
	}
	
	@Test(groups={"ut"}, expectedExceptions=IllegalArgumentException.class)
	public void testConcatImagesErrorNegativeArgs() throws IOException {
		File tempFile = File.createTempFile("img", null);
		File ffLogo = createFileFromResource("tu/ffLogo1.png");
		ImageProcessor.concat(ffLogo, ffLogo, tempFile, 0, -70);
	}
}
