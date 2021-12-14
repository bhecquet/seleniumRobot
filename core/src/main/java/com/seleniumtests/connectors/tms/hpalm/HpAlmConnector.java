/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.tms.hpalm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.ITestResult;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.primitives.Bytes;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

/**
 * Class to connect to HP ALM using  API
 * @author 
 *
 */
public class HpAlmConnector extends TestManager {
	
	private static final String DOMAIN_NAME = "domain";
	private static final String PROJECT_NAME = "project";
	private static final String ENTITY_ID = "entityId";
	private static final String ENTITY_TYPE = "entityType";
	
	
	private String serverUrl;
	private String project;
	private String domain;
	private String user;
	private String password;
	private String currentRunId;
	private Boolean loggedIn;

	/**
	 * Constructeur
	 * Configure connection to server (>=11) from following variables searched in configuration (env.ini)
	 * hpAlmServerUrl, hpAlmProject, hpAlmDomain, hpAlmUser, hpAlmPassword
	 * @param configString	JSON conf for HP ALM. must contain the key 'run' (currently runnning test)
	 */
	public HpAlmConnector(JSONObject configString) {
		super();
		
		// check configuration
		try {
			currentRunId = Integer.toString(configString.getInt("tmsRun"));
		} catch (JSONException e) {
			throw new ConfigurationException("Test manager configuration does not contain 'run' parameter");
		}

	}

	@Override
	public void init(JSONObject connectParams) {
		
		String serverUrlVar = connectParams.optString(TMS_SERVER_URL, null);
		String projectVar = connectParams.optString(TMS_PROJECT, null);
		String domainVar = connectParams.optString(TMS_DOMAIN, null);
		String userVar = connectParams.optString(TMS_USER, null);
		String passwordVar = connectParams.optString(TMS_PASSWORD, null);

		if (serverUrlVar == null || projectVar == null || domainVar == null || userVar == null || passwordVar == null) {
			throw new ConfigurationException(String.format("HP ALM access not correctly configured. Environment configuration must contain variables"
					+ "%s, %s, %s, %s, %s", TMS_SERVER_URL, TMS_PASSWORD, TMS_USER, TMS_DOMAIN, TMS_PROJECT));
		}
		
		serverUrl = serverUrlVar;
		project = projectVar;
		domain = domainVar;
		user = userVar;
		password = passwordVar;
		
		serverUrl += "/qcbin";
		initialized = true;
	}
	
	/**
	 * connect to server
	 */
	@Override
	public void login() {
		if (!initialized) {
			throw new ConfigurationException("test manager has not been previously initialized");
		}
		
		try {
			HttpResponse<String> response = Unirest.get(serverUrl + "/authentication-point/authenticate")
				.basicAuth(user, password).asString();
			if (response.getStatus() != 200) {
				throw new ConfigurationException("Cannot connect to HP server: " + response.getStatusText());
			}
		} catch (UnirestException e) {
			throw new ConfigurationException("Cannot connect to HP server", e);
		}
		loggedIn = true;
	}
	
	/**
	 * disconnect from server
	 */
	@Override
	public void logout() {
		if (loggedIn) {
			try {
				Unirest.get(serverUrl + "/authentication-point/logout").asString();
			} catch (UnirestException e) {
				logger.warn("Error while logout", e);
			}
		}
	}
	
	public void updateRunStatus() throws JAXBException {

		// création du XML de modification
		HashMap<String, String> fields = new HashMap<>();
		fields.put("status", "Failed");
		Entity entity = buildTestRunResultEntity(fields, "run");
		String xml = EntityMarshallingUtils.unmarshal(Entity.class, entity);
		
		try {
			lockEntity("run", currentRunId);
			try {
				checkoutEntity("run", currentRunId);

				HttpResponse<String> response = Unirest.put(serverUrl + "/rest/domains/{domain}/projects/{project}/runs/{runId}")
						.routeParam("runId", currentRunId)
						.routeParam(PROJECT_NAME, project)
						.routeParam(DOMAIN_NAME, domain)
						.header("Content-Type", "application/xml")
						.body(xml)
						.asString();
				
				if (response.getStatus() != 200) {
					logger.error("Mise à jour du statut impossible: " + response.getStatusText());
				}
		
			} finally {
				checkinEntity("run", currentRunId);
			}
		} finally {
			unlockEntity("run", currentRunId);
		}
	}
	
	/**
	 * Do nothing as result is directly get from .bat execution
	 */
	@Override
	public void recordResult() {	
		// nothing to do
	}


	/**
	 * compress and record results in ALM
	 */
	@Override
	public void recordResultFiles() {
		

		try {
			File resultFile = File.createTempFile("result-", ".zip");
			resultFile.deleteOnExit();
			ZipUtil.pack(new File(SeleniumTestsContextManager.getThreadContext().getOutputDirectory()), resultFile);
			
			// Unirest does not allow sending result to ALM
			String boundary = "azertyuiop";
			String body = "--" + boundary + "\r\n";
			body += "Content-Disposition: form-data; name=\"filename\"\r\n";
			body += "Content-type: application/octet-stream\r\n\r\n";
			body += String.format("%s\r\n", resultFile.getName());
			body += "--" + boundary + "\r\n";
			body += String.format("Content-Disposition: form-data; name=\"file\"; filename=\"%s\"\r\n\r\n", resultFile.getName());
			
			byte[] bodyBytes = Bytes.concat(body.getBytes(), FileUtils.readFileToByteArray(resultFile), String.format("\r\n--%s--\r\n", boundary).getBytes());
			
			Unirest.post(serverUrl + "/rest/domains/{domain}/projects/{project}/{entityType}s/{entityId}/attachments")
					.routeParam(ENTITY_TYPE, "run")
					.routeParam(ENTITY_ID, currentRunId)
					.routeParam(PROJECT_NAME, project)
					.routeParam(DOMAIN_NAME, domain)
					.header("Content-Type", "multipart/form-data; boundary=" + boundary)
					.body(bodyBytes)
					.asString();
			
		} catch (UnirestException | IOException e) {
			logger.error("Result record failed", e);
		}
	}
	
	/**
	 * lock entity object so that it can be found during test
	 * @param entityType
	 * @param entityId
	 * @throws UnirestException
	 */
	private void lockEntity(String entityType, String entityId) throws UnirestException {
		Unirest.get(serverUrl + "/rest/domains/{domain}/projects/{project}/{entityType}s/{entityId}/lock")
			.routeParam(ENTITY_TYPE, entityType)
			.routeParam(ENTITY_ID, entityId)
			.routeParam(PROJECT_NAME, project)
			.routeParam(DOMAIN_NAME, domain)
			.asString();
	}
	
	/**
	 * lock entity object so that it can be found during test
	 * @param entityType
	 * @param entityId
	 * @throws UnirestException
	 */
	private void unlockEntity(String entityType, String entityId) throws UnirestException {
		Unirest.delete(serverUrl + "/rest/domains/{domain}/projects/{project}/{entityType}s/{entityId}/lock")
			.routeParam(ENTITY_TYPE, entityType)
			.routeParam(ENTITY_ID, entityId)
			.routeParam(PROJECT_NAME, project)
			.routeParam(DOMAIN_NAME, domain)
			.asString();
	}
	
	/**
	 * checkout entity object so that it can be found during test
	 * used when versionning is enabled
	 * @param entityType
	 * @param entityId
	 * @throws UnirestException
	 */
	private void checkoutEntity(String entityType, String entityId) throws UnirestException {
		Unirest.post(serverUrl + "/rest/domains/{domain}/projects/{project}/{entityType}s/{entityId}/versions/check-out")
			.routeParam(ENTITY_TYPE, entityType)
			.routeParam(ENTITY_ID, entityId)
			.routeParam(PROJECT_NAME, project)
			.routeParam(DOMAIN_NAME, domain)
			.asString();
	}
	
	/**
	 * checkin entity object so that it can be found during test
	 * used when versionning is enabled
	 * @param entityType
	 * @param entityId
	 * @throws UnirestException
	 */
	private void checkinEntity(String entityType, String entityId) throws UnirestException {
		Unirest.post(serverUrl + "/rest/domains/{domain}/projects/{project}/{entityType}s/{entityId}/versions/check-in")
			.routeParam(ENTITY_TYPE, entityType)
			.routeParam(ENTITY_ID, entityId)
			.routeParam(PROJECT_NAME, project)
			.routeParam(DOMAIN_NAME, domain)
			.asString();
	}

	/**
	 * @param fieldMap
	 *            Map consisting of values to be sent to the web service (key,
	 *            value)
	 * @return Entity object based on the given input values
	 */
	private Entity buildTestRunResultEntity(Map<String, String> fieldMap, String entityType) {
		Entity entity = new Entity();
		Entity.Fields fields = new Entity.Fields();

		Iterator<Entry<String, String>> headersIterator = fieldMap.entrySet().iterator();
		while (headersIterator.hasNext()) {
			Entry<String, String> header = headersIterator.next();
			Entity.Fields.Field field = new Entity.Fields.Field();
			field.setName(header.getKey());
			field.addValue(header.getValue());
			fields.addField(field);
		}

		entity.setFields(fields);
		entity.setType(entityType);
		return entity;
	}

	public String getCurrentRunId() {
		return currentRunId;
	}

	@Override
	public void recordResult(ITestResult testResult) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordResultFiles(ITestResult testResult) {
		// TODO Auto-generated method stub
		
	}

	
}
