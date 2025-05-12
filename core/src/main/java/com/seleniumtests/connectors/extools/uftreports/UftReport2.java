package com.seleniumtests.connectors.extools.uftreports;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jsoup.Jsoup;
import org.testng.Reporter;
import org.xml.sax.InputSource;

import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestStep;

/**
 * Class for analyzing "Result viewer" XML report
 *
 */
public class UftReport2 extends IUftReport {
	
	private static final String TAG_STEP = "Step";
	private static final String TAG_ACTION = "Action";

	public UftReport2(String xmlReport, String scriptName) {
		super(xmlReport, scriptName);
	}

	public List<TestStep> readXmlResult() {
        Document document;
        SAXBuilder builder = new SAXBuilder();

        List<TestStep> listStep = new ArrayList<>();

        try {
            String xml = xmlReport.substring(xmlReport.indexOf("<"));
            String xml10pattern = "[^"
		        + "\u0009\r\n"
		        + "\u0020-\uD7FF"
		        + "\uE000-\uFFFD"
		        + "\ud800\udc00-\udbff\udfff"
		        + "]";
            xml = xml.replaceAll(xml10pattern, "");
		
            document = builder.build(new InputSource(new StringReader(xml))); // we skip BOM by searching the first "<" character
            Element docElement = document.getRootElement().getChild("Doc");
            Element elementToIterate = docElement.getChild("DIter");
            Element iterationChild = elementToIterate.getChild(TAG_ACTION);
            
			if (!iterationChild.getChildren(TAG_ACTION).isEmpty()) {
				  elementToIterate = iterationChild;
			}
			
			for (Element element : elementToIterate.getChildren()) {
                if (TAG_ACTION.equals(element.getName())) {
                    TestStep readStep = readAction(element);
                    listStep.add(readStep);
                } else if (TAG_STEP.equals(element.getName())) {
                    readStep(element);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            addStepWithoutXml(scriptName, listStep, "Invalid XML data: ", e);
        } catch (JDOMException | IOException e) {
            addStepWithoutXml(scriptName, listStep, "Could not read UFT report: ", e);
        }

        return listStep;
    }
	
	/**
	 * Read an action element
	 * <p>
	 * // * @param parentStep
	 *
	 * @param actionElement
	 * @throws DataConversionException
	 */
	private TestStep readAction(Element actionElement) throws DataConversionException {
		TestStep actionStep = new TestStep("UFT: " + actionElement.getChildText("AName").trim(),
				"UFT: " + actionElement.getChildText("AName").trim(),
				IUftReport.class,
				Reporter.getCurrentTestResult(),
				new ArrayList<>(),
				false);
		Element summary = actionElement.getChild("Summary");
		if (summary != null && summary.getAttribute("failed").getIntValue() != 0) {
			actionStep.setFailed(true);
		}

		for (Element element : actionElement.getChildren()) {
			if (TAG_ACTION.equals(element.getName())) {
				TestStep readStep = readAction(element);
				actionStep.addStep(readStep);
			} else if (TAG_STEP.equals(element.getName())) {
				TestAction readAction = readStep(element);
				actionStep.addAction(readAction);
			}
		}
		return actionStep;
	}

	/**
	 * Read a step element
	 * <p>
	 * // * @param parentStep
	 *
	 * @param stepElement
	 */
	private TestAction readStep(Element stepElement) {
	    
		TestAction stepAction;
		List<Element> stepList = stepElement.getChildren(TAG_STEP);
		
		org.jsoup.nodes.Document htmlDoc = Jsoup.parseBodyFragment(stepElement.getChildText("Details"));
		String details = htmlDoc.text();
		
		String stepDescription = String.format("%s: %s", stepElement.getChildText("Obj"),  details).trim();

		if (stepList.isEmpty()) {
			stepAction = new TestAction(stepDescription, false, new ArrayList<>());
		} else {
			stepAction = new TestStep(stepDescription,
					stepDescription,
					IUftReport.class,
					Reporter.getCurrentTestResult(),
					new ArrayList<>(),
					false);
			for (Element subStepElement : stepElement.getChildren(TAG_STEP)) {
				TestAction readAction = readStep(subStepElement);
				((TestStep) stepAction).addAction(readAction);
			}
		}
		return stepAction;
	}

	@Override
	public boolean appliesTo() {
		return xmlReport.contains("<!ELEMENT DIter");
	}

}
