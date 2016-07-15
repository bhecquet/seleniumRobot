/*
 * Copyright 2015 www.seleniumtests.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.driver;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * Holds information about a JavaScript errorLogger that has occurred in the browser. This can be currently only used
 * with the {@link FirefoxDriver} (see {@link #addExtension(FirefoxProfile)}.
 *
 * @author   Marc Guillemot
 * @version  $Revision: $
 */
public class JavaScriptError {
    private final String errorMessage;
    private final String sourceName;
    private final int lineNumber;
    private final String console;

    JavaScriptError(final Map<String, ? extends Object> map) {
        errorMessage = (String) map.get("errorMessage");
        sourceName = (String) map.get("sourceName");
        lineNumber = ((Number) map.get("lineNumber")).intValue();
        console = (String) map.get("console");
    }

    JavaScriptError(final String errorMessage, final String sourceName, final int lineNumber, final String console) {
        this.errorMessage = errorMessage;
        this.sourceName = sourceName;
        this.lineNumber = lineNumber;
        this.console = console;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSourceName() {
        return sourceName;
    }

    /**
     * If Firebug is installed and active, this will contain the content of the Firebug Console since the previous
     * JavaScript errorLogger.
     *
     * @return console value
     */
    public String getConsole() {
        return console;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((console == null) ? 0 : console.hashCode());
        result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
        result = prime * result + lineNumber;
        result = prime * result + ((sourceName == null) ? 0 : sourceName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        JavaScriptError other = (JavaScriptError) obj;
        if (console == null) {
            if (other.console != null) {
                return false;
            }
        } else if (!console.equals(other.console)) {
            return false;
        }

        if (errorMessage == null) {
            if (other.errorMessage != null) {
                return false;
            }
        } else if (!errorMessage.equals(other.errorMessage)) {
            return false;
        }

        if (lineNumber != other.lineNumber) {
            return false;
        }

        if (sourceName == null) {
            if (other.sourceName != null) {
                return false;
            }
        } else if (!sourceName.equals(other.sourceName)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        String s = errorMessage + " [" + sourceName + ":" + lineNumber + "]";
        if (console != null) {
            s += "\nConsole: " + console;
        }

        return s;
    }

    /**
     * Gets the collected JavaScript errors that have occurred since last call to this method.
     *
     * @param   driver  the driver providing the possibility to retrieved JavaScript errors (see
     *                  {@link #addExtension(FirefoxProfile)}.
     *
     * @return  the errors or an empty list if the driver doesn't provide access to the JavaScript errors
     */
    @SuppressWarnings("unchecked")
    public static List<JavaScriptError> readErrors(final WebDriver driver) {
        final String script = "return window.JSErrorCollector_errors ? window.JSErrorCollector_errors.pump() : []";
        final List<Object> errors = (List<Object>) ((JavascriptExecutor) driver).executeScript(script);
        final List<JavaScriptError> response = new ArrayList<>();
        for (final Object rawError : errors) {
            response.add(new JavaScriptError((Map<String, ? extends Object>) rawError));
        }

        return response;
    }

    /**
     * Adds the Firefox extension collecting JS errors to the profile what allows later use of
     * {@link #readErrors(WebDriver)}.
     *
     * <p>Example:<br>
     * <code>
       final FirefoxProfile profile = new FirefoxProfile();
       JavaScriptError.addExtension(profile);
       final WebDriver driver = new FirefoxDriver(profile);
     * </code>
     *
     * @param   ffProfile  the Firefox profile to which the extension should be added.
     *
     * @throws  IOException  in case of problem
     */
    public static void addExtension(final FirefoxProfile ffProfile) throws IOException {
        ffProfile.addExtension(JavaScriptError.class, "JSErrorCollector.xpi");
    }
}
