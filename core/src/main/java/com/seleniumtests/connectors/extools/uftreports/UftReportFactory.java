package com.seleniumtests.connectors.extools.uftreports;

public class UftReportFactory {

	/**
	 * Returns the right uft report analyzer based on content
	 * If report is not recongnized, 
	 * @param uftReport
	 * @return
	 */
	public static IUftReport getInstance(String xmlReport, String scriptName) {
		
		IUftReport uftReportInstance;
		if ((uftReportInstance = new UftReport1(xmlReport, scriptName)).appliesTo()) {
			return uftReportInstance;
		} else if ((uftReportInstance = new UftReport2(xmlReport, scriptName)).appliesTo()) {
			return uftReportInstance;
		} else {
			return new FallbackUftReport(xmlReport, scriptName);
		}
		
	}
}
