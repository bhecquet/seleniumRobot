package com.seleniumtests.connectors.mails;

import java.io.IOException;

import javax.mail.MessagingException;


public class ExchangeClient extends ImapClient {
	
	public ExchangeClient(String host, String username, String password, String folder) throws MessagingException, IOException {
		super(host, username, password, folder, 143);
	}
}