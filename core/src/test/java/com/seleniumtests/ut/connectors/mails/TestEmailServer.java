package com.seleniumtests.ut.connectors.mails;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.mails.EmailServer;
import com.seleniumtests.connectors.mails.EmailServer.EmailServerTypes;
import com.seleniumtests.customexception.ConfigurationException;

public class TestEmailServer extends Object {

	
	
	@Test(groups={"ut"})
	public void testFromJson() {
		EmailServer server = EmailServer.fromJson("{'url': 'msg.company.com', 'type': 'EXCHANGE_EWS', 'domain': 'company'}");
		Assert.assertEquals(server.getUrl(), "msg.company.com");
		Assert.assertEquals(server.getType(), EmailServerTypes.EXCHANGE_EWS);
		Assert.assertEquals(server.getDomain(), "company");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testFromJson2() {
		EmailServer.fromJson("{'url': 'msg.company.com', 'type': 'EXCHANGE_EWS'}");
	}
	
	@Test(groups={"ut"})
	public void testToJson() {
		EmailServer server = new EmailServer("msg.company.com", EmailServerTypes.EXCHANGE, "company");
		Assert.assertEquals(server.toJson(), "{\"domain\":\"company\",\"type\":\"EXCHANGE\",\"url\":\"msg.company.com\"}");
	}
}
