package com.seleniumtests.ut.connectors.mails;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.mails.EmailAccount;
import com.seleniumtests.customexception.ConfigurationException;

public class TestEmailAccount extends Object {

	@Test(groups={"ut"})
	public void testCantConnectWithoutInfo() {
		EmailAccount account = new EmailAccount();
		Assert.assertFalse(account.canConnect(), "We should not be able to connect to default account");
	}
	
	@Test(groups={"ut"})
	public void testCantConnectWithOnlyAddress() {
		EmailAccount account = new EmailAccount("email@free.fr", null, null);
		Assert.assertFalse(account.canConnect(), "We should not be able to connect to default account");
	}
	
	@Test(groups={"ut"})
	public void testCantConnectWithDefaultAddress() {
		EmailAccount account = new EmailAccount("no.email@free.fr", "no.email@free.fr", "aaa");
		Assert.assertFalse(account.canConnect(), "We should not be able to connect to default account");
	}
	
	@Test(groups={"ut"})
	public void testCanConnect() {
		EmailAccount account = new EmailAccount("email@free.fr", "email@free.fr", "aaa");
		Assert.assertTrue(account.canConnect(), "We should be able to connect");
	}
	
	@Test(groups={"ut"})
	public void testFromJson() {
		EmailAccount account = EmailAccount.fromJson("{'email': 'someone@company.com', 'login': \"someone\", 'password': 'someone'}");
		Assert.assertEquals(account.getEmail(), "someone@company.com");
		Assert.assertEquals(account.getEmailLogin(), "someone");
		Assert.assertEquals(account.getEmailPassword(), "someone");
	}
	
	@Test(groups={"ut"}, expectedExceptions=ConfigurationException.class)
	public void testFromJson2() {
		EmailAccount.fromJson("{'email': 'someone@company.com', 'password': 'someone'}");
	}
	
	@Test(groups={"ut"})
	public void testToJson() {
		EmailAccount account = new EmailAccount("someone@company.com", "someone", "someone");
		String jsonString = account.toJson();
		Assert.assertTrue(jsonString.contains("\"email\":\"someone@company.com\""));
		Assert.assertTrue(jsonString.contains("\"login\":\"someone\""));
		Assert.assertTrue(jsonString.contains("\"password\":\"someone\""));
	}
}
