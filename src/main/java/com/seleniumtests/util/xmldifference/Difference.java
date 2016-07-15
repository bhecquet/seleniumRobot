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

/**
 * Value object that describes a difference between DOM Nodes using one of.
 *
 * <p/>the DifferenceConstants ID values and a XNode instance.
 *
 * <p/>
 * <br />
 * Examples and more at <a href="http://xmlunit.sourceforge.net"/>xmlunit.sourceforge.net</a>
 *
 * @see  XNode
 */

public class Difference {

    /**
     * Simple unique identifier.
     */

    private int id;

    /**
     * Description of the difference.
     */

    private final String description;

    /**
     * TRUE if the difference represents a similarity, FALSE otherwise.
     */

    private boolean recoverable;

    private XNode xControlNode = null;

    private XNode xTestNode = null;

    /**
     * Constructor for non-similar Difference instances.
     *
     * @param  id
     * @param  description
     */

    protected Difference(final int id, final String description) {

        this(id, description, false);

    }

    /**
     * Constructor for similar Difference instances.
     *
     * @param  id
     * @param  description
     */

    protected Difference(final int id, final String description, final boolean recoverable) {

        this.id = id;

        this.description = description;

        this.recoverable = recoverable;

    }

    /**
     * Copy constructor using prototype Difference and.
     *
     * <p/>encountered NodeDetails
     */

    protected Difference(final Difference prototype, final XNode xControlNode, final XNode xTestNode) {

        this(prototype.getId(), prototype.getDescription(), prototype.isRecoverable());

        this.xControlNode = xControlNode;

        this.xTestNode = xTestNode;

    }

    /**
     * @return  the id
     */

    public int getId() {

        return id;

    }

    public void setId(final int id) {

        this.id = id;

    }

    /**
     * @return  the description
     */

    public String getDescription() {

        return description;

    }

    /**
     * @return  TRUE if the difference represents a similarity, FALSE otherwise
     */

    public boolean isRecoverable() {

        return recoverable;

    }

    /**
     * Allow the recoverable field value to be overridden.
     *
     * <p/>Used when an override DifferenceListener is used in conjunction with
     *
     * <p/>a DetailedDiff.
     */

    protected void setRecoverable(final boolean overrideValue) {

        recoverable = overrideValue;

    }

    /**
     * @return  the XNode from the piece of XML used as the control
     *
     *          <p/>at the Node where this difference was encountered
     */

    public XNode getControlNodeDetail() {

        return xControlNode;

    }

    /**
     * Sets control Node detail.
     */

    public void setControlNodeDetail(final XNode xNode) {

        xControlNode = xNode;

    }

    /**
     * @return  the XNode from the piece of XML used as the test
     *
     *          <p/>at the Node where this difference was encountered
     */

    public XNode getTestNodeDetail() {

        return xTestNode;

    }

    /**
     * Sets test Node detail.
     */

    public void setTestNodeDetail(final XNode xNode) {

        xTestNode = xNode;

    }

    /**
     * @return  a basic representation of the object state and identity
     *
     *          <p/>and if <code>XNode</code> instances are populated append
     *
     *          <p/>their details also
     */

    @Override
    public String toString() {

        StringBuilder buf = new StringBuilder();

        if (xControlNode == null || xTestNode == null) {

            appendBasicRepresentation(buf);

        } else {

            appendDetailedRepresentation(buf);

        }

        return buf.toString();

    }

    private void appendBasicRepresentation(final StringBuilder buf) {

        buf.append("Difference (#").append(id).append(") ").append(description);

    }

    private void appendDetailedRepresentation(final StringBuilder buf) {

        buf.append("Expected ").append(getDescription()).append(" '").append(xControlNode.getValue())
           .append("' but was '").append(xTestNode.getValue()).append("' - comparing ");

        NodeDescription.appendNodeDetail(buf, xControlNode);

        buf.append(" to ");

        NodeDescription.appendNodeDetail(buf, xTestNode);

    }

}
