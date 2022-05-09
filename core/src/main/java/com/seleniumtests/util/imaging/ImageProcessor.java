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
package com.seleniumtests.util.imaging;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.seleniumtests.util.FileUtility;

import gherkin.deps.net.iharder.Base64;

public class ImageProcessor {

	private ImageProcessor() {
		// do nothing
	}
	
	/**
	 * Load picture from file
	 * @param file
	 * @return l'image chargée
	 * @throws IOException
	 */
	public static BufferedImage loadFromFile(File file) throws IOException {
		return ImageIO.read(file);
	}
	
	public static BufferedImage loadFromFile(byte[] imgData) throws IOException {
		InputStream in = new ByteArrayInputStream(imgData);  
		return ImageIO.read(in);  
	}
	
	public static BufferedImage loadFromB64String(String b64String) throws IOException {
		byte[] byteArray = b64String.getBytes();
        byte[] decodeBuffer = org.apache.commons.codec.binary.Base64.decodeBase64(byteArray);
        return loadFromFile(decodeBuffer);
	}
	
	public static String toBase64(BufferedImage img) throws IOException {   
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
        OutputStream b64 = new Base64.OutputStream(os);
        ImageIO.write(img, "png", b64);
        return os.toString("UTF-8"); 
	}
	
	public static void drawRectangles(File file, Color color, Rectangle ... rectangles) throws IOException {
		BufferedImage bi = loadFromFile(file);
		Graphics graph = bi.getGraphics();
		graph.setColor(color);
		for (Rectangle rect: rectangles) {
			graph.drawRect(rect.x, rect.y, rect.width, rect.height);
			graph.drawRect(rect.x+1, rect.y+1, rect.width - 2,rect.height - 2);
		}
		FileUtility.writeImage(file.getAbsolutePath(), bi);
		
	}
	
	public static void drawLines(File file, Color color, Line2D.Double ... lines) throws IOException {
		BufferedImage bi = loadFromFile(file);
		Graphics2D graph = (Graphics2D)bi.getGraphics();
		graph.setColor(color);
		for (Line2D line: lines) {
			graph.draw(line);
			graph.draw(new Line2D.Double(line.getX1(), line.getY1() + 1, line.getX2(), line.getY2() + 1));
		}
		FileUtility.writeImage(file.getAbsolutePath(), bi);
		
	}
	
	/**
	 * cut part of an image
	 * @param img		source image
	 * @param cropX		x coord of the top left point
	 * @param cropY		y coord of the top left point
	 * @param width		width of picture portion to keep
	 * @param height	height of picture portion to keep
	 * @return 			cut image
	 */
	public static BufferedImage cropImage(BufferedImage img, Integer cropX, Integer cropY, Integer width, Integer height) {
		
		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		newImg.createGraphics().drawImage(img, 0, 0, width, height, cropX, cropY, cropX + width, cropY + height, null);

		return newImg;
	}
	
	/**
	 * Agregate 2 pictures
	 * @param imgf1			first picture to agregate
	 * @param imgf2			seconde picture to aggregate
	 * @param finalImage	file to create
	 * @param img2PosX		X coord where second picture will be but, relative to first one 
	 * @param img2PosY		Y coord where second picture will be but, relative to first one 
	 */
	public static void concat(File imgf1, File imgf2, File finalImage, Integer img2PosX, Integer img2PosY) {
		
		try {
			BufferedImage img1 = ImageIO.read(imgf1);
			BufferedImage img2 = ImageIO.read(imgf2);
			ImageIO.write(concat(img1, img2, img2PosX, img2PosY), "jpg", finalImage);
		} catch (IOException e) {
		}
	}
	
	/**
	 * Agregate 2 pictures
	 * @param imgf1			first picture to agregate
	 * @param imgf2			seconde picture to aggregate
	 * @param img2PosX		X coord where second picture will be but, relative to first one 
	 * @param img2PosY		Y coord where second picture will be but, relative to first one 
	 * @return				the complete picture
	 */
	public static BufferedImage concat(BufferedImage img1, BufferedImage img2, Integer img2PosX, Integer img2PosY) {
		
		if (img2PosX < 0 || img2PosY < 0) {
			throw new IllegalArgumentException("relative position must be > 0");
		}
		
		Integer finalWidth = Math.max(img2.getWidth() + img2PosX, img1.getWidth());
		Integer finalHeight = Math.max(img2.getHeight() + img2PosY, img1.getHeight());
		
		BufferedImage img = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
		
		img.createGraphics().drawImage(img1, 0, 0, null);
		img.createGraphics().drawImage(img2, img2PosX, img2PosY, null);

		return img;
	}
}
