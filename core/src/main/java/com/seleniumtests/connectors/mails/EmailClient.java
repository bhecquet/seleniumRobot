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
import java.util.List;

import com.seleniumtests.connectors.mails.EmailClientImpl.SearchMode;
import jakarta.mail.MessagingException;


public interface EmailClient {

	public List<Email> getLastEmails(String folderName) throws Exception;
	public List<Email> getLastEmails() throws Exception;
	public List<Email> getEmails(int firstMessageIndex) throws Exception;
	public List<Email> getEmails(String folderName, int firstMessageIndex) throws Exception;
	public List<Email> getEmails(LocalDateTime firstMessageTime) throws Exception;
	public List<Email> getEmails(String folderName, LocalDateTime firstMessageTime) throws Exception;
	public List<Email> getEmails(String folderName, int firstMessageIndex, LocalDateTime firstMessageTime) throws Exception;
	List<Email> getEmailsByContent(String content) throws Exception;
	public void disconnect() throws MessagingException;
	public void setLastMessageIndex() throws Exception;
	public void setLastMessageIndex(int messageIndex);
	public int getLastMessageIndex();
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames, Email email) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames, Email email) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames, Email email, int timeoutInSeconds) throws Exception;
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames, Email email, int timeoutInSeconds) throws Exception;
	public Email getEmail(String subject, List<String> attachmentNames) throws Exception;

	public List<String> checkMessagePresenceInLastMessagesByBody(String content, String[] attachmentNames, Email email) throws Exception;
	List<String> checkMessagePresenceInLastMessagesByBody(String content, List<String> attachmentNames, Email emailOut, int timeoutInSeconds) throws Exception;
	List<String> checkMessagePresenceInLastMessagesByBody(String content, String[] attachmentNames, Email emailOut, int timeoutInSeconds) throws Exception;

	public Email getEmail(String subject, String[] attachmentNames) throws Exception;
	public SearchMode getSearchMode();
	public void setSearchMode(SearchMode searchMode);
	public LocalDateTime getFromDate();
	public void setFromDate(LocalDateTime fromDate);
	public void sendMessage(List<String> to, String title, String body) throws Exception;
	
}
