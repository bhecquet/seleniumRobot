package com.seleniumtests.util.ide;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.FailurePolicy;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.javaparser.ParseProblemException;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

import net.openhft.compiler.CompilerUtils;

public class SeleniumIdeLauncher {
	
    private static Logger logger = SeleniumRobotLogger.getLogger(SeleniumIdeLauncher.class);
	private static Random random = new Random();
	
	@Parameter(names = "-scripts", variableArity = true, description= "List of selenium .java files to execute within seleniumRobot. These files are exported from Selenium IDE")
	public List<String> scripts = new ArrayList<>();
	
	@Parameter(names = "-threadcount", description = "The default number of threads to use when running tests in parallel.")
	public int threadCount = 1;

	public static void main(String ... args) throws ClassNotFoundException {
		
		// read program options
		SeleniumIdeLauncher main = new SeleniumIdeLauncher();
        JCommander.newBuilder()
            .addObject(main)
            .build()
            .parse(args);

        
        main.executeScripts();
	}

	public void executeScripts() throws ClassNotFoundException {
		
		executeScripts(scripts, threadCount);
	}
	
	/**
	 * Check if mandatory options are set
	 */
	private void checkPrerequisites() {
		if (System.getProperty(SeleniumTestsContext.BROWSER) == null) {
			throw new ConfigurationException("'-Dbrowser=<browser>' option is mandatory");
		}
	}
	
	public void executeScripts(List<String> scriptFiles, int numberOfThreads) throws ClassNotFoundException {
        try {
			checkPrerequisites();
			Map<String, String> classCodes = generateTestClasses(scriptFiles);
			executeGeneratedClasses(classCodes, numberOfThreads);
        } catch (ParseProblemException | ClassNotFoundException e) {
	        String parse = e.getMessage().split("Problem")[0];
                logger.error("--------------------------------------------------------------------------------------------------------------------------------------------------------");
                logger.error("invalid code, one element is missing : " + parse);
                logger.error("--------------------------------------------------------------------------------------------------------------------------------------------------------");
            throw new ScenarioException("invalid java code");
        }
	}
	
	/**
	 * Generates a compatible test class from the code exported from Selenium IDE
	 * We take all the methods (without \@After and \@Before) and copy them in a new file
	 */
	public Map<String, String> generateTestClasses(List<String> scriptFiles) {
		Map<String, String> classCodes = new LinkedHashMap<>();
		
		for (String scriptFile: scriptFiles) {
			try {
				classCodes.putAll(new SeleniumIdeParser(scriptFile).parseSeleniumIdeFile());
			} catch (FileNotFoundException e) {
				logger.error(String.format("File %s cannot be parsed: %s", scriptFile, e.getMessage()));
			}
		}
		return classCodes;
	}
	
	public void executeGeneratedClasses(Map<String, String> classCodes, int numberOfThreads) throws ClassNotFoundException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		// load web page classes
		List<String> classes = new ArrayList<>();
		for (Entry<String, String> entry: classCodes.entrySet()) {
			if (entry.getKey().endsWith("Page")) {
				CompilerUtils.CACHED_COMPILER.loadFromJava(loader, entry.getKey(), entry.getValue());
			}
		}
		
		// now compile tests which use page classes
		for (Entry<String, String> entry: classCodes.entrySet()) {
			if (!entry.getKey().endsWith("Page")) {
				Class<?> aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(loader, entry.getKey(), entry.getValue());
				classes.add(aClass.getCanonicalName());
			}
		}
		
		Thread.currentThread().setContextClassLoader(loader);
		executeTest(numberOfThreads, classes.toArray(new String[] {}), new String[] {});
	}
	
	private TestNG executeTest(int threadCount, String[] testClasses, String[] methods) {

		XmlSuite suite = new XmlSuite();
		suite.setName("TmpSuite");
		suite.setParallel(ParallelMode.NONE);
		suite.setFileName("/home/test/seleniumRobot/data/core/testng/testLoggging.xml");
		Map<String, String> suiteParameters = new HashMap<>();
		suiteParameters.put(SeleniumTestsContext.OVERRIDE_SELENIUM_NATIVE_ACTION, "true");
		suite.setParameters(suiteParameters);
		suite.setConfigFailurePolicy(FailurePolicy.CONTINUE);
		List<XmlSuite> suites = new ArrayList<>();
		suites.add(suite);
		
		
		if (threadCount > 1) {
			suite.setThreadCount(threadCount);
			suite.setParallel(XmlSuite.ParallelMode.TESTS);
		}
		
		for (String testClass: testClasses) {
			XmlTest test = new XmlTest(suite);
			test.setName(String.format("%s_%d", testClass.substring(testClass.lastIndexOf(".") + 1), random.nextInt()));
			List<XmlClass> classes = new ArrayList<>();
			XmlClass xmlClass = new XmlClass(testClass);
			if (methods.length > 0) {
				List<XmlInclude> includes = new ArrayList<>();
				for (String method: methods) {
					includes.add(new XmlInclude(method));
				}
				xmlClass.setIncludedMethods(includes);
			}
			classes.add(xmlClass);
			test.setXmlClasses(classes) ;
		}		
		
		TestNG tng = new TestNG(false);
		tng.setXmlSuites(suites);
		tng.run(); 
		
		return tng;
	}
}
