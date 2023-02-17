package com.seleniumtests.ut.connectors.extools.uftreports;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.uftreports.FallbackUftReport;
import com.seleniumtests.connectors.extools.uftreports.UftReport1;
import com.seleniumtests.connectors.extools.uftreports.UftReport2;
import com.seleniumtests.connectors.extools.uftreports.UftReportFactory;

public class TestUftReportFactory extends GenericTest {

	@Test(groups= {"ut"})
	public void testUftReport1() throws IOException {
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");
		Assert.assertTrue(UftReportFactory.getInstance(report, "test1") instanceof UftReport1);
	}
	
	@Test(groups= {"ut"})
	public void testUftReport2() throws IOException {
		String report = GenericTest.readResourceToString("tu/uftReport.xml");
		Assert.assertTrue(UftReportFactory.getInstance(report, "test1") instanceof UftReport2);
	}
	
	@Test(groups= {"ut"})
	public void testUnknownFormat() throws IOException {
		Assert.assertTrue(UftReportFactory.getInstance("foobar", "test1") instanceof FallbackUftReport);
	}
}
