package com.seleniumtests.ut.connectors.extools.uftreports;

import java.io.IOException;
import java.util.List;

import org.jdom2.DataConversionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
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
public class TestUftReport1 extends GenericTest {


	@Test(groups = { "ut" })
	public void testReadReportListActions() throws IOException, DataConversionException {
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");

		List<TestStep> stepList = new UftReport1(report, "test1").readXmlResult();

		// check all content has been read
		Assert.assertEquals(stepList.size(), 3);
		
		Assert.assertEquals(stepList.get(0).getName(), "UFT: DebutTest [DebutTest]");
		Assert.assertEquals(stepList.get(1).getName(), "UFT: FinTest [FinTest]");


		// check the while content
		Assert.assertEquals(stepList.get(0).getStepStatus(), StepStatus.SUCCESS);
		Assert.assertEquals(stepList.get(1).getStepStatus(), StepStatus.FAILED);
		Assert.assertEquals(stepList.get(1).toString(), "Step UFT: FinTest [FinTest]\n" +
				"  - Step UFT: Opérations de fin de test\n" +
				"  - Step UFT: 63\n" +
				"  - Item of type <CYCL_FOLD>, with Id <-2> does not exist.: Item of type <CYCL_FOLD>, with Id <-2> does not exist. Line (39): \"TargetCycleName = CurrentTestSet.TestSetFolder.TargetCycle.Name\".\n" +
				"  - Stop Run: Run stopped by user.");
	}

	/**
	 * Check report file is correctly read
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testReadReport() throws IOException {
		String report = GenericTest.readResourceToString("tu/uftReport2023.xml");

		List<TestStep> stepList = new UftReport1(report, "test1").readXmlResult();
		
		// check all content has been read
		Assert.assertEquals(stepList.size(), 3);
		Assert.assertEquals(((TestStep) stepList.get(0).getStepActions().get(1)).getStepActions().get(0).toString(),  "Step UFT: Lancer P9");
	}

	/**
	 * Check that with wrong formatted report, the main test step is still present
	 * but without content
	 */
	@Test(groups = { "ut" })
	public void testReadBadReport() throws IOException {
		String report = GenericTest.readResourceToString("tu/wrongUftReport2023.xml");

		List<TestStep> stepList = new UftReport1(report, "test1").readXmlResult();
		
		Assert.assertEquals(stepList.get(0).getStepActions().size(), 1);
		Assert.assertEquals(((TestMessage) stepList.get(0).getStepActions().get(0)).getMessageType(), MessageType.ERROR);
	}
	
	/**
	 * Test when no report is available
	 * the main test step is still present
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testReadNoReport() throws IOException {
		String report = "some bad report";

		List<TestStep> stepList = new UftReport1(report, "test1").readXmlResult();
		
		Assert.assertEquals(stepList.get(0).getStepActions().size(), 1);
		Assert.assertEquals(((TestMessage) stepList.get(0).getStepActions().get(0)).getMessageType(), MessageType.ERROR);

	}
}
