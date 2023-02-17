package com.seleniumtests.ut.connectors.extools.uftreports;

import java.io.IOException;
import java.util.List;

import org.jdom2.DataConversionException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.uftreports.UftReport2;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.reporter.logger.TestStep.StepStatus;

/**
 * Test reports which have the following format
 * <?xml version="1.0"?>
<!DOCTYPE Report[<!ELEMENT Report (General ,(Doc|BPT)) >
		<!ATTLIST Report ver CDATA #REQUIRED tmZone CDATA #REQUIRED>
			<!ELEMENT General ( DocLocation ) >
				<!ATTLIST General productName CDATA #REQUIRED productVer CDATA #REQUIRED os CDATA #REQUIRED host CDATA #REQUIRED qcserver CDATA #IMPLIED qcproject CDATA #IMPLIED SolManSolutionId CDATA #IMPLIED SolManProjectId CDATA #IMPLIED SolManTestPlanId CDATA #IMPLIED SolManTestPackageId CDATA #IMPLIED SolManUserInfoData CDATA #IMPLIED  >
					<!ELEMENT BPT (DName,Res,DVer?,TSet?,TInst?,NodeArgs,AdditionalInfo*,Doc*) >
						<!ATTLIST BPT rID ID #REQUIRED >
							<!ELEMENT Doc (DName,ConfName?,Res,DVer?,TSet?,TInst?,RunType?,DT?,AdditionalInfo*,Step*,DIter*,Step*,Action*,Doc*,Summary?,TestMaintenanceSummary*,NodeArgs?) >
								<!ATTLIST Doc rID ID #REQUIRED type (Test|BC|BPTWrapperTest|Flow|Group|Action) "Test" productName CDATA #REQUIRED BCIter CDATA #IMPLIED >
									<!ELEMENT RunType ( #PCDATA )>
										<!ATTLIST RunType fmStep (False|True) "False" batch (False|True) "False" upDesc (False|True) "False" upChk (False|True) "False" upAS (False|True) "False">
											<!ELEMENT DName ( #PCDATA ) >
                        ...
 * 
 * @author S047432
 *
 */
public class TestUftReport2 extends GenericTest {


	@Test(groups = { "ut" })
	public void testReadReportListActions() throws IOException, DataConversionException {
		String report = GenericTest.readResourceToString("tu/Results.xml");
		List<TestStep> stepList = new UftReport2(report, "test1").readXmlResult();

		// check all content has been read
		Assert.assertEquals(stepList.size(), 2);
		
		Assert.assertEquals(stepList.get(0).getName(), "UFT: DebutTest [DebutTest]");
		Assert.assertEquals(stepList.get(1).getName(), "UFT: Choixproduit [Choixproduit]");


		// check the while content
		Assert.assertEquals(stepList.get(0).getStepStatus(), StepStatus.FAILED);
		Assert.assertEquals(stepList.get(1).getStepStatus(), StepStatus.SUCCESS);
		Assert.assertEquals(stepList.get(1).toString(), "Step UFT: Choixproduit [Choixproduit]\n"
			+ "  - Choix du produit\n"
			+ "						: Permet de sélectionner le produit à traiter dans l'arbre produit\n"
			+ "  - Step P9\n"
			+ "						: Local Browser\n"
			+ "    - Step P9 - Agence\n"
			+ "							: Page\n"
			+ "      - Step Onglets\n"
			+ "								: Frame\n"
			+ "        - Particulier.Exist\n"
			+ "									: \"Object does not exist\"\n"
			+ "      - Step Menu\n"
			+ "								: Frame\n"
			+ "        - Assurance.Click\n"
			+ "									:"); 
	}

	/**
	 * Check report file is correctly read
	 * 
	 * @throws IOException
	 */
	@Test(groups = { "ut" })
	public void testReadReport() throws IOException {
		String report = GenericTest.readResourceToString("tu/uftReport.xml");
		List<TestStep> stepList = new UftReport2(report, "test1").readXmlResult();
		
		// check all content has been read
		Assert.assertEquals(stepList.get(0).getName(), "UFT: IP_Config_Poste_W10 [IP_Config_Poste_W10]");
		// check all content has been read
		Assert.assertEquals(stepList.size(), 1);
 
		// check the while content 
		Assert.assertEquals(stepList.get(0).toString(), "Step UFT: IP_Config_Poste_W10 [IP_Config_Poste_W10]");
	}

	/**
	 * Check that with wrong formatted report, the main test step is still present
	 * but without content
	 */
	@Test(groups = { "ut" })
	public void testReadBadReport() throws IOException {
		String report = GenericTest.readResourceToString("tu/wrongUftReport.xml");
		List<TestStep> stepList = new UftReport2(report, "test1").readXmlResult();
		
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
		List<TestStep> stepList = new UftReport2(report, "test1").readXmlResult();
		
		Assert.assertEquals(stepList.get(0).getStepActions().size(), 1);
		Assert.assertEquals(((TestMessage) stepList.get(0).getStepActions().get(0)).getMessageType(), MessageType.ERROR);

	}
}
