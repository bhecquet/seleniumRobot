package com.seleniumtests.connectors.mails;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.Mailbox;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;


public class EWSClient extends EmailClientImpl {
	
	private Integer lastMessageIndex;
	private Mailbox mb;
	
	private ExchangeService service;
	private FolderId rootFolderId;
	private FolderId defaultFolderId;
	private Integer timeOffset;		// in hours to correct time beween robot and server
	private HashMap<String, FolderId> folders = new HashMap<>();

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
	public EWSClient(String host, String username, String password, String email, String domain, String folder, ExchangeVersion version, Integer timeOffset) throws Exception {
		
		super();
		// connexion au serveur
		
		connect(host, username, password, email, domain, version);
		
		defaultFolderId = findFolder(folder);
		this.timeOffset = timeOffset; 
		if (folder != null) {
			setLastMessageIndex();
		} 
		this.folder = defaultFolderId.getFolderName().name();
		
	}
	
	/**
	 * search a folder in mailbox
	 * @param folderName
	 * @return
	 * @throws Exception 
	 */
	private FolderId findFolder(String folderName) throws Exception {
		
		if (folders.containsKey(folderName)) {
			return folders.get(folderName);
		}
		
		if (folderName.equals(WellKnownFolderName.Inbox.name())) {
			FolderId fId = new FolderId(WellKnownFolderName.Inbox, mb);
			folders.put(folderName, fId);
			return fId;
		}
		
		// search folder
		FolderView view = new FolderView(100);
        view.setPropertySet(new PropertySet(BasePropertySet.IdOnly));
        view.getPropertySet().add(FolderSchema.DisplayName);
        view.setTraversal(FolderTraversal.Deep);
        FindFoldersResults findFolderResults = service.findFolders(rootFolderId, view);

        for(Folder f: findFolderResults) {
            if (f.getDisplayName().equals(folderName)) {
            	folders.put(folderName, f.getId());
                return f.getId();
            }
        }
        throw new ConfigurationException("fodler " + folderName + " does not exist for this account");
	}
	
	/**
	 * connect to folder on email server
	 * @param host				server address
	 * @param username			login for server
	 * @param password			password for server
	 * @param email				email address we would like to read messages on
	 * @param domain			email associated to email address
	 * @throws Exception 
	 */
	private void connect(String host, String username, String password, String email, String domain, ExchangeVersion version) throws Exception {
		
		service = new ExchangeService(version);
		ExchangeCredentials credentials = new WebCredentials(username, password, domain);
		service.setCredentials(credentials);
		service.setUrl(new URI("http://" + host + "/EWS/Exchange.asmx"));
		
		// mailbox to use (a mailbox could have several emails
		mb = new Mailbox();
		mb.setAddress(email);

		rootFolderId = new FolderId(WellKnownFolderName.Root, mb);
	}
	
	/**
	 * Se deconnecte du serveur
	 */
	@Override
	public void disconnect() {
		// nothing as we use API
	}
	
	/**
	 * Récupère la liste des évenements
	 * @param password
	 * @throws MalformedURLException
	 */
	public void getEvents(String password) throws MalformedURLException {
		// TODO: implement
		throw new CustomSeleniumTestsException("calendar is not implemented");
	}
	
	/**
	 * get list of all emails in folder
	 * 
	 * @param folderName		folder to read
	 * @param firstMessageTime	date from which we should get messages
	 * @throws MessagingException
	 * @throws IOException
	 */
	@Override
	public List<Email> getEmails(String folderName, Integer firstMessageIndex, LocalDateTime firstMessageTime) throws Exception {

		if (folderName == null) {
			throw new ConfigurationException("folder should not be empty");
		}
		
		FolderId folderId = findFolder(folderName);
		Folder folder = Folder.bind(service, folderId);
		
		List<Email> emails = new ArrayList<>();
		List<Item> preFilteredItems;
		
		// filter messages
		if (searchMode == SearchMode.BY_INDEX) {
			lastMessageIndex = folder.getTotalCount();
			preFilteredItems = service.findItems(folderId, new ItemView(lastMessageIndex - firstMessageIndex + 1)).getItems();
		} else {
			SearchFilter.SearchFilterCollection filter = new SearchFilter.SearchFilterCollection();
			filter.add(new SearchFilter.IsGreaterThan(ItemSchema.DateTimeReceived, Date.from(firstMessageTime.plusHours(timeOffset).atZone(ZoneId.systemDefault()).toInstant())));
			preFilteredItems = service.findItems(folderId, filter, new ItemView(lastMessageIndex)).getItems();
		}
			
		for (Item item: preFilteredItems) {

			item.load();
			List<String> attachments = new ArrayList<>();
			for (Attachment att: item.getAttachments()) {
				attachments.add(att.getName());
			}

			emails.add(new Email(item.getSubject(), item.getBody().toString(), "", item.getDateTimeReceived(), attachments));
		}

		return emails;
	}
	
	/**
	 * define last message index on the server, so that on next request, we only get the newly received messages
	 * @throws Exception 
	 */
	@Override
	public void setLastMessageIndex() throws Exception {
		Folder folder = Folder.bind(service, defaultFolderId);
		lastMessageIndex = folder.getTotalCount();
	}
	
	/**
	 * define last message index on the server
	 * 
	 * @param messageIndex	index for the reference message
	 */
	@Override
	public void setLastMessageIndex(Integer messageIndex) {
		lastMessageIndex = messageIndex;
	}

	@Override
	public Integer getLastMessageIndex() {
		return lastMessageIndex;
	}
}
