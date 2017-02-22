/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Point;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.api.client.util.DateTime;
import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.util.imaging.ImageDetector;

public class TestImageDetector extends GenericTest {

	private File createFileFromResource(String resource) throws IOException {
		File tempFile = File.createTempFile("img", null);
		tempFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource), tempFile);
		
		return tempFile;
	}
	
	/**
	 * Search an image inside an other with templace matching
	 * @throws IOException 
	 */
	// low resolution
	@Test(groups={"ut"})
	public void searchPicturesByTemplate() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/infolidays.png"), 
				createFileFromResource("tu/images/bouton_enregistrer.png"), 0.06);

		detector.detectExactZoneWithScale();
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(457, 1582, 232, 487));
		Assert.assertEquals(detector.getSizeRatio(), 2.5, 0.05);
	}
	@Test(groups={"ut"})
	public void searchPicturesByTemplate2() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/infolidays.png"), 
				createFileFromResource("tu/images/bouton_enregistrer2.png"), 0.06);
		detector.detectExactZoneWithScale();
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(69, 1609, 185, 1299));
		Assert.assertEquals(detector.getSizeRatio(), 1.2, 0.05);
	}
	// small object
	@Test(groups={"ut"})
	public void searchPicturesByTemplate3() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/infolidays.png"), 
				createFileFromResource("tu/images/bouton_enregistrer3.png"), 0.06);
		detector.detectExactZoneWithScale();
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(677, 1595, 220, 155));
		Assert.assertEquals(detector.getSizeRatio(), 1.2, 0.05);
	}
	// highly detailed
	@Test(groups={"ut"})
	public void searchPicturesByTemplate4() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/RIB.png"), 
				createFileFromResource("tu/images/creditMutuelLogo.png"), 0.06);
		detector.detectExactZoneWithScale();
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(604, 147, 77, 493));
		Assert.assertEquals(detector.getSizeRatio(), 1.0, 0.05);
	}
	
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void searchPicturesByTemplateNoMatching() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/p9.png"), 
				createFileFromResource("tu/images/creditMutuelLogo.png"), 0.06);
		detector.detectExactZoneWithScale();
		Assert.assertEquals(detector.getSizeRatio(), 1.0, 0.05);
	}
	
	/**
	 * Search an image inside an other one but no corresponding zone should be found
	 * @throws IOException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void searchNonCorrespondingPicture() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/p9.png"), 
													createFileFromResource("tu/images/creditMutuelLogo.png"));
		detector.detectCorrespondingZone();
	}
	
	/**
	 * Search an image inside an other one but no corresponding zone should be found
	 * @throws IOException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void searchWithBlackPicture() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/blackScreen.png"), 
													createFileFromResource("tu/images/creditMutuelLogo.png"));
		detector.detectCorrespondingZone();
	}
	
	/**
	 * Initialize detector with non existing file
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void searchNonExistingPicture() {
		new ImageDetector(new File("not_existing_file.png"), 
							new File("not_existing_file2.png"));
	}
	
	/**
	 * Search an image inside an other one, no rotation, no resizing
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void searchPictureWithoutRotation() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/RIB.png"), 
						createFileFromResource("tu/images/creditMutuelLogo.png"));
		detector.detectCorrespondingZone();
		Assert.assertEquals(detector.getRotationAngle(), 0);
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(604, 147, 76, 492));
	}
	
	/**
	 * Search an image inside an other one, no rotation, no resizing
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void searchPictureWithoutRotationDriverPage() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/driverTestPage.png"), 
				createFileFromResource("tu/images/logo_text_field.png"));
		detector.detectCorrespondingZone();
		Assert.assertEquals(detector.getRotationAngle(), 0);
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(3, 714, 94, 138));
	}
	
	/**
	 * Search an image inside an other one, 90° rotation, no resizing
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void searchPictureWith90degRotation() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/p9.png"), 
				createFileFromResource("tu/images/vosAlertesRotate90.png"));
		detector.detectCorrespondingZone();
		Assert.assertEquals(detector.getRotationAngle(), 90);
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(574, 136, 29, 107));
	}
	
	/**
	 * Search an image inside an other one, 180° rotation, no resizing
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void searchPictureWith180degRotation() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/p9.png"), 
				createFileFromResource("tu/images/vosAlertesRotate180.png"));
		detector.detectCorrespondingZone();
		Assert.assertEquals(detector.getRotationAngle(), 180);
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(574, 135, 29, 107));
	}
	
	/**
	 * Search an image inside an other one, 180° rotation, no resizing
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void searchPictureWith270degRotation() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/p9.png"), 
				createFileFromResource("tu/images/vosAlertesRotate270.png"));
		detector.detectCorrespondingZone();
		Assert.assertEquals(detector.getRotationAngle(), 270);
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(575, 135, 29, 107));
	}
	
	/**
	 * Search an image inside an other one, 0° rotation, 80% resizing
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void searchPictureWithResizing() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/RIB.png"), 
				createFileFromResource("tu/images/creditMutuelLogo0.8.png"));
		detector.detectCorrespondingZone();
		Assert.assertEquals(detector.getRotationAngle(), 0);
		Assert.assertEquals(detector.getDetectedRectangle(), new Rectangle(603, 147, 77, 493));
	}
	
	/**
	 * Search an image inside an other one, 0° rotation, 80% resizing in width and 100% in height
	 * Error should be raised
	 * @throws IOException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void searchPictureWithNonProportianalResizing() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/RIB.png"), 
				createFileFromResource("tu/images/creditMutuelLogo0.8-1.png"));
		detector.detectCorrespondingZone();
	}
	
	/**
	 * Search an image inside an other one, 45° rotation, 
	 * Error should be raised
	 * @throws IOException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void searchPictureWithWrongRotating() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/RIB_rotated.png"), 
				createFileFromResource("tu/images/creditMutuelLogo.png"), 0.07);
		try {
			detector.detectCorrespondingZone();
		} finally {
			Assert.assertEquals(detector.getRotationAngle(), 44);
		}
	}
	
	/**
	 * Search an image with template matching. Here, image should not be found
	 * @throws IOException 
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void searchExactPictureNoDetection() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/RIB.png"), 
						createFileFromResource("tu/images/vosAlertes.png"));
		detector.detectExactZoneWithScale();
	}
	
	/**
	 * Search an image with template matching
	 * @throws IOException 
	 */
	@Test(groups={"ut"})
	public void searchExactPicture() throws IOException {
		ImageDetector detector = new ImageDetector(createFileFromResource("tu/images/p9.png"), 
				createFileFromResource("tu/images/vosAlertes.png"));
		detector.detectExactZoneWithScale();
	}
	
	
	private class SubImageDetector extends ImageDetector {
		
		public long testAngle(Point vec1p1, Point vec1p2, Point vec2p1, Point vec2p2) {
			return getAngleBetweenVectors(vec1p1, vec1p2, vec2p1, vec2p2);
		}
		public void testRotationAngle(Point p1, Point p2, Point p3, Point p4, Point po1, Point po2, Point po3, Point po4) {
			checkRotationAngle(p1, p2, p3, p4, po1, po2, po3, po4);
		}
		public void testDetectionZoneAspectRatio(Point p1, Point p2, Point p4, Point po1, Point po2, Point po4) {
			checkDetectionZoneAspectRatio(p1, p2, p4, po1, po2, po4);
		}
	}
	
	@Test(groups={"ut"})
	public void testGetAngleBetweenVectors90() {
		SubImageDetector detector = new SubImageDetector();
		Assert.assertEquals(detector.testAngle(new Point(0, 10), new Point(10, 10), new Point(0, 10), new Point(0, 0)), 90);
	}
	
	@Test(groups={"ut"})
	public void testGetAngleBetweenVectors89() {
		SubImageDetector detector = new SubImageDetector();
		Assert.assertEquals(detector.testAngle(new Point(0, 10), new Point(10, 10), new Point(0, 10), new Point(0.174, 0)), 90);
	}
	
	@Test(groups={"ut"})
	public void testGetAngleBetweenVectors88() {
		SubImageDetector detector = new SubImageDetector();
		Assert.assertEquals(detector.testAngle(new Point(0, 10), new Point(10, 10), new Point(0, 10), new Point(0.349, 0)), 88);
	}
	
	@Test(groups={"ut"})
	public void testGetAngleBetweenVectors91() {
		SubImageDetector detector = new SubImageDetector();
		Assert.assertEquals(detector.testAngle(new Point(0, 10), new Point(10, 10), new Point(0, 10), new Point(-0.174, 0)), 90);
	}
	
	@Test(groups={"ut"})
	public void testGetAngleBetweenVectors92() {
		SubImageDetector detector = new SubImageDetector();
		Assert.assertEquals(detector.testAngle(new Point(0, 10), new Point(10, 10), new Point(0, 10), new Point(-0.349, 0)), 92);
	}
	
	@Test(groups={"ut"})
	public void testCheckRotationAngle0() {
		Point po1 = new Point(0, 0);
		Point po2 = new Point(2, 0);
		Point po3 = new Point(2, 1);
		Point po4 = new Point(0, 1);
		Point p1 = new Point(0, 0);
		Point p2 = new Point(2, 0);
		Point p3 = new Point(2, 1);
		Point p4 = new Point(0, 1);
		SubImageDetector detector = new SubImageDetector();
		detector.testRotationAngle(p1, p2, p3, p4, po1, po2, po3, po4);
		Assert.assertEquals(detector.getRotationAngle(), 0);
	}
	
	/**
	 * Error is raised when detected zone is not a rectangle
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void testCheckNonRectangleDetectedZone() {
		Point po1 = new Point(0, 0);
		Point po2 = new Point(2, 0);
		Point po3 = new Point(2, 1);
		Point po4 = new Point(0, 1);
		Point p1 = new Point(0, 0);
		Point p2 = new Point(2, 0);
		Point p3 = new Point(2.1, 1);
		Point p4 = new Point(0, 1);
		SubImageDetector detector = new SubImageDetector();
		detector.testRotationAngle(p1, p2, p3, p4, po1, po2, po3, po4);
	}
	
	/**
	 * Error should be raised as rotation angle is 45°
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void testCheckRotationAngle45() {
		Point po1 = new Point(0, 0);
		Point po2 = new Point(2, 0);
		Point po3 = new Point(2, 1);
		Point po4 = new Point(0, 1);
		Point p1 = new Point(0, 0);
		Point p2 = new Point(2, 2);
		Point p3 = new Point(1, 3);
		Point p4 = new Point(-1, 1);
		SubImageDetector detector = new SubImageDetector();
		detector.testRotationAngle(p1, p2, p3, p4, po1, po2, po3, po4);
	}
	
	/**
	 * Rotation 0°, aspect ratio of found image: 1
	 */
	@Test(groups={"ut"})
	public void testDetectionZoneAspectRatio1() {
		Point po1 = new Point(0, 0);
		Point po2 = new Point(2, 0);
		Point po4 = new Point(0, 1);
		Point p1 = new Point(0, 0);
		Point p2 = new Point(2, 0);
		Point p4 = new Point(0, 1);
		SubImageDetector detector = new SubImageDetector();
		detector.setRotationAngle(0);
		detector.testDetectionZoneAspectRatio(p1, p2, p4, po1, po2, po4);
		Assert.assertEquals(detector.getSizeRatio(), 1.0, 0.01);
	}
	
	/**
	 * Rotation 90°, aspect ratio of found image: 2
	 */
	@Test(groups={"ut"})
	public void testDetectionZoneAspectRatio2() {
		Point po1 = new Point(0, 0);
		Point po2 = new Point(2, 0);
		Point po4 = new Point(0, 1);
		Point p1 = new Point(0, 4);
		Point p2 = new Point(0, 0);
		Point p4 = new Point(2, 4);
		SubImageDetector detector = new SubImageDetector();
		detector.setRotationAngle(90);
		detector.testDetectionZoneAspectRatio(p1, p2, p4, po1, po2, po4);
		Assert.assertEquals(detector.getSizeRatio(), 2.0, 0.01);
	}
	
	/**
	 * Rotation 0°, aspect ratio of found image: 1
	 */
	@Test(groups={"ut"}, expectedExceptions=ImageSearchException.class)
	public void testDetectionZoneDifferentAspectRatio() {
		Point po1 = new Point(0, 0);
		Point po2 = new Point(2, 0);
		Point po4 = new Point(0, 1);
		Point p1 = new Point(0, 0);
		Point p2 = new Point(3, 0);
		Point p4 = new Point(0, 1);
		SubImageDetector detector = new SubImageDetector();
		detector.setRotationAngle(0);
		detector.testDetectionZoneAspectRatio(p1, p2, p4, po1, po2, po4);
	}
}
