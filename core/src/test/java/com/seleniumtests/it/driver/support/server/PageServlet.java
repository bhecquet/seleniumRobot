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
package com.seleniumtests.it.driver.support.server;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
			if (this.resourceFile.endsWith(".css")) {
				resp.addHeader("Content-Type", "text/css ");
			}
			if (this.resourceFile.endsWith(".js")) {
				resp.addHeader("Content-Type", "text/javascript");
			}
			IOUtils.copy(getClass().getResourceAsStream(this.resourceFile), resp.getOutputStream());
		} catch (IOException e) {
        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error while handling request: " + e.getMessage());
        }
    }
 
}
