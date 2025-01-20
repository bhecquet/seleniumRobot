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
           &lt;element ref="{}plugin" maxOccurs="unbounded"/&gt;
         &lt;/sequence&gt;
       &lt;/restriction&gt;
     &lt;/complexContent&gt;
   &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"plugin"})
@XmlRootElement(name = "plugins")
public class SeleniumTestsPlugins {

    @XmlElement(required = true)
    protected List<Plugin> plugin;

    /**
     * Gets the value of the plugin property.
     *
     * 
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
     * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
     * method for the plugin property.
     *
     * 
     * For example, to add a new item, do as follows:
     *
     * 
     * <pre>
       getPlugin().add(newItem);
     * </pre>
     *
     * 
     * 
     * Objects of the following type(s) are allowed in the list {@link Plugin }
     */
    public List<Plugin> getPlugin() {
        if (plugin == null) {
            plugin = new ArrayList<>();
        }

        return this.plugin;
    }

}
