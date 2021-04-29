package com.seleniumtests.connectors.extools;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.testng.Reporter;
import org.xml.sax.InputSource;

import com.seleniumtests.core.TestTasks;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.ScenarioLogger;

/**
 * Connector for executing UFT tests either locally or remotely on selenium grid
 * 
 * To allow this feature, you would use such VBS script (uft.vbs) that should be present somewhere on your computer (or on grid node)
 * 
 * 
Dim fso,fso1

resultFolder = "D:\\uft\\output"

Set fso=createobject("Scripting.FileSystemObject")
If fso.FolderExists(resultFolder) Then
	fso.DeleteFolder resultFolder,True
End If


'---------------------------------------------------------------------------------'

Test_path = Wscript.Arguments(0)
Set qtApp = CreateObject("QuickTest.Application") ' Create the Application object
qtApp.Launch ' Start QuickTest
qtApp.Visible = True ' Make the QuickTest application visible

'' Set QuickTest run options
qtApp.Options.Run.RunMode = "Normal"
qtApp.Options.Run.ViewResults = False

'connection à QC
If qtApp.TDConnection.IsConnected Then
	qtApp.TDConnection.disconnect
End If
qtApp.TDConnection.Connect "<server>:8080/qcbin","<domain>","<project>","<user>","<password>",False

qtApp.Open Test_path, False ' Open the test in read-only mode

Set pDefColl = qtApp.Test.ParameterDefinitions
Set rtParams = pDefColl.GetParameters()
Dim keyValue

For Each strArg in Wscript.Arguments
	WScript.Echo strArg
	keyValue = Split(strArg, "=")
	
	cnt = pDefColl.Count
	Indx = 1
	While Indx <= cnt
		Set pDef = pDefColl.Item(Indx)
		Indx = Indx + 1
		If StrComp(pDef.Name, keyValue(0)) = 0 Then
			Set rtParam2 = rtParams.Item(keyValue(0))
			WScript.Echo keyValue(1)
			rtParam2.Value = keyValue(1)
		End If
		
	Wend
	
Next

Dim qtResultsOpt 'As QuickTest.RunResultsOptions ' Declare a Run Results Options object variable

Set qtResultsOpt = CreateObject("QuickTest.RunResultsOptions") ' Create the Run Results Options object
qtResultsOpt.ResultsLocation = resultFolder ' Set the results location

' set run settings for the test
Set qtTest = qtApp.Test
qtTest.Run qtResultsOpt, true, rtParams 	' Run the test

WScript.Echo resultFolder + "\Report\Results.xml"
Set file = fso.OpenTextFile(resultFolder + "\Report\Results.xml", 1)
content = file.ReadAll
WScript.Echo "_____OUTPUT_____"
WScript.Echo content
WScript.Echo "_____ENDOUTPUT_____"


qtTest.Close			' Close the test
qtApp.quit
Set qtTest = Nothing		' Release the Test object
Set qtApp = Nothing 		' Release the Application object 
Set qtResultsOpt = Nothing

wscript.quit 0


 * 
 * @author S047432
 *
 */
public class Uft {
	
	private static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(Uft.class); 
	private static final String START_LOGS = "_____OUTPUT_____";
	private static final String END_LOGS = "_____ENDOUTPUT_____";

	private String scriptPath;
	private String scriptName;
	private String vbsPath;
	Map<String, String> parameters;
	
	/**
	 * @param vbsPath		path to the vbs file (locally, or on the remote machine)
	 * @param scriptPath	path to the script, either local or from ALM. If test is from ALM, prefix it with '[QualityCenter]'. e.g: '[QualityCenter]Subject\TOOLS\TestsFoo\foo'
	 * @param parameters	parameters to pass to the script
	 */
	public Uft(String vbsPath, String scriptPath, Map<String, String> parameters) {
		this.vbsPath = vbsPath;
		this.scriptPath = scriptPath;
		this.scriptName = new File(scriptPath).getName();
		this.parameters = parameters;
	}
	
	/**
	 * Executes an UFT script
	 * @return	the generated test step
	 */
	public TestStep executeScript() {
		List<String> args = new ArrayList<>();
		args.add(vbsPath);
		args.add(scriptPath);
		parameters.forEach((key, value) -> args.add(String.format("\"%s=%s\"", key, value)));
		
		TestStep testStep = new TestStep(String.format("UFT: %s", scriptName), Reporter.getCurrentTestResult(), new ArrayList<String>(), false);
		
		Date startDate = new Date();
		String output = TestTasks.executeCommand("cscript.exe", 60, null, args.toArray(new String[] {}));
		
		testStep.setDuration(new Date().getTime() - startDate.getTime());
		return analyseOutput(output, testStep);
	}
	
	/**
	 * Analyze Result.xml content
	 * @param output		the Result.xml content as a string
	 * @param duration		duration of the execution
	 * @return
	 */
	public TestStep analyseOutput(String output, TestStep testStep) {
		StringBuilder uftOutput = new StringBuilder();
		
		boolean logging = false;
		for (String line: output.split("\n")) {
			line = line.trim();
			
			if (line.contains(START_LOGS)) {
				logging = true;
				continue;
			} else if (line.contains(END_LOGS)) {
				logging = false;
			}
			
			if (logging) {
				uftOutput.append(line);
			}
		}
		
		readXmlResult(uftOutput.toString(), testStep);
		
		return testStep;
	}
	
	/**
	 * Read an action element
	 * @param parentStep
	 * @param actionElement
	 * @throws DataConversionException
	 */
	private void readAction(TestStep parentStep, Element actionElement) throws DataConversionException {
		TestStep actionStep = new TestStep(actionElement.getChildText("AName"), Reporter.getCurrentTestResult(), new ArrayList<>(), false);
		parentStep.addStep(actionStep);
		Element summary = actionElement.getChild("Summary");
		if (summary!= null && summary.getAttribute("failed").getIntValue() != 0) {
			actionStep.setFailed(true);
		}
		
		for (Element element: actionElement.getChildren()) {
			if ("Action".equals(element.getName())) {
				readAction(actionStep, element);
			} else if ("Step".equals(element.getName())) {
				readStep(actionStep, element);
			}
		}
		
	}
	
	/**
	 * Read a step element
	 * @param parentStep
	 * @param stepElement
	 */
	private void readStep(TestStep parentStep, Element stepElement) {
		List<Element> stepList = stepElement.getChildren("Step");
		
		String stepDescription = String.format("%s: %s", stepElement.getChildText("Obj"),  stepElement.getChildText("Details"));
		
		if (stepList.isEmpty()) {
			TestAction action = new TestAction(stepDescription, false, new ArrayList<>());
			parentStep.addAction(action);
		} else {
			TestStep stepStep = new TestStep(stepDescription, Reporter.getCurrentTestResult(), new ArrayList<>(), false);
			parentStep.addStep(stepStep);
			for (Element subStepElement: stepElement.getChildren("Step")) {
				readStep(stepStep, subStepElement);
			}
		}
		
	}
	
	public void readXmlResult(String xmlString, TestStep testStep) {
		
		Document document;
		SAXBuilder builder = new SAXBuilder();

		try {
			document = builder.build(new InputSource(new StringReader(xmlString.substring(xmlString.indexOf("<"))))); // we skip BOM by searching the first "<" character
			Element docElement = document.getRootElement().getChild("Doc");
			Element summary = docElement.getChild("Summary");
			if (summary!= null && summary.getAttribute("failed").getIntValue() != 0) {
				testStep.setFailed(true);
			}
			
			Element iteration = docElement.getChild("DIter");
			
			for (Element element: iteration.getChildren()) {
				if ("Action".equals(element.getName())) {
					readAction(testStep, element);
				} else if ("Step".equals(element.getName())) {
					readStep(testStep, element);
				}
			}
		
		} catch (JDOMException | IOException e) {
			logger.error("Could not read UFT report: " + e.getMessage());
			testStep.addMessage(new TestMessage("Could not read UFT report: " + e.getMessage(), MessageType.ERROR));
		}
		
	}
}
