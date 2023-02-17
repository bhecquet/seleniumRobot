package com.seleniumtests.connectors.extools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.testng.Reporter;

import com.seleniumtests.connectors.extools.uftreports.UftReportFactory;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.reporter.logger.GenericFile;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.logging.ScenarioLogger;

/**
 * Connector for executing UFT tests either locally or remotely on selenium grid
 *
 * @author S047432
 */
public class Uft {

    private static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(Uft.class);
    private static final String START_LOGS = "_____OUTPUT_____";
    private static final String END_LOGS = "_____ENDOUTPUT_____";
    private static final String SCRIPT_NAME = "uft.vbs";

    private String almServer;
    private String almUser;
    private String almPassword;
    private String almDomain;
    private String almProject;
    private String scriptPath;
    private String scriptName;
    private boolean killUftOnStartup = true;
    private boolean loaded = false;
    Map<String, String> parameters = new HashMap<>();
    ;

    /**
     * @param scriptPath path to the script, either local or from ALM. If test is
     *                   from ALM, prefix it with '[QualityCenter]'. e.g:
     *                   '[QualityCenter]Subject\TOOLS\TestsFoo\foo'
     */
    public Uft(String scriptPath) {
        this.scriptPath = scriptPath;
        this.scriptName = new File(scriptPath).getName();
    }

    public Uft(String almServer, String almUser, String almPassword, String almDomain, String almProject, String scriptPath) {
        this.scriptPath = scriptPath;
        this.scriptName = new File(scriptPath).getName();
        this.almServer = almServer;
        this.almUser = almUser;
        this.almPassword = almPassword;
        this.almDomain = almDomain;
        this.almProject = almProject;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void loadScript(boolean killUftOnStartup) {
        this.killUftOnStartup = killUftOnStartup;
        List<String> args = prepareArguments(true, false);
        String out = TestTasks.executeCommand("cscript.exe", 60, null, args.toArray(new String[]{}));
        loaded = true;
    }

    /**
     * Executes an UFT script with timeout
     *
     * @param timeout timeout in seconds for UFT execution
     * @return the generated test step
     */
    public List<TestStep> executeScript(int timeout, Map<String, String> parameters) {
        if (!loaded) {
            throw new IllegalStateException("Test script has not been loaded. Call 'loadScript' before");
        }
        this.parameters = parameters;

        String output = TestTasks.executeCommand("cscript.exe", timeout, null, prepareArguments(false, true).toArray(new String[]{}));

        // when execution ends, UFT is stopped
        loaded = false;
        return analyseOutput(output);
    }

    /**
     * Prepare list of arguments
     *
     * @param load    if true, add '/load'
     * @param execute if true, add '/execute'
     * @return
     */
    public List<String> prepareArguments(boolean load, boolean execute) {
        // copy uft.vbs to disk
        String vbsPath;
        try {
            File tempFile = Files.createTempDirectory("uft").resolve(SCRIPT_NAME).toFile();
            tempFile.deleteOnExit();
            FileUtils.copyInputStreamToFile(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("uft/" + SCRIPT_NAME), tempFile);
            vbsPath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new ScenarioException("Error sending UFT script to grid node: " + e.getMessage());
        }

        if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
            SeleniumGridConnector gridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
            if (gridConnector != null) {
                vbsPath = Paths.get(gridConnector.uploadFileToNode(vbsPath, true), SCRIPT_NAME).toString();
            } else {
                throw new ScenarioException("No grid connector present, executing UFT script needs a browser to be initialized");
            }
        }

        List<String> args = new ArrayList<>();
        args.add(vbsPath);
        args.add(scriptPath);

        if (execute) {
            args.add("/execute");
            parameters.forEach((key, value) -> args.add(String.format("\"%s=%s\"", key, value)));
        }

        if (load) {
            if (almServer != null && almUser != null && almPassword != null && almDomain != null && almProject != null) {
                args.add("/server:" + almServer);
                args.add("/user:" + almUser);
                args.add("/password:" + almPassword);
                args.add("/domain:" + almDomain);
                args.add("/project:" + almProject);
            } else if (almServer != null || almUser != null || almPassword != null || almDomain != null || almProject != null) {
                throw new ConfigurationException(
                        "All valuers pour ALM connection must be provided: server, user, password, domain and project");
            }

            args.add("/load");

            if (killUftOnStartup) {
                args.add("/clean");
            }
        }
        return args;
    }

    /**
     * Analyze Result.xml content
     *
     * @param output the Result.xml content as a string // * @param duration
     *               duration of the execution
     * @return
     */
    public List<TestStep> analyseOutput(String output) {
        StringBuilder uftOutput = new StringBuilder();
        List<TestStep> stepList = new ArrayList<>();

        boolean logging = false;
        for (String line : output.split("\n")) {
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

        stepList.addAll(UftReportFactory.getInstance(uftOutput.toString(), scriptName).readXmlResult());
        try {
        	if (stepList.isEmpty()) {
        		stepList.add(new TestStep("UFT: " + scriptName, Reporter.getCurrentTestResult(), new ArrayList<>(), false));
        	}
        	File reportFile = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), String.format("uftReport-%d.txt", (int)(Math.random() * 100))).toFile();
			FileUtils.write(reportFile, output, StandardCharsets.UTF_8);
			stepList.get(0).addFile(new GenericFile(reportFile, "Uft report"));
			
		} catch (IOException e) {
			logger.error("could not write UFT output file: " + e.getMessage());
		}

        return stepList;
    }


    public void setKillUftOnStartup(boolean killUftOnStartup) {
        this.killUftOnStartup = killUftOnStartup;
    }

    public boolean isKillUftOnStartup() {
        return killUftOnStartup;
    }
}