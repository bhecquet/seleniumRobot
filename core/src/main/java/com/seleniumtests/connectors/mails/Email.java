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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Email {
	
	private String subject;
	private String content;
	private List<String> attachment;
	private String sender;
	private LocalDateTime datetime;
	
	public Email() {
		subject = "";
		content = "";
		sender = "";
		datetime = LocalDateTime.now();
		attachment = null;
	}

	public Email(String subject, String content, String sender, LocalDateTime datetime, List<String> attachment) {
		this.subject = subject;
		this.content = content;
		this.attachment = attachment;
		this.sender = sender;
		this.datetime = datetime;
	}
	
	/**
	 * Returns list of link in an email
	 * @return
	 */
	public List<String> getContentHyperlinks() {
		List<String> linksList = new ArrayList<>();
		
		Pattern p = Pattern.compile("<a.*?href=\"(.*?)\"");
		Matcher matcher = p.matcher(getContent().replace("\n", "").replace("\r", ""));
		while (matcher.find()) {
			linksList.add(matcher.group(1));
		}
		
		return linksList;
	}
	
	/**
	 * Copy email into this one
	 * @param email
	 */
	public void copy(Email email) {
		setSubject(email.getSubject());
		setSender(email.getSender());
		setContent(email.getContent());
		setDatetime(email.getDatetime());
		setAttachment(email.getAttachment());
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	public List<String> getAttachment() {
		return attachment;
	}

	public String getSender() {
		return sender;
	}

	public LocalDateTime getDatetime() {
		return datetime;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setAttachment(List<String> attachment) {
		this.attachment = attachment;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setDatetime(LocalDateTime datetime) {
		this.datetime = datetime;
	}
}
