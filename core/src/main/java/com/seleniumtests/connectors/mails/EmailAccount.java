package com.seleniumtests.connectors.mails;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

import com.seleniumtests.connectors.mails.EmailClientImpl.SearchMode;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class EmailAccount {

	private String email;
	private String emailLogin;
	private String emailPassword;
	private LocalDateTime creationDate;
	private EmailClient emailClient;
	private static final String DEFAULT_EMAIL = "no.email@free.fr";
	private static final Logger logger = SeleniumRobotLogger.getLogger(EmailAccount.class);
	
	/**
	 * The account should be created before any mail has been send because it will only return email received from the creation date
	 * @param email
	 * @param emailLogin
	 * @param emailPassword
	 */
	public EmailAccount(String email, String emailLogin, String emailPassword) {
		this.email = email;
		this.emailLogin = emailLogin;
		this.emailPassword = emailPassword;
		this.emailClient = null;
		creationDate = LocalDateTime.now();
	}

	public EmailAccount() {
		this(DEFAULT_EMAIL, "", "");
	}
	
	/**
	 * Set email in account without connecting to mailbox
	 * @param email
	 */
	public EmailAccount(String email) {
		this(email, "", "");
	}
	
	/**
	 * @return true if all account information are set
	 */
	public boolean canConnect() {
		return !(email.equals(DEFAULT_EMAIL) || emailLogin == null || emailLogin.isEmpty());
	}
	
	/**
	 * Create an email from json string
	 * format is {'email': <email>, 'login': <login>, 'password': <password>}
	 * @return
	 */
	public static EmailAccount fromJson(String jsonStr) {
		
		// dans le cas où la clé n'est pas trouvé dans le json, on renvoie null
		try {
			JSONObject json = new JSONObject(jsonStr);
			return new EmailAccount(json.getString("email"), json.getString("login"), json.getString("password"));
		} catch (JSONException e) {
			throw new ConfigurationException("email format must be {'email': <email>, 'login': <login>, 'password': <password>}");
		}
	}
	
	/**
	 * serialize email {'email': <email>, 'login': <login>, 'password': <password>}
	 * @return
	 */
	public String toJson() {
		return toJsonObj().toString();
	}
	
	public JSONObject toJsonObj() {
		JSONObject json = new JSONObject();
		json.put("email", email);
		json.put("login", emailLogin);
		json.put("password", emailPassword);
		
		return json;
	}
	
	/**
	 * Check email has been get
	 * 
	 * @param emailTitle	email title to get
	 * @param attachments	list of attachments in email
	 * 
	 * TODO: should be moved elsewhere
	 */
	public Email checkEmailPresence(String emailTitle, String[] attachments) {

		// is client configured
		getEmailClient();
		
		Email foundEmail = new Email();
		if (emailClient != null) {
			List<String> missingAttachments;
			try {
				missingAttachments = emailClient.checkMessagePresenceInLastMessages(emailTitle, attachments, foundEmail);
			} catch (Exception e) {
				logger.error("Could not get messages", e);
				missingAttachments = null;
			} 
			
			if (missingAttachments == null) {
				Assert.assertNotNull(missingAttachments, "Email '" + emailTitle + "' not found");
				
			} else if (!missingAttachments.isEmpty()) {
				Assert.assertTrue(missingAttachments.isEmpty(), "Email '" + emailTitle + "' found but attachments are missing: " + missingAttachments);
			} 
		} else {
			throw new ConfigurationException("Mail server client has not been configured");
		}
		return foundEmail;
	}
	
	/**
	 * configure mail account
	 * @param emailAccount		email account to use
	 * @return email client
	 * @throws ConfigurationException	when account information are missing
	 */
	public void configureEmailAccount() {
		if (!canConnect()) {
			throw new ConfigurationException("Email account cannot be used, we can't connect it");
		}
		
		logger.info("email " + getEmail() + " will be used to access server");
		
		emailClient = EmailClientSelector.routeEmail(EmailServer.fromJson(SeleniumTestsContextManager.getThreadContext().getConfiguration().get("mailServer").getValue()),
				getEmail(),
				getEmailLogin(),
				getEmailPassword());
		
		// we should only get last received emails
		if (emailClient != null) {
			emailClient.setSearchMode(SearchMode.BY_DATE);
			emailClient.setFromDate(creationDate);
		} 
	}
	
	/**
	 * @return 	email client. create it if it does not exist
	 */
	public EmailClient getEmailClient() {
		if (emailClient == null) {
			configureEmailAccount();
		}
		return emailClient;
	}
	
	// TODO: add reservation of email

	public String getEmail() {
		return email;
	}

	public String getEmailLogin() {
		return emailLogin;
	}

	public String getEmailPassword() {
		return emailPassword;
	}
}
