/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
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
package com.seleniumtests.uipage.htmlelements;

import org.openqa.selenium.By;

public class LinkElement extends HtmlElement {

    public LinkElement(final String label, final By by) {
        super(label, by);
    }
    
    public LinkElement(final String label, final By by, final HtmlElement parent) {
    	super(label, by, parent);
    }

    public LinkElement(final String label, final By by, final HtmlElement parent, final Integer index) {
    	super(label, by, parent, index);
    }
    
    public LinkElement(final String label, final By by, final Integer index) {
    	super(label, by, index);
    }
    
    public LinkElement(final String label, final By by, final FrameElement frame) {
    	super(label, by, frame);
    }
    
    public LinkElement(final String label, final By by, final FrameElement frame, final Integer index) {
    	super(label, by, frame, index);
    }

    public String getUrl() {
        return super.getAttribute("href");
    }
}
