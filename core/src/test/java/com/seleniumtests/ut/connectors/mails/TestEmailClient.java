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
	private ImapClient emailClient;

	@BeforeMethod(groups={"ut"})
	public void init() throws Exception {
		PowerMockito.mockStatic(EmailClientSelector.class);
		when(EmailClientSelector.routeEmail(any(EmailServer.class), anyString(), anyString(), anyString())).thenReturn(emailClient);
		when(emailClient.getEmail(anyString(), anyList())).thenCallRealMethod();
		when(emailClient.getEmail(anyString(), any(String[].class))).thenCallRealMethod();
		when(emailClient.checkMessagePresenceInLastMessages(anyString(), any(String[].class))).thenCallRealMethod();
		when(emailClient.checkMessagePresenceInLastMessages(anyString(), anyList())).thenCallRealMethod();
		when(emailClient.checkMessagePresenceInLastMessages(anyString(), any(String[].class), any(Email.class))).thenCallRealMethod();
		when(emailClient.checkMessagePresenceInLastMessages(anyString(), anyList(), any(Email.class))).thenCallRealMethod();
		when(emailClient.getLastEmails(nullable(String.class))).thenCallRealMethod();
		when(emailClient.getLastEmails()).thenCallRealMethod();
		when(emailClient.getEmails(anyString())).thenCallRealMethod();
		when(emailClient.getEmails(anyInt())).thenCallRealMethod();
		when(emailClient.getEmails(nullable(String.class), anyInt())).thenCallRealMethod();
		when(emailClient.getEmails(nullable(String.class), nullable(LocalDateTime.class))).thenCallRealMethod();
		when(emailClient.getEmails(nullable(LocalDateTime.class))).thenCallRealMethod();
		when(emailClient.getLastMessageIndex()).thenReturn(1);
		when(emailClient.isTestMode()).thenReturn(true);
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Assert.assertNull(emailClient.getEmail("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt"}));
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails.subList(0, 1), emails);
		when(emailClient.isTestMode()).thenReturn(false);
		
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Email email = new Email();
		Assert.assertEquals(emailClient.checkMessagePresenceInLastMessages("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt"}, email).size(), 0);
		Assert.assertEquals(email.getSubject(), "subject2");
	}
	
	/**
	 * Check we an email is found, but some attachments are missing
	 * @throws Exception
	 */
	@Test(groups={"ut"})
	public void testCheckMessagePresenceInLastMessagesNotAllAttachments() throws Exception {
		
		List<Email> emails = new ArrayList<>();
		emails.add(new Email("subject", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"infos.pdf", "contract-123.txt"})));
		emails.add(new Email("subject2", "text", "someone@company.com", LocalDateTime.now(), Arrays.asList(new String[] {"contract-123.pdf"})));
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
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
		
		when(emailClient.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
		
		EmailServer server = new EmailServer(serverUrl, EmailServerTypes.EXCHANGE, "");
		EmailClient emailClient = EmailClientSelector.routeEmail(server, emailAddress, login, password);
		emailClient.setLastMessageIndex(emailClient.getLastMessageIndex() - 1);
		
		Email email = new Email();
		List<String> missingAttachments = emailClient.checkMessagePresenceInLastMessages("subject.*", new String[] {"contract-\\d+\\.pdf", "infos.txt", "user.info"}, email);
		Assert.assertEquals(missingAttachments.size(), 1);
		Assert.assertEquals(email.getSubject(), "subject2");
	}

}


