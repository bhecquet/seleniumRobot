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

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.mail.MessagingException;

import org.apache.logging.log4j.Logger;

import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class EmailClientImpl implements EmailClient {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(EmailClientImpl.class);
	
	public enum SearchMode {
		BY_DATE, BY_INDEX
	}
	
	protected SearchMode searchMode;
	protected LocalDateTime fromDate;
	protected int lastMessageIndex;
	protected String folder;
	protected boolean testMode = false;
	
	protected EmailClientImpl() {
		searchMode = SearchMode.BY_INDEX;
		lastMessageIndex = 0;
		
		// date from which messages should be searched
		fromDate = LocalDateTime.now();
	}
	
	/**
	 * Return list of emails since last server request
	 * 
	 * @param folderName	folder to read on server
	 * @return list of received emails
	 * @throws Exception 
	 */
	@Override
	public List<Email> getLastEmails(String folderName) throws Exception {
	
		if (searchMode == SearchMode.BY_INDEX || fromDate == null) {
			return getEmails(folderName, getLastMessageIndex());
		} else {
			return getEmails(folderName, fromDate);
		}
	}
	
	/**
	 * Return list of email received from the date in parameter in given folder
	 * @param folderName		name of folder to read
	 * @param firstMessageTime	date from which we should get messages
	 * @return	email list
	 */
	@Override
	public List<Email> getEmails(String folderName, LocalDateTime firstMessageTime) throws Exception {
		return getEmails(folderName, lastMessageIndex, firstMessageTime);
	}
	
	/**
	 * Return list of email in server folder
	 * 
	 * @param folderName	name of folder to read
	 * @param firstMessageTime	date from which we should get messages
	 * @throws MessagingException
	 * @throws IOException
	 */
	@Override
	public List<Email> getEmails(String folderName, int firstMessageIndex) throws Exception {
		return getEmails(folderName, firstMessageIndex, fromDate);
	}
	
	/**
	 * Returns email list since last server request
	 * @return	email list
	 * @throws Exception 
	 */
	@Override
	public List<Email> getLastEmails() throws Exception {
		return getLastEmails(folder);
	}
	
	/**
	 * Return list of email in server folder
	 * 
	 * @param firstMessageTime	date from which we should get messages
	 * @throws Exception 
	 */
	@Override
	public List<Email> getEmails(int firstMessageIndex) throws Exception {
		return getEmails(folder, firstMessageIndex, fromDate);
	}

	/**
	 * Return list of email received from the date in parameter 
	 * @param firstMessageTime	date from which we should get messages
	 * @return	email list
	 */
	@Override
	public List<Email> getEmails(LocalDateTime firstMessageTime) throws Exception {
		return getEmails(folder, lastMessageIndex, firstMessageTime);
	}
	
	/**
	 * Check that email whose subject and attachements are specified have been received
	 * @param subject			subject title. Regex are accepted
	 * @param attachmentNames	list of attachment names. Regex are allowed
	 * 
	 * @return null if no email found or list of missing attachments if an email has been found
	 */
	@Override
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames) throws Exception {
		return checkMessagePresenceInLastMessages(subject, Arrays.asList(attachmentNames));
	}
	
	/**
	 * Check that email whose subject and attachements are specified have been received
	 * @param subject			subject title. Regex are accepted
	 * @param attachmentNames	list of attachment names. Regex are allowed
	 * 
	 * @return null if no email found or list of missing attachments if an email has been found
	 * @throws Exception 
	 */
	@Override
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames) throws Exception {
		return checkMessagePresenceInLastMessages(subject, attachmentNames, new Email());
	}
	
	/**
	 * Check that email whose subject and attachements are specified have been received
	 * @param subject			subject title. Regex are accepted
	 * @param attachmentNames	list of attachment names. Regex are allowed
	 * @param email				an empty email which will store found email
	 * 
	 * @return null if no email found or list of missing attachments if an email has been found
	 * @throws Exception 
	 */
	@Override
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames, Email email) throws Exception {
		return checkMessagePresenceInLastMessages(subject, Arrays.asList(attachmentNames), email);
	}

	private List<String> getMissingAttachments(List<String> attachmentNames, Email email) {
		List<String> missingAttachments = new ArrayList<>();
		missingAttachments.addAll(attachmentNames);

		// do we have the requested attachments
		for (String attachmentName: attachmentNames) {

			for (String emailAttachment: email.getAttachment()) {
				if (emailAttachment.matches(attachmentName)) {
					missingAttachments.remove(attachmentName);
					break;
				}
			}
		}
		return missingAttachments;
	}
	
	/**
	 * Check that email whose subject and attachements are specified have been received. Wait 90 secs
	 * @param subject			subject title. Regex are accepted
	 * @param attachmentNames	list of attachment names. Regex are allowed
	 * @param emailOut			an empty email which will store found email
	 * 
	 * @return null if no email found or list of missing attachments if an email has been found
	 * @throws Exception 
	 */
	@Override
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames, Email emailOut) throws Exception {
		return checkMessagePresenceInLastMessages(subject, attachmentNames, emailOut, 90);
	}
	
	@Override
	public List<String> checkMessagePresenceInLastMessages(String subject, String[] attachmentNames, Email emailOut, int timeoutInSeconds) throws Exception {
		return checkMessagePresenceInLastMessages(subject, Arrays.asList(attachmentNames), emailOut, timeoutInSeconds);
	}
	
	/**
	 * Check that email whose subject and attachements are specified have been received
	 * @param subject			subject title. Regex are accepted
	 * @param attachmentNames	list of attachment names. Regex are allowed
	 * @param emailOut			an empty email which will store found email
	 * @param timeoutInSeconds	time to wait for the requested emails
	 * 
	 * @return null if no email found or list of missing attachments if an email has been found
	 * @throws Exception 
	 */
	@Override
	public List<String> checkMessagePresenceInLastMessages(String subject, List<String> attachmentNames, Email emailOut, int timeoutInSeconds) throws Exception {
		
		List<Email> emailList = getEmails(subject);
		Map<Email, List<String>> missingAttachmentsPerEmail = new HashMap<>();
		
		Clock clock = Clock.systemUTC();
		Instant end = clock.instant().plusSeconds(timeoutInSeconds);
		
		// try several times
		while (end.isAfter(clock.instant())) {
			
			for (Email email: emailList) {
				List<String> missingAttachments = getMissingAttachments(attachmentNames, email);
				
				// title and attachments OK
				if (missingAttachments.isEmpty()) {
					emailOut.copy(email);
					return missingAttachments;
				} else {
					missingAttachmentsPerEmail.put(email, missingAttachments.subList(0, missingAttachments.size()));
				}
			}
			
			if (isTestMode()) {
				break;
			}
			
			// not found retry with last received emails
			emailList.addAll(getEmails(subject));			
			WaitHelper.waitForSeconds(10);
		}

		SortedSet<Map.Entry<Email, List<String>>> sortedset = new TreeSet<>((e1, e2) -> ((Integer)e1.getValue().size()).compareTo((Integer)e2.getValue().size()));

		sortedset.addAll(missingAttachmentsPerEmail.entrySet());
		
		if (sortedset.isEmpty()) {
			return null;
		} else {
			Entry<Email, List<String>> entry = sortedset.first();
			emailOut.copy(entry.getKey());
			return entry.getValue();
		}
	}
	
	/**
	 * Check that email whose subject and attachments are specified have been received
	 * several retries are done in case it's not available
	 * 
	 * @param subject			subject title. Regex are accepted
	 * @param attachmentNames	list of attachment names. Regex are allowed
	 * 
	 * @return 					found email
	 * @throws Exception 
	 */
	@Override
	public Email getEmail(String subject, String[] attachmentNames) throws Exception {
		return getEmail(subject, Arrays.asList(attachmentNames));
	}
	
	/**
	 * Check if the email contains all requested attachment names
	 * @param attachmentNames
	 * @param email
	 * @return
	 */
	private boolean areAllAttachmentsFound(List<String> attachmentNames, Email email) {
		Boolean attachmentsFound = true;
		for (String attachmentName: attachmentNames) {
			Boolean attachmentFound = false;
			for (String emailAttachment: email.getAttachment()) {
				if (emailAttachment.matches(attachmentName)) {
					attachmentFound = true;
					break;
				}
			}
			attachmentsFound = attachmentsFound && attachmentFound;
		}
		return attachmentsFound;
	}
	
	/**
	 * Check that email whose subject and attachments are specified have been received
	 * several retries are done in case it's not available
	 * 
	 * @param subject			subject title. Regex are accepted
	 * @param attachmentNames	list of attachment names. Regex are allowed
	 * 
	 * @return 					found email
	 * @throws Exception 
	 */
	@Override
	public Email getEmail(String subject, List<String> attachmentNames) throws Exception {
		
		List<Email> emailList = getEmails(subject);
		
		// check several times
		for (int i = 0; i < 10; i++) {
			
			for (Email email: emailList) {

				// title and attachments OK
				if (areAllAttachmentsFound(attachmentNames, email)) {
					return email;
				}
			}
			
			if (isTestMode()) {
				break;
			}
			
			// non found retry with last received emails
			emailList.addAll(getEmails(subject));
			WaitHelper.waitForSeconds(5);
		}
		
		return null;
	}
	
	/**
	 * Returns list of newly received email whose title matches subject
	 * 
	 * @param subject			subject title. Regex are accepted
	 * @return 					email list
	 * @throws Exception 
	 */
	public List<Email> getEmails(String subject) throws Exception {

		List<Email> matchingEmails = new ArrayList<>();

		List<Email> lastEmails = getLastEmails();
		for (Email email: lastEmails) {
			
			// does title matches
			if (email.getSubject().matches(subject)) {
				matchingEmails.add(email);
			}
		}

		return matchingEmails;
	}

	@Override
	public SearchMode getSearchMode() {
		return searchMode;
	}

	@Override
	public void setSearchMode(SearchMode searchMode) {
		this.searchMode = searchMode;
	}

	@Override
	public LocalDateTime getFromDate() {
		return fromDate;
	}

	@Override
	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}

	public boolean isTestMode() {
		return testMode;
	}
}
