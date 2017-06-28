package com.seleniumtests.connectors.mails;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Email {
	
	private String subject;
	private String content;
	private List<String> attachment;
	private String sender;
	private Date datetime;
	
	public Email() {
		subject = "";
		content = "";
		sender = "";
		datetime = Calendar.getInstance().getTime();
		attachment = null;
	}

	public Email(String subject, String content, String sender, Date datetime, List<String> attachment) {
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

	public Date getDatetime() {
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

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}
}
