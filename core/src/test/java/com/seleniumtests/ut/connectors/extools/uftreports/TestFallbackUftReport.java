package com.seleniumtests.ut.connectors.extools.uftreports;

import java.io.IOException;
import java.util.List;

import org.jdom2.DataConversionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.uftreports.FallbackUftReport;
import com.seleniumtests.connectors.extools.uftreports.UftReport1;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestStep.StepStatus;

/**
 * Test reports which have the following format
 * Results version="1.0">
    <ReportNode type="testrun">
        <ReportNode type="Iteration">
            <ReportNode type="Action">
                <ReportNode type="Action">
                    <ReportNode type="Action">
                        <ReportNode type="User">
                        ...
 * 
 * @author S047432
 *
 */
public class TestFallbackUftReport extends GenericTest {



	/**
	 * Check report file is correctly read
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testReadReport() throws IOException {

		List<TestStep> stepList = new FallbackUftReport("incompatible report", "test1").readXmlResult();
		
		// check all content has been read
		Assert.assertEquals(stepList.size(), 1);
		Assert.assertEquals((((TestStep) stepList.get(0)).getStepActions().get(0)).getName(),  "No UftReport class applies to the following text: incompatible report: ");
	}

}
