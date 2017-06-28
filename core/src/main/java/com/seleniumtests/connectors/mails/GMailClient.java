package com.seleniumtests.connectors.mails;

import java.io.IOException;

import javax.mail.MessagingException;


public class GMailClient extends ImapClient {
	
	public GMailClient(String host, String username, String password, String folder) throws MessagingException, IOException {
		super(host, username, password, folder, 993);
	}
}