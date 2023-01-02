package com.seleniumtests.connectors.extools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.deque.html.axecore.results.ResultType;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.selenium.AxeBuilderOptions;
import com.deque.html.axecore.selenium.AxeReporter;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.ScenarioLogger;

/**
 * Class that checks WCAG violations
 * @author S047432
 *
 */
public class WcagChecker {
	
	private static final String WCAG_CHECKER_PATH = "wcag";
	private static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(WcagChecker.class);

	/**
	 * Analyze the page or elements using the options a
	 * @param builderOptions		Options to pass to AxeCore. See https://github.com/dequelabs/axe-core-maven-html/blob/develop/selenium/src/test/java/com/deque/html/axecore/selenium/AxeExampleUnitTest.java
	 * @param driver				The current driver
	 * @param elements				If any, the elements to analyze. If list is empty, then, the whole page will be analyzed
	 * @return
	 */
	public static Results analyze(WebDriver driver, WebElement ... elements) {
		
		Results result;
		AxeBuilder axeBuilder = new AxeBuilder();
		
		if (elements.length > 0) {
			result = axeBuilder.analyze(driver, elements);
		} else {
			result = axeBuilder.analyze(driver);
		}
		
		
		AxeReporter.getReadableAxeResults(ResultType.Violations.getKey(), driver, result.getViolations());
		
		// write axe report to output folder
		try {
			Files.createDirectories(Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), WCAG_CHECKER_PATH));
		} catch (IOException e) {
			logger.error("cannot create 'wcag' directory: " + e.getMessage());
		}
		String fileName = StringUtility.replaceOddCharsFromFileName(result.getUrl()) + "-" + UUID.randomUUID().toString().substring(0, 5);
		String outputPath = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), WCAG_CHECKER_PATH, fileName).toAbsolutePath().toString();
		AxeReporter.writeResultsToTextFile(outputPath, AxeReporter.getAxeResultString());
		
		if (!result.violationFree()) {
			logger.warn(String.format("%d violations found, see attached file", result.getViolations().size()));
		}
		logger.logFile(new File(outputPath + ".txt"), "WCAG report", false);
		
		return result;
	}
}
