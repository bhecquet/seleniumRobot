package com.seleniumtests.connectors.selenium;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;

public class SeleniumRobotSnapshotServerConnector extends SeleniumRobotServerConnector {
	
	private static final String VERSION_API_URL = "/api/version/";
	private static final String APPLICATION_API_URL = "/api/application/";
	private static final String ENVIRONMENT_API_URL = "/api/environment/";
	private static final String SESSION_API_URL = "/api/session/";
	private Integer applicationId;
	private Integer versionId;
	private Integer environmentId;
	private Integer sessionId;

	public SeleniumRobotSnapshotServerConnector() {
		super();
		active = isAlive();
	}
	
	@Override
	protected boolean isAlive() {
		return isAlive("/compare/");
	}

	public void createApplication() {
		if (!active) {
			return;
		}
		try {
			JSONObject applicationJson = Unirest.post(url + APPLICATION_API_URL)
					.field("name", SeleniumTestsContextManager.getApplicationName())
					.asJson()
					.getBody()
					.getObject();
			applicationId = applicationJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			logger.error("cannot create application", e);
		}
	}
	
	public void createVersion() {
		if (!active) {
			return;
		}
		if (applicationId == null) {
			createApplication();
		}
		try {
			JSONObject versionJson = Unirest.post(url + VERSION_API_URL)
					.field("name", SeleniumTestsContextManager.getApplicationVersion())
					.field("application", applicationId)
					.asJson()
					.getBody()
					.getObject();
			versionId = versionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			logger.error("cannot create application", e);
		}
	}
	
	public void createEnvironment() {
		if (!active) {
			return;
		}
		try {
			JSONObject envJson = Unirest.post(url + ENVIRONMENT_API_URL)
					.field("name", SeleniumTestsContextManager.getThreadContext().getTestEnv())
					.asJson()
					.getBody()
					.getObject();
			environmentId = envJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			logger.error("cannot create application", e);
		}
	}
	
	public void createSession() {
		if (!active) {
			return;
		}
		if (environmentId == null) {
			createEnvironment();
		}
		if (versionId == null) {
			createVersion();
		}
		try {
			BrowserType browser = SeleniumTestsContextManager.getThreadContext().getBrowser();
			browser = browser == null ? BrowserType.NONE : browser;
			
			JSONObject sessionJson = Unirest.post(url + SESSION_API_URL)
					.field("sessionId", UUID.randomUUID().toString())
					.field("date", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
					.field("browser", browser.getBrowserType())
					.field("environment", SeleniumTestsContextManager.getThreadContext().getTestEnv())
					.field("version", versionId)
					.asJson()
					.getBody()
					.getObject();
			sessionId = sessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			logger.error("cannot create application", e);
		}
	}

	public int getApplicationId() {
		return applicationId;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public Integer getEnvironmentId() {
		return environmentId;
	}

	public Integer getSessionId() {
		return sessionId;
	}
}
