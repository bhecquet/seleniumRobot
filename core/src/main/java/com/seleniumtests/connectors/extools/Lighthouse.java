package com.seleniumtests.connectors.extools;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.reporter.logger.GenericFile;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.SystemUtility;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Connector for executing UFT tests either locally or remotely on selenium grid
 *
 * @author S047432
 */
public class Lighthouse {

	private static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(Lighthouse.class);
	private static final String LIGHTHOUSE_FOLDER = "lighthouse";

	private int port;
	private String outputPath;
	private Path storagePath;
	private Boolean available = null;
	private Boolean isInPath = null;
	private File jsonReport;
	private File htmlReport;
	private File logs;
	Map<String, String> parameters = new HashMap<>();

	private String lighthouseCommand = OSCommand.USE_PATH + "lighthouse";
	private String nodeCommand = OSCommand.USE_PATH + "node";


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

	/**
	 * @param port       port on which lighthouse will connect
	 * @param outputPath base name of files lighthouse will produce. We will get 2 files: <outputPath>.report.html and <outputPath>.report.json
	 */
	public Lighthouse(int port, String outputPath) {
		this.port = port;
		this.outputPath = outputPath;
		this.storagePath = Paths.get(SeleniumTestsContextManager.getThreadContext().getOutputDirectory(), LIGHTHOUSE_FOLDER);
	}

	public boolean isAvailable() {
		if (available == null) {
			available = isInstalled();
		}
		return available;
	}

	private boolean isInstalled() {
		List<String> args = new ArrayList<>();
		args.add("--help");
		String out = executeLighthouse(args);
		return out.contains("--port");
	}

	public void execute(String url, List<String> options) {

		jsonReport = null;
		htmlReport = null;
		logs = null;

		String baseName = StringUtility.replaceOddCharsFromFileName(url) + "-" + UUID.randomUUID().toString().substring(0, 5);
		List<String> args = new ArrayList<>();
		args.add(url);
		args.add(String.format("--port=%d", port));
		args.add("--output=html,json");
		args.add(String.format("--output-path=%s", outputPath));
		args.addAll(options);

		if (!isAvailable()) {
			throw new ScenarioException("Lighthouse not available");
		}

		String out = executeLighthouse(args);

		if (!out.contains("json output written to")) {
			logger.error("Lighthouse did not execute correctly");
			logs = storagePath.resolve(baseName + ".log").toFile();
			try {
				FileUtils.write(logs, out, StandardCharsets.UTF_8);
				logger.logFile(logs, "Lighthouse logs " + url);
			} catch (IOException e) {
				logger.error("Lighthouse logs could not be written: " + e.getMessage());
			}
		} else {

			File jsonFile = new File(outputPath + ".report.json");
			File htmlFile = new File(outputPath + ".report.html");

			// With grid, download file first
			if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
				SeleniumGridConnector gridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
				jsonFile = gridConnector.downloadFileFromNode(outputPath + ".report.json");
				htmlFile = gridConnector.downloadFileFromNode(outputPath + ".report.html");
			}

			// get result
			jsonReport = storagePath.resolve(baseName + ".json").toFile();

			try {
				FileUtils.createParentDirectories(jsonReport);
				Files.move(jsonFile.toPath(), jsonReport.toPath());
			} catch (IOException e) {
				jsonReport = jsonFile;
			}

			logger.logFile(jsonReport, "Lighthouse JSON " + url, GenericFile.FileOperation.KEEP);


			htmlReport = storagePath.resolve(baseName + ".html").toFile();
			try {
				FileUtils.createParentDirectories(htmlReport);
				Files.move(htmlFile.toPath(), htmlReport.toPath());
			} catch (IOException e) {
				htmlReport = htmlFile;
			}
			logger.logFile(htmlReport, "Lighthouse HTML " + url, GenericFile.FileOperation.KEEP);
		}
	}

	/**
	 * Get the score associated to the selected category
	 *
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

	/**
	 * Checks if the Lighthouse home is configured.
	 *
	 * @return {true} if the Lighthouse home is configured or on the path,
	 *         {false} if it is not configured or is a environment variable
	 * @throws ConfigurationException if the "LIGHTHOUSE_HOME" environment variable
	 *         doesn't exist and Lighthouse is not on the path.
	 */
	public boolean isLighthouseInPath() {
		String name = "LIGHTHOUSE_HOME";
		try {
			String lighthouseHomeEnv = SystemUtility.getenv(name);
			if (lighthouseHomeEnv == null || lighthouseHomeEnv.isEmpty()) {
				logger.info("Lighthouse is on path");
				return true;
			} else {
				lighthouseCommand = lighthouseHomeEnv;
				return false;
			}
		} catch (ConfigurationException e) {
			throw new ConfigurationException(name + " environment variable doesn't exist and lighthouse isn't on path");
		}
	}

	/**
	 * Executes the Lighthouse index command with the given arguments.
	 *
	 * @param args the arguments to pass to the Lighthouse index command.
	 * @return the output of the command execution.
	 */
	public String executeLighthouse(List<String> args) {
		String out = "";
		isInPath = isLighthouseInPath();

		if (args != null) {
			if (isInPath) {
				out = TestTasks.executeCommand(lighthouseCommand, 90, null, args.toArray(new String[args.size()]));
			} else {
				args.add(0, (lighthouseCommand + "index.js"));
				out = TestTasks.executeCommand(nodeCommand, 90, null, args.toArray(new String[args.size()]));
			}
		}
		return out;
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

	public int getPort() {
		return port;
	}

	public String getOutputPath() {
		return outputPath;
	}
}