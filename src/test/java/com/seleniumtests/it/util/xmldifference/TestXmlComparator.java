/*
 * Copyright 2016 www.infotel.com
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

package com.seleniumtests.it.util.xmldifference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.seleniumtests.util.xmldifference.Config;
import com.seleniumtests.util.xmldifference.Differences;
import com.seleniumtests.util.xmldifference.XMLDog;
import com.seleniumtests.util.xmldifference.XMLUtil;

/**
 * Test Comparator xml
 * @author Sophie
 *
 */
public class TestXmlComparator {
	String comparePath = Thread.currentThread().getContextClassLoader().getResource("ti/compare/testCompare.xml").getFile();
	String toComparePath = Thread.currentThread().getContextClassLoader().getResource("ti/toCompare/testCompare.xml").getFile();
	File compare = new File(comparePath);
	File toCompare = new File(toComparePath);
	XMLDog xmldog = null;

	@Test(groups={"it"})
	public void testCompareSameFile() throws SAXException, IOException{
		try{
		xmldog = new XMLDog();
		}
		catch(ParserConfigurationException e){
			return;
		}
		Differences difference = xmldog.compare(compare, compare);
		Assert.assertTrue(difference.getHTML().contains("Nodes are identical, No differences found"));
	}
	
	@Test(groups={"it"})
	public void testCompareWithXmlPaths() throws SAXException, IOException{
		try{
		xmldog = new XMLDog();
		}
		catch(ParserConfigurationException e){
			return;
		}
		Differences difference = xmldog.compare(comparePath, toComparePath);
		Assert.assertTrue(difference.getHTML().contains("Different Attributes"));
		Assert.assertTrue(difference.getHTML().contains("[@skipped=0]"));
		Assert.assertTrue(difference.getHTML().contains("[@skipped=1]"));
		Assert.assertTrue(difference.getHTML().contains("[@failed=2]"));
		Assert.assertTrue(difference.getHTML().contains("[@failed=3]"));
		Assert.assertTrue(difference.getHTML().contains("[@total=2]"));
		Assert.assertTrue(difference.getHTML().contains("[@total=5]"));
		Assert.assertTrue(difference.getHTML().contains("[@passed=0]"));
		Assert.assertTrue(difference.getHTML().contains("[@passed=1]"));
		Assert.assertTrue(difference.getHTML().contains("[@pepe=youpla]"));
		Assert.assertTrue(difference.getHTML().contains("[@pepe=youp]"));
		Assert.assertTrue(difference.getHTML().contains("Missing Attribute"));
		Assert.assertTrue(difference.getHTML().contains("Missing Node"));
	}
	
	@Test(groups={"it"})
	public void testCompareWithXmlFiles() throws SAXException, IOException{
		try{
		xmldog = new XMLDog();
		}
		catch(ParserConfigurationException e){
			return;
		}
		Differences difference = xmldog.compare(compare, toCompare);
		Assert.assertTrue(difference.getHTML().contains("Different Attributes"));
		Assert.assertTrue(difference.getHTML().contains("Missing Node"));
	}
	
	
	@Test(groups={"it"})
	public void testCompareXmlDirs() throws SAXException, IOException{
		try{
		xmldog = new XMLDog();
		}
		catch(ParserConfigurationException e){
			return;
		}
		xmldog.compareDir(comparePath.split("/testComp")[0], toComparePath.split("/testComp")[0], comparePath.split("/compare")[0].concat("/result"));
	}
	
	@Test(groups={"it"})
	public void testCompareXmlPathsWithExcludedAttribute() throws SAXException, IOException{
		Config config = new Config();
		config.addExcludedAttribute("testng-results","total");
		try{
		xmldog = new XMLDog(config);
		}
		catch(ParserConfigurationException e){
			return;
		}
		Differences difference = xmldog.compare(comparePath, toComparePath);
		Assert.assertTrue(difference.getHTML().contains("Different Attributes"));
		Assert.assertFalse(difference.getHTML().contains("[@total=2]"));
		Assert.assertFalse(difference.getHTML().contains("[@total=5]"));
		Assert.assertTrue(difference.getHTML().contains("Missing Node"));

	}
	
	@Test(groups={"it"})
	public void testCompareXmlPathsWithExcludedAttributes() throws SAXException, IOException{
		Config config = new Config();
		List<String> attributes = new ArrayList<String>();
		attributes.add("failed");
		attributes.add("total");
		config.addExcludedAttributes("testng-results",attributes);
		try{
		xmldog = new XMLDog(config);
		}
		catch(ParserConfigurationException e){
			return;
		}
		Differences difference = xmldog.compare(comparePath, toComparePath);
		Assert.assertTrue(difference.getHTML().contains("Different Attributes"));
		Assert.assertFalse(difference.getHTML().contains("[@failed=2]"));
		Assert.assertFalse(difference.getHTML().contains("[@failed=3]"));
		Assert.assertFalse(difference.getHTML().contains("[@total=2]"));
		Assert.assertFalse(difference.getHTML().contains("[@total=5]"));
		Assert.assertTrue(difference.getHTML().contains("Missing Node"));
	}
	
	@Test(groups={"it"})
	public void testCompareXmlPathsWithExcludedElement() throws SAXException, IOException{
		Config config = new Config();
		config.addExcludedElement("correct");
		try{
		xmldog = new XMLDog(config);
		}
		catch(ParserConfigurationException e){
			return;
		}
		Differences difference = xmldog.compare(comparePath, toComparePath);
		Assert.assertTrue(difference.getHTML().contains("Different Attributes"));
		Assert.assertFalse(difference.getHTML().contains("Missing Node"));
	}
	
	@Test(groups={"it"})
	public void testParseRegEx() {
		Assert.assertTrue(Config.parseRegEx("[@toto=prince]").equals("prince"));
	}
	
	@Test(groups={"it"})
	public void testPrintNode() throws SAXException, IOException{
		Document docxml = XMLUtil.getDocument(comparePath);
		Assert.assertTrue(XMLUtil.print(docxml,false, true).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
		Assert.assertTrue(XMLUtil.print(docxml,false, true).contains("<testng-results"));
	}
	
	@Test(groups={"it"})
	public void testXMLUtilGetText() throws SAXException, IOException{
		Document doc = XMLUtil.getDocument(comparePath);
		NodeList nodes = doc.getElementsByTagName("correct");
		Assert.assertTrue(XMLUtil.getText(nodes.item(0)).trim().equals("test"));
	}
	
	@Test(groups={"it"})
	public void testXMLUtilIsStrElementNode() throws SAXException, IOException{
		Document doc = XMLUtil.getDocument(comparePath);
		NodeList nodes = doc.getElementsByTagName("correct");
		String toCompare = "correct";
		Assert.assertTrue(XMLUtil.isStrElementNode(toCompare,nodes.item(0),false));
	}
	
	@Test(groups={"it"})
	public void testXMLUtilreplaceElementText() throws SAXException, IOException{
		List<String> list = new ArrayList<>();
		list.add("bleu");
		XMLUtil.replaceElementText(comparePath, "correct", list, true);
		Document doc = XMLUtil.getDocument(comparePath);
		NodeList nodes = doc.getElementsByTagName("correct");
		Assert.assertEquals(XMLUtil.getText(nodes.item(0)).trim(), "bleu");
		list.clear();
		list.add("test");
		XMLUtil.replaceElementText(comparePath, "correct", list, true);
	}
	
	@Test(groups={"it"})
	public void  testXMLUtilreplaceElementTextWithMap() throws SAXException, IOException{
		Map map = new HashMap();
		map.put("line", "jaune");
		XMLUtil.replaceElementText(toComparePath, map, true, true);
		Document doc = XMLUtil.getDocument(toComparePath);
		NodeList nodes = doc.getElementsByTagName("line");
		Assert.assertEquals(XMLUtil.getText(nodes.item(0)).trim(), "jaune");
		map.put("line", "<![CDATA[@@lt@@li^^greaterThan^^@@lt@@b^^greaterThan^^@@lt@@font color='#6600CC'^^greaterThan^^Variable appURL is not defined@@lt@@/font^^greaterThan^^@@lt@@/b^^greaterThan^^@@lt@@/li^^greaterThan^^]]>");
		XMLUtil.replaceElementText(toComparePath, map, true, true);
	}
	

}
