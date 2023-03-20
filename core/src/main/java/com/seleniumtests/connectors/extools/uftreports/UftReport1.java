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
 * Class for analyzing HTML report XML file (run_results.xml) file
 * @author S047432
 *
 */
public class UftReport1 extends IUftReport {
	
	public UftReport1(String xmlReport, String scriptName) {
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
            Element docElement = document.getRootElement().getChild("ReportNode");
            Element elementToIterate = docElement.getChild("ReportNode");
            Element iterationChild = elementToIterate.getChild("ReportNode");

            if (!iterationChild.getChildren("ReportNode").isEmpty()) {
                elementToIterate = iterationChild;
            }

            for (Element element : elementToIterate.getChildren()) {
                if ("ReportNode".equals(element.getName())) {
                    TestStep readStep = readAction(element);
                    listStep.add(readStep);
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
        Element data = actionElement.getChild("Data");
        
        TestStep actionStep = new TestStep("UFT: " + data.getChild("Name").getValue().trim(), Reporter.getCurrentTestResult(), new ArrayList<>(), false);

        if (data != null && data.getChild("Result").getValue().contains("Failed")) {
            actionStep.setFailed(true);
        }

        for (Element element : actionElement.getChildren()) {
                if ("Data".equals(element.getName())) { }
                else if (element.getAttributeValue("type").equals("Action")
                		|| element.getAttributeValue("type").equals("Context")
                		|| element.getAttributeValue("type").equals("User")) {
                    TestStep readStep = readAction(element);
                    actionStep.addStep(readStep);
                } else if (element.getAttributeValue("type").equals("Step")) {
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
        String stepDescription = "";

        TestAction stepAction;
        List<Element> stepList = stepElement.getChildren("ReportNode");

        if (stepElement.getChild("Data").getChild("Description") != null) {
            if (!stepElement.getChild("Data").getChild("Description").getContent().isEmpty()) {
                org.jsoup.nodes.Document htmlDoc = Jsoup.parseBodyFragment(stepElement.getChild("Data").getChildText("Description"));
                String details = htmlDoc.text();
                stepDescription = String.format("%s: %s", stepElement.getChild("Data").getChildText("Name"), details).trim();
            }
        } else {
            stepDescription = String.format(stepElement.getChild("Data").getChildText("Name")).trim();
        }

        if (stepList.isEmpty()) {
            stepAction = new TestAction(stepDescription, false, new ArrayList<>());
        } else {
            stepAction = new TestStep(stepDescription, Reporter.getCurrentTestResult(), new ArrayList<>(), false);
            for (Element subStepElement : stepElement.getChildren("ReportNode")) {
                TestAction readAction = readStep(subStepElement);
                ((TestStep) stepAction).addAction(readAction);
            }
        }
        return stepAction;
    }

	@Override
	public boolean appliesTo() {
		return xmlReport.contains("<ReportNode type=\"testrun\">");
	}

}
