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
package com.seleniumtests.driver;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for performing actions that cannot be handled by Selenium
 * - do actions by robot class (Keyboard and Mouse)
 * - Take desktop capture
 * @author worm
 *
 */
public class LocalSeleniumHostUtility extends SeleniumHostUtility {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(LocalSeleniumHostUtility.class);


	/**
	 * Take screenshot of the desktop and put it in a file
	 */
	public BufferedImage captureDesktopToBuffer() {
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultGraphicDevice = ge.getDefaultScreenDevice();
		Integer screenWidth = defaultGraphicDevice.getDisplayMode().getWidth();
		Integer screenHeight = defaultGraphicDevice.getDisplayMode().getHeight();
		
		// Capture the screen shot of the area of the screen defined by the rectangle
		try {
			return new Robot().createScreenCapture(new Rectangle(screenWidth, screenHeight));
		} catch (AWTException e) {
			throw new ScenarioException("Cannot capture image", e);
		}
	}
	
	public void uploadFile(String filePath) {

		// Copy to clipboard
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(filePath), null);
		Robot robot;
		try {
			robot = new Robot();
		
			WaitHelper.waitForSeconds(1);
	
			// Press Enter
			robot.keyPress(KeyEvent.VK_ENTER);
	
			// Release Enter
			robot.keyRelease(KeyEvent.VK_ENTER);
	
			// Press CTRL+V
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
	
			// Release CTRL+V
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_V);
			WaitHelper.waitForSeconds(1);
	
			// Press Enter
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
		} catch (AWTException e) {
			throw new ScenarioException("could not initialize robot to upload file: " + e.getMessage());
		}
	}

}
