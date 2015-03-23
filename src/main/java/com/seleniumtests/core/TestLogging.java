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

package com.seleniumtests.core;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import org.testng.ITestResult;
import org.testng.Reporter;

import com.google.common.collect.Lists;

import com.google.gdata.util.common.html.HtmlToText;

import com.seleniumtests.driver.ScreenShot;

import com.seleniumtests.helper.FileUtility;
import com.seleniumtests.helper.StringUtility;

import com.seleniumtests.reporter.PluginsHelper;

import com.seleniumtests.xmldog.Config;
import com.seleniumtests.xmldog.Differences;
import com.seleniumtests.xmldog.XMLDog;

/**
 * Log methods for test operations.
 */
public class TestLogging {

    private static Map<String, Map<String, Map<String, List<String>>>> logMap = Collections.synchronizedMap(
            new HashMap<String, Map<String, Map<String, List<String>>>>());

    /**
     * error Logger.
     *
     * @param  message
     */
    public static void errorLogger(String message) {
        message = "<li><b><font color='#6600CC'>" + message + "</font></b></li>";
        log(message, false, false);
    }

    public static Logger getLogger(final Class<?> cls) {
        boolean rootIsConfigured = Logger.getRootLogger().getAllAppenders().hasMoreElements();
        if (!rootIsConfigured) {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.INFO);

            Appender appender = (Appender) Logger.getRootLogger().getAllAppenders().nextElement();
            appender.setLayout(new PatternLayout(" %-5p %d [%t] %C{1}: %m%n"));
        }

        return Logger.getLogger(cls);
    }

    public static Map<String, Map<String, List<String>>> getPageListenerLog(final String pageListenerClassName) {
        return logMap.get(pageListenerClassName);
    }

    public static List<String> getPageListenerLogByMethodInstance(final ITestResult testResult) {

        for (Entry<String, Map<String, Map<String, List<String>>>> listenerEntry : logMap.entrySet()) {
            if (!PluginsHelper.getInstance().isTestResultEffected(listenerEntry.getKey())) {
                continue;
            }

            Map<String, Map<String, List<String>>> pageMap = listenerEntry.getValue();
            for (Entry<String, Map<String, List<String>>> pageEntry : pageMap.entrySet()) {
                Map<String, List<String>> errorMap = pageEntry.getValue();
                String methodInstance = StringUtility.constructMethodSignature(testResult.getMethod()
                            .getConstructorOrMethod().getMethod(), testResult.getParameters());
                return errorMap.get(methodInstance);
            }
        }

        return null;
    }

    /**
     * Log info.
     *
     * @param  message
     */
    public static void logInfo(String message) {
        message = "<li><font color='#00cd00'>" + message + "</font></li>";
        log(message, false, false);
    }

    /**
     * Log method.
     *
     * @param  message
     */
    public static void log(final String message) {
        log(message, false, false);
    }

    /**
     * Log.
     *
     * @param  message
     * @param  logToStandardOutput
     */
    public static void log(final String message, final boolean logToStandardOutput) {
        log(message, false, logToStandardOutput);
    }

    public static void log(String message, final boolean failed, final boolean logToStandardOutput) {

        if (message == null) {
            message = "";
        }

        message = message.replaceAll("\\n", "<br/>");

        if (failed) {
            message = "<span style=\"font-weight:bold;color:#cc0052;\">" + message + "</span>";
        }

        Reporter.log(escape(message), logToStandardOutput);
    }

    public static String escape(final String message) {
        return message.replaceAll("\\n", "<br/>").replaceAll("<", "@@lt@@").replaceAll(">", "^^greaterThan^^");
    }

    public static String unEscape(String message) {
        message = message.replaceAll("<br/>", "\\n").replaceAll("@@lt@@", "<").replaceAll("\\^\\^gt\\^\\^", ">");

        message = HtmlToText.htmlToPlainText(message);
        return message;
    }

    public static void logWebOutput(final String url, final String message, final boolean failed) {
        log("Output: " + message + "<br/>", failed, false);
    }

    public static void logWebStep(final String url, final String message, final boolean failed) {
        log("<li>" + (failed ? "<b>FailedStep</b>: " : " ") + message + "</li>", failed, false);
    }

    public static String buildScreenshotLog(final ScreenShot screenShot) {
        StringBuffer sbMessage = new StringBuffer("");
        if (screenShot.getLocation() != null) {
            sbMessage.append("<a href='" + screenShot.getLocation() + "' target=url>Application URL</a>");
        }

        if (screenShot.getHtmlSourcePath() != null) {
            sbMessage.append(" | <a href='" + screenShot.getHtmlSourcePath()
                    + "' target=html>Application HTML Source</a>");
        }

        if (screenShot.getImagePath() != null) {
            sbMessage.append(" | <a href='" + screenShot.getImagePath()
                    + "' class='lightbox'>Application Snapshot</a>");
        }

        return sbMessage.toString();
    }

    /**
     * Log method.
     *
     * @param  message
     */
    public static void warning(String message) {
        message = "<li><font color='#FFFF00'>" + message + "</font></li>";
        log(message, false, false);
    }

    public static ArrayList<String> getRawLog(final ITestResult result) {
        ArrayList<String> messages = Lists.newArrayList();
        for (String line : Reporter.getOutput(result)) {
            line = unEscape(line);
            messages.add(line);
        }

        return messages;
    }

    private static String getXMLTitle(final String xmlString) {
        String xmlTitle = "";

        String startPattern = "<S:Body>";
        String endPattern = "xmlns=";
        xmlTitle = xmlString;

        int startIndex = xmlString.indexOf(startPattern) + 13;
        int endIndex = xmlString.indexOf(endPattern, startIndex);

        xmlTitle = xmlTitle.substring(startIndex, endIndex);
        xmlTitle = xmlTitle.replace('<', ' ');
        xmlTitle = xmlTitle.replace('>', ' ');
        xmlTitle = xmlTitle.trim();

        System.out.println("XML title : " + xmlTitle);
        return xmlTitle;

    }

    public static Differences compareGoldenFile(String goldenFile, final String responseXML, final String outputDir,
            final String outputfilename, final StringBuffer sbMessage, final Config xmlCompareConfig) {
        if (goldenFile != null && !goldenFile.isEmpty()) {

            // Save API response to gold fold
            goldenFile = goldenFile.replaceAll("\\\\", "/");

            if (!new File(goldenFile).getParentFile().exists()) {
                new File(goldenFile).getParentFile().mkdirs();
            }

            if (!new File(goldenFile).exists()) {
                try {
                    FileUtility.writeToFile(goldenFile, responseXML);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String xmlOutputFile = outputDir + "/xmls/" + outputfilename + ".xml";
            xmlOutputFile = xmlOutputFile.replaceAll("\\\\", "/");

            sbMessage.append(" | <a href='file:///" + goldenFile + "' target=golden>golden</a>");
            sbMessage.append(" | <a href=\"javascript:copyFile('" + xmlOutputFile + "','" + goldenFile
                    + "');\">save as golden</a>");

            Differences diff = null;
            XMLDog dog = null;
            try {
                if (xmlCompareConfig != null) {
                    dog = new XMLDog(xmlCompareConfig);
                } else {
                    dog = new XMLDog();
                }

                diff = dog.compare(goldenFile, xmlOutputFile);

            } catch (Exception e) {
                e.printStackTrace();
            }

            sbMessage.append(" | <a href=\"javascript:toggle('" + outputfilename + "');\" class='xmldifflnk'>xml diff ("
                    + ((diff != null) ? diff.getDiffCount() : "UNKNOWN") + ") [+]</a>");
            sbMessage.append(" <div id='" + outputfilename + "' class='xmldiff'> "
                    + (diff == null ? "" : diff.getHTML()) + "</div>");
            return diff;

        } else {
            return null;
        }
    }

}
