package com.seleniumtests.connectors.selenium;

import java.awt.Point;
import java.io.File;
import java.util.List;

import org.openqa.selenium.Capabilities;

public interface ISeleniumGridConnector {

	public void uploadMobileApp(Capabilities caps);
	
	/**
	 * Upload a file given file path
	 * @param filePath
	 */
	public void uploadFile(String filePath);
	
	/**
	 * Upload a file given file path
	 * @param filePath
	 */
	public String uploadFileToNode(String filePath, boolean returnLocalFile);
	
	/**
	 * Download a file given file path
	 * @param filePath
	 */
	public File downloadFileFromNode(String filePath);
	/**
	 * Kill process
	 * @param processName
	 */
	public void killProcess(String processName);
	
	/**
	 * Execute command
	 * @param program	name of the program
	 * @param args		arguments of the program
	 */
	public String executeCommand(String program, String ... args);
	/**
	 * Execute command with timeout
	 * @param program	name of the program
	 * @param timeout	if null, default timeout will be applied
	 * @param args		arguments of the program
	 */
	public String executeCommand(String program, Integer timeout, String ... args) ;

	/**
	 * Upload a file to a browser uplpoad window
	 * @param filePath
	 */
	public void uploadFileToBrowser(String fileName, String base64Content) ;
	
	/**
	 * Left clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void leftClic(int x, int y) ;
	
	/**
	 * double clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void doubleClick(int x, int y) ;
	
	/**
	 * Get position of mouse pointer
	 * @return
	 */
	public Point getMouseCoordinates();
	
	/**
	 * right clic on desktop at x,y
	 * @param x		x coordinate
	 * @param y		y coordinate
	 */
	public void rightClic(int x, int y) ;
	
	/**
	 * Take screenshot of the full desktop and return a base64 string of the image
	 * @return
	 */
	public String captureDesktopToBuffer() ;
	
	/**
	 * Send keys to desktop
	 * @param keys
	 */
	public void sendKeysWithKeyboard(List<Integer> keyCodes);
	
	public void startVideoCapture();
	public File stopVideoCapture(String outputFile) ;

	public List<Integer> getProcessList(String processName);
	
	/**
	 * 
	 * @return true if grid is active. Raises an exception if it's not there anymore
	 */
	public boolean isGridActive();
	
	/**
	 * Write text to desktop using keyboard
	 * @param text
	 */
	public void writeText(String text);

}
