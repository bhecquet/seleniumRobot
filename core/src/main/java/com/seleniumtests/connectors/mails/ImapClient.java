package com.seleniumtests.connectors.mails;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

import org.apache.commons.lang3.StringEscapeUtils;


public class ImapClient extends EmailClientImpl {
	
	private Store store;
	
	
	private Integer imapPort;
	
	public ImapClient(String host, String username, String password, String folder) throws MessagingException, IOException {
		this(host, username, password, folder, 143);
	}
	
	/**
	 * Constructor
	 * @param host				server address
	 * @param username			login for server
	 * @param password			password for server
	 * @param folder			folder to read
	 * @param imapPort			port to use to connect
	 * @param timeOffset		workaround when server does not report the same time
	 * @throws Exception 
	 */
	public ImapClient(String host, String username, String password, String folder, Integer imapPort) throws MessagingException, IOException {
		
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
	 * @param firstMessageIndex index of the firste message to find
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
		
		List<Email> filteredEmails = new ArrayList<>();
		lastMessageIndex = messages.length;

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
				List<BodyPart> partList = getMessageParts((Multipart) message
						.getContent());

				// store content in list
				for (BodyPart part : partList) {

					String partContentType = part.getContentType().toLowerCase();
					if (partContentType.contains("text/html")) {
						messageContent = messageContent.concat(StringEscapeUtils.unescapeHtml4(part.getContent().toString()));
						
					} else if (partContentType.contains("text/")
						&& !partContentType.contains("vcard")) {
						messageContent = messageContent.concat((String)part.getContent().toString());					
						
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
			}
			
			// create a new email
			filteredEmails.add(new Email(message.getSubject(), messageContent, "", message.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), attachments));
		}
		

		folder.close(false);
		
		return filteredEmails;
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

}
