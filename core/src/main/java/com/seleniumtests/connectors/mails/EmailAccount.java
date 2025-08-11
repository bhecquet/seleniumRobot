/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.mails;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.seleniumtests.customexception.ScenarioException;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

import com.seleniumtests.connectors.mails.EmailClientImpl.SearchMode;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class EmailAccount {

    private String email;
    private String emailLogin;
    private String emailPassword;
    private LocalDateTime creationDate;
    private EmailClient emailClient;
    private EmailServer emailServer;
    private static final String DEFAULT_EMAIL = "no.email@free.fr";
    private static final Logger logger = SeleniumRobotLogger.getLogger(EmailAccount.class);
 // Parameters exclusive to Exchange Online
 	private String tenantId;
 	private String clientId;
 	private String certificate;
 	private String certificatePrivateKey;
 	private String certificatePrivateKeyPassword;

    /**
     * The account should be created before any mail has been send because it will only return email received from the creation date
     *
     * @param email
     * @param emailLogin
     * @param emailPassword
     * @param emailServer   the mail server instance to connect to
     */
    public EmailAccount(String email, String emailLogin, String emailPassword, EmailServer emailServer) {
        this.email = email;
        this.emailLogin = emailLogin;
        this.emailPassword = emailPassword;
        this.emailClient = null;
        this.emailServer = emailServer;
        creationDate = LocalDateTime.now();
    }

    public EmailAccount(String email, String emailLogin, String emailPassword) {
        this(email, emailLogin, emailPassword, null);
    }

    public EmailAccount() {
        this(DEFAULT_EMAIL, "", "");
    }

    /**
	 * @param email
	 * @param tenantId
	 * @param clientId               given at your entra registration, might be called applicationId
	 * @param certificateFileContent        the content of your certificate file .pem, without the -----BEGIN CERTIFICATE----- and -----END CERTIFICATE----- lines
	 * @param certificatePrivateKeyFileContent  the content of your private key file .key, without the -----BEGIN PRIVATE KEY----- and -----END PRIVATE KEY----- lines
	 * @param certificatePrivateKeyPassword
	 */
	public EmailAccount(String email, String tenantId, String clientId, String certificateFileContent, String certificatePrivateKeyFileContent, String certificatePrivateKeyPassword, EmailServer server) {
		this.email = email;
		this.tenantId = tenantId;
		this.clientId = clientId;
		this.certificate = certificateFileContent;
		this.certificatePrivateKey = certificatePrivateKeyFileContent;
		this.certificatePrivateKeyPassword = certificatePrivateKeyPassword;
		this.emailServer = server;
	}
	
    
    /**
     * Set email in account without connecting to mailbox
     *
     * @param email
     */
    public EmailAccount(String email) {
        this(email, "", "");
    }

    /**
     * @return true if all account information are set
     */
    public boolean canConnect() {
    	if (emailServer.getType().equals(EmailServer.EmailServerTypes.EXCHANGE_ONLINE)) {
			return !(email.equals(DEFAULT_EMAIL) || certificate == null || certificatePrivateKey == null || certificatePrivateKeyPassword == null);
		} else {
			return !(email.equals(DEFAULT_EMAIL) || emailLogin == null || emailLogin.isEmpty());
		}
    }

    /**
     * Create an email from json string
     * format is {'email': <email>, 'login': <login>, 'password': <password>}
     *
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
     *
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
     * @param emailTitle  email title to get. This can be a regular expression as underlying used method is String.matches()
     * @param attachments list of attachments in email
     */
    public Email checkEmailPresence(String emailTitle, String[] attachments) {
        return checkEmailPresence(emailTitle, attachments, 90);

    }

    /**
     * Check email has been get
     *
     * @param emailTitle       email title to get. This can be a regular expression as underlying used method is String.matches()
     * @param attachments      list of attachments in email
     * @param timeoutInSeconds time to wait for expected message
     */
    public Email checkEmailPresence(String emailTitle, String[] attachments, int timeoutInSeconds) {

        // is client configured
        getEmailClient();

        Email foundEmail = new Email();
        if (emailClient != null) {
            List<String> missingAttachments;
            try {
                missingAttachments = emailClient.checkMessagePresenceInLastMessages(emailTitle, attachments, foundEmail, timeoutInSeconds);
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
     * Check email has been get by his a part or all of his content
     *
     * @param content          email content to get.
     * @param attachments      list of attachments in email
     * @param timeoutInSeconds time to wait for expected message
     */
    public Email checkEmailPresenceByBody(String content, String[] attachments, int timeoutInSeconds) throws Exception {

        // is client configured
        getEmailClient();

        Email foundEmail = new Email();
        if (emailClient != null) {
            List<String> missingAttachments;

            try {
                missingAttachments = emailClient.checkMessagePresenceInLastMessagesByBody(content, attachments, foundEmail, timeoutInSeconds);

                if (missingAttachments == null) {
                    Assert.assertNotNull(missingAttachments, "No Email found which contains :" + content + " in his content");
                } else if (!missingAttachments.isEmpty()) {
                    Assert.assertTrue(missingAttachments.isEmpty(), "Email with " + content + " content found but attachments are missing: " + missingAttachments);
                }
            } catch (ScenarioException e) {
                throw e;
            }
        } else {
            throw new ConfigurationException("Mail server client has not been configured");
        }
        return foundEmail;
    }

    /**
     * Sends a message
     *
     * @param to    list of recipients
     * @param title email title
     * @param body  content of the email
     * @throws Exception
     */
    public void sendMessage(List<String> to, String title, String body) throws Exception {
        sendMessage(to,title, body,new ArrayList<>());
    }
    public void sendMessage(List<String> to, String title, String body, List<File> attachments) throws Exception {

        getEmailClient();
        if (emailClient != null) {
            emailClient.sendMessage(to, title, body, attachments);
        } else {
            throw new ConfigurationException("Mail server client has not been configured");
        }
    }

    /**
     * configure mail account
     *
     * @return email client
     * @throws ConfigurationException when account information are missing
     */
    public void configureEmailAccount() {
        if (!canConnect()) {
            throw new ConfigurationException("Email account cannot be used, we can't connect it");
        }

        logger.info("email " + getEmail() + " will be used to access server");

        if (emailServer == null) {
            throw new ConfigurationException("email server has not been configured");
        }

        if (emailServer.getType().equals(EmailServer.EmailServerTypes.EXCHANGE_ONLINE)) {
			emailClient = EmailClientSelector.routeEmail(emailServer, clientId, tenantId, certificate, certificatePrivateKey, certificatePrivateKeyPassword, email);
		} else {
			emailClient = EmailClientSelector.routeEmail(emailServer,
					getEmail(),
					getEmailLogin(),
					getEmailPassword());
		}
        
        // we should only get last received emails
        if (emailClient != null) {
            emailClient.setSearchMode(SearchMode.BY_DATE);
            emailClient.setFromDate(creationDate);
        }
    }

    /**
     * @return email client. create it if it does not exist
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

    public EmailServer getEmailServer() {
        return emailServer;
    }

    public void setEmailServer(EmailServer emailServer) {
        this.emailServer = emailServer;
    }
}
