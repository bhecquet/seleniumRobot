/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		tempFile.deleteOnExit();
		File ffLogo = createFileFromResource("tu/ffLogo1.png");
		ImageProcessor.concat(ffLogo, ffLogo, tempFile, 0, 70);
		
		File concatFfLogoOk = createFileFromResource("tu/images/ffLogoConcat.png");
		BufferedImage concatFfLogoOkBuf = ImageProcessor.loadFromFile(concatFfLogoOk);
		
		Assert.assertEquals(ImageProcessor.toBase64(concatFfLogoOkBuf), ImageProcessor.toBase64(ImageProcessor.loadFromFile(tempFile)));
	}
	
	@Test(groups={"ut"}, expectedExceptions=IllegalArgumentException.class)
	public void testConcatImagesErrorNegativeArgs() throws IOException {
		File tempFile = File.createTempFile("img", null);
		tempFile.deleteOnExit();
		File ffLogo = createFileFromResource("tu/ffLogo1.png");
		ImageProcessor.concat(ffLogo, ffLogo, tempFile, 0, -70);
	}
}
