/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class StringUtility {

	private static final Logger logger = SeleniumRobotLogger.getLogger(StringUtility.class);
	public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.*?)}");

	private StringUtility() {
		// As a utility class, it is not meant to be instantiated.
	}

    public static String md5(final String str) {

        if (str == null) {
            return null;
        }

        MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
        	logger.error(e);
            return str;
        }

        byte[] byteArray = messageDigest.digest();

        return toHexString(byteArray);
    }

    public static String toHexString(byte[] byteArray) {
        StringBuilder builder = new StringBuilder();

        for (byte b : byteArray) {
            if (Integer.toHexString(0xFF & b).length() == 1)
                builder.append("0").append(String.format("%02X", b));
            else
                builder.append(String.format("%02X", b));
        }

        return builder.toString();
    }

	/**
	 * Replace chars that cannot be used for file names
	 * @return a String without invalid chars
	 */
	public static String replaceOddCharsFromFileName(String inString) {
		if (inString == null) {
			return "null";
		}

		return StringUtils.stripAccents(inString).replace(" ",  "_")
				.replaceAll("['\"/|%]", "")
				.replaceAll("[:*?]", ".")
				.replaceAll("[<>]", "-")
				.replace("\\", "_");
	}

	/**
     * Do interpolation like groovy language, using context variables
     * ex: provided the 'url' variable is present in test configuration with value '<a href="http://my.site">...</a>',
     *     'connect to ${url}' => 'connect to <a href="http://my.site">...</a>
     * If testContext is set to mask password, then, they will be replaced by '****'
     *
     * @param initialString        the string to interpolate
     * @return the interpolated string
     */
	public static String interpolateString(String initialString, SeleniumTestsContext testContext) {

    	if (testContext == null) {
    		return initialString;
    	}
		return interpolateString(initialString, testContext, testContext.getMaskedPassword());
	}

	/**
     * Do interpolation like groovy language, using context variables
     * ex: provided the 'url' variable is present in test configuration with value '<a href="https://my.site">...</a>',
     *     'connect to ${url}' => 'connect to <a href="https://my.site">...</a>
     *
     * @param initialString        the string to interpolate
     * @return the interpolated string
     */
	public static String interpolateString(String initialString, SeleniumTestsContext testContext, Boolean maskPassword) {
		if (initialString == null) {
			return null;
		}

		String interpolatedString  = initialString;

    	if (testContext == null || testContext.getConfiguration() == null) {
    		return initialString;
    	}    	
    	Map<String, TestVariable> variables = testContext.getFullContextDataMapAsTestVariables();
    	variables.putAll(testContext.getConfiguration());
		List<String> unknownKeys = new ArrayList<>();

    	for (int i = 0; i < 10; i++) {
	    	Matcher matcher = PLACEHOLDER_PATTERN.matcher(interpolatedString);
	    	boolean processed = false;

	    	while (matcher.find()) {
	    		processed = true;
	    		String key = matcher.group(1);

	    		if (Boolean.TRUE.equals(maskPassword) && (key.toLowerCase().contains("password") || key.toLowerCase().contains("pwd") || key.toLowerCase().contains("passwd"))) {
	    			interpolatedString = interpolatedString.replace(String.format("${%s}",  key), "****");
	    		} else if (variables.containsKey(key)) {
	    			interpolatedString = interpolatedString.replace(String.format("${%s}",  key), variables.get(key).getValueNoInterpolation());
	    		} else if (!unknownKeys.contains(key)){
	    			unknownKeys.add(key);
	    			logger.warn("Error while interpolating '{}', key '{}' not found in configuration", interpolatedString, key);
	    		}
	    	}

	    	if (!processed) {
	    		break;
	    	}
    	}

    	return interpolatedString;
	}

	/**
	 * Encode string according to provided format
	 * @param message		message to encode
	 * @param format		'xml', 'csv', 'html', 'json', 'text'. the later does not change anything
	 * @return the encoded string
	 */
	public static String encodeString(String message, String format) {
		String newMessage;

		if (message == null) {
			return null;
		}
		if (format == null) { throw new CustomSeleniumTestsException("only escaping of 'xml', 'html', 'csv', 'json' is allowed"); }

        newMessage = switch (format) {
            case "xml" -> StringEscapeUtils.escapeXml11(message);
            case "csv" -> StringEscapeUtils.escapeCsv(message);
            case "html" -> StringEscapeUtils.escapeHtml4(message.replace("\n", "__BR__")).replace("__BR__", "<br/>\n");
            case "json" -> StringEscapeUtils.escapeJson(message);
            case "text" -> message;
            default ->
                    throw new CustomSeleniumTestsException("only escaping of 'xml', 'html', 'csv', 'json' is allowed");
        };
		return newMessage;
	}


}
