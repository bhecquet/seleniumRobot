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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;

/**
 * Comparator class containing the Core of XML Comparison Engine.
 *
 * <p/>Comparison Engine takes each Test Document Node and tries and find the Control/Golden
 *
 * <p/>Document Node which is a Best fit
 */

public class Comparator implements XMLDogConstants {

	private static final Logger logger = TestLogging.getLogger(Comparator.class);
	
    private Node controlNode;

    private Node testNode;

    private Config config;

    // List of listeners

    private List listeners = new ArrayList();

    private boolean ignoringWhitespace = true;

    private boolean includeNodeValueInXPath = true;

    /**
     * Default Contructor.
     */

    public Comparator() {

        this(null, null, new Config());

    }

    /**
     * Constructor.
     *
     * @param  controlDoc  the Control Document to compare test Document against
     * @param  testDoc     the test Document to be compared for differences
     */

    public Comparator(final Node controlNode, final Node testNode, final Config config) {

        this.config = config;

        this.controlNode = controlNode;

        this.testNode = testNode;

        this.ignoringWhitespace = config.isIgnoringWhitespace();

        this.includeNodeValueInXPath = config.includesNodeValuesInXPath();

        this.listeners.clear();

        this.listeners.add(new Differences());

    }

    /**
     * Adds DifferenceListener.
     */

    public void addDifferenceListener(final DifferenceListener listener) {

        if (listener != null) {

            listeners.add(listener);
        }

    }

    /**
     * Overrides DifferenceListener.<br>
     *
     * <p/>Method will clear all the DifferenceListeners and put only one
     *
     * <p/>DifferenceListener passed as an argument
     */

    public void overrideDifferenceListener(final DifferenceListener listener) {

        if (listener != null) {

            listeners.clear();

            listeners.add(listener);

        }

    }

    /**
     * Sets Control Node.
     */

    public void setControlNode(final Node controlNode) {

        if (controlNode == null) {

            throw new IllegalArgumentException("Cannot compare null node");
        }

        this.controlNode = controlNode;

    }

    /**
     * Gets Control Node.
     */

    public Node getControlNode() {

        return controlNode;

    }

    /**
     * Sets Test Node.
     */

    public void setTestNode(final Node testNode) {

        if (testNode == null) {

            throw new IllegalArgumentException("Cannot compare null node");
        }

        this.testNode = testNode;

    }

    /**
     * Gets Test Node.
     */

    public Node getTestNode() {

        return testNode;

    }

    /**
     * Convenience method to check if the whitespace is being ignored.
     */

    protected boolean isIgnoringWhitespace() {

        return config.isIgnoringWhitespace();

    }

    /**
     * Compares Control and Test nodes.
     */

    public Differences compare() {

        logger.info("IN comparator compare method");

        return compare(getControlNode(), getTestNode());

    }

    /**
     * Compares nodes and its children recursively.
     *
     * @param   control  the control Node
     * @param   test     the test Node
     *
     * @return  the Differences between two Nodes
     *
     * @see     Differences
     */

    public Differences compare(final Node controlNode, final Node testNode) {

        Differences differences = new Differences();

        logger.info("IN the compare(node, node) method");

        XNode xControlNode = new XNode(controlNode, XMLUtil.generateXPath(controlNode, 
        								ignoringWhitespace, includeNodeValueInXPath, false));

        XNode xTestNode = new XNode(testNode, XMLUtil.generateXPath(testNode, ignoringWhitespace, 
        															includeNodeValueInXPath, false));

        xControlNode.setDepth(0);

        xTestNode.setDepth(0);

        // For Document type Node

        if (testNode.getNodeType() == Node.DOCUMENT_NODE) {

            log("Test Node is Document Node");

            if (testNode.hasChildNodes()) {

                if (!controlNode.hasChildNodes()) {

                    if (!config.isCustomDifference()) {

                        differences.add("/" + testNode.getNodeName() + " is entirely new document");
                    } else {

                        Difference diff = new Difference(DifferenceConstants.NEW_DOCUMENT, null, xTestNode);

                        differences.add(diff);

                    }

                    return differences;

                } else {

                    // Get all the Document Children and compare them

                    differences.add(compareChildNodes(xControlNode, xTestNode));

                }

            } else {

                if (!config.isCustomDifference()) {

                    differences.add("/" + testNode.getNodeName() + " is an empty document");
                } else {

                    Difference diff = new Difference(DifferenceConstants.EMPTY_DOCUMENT, xControlNode, xTestNode);

                    differences.add(diff);

                }

                return differences;

            }

        } else {

        	NodeResult nodeResult = compareSimilarNodes(xControlNode, xTestNode,
                    new OrderedMap(OrderedMap.TYPE_UNSYNCHRONIZED_MOV));

            differences.add(nodeResult);

        }

        return differences;

    }

    /**
     * Compares Nodes similar to the ones which are input and returns the one.
     *
     * <p/>that is the BEST fit
     *
     * <p/>
     * <br>
     * OrderedMap is used for information such as which control Nodes are already
     *
     * <p/>visited and which test Nodes match a given control Node
     *
     * <p/>
     * <br>
     * For a given test Node and control Node set, similar Nodes are found using the parent
     *
     * <p/>of the control Node which closely match a given test Node and only the Node matching most
     *
     * <p/>closely to the test Node is returned
     *
     * @param   control      the control Node
     * @param   test         the test Node
     * @param   nodeTracker  the OrderedMap which contains control Nodes already visited as keys and
     *
     *                       <p/>all the matching test Nodes as the values
     *
     * @return  the NodeResult containing the Differences between two Nodes
     *
     * @see     NodeResult
     */

    protected NodeResult compareSimilarNodes(final XNode xControl, final XNode xTest, final OrderedMap nodeTracker) {

        Differences differences = new Differences();

        boolean noSimilarNodes = true; // to indicate no nodes of the type found

        boolean unset = true; // to indicate bestfit is uninitialzed

        Node control = xControl.getNode();

        Node test = xTest.getNode();

        XNode xSimilarNode;

        String similarNodeXPath;

        NodeResult bestfitNodeResult = new NodeResult(xControl, xTest, differences);

        Node parent = control.getParentNode();

        log("Parent XPath " + XMLUtil.generateXPath(xControl.getXPath()));

        if (parent != null) {

            List similarNodes = XMLUtil.getSimilarChildXNodes(parent, test, isIgnoringWhitespace());

            Node similarNode = null;

            NodeResult nr = null;

            if (!similarNodes.isEmpty()) {

                noSimilarNodes = false;
            }


            // Later control can be transferred to the App User code, to give them

            // control over comparing 2 similar nodes

            for (int i = 0; i < similarNodes.size(); i++) {

                xSimilarNode = (XNode) similarNodes.get(i);

                similarNodeXPath = XMLUtil.generateXPath(xSimilarNode.getNode(),


                        XMLUtil.generateXPath(xControl.getXPath()),


                        ignoringWhitespace,


                        includeNodeValueInXPath,


                        false);

                xSimilarNode.setXPath(similarNodeXPath);

                // For Element type Node

                if (test.getNodeType() == Node.ELEMENT_NODE) {

                    log("compareSimilarNodes: Test node is ELEMENT type");

                    nr = compareElements(xSimilarNode, xTest);

                } else

                // For Entity Reference Node

                if (test.getNodeType() == Node.ENTITY_REFERENCE_NODE) {

                    log("compareSimilarNodes: Test node is Entity Reference type");

                } else

                // For Text Node

                if (test.getNodeType() == Node.TEXT_NODE) {

                    log("compareSimilarNodes: Test node is TEXT type");

                    if ((StringUtil.isWhitespaceStr(test.getNodeValue())) && (isIgnoringWhitespace())) {

                        log("compareSimilarNodes: Ignoring WHITE space node");
                    } else {

                        nr = compareText(xSimilarNode, xTest);
                    }

                } else

                // For Document Type Node

                if (test.getNodeType() == Node.DOCUMENT_TYPE_NODE) {

                    log("compareSimilarNodes: Test node is DOCUMENT TYPE type");

                    nr = compareDocumentType(xSimilarNode, xTest);

                } else

                // For Comment Node

                if (test.getNodeType() == Node.COMMENT_NODE) {

                    log("compareSimilarNodes: Test node is COMMENT type");

                    nr = compareComments(xSimilarNode, xTest);

                } else

                // For CDATA section Node

                if (test.getNodeType() == Node.CDATA_SECTION_NODE) {

                    log("compareSimilarNodes: Test node is CDATA SECTION type");

                    nr = compareCDATA(xSimilarNode, xTest);

                }

                // Make sure bestfitNodeResult has the best result so far

                if (nr != null) {

                    if ((nr.isExactMatch()) || (nr.isUniqueAttrMatch())) {

                        bestfitNodeResult = nr;

                        // Continue only if this similarNode (controlNode) has NOT

                        // been taken already

                        Object ntObject;

                        boolean shouldBreak = false;

                        if ((ntObject = nodeTracker.getElement(similarNode)) == null) {

                            shouldBreak = true;
                        } else {

                            if (ntObject instanceof List) {

                                List l = (List) ntObject;

                                for (int j = 0; j < l.size(); j++) {

                                    if (((NodeResult) l.get(j)).isMatch()) {

                                        shouldBreak = true;

                                        break;

                                    }

                                }

                            }

                            if ((ntObject instanceof NodeResult) && (!((NodeResult) ntObject).isMatch())) {

                                shouldBreak = true;
                            }

                        }

                        if (shouldBreak) {

                            break;
                        }

                    } else {

                        // Assign first NR as the Best Fit and then compare

                        if (unset) {

                            bestfitNodeResult = nr;

                            unset = false;

                        }

                        // whichever has minimum differences is the best fit

                        else {

                            if (nr.getDifferences().size() < bestfitNodeResult.getDifferences().size()) {

                                bestfitNodeResult = nr;
                            } else

                            // If they are equal take the one which is not present in the nodeTracker

                            if ((nr.getDifferences().size() == bestfitNodeResult.getDifferences().size())
                                    &&

                                    (nodeTracker.getElement(bestfitNodeResult.getControlNode().getNode()) != null)) {

                                bestfitNodeResult = nr;
                            }

                        }

                    }

                }

            } // end for all the similar nodes

        }

        // If no similar Nodes found and if its ignoring white space

        if (noSimilarNodes) {

            if ((isIgnoringWhitespace()) && (XMLUtil.isWhitespaceTextNode(test))) {

                // ignore

            } else {

                // BUG FIX

                // Explicitly set control node to NULL to indicate NO MATCHES found

                bestfitNodeResult.setControlNode(new XNode(null, null));

                if (!config.isCustomDifference()) {

                    differences.add("Added Node: Test Node " + xTest.getXPath());
                } else {

                    Difference diff = new Difference(DifferenceConstants.NODE_NOT_FOUND, xControl, xTest);

                    differences.add(diff);

                }

            }

        }

        log("Start===================================================================");

        log("compareSimilarNodes: BestFitNode is " + bestfitNodeResult.toString());

        log("End===================================================================");

        return bestfitNodeResult;

    }

    /**
     * Compares DocumentType Node.
     *
     * @param   control  the control DOCTYPE Node
     * @param   test     the test DOCTYPE Node
     *
     * @return  the NodeResult containing the Differences between two DOCTYPE Nodes
     *
     * @see     NodeResult
     */

    protected NodeResult compareDocumentType(final XNode xControl, final XNode xTest) {

        Differences differences = new Differences();

        NodeResult nodeResult = new NodeResult(xControl, xTest, differences);

        boolean diffName;

        boolean diffPublicId;

        boolean diffSysId;

        DocumentType control = (DocumentType) xControl.getNode();

        DocumentType test = (DocumentType) xTest.getNode();
        diffName = XMLUtil.areNullorEqual(control.getPublicId(), test.getPublicId(), ignoringWhitespace, includeNodeValueInXPath);
        diffSysId = XMLUtil.areNullorEqual(control.getSystemId(), test.getSystemId(), ignoringWhitespace, includeNodeValueInXPath);
        diffPublicId = XMLUtil.areNullorEqual(control.getName(), test.getName(), ignoringWhitespace, includeNodeValueInXPath);
        
        if (diffName && diffSysId && diffPublicId) {
 
            // do nothing

        } else {

            if (!config.isCustomDifference()) {

                differences.add("Different DocumentType Node: Current Node " + xTest.getXPath() + " --> Golden Node "
                        + xControl.getXPath());
            } else {

                Difference diff = null;

                if (diffName) {

                    diff = new Difference(DifferenceConstants.DOCTYPE_NAME, xControl, xTest);
                } else if (diffSysId) {

                    diff = new Difference(DifferenceConstants.DOCTYPE_SYSTEM_ID, xControl, xTest);
                } else if (diffPublicId) {

                    diff = new Difference(DifferenceConstants.DOCTYPE_PUBLIC_ID, xControl, xTest);
                }

                differences.add(diff);

            }

        }

        return nodeResult;

    }

    /**
     * Compares Comment Nodes.
     *
     * @param   control  the control Comment Node
     * @param   test     the test Comment Node
     *
     * @return  the NodeResult containing the Differences between two Comment Nodes
     *
     * @see     NodeResult
     */

    protected NodeResult compareComments(final XNode xControl, final XNode xTest) {

        Differences differences = new Differences();

        NodeResult nodeResult = new NodeResult(xControl, xTest, differences);

        Comment control = (Comment) xControl.getNode();

        Comment test = (Comment) xTest.getNode();

        if (!XMLUtil.nodesEqual(control, test, isIgnoringWhitespace())) {

            if (!config.isCustomDifference()) {

                differences.add("Different Comment Node: Current Node" + xTest.getXPath()
                        +

                        " --> Golden Node " + xControl.getXPath());
            } else {

                Difference diff = new Difference(DifferenceConstants.COMMENT_VALUE, xControl, xTest);

                differences.add(diff);

            }

        } else {

            nodeResult.setIfExactMatch(true);
        }

        return nodeResult;

    }

    /**
     * Compares CDATASECTION Nodes.
     *
     * @param   control  the control CDATA Node
     * @param   test     the test CDATA Node
     *
     * @return  the NodeResult containing the Differences between two CDATA Nodes
     *
     * @see     NodeResult
     */

    protected NodeResult compareCDATA(final XNode xControl, final XNode xTest) {

        Differences differences = new Differences();

        CDATASection control = (CDATASection) xControl.getNode();

        CDATASection test = (CDATASection) xTest.getNode();

        NodeResult nodeResult = new NodeResult(xControl, xTest, differences);

        if (!XMLUtil.nodesEqual(control, test, isIgnoringWhitespace())) {

            if (!config.isCustomDifference()) {

                differences.add("Different CDATA Node : Current Node " + xTest.getXPath()
                        +

                        " --> Golden Node " + xControl.getXPath());
            } else {

                Difference diff = new Difference(DifferenceConstants.CDATA_VALUE, xControl, xTest);

                differences.add(diff);

            }

        } else {

            nodeResult.setIfExactMatch(true);
        }

        return nodeResult;

    }

    /**
     * Compares Entity Reference Nodes.
     *
     * @param   control  the control EntityRef Node
     * @param   test     the test EntityRef Node
     *
     * @return  the NodeResult containing the Differences between two EntityRef Nodes
     *
     * @see     NodeResult
     */

    protected NodeResult compareEntityRefs(final XNode xControl, final XNode xTest) {

        Differences differences = new Differences();

        EntityReference control = (EntityReference) xControl.getNode();

        EntityReference test = (EntityReference) xTest.getNode();

        NodeResult nodeResult = new NodeResult(xControl, xTest, differences);

        if (!XMLUtil.nodesEqual(control, test, isIgnoringWhitespace())) {

            differences.add("Different Comment Node : Current Node " + xTest.getXPath()
                    +

                    " --> Golden Node " + xControl.getXPath());
        } else {

            nodeResult.setIfExactMatch(true);
        }

        return nodeResult;

    }

    /**
     * Compares Text Nodes.
     *
     * @param   control  the control Text Node
     * @param   test     the test Text Node
     *
     * @return  the NodeResult containing the Differences between two Text Nodes
     *
     * @see     NodeResult
     */

    protected NodeResult compareText(final XNode xControl, final XNode xTest) {

        Differences differences = new Differences();

        Text control = (Text) xControl.getNode();

        Text test = (Text) xTest.getNode();

        NodeResult nodeResult = new NodeResult(xControl, xTest, differences);

        if (!XMLUtil.nodesEqual(control, test, isIgnoringWhitespace())) {

            if (DEBUG) {

                logger.info("===> Compare Text is ignoring whitespace " + isIgnoringWhitespace());

                logger.info("=====> Text nodes Control and test ");

                XMLUtil.printNodeBasics(control);

                XMLUtil.printNodeBasics(test);

            }

            if (!config.isCustomDifference()) {

                differences.add("Different Text Node: Current Node " + xTest.getXPath()
                        +

                        " --> Golden Node " + xControl.getXPath());
            } else {

                Difference diff = new Difference(DifferenceConstants.TEXT_VALUE, xControl, xTest);

                differences.add(diff);

            }

        } else {

            nodeResult.setIfExactMatch(true);
        }

        return nodeResult;

    }

    /**
     * Compares Control and Test Element nodes.
     *
     * <p/>
     * <br>
     * While comparing Element nodes, all the Attributes as well as Children of the Element nodes
     *
     * <p/>are compared recursively
     *
     * @param   control  the control Element Node
     * @param   test     the test Element Node
     *
     * @return  the NodeResult containing the Differences between two Element Node subtree
     *
     * @see     NodeResult
     */

    protected NodeResult compareElements(final XNode xControl, final XNode xTest) {

        Differences differences = new Differences();

        Element control = (Element) xControl.getNode();

        Element test = (Element) xTest.getNode();

        NodeResult nodeResult = new NodeResult(xControl, xTest, differences);

        logger.info("Comparing Elements at Test " + xTest.getXPath() + " Control " + xControl.getXPath());


        String uniqueAttrName = config.getUniqueAttributeMap().get(test.getTagName());

        // Special case if the nodes are same in the first shot itself

        // based on the unique attribute

        if (uniqueAttrName != null 
        		&& (XMLUtil.nodesEqual(control, test, isIgnoringWhitespace()))
        		&& control.hasAttributes()
        		&& test.hasAttributes()
        		) {

            String testAttrValue = test.getAttribute(uniqueAttrName);

            String controlAttrValue = control.getAttribute(uniqueAttrName);

            if ((!"".equals(testAttrValue.trim())) && (controlAttrValue.equals(testAttrValue))) {

                nodeResult.setUniqueAttrMatch(true);

                return nodeResult;

            }

        }

        reportMissingAttrs(xControl, xTest, differences);

        // Compare all the Child Nodes

        differences.add(compareChildNodes(xControl, xTest));

        // if no differences, its an exact match

        if (differences.isEmpty()) {

            log(" Exact match for Test ELEMENT node " + XMLUtil.getNodeBasics(test)
                    +

                    " with control node " + XMLUtil.getNodeBasics(control));

            nodeResult.setIfExactMatch(true);

        }

        return nodeResult;

    }

    private Differences reportMissingAttrs(final XNode xControl, final XNode xTest, final Differences differences) {

        boolean includedEmpty = false;

        boolean excludedEmpty = false;

        Element control = (Element) xControl.getNode();

        Element test = (Element) xTest.getNode();

        String controlNodeXPath = xControl.getXPath();

        String testNodeXPath = xTest.getXPath();

        log("Comparing Attributes for test " + testNodeXPath + " control " + controlNodeXPath);

        Element ctrlNode = (Element) control.cloneNode(true);

        Element tstNode = (Element) test.cloneNode(true);

        // Since the elementList contains elements with the same name, we only

        // need to get this once

        List<String> excludedAttrs = config.getExcludedAttributesMap().get(tstNode.getTagName());

        List<String> includedAttrs = config.getIncludedAttributesMap().get(tstNode.getTagName());

        if ((includedAttrs == null) || (includedAttrs.isEmpty())) {

            includedEmpty = true;
        }

        if ((excludedAttrs == null) || (excludedAttrs.isEmpty())) {

            excludedEmpty = true;
        }

        NamedNodeMap testAttrs = tstNode.getAttributes();

        NamedNodeMap controlAttrs = ctrlNode.getAttributes();

        NamedNodeMap testAttrsForXPath = test.getAttributes();

        NamedNodeMap controlAttrsForXPath = control.getAttributes();

        if (DEBUG) {

            logger.info(" ********** " + test.getNodeName() + " test node has " 
            							+ testAttrs.getLength() + " attrs ");

            logger.info(" ********** " + control.getNodeName() + " control node has " 
            							+ controlAttrs.getLength() + " attrs ");

        }

        // Remove all the excluded attributes, no need to compare them

        if (!excludedEmpty) {

            for (int i = 0; i < excludedAttrs.size(); i++) {

                if (testAttrs != null) {

                    testAttrs.removeNamedItem((String) excludedAttrs.get(i));
                }

                if (controlAttrs != null) {

                    controlAttrs.removeNamedItem((String) excludedAttrs.get(i));
                }

            }

        }

        String testAttrName;

        Attr testAttrNode;

        Attr testAttrNodeXPath;

        Attr controlAttrNode = null;

        Attr controlAttrNodeXPath = null;

        String controlAttrNodeXPathStr;

        if (testAttrs != null) {

            for (int j = 0; j < testAttrs.getLength(); j++) {

                testAttrNode = (Attr) testAttrs.item(j);

                testAttrName = testAttrNode.getName();

                testAttrNodeXPath = (Attr) testAttrsForXPath.getNamedItem(testAttrName);

                // Skip this attribute if its not in the included attrs

                if ((!includedEmpty) && (!includedAttrs.contains(testAttrName))) {

                    continue;
                }

                String nodeXPath =

                    XMLUtil.generateXPath(testAttrNode, testNodeXPath, ignoringWhitespace, includeNodeValueInXPath,
                        false);

                // Skip this attribute if its in the EList

                if (config.isXPathEListEnabled()) {

                    if (config.applyEListToSiblings()) {

                        nodeXPath = XMLUtil.getNoIndexXPath(nodeXPath);
                    }

                    if (config.getXPathEList().containsKey(nodeXPath)) {

                        continue;

                        // exclude this if its Xpath is in the EList

                    }

                }

                if (controlAttrs != null) {

                    controlAttrNode = (Attr) controlAttrs.getNamedItem(testAttrName);

                    controlAttrNodeXPath = (Attr) controlAttrsForXPath.getNamedItem(testAttrName);

                }

                XNode xTestNode = null;

                XNode xControlNode = null;

                if (controlAttrNode == null) {

                    log("Added Attribute: Test node " + nodeXPath);

                    if (!config.isCustomDifference()) {

                        differences.add("New Attribute added: Test Node " + nodeXPath);
                    } else {

                        if (testAttrNode != null) {

                            xTestNode = new XNode(testAttrNodeXPath,


                                    XMLUtil.generateXPath(testAttrNode, testNodeXPath, ignoringWhitespace,
                                        includeNodeValueInXPath, false));
                        }

                        if (controlAttrNode != null) {

                            xControlNode = new XNode(controlAttrNodeXPath,


                                    XMLUtil.generateXPath(controlAttrNode, controlNodeXPath, ignoringWhitespace,
                                        includeNodeValueInXPath, false));
                        }

                        Difference diff = new Difference(DifferenceConstants.ATTR_NAME_NOT_FOUND, xControlNode,
                                xTestNode);

                        differences.add(diff);

                    }

                } else {

                    if (!controlAttrNode.getValue().equals(testAttrNode.getValue())) {

                        controlAttrNodeXPathStr = XMLUtil.generateXPath(controlAttrNode, controlNodeXPath,
                                ignoringWhitespace, includeNodeValueInXPath, false);

                        log("Different Attributes: Test document Node " + nodeXPath
                                +

                                " --> Control document Node " + controlAttrNodeXPathStr);

                        if (!config.isCustomDifference()) {

                            differences.add("Different Attributes: Current Node " + nodeXPath
                                    +

                                    " --> Golden Node " + controlAttrNodeXPathStr);
                        } else {

                            if (testAttrNode != null) {

                                xTestNode = new XNode(testAttrNode,


                                        XMLUtil.generateXPath(testAttrNode, testNodeXPath, ignoringWhitespace,
                                            includeNodeValueInXPath, false));
                            }

                            if (controlAttrNode != null) {

                                xControlNode = new XNode(controlAttrNode,


                                        controlAttrNodeXPathStr);
                            }

                            Difference diff = new Difference(DifferenceConstants.ATTR_VALUE, xControlNode, xTestNode);

                            differences.add(diff);

                        }

                    }

                    // Remove Attribute from the Control document, whatever that is left

                    // in the end didnt exist in the Test document

                    controlAttrs.removeNamedItem(testAttrName);

                }

            }

        }

        // If any attributes left unmatched, add them to differences list

        if (controlAttrs.getLength() > 0) {

            XNode xTestNode = null;

            XNode xControlNode = null;

            for (int i = 0; i < controlAttrs.getLength(); i++) {

                controlAttrNode = (Attr) controlAttrs.item(i);

                controlAttrNodeXPath = (Attr) controlAttrsForXPath.getNamedItem(controlAttrNode.getName());

                testAttrNodeXPath = (Attr) testAttrsForXPath.getNamedItem(controlAttrNode.getName());

                controlAttrNodeXPathStr = XMLUtil.generateXPath(controlAttrNodeXPath, controlNodeXPath,
                        ignoringWhitespace, includeNodeValueInXPath, false);

                // Skip this attribute if its in the EList

                if (config.isXPathEListEnabled()) {

                    if (config.applyEListToSiblings()) {

                        controlAttrNodeXPathStr = XMLUtil.getNoIndexXPath(controlAttrNodeXPathStr);
                    }

                    if (config.getXPathEList().containsKey(controlAttrNodeXPathStr)) {

                        continue;
                    }

                }

                log("Missing Attribute: Test document is missing attribute "
                        + XMLUtil.generateXPath(controlAttrNodeXPath, isIgnoringWhitespace()));

                if (!config.isCustomDifference()) {

                    differences.add("Missing Attribute: Test Node " + controlAttrNodeXPathStr);
                } else {

                    if (testAttrNodeXPath != null) {

                        xTestNode = new XNode(testAttrNodeXPath,


                                XMLUtil.generateXPath(testAttrNodeXPath, testNodeXPath, ignoringWhitespace,
                                    includeNodeValueInXPath, false));
                    }

                    if (controlAttrNodeXPath != null) {

                        xControlNode = new XNode(controlAttrNodeXPath, controlAttrNodeXPathStr);
                    }

                    Difference diff = new Difference(DifferenceConstants.ATTR_NAME_NOT_FOUND, xControlNode, xTestNode);

                    differences.add(diff);

                }

            }

        }

        return differences;

    }

    /**
     *
     * <p/>Refactor above function to reduce the complexity and put compare attributes here
     *
     * <p/>Compares Attribute Nodes
     */

    protected NodeResult compareAttributes(final XNode xControl, final XNode xTest) {

        Differences differences = new Differences();

        return new NodeResult(xControl, xTest, differences);

    }

    /**
     * Compares all the Child Nodes for given Control and Test Nodes recursively.
     *
     * <p/>
     * <br>
     * Reports differences between the entire subtree till the leaf nodes for
     *
     * <p/>control and test Nodes
     *
     * @param   controlNode  the control Node
     * @param   testNode     the test Node
     *
     * @return  the Differences between control Node and test Node
     *
     * @see     Differences
     */

    protected Differences compareChildNodes(final XNode xControl, final XNode xTest) {

        if ((xControl == null) || (xTest == null)) {

            throw new IllegalArgumentException("Test and/or Control Node argument cannot be null");
        }

        log("Comparing CHILD Nodes for Test:" + xTest.getXPath() + "-Control:" + xControl.getXPath() + "-");

        Differences differences = new Differences();

        OrderedMap nodeTracker = new OrderedMap(OrderedMap.TYPE_UNSYNCHRONIZED_MOV);

        Node control = xControl.getNode();

        Node test = xTest.getNode();

        NodeList testChildNodes;

        NodeList controlChildNodes;

        Node testChildNode;

        Node controlChildNode;

        // Check to see if the Control Node or Test Node has been added

        if (control == null) {

            if (test != null) {

                if (!config.isCustomDifference()) {

                    differences.add("Test Node added at " + xTest.getXPath());
                } else {

                    Difference diff = new Difference(DifferenceConstants.ADDED_NODE, null, xTest);

                    differences.add(diff);

                }

                return differences;

            } else {

                return null;
            }

        } else {

            if (test == null) {

                if (!config.isCustomDifference()) {

                    differences.add("Golden Node added at " + xControl.getXPath());
                } else {

                    Difference diff = new Difference(DifferenceConstants.ADDED_NODE, xControl, null);

                    differences.add(diff);

                }

                return differences;

            }

        }

        // If it came this far, then both control and test are NON NULL

        if (test.hasChildNodes()) {

            if (control.hasChildNodes()) {

                testChildNodes = test.getChildNodes();

                controlChildNodes = control.getChildNodes();

                NodeResult matchedNodeResult;

                String testNodeXPath = null;

                XNode xControlChildNode;

                XNode xTestChildNode;

                String testChildXPath;

                String controlChildXPath;

                // For Test Node Child and find a matching Control Node

                for (int i = 0; i < testChildNodes.getLength(); i++) {

                    testChildNode = testChildNodes.item(i);

                    testChildXPath = XMLUtil.generateXPath(testChildNode, xTest.getXPath(), ignoringWhitespace,
                            includeNodeValueInXPath, false);

                    xTestChildNode = new XNode(testChildNode, testChildXPath);

                    xTestChildNode.setDepth(xTest.getDepth() + 1);

                    xTestChildNode.setPosition(i);

                    // If controlChildNode is null, return first child since we are going to get its parent

                    // and find similar Nodes anyway

                    controlChildNode = controlChildNodes.item(i) == null ? controlChildNodes.item(0)
                                                                         : controlChildNodes.item(i);

                    log("******** controlChildNode " + controlChildNode.getNodeName() + " type "
                            + controlChildNode.getNodeType());

                    controlChildXPath = XMLUtil.generateXPath(controlChildNode, xControl.getXPath(),
                            ignoringWhitespace, includeNodeValueInXPath, false);

                    log("compareChildNodes()......controlChildXPath " + controlChildXPath);

                    xControlChildNode = new XNode(controlChildNode, controlChildXPath);

                    if (config.isXPathEListEnabled()) {

                        testNodeXPath = config.applyEListToSiblings() ? xTestChildNode.getNoIndexXPath()
                                                                       : xTestChildNode.getXPath();

                    }

                    if ((XMLUtil.isWhitespaceTextNode(testChildNode)) && (isIgnoringWhitespace())) {

                        // skip

                        // Ignore Whitespace only TEXT nodes

                        log("Ignoring Whitespace Node");

                    } else if (XMLUtil.isCommentNode(testChildNode) && (config.isIgnoringComments())) {

                        // skip

                        // Ignore Comment nodes

                        log("Ignoring Comment Node");

                    } else if ((testNodeXPath != null) && (config.getXPathEList().containsKey(testNodeXPath))) {

                        // exclude this if its Xpath is in the EList

                        log("Ignoring the Node since its XPath entry in EList file");

                    }

                    // Now we need to find Similar control nodes

                    else {

                        // Skip the global excluded Elements

                        if ((testChildNode.getNodeType() == Node.ELEMENT_NODE)
                                &&

                                (config.getExcludedElementsSet().contains(testChildNode.getNodeName()))) {

                            // Skip

                            log("compareChildNodes: Ignoring element child node at "
                                    + XMLUtil.generateXPath(testChildNode, isIgnoringWhitespace())
                                    +

                                    " since its in ignore list");

                        } else {

                            matchedNodeResult = compareSimilarNodes(xControlChildNode, xTestChildNode, nodeTracker);

                            log("CompareChildNodes: Matched node result " + matchedNodeResult.toString());

                            // Ignore Control nodes for non matched Test nodes

                            if (matchedNodeResult.getControlNode() != null) {

                                // Add this matched entry in the nodeTracker

                                // This is for the scenerio where multiple Test Nodes potentially

                                // match a given Control Node

                                nodeTracker.add(matchedNodeResult.getControlNode().getNode(), matchedNodeResult);

                            } else {

                                differences.add(matchedNodeResult);

                            }

                        }

                    }

                } // end for each test child Node

                // Node Tracker contains Control Nodes as keys and Matching Test nodes

                // as Objects, these Objects can be List if there are more Test nodes

                // for a given Control node

                // Go thru the matched Test nodes to see if there are more Test Nodes

                // for a given Control Node

                // Since potentially many Test Nodes can match one Control Node

                Object[] nodeResults = nodeTracker.elements();

                for (int i = 0; i < nodeResults.length; i++) {

                    Object nodeResult = nodeResults[i];

                    NodeResult minDiffNR;

                    // if its a List its MOV

                    if (nodeResult instanceof List) {

                        log("CompareChildNodes: ++++++ Adding child differences from LIST NodeResult "
                                + nodeResult.toString());

                        minDiffNR = (NodeResult) ((List) nodeResult).get(0);

                        for (int j = 1; j < ((List) nodeResult).size(); j++) {

                        	NodeResult currentNR = (NodeResult) ((List) nodeResult).get(j);

                            // Gather all the Nodes with more Differences

                            if ((currentNR.getNumDifferences()) < (minDiffNR.getNumDifferences())) {

                                if (!config.isCustomDifference()) {

                                    // If currentNR is an exact match

                                    if (currentNR.getNumDifferences() == 0) {

                                        differences.add("Added Node: Test Node " + minDiffNR.getTestNode().getXPath());

                                    } else {

                                        differences.add("Added Node/Multiple Matches: Current Node "
                                                + minDiffNR.getTestNode().getXPath()
                                                +

                                                " seems to match with already matched Golden Node at "
                                                + minDiffNR.getControlNode().getXPath());

                                        differences.add(minDiffNR);

                                    }

                                } else {

                                    XNode xTNode = new XNode(minDiffNR.getTestNode().getNode(),


                                            minDiffNR.getTestNode().getXPath());

                                    XNode xCNode = new XNode(minDiffNR.getControlNode().getNode(),


                                            minDiffNR.getControlNode().getXPath());

                                    Difference diff;

                                    if (currentNR.getNumDifferences() == 0) {

                                        diff = new Difference(DifferenceConstants.ADDED_NODE,


                                                xCNode, xTNode);

                                    } else {

                                        diff = new Difference(DifferenceConstants.MULTIPLE_MATCHES_ADDED_NODE,


                                                xCNode, xTNode);

                                    }

                                    differences.add(diff);

                                }

                                minDiffNR = currentNR;

                            } else {

                                if (!config.isCustomDifference()) {

                                    // If minNR is an exact match

                                    if (minDiffNR.getNumDifferences() == 0) {

                                        differences.add("Added Node: Test Node " + currentNR.getTestNode().getXPath());

                                    } else {

                                        differences.add("Added Node/Multiple matches: Current Node "
                                                + currentNR.getTestNode().getXPath()
                                                +

                                                " seems to match with already matched Golden Node at "
                                                + currentNR.getControlNode().getXPath());

                                        differences.add(currentNR);

                                    }

                                } else {

                                    XNode xTNode = new XNode(currentNR.getTestNode().getNode(),


                                            currentNR.getTestNode().getXPath());

                                    XNode xCNode = new XNode(minDiffNR.getControlNode().getNode(),


                                            currentNR.getControlNode().getXPath());

                                    Difference diff;

                                    if (minDiffNR.getNumDifferences() == 0) {

                                        diff = new Difference(DifferenceConstants.ADDED_NODE,


                                                xCNode, xTNode);

                                    } else {

                                        diff = new Difference(DifferenceConstants.MULTIPLE_MATCHES_ADDED_NODE,


                                                xCNode, xTNode);

                                    }

                                    differences.add(diff);

                                }

                            }

                        }

                        // The Node with least differences is now added

                        // to the list of differences

                        if ((!config.isIgnoringOrder())
                                &&

                                (minDiffNR.getControlNode().getPosition() != minDiffNR.getTestNode().getPosition())) {

                            if (!config.isCustomDifference()) {

                                differences.add("Position Mismatch: Current Node " + minDiffNR.getTestNode().getXPath()
                                        + " at position " + Integer.toString(minDiffNR.getTestNode().getPosition())
                                        +

                                        " matches " + (minDiffNR.isExactMatch() ? "" : "closely")
                                        +

                                        " with Golden Node " + minDiffNR.getControlNode().getXPath() + " at position "
                                        + Integer.toString(minDiffNR.getControlNode().getPosition()));

                            } else {

                                differences.add(new Difference(DifferenceConstants.POSITION_MISMATCH,


                                        minDiffNR.getControlNode(),


                                        minDiffNR.getTestNode()));
                            }

                        }

                        differences.add(minDiffNR);

                    } else {

                        NodeResult nr = (NodeResult) nodeResult;

                        log("compareChildNodes: ++++++ Adding child differences from INDIVIDUAL NodeResult "
                                + nodeResult.toString());

                        if ((!config.isIgnoringOrder())
                                &&

                                (nr.getControlNode().getPosition() != nr.getTestNode().getPosition())) {

                            if (!config.isCustomDifference()) {

                                differences.add("Position Mismatch: Current Node " + nr.getTestNode().getXPath()
                                        + " at position " + Integer.toString(nr.getTestNode().getPosition())
                                        +

                                        " matches " + (nr.isExactMatch() ? "" : "closely")
                                        +

                                        " with Golden Node " + nr.getControlNode().getXPath() + " at position "
                                        + Integer.toString(nr.getControlNode().getPosition()));

                            } else {

                                differences.add(new Difference(DifferenceConstants.POSITION_MISMATCH,


                                        nr.getControlNode(),


                                        nr.getTestNode()));
                            }

                        }

                        differences.add(nr);

                    }

                }

                String controlChildNodeXPathStr;

                // Go thru the Control Nodes to see if there are unmatched Control nodes

                for (int i = 0; i < controlChildNodes.getLength(); i++) {

                    controlChildNode = controlChildNodes.item(i);

                    controlChildNodeXPathStr = XMLUtil.generateXPath(controlChildNode, xControl.getXPath(),
                            ignoringWhitespace, includeNodeValueInXPath, false);

                    if (!nodeTracker.containsElementKey(controlChildNode)) {

                        if (config.applyEListToSiblings()) {

                            controlChildNodeXPathStr = XMLUtil.getNoIndexXPath(controlChildNodeXPathStr);
                        }

                        if (!config.getXPathEList().containsKey(controlChildNodeXPathStr)) {

                            reportNodeDifference(new XNode(controlChildNode,


                                    XMLUtil.generateXPath(controlChildNode, xControl.getXPath(), ignoringWhitespace,
                                        includeNodeValueInXPath, false)),


                                xTest, differences, "Missing Node: Current Node ");
                        }

                    }

                }

            }

            // Report all the Nodes under Test node as being NEWLY added

            else {

                testChildNodes = test.getChildNodes();

                String testChildNodeXPathStr;

                for (int i = 0; i < testChildNodes.getLength(); i++) {

                    testChildNode = testChildNodes.item(i);

                    testChildNodeXPathStr = XMLUtil.generateXPath(testChildNode, xTest.getXPath(), ignoringWhitespace,
                            includeNodeValueInXPath, false);

                    reportNodeDifference(xControl, new XNode(testChildNode, testChildNodeXPathStr),


                        differences, "Added Node: Test Node ");

                }

            } // end else NO control child Nodes

        } // end if Test node has Child Nodes

        // Report all the Control Nodes as being missing

        else {

            if (control.hasChildNodes()) {

                String controlChildNodeXPathStr;

                controlChildNodes = control.getChildNodes();

                for (int i = 0; i < controlChildNodes.getLength(); i++) {

                    controlChildNode = controlChildNodes.item(i);

                    controlChildNodeXPathStr = XMLUtil.generateXPath(controlChildNode, xControl.getXPath(),
                            ignoringWhitespace, includeNodeValueInXPath, false);

                    reportNodeDifference(new XNode(controlChildNode, controlChildNodeXPathStr), xTest,


                        differences, "Missing Node: Test document is missing node ");

                }

            }

        }

        return differences;

    }

    /**
     * Reports a Node difference, based on the configuration settings.
     *
     * @param  node         the Node to report the Differences for
     * @param  differences  the Differences to which all the differences will be added
     * @param  msg          the Message that will be appended before the actual difference String
     */

    private void reportNodeDifference(final XNode xControl, final XNode xTest, final Differences differences,
            final String msg) {

        Node control = xControl.getNode();

        Node test = xTest.getNode();

        if ((control == null) && (test == null)) {

            return;
        }

        XNode node = control == null ? xTest : xControl;

        if ((XMLUtil.isWhitespaceTextNode(node.getNode())) && (isIgnoringWhitespace())) {

            // skip

        } else if (XMLUtil.isCommentNode(node.getNode()) && (config.isIgnoringComments())) {

            // skip

            // Ignore Comment nodes

        } else {

            // Skip the excluded Elements

            if ((node.getNode().getNodeType() == Node.ELEMENT_NODE)
                    &&

                    (config.getExcludedElementsSet().contains(node.getNode().getNodeName()))) {

                // Skip

                log("Ignoring element child node at " + node.getXPath()
                        +

                        " since its in ignore list");

            } else {

                if (!config.isCustomDifference()) {

                    differences.add(msg + node.getXPath());

                } else {
                	
                    Difference diff = new Difference(DifferenceConstants.NODE_NOT_FOUND, xControl, xTest);

                    differences.add(diff);

                }

            }

        }

    }

    /**
     * Prints msg to System.out.
     */

    public static void log(final String msg) {

        if (DEBUG) {

            logger.info("Comparator:" + msg);
        }

    }

    /**
     * Prints msg and Exception to System.out.
     */

    public static void log(final String msg, final Throwable t) {

        if (DEBUG) {

            log(msg);

            logger.error(t);

        }

    }

}
