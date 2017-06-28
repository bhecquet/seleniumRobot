package com.seleniumtests.connectors.mails;

import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

public class Exchange2010Client extends EWSClient {
	
	public Exchange2010Client(String host, String username, String password, String email, String domain, String folder, Integer timeOffset) throws Exception {
		super(host, username, password, email, domain, folder, ExchangeVersion.Exchange2010_SP2, timeOffset);
	}
}