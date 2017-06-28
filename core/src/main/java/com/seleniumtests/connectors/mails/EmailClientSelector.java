package com.seleniumtests.connectors.mails;


import org.apache.log4j.Logger;

import com.seleniumtests.connectors.mails.EmailServer.EmailServerTypes;
import com.seleniumtests.util.logging.SeleniumRobotLogger;


public class EmailClientSelector {

	private static final Logger logger = SeleniumRobotLogger.getLogger(EmailClientSelector.class);


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
				return new ExchangeClient(server.getUrl(), loginEmailAccount, passwordEmailAccount, "INBOX");
			} catch (Exception e) {
				logger.error("Erreur de connexion au serveur mail Exchange 2003: " + e.getMessage());
			}
		} else if (server.getType() == EmailServerTypes.EXCHANGE_EWS) {
			try {
				return new Exchange2010Client(server.getUrl(), loginEmailAccount, passwordEmailAccount, emailAddress, server.getDomain(), "Inbox", 0);
			} catch (Exception e) {
				logger.error("Erreur de connexion au serveur mail exchange EWS: " + e.getMessage());
			} 
		} else if (server.getType() == EmailServerTypes.GMAIL) {
				try {
					return new GMailClient(server.getUrl(), loginEmailAccount, passwordEmailAccount, "INBOX");
				} catch (Exception e) {
					logger.error("Erreur de connexion au serveur GMail: " + e.getMessage());
				} 
		} else {
			try {
				return new ImapClient(server.getUrl(), loginEmailAccount, passwordEmailAccount, "INBOX");
			} catch (Exception e) {
				logger.error("Erreur de connexion au serveur IMAP: " + e.getMessage());
			} 
		}
		return null;
	}

}
