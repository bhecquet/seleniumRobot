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
package com.seleniumtests.reporter.pluginmodel;

import jakarta.xml.bind.annotation.*;

/**
 * <p/>Java class for anonymous complex type.
 *
 * <p/>
 * <p/>The following schema fragment specifies the expected content contained within this class.
 *
 * <p/>
 * <pre>
   &lt;complexType&gt;
     &lt;complexContent&gt;
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         &lt;attribute name="class-name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
       &lt;/restriction&gt;
     &lt;/complexContent&gt;
   &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "page")
public class Page {

    @XmlAttribute(name = "class-name", required = true)
    protected String className;

    /**
     * Gets the value of the className property.
     *
     * @return  possible object is {@link String }
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setClassName(final String value) {
        this.className = value;
    }

}
