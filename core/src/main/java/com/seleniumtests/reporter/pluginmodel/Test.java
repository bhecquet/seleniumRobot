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

import java.util.ArrayList;
import java.util.List;

/**
 * Java class for anonymous complex type.
 *
 * 
 * The following schema fragment specifies the expected content contained within this class.
 *
 * 
 * <pre>
   &lt;complexType&gt;
     &lt;complexContent&gt;
       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         &lt;sequence&gt;
           &lt;element ref="{}page" maxOccurs="unbounded" minOccurs="0"/&gt;
           &lt;element ref="{}method" maxOccurs="unbounded" minOccurs="0"/&gt;
         &lt;/sequence&gt;
         &lt;attribute name="class-name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
       &lt;/restriction&gt;
     &lt;/complexContent&gt;
   &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"page", "method"})
@XmlRootElement(name = "test")
public class Test {

    protected List<Page> page;
    protected List<Method> method;
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
     * Gets the value of the method property.
     *
     * 
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
     * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
     * method for the method property.
     *
     * 
     * For example, to add a new item, do as follows:
     *
     * 
     * <pre>
       getMethod().add(newItem);
     * </pre>
     *
     * 
     * 
     * Objects of the following type(s) are allowed in the list {@link Method }
     */
    public List<Method> getMethod() {
        if (method == null) {
            method = new ArrayList<>();
        }

        return this.method;
    }

    /**
     * Gets the value of the page property.
     *
     * 
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
     * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
     * method for the page property.
     *
     * 
     * For example, to add a new item, do as follows:
     *
     * 
     * <pre>
       getPage().add(newItem);
     * </pre>
     *
     * 
     * 
     * Objects of the following type(s) are allowed in the list {@link Page }
     */
    public List<Page> getPage() {
        if (page == null) {
            page = new ArrayList<>();
        }

        return this.page;
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
