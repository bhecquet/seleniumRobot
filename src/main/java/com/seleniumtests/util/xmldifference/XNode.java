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

import java.io.Serializable;

import org.w3c.dom.Node;

/**
 * Convenience Node class containing the Node itself and corresponding XPath.
 *
 * <p/>expression.
 *
 * <p/>
 * <br>
 * This can be used to improve the performance where XPath of all the children
 *
 * <p/>needs to be computed, saves time not traversing to the ROOT multiple times.
 */
public class XNode implements Serializable {

    private Node node = null;

    private String xPath = null;

    private String noIndexXPath = null;

    private int position = 0; // Position of the Node under a parent

    private int depth = 0; // Depth of the Node in the Document

    /**
     * Default Constructor.
     */
    public XNode() { }

    /**
     * Constructor.
     *
     * @param  node   the Node
     * @param  xPath  the XPath expression for the Node
     */
    public XNode(final Node node, final String xPath) {

        this.node = node;
        this.xPath = xPath;
    }

    /**
     * Constructor.
     *
     * @param  node          the Node
     * @param  xPath         the XPath expression for the Node
     * @param  noIndexXPath  the XPath expression without the indexes
     */
    public XNode(final Node node, final String xPath, final String noIndexXPath) {

        this.node = node;
        this.xPath = xPath;
        this.noIndexXPath = noIndexXPath;
    }

    /**
     * Gets Node.
     */
    public Node getNode() {

        return node;
    }

    /**
     * Sets Node.
     */
    public void setNode(final Node node) {

        this.node = node;
    }

    /**
     * Gets the position of the Node under the parent.
     */
    public int getPosition() {

        return position;
    }

    /**
     * Sets the position of the Node under the parent.
     */
    public void setPosition(final int position) {

        this.position = position;
    }

    /**
     * Gets the depth of the Node in the Document.
     */
    public int getDepth() {

        return depth;
    }

    /**
     * Sets the depth of the Node in the Document.
     */
    public void setDepth(final int depth) {

        this.depth = depth;
    }

    /**
     * Gets the XPath expression.
     */
    public String getXPath() {

        return xPath;
    }

    /**
     * Sets XPath expression.
     */
    public void setXPath(final String xPath) {

        this.xPath = xPath;
    }

    /**
     * Gets XPath expression without the indexes.
     */
    public String getNoIndexXPath() {

        String xPath = getXPath();
        xPath = XMLUtil.getNoIndexXPath(xPath);
        setNoIndexXPath(xPath);

        return xPath;
    }

    /**
     * Sets XPath expression without the indexes.
     */
    public void setNoIndexXPath(final String noIndexXPath) {

        this.noIndexXPath = noIndexXPath;
    }

    /**
     * Gets Node Value.
     */
    public String getValue() {

        if (node == null) {

            return null;
        }

        return node.getNodeValue();
    }

    /**
     * Gets Node Name.
     */
    public String getName() {

        if (node == null) {

            return null;
        }

        return node.getNodeName();
    }

    /**
     * Gets String representation of the XNode.
     */
    public String toString() {

        String eol = System.getProperty("line.separator");

        StringBuffer sb = new StringBuffer("XNode:[");

        sb.append("Node Name:" + getNode().getNodeName());

        sb.append(eol);

        sb.append("Node Value:" + getNode().getNodeValue());

        sb.append(eol);

        sb.append("Node Type:" + getNode().getNodeType());

        sb.append(eol);

        sb.append("Node XPath:" + getXPath());

        sb.append(eol);

        sb.append("]");

        return sb.toString();
    }

}
