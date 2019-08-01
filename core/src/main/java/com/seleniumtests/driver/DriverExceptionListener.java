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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.ScreenshotUtil.Target;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class DriverExceptionListener implements WebDriverEventListener {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(DriverExceptionListener.class);
	
	@Override
    public void afterChangeValueOf(final WebElement element, final WebDriver driver, CharSequence[] arg2) {
    	// do nothing
    }
	
	@Override
    public void afterClickOn(final WebElement arg0, final WebDriver driver) {
		// do nothing
	}

	@Override
    public void afterFindBy(final By arg0, final WebElement arg1, final WebDriver arg2) {
		// do nothing
	}

	@Override
    public void afterNavigateBack(final WebDriver arg0) {
		// do nothing
	}

	@Override
    public void afterNavigateForward(final WebDriver arg0) {
		// do nothing
	}

	@Override
    public void afterNavigateTo(final String arg0, final WebDriver arg1) {
		// do nothing
	}

    @Override
    public void afterScript(final String arg0, final WebDriver arg1) {
    	// do nothing
    }

	@Override
	public void beforeChangeValueOf(WebElement arg0, WebDriver arg1, CharSequence[] arg2) {
		// do nothing
		
	}

    @Override
    public void beforeClickOn(final WebElement arg0, final WebDriver driver) {
    	if (SeleniumTestsContextManager.isWebTest()) {
    		((CustomEventFiringWebDriver)WebUIDriver.getWebDriver(false)).updateWindowsHandles();
    	}
    }

    @Override
    public void beforeFindBy(final By arg0, final WebElement arg1, final WebDriver arg2) {
    	// do nothing
    }

    @Override
    public void beforeNavigateBack(final WebDriver arg0) {
    	// do nothing
    }

    @Override
    public void beforeNavigateForward(final WebDriver arg0) {
    	// do nothing
    }

    @Override
    public void beforeNavigateTo(final String arg0, final WebDriver arg1) {
    	// do nothing
    }

    @Override
    public void beforeScript(final String arg0, final WebDriver arg1) {
    	// do nothing
    }
    
    @Override
    public void afterNavigateRefresh(WebDriver arg0) {
    	// do nothing
    }

    @Override
	public void beforeNavigateRefresh(WebDriver arg0) {
    	// do nothing
    }

    @Override
    public void onException(final Throwable ex, final WebDriver arg1) {
    	
        if (ex.getMessage() == null) {
            return;
        } else if (ex.getMessage().contains("Element must be user-editable in order to clear it")) {
            return;
        } else if (ex.getMessage().contains("Element is not clickable at point")) {
            return;
        } else if (ex instanceof UnsupportedCommandException) {
            return;
        } else if (ex.getMessage().contains(" read-only")) {
            return;
            
        // Edge driver does return a WebDriverException when doing getPageSource
        } else if (ex.getMessage().contains("not implemented")) {
        	return;
        } else if (ex.getMessage().contains("No response on ECMAScript evaluation command")) { // Opera

            // customexception
            for (int i = 0; i < ex.getStackTrace().length; i++) {
                String method = ex.getStackTrace()[i].getMethodName();
                if (method.contains("getTitle") || method.contains("getWindowHandle") || method.contains("click")
                        || method.contains("getPageSource")) {
                    return;
                }
            }

            logger.error(ex);

        } else if (ex.getMessage().contains("Error communicating with the remote browser. It may have died.")) {

            // Session has lost connection, remove it then ignore quit() method.
            if (WebUIDriver.getWebUIDriver(false).getConfig().getMode() == DriverMode.GRID) {
                WebUIDriver.setWebDriver(null);
                throw new WebSessionEndedException(ex);
            }

            return;
        } else if (ex instanceof org.openqa.selenium.remote.UnreachableBrowserException) {
            return;
        } else if (ex instanceof org.openqa.selenium.UnsupportedCommandException) {
            return;
        } else if (ex instanceof NoSuchWindowException) {
        	try {
	        	WebDriver driver = WebUIDriver.getWebDriver(false);
	        	List<String> handles = new ArrayList<>(driver.getWindowHandles());
	        	logger.info("Current window has been closed, switching to first window to avoid problems in future commands");
	        	if (!handles.isEmpty()) {
	        		driver.switchTo().window(handles.get(0));
	        	} 
        	} catch (Exception e) {}
        	return;
        } else {
            String message = ex.getMessage().split("\\n")[0];
            logger.warn("Got exception:" + message);
            if (message.matches("Session .*? was terminated due to(.|\\n)*")
            		|| message.matches("Session .*? not available .*")
                    || message.matches("cannot forward the request .*")
                    || message.matches("Session is closed")
                    || message.contains("Unable to get browser")
                    || message.matches("Session ID is null.*")
                    || message.contains("java.net.ConnectException: Failed to connect")
                    || message.contains("java.net.ConnectException: Connection refused")
                    ) {
                WebUIDriver.setWebDriver(null); // can't quit anymore, save time.

                // since the session was
                // terminated.
                throw new WebSessionEndedException(ex);
            }
        }

    }

	@Override
	public void afterAlertAccept(WebDriver arg0) {
		// do nothing
	}

	@Override
	public void afterAlertDismiss(WebDriver arg0) {
		// do nothing
	}


	@Override
	public void beforeAlertAccept(WebDriver arg0) {
		// do nothing
	}

	@Override
	public void beforeAlertDismiss(WebDriver arg0) {
		// do nothing
	}

	@Override
	public <X> void afterGetScreenshotAs(OutputType<X> arg0, X arg1) {
		// do nothing
		
	}

	@Override
	public void afterGetText(WebElement arg0, WebDriver arg1, String arg2) {
		// do nothing
		
	}

	@Override
	public void afterSwitchToWindow(String arg0, WebDriver arg1) {
		// do nothing
		
	}

	@Override
	public <X> void beforeGetScreenshotAs(OutputType<X> arg0) {
		// do nothing
		
	}

	@Override
	public void beforeGetText(WebElement arg0, WebDriver arg1) {
		// do nothing
		
	}

	@Override
	public void beforeSwitchToWindow(String arg0, WebDriver arg1) {
		// do nothing
		
	}	
}
