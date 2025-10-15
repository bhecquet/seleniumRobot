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
package com.seleniumtests.util.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVHelper {

    public static final String DOUBLE_QUOTE = "\"";
    public static final String DELIM_CHAR = ",";

    private CSVHelper() {}

    /**
     * Parses line.
     *
     * @param   line    line to read
     * @param   delim   data delimiter
     *
     * @return  array of data. Each item is the content of a cell in a row
     */
    public static String[] parseLine(final String line, final String delim) {
        if (line == null || line.trim().isEmpty()) {
            return new String[] {};
        }

        List<String> tokenList = new ArrayList<>();
        String[] result = null;

        String[] tokens = line.split(delim);
        int count = 0;
        while (count < tokens.length) {
            if (tokens[count] == null || tokens[count].isEmpty()) {
                tokenList.add("");
                count++;
                continue;
            }

            if (tokens[count].startsWith(DOUBLE_QUOTE)) {
                StringBuilder sbToken = new StringBuilder(tokens[count].substring(1));
                while (count < tokens.length && !tokens[count].endsWith(DOUBLE_QUOTE)) {
                    count++;
                    sbToken.append(DELIM_CHAR).append(tokens[count]);
                }

                sbToken.deleteCharAt(sbToken.length() - 1);
                tokenList.add(sbToken.toString());
            } else {
                tokenList.add(tokens[count]);
            }

            count++;
        }

        if (!tokenList.isEmpty()) {
            result = new String[tokenList.size()];
            tokenList.toArray(result);
        }

        return result;

    }

    /**
     * Parses CSV file which has no header and returns a String[][] object.
     *
     * @param   file    CSV file to read
     *
     * @return data as an array representing rows
     */
    public static String[][] read(final File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return read(fis);
    }
    
    public static String[][] read(final File file, final String delim) throws IOException {
    	FileInputStream fis = new FileInputStream(file);
    	return read(fis, delim);
    }


    /**
     * Parses CSV file which has no header and returns a String[][] object.
     *
     * @param  is     the input stream
     *
     * @return  data as an array representing rows
     */
    public static String[][] read(final InputStream is) throws IOException {
        return read(is, null);
    }
    
    /**
     * Parses CSV file which has header on first line and returns a String[][] object.
     *
     * @param   file    CSV file to read
     *
     * @return data as an array representing rows
     */
    public static String[][] readWithHeader(final File file) throws IOException {
    	return readWithHeader(file, null);
    }
    
    public static String[][] readWithHeader(final File file, final String delim) throws IOException {
    	FileInputStream fis = new FileInputStream(file);
    	return read(fis, delim, true);
    }

    /**
     * Parses an input stream and returns a String[][] object.
     *
     * @param  is     the input stream
     * @param  delim    data delimiter
     *
     * @return data as an array representing rows
     */
    public static String[][] read(final InputStream is, final String delim) throws IOException {
    	return read(is, delim, false);
    }
    public static String[][] read(final InputStream is, final String delim, final boolean skipHeader) throws IOException {

        String[][] result = null;
        List<String[]> list = new ArrayList<>();
        String inputLine;

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        int i = 0;
        while ((inputLine = reader.readLine()) != null) {

        	i++;
        	if (i == 1 && skipHeader) {
        		continue;
        	}
        	
            try {
                String[] item;
                item = parseLine(inputLine, Objects.requireNonNullElse(delim, DELIM_CHAR));

                if (item != null) {
                    list.add(item);
                }
            } catch (Exception e) {
            	// ignore
            }
        }

        reader.close();

        if (!list.isEmpty()) {
            result = new String[list.size()][];
            list.toArray(result);
        }

        return result;
    }
}
