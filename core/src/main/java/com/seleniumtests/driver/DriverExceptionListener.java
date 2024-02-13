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
package com.seleniumtests.driver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.events.WebDriverListener;

import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class DriverExceptionListener implements WebDriverListener {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(DriverExceptionListener.class);

	private final CustomEventFiringWebDriver driver;
	
	public DriverExceptionListener(CustomEventFiringWebDriver driver) {
		this.driver = driver;
	}

    @Override
    public void beforeClick(WebElement arg0) {
		driver.updateWindowsHandles();
    }

    @Override
    public void onError(Object target, Method method, Object[] args, InvocationTargetException e) {
    	
    	Throwable ex = e.getCause();
    	
        if (ex.getMessage() == null
        		|| ex.getMessage().contains("Element must be user-editable in order to clear it")
        		|| ex.getMessage().contains("Element is not clickable at point")
        		|| ex instanceof NoAlertPresentException
        		|| ex instanceof UnsupportedCommandException
        		|| ex.getMessage().contains(" read-only")
        		|| ex.getMessage().contains("not implemented")
        		|| ex instanceof org.openqa.selenium.UnsupportedCommandException
        		|| ex instanceof MoveTargetOutOfBoundsException  // exception raised when element is non clickable
        		|| ex instanceof InvalidElementStateException    // exception raised when element is non clickable
        		) {
            // do nothing
            
        // Edge driver does return a WebDriverException when doing getPageSource
        } else if (ex.getMessage().contains("No response on ECMAScript evaluation command")) { // Opera

            // customexception
            for (int i = 0; i < ex.getStackTrace().length; i++) {
                String methodInEx = ex.getStackTrace()[i].getMethodName();
                if (methodInEx.contains("getTitle") || methodInEx.contains("getWindowHandle") || methodInEx.contains("click")
                        || methodInEx.contains("getPageSource")) {
                    return;
                }
            }

            logger.error(ex);

        } else if (ex.getMessage().contains("Error communicating with the remote browser. It may have died.")) {

            // Session has lost connection, remove it then ignore quit() method.
        	driver.setDriverExited();
            throw new WebSessionEndedException(ex);

        } else if (ex instanceof NoSuchWindowException) {
        	try {
	        	List<String> handles = new ArrayList<>(driver.getWindowHandles());
	        	
	        	if (!handles.isEmpty()) {
	        		try {
	        			driver.switchTo().window(handles.get(handles.size() - 1));
	        			logger.info("Current window has been closed, switching to previous window to avoid problems in future commands");
	        		} catch (IndexOutOfBoundsException | NoSuchWindowException e1) {
	        			driver.switchTo().window(handles.get(0));
	        			logger.info("Current window has been closed, switching to first window to avoid problems in future commands");
	            	}
	        	} 
        	} catch (Exception e1) {
        		// ignore, do not raise exception during handling it
        	}

        } else {
            String message = ex.getMessage().split("\\n")[0];
            logger.warn("Got exception:" + message);
            if (
            		ex instanceof org.openqa.selenium.remote.UnreachableBrowserException
            		|| ex instanceof NoSuchSessionException
            		|| message.matches("Session .*? was terminated due to.*")
            		|| message.matches("Session .*? not available .*")
                    || message.matches("cannot forward the request .*")
                    || message.matches("Session is closed")
                    || message.contains("Unable to get browser")
                    || message.contains("not reachable")
                    || message.contains("Tried to run command without establishing a connection")
                    || message.matches("Session ID is null.*")
                    || message.contains("java.net.ConnectException: Failed to connect")
                    || message.contains("java.net.ConnectException: Connection refused")
                    ) {
            	
            	driver.setDriverExited();

                // since the session was
                // terminated.
                throw new WebSessionEndedException(ex);
                
            // issue #281: chrome < 73: WebDriverException is raised instead of ElementClickInterceptedException
            } else if (message.contains("Other element would receive the click")) {
            	throw new ElementClickInterceptedException(message);
            }
        }

    }

}
