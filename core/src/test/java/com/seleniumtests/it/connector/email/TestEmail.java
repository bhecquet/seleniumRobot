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
import com.seleniumtests.connectors.mails.Email;
import com.seleniumtests.connectors.mails.EmailAccount;

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
	
	/**
	 * @param exoClientId                  			Might be called applicationId > id of your RegisterApp which has the right Access Policy on the email you're using
	 * @param exoTenantId							Exchange tenant id > from your Microsoft Azure instance
	 * @param exoCertFileAsStringOrFile				Either a File object with the certificate (.pem) or a String with its content > certificate used to log on your RegisterApp
	 * @param exoCertPrivateKeyFileAsStringOrFile	Either a File object with the private key (.key) or a String with its content > linked to the above certificate
	 * @param exoCertPrivateKeyPass					Private key password > linked to the above private key
	 * @param exoUserEmail                 			The email address you want to read or send mail from (abc.edf@ghi.com)
	 * @throws Exception
	 */
	@Test(groups={"it"}, enabled=false)
	public void testMailExchangeOnline() throws Exception {
		EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_ONLINE, null);
		EmailClient client = EmailClientSelector.routeEmail(server, System.getProperty("exoClientId"), System.getProperty("exoTenantId"), System.getProperty("exoCertAsStringOrFile"), System.getProperty("exoCertPrivateKeyAsStringOrFile"), System.getProperty("exoCertPrivateKeyPass"), System.getProperty("exoUsermail"));
		client.getLastEmails();
	}

	/**
 	 * @param exoUserEmail                 			The email address you want to read or send mail from (abc.edf@ghi.com)
 	 * @param exoTenantId							Exchange tenant id > from your Microsoft Azure instance
	 * @param exoClientId                  			Might be called applicationId > id of your RegisterApp which has the right Access Policy on the email you're using
	 * @param exoCertFileAsStringOrFile				Either a File object with the certificate (.pem) or a String with its content > certificate used to log on your RegisterApp
	 * @param exoCertPrivateKeyFileAsStringOrFile	Either a File object with the private key (.key) or a String with its content > linked to the above certificate
	 * @param exoCertPrivateKeyPass					Private key password > linked to the above private key
	 * @throws Exception
	 */
	@Test(groups = {"it"}, enabled = false)
    public void testMailExchangeOnlineViaAccount() throws Exception {
        EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_ONLINE, null);
        EmailAccount account = new EmailAccount(System.getProperty("exoUsermail"), System.getProperty("exoTenantId"), System.getProperty("exoClientId"), System.getProperty("exoCertAsStringOrFile"), System.getProperty("exoCertPrivateKeyAsStringOrFile"), System.getProperty("exoCertPrivateKeyPass"), server);
        account.checkEmailPresenceByBody("content of the mail body", new String[]{}, 90);
    }
	
	@Test(groups={"it"}, enabled=false)
	public void testSendMail() throws Exception {
		EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_EWS, "<domain_for_user>");
		EmailClient client = EmailClientSelector.routeEmail(server, "<email_of_mailbox_to_consult>", "<user_to_connect_to_mailbox>", "<password>");
		client.sendMessage(Arrays.asList("myaddress@mydomain.com"), "hello", "hello");
	}
	
	@Test(groups={"it"}, enabled=false)
	public void testSendMailExchangeOnline() throws Exception {
		EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_ONLINE, null);
		EmailClient client = EmailClientSelector.routeEmail(server, System.getProperty("exoClientId"), System.getProperty("exoTenantId"), System.getProperty("exoCert"), System.getProperty("exoCertPrivateKey"), System.getProperty("exoCertPrivateKeyPass"), System.getProperty("exoUsermail"));
		client.sendMessage(Arrays.asList("myaddress@mydomain.com"), "hello", "hello");
	}
}
