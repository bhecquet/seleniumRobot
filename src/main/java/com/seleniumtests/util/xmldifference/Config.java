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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * Config class containing configuration information about the XMLDog Application including the XML parser.
 *
 * <p/>configuration
 *
 * <p/>
 * <br>
 * Attach Config when instantiating XMLDog to provide your own configuration
 *
 * <p/>
 * <br>
 * Attributes for the Element Node to be excluded during comparison and Only the Attributes
 *
 * <p/>to be used to compare the Element Nodes can be specified.
 *
 * <p/>
 * <br>
 * Elements to be ignored during the comparison can also be specified
 *
 * <p/>
 * <br>
 * Unique attributes for Element Nodes can be specified which will be used to identify matching
 *
 * <p/>Element Nodes
 */

public class Config implements XMLDogConstants {

	private static final Logger logger = Logger.getLogger(Config.class);
    private boolean isValidating = false;

    private boolean isIgnoringWS = true;

    private boolean isNamespaceAware = false;

    private boolean isIgnoringComments = true;

    private boolean isExpandingEntityRefs = true;

    private boolean isDetailedMode = true;

    private boolean isCustomDifference = false;

    private boolean elistEnabled = false;

    private boolean elistToSiblings = false;

    private boolean isIgnoringOrder = true;

    private boolean includeNodeValuesInXPath = true;

    // HashMap containing Element names as Keys and List of Attributes

    // as Objects

    private HashMap<String, List<String>> includedElementAttrsMap = new HashMap<>();

    private HashMap<String, List<String>> excludedElementAttrsMap = new HashMap<>();

    private HashSet<String> excludedElementsSet = new HashSet<>();

    private HashMap<String, String> xpathEList = new HashMap<>();

    // HashMap containing Element names as Keys and unique attribute

    // names as objects

    private HashMap<String, String> uniqueElementAttrMap = new HashMap<>();

    /**
     * Sets Validating flag.
     */

    public void setValidating(final boolean flag) {

        isValidating = flag;

    }

    /**
     * Gets Validating flag.
     */

    public boolean isValidating() {

        return isValidating;

    }

    /**
     * Sets IgnoringWhitespace flag.
     */

    public void setIgnoringWhitespace(final boolean flag) {

        isIgnoringWS = flag;

    }

    /**
     * Gets IgnoringWhitespace flag.
     */

    public boolean isIgnoringWhitespace() {

        return isIgnoringWS;

    }

    /**
     * Sets IgnoringOrder flag.
     *
     * <p/>
     * <br>
     * Set this flag to true if the order in which the elements occur doesnt matter,
     *
     * <p/>for stricter comparison set it to false
     */

    public void setIgnoringOrder(final boolean flag) {

        isIgnoringOrder = flag;

    }

    /**
     * Gets IgnoringOrder flag.
     *
     * <p/>
     * <br>
     * This flag is used to determine if order in which elements occur in Golden
     *
     * <p/>and current Document is ignored
     */

    public boolean isIgnoringOrder() {

        return isIgnoringOrder;

    }

    /**
     * Sets Namespace aware flag.
     */

    public void setNamespaceAware(final boolean flag) {

        isNamespaceAware = flag;

    }

    /**
     * Gets Namespace aware flag.
     */

    public boolean isNamespaceAware() {

        return isNamespaceAware;

    }

    /**
     * Sets flag for ignoring XML Comments.
     */

    public void setIgnoringComments(final boolean flag) {

        isIgnoringComments = flag;

    }

    /**
     * Checks if XMLDog is ignoring comments.
     */

    public boolean isIgnoringComments() {

        return isIgnoringComments;

    }

    /**
     * Sets if XMLDog is expanding Entity regferences in the Documents.
     */

    public void setExpandingEntityRefs(final boolean flag) {

        isExpandingEntityRefs = flag;

    }

    /**
     * Checks if XMLDog is expanding Entity References in the Documents.
     */

    public boolean isExpandingEntityRefs() {

        return isExpandingEntityRefs;

    }

    /**
     * Sets if XMLDog is in the Detailed mode.
     *
     * <p/>
     * <br>
     * A <i>Detailed</i> mode forces XMLDog to continue finding the
     *
     * <p/>differences in the entire Document versus a <i>Non-Detailed</i> mode
     *
     * <p/>will stop processing the Document as soon as first difference is found.
     *
     * <p/>
     * <br>
     * Use this feature based on the for performance and Application requirements.
     */

    public void setDetailedMode(final boolean flag) {

        isDetailedMode = flag;

    }

    /**
     * Checks if XMLDog is working in the detailed mode.
     */

    public boolean isDetailedMode() {

        return isDetailedMode;

    }

    public void setIncludeNodeValuesInXPath(final boolean flag) {

        includeNodeValuesInXPath = flag;

    }

    public boolean includesNodeValuesInXPath() {

        return includeNodeValuesInXPath;

    }

    /**
     * Sets Custom difference flag.
     *
     * <p/>If Custom difference is set to true, Differences will be logged identical to
     *
     * <p/>XMLUnit<br>
     *
     * @see  DifferenceConstants
     */

    public void setCustomDifference(final boolean flag) {

        isCustomDifference = flag;

    }

    /**
     * Checks if Custom Difference is turned on.
     *
     * <p/>If Custom difference is turned on, each Node difference as defined in DifferenceConstants
     *
     * <p/>will be logged
     *
     * @see  DifferenceConstants, NodeDetail
     */

    public boolean isCustomDifference() {

        return isCustomDifference;

    }

    /**
     * Sets the flag indicating whether EList entries should be applied to the siblings of the.
     *
     * <p/>same type or not<br>
     */

    public void setApplyEListToSiblings(final boolean flag) {

        elistToSiblings = flag;

    }

    /**
     * Checks if EList entries apply to the siblings or not.
     *
     * @return  true if it does, false otherwise
     */

    public boolean applyEListToSiblings() {

        return elistToSiblings;

    }

    /**
     * Adds Attribute to be included in the Element comparison.
     */

    public void addIncludedAttribute(final String elementName, final String attrName) {

        if ((elementName == null) || ("".equals(elementName.trim()))) {
            return;
        }

        if ((attrName == null) || ("".equals(attrName.trim()))) {
            return;
        }

        List<String> attrNames = includedElementAttrsMap.get(elementName);

        if (attrNames == null) {
            attrNames = new ArrayList<>();
        }

        attrNames.add(attrName);

        includedElementAttrsMap.put(elementName, attrNames);

    }

    /**
     * Adds Attributes to be included in the Element comparison.
     */

    public void addIncludedAttributes(final String elementName, final List<String> attrNames) {

        List<String> attrNamesList = includedElementAttrsMap.get(elementName);

        if (attrNamesList == null) {
            attrNamesList = new ArrayList<>();
        }

        attrNamesList.addAll(attrNames);

        includedElementAttrsMap.put(elementName, attrNamesList);

    }

    /**
     * Adds Attribute to be excluded in the Element comparison.
     */

    public void addExcludedAttribute(final String elementName, final String attrName) {

        if ((elementName == null) || ("".equals(elementName.trim()))) {
            return;
        }

        if ((attrName == null) || ("".equals(attrName.trim()))) {
            return;
        }

        List<String> attrNames = excludedElementAttrsMap.get(elementName);

        if (attrNames == null) {
            attrNames = new ArrayList<>();
        }

        attrNames.add(attrName);

        excludedElementAttrsMap.put(elementName, attrNames);

    }

    /**
     * Adds Attributes to be excluded in the Element comparison.
     */

    public void addExcludedAttributes(final String elementName, final List<String> attrNames) {

        if ((elementName == null) || ("".equals(elementName.trim()))) {
            return;
        }

        List<String> attrNamesList = excludedElementAttrsMap.get(elementName);

        if (attrNamesList == null) {
            attrNamesList = new ArrayList<>();
        }

        attrNamesList.addAll(attrNames);

        excludedElementAttrsMap.put(elementName, attrNamesList);

    }

    /**
     * Add Unique Attribute to the Element which will force Elements to be identical.
     */

    public void addUniqueAttribute(final String elementName, final String attrName) {

        if ((elementName != null) && (attrName != null)) {

            uniqueElementAttrMap.put(elementName, attrName);
        }

    }

    /**
     * Adds Element name to the excluded Elements Set.
     */

    public void addExcludedElement(final String elementName) {

        if ((elementName == null) || ("".equals(elementName.trim()))) {

            return;
        }

        excludedElementsSet.add(elementName);

    }

    /**
     * Gets excluded Attributes Map.
     */

    public Map<String, List<String>> getExcludedAttributesMap() {

        return excludedElementAttrsMap;

    }

    /**
     * Gets included Attributes Map.
     */

    public Map<String, List<String>> getIncludedAttributesMap() {

        return includedElementAttrsMap;

    }

    /**
     * Gets unique Attributes Map.
     */

    public Map<String, String> getUniqueAttributeMap() {

        return uniqueElementAttrMap;

    }

    /**
     * Gets excluded Elements Map.
     */

    public Set<String> getExcludedElementsSet() {

        return excludedElementsSet;

    }

    /**
     * Enables - disables XPath elist.
     *
     * @param  flag  the boolean flag
     */

    public void setXPathEListEnabled(final boolean flag) {

        elistEnabled = flag;

    }

    /**
     * Checks if XPath elist is enabled.
     *
     * @return  true if elist enabled, false otherwise
     */

    public boolean isXPathEListEnabled() {

        return elistEnabled;

    }

    /**
     * Loads XPath EList.
     *
     * <p/>
     * <br>
     * Elist is a list of XPath expressions to exclude Nodes represented by the XPath expression
     *
     * <p/>from the comparison
     *
     * @return  the Map containing all the XPath exclusion entries
     */

    public Map<String, String> loadXPathEList(final String filename) {

        if (!(new File(filename)).exists()) {

            logger.info("Elist (" + filename + ") doesn't exist -- exclude list turned off");

        }

        try (FileReader fr = new FileReader(filename);
        	 BufferedReader br = new BufferedReader(fr);
        		){

            String line;

            while ((line = br.readLine()) != null) {

                xpathEList.put(line.trim(), null);

            }

        } catch (IOException ex) {

            logger.error(ex);

        } 

        return xpathEList;

    }

    /**
     * Parses input string for the Regular Expression.
     *
     * <p/>
     * <br>
     * e.g. Line containing Regular Expression for Attribute value
     *
     * <p/>/emp/[@name="value"]
     */

    public static String parseRegEx(String line) {

    	
        if ((line == null) || (line.trim().length() == 0)) {

            return null;
        }

        // Incorrect format if [ is absent

        if (line.indexOf(XMLConstants.XPATH_REGEX_BEGIN) < 0) {

            return null;
        }

        line = line.trim();

        int regExBegin, regExEnd;

        // Incorrect format if multiple ] occurs

        if ((line.indexOf(']') != (regExEnd = line.lastIndexOf(']')))
                ||

                (regExEnd <= 0)) {

            return null;
        }

        if ((regExBegin = line.lastIndexOf('=')) < 0) {

            return null;
        }

        regExBegin = regExBegin + 1;
        return line.substring(regExBegin, regExEnd);

    }

    /**
     * Gets Elist containing XPath.
     *
     * @return  the Set containing XPath Elist
     */

    public Map<String, String> getXPathEList() {

        return xpathEList;

    }

    /**
     * Prints msg to System.out.
     */

    public static void log(final String msg) {

        if (DEBUG) {

            logger.debug("Config:" + msg);
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
