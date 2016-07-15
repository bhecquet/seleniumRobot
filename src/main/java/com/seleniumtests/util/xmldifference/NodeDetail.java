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

import org.w3c.dom.Node;

/**
 * Class that mimics XMLUnit <i>NodeDetail</i> class. Primarily used
 *
 * <p/>to store the details about the Node being compared
 */

public class NodeDetail {

    private Node ndNode = null;

    private String ndXpath = null;

    private String ndValue = null;

    /**
     * Constructor.
     *
     * @param  node  the Document Node
     *
     * @paran  xpath the XPath location of the Node
     */

    public NodeDetail(final Node node, final String xpath) {

        ndNode = node;

        ndXpath = xpath;

    }

    /**
     * Constructor.
     *
     * @param  value  the value of the Node
     * @param  node   the Document Node
     *
     * @paran  xpath the XPath location of the Node
     */

    public NodeDetail(final String value, final Node node, final String xpath) {

        ndValue = value;

        ndNode = node;

        ndXpath = xpath;

    }

    /**
     * Gets the Node.
     *
     * @return  the Document Node
     */

    public Node getNode() {

        return ndNode;

    }

    /**
     * Sets the Node.
     *
     * @param  node  the Document Node
     */

    public void setNode(final Node node) {

        ndNode = node;

    }

    /**
     * Gets XPath expression for the Node.
     *
     * @return  the XPath expression for the Node
     */

    public String getXPathLocation() {

        return ndXpath;

    }

    /**
     * Sets XPath expression for the Node.
     *
     * @param  xpath  the XPath expression for the Node
     */

    public void setXPathLocation(final String xpath) {

        ndXpath = xpath;

    }

    /**
     * Gets the value of the Node.
     *
     * @return  the Node value
     */

    public String getValue() {

        if (ndValue == null) {

            return getNode().getNodeValue();
        }

        return ndValue;

    }

}
