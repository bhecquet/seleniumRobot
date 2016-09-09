/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.seleniumtests.core.Filter;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.DatasetException;
import com.seleniumtests.reporter.TestLogging;

public class CSVHelper {
    private static Logger logger = TestLogging.getLogger(CSVHelper.class);

    public static final String DOUBLE_QUOTE = "\"";
    public static final String DELIM_CHAR = ",";
    public static final String TAB_CHAR = "	";

    private CSVHelper() {}
    
    /**
     * Reads data from csv formatted file. Keep csv file in the same folder as the test case class and specify class as
     * <code>this.getClass()</code>.
     *
     * @param   clazz
     * @param   filename
     * @param   filter
     * @param   readHeaders
     *
     * @return
     *
     * @throws  Exception
     */
    public static Iterator<Object[]> getDataFromCSVFile(final Class<?> clazz, final String filename,
            final Filter filter, final boolean readHeaders, final boolean supportDPFilter) {
        return getDataFromCSVFile(clazz, filename, filter, readHeaders, null, supportDPFilter);
    }

    /**
     * Reads data from csv file.
     * class : null => filename : entire path of file
     * class : this.getClass(), filename : the filename will be search in the same directory of the class
     * 
     * @param clazz
     * @param filename
     * @param filter
     * @param readHeaders
     * @param delimiter
     * @param supportDPFilter
     * @return
     */
    public static Iterator<Object[]> getDataFromCSVFile(final Class<?> clazz, final String filename, Filter filter,
            final boolean readHeaders, final String delimiter, final boolean supportDPFilter) {

    	Filter _filter = filter;
        InputStream is = null;
        try {
            if (clazz != null) {
                is = clazz.getResourceAsStream(filename);
            } else {
                is = new FileInputStream(filename);
            }

            if (is == null) {
                return new ArrayList<Object[]>().iterator();
            }

            // Get the sheet
            String[][] csvData = read(is, delimiter);

            List<Object[]> sheetData = new ArrayList<>();
            if (readHeaders) {
                List<Object> rowData = new ArrayList<>();
                for (int j = 0; j < csvData[0].length; j++) {
                    rowData.add(csvData[0][j]);
                }

                sheetData.add(rowData.toArray(new Object[rowData.size()]));
            }

            // Check for blank rows first
            // First row is the header
            StringBuilder sbBlank = new StringBuilder();

            if (sbBlank.length() > 0) {
                sbBlank.deleteCharAt(sbBlank.length() - 1);
                throw new CustomSeleniumTestsException("Blank TestTitle found on Row(s) " + sbBlank.toString() + ".");
            }

            // Support include tags and exclude tags
            if (supportDPFilter) {
                Filter dpFilter = SpreadSheetHelper.getDPFilter();

                if (dpFilter != null) {
                    if (_filter == null) {
                    	_filter = dpFilter;
                    } else {
                    	_filter = Filter.and(_filter, dpFilter);
                    }
                }
            }

            // The first row is the header data
            for (int i = 1; i < csvData.length; i++) {

                Map<String, Object> rowDataMap = new HashMap<>();
                List<Object> rowData = new ArrayList<>();

                // Create the mapping between headers and column data
                for (int j = 0; j < csvData[i].length; j++) {
                    rowDataMap.put(csvData[0][j], csvData[i][j]);
                }

                for (int j = 0; j < csvData[0].length; j++) {

                    // Fix for null values not getting created when number of columns in a row is less than
                    // expected.
                    if (csvData[i].length > j) {
                        rowData.add(csvData[i][j]);
                    } else {
                        rowData.add(null);
                    }
                }

                // To support include tags and exclude tags
                if (supportDPFilter) {
                    SpreadSheetHelper.formatDPTags(rowDataMap);
                }

                if (_filter == null || _filter.match(rowDataMap)) {
                    sheetData.add(rowData.toArray(new Object[rowData.size()]));
                }
            }

            if ((!readHeaders && sheetData.isEmpty()) || (readHeaders && sheetData.size() <= 1)) {
                logger.warn("No matching data found on csv file: " + filename + " with filter criteria: "
                        + _filter.toString());
            }

            return sheetData.iterator();
        } catch (Exception e) {
            throw new DatasetException(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        }
    }

    /**
     * Get headers from a csv file.
     *
     * @param   clazz      - null means use the absolute file path, otherwise use relative path under the class
     * @param   filename
     * @param   delimiter  - null means ","
     *
     * @return
     */
    public static List<String> getHeaderFromCSVFile(final Class<?> clazz, final String filename, String delimiter) {
        String _delimiter = delimiter == null ? ",": delimiter;
    	
        InputStream is = null;
        try {
            if (clazz != null) {
                is = clazz.getResourceAsStream(filename);
            } else {
                is = new FileInputStream(filename);
            }

            if (is == null) {
                return new ArrayList<>();
            }

            // Get the sheet
            String[][] csvData = read(is, _delimiter);

            ArrayList<String> rowData = new ArrayList<>();

            for (int j = 0; j < csvData[0].length; j++) {
                rowData.add(csvData[0][j]);
            }

            return rowData;
        } catch (Exception e) {
            throw new DatasetException(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    logger.warn(e);
                }
            }
        }

    }

    /**
     * Parses line.
     *
     * @param   line
     * @param   delim
     *
     * @return
     */
    public static String[] parseLine(final String line, final String delim) {
        if (line == null || line.trim().length() == 0) {
            return new String[] {};
        }

        List<String> tokenList = new ArrayList<>();
        String[] result = null;

        String[] tokens = line.split(delim);
        int count = 0;
        while (count < tokens.length) {
            if (tokens[count] == null || tokens[count].length() == 0) {
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
     * Parses file and returns a String[][] object.
     *
     * @param   file
     *
     * @return
     *
     * @throws  IOException
     */
    public static String[][] read(final File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return read(fis);
    }

    public static String[][] read(final InputStream is) throws IOException {
        return read(is, null);
    }

    /**
     * Parses an input stream and returns a String[][] object.
     *
     * @param   is
     *
     * @return
     *
     * @throws  IOException
     */
    public static String[][] read(final InputStream is, final String delim) throws IOException {

        String[][] result = null;
        List<String[]> list = new ArrayList<>();
        String inputLine;

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        while ((inputLine = reader.readLine()) != null) {
            try {
                String[] item;
                if (delim == null) {
                    item = parseLine(inputLine, DELIM_CHAR);
                } else {
                    item = parseLine(inputLine, delim);
                }

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

    /**
     * Parses URL and returns a String[][] object.
     *
     * @param   url
     *
     * @return
     *
     * @throws  IOException
     */
    public static String[][] read(final URL url) throws IOException {
        URLConnection con = url.openConnection();
        return read(con.getInputStream());
    }
}
