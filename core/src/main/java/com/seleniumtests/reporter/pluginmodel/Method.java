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
         &lt;sequence&gt;
           &lt;element ref="{}page" maxOccurs="unbounded" minOccurs="0"/&gt;
         &lt;/sequence&gt;
         &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
       &lt;/restriction&gt;
     &lt;/complexContent&gt;
   &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"page"})
@XmlRootElement(name = "method")
public class Method {

    protected List<Page> page;
    @XmlAttribute(required = true)
    protected String name;

    /**
     * Gets the value of the name property.
     *
     * @return  possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the page property.
     *
     * <br>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
     * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
     * method for the page property.
     *
     * <br>
     * <br>For example, to add a new item, do as follows:
     *
     * <br>
     * <pre>
       getPage().add(newItem);
     * </pre>

     * Objects of the following type(s) are allowed in the list {@link Page }
     */
    public List<Page> getPage() {
        if (page == null) {
            page = new ArrayList<>();
        }

        return this.page;
    }

    /**
     * Sets the value of the name property.
     *
     * @param  value  allowed object is {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

}
