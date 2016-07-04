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

package com.seleniumtests.util.xmldifference;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

/**
 * XMLDog class for comparing XML documents.
 */
public class XMLDog implements XMLDogConstants {
    private Config _config = null;

    private DocumentBuilderFactory _factory = null;

    private DocumentBuilder _parser = null;
    
    private static final Logger logger = Logger.getLogger(XMLDog.class);

    /**
     * Default Constructor.
     */
    public XMLDog() throws ParserConfigurationException {
        this(new Config());
    }

    /**
     * Constructor.
     *
     * @param  config  the XML Parser Configuration
     *
     * @see    Config
     */
    public XMLDog(final Config config) throws ParserConfigurationException {
        setConfig(config);
    }

    /**
     * Compares test document with the control document.
     *
     * @param   controlFilename  the name of the control/golden XML file
     * @param   testFilename     the name of the test XML file
     *
     * @return  the Differences between the golden and test XML files
     *
     * @see     Differences
     */
    public Differences compare(final String controlFilename, final String testFilename) throws SAXException,
        IOException {
        File control = new File(controlFilename);
        File test = new File(testFilename);
        return compare(control, test);
    }

    /**
     * Sets config.
     */
    public void setConfig(final Config config) throws ParserConfigurationException {
        _config = config;
        _factory = DocumentBuilderFactory.newInstance();
        _factory.setExpandEntityReferences(_config.isExpandingEntityRefs());
        _factory.setIgnoringComments(_config.isIgnoringComments());
        _factory.setIgnoringElementContentWhitespace(_config.isIgnoringWhitespace());
        _factory.setCoalescing(true);
        _factory.setNamespaceAware(_config.isNamespaceAware());
        _parser = _factory.newDocumentBuilder();
    }

    /**
     * Compares Test XML file with the Control XML file.
     *
     * @return  the Differences between the files
     *
     * @see     Differences, FileUtil.writeListAsStr()
     */
    public Differences compare(final File controlFile, final File testFile) throws SAXException, IOException {
        Document control = _parser.parse(controlFile);
        Document test = _parser.parse(testFile);
        Comparator comparator = new Comparator(control, test, _config);
        return comparator.compare();
    }

    /**
     * Compares XML files in the control directory with the corresponding files in the test directory.
     *
     * @param  controlDirPath  the directory containing control docs
     * @param  testDirPath     the directory containing test docs
     * @param  resultDirPath   the directory containing result docs
     */
    public void compareDir(final String controlDirPath, final String testDirPath, final String resultDirPath) throws SAXException, IOException {
        File[] controlFiles = null;
        File controlDir = null;
        File testDir = null;
        File resultDir = null;
        try {
            controlDir = new File(controlDirPath);
            testDir = new File(testDirPath);
            resultDir = new File(resultDirPath);
            if (controlDir.isDirectory() && testDir.isDirectory() && resultDir.isDirectory()) {
                if ((!resultDir.exists()) || (!resultDir.isDirectory())) {
                    resultDir.mkdirs();
                }

                // Take only the files ending in .xml
                FilenameFilter filter = new FilenameFilter() {

                    /**
                     * @see  java.io.FilenameFilter#accept(File, String)
                     */
                    public boolean accept(final File dir, final String name) {
                        if (name.endsWith(".xml")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
                controlFiles = controlDir.listFiles(filter);

                String controlFilename = null;
                String testFilename = null;
                String diffFilename = null;
                for (int i = 0; i < controlFiles.length; i++) {
                    controlFilename = controlFiles[i].getName();
                    diffFilename = resultDirPath + File.separator + FileUtil.getPrefix(controlFilename) + "_diff.txt";
                    testFilename = testDirPath + File.separator + controlFilename;
                    log("Diff file name: " + diffFilename);
                    log("Control XML filename: " + controlFilename);
                    log("Test XML filename: " + testFilename);
                    Differences diff = compare(controlFiles[i].getAbsolutePath(), testFilename);
                    FileUtil.writeListAsStr(diffFilename, diff);
                }
            } else {
                throw new IOException("One of the input paths is not a directory");
            }
        } finally {
            controlFiles = null;
            controlDir = null;
            testDir = null;
            resultDir = null;
        }
    }

    /**
     * Gets Document for a given XML file path.
     *
     * @param   xmlFilePath  the path of the XML file
     *
     * @return  the DOM Document
     */
    public Document getDocument(final String xmlFilePath) throws SAXException, IOException {
        return _parser.parse(new File(xmlFilePath));
    }

    /**
     * Prints msg to System.out.
     */
    public static void log(final String msg) {
        if (DEBUG) {
            System.out.println("XMLDog:" + msg);
        }
    }

    /**
     * Prints msg and Exception to System.out.
     */
    public static void log(final String msg, final Throwable t) {
        if (DEBUG) {
            log(msg);
        }
    }

}
