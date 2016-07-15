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

import com.google.api.client.util.StringUtils;



/**
 * NodeResult class used to store the Control Node, Test Node and Differences between them.
 *
 * <p/>
 * <br>
 * NodeResult is used to pass results between classes in the XMLDog Application
 */

public class NodeResult implements Serializable {

    private XNode nrTestNode = null;

    private XNode nrControlNode = null;

    private Differences nrDifferences = null;

    private boolean nrUniqueAttrMatch = false;

    private boolean nrIsExactMatch = false;


    /**
     * Constructor.
     *
     * @param  controlNode  the Control Node
     * @param  testNode     the test Node
     * @param  diff         the Differences
     *
     * @see    Differences
     */

    public NodeResult(final XNode controlNode, final XNode testNode, final Differences diff) {

        nrControlNode = controlNode;

        nrTestNode = testNode;

        nrDifferences = diff;

    }

    /**
     * Gets Test Node.
     */

    public XNode getTestNode() {

        return nrTestNode;

    }

    /**
     * Sets Test Node.
     */

    public void setTestNode(final XNode node) {

        nrTestNode = node;

    }

    /**
     * Gets control Node.
     */

    public XNode getControlNode() {

        return nrControlNode;

    }

    /**
     * Sets Control Node.
     */

    public void setControlNode(final XNode node) {

        nrControlNode = node;

    }

    /**
     * Get Differences.
     *
     * @see  Differences
     */

    public Differences getDifferences() {

        return nrDifferences;

    }

    /**
     * Sets Differences.
     *
     * @see  Differences
     */

    public void setDifferences(final Differences diff) {

        nrDifferences = diff;

    }

    /**
     * Checks if unique Attribute matches.
     */

    public boolean isUniqueAttrMatch() {

        return nrUniqueAttrMatch;

    }

    /**
     * Sets Unique Attribute match flag.
     */

    public void setUniqueAttrMatch(final boolean flag) {

        nrUniqueAttrMatch = flag;

    }

    /**
     * Checks if its an exact match.
     */

    public boolean isExactMatch() {

        return nrIsExactMatch;

    }

    /**
     * Sets the flag if its an exact match.
     */

    public void setIfExactMatch(final boolean flag) {

        nrIsExactMatch = flag;

    }

    /**
     * Checks if the NodeResult is a match, either by unique attribute.
     *
     * <p/>or an exact match
     *
     * <p/>
     * <br>
     * Convenience method, since sometimes it is JUST useful to
     *
     * <p/>check if its match, immaterial of the type of match
     */

    public boolean isMatch() {

        return isExactMatch() || isUniqueAttrMatch();

    }

    /**
     * Gets number of Differences for given Nodes.
     */

    public int getNumDifferences() {

        if (nrDifferences == null) {

            return 0;
        }

        return nrDifferences.size();

    }

    /**
     * Gets String representation of the Instance.
     */

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("NodeResult[");

        sb.append(StringUtils.LINE_SEPARATOR);

        sb.append("Golden");

        sb.append(XMLUtil.getNodeBasics(getControlNode().getNode()));

        sb.append(StringUtils.LINE_SEPARATOR);

        sb.append("Current");

        sb.append(StringUtils.LINE_SEPARATOR);

        sb.append(XMLUtil.getNodeBasics(getTestNode().getNode()));

        sb.append(StringUtils.LINE_SEPARATOR);

        sb.append(getDifferences());

        sb.append(StringUtils.LINE_SEPARATOR + "]");

        return sb.toString();

    }

}
