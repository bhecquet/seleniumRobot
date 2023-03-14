package com.seleniumtests.connectors.extools.uftreports;

import java.util.ArrayList;
import java.util.List;

import org.testng.Reporter;

import com.seleniumtests.connectors.extools.Uft;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.util.logging.ScenarioLogger;

public abstract class IUftReport {
	
	protected static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(Uft.class);


	protected String scriptName;
	protected String xmlReport;
	

	public IUftReport(String xmlReport, String scriptName) {
		this.scriptName = scriptName;
		this.xmlReport = xmlReport;
	}
	
	
	public abstract List<TestStep> readXmlResult();
	
	
	/**
	 * Returns true if the XML content applies to this report
	 * @return
	 */
	public abstract boolean appliesTo();

    protected void addStepWithoutXml(String scriptName, List<TestStep> listStep, String messageException, Exception e) {
    	String completeMessage = e != null ? e.getMessage(): "";
        logger.error(messageException + ": " + completeMessage);
        TestStep readStep = new TestStep("UFT: " + scriptName, Reporter.getCurrentTestResult(), new ArrayList<>(), false);
        readStep.addMessage(new TestMessage(messageException + ": " + completeMessage, MessageType.ERROR));
        listStep.add(readStep);
    }
}
