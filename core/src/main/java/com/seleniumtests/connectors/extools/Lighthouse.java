package com.seleniumtests.connectors.extools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.io.Files;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.osutility.OSCommand;

/**
 * Connector for executing UFT tests either locally or remotely on selenium grid
 * 
 * 
 * @author S047432
 *
 */
public class Lighthouse {

	private static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(Lighthouse.class);
	private static final String LIGHTHOUSE_FOLDER = "lighthouse";

	private int port;
	private String outputPath;
	private boolean available = false;
	private File jsonReport;
	private File htmlReport;
	private File logs;
	Map<String, String> parameters = new HashMap<>();;

	public enum Category {
		
		ACCESSIBILITY("accessibility"),
		PERFORMANCE("performance"),
		BEST_PRACTICES("best-practices"),
		SEO("seo"),
		PWA("pwa");
		
		public String key;
		
		private Category(String key) {
			this.key = key;
		}
	}

	public Lighthouse(int port, String outputPath) {
		this.available = isInstalled();
		this.port = port;
		this.outputPath = outputPath;
	}
	
	private boolean isInstalled() {
		String out = TestTasks.executeCommand(OSCommand.USE_PATH + "lighthouse", 5, null, "--help");
		return out.contains("--port");
	}
	
	public void execute(String url, List<String> options) {
		
		jsonReport = null;
		htmlReport = null;
		logs = null;
		
		if (!available) {
			throw new ScenarioException("Lighthouse not available");
		}
		
		String baseName = StringUtility.replaceOddCharsFromFileName(url) + "-" + UUID.randomUUID().toString().substring(0, 5);
		List<String> args = new ArrayList<>();
		args.add(url);
		args.add(String.format("--port=%d", port));
		args.add("--output=html,json");
		args.add(String.format("--output-path=%s", outputPath));
		args.addAll(options);
		
		String out = TestTasks.executeCommand(OSCommand.USE_PATH + "lighthouse", 90, null, args.toArray(new String[] {}));
		
		logs = Paths.get(new File(outputPath).getParentFile().getAbsolutePath(), LIGHTHOUSE_FOLDER, baseName + ".log").toFile();
		if (!out.contains("json output written to")) {
			logger.error("Lighthouse did not execute correctly");
			try {
				FileUtils.write(logs, out, StandardCharsets.UTF_8);
				logger.logFile(logs, "Lighthouse logs " + url);
			} catch (IOException e) {
				logger.error("Lighthouse logs could not be written: " + e.getMessage());
			}
		} else {
		
			// get result
			if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
				
				File jsonFile = new File(outputPath + ".report.json");
				jsonReport = Paths.get(jsonFile.getParentFile().getAbsolutePath(), LIGHTHOUSE_FOLDER, baseName + ".json").toFile();
				try {
					Files.move(jsonFile, jsonReport);
				} catch (IOException e) {
					jsonReport = jsonFile;
				}
				
				logger.logFile(jsonReport, "Lighthouse JSON " + url, false);
				
				File htmlFile = new File(outputPath + ".report.html");
				htmlReport = Paths.get(jsonFile.getParentFile().getAbsolutePath(), LIGHTHOUSE_FOLDER, baseName + ".html").toFile();
				try {
					Files.move(htmlFile, htmlReport);
				} catch (IOException e) {
					htmlReport = htmlFile;
				}
				logger.logFile(htmlReport, "Lighthouse HTML " + url, false);
				
			} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
				
			}
		}
	}
	
	/**
	 * Get the score associated to the selected category
	 * @param category
	 * @return
	 */
	public Double getScore(Category category) {
		if (jsonReport == null) {
			logger.error(String.format("Cannot get %s score, lighthouse has not been run or is in error", category));
			return 0.0;
		} else {
			try {
				JSONObject result = new JSONObject(FileUtils.readFileToString(jsonReport, StandardCharsets.UTF_8));
				return result.getJSONObject("categories").getJSONObject(category.key).getDouble("score") * 100;
			} catch (JSONException | IOException e) {
				logger.error(String.format("Cannot read %s score, lighthouse has not been run or is in error", category));
				return 0.0;
			}
		}
	}

	public File getJsonReport() {
		return jsonReport;
	}

	public File getHtmlReport() {
		return htmlReport;
	}

	public File getLogs() {
		return logs;
	}
}