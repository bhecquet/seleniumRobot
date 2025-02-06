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
package com.seleniumtests.connectors.mails;

import org.json.JSONException;
import org.json.JSONObject;

import com.seleniumtests.customexception.ConfigurationException;

public class EmailServer {

	private String url;
	private EmailServerTypes type;
	private String domain;
	
	public enum EmailServerTypes {
		EXCHANGE, EXCHANGE_EWS, IMAP, GMAIL, EXCHANGE_ONLINE
	}
	
	public EmailServer(String url, EmailServerTypes type, String domain) {
		this.url = url;
		this.type = type;
		this.domain = domain;
	}
	
	/**
	 * Default server type is IMAP
	 */
	public EmailServer(String url) {
		this(url, EmailServerTypes.IMAP, null);
	}
	
	/**
	 * deserialize email server object
	 * expected format is {'url': <url>, 'type': <type>, 'domain': <domain>}
	 * @return
	 */
	public static EmailServer fromJson(String jsonStr) {
		
		try {
			JSONObject json = new JSONObject(jsonStr);
			return new EmailServer(json.getString("url"), EmailServerTypes.valueOf(json.getString("type")), json.getString("domain"));
		} catch (JSONException e) {
			throw new ConfigurationException("expected format must be {'url': <url>, 'type': <type>, 'domain': <domain>}");
		}
	}
	
	/**
	 * serialize meail server
	 * @return
	 */
	public String toJson() {
		return toJsonObj().toString();
	}
	
	public JSONObject toJsonObj() {
		JSONObject json = new JSONObject();
		json.put("url", url);
		json.put("type", type);
		json.put("domain", domain);
		
		return json;
	}

	public String getUrl() {
		return url;
	}

	public EmailServerTypes getType() {
		return type;
	}

	public String getDomain() {
		return domain;
	}
}
