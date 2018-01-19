package com.seleniumtests.it.driver.support.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;


public class PageServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String resourceFile;
	
	/**
	 * The file to expose. This must be stored in resources
	 * @param resourceFile
	 */
	public PageServlet(String resourceFile) {
		this.resourceFile = resourceFile;
	}

	/**
     * Allow downloading of files in upload folder
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			IOUtils.copy(getClass().getResourceAsStream(this.resourceFile), resp.getOutputStream());
		} catch (IOException e) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error while handling request: " + e.getMessage());
        }
    }
 
}
