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


import org.apache.logging.log4j.Logger;

import com.seleniumtests.connectors.mails.EmailServer.EmailServerTypes;
import com.seleniumtests.util.logging.SeleniumRobotLogger;


public class EmailClientSelector {

	private static final String INBOX = "INBOX";
	private static final Logger logger = SeleniumRobotLogger.getLogger(EmailClientSelector.class);

	private EmailClientSelector() {
		// private constructor
	}

	/**
	 * Returns a client depending on email address
	 * 
	 * @param mailServer			mail server address
	 * @param emailAddress			email address we should consult
	 * @param loginEmailAccount		login
	 * @param passwordEmailAccount	password
	 * @return						email client
	 */
	public static EmailClient routeEmail(EmailServer server, String emailAddress, String loginEmailAccount, String passwordEmailAccount) {
		if (emailAddress == null || server == null) {
			return null;
		}
		
		if (server.getType() == EmailServerTypes.EXCHANGE) {
			try {
				return new ExchangeClient(server.getUrl(), loginEmailAccount, passwordEmailAccount, INBOX);
			} catch (Exception e) {
				logger.error("Cannot connect exchange 2003 server: " + e.getMessage());
			}
		} else if (server.getType() == EmailServerTypes.EXCHANGE_EWS) {
			try {
				return new Exchange2010Client(server.getUrl(), loginEmailAccount, passwordEmailAccount, emailAddress, server.getDomain(), "Inbox", 0);
			} catch (Exception e) {
				logger.error("Cannot connect to exchange via web service: " + e.getMessage());
			}
		} else if (server.getType() == EmailServerTypes.EXCHANGE_ONLINE) {
			try {
				return new ExchangeOnline(server.getUrl(), loginEmailAccount, passwordEmailAccount, emailAddress);
			} catch (Exception e) {
				logger.error("Cannot connect to exchange via web service: " + e.getMessage());
			}
		} else if (server.getType() == EmailServerTypes.GMAIL) {
				try {
					return new GMailClient(server.getUrl(), loginEmailAccount, passwordEmailAccount, INBOX);
				} catch (Exception e) {
					logger.error("Cannot connect to gmail server: " + e.getMessage());
				} 
		} else {
			try {
				return new ImapClient(server.getUrl(), loginEmailAccount, passwordEmailAccount, INBOX);
			} catch (Exception e) {
				logger.error("Cannot connect to imap server: " + e.getMessage());
			} 
		}
		return null;
	}

}
