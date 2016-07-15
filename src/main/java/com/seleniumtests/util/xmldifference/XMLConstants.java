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
 * To change this generated comment edit the template variable "typecomment": Window>Preferences>Java>Templates. To
 * enable and disable the creation of type comments go to Window>Preferences>Java>Code Generation.
 */

/**
 * A convenient place to hang constants relating to general XML usage.
 */

public final class XMLConstants {

    /**
     * &lt;?xml&greaterThan; declaration.
     */

    public static final String XML_DECLARATION =

        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    /**
     * xmlns attribute prefix.
     */

    public static final String XMLNS_PREFIX = "xmlns";

    /**
     * "&lt;"
     */

    public static final String OPEN_START_NODE = "<";

    /**
     * "&lt;/"
     */

    public static final String OPEN_END_NODE = "</";

    /**
     * "&greaterThan;"
     */

    public static final String CLOSE_NODE = ">";

    /**
     * "![CDATA["
     */

    public static final String START_CDATA = "![CDATA[";

    /**
     * "]]"
     */

    public static final String END_CDATA = "]]";

    /**
     * "!--"
     */

    public static final String START_COMMENT = "!--";

    /**
     * "--""
     */

    public static final String END_COMMENT = "--";

    /**
     * "?"
     */

    public static final String START_PROCESSING_INSTRUCTION = "?";

    /**
     * "?"
     */

    public static final String END_PROCESSING_INSTRUCTION = "?";

    /**
     * "!DOCTYPE"
     */

    public static final String START_DOCTYPE = "!DOCTYPE ";

    /**
     * "/"
     */

    public static final String XPATH_SEPARATOR = "/";

    /**
     * "["
     */

    public static final String XPATH_NODE_INDEX_START = "[";

    /**
     * "]"
     */

    public static final String XPATH_NODE_INDEX_END = "]";

    /**
     * "comment()"
     */

    public static final String XPATH_COMMENT_IDENTIFIER = "comment()";

    /**
     * "processing-instruction()"
     */

    public static final String XPATH_PROCESSING_INSTRUCTION_IDENTIFIER = "processing-instruction()";

    /**
     * "text()"
     */

    public static final String XPATH_CHARACTER_NODE_IDENTIFIER = "text()";

    /**
     * "&at;"
     */

    public static final String XPATH_ATTRIBUTE_IDENTIFIER = "@";

    /**
     * Regular Expression startor for Exclusion List.
     */

    public static final String XPATH_REGEX_BEGIN = "[";

    /**
     * Regular Expression terminator for Exclusion List.
     */

    public static final String XPATH_REGEX_END = "]";

}
