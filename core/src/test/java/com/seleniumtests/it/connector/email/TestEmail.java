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
package com.seleniumtests.it.connector.email;

import java.util.Arrays;

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
	@Test(groups={"it"}, enabled=false)
	public void testMailExchangeOnline() throws Exception {
		EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_EWS, "<domain_for_user>");
		EmailClient client = EmailClientSelector.routeEmail(server, "<email_of_mailbox_to_consult>", "<user_to_connect_to_mailbox>", "<password>");
		client.getLastEmails();
	}

	@Test(groups={"it"}, enabled=false)
	public void testSendMail() throws Exception {
		EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_EWS, "<domain_for_user>");
		EmailClient client = EmailClientSelector.routeEmail(server, "<email_of_mailbox_to_consult>", "<user_to_connect_to_mailbox>", "<password>");
		client.sendMessage(Arrays.asList("myaddress@mydomain.com"), "hello", "hello");
	}
}
