/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

package com.seleniumtests.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for parsing additional configuration
 * @author behe
 *
 */
public class TestConfigurationParser {
	
	private Document doc;
	
	public TestConfigurationParser(final String xmlFileName) {
		File xmlFile = new File(xmlFileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

    public List<Node> getParameterNodes() {
        List<Node> nList = new ArrayList<Node>();
        NodeList tmpnList = doc.getFirstChild().getChildNodes();
        
        for (int i = 0; i < tmpnList.getLength(); i++) {
            Node nNode = tmpnList.item(i);
            if (nNode.getNodeName().equals("parameter")) {
            	nList.add(nNode);
            }
        }

        return nList;
    }
    
    public List<Node> getDeviceNodes() {
    	List<Node> nList = new ArrayList<Node>();
    	NodeList tmpnList = doc.getElementsByTagName("device");
        
        for (int i = 0; i < tmpnList.getLength(); i++) {
            Node nNode = tmpnList.item(i);
            nList.add(nNode);
        }
    	return nList;
    }
    
    /**
     * returns a json string containing all devices information
     * @return
     */
    public String getDeviceNodesAsJson() {
    	JSONObject devices = new JSONObject();
    	for (Node node: getDeviceNodes()) {
    		devices.put(node.getAttributes().getNamedItem("name").getNodeValue(), node.getAttributes().getNamedItem("platform").getNodeValue());
    	}
    	return devices.toString();
    }
    
    public List<Node> getServiceNodes() {
    	List<Node> nList = new ArrayList<Node>();
    	NodeList tmpnList = doc.getElementsByTagName("service");
        
        for (int i = 0; i < tmpnList.getLength(); i++) {
            Node nNode = tmpnList.item(i);
            nList.add(nNode);
        }
    	return nList;
    }

}
