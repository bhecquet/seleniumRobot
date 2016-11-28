package com.seleniumtests.util.imaging;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.openqa.selenium.Rectangle;

import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class made for detecting an image inside an other one
 * It uses openCV to look for the objectImage inside the sceneImage
 * Rotations and resizing are supported
 * @author behe
 *
 */
public class ImageDetector {
	
	private Rectangle detectedRectangle;
	private boolean computed = false;
	private long rotationAngle;
	private File sceneImage;
	private File objectImage;
	private boolean debug = false;
	private double detectionThreshold = 0.05;
	private Mat imgMatch = new Mat();
	private double sizeRatio;
	private static Logger logger = SeleniumRobotLogger.getLogger(ImageDetector.class);
	
	// load openCV
	// In case of "UnsatisfiedLinkError, library already loaded in another class loader", during unit tests, check that 
	// this class or a calling one is not "prepared" through PowerMockito (which reloads the class in another class loader)
	static {
		nu.pattern.OpenCV.loadShared();
	}
	
	public ImageDetector() {
		// do nothing, only for test
	}
	
	public ImageDetector(File sceneImage, File objectImage) {
		this(sceneImage, objectImage, 0.05);
	}
	
	public ImageDetector(File sceneImage, File objectImage, double detectionThreshold) {
		setSceneImage(sceneImage);
		setObjectImage(objectImage);
		this.detectionThreshold = detectionThreshold;
	}
	
	/**
	 * Compute the rectangle where the searched picture is and the rotation angle between both images
	 * Throw {@link ImageSearchException} if picture is not found
	 * @return
	 */
	public void detectCorrespondingZone() {
		Mat objectImageMat = Highgui.imread(objectImage.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
		Mat sceneImageMat = Highgui.imread(sceneImage.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
		FeatureDetector surf = FeatureDetector.create(FeatureDetector.SURF);
		
		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		
		surf.detect(objectImageMat, objectKeyPoints);
		surf.detect(sceneImageMat, sceneKeyPoints);
		
		DescriptorExtractor surfExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		Mat objectDescriptor = new Mat();
		Mat sceneDescriptor = new Mat();
		surfExtractor.compute(objectImageMat, objectKeyPoints, objectDescriptor);
		surfExtractor.compute(sceneImageMat, sceneKeyPoints, sceneDescriptor);
		
		// http://stackoverflow.com/questions/29828849/flann-for-opencv-java
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match( objectDescriptor, sceneDescriptor, matches );
		
		double maxDist = 0; 
		double minDist = 100;
		
		for( int i = 0; i < objectDescriptor.rows(); i++ ) { 
			double dist = matches.toList().get(i).distance;
		    if ( dist < minDist ) {
		    	minDist = dist;
		    }
		    if( dist > maxDist )  {
		    	maxDist = dist;
		    }
		}
		
		logger.debug("-- Max dist : " + maxDist);
		logger.debug("-- Min dist : " + minDist);
		
		LinkedList<DMatch> goodMatches = new LinkedList<>();
		MatOfDMatch gm = new MatOfDMatch();

		for(int i = 0; i < objectDescriptor.rows(); i++){
		    if(matches.toList().get(i).distance < detectionThreshold){
		        goodMatches.addLast(matches.toList().get(i));
		    }
		}
		gm.fromList(goodMatches);

		Features2d.drawMatches(objectImageMat, objectKeyPoints, sceneImageMat, sceneKeyPoints, 
				gm, imgMatch, Scalar.all(-1), Scalar.all(-1), new MatOfByte(), Features2d.NOT_DRAW_SINGLE_POINTS);
		
		if (goodMatches.isEmpty()) {
			throw new ImageSearchException("Cannot find matching zone");
		}
		
		LinkedList<Point> objList = new LinkedList<>();
		LinkedList<Point> sceneList = new LinkedList<>();

		List<KeyPoint> objectKeyPointsList = objectKeyPoints.toList();
		List<KeyPoint> sceneKeyPointsList = sceneKeyPoints.toList();

		for(int i = 0; i<goodMatches.size(); i++){
		    objList.addLast(objectKeyPointsList.get(goodMatches.get(i).queryIdx).pt);
		    sceneList.addLast(sceneKeyPointsList.get(goodMatches.get(i).trainIdx).pt);
		}

		MatOfPoint2f obj = new MatOfPoint2f();
		obj.fromList(objList);

		MatOfPoint2f scene = new MatOfPoint2f();
		scene.fromList(sceneList);
		
		// Calib3d.RANSAC could be used instead of 0
		Mat hg = Calib3d.findHomography(obj, scene, 0, 5);

		Mat objectCorners = new Mat(4,1,CvType.CV_32FC2);
		Mat sceneCorners = new Mat(4,1,CvType.CV_32FC2);

		objectCorners.put(0, 0, new double[] {0,0});
		objectCorners.put(1, 0, new double[] {objectImageMat.cols(),0});
		objectCorners.put(2, 0, new double[] {objectImageMat.cols(),objectImageMat.rows()});
		objectCorners.put(3, 0, new double[] {0,objectImageMat.rows()});
		
		Core.perspectiveTransform(objectCorners, sceneCorners, hg);
		
		// points of object
		Point po1 = new Point(objectCorners.get(0,0));
		Point po2 = new Point(objectCorners.get(1,0));
		Point po3 = new Point(objectCorners.get(2,0));
		Point po4 = new Point(objectCorners.get(3,0));
		
		// point of object in scene
		Point p1 = new Point(sceneCorners.get(0,0)); // top left
		Point p2 = new Point(sceneCorners.get(1,0)); // top right
		Point p3 = new Point(sceneCorners.get(2,0)); // bottom right
		Point p4 = new Point(sceneCorners.get(3,0)); // bottom left
		
		logger.debug(po1);
		logger.debug(po2);
		logger.debug(po3);
		logger.debug(po4);
		logger.debug(p1); // top left
		logger.debug(p2); // top right
		logger.debug(p3); // bottom right
		logger.debug(p4); // bottom left
		
		if (debug) {
			try {
				// translate corners
				p1.set(new double[] {p1.x + objectImageMat.cols(), p1.y});
				p2.set(new double[] {p2.x + objectImageMat.cols(), p2.y});
				p3.set(new double[] {p3.x + objectImageMat.cols(), p3.y});
				p4.set(new double[] {p4.x + objectImageMat.cols(), p4.y});
				
				Core.line(imgMatch, p1, p2, new Scalar(0, 255, 0),1);
				Core.line(imgMatch, p2, p3, new Scalar(0, 255, 0),1);
				Core.line(imgMatch, p3, p4, new Scalar(0, 255, 0),1);
				Core.line(imgMatch, p4, p1, new Scalar(0, 255, 0),1);
				
				showResultingPicture();
			} catch (IOException e) {
			}
		}
		
		// check rotation angles
		checkRotationAngle(p1, p2, p3, p4, po1, po2, po3, po4);
		
		// rework on scene points as new, we are sure the object rotation is 0, 90, 180 or 270°
		reworkOnScenePoints(p1, p2, p3, p4);
		
		// check that aspect ratio of the detected height and width are the same
		checkDetectionZoneAspectRatio(p1, p2, p4, po1, po2, po4);
		
		recordDetectedRectangle(p1, p2, p3, p4);
	}
	
	/**
	 * returns angle between vectors defined by 2 points
	 * @param vec1p1	first point of first vector
	 * @param vec1p2	second point of first vector
	 * @param vec2p1	first point of second vector
	 * @param vec2p2	second point of second vector
	 * @return	an angle between 0 and 360°
	 */
	protected long getAngleBetweenVectors(Point vec1p1, Point vec1p2, Point vec2p1, Point vec2p2) {
		long realAngle = (Math.round(Math.toDegrees(Math.atan2(vec1p2.y - vec1p1.y, vec1p2.x - vec1p1.x)
				- Math.atan2(vec2p2.y - vec2p1.y, vec2p2.x - vec2p1.x))) + 360) % 360;
		long approximateAngle = (realAngle + 1) % 90;
		
		// for angles near a multiple of 90 (1° of tolerance), return an angle of 0, 90, 180 or 270
		if (approximateAngle < 3) {
			return realAngle - approximateAngle + 1;
		} else {
			return realAngle;
		}
	}
	
	/**
	 * Check that rotation angle between object and zone detected in scene is a multiple of 90°
	 * @param p1	corner corresponding to top left corner of origin possibly rotated
	 * @param p2	corner corresponding to top right corner of origin possibly rotated
	 * @param p3	corner corresponding to bottom right corner of origin possibly rotated
	 * @param p4	corner corresponding to bottom left corner of origin possibly rotated
	 * @param po1	top left corner of object picture
	 * @param po2	top right corner of object picture
	 * @param po3	bottom right corner of object picture
	 * @param po4	bottom left corner of object picture
	 */
	protected void checkRotationAngle(Point p1, Point p2, Point p3, Point p4, Point po1, Point po2, Point po3, Point po4) {
		rotationAngle = getAngleBetweenVectors(p1, p2, po1, po2);
		if (rotationAngle % 90 != 0) {
			throw new ImageSearchException("only rotations of 90, 180 or 270 are supported");
		}
		logger.debug("rotation angle is " + rotationAngle);
		
		// check that the translated zone is a rectangle
		if (getAngleBetweenVectors(p2, p3, po2, po3) != rotationAngle
				||getAngleBetweenVectors(p3, p4, po3, po4) != rotationAngle 
				||getAngleBetweenVectors(p4, p1, po4, po1) != rotationAngle 
				) {
			throw new ImageSearchException("source image transform does not produce a rectangle");
		}
	}
	
	/**
	 * In case angles are not strictly multiples of 90°, move points so that we have a real rectangle
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param p4
	 */
	protected void reworkOnScenePoints(Point p1, Point p2, Point p3, Point p4) {
		if (rotationAngle == 0 || rotationAngle == 180) {
			p1.y = p2.y = (p1.y + p2.y) / 2;
			p3.y = p4.y = (p3.y + p4.y) / 2;
			p1.x = p4.x = (p1.x + p4.x) / 2;
			p2.x = p3.x = (p2.x + p3.x) / 2;
		} else {
			p1.y = p4.y = (p1.y + p4.y) / 2;
			p2.y = p3.y = (p3.y + p2.y) / 2;
			p1.x = p2.x = (p1.x + p2.x) / 2;
			p4.x = p3.x = (p4.x + p3.x) / 2;
		}
	}
	
	/**
	 * Check aspect ratio between the searched picture (object) and the detected zone in the scene picture
	 * Width and Height ratios must be the same
	 * @param p1	corner corresponding to top left corner of origin possibly rotated
	 * @param p2	corner corresponding to top right corner of origin possibly rotated
	 * @param p4	corner corresponding to bottom left corner of origin possibly rotated
	 * @param po1	top left corner of object picture
	 * @param po2	top right corner of object picture
	 * @param po4	bottom left corner of object picture
	 */
	protected void checkDetectionZoneAspectRatio(Point p1, Point p2, Point p4, Point po1, Point po2, Point po4) {
		double widthRatio;
		double heightRatio;
		if (rotationAngle == 90 || rotationAngle == 270) {
			widthRatio = Math.abs(p1.y - p2.y) / Math.abs(po1.x - po2.x);
			heightRatio = Math.abs(p1.x - p4.x) / Math.abs(po1.y - po4.y);
			
		} else {
			widthRatio = Math.abs(p1.x - p2.x) / Math.abs(po1.x - po2.x);
			heightRatio = Math.abs(p1.y - p4.y) / Math.abs(po1.y - po4.y);
			
		}
		if (Math.abs(widthRatio - heightRatio) > 0.1) {
			throw new ImageSearchException("Aspect ratio between source and detected image is not the same");
		} else {
			logger.debug("Transform ratio is " + Math.round(widthRatio * 100) / 100.0);
			sizeRatio = widthRatio;
		}
	}
	
	/**
	 * Record detected zone as a rectangle
	 * Take into account the rotating angle so that resulting rectangle correspond to points (origin point depends on rotation)
	 * @param p1	corner corresponding to top left corner of origin possibly rotated
	 * @param p2	corner corresponding to top right corner of origin possibly rotated
	 * @param p3	corner corresponding to bottom right corner of origin possibly rotated
	 * @param p4	corner corresponding to bottom left corner of origin possibly rotated
	 */
	protected void recordDetectedRectangle(Point p1, Point p2, Point p3, Point p4) {
		switch ((int)rotationAngle) {
		case 0:
			detectedRectangle = new Rectangle((int)p1.x, (int)p1.y, (int)Math.abs(p4.y - p1.y), (int)Math.abs(p2.x - p1.x));
			break;
		case 90:
			detectedRectangle = new Rectangle((int)p4.x, (int)p4.y, (int)Math.abs(p3.y - p4.y), (int)Math.abs(p1.x - p4.x));
			break;
		case 180:
			detectedRectangle = new Rectangle((int)p3.x, (int)p3.y, (int)Math.abs(p2.y - p3.y), (int)Math.abs(p4.x - p3.x));
			break;
		case 270:
			detectedRectangle = new Rectangle((int)p2.x, (int)p2.y, (int)Math.abs(p1.y - p2.y), (int)Math.abs(p3.x - p2.x));
			break;
		default:
			break;
		}
	}
	
	private void showResultingPicture() throws IOException {
		String tempFile = File.createTempFile("img", ".png").getAbsolutePath();
		writeComparisonPictureToFile(tempFile);
		showResultingImage(tempFile);
	}
	
	/**
	 * File path should end with an image extension (jpg, png)
	 * @param filePath
	 */
	public void writeComparisonPictureToFile(String filePath) {
		if (filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".png")) {
			Highgui.imwrite(filePath, imgMatch);
		} else {
			throw new ImageSearchException("only .JPG and .PNG files are supported");
		}
	}
	
	/**
	 * Method to display the result of image detection
	 * @param imgStr
	 * @param m
	 */
	public void showResultingImage(String filePath) {
		JFrame frame = new JFrame("My GUI");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
	
		// Inserts the image icon
		ImageIcon image = new ImageIcon(filePath);
		frame.setSize(image.getIconWidth()+10,image.getIconHeight()+35);
		// Draw the Image data into the BufferedImage
		JLabel label1 = new JLabel(" ", image, JLabel.CENTER);
		frame.getContentPane().add(label1);
	
		frame.validate();
		frame.setVisible(true);
	}
	

	public Rectangle getDetectedRectangle() {
		return detectedRectangle;
	}

	public boolean isComputed() {
		return computed;
	}

	public long getRotationAngle() {
		return rotationAngle;
	}

	public void setRotationAngle(long rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	/**
	 * Returns the ratio between the detected image (in scene) and the source image (to find)
	 * @return
	 */
	public double getSizeRatio() {
		return sizeRatio;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setSceneImage(File sceneImage) {
		if (!sceneImage.exists()) {
			throw new ImageSearchException(String.format("File for object to detect %s does not exist", sceneImage));
		}
		this.sceneImage = sceneImage;
	}

	public void setObjectImage(File objectImage) {
		if (!objectImage.exists()) {
			throw new ImageSearchException(String.format("File for scene to detect object in %s does not exist", objectImage));
		}
		this.objectImage = objectImage;
	}
}
