/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

package com.seleniumtests.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;

import com.seleniumtests.reporter.TestLogging;

public class StringUtility {
	
	private static final Logger logger = TestLogging.getLogger(StringUtility.class);

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
     * Checks if the input String is Whitespace only.
     */
    public static boolean isWhitespaceStr(String str) {

        if (str == null) {
            return false;
        }

        str = str.trim();

        for (int i = 0; i < str.length(); i++) {

            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets Platform independent line separator (new line character(s).
     */
    public static String getNewlineStr() {

        return System.getProperty("line.separator");
    }
    
    /**
     * Gather String of a list, and separate them with a backspace
     * @param stringList
     * @return String
     */
    public static String fromListToString(List<String> stringList) {
    	StringBuilder sb = new StringBuilder();
    	for (String line : stringList) {
    		sb.append(line + "\n");
    	}
    	return sb.toString();
    }
    
    /**
	 * @param key
	 * @param line
	 * @return true if the key is found with no prefix and no suffix in the given line.
	 */
	public static boolean existsAlone(String key, String line){
		
		int startIndex = findStart(key, line); 
		
		if (line.contains(key)) {
			// word exists
			if (startIndex == 0 || !isLetter(line.charAt(startIndex-1))) {
				// no prefix
				if (startIndex + key.length() == line.length()
					|| !isLetter(line.charAt(startIndex+key.length()))) {
					// no suffix
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param line : String
	 * @param key : String to find in the given "line".
	 * @return the index where the key starts in the given line. 0 if not.
	 */
	public static int findStart(String key, String line) {
		
		if (line==null || key==null) 
    		return 0;
		if (!line.contains(key)) 
    		return 0;
		    	
    	int j=0;
    	char cLine;
		char cKey;
		int l;
		
		for (int i=0; i<line.length(); i++) {
			cLine = line.charAt(i);
			cKey = key.charAt(j);
    		if (cLine == cKey){
    			l = 0;
    			while (cLine == cKey && key.length()-1>j) {
    				i++;
    				j++;
    				l++;
    				cLine = line.charAt(i);
    				cKey = key.charAt(j);
    			}
    			if (l == key.length()-1) {
    				// the key has been found in the line.
    				return i-(key.length()-1);
    			}
    			j = 0;
    		} // end of if key is maybe found.
		}
		return 0;
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
     * Shows where two strings are different
     * @param a
     * @param b
     * @return true if strings are almost* the same
     * *almost, because the back line character can differ from ascii 10 to 13. 
     */
    public static boolean compareStrings(String a, String b, boolean shows){
    	if (a==null || b==null) 
    		return false;
    	int lengthA = a.length();
    	int lengthB = b.length();
    	if (lengthA != lengthB) {
    		if (shows)
    			System.out.println("String A has " + lengthA + " characters, whereas String B has " + lengthB);
    	}
    	
    	int j=0;
    	char chA;
		char chB;
		int asciiA;
		int asciiB;
		
    	for (int i=0; i<lengthA; i++) {
    		chA = a.charAt(i);
    		chB = b.charAt(j);
    		if (chA == chB){
    			if (shows) {System.out.println("i:"+i+";j:"+j+" ; a:" + chA + " = b:" + chB);}
    		}
    		else {
    			asciiA = (int) chA;
    			asciiB = (int) chB;
    			if (shows) {
    				System.out.println("i:"+i+";j:"+j+" ; a:" + chA + " (ascii " + asciiA + ") "
    										+ "!= b:" + chB + " (ascii " + asciiB + ")");
    			}
    			if ((asciiA == 10 || asciiA == 13) && (asciiB == 10 || asciiB == 13)) {
    				if (shows) {System.out.println("i:"+i+";j:"+j+" (different back to line char) ");}
    				
    	    		asciiA = (int) a.charAt(i+1);
    				if (asciiA == 10 || asciiA == 13) i++;
    				
    				asciiB = (int) b.charAt(j+1);
    				if (asciiB == 10 || asciiB == 13) j++;
    				
    			} else {
    				return false;
    			}
    		}
    		j++;
    	}
    	if (shows)
			System.out.println("=> the strings are the same.");
    	return true;
    }
}
