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
package com.seleniumtests.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class StringUtility {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(StringUtility.class);

	private StringUtility() {
		// As a utility class, it is not meant to be instantiated.
	}
	
    public static String constructMethodSignature(final Method method, final Object[] parameters) {
        return method.getDeclaringClass().getCanonicalName() + "." + method.getName() + "("
                + constructParameterString(parameters) + ")";
    }

    public static String constructParameterString(final Object[] parameters) {
        StringBuilder sbParam = new StringBuilder();

        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == null) {
                    sbParam.append("null, ");
                } else if (parameters[i] instanceof java.lang.String) {
                    sbParam.append("\"").append(parameters[i]).append("\", ");
                } else {
                    sbParam.append(parameters[i]).append(", ");
                }
            }
        }

        if (sbParam.length() > 0) {
            sbParam.delete(sbParam.length() - 2, sbParam.length() - 1);
        }

        return sbParam.toString();
    }

    public static String md5(final String str) {

        if (str == null) {
            return null;
        }

        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException|NoSuchAlgorithmException e) {
        	logger.error(e);
            return str;
        }

        byte[] byteArray = messageDigest.digest();

        return toHexString(byteArray);
    }

    public static String toHexString(byte[] byteArray) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                builder.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                builder.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return builder.toString();
    }
	
	/**
	 * @param ch
	 * @return true if the given character is a letter
	 */
	public static boolean isLetter(char ch) {
		int ascii = (int) ch;
		/*
		 * A=65 ; Z=90
		 * a=97 ; z=122
		 */
		if ((ascii >= 65 && ascii <= 90)
			|| (ascii >= 97 && ascii <= 122)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Replace chars that cannot be used for file names 
	 * @return
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
	 * ex: provided the 'url' variable is present in test configuration with value 'http://my.site', 
	 *     'connect to ${url}' => 'connect to http://my.site
	 * 
	 * @param initialString
	 * @return
	 */
	public static String interpolateString(String initialString, SeleniumTestsContext testContext) {
		if (initialString == null) {
			return null;
		}
		
		String interpolatedString  = initialString;
    	Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
    	Matcher matcher = pattern.matcher(initialString);
    	
    	if (testContext == null || testContext.getConfiguration() == null) {
    		return initialString;
    	}
    	Map<String, TestVariable> variables = testContext.getConfiguration();
    	
    	while (matcher.find()) {
    		String key = matcher.group(1);
    		
    		if (Boolean.TRUE.equals(SeleniumTestsContextManager.getThreadContext().getMaskedPassword()) && (key.toLowerCase().contains("password") || key.toLowerCase().contains("pwd") || key.toLowerCase().contains("passwd"))) {
    			interpolatedString = interpolatedString.replace(String.format("${%s}",  key), "****");
    		} else if (variables.containsKey(key)) {
    			interpolatedString = interpolatedString.replace(String.format("${%s}",  key), variables.get(key).getValue());
    		} else {
    			logger.warn(String.format("Error while interpolating, key '%s' not found in configuration", key));
    		}
    		
    	}
    	
    	return interpolatedString;
	}

	/**
	 * Encode string according to provided format
	 * @param message		message to encode
	 * @param format		'xml', 'csv', 'html', 'json', 'text'. the later does not change anything
	 * @return
	 */
	public static String encodeString(String message, String format) {
		String newMessage;
		switch (format) {
		case "xml":
			newMessage = StringEscapeUtils.escapeXml11(message);
			break;
		case "csv":
			newMessage = StringEscapeUtils.escapeCsv(message);
			break;
		case "html":
			if (message == null) {
				newMessage = null;
			} else {
				newMessage = StringEscapeUtils.escapeHtml4(message.replace("\n", "__BR__")).replace("__BR__", "<br/>\n");
			}
			break;
		case "json":
			newMessage = StringEscapeUtils.escapeJson(message);
			break;
		case "text":
			newMessage = message;
			break;
		default:
			throw new CustomSeleniumTestsException("only escaping of 'xml', 'html', 'csv', 'json' is allowed");
		}
		return newMessage;
	}
	
	
}
