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

import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.SearchTerm;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class ImapClient extends EmailClientImpl {
	
	private Store store;
	
	
	private Integer imapPort;
	
	public ImapClient(String host, String username, String password, String folder) throws MessagingException {
		this(host, username, password, folder, 143);
	}
	
	/**
	 * Constructor
	 * @param host				server address
	 * @param username			login for server
	 * @param password			password for server
	 * @param folder			folder to read
	 * @param imapPort			port to use to connect
	 * @throws Exception 
	 */
	public ImapClient(String host, String username, String password, String folder, Integer imapPort) throws MessagingException {
		
		super();
		
		// connect to server
		this.imapPort = imapPort;
		connect(host, username, password);
		this.folder = folder;
		
		if (folder != null) {
			setLastMessageIndex();
		} 	
	}
	
	
	/**
	 * returns message parts
	 * @param content	
	 * @return			message parts
	 * @throws IOException
	 * @throws MessagingException
	 */
	private List<BodyPart> getMessageParts(Multipart content) throws IOException, MessagingException {
		List<BodyPart> partList = new ArrayList<>();

		for (int partId=0; partId < content.getCount(); partId++) {
			BodyPart part = content.getBodyPart(partId);
			   
			if (part.getContentType().toLowerCase().contains("multipart/")) {
				
				partList.addAll(getMessageParts((Multipart)part.getContent()));
			} else {
				partList.add(part);
			}
		}
		
		return partList;
	}
	
	/**
	 * connect to folder
	 * @param host				server address
	 * @param username			login for server
	 * @param password			password for server
	 * @throws MessagingException
	 */
	private void connect(String host, String username, String password) throws MessagingException {

		// Create empty properties
		Properties props = new Properties();

		// Get session
		Session session = Session.getDefaultInstance(props, null);

		// Get the store
		store = session.getStore("imap");
		store.connect(host, imapPort, username, password);
	}
	
	/**
	 * disconnect from server
	 */
	@Override
	public void disconnect() throws MessagingException {
		store.close();
	}
	
	/**
	 * get list of all emails in folder
	 * 
	 * @param folderName		folder to read
	 * @param firstMessageTime	date from which we should get messages
	 * @param firstMessageIndex index of the first message to find
	 * @throws MessagingException
	 * @throws IOException
	 */
	@Override
	public List<Email> getEmails(String folderName, int firstMessageIndex, LocalDateTime firstMessageTime) throws MessagingException, IOException {
		
		if (folderName == null) {
			throw new MessagingException("folder ne doit pas être vide");
		}
		
		// Get folder
		Folder folder = store.getFolder(folderName);
		folder.open(Folder.READ_ONLY);

		// Get directory
		Message[] messages = folder.getMessages();
		
		List<Message> preFilteredMessages = new ArrayList<>();
		
		final LocalDateTime firstTime = firstMessageTime;
		
		// on filtre les message en fonction du mode de recherche
		if (searchMode == SearchMode.BY_INDEX || firstTime == null) {
			//preFilteredMessages = Arrays.asList(Arrays.copyOfRange(messages, firstMessageIndex, messages.length)); => to test
			for (int i = firstMessageIndex, n = messages.length; i < n; i++) {
				preFilteredMessages.add(messages[i]);
			}
		} else {
			preFilteredMessages = Arrays.asList(folder.search(new SearchTerm() {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean match(Message msg) {
					try {
						return !msg.getReceivedDate().before(Date.from(firstTime.atZone(ZoneId.systemDefault()).toInstant()));
					} catch (MessagingException e) {
						return false;
					}
				}
			}));
			
		}
		
		lastMessageIndex = messages.length;
		List<Email> filteredEmails = filterMessages(preFilteredMessages);
		

		folder.close(false);
		
		return filteredEmails;
	}

	/**
	 * @param preFilteredMessages
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	private List<Email> filterMessages(List<Message> preFilteredMessages) throws MessagingException, IOException {
		List<Email> filteredEmails = new ArrayList<>();

		for (Message message: preFilteredMessages) {

			String contentType = "";
			try {
				contentType = message.getContentType();
			} catch (MessagingException e) {
				MimeMessage msg = (MimeMessage) message;
				message = new MimeMessage(msg);
				contentType = message.getContentType();
			}

			// decode content
			String messageContent = "";
			List<String> attachments = new ArrayList<>();

			if (contentType.toLowerCase().contains("text/html")) {
				messageContent += StringEscapeUtils.unescapeHtml4(message.getContent().toString());
			} else if (contentType.toLowerCase().contains("multipart/")) {
				messageContent = parseMultipartBody(message, messageContent, attachments);
			}
			
			// create a new email
			filteredEmails.add(new Email(message.getSubject(), messageContent, "", message.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), attachments));
		}
		return filteredEmails;
	}

	/**
	 * @param message
	 * @param messageContent
	 * @param attachments
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 */
	private String parseMultipartBody(Message message, String messageContent, List<String> attachments)
			throws IOException, MessagingException {
		List<BodyPart> partList = getMessageParts((Multipart) message.getContent());

		// store content in list
		for (BodyPart part : partList) {

			String partContentType = part.getContentType().toLowerCase();
			if (partContentType.contains("text/html")) {
				messageContent = messageContent.concat(StringEscapeUtils.unescapeHtml4(part.getContent().toString()));
				
			} else if (partContentType.contains("text/")
				&& !partContentType.contains("vcard")) {
				messageContent = messageContent.concat(part.getContent().toString());					
				
			} else if (partContentType.contains("image")
					|| partContentType.contains("application/")
					|| partContentType.contains("text/x-vcard")) {
				if (part.getFileName() != null) {
					attachments.add(part.getFileName());
				} else {
					attachments.add(part.getDescription());
				}
			} else {
				logger.debug("type: " + part.getContentType());
			}
		}
		return messageContent;
	}
	
	/**
	 * define last message index on the server, so that on next request, we only get the newly received messages
	 * @throws Exception 
	 */
	@Override
	public void setLastMessageIndex() throws MessagingException {
		lastMessageIndex = getMessageNumber(folder);
	}
	
	/**
	 * define last message index on the server
	 * 
	 * @param messageIndex	index for the reference message
	 */
	@Override
	public void setLastMessageIndex(int messageIndex) {
		lastMessageIndex = messageIndex;
	}
	
	@Override
	public int getLastMessageIndex() {
		return lastMessageIndex;
	}


	/**
	 * Returns the number of messages on the server
	 * 
	 * @param folderName
	 * @return 
	 * @throws MessagingException
	 */
	private Integer getMessageNumber(String folderName) throws MessagingException {
		// Get folder
		Folder folder = store.getFolder(folderName);
		folder.open(Folder.READ_ONLY);
		
		Integer messageCount = folder.getMessageCount();
		
		folder.close(false);
		
		return messageCount;
	}

	@Override
	public LocalDateTime getFromDate() {
		return fromDate;
	}

	@Override
	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}

	@Override
	public void sendMessage(List<String> to, String title, String body) throws Exception {
		throw new NotImplementedException();
		
	}

	@Override
	public void sendMessage(List<String> to, String title, String body, List<File> attachments) throws Exception {
		throw new NotImplementedException();
	}

}
