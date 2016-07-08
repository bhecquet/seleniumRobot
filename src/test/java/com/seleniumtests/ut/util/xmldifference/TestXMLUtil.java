package com.seleniumtests.ut.util.xmldifference;

import com.seleniumtests.util.xmldifference.XMLUtil;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TestXMLUtil {

	/**
     * Test if the method can print an XML document.
     * (work for the old version)
     */
    @Test(groups={"ut"})
    public void testPrintDocumentXML() {
    	String docPath = "src/test/resources/tu/xmlFileToTest.xml";
    	String docString = getXmlToTest();
    	
    	Node docXml = null;
    	try {
			docXml = XMLUtil.getDocument(docPath);
		} catch (SAXException|IOException e) {
			e.printStackTrace();
		}
    	String result = XMLUtil.print(docXml, false, false);
    	
    	boolean showStringDifference = false;
    	if (showStringDifference) {
    		System.out.println("===== XML to test : ==========\n" + docString + "\n==============================");
    		System.out.println("===== XML from method :=======\n" + result + "\n==============================");
    	}
    	Assert.assertTrue(compareStrings(docString, result, showStringDifference));
    }
	
    /**
     * 
     * @return a String of the xmlFileToTest.xml
     */
    public String getXmlToTest() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    	sb.append("<testng-results failed=\"2\" nul=\"false\" passed=\"0\" skipped=\"0\" total=\"2\">\n");
    	sb.append("  <reporter-output>\n");
    	sb.append("    <line pepe=\"youpla\">\n");
    	sb.append("      someValue\n");
    	sb.append("    </line>\n");
    	sb.append("    <correct>\n");
    	sb.append("     <![CDATA[test]]>\n");
    	sb.append("    </correct>\n");
    	sb.append("  </reporter-output>\n");
    	sb.append("</testng-results>");
    	return sb.toString();
    }
    
    /**
     * Shows where two strings are different
     * @param a
     * @param b
     * @return true if strings are almost* the same
     * *almost, because the back line character can differ from ascii 10 to 13. 
     */
    public boolean compareStrings(String a, String b, boolean shows){
    	if (a==null || b==null) 
    		return false;
    	int lengthA = a.length();
    	int lengthB = b.length();
    	if (lengthA != lengthB) {
    		if (shows)
    			System.out.println("String A has " + lengthA + " characters, whereas String B has " + lengthB);
    	}
    	
    	int j=0;
    	char chA;
		char chB;
		int asciiA;
		int asciiB;
		
    	for (int i=0; i<lengthA; i++) {
    		chA = a.charAt(i);
    		chB = b.charAt(j);
    		if (chA == chB){
    			if (shows) {System.out.println("i:"+i+";j:"+j+" ; a:" + chA + " = b:" + chB);}
    		}
    		else {
    			asciiA = (int) chA;
    			asciiB = (int) chB;
    			if (shows) {
    				System.out.println("i:"+i+";j:"+j+" ; a:" + chA + " (ascii " + asciiA + ") "
    										+ "!= b:" + chB + " (ascii " + asciiB + ")");
    			}
    			if ((asciiA == 10 || asciiA == 13) && (asciiB == 10 || asciiB == 13)) {
    				if (shows) {System.out.println("i:"+i+";j:"+j+" (different back to line char) ");}
    				
    	    		asciiA = (int) a.charAt(i+1);
    				if (asciiA == 10 || asciiA == 13) i++;
    				
    				asciiB = (int) b.charAt(j+1);
    				if (asciiB == 10 || asciiB == 13) j++;
    				
    			} else {
    				return false;
    			}
    		}
    		j++;
    	}
    	if (shows)
			System.out.println("=> the strings are the same.");
    	return true;
    }
    
	/**
     * Test if the method returns the name of the Xpath 
     * without the index information.
     */
    @Test(groups={"ut"})
    public void testGetNoIndexXPath() {
    	String xPath = "xPathValue[25]";
    	String result = XMLUtil.getNoIndexXPath(xPath);
		Assert.assertEquals(result, "xPathValue");
    }
}
