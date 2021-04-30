package com.seleniumtests.ut.connectors.extools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.extools.Uft;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;

public class TestUft extends GenericTest {
	
	/**
	 * Check report file is correctly read
	 * @throws IOException
	 */
	@Test(groups= {"ut"})
	public void testReadReport() throws IOException {
		String report = readResourceToString("tu/uftReport.xml");
		Uft uft = new Uft("", "[QualityCenter]Subject\\Tools\\Tests\\test1", new HashMap());
		TestStep testStep = new TestStep("UFT: test1", Reporter.getCurrentTestResult(), new ArrayList<String>(), false);
		
		uft.readXmlResult(report, testStep);
		
		// check all content has been read
		Assert.assertEquals((boolean)testStep.getFailed(), false);
		Assert.assertEquals(testStep.getStepActions().size(), 1);
		
		TestStep subStep = (TestStep) testStep.getStepActions().get(0);
		Assert.assertEquals(subStep.getName(), "test1");
		
		// check the whiile content
		Assert.assertEquals(testStep.toString(), "Step UFT: test1\n"
				+ "  - Step test1\n"
				+ "    - navigteur: La transaction \"navigteur\" a démarré.\n"
				+ "    - Step S'identifier [Jenkins]: <table><tr><td><span style=\"text-align : left; font-size : 12px; \">Local Browser</span></td></tr></table>\n"
				+ "      - Step S'identifier [Jenkins]: Page\n"
				+ "        - Utilisateur.Set: \"toto\"\n"
				+ "        - Mot de passe.SetSecure: \"6075ac75a88a13a533eb7f5db06e\"\n"
				+ "        - S'identifier.Click:\n"
				+ "      - S'identifier [Jenkins].Close:\n"
				+ "    - Step IP_Config_Poste_W10 [IP_Config_Poste_W10]\n"
				+ "    - navigteur: La transaction \"navigteur\" s’est terminée avec l’état \"Réussite\" (Durée totale : 17,7658 s Temps inutilisé : 0,0888 s).");
	}
	
	/**
	 * Check that with wrong formatted report, the main test step is still present but without content
	 */
	@Test(groups= {"ut"})
	public void testReadBadReport() throws IOException {
		String report = readResourceToString("tu/wrongUftReport.xml");
		Uft uft = new Uft("", "[QualityCenter]Subject\\Tools\\Tests\\test1", new HashMap());
		
		TestStep testStep = new TestStep("UFT: test1", Reporter.getCurrentTestResult(), new ArrayList<String>(), false);
		
		uft.readXmlResult(report, testStep);
		Assert.assertEquals(testStep.getStepActions().size(), 1);
		Assert.assertEquals(((TestMessage)testStep.getStepActions().get(0)).getMessageType(), MessageType.ERROR);
	}

	// test KO
	// test avec exécution, mais sans contenu retourné
	// test avec BOM / sans BOM
	
	@Test(groups= {"ut"}, enabled = false)
	public void testExecute() throws IOException {
		Map<String, String> args = new HashMap<>();
		args.put("User", "toto");
		Uft uft = new Uft("D:\\Dev\\UFT\\uft.vbs", "[QualityCenter]Subject\\OUTILLAGE\\Tests_BHE\\test1", args);
		TestStep step = uft.executeScript();
		System.out.println(step);
	}
	
}
