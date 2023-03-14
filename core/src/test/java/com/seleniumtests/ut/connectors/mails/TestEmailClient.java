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
package com.seleniumtests.ut.connectors.mails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;
import io.cucumber.java.an.E;
import io.cucumber.java.bs.A;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.mails.Email;
import com.seleniumtests.connectors.mails.EmailClient;
import com.seleniumtests.connectors.mails.EmailClientSelector;
import com.seleniumtests.connectors.mails.EmailServer;
import com.seleniumtests.connectors.mails.EmailServer.EmailServerTypes;
import com.seleniumtests.connectors.mails.ImapClient;

@PrepareForTest({EmailClientSelector.class})
public class TestEmailClient extends MockitoTest {
	
	private String serverUrl = "";
	private String emailAddress = "";
	private String login = "";
	private String password = "";
	
	@Mock
	private ImapClient emailClientMock;

	@BeforeMethod(groups={"ut"})
	public void init() throws Exception {
		PowerMockito.mockStatic(EmailClientSelector.class);
		when(EmailClientSelector.routeEmail(any(EmailServer.class), anyString(), anyString(), anyString())).thenReturn(emailClientMock);
		when(emailClientMock.getEmail(anyString(), anyList())).thenCallRealMethod();
		when(emailClientMock.getEmail(anyString(), any(String[].class))).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), any(String[].class))).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), anyList())).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), any(String[].class), any(Email.class))).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), anyList(), any(Email.class))).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), any(String[].class), any(Email.class), anyInt())).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), anyList(), any(Email.class), anyInt())).thenCallRealMethod();

		when(emailClientMock.checkMessagePresenceInLastMessagesByBody(nullable(String.class), any(String[].class), any(Email.class), anyInt())).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessagesByBody(nullable(String.class), anyList(), any(Email.class), anyInt())).thenCallRealMethod();
		when(emailClientMock.checkMessagePresenceInLastMessagesByBody(nullable(String.class), any(String[].class), any(Email.class))).thenCallRealMethod();

		when(emailClientMock.getLastEmails(nullable(String.class))).thenCallRealMethod();
		when(emailClientMock.getLastEmails()).thenCallRealMethod();
		when(emailClientMock.getEmails(anyString())).thenCallRealMethod();
		when(emailClientMock.getEmails(anyInt())).thenCallRealMethod();
		when(emailClientMock.getEmails(nullable(String.class), anyInt())).thenCallRealMethod();
		when(emailClientMock.getEmails(nullable(String.class), nullable(LocalDateTime.class))).thenCallRealMethod();
		when(emailClientMock.getEmails(nullable(LocalDateTime.class))).thenCallRealMethod();
		when(emailClientMock.getLastMessageIndex()).thenReturn(1);
		when(emailClientMock.isTestMode()).thenReturn(true);
	}
	
	/**
	 * Check we get the first email if several match
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testGetEmail() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), new ArrayList<>()));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), new ArrayList<>()));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Email email = null;
		try {
			email = emailClient.getEmail(".*", new String[] {});
		} catch (Exception e) {
		} 
		Assert.assertEquals(email, emails.get(0));
	}
	
	/**
	 * Check no email is get when subject does not match
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testGetEmailNonMatchingSubject() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), new ArrayList<>()));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), new ArrayList<>()));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);

		Assert.assertNull(emailClient.getEmail("a title", new String[] {}));
	}
	
	/**
	 * Check email is get when all attachments match
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testGetEmailMatchingAttachment() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf", "infos.txt"})));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Assert.assertEquals(emailClient.getEmail("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt"}), emails.get(1));
	}
	
	/**
	 * Check nothing is returned when an attachment is missing
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testGetEmailNonMatchingAttachment() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf"})));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Assert.assertNull(emailClient.getEmail("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt"}));
	}

	/**
	 * Check if email(s) found with it/them one word content
	 */
	@Test(groups = {"ut"})
	public void testGetEmailByContentOneWord() throws Exception {
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf"})));

		when(emailClientMock.getEmailsByContent(nullable(String.class))).thenCallRealMethod();
		when(emailClientMock.getLastEmails()).thenReturn(emails);

		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);

		Assert.assertEquals(emailClient.getEmailsByContent("text").size(), emails.size());
	}

	/**
	 * Check if email(s) found with it/them part of content
	 */
	@Test(groups = {"ut"})
	public void testGetEmailByContent() throws Exception {
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("Jellyfish", "jellyfish let themselves be carried by the current", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf"})));
		emails.add(new Email("Shark", "sharks eat things", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"ocean.pdf"})));
		emails.add(new Email("Dolphin", "dolphins are mean", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "ocean.txt"})));
		emails.add(new Email("Whale", "whales are peacefull", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"deep.pdf", "whali.png"})));

		when(emailClientMock.getEmailsByContent(nullable(String.class))).thenCallRealMethod();
		when(emailClientMock.getLastEmails()).thenReturn(emails);

		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);

		List<Email> listToCheck = emailClient.getEmailsByContent("are");

		Assert.assertEquals(listToCheck.size(), 2);
		Assert.assertEquals(listToCheck.get(0).getSubject(), "Dolphin");
	}

	/**
	 * Check if getEmailsByContent get a ScenarioException if content is null
	 */
	@Test(groups = {"ut"}, expectedExceptions = ScenarioException.class)
	public void testGetEmailByContentNull() throws Exception {
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("Jellyfish", "jellyfish let themselves be carried by the current", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf"})));
		emails.add(new Email("Whale", "whales are peacefull", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"deep.pdf", "whali.png"})));

		when(emailClientMock.getEmailsByContent(nullable(String.class))).thenCallRealMethod();
		when(emailClientMock.getLastEmails()).thenReturn(emails);

		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);

		emailClient.getEmailsByContent(null);
	}

	/**
	 * Check if getEmailsByContent get a IllegalArgumentException if content is less than 3 characters
	 */
	@Test(groups = {"ut"}, expectedExceptions = IllegalArgumentException.class)
	public void testGetEmailByContentEmpty() throws Exception {
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("Jellyfish", "jellyfish let themselves be carried by the current", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf"})));
		emails.add(new Email("Whale", "whales are peacefull", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"deep.pdf", "whali.png"})));

		when(emailClientMock.getEmailsByContent(nullable(String.class))).thenCallRealMethod();
		when(emailClientMock.getLastEmails()).thenReturn(emails);

		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);

		emailClient.getEmailsByContent("");
	}

	/**
	 * Check email is get when all attachments match. Test retry is available
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testGetEmailMatchingAttachmentWithRetry() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf", "infos.txt"})));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails.subList(0, 1), emails);
		when(emailClientMock.isTestMode()).thenReturn(false);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Assert.assertEquals(emailClient.getEmail("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt"}), emails.get(1));
	}
	
	/**
	 * Check we get the right email and that no attachments are missing
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCheckMessagePresenceInLastMessages() throws Exception {
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf", "infos.txt"})));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Email email = new Email();
		Assert.assertEquals(emailClient.checkMessagePresenceInLastMessages("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt"}, email).size(), 0);
		Assert.assertEquals(email.getSubject(), "subject2");
	}

	@Test(groups={"ut"})
	public void testCheckMessagePresenceInLastMessagesByBody() throws Exception {
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text without good content", "someone@company.com", LocalDateTime.now(), Arrays.asList("infos.pdf", "contract-123.txt")));
		emails.add(new Email("subject2", "text within good content", "someone@company.com", LocalDateTime.now(), Arrays.asList("contract-123.pdf", "infos.txt")));

		when(emailClientMock.getEmailsByContent(nullable(String.class))).thenReturn(emails);

		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);

		Email email = new Email();
		emailClient.checkMessagePresenceInLastMessagesByBody("text within good content", new String[] {"contract-\\d+\\.pdf", "infos.txt"}, email);

		Assert.assertEquals(email.getContent(), "text within good content");
	}

	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testCheckMessagePresenceInLastMessagesByBodyWithNullContent() throws Exception {
		when(emailClientMock.getEmailsByContent(nullable(String.class))).thenCallRealMethod();

		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);

		Email email = new Email();
		emailClientMock.checkMessagePresenceInLastMessagesByBody(null, new String[] {"contract-\\d+\\.pdf", "infos.txt"}, email);
	}
	
	/**
	 * Check we have an email is found, but some attachments are missing
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCheckMessagePresenceInLastMessagesNotAllAttachments() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf"})));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Email email = new Email();
		List<String> missingAttachments = emailClient.checkMessagePresenceInLastMessages("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt"}, email);
		Assert.assertEquals(missingAttachments.size(), 1);
		Assert.assertEquals(email.getSubject(), "subject2");
	}
	
	/**
	 * Check no email found
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCheckMessagePresenceInLastMessagesNoEmailFound() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf"})));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Email email = new Email();
		List<String> missingAttachments = emailClient.checkMessagePresenceInLastMessages("title", new String[] {"contract-\\d+\\.pdf", "infos.txt"}, email);
		Assert.assertNull(missingAttachments);
	}
	
	/**
	 * Check that best email is rendered when serveral match (the one with the most matching attachments)
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCheckMessagePresenceInLastMessagesBestMatching() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf", "infos.txt"})));
		emails.add(new Email("subject3", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf"})));
		
		when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Email email = new Email();
		List<String> missingAttachments = emailClient.checkMessagePresenceInLastMessages("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt", "user.info"}, email);
		Assert.assertEquals(missingAttachments.size(), 1);
		Assert.assertEquals(email.getSubject(), "subject2");
	}

}


