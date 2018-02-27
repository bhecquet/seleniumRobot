package com.seleniumtests.it.connector.email;

import org.testng.annotations.Test;

import com.seleniumtests.connectors.mails.EmailClient;
import com.seleniumtests.connectors.mails.EmailClientSelector;
import com.seleniumtests.connectors.mails.EmailServer;
import com.seleniumtests.connectors.mails.EmailServer.EmailServerTypes;

public class TestEmail {

	/**
	 * Test to show how to use email access
	 * @throws Exception
	 */
	@Test(groups={"it"}, enabled=false)
	public void testMail() throws Exception {
		EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_EWS, "<domain_for_user>");
		EmailClient client = EmailClientSelector.routeEmail(server, "<email_of_mailbox_to_consult>", "<user_to_connect_to_mailbox>", "<password>");
		client.getLastEmails();
	}
}
