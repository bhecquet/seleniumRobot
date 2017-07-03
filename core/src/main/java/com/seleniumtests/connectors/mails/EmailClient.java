package com.seleniumtests.connectors.mails;

import java.time.LocalDateTime;
import java.util.List;

import javax.mail.MessagingException;

import com.seleniumtests.connectors.mails.EmailClientImpl.SearchMode;


public interface EmailClient {
	
	
	public List<Email> getLastEmails(String folderName) throws Exception;
	public List<Email> getLastEmails() throws Exception;
	public List<Email> getEmails(int firstMessageIndex) throws Exception;
	public List<Email> getEmails(String folderName, int firstMessageIndex) throws Exception;
	public List<Email> getEmails(LocalDateTime firstMessageTime) throws Exception;
	public List<Email> getEmails(String folderName, LocalDateTime firstMessageTime) throws Exception;
	public List<Email> getEmails(String folderName, int firstMessageIndex, LocalDateTime firstMessageTime) throws Exception;
	public void disconnect() throws MessagingException;
	public void setLastMessageIndex() throws Exception;
	public void setLastMessageIndex(int messageIndex);
	public int getLastMessageIndex();
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames, Email email) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames, Email email) throws Exception;
	public Email getEmail(String subject, List<String> attachmentNames) throws Exception;
	public Email getEmail(String subject, String[] attachmentNames) throws Exception;
	public SearchMode getSearchMode();
	public void setSearchMode(SearchMode searchMode);
	public LocalDateTime getFromDate();
	public void setFromDate(LocalDateTime fromDate);
	
}
