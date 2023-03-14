package com.seleniumtests.connectors.extools.uftreports;

import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.reporter.logger.TestStep;

public class FallbackUftReport extends IUftReport {

	
	public FallbackUftReport(String xmlReport, String scriptName) {
		super(xmlReport, scriptName);
	}

	@Override
	public List<TestStep> readXmlResult() {
		List<TestStep> listStep = new ArrayList<>();
		logger.warn("No UftReport class applies to the following text: " + xmlReport);
		addStepWithoutXml(scriptName, listStep, "No UftReport class applies to the following text: " + xmlReport, null);
		return listStep;
	}

	@Override
	public boolean appliesTo() {
		return true;
	}

}
