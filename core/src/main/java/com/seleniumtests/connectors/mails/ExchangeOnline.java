package com.seleniumtests.connectors.mails;

import com.microsoft.aad.msal4j.*;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import jakarta.mail.MessagingException;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

import java.io.*;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.*;

public class ExchangeOnline extends EmailClientImpl {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(ExchangeOnline.class);
	private static final Set<String> SCOPE = Collections.singleton("https://graph.microsoft.com/.default");
	private static final String GRAPH_URL = "https://graph.microsoft.com/v1.0/users/";
	private static final String AUTH_URL = "https://login.microsoftonline.com/";
	private static String baseUrl;
	protected static final String TMP_PRIVATE_KEY_FILE_PATH = "." + File.separator + "tmpPrivateKeyFilePath.key";
	
	/**
	 * @param tenantId
	 * @param clientId                  might be called applicationId
	 * @param certificateFileContent
	 * @param certificatePrivateKeyFileContent
	 * @param certificatePrivateKeyPassword
	 * @param userEmail                 the email address you want to read or send mail from (abc.edf@ghi.com)
	 * @throws Exception
	 */
	public ExchangeOnline(String tenantId, String clientId, String certificateFileContent, String certificatePrivateKeyFileContent, String certificatePrivateKeyPassword, String userEmail) throws Exception {
		baseUrl = GRAPH_URL + userEmail + "/";
		
		IAuthenticationResult tokenProvider = acquireToken(certificateFileContent, certificatePrivateKeyFileContent, certificatePrivateKeyPassword, tenantId, clientId);
		String token = tokenProvider.accessToken();
		
		logger.info(token);
		
		Unirest.config().addDefaultHeader("Authorization", "Bearer " + token);
	}
	
	// ************************************************************
	// *********************** GET TOKEN **************************
	// ************************************************************
	private static IAuthenticationResult acquireToken(String certFileContent, String certPrivateKeyFileContent, String certPrivateKeyPassword, String tenantId, String clientId) throws Exception {
		
		PrivateKey certPrivateKey = readBouncyPrivateKey(certPrivateKeyFileContent, certPrivateKeyPassword);
		
		X509Certificate cert = getCertObject(certFileContent);
		
		// Connection to API Graph
		IClientCredential credential = ClientCredentialFactory.createFromCertificate(certPrivateKey, cert);
		ConfidentialClientApplication cca = ConfidentialClientApplication.builder(clientId, credential)
				.authority(AUTH_URL + tenantId).build();
		
		// Add parameters
		ClientCredentialParameters parameters = ClientCredentialParameters.builder(SCOPE).build();
		
		// Call for the token
		return cca.acquireToken(parameters).join();
	}
	
	private static X509Certificate getCertObject(String cert) throws CertificateException {
		String cleanedCert = cert.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "")
				.replaceAll("\\s", "");
		
		byte[] byteData = Base64.getDecoder().decode(cleanedCert);
		
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		
		return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(byteData));
	}
	
	private static PrivateKey readBouncyPrivateKey(String keyFileContent, String keyFilePassword) throws Exception {
		logger.info(TMP_PRIVATE_KEY_FILE_PATH);
		File keyFile = new File(TMP_PRIVATE_KEY_FILE_PATH);
		if (keyFile.createNewFile()) {
			FileWriter fw = new FileWriter(TMP_PRIVATE_KEY_FILE_PATH);
			fw.write("-----BEGIN ENCRYPTED PRIVATE KEY-----\n");
			fw.write(keyFileContent);
			fw.write("\n-----END ENCRYPTED PRIVATE KEY-----");
			fw.close();
		}
		String keyFilePath = keyFile.getPath();
		
		// 1. Add Bouncy Castle as provider
		Security.addProvider(new BouncyCastleProvider());
		
		// 2. Open private key file (PEM format)
		PEMParser pemParser = new PEMParser(new FileReader(keyFilePath));
		Object object = pemParser.readObject();
		pemParser.close();
		
		// 3. Prepare the Bouncy Castle converter
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
		PrivateKey privateKey;
		
		// 4. Vérifier si la clé est encryptée ou non et déchiffrer le cas échéant
		if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
			// La clé est encryptée : utiliser le mot de passe pour déchiffrer
			PKCS8EncryptedPrivateKeyInfo encryptedInfo = (PKCS8EncryptedPrivateKeyInfo) object;
			InputDecryptorProvider decryptorProvider = new JceOpenSSLPKCS8DecryptorProviderBuilder()
					.build(keyFilePassword.toCharArray());
			privateKey = converter.getPrivateKey(encryptedInfo.decryptPrivateKeyInfo(decryptorProvider));
		} else {
			throw new IllegalArgumentException("Format de clé inconnu ou incompatible");
		}
		keyFile.delete();
		return privateKey;
		
	}

// ************************************************************
// ********************** READ MAILS **************************
// ************************************************************
	
	/**
	 * @param parameters parameters is a map of options to filter the mails to read, the parameters are optional and can be combined in any way :<br/>
	 *                   - folder (String) : the folder to read mails from<br/>
	 *                   - unread (boolean) : true to read unread mails only, false to read readen mails only<br/>
	 *                   - fromName (String) : the sender's displayed name of the mails to read<br/>
	 *                   - fromMail (String) : the sender's mail address of the mails to read<br/>
	 *                   - subject (String) : the subject of the mails to read<br/>
	 *                   - text (String) : the text to search in the mails to read<br/>
	 *                   - attach (boolean) : true to read mails with attachments only, false to read mails without attachments only<br/>
	 *                   - last (true) : get only the last mail matching the given parameters<br/>
	 *                   - select (String) : the fields to select in the returned mails (ex: subject,body,from,toRecipients,receivedDateTime)
	 * @return a list of mails matching the given parameters in JSON format
	 */
	public HttpResponse<JsonNode> readMails(Map<String, Object> parameters) {
		StringBuilder requestUrl = new StringBuilder();
		requestUrl.append(baseUrl);
		// Search mails in a specific folder
		if (parameters.containsKey("folder")) {
			requestUrl.append(String.format("mailFolders('%1$s')/", parameters.get("folder")));
		}
		requestUrl.append("messages?&filter=");
		
		// Search for read/unread mails only
		if (parameters.containsKey("unread")) {
			requestUrl.append(String.format("(isRead) eq %1$b", parameters.get("unread")));
		}
		// Search for specific sender
		if (parameters.containsKey("fromName")) {
			requestUrl.append(String.format(" and (from/emailAddress/name) eq '%1$s'", parameters.get("fromName")));
		}
		if (parameters.containsKey("fromMail")) {
			requestUrl.append(String.format(" and (from/emailAddress/address) eq '%1$s'", parameters.get("fromMail")));
		}
		// Search for specific subject
		if (parameters.containsKey("subject")) {
			requestUrl.append(String.format(" and contains(subject, '%1$s')", parameters.get("subject")));
		}
		// Search for mails with or without attachment
		if (parameters.containsKey("attach")) {
			requestUrl.append(String.format(" and (hasAttachments) eq %1$b", parameters.get("attach")));
		}
		// Search for mails from a specific date
		if (parameters.containsKey("fromDate")) {
			requestUrl.append(String.format(" and receivedDateTime ge %1$sZ", parameters.get("fromDate")));
		}
		// Search for specific text in messages
		if (parameters.containsKey("search")) {
			requestUrl.append(String.format("&$search='%1$s'", parameters.get("search")));
		}
		// Search for the last mail (matching the parameters if given)
		if (parameters.containsKey("last")) {
			requestUrl.append("&$top=1&$orderby=receivedDateTime desc");
		}
		
		// Select only the required fields
		if (parameters.containsKey("select")) {
			requestUrl.append(String.format("&$select=%1$s", parameters.get("select")));
		}
		
		return Unirest.get(requestUrl.toString().replace("&filter= and ", "&filter=")).asJson();
	}
	
	/**
	 * @param mailId : the ID of the mail you want to retrieve the attachments from. You can get it from the readMails function.
	 * @return the list of attachments of the mail as a JSON object. Contains only id, name, size and contentType to
	 * avoid downloading the whole content of every attachment, but giving you the necessary information to identify the attachment you want to download.
	 * To get the content of an attachment, use the getAttachmentContent method.
	 */
	public HttpResponse<JsonNode> getAttachments(String mailId) {
		String requestUrl = baseUrl +
				String.format("messages/%1$s/attachments?", mailId) +
				"$select=id,name,size,contentType";
		
		return Unirest.get(requestUrl).asJson();
	}
	
	/**
	 * @param mailId       : the ID of the mail you want to retrieve the attachments from. You can get it from the readMails method.
	 * @param attachmentId : the ID of the attachment you want to retrieve. You can get it from the getAttachments method.
	 * @return the content of the attachment as a JSON object.
	 */
	public HttpResponse<JsonNode> getAttachmentContent(String mailId, String attachmentId) {
		String requestUrl = baseUrl +
				String.format("messages/%1$s/attachments/%2$s", mailId, attachmentId);
		
		return Unirest.get(requestUrl).asJson();
	}

// ************************************************************
// ********************** SEND MAILS **************************
// ************************************************************
	
	/**
	 * @param subject     : the subject of the mail
	 * @param bodyType    : the type of the body (HTML or TEXT)
	 * @param bodyContent : the content of the body
	 * @param to          : the recipient of the mail, you can provide multiple recipients by separating them with a comma
	 * @return TBD
	 * @throws IOException : should be managed inside the function
	 */
	public HttpResponse<JsonNode> sendMail(String subject, String bodyType, String bodyContent, List<String> to) throws IOException {
		return sendMail(subject, bodyType, bodyContent, to, new ArrayList<>());
	}
	
	/**
	 * @param subject     : the subject of the mail
	 * @param bodyType    : the type of the body (HTML or TEXT)
	 * @param bodyContent : the content of the body
	 * @param to          : list of the recipient of the mail
	 * @param files       : list of the files you want to attach to the mail
	 * @return TBD
	 * @throws IOException : should be managed inside the function
	 */
	public HttpResponse<JsonNode> sendMail(String subject, String bodyType, String bodyContent, List<String> to, List<File> files) throws IOException {
		JSONArray recipientsNode = new JSONArray();
		for (String mail : to) {
			JSONObject addressNode = new JSONObject().put("address", mail);
			JSONObject recipientNode = new JSONObject().put("emailAddress", addressNode);
			recipientsNode.put(recipientNode);
		}
		JSONObject bodyNode = new JSONObject().put("contentType", bodyType).put("content", bodyContent);
		
		JSONObject messageNode = new JSONObject().put("subject", subject).put("body", bodyNode).put("toRecipients", recipientsNode);
		if (!files.isEmpty()) {
			JSONArray attachmentsNode = new JSONArray();
			for (File fileToAttach : files) {
				JSONObject attachment = new JSONObject();
				attachment.put("@odata.type", "#microsoft.graph.fileAttachment");
				attachment.put("name", fileToAttach.getName());
				attachment.put("contentType", Files.probeContentType(fileToAttach.toPath()));
				attachment.put("contentBytes", Base64.getEncoder().encodeToString(Files.readAllBytes(fileToAttach.toPath())));
				attachmentsNode.put(attachment);
			}
			messageNode.put("attachments", attachmentsNode);
		}
		
		JSONObject requestBody = new JSONObject().put("message", messageNode).put("saveToSendItems", "false");
		
		String requestUrl = baseUrl + "sendMail";
		logger.info(requestBody);
		logger.info(requestUrl);
		return Unirest.post(requestUrl).header("Content-Type", "application/json").body(requestBody).asJson();
	}

// ************************************************************
// *********************** OVERRIDES **************************
// ************************************************************
	
	/**
	 * Return the id of existing folder in the mailbox
	 *
	 * @param folderName
	 * @return id of the folder
	 * @throws Exception
	 */
	private String findFolder(String folderName) throws Exception {
		
		String requestUrl = baseUrl + "mailFolders?$select=id,displayName";
		HttpResponse<JsonNode> folderResponse = Unirest.get(requestUrl).asJson();
		JSONArray jsFolders = folderResponse.getBody().getObject().getJSONArray("value");
		HashMap<String, String> folders = new HashMap<>();
		for (Object jsFolder : jsFolders) {
			JSONObject jsFolderObj = (JSONObject) jsFolder;
			folders.put(jsFolderObj.getString("displayName"), jsFolderObj.getString("id"));
		}
		if (folders.containsKey(folderName)) {
			return folders.get(folderName);
		} else {
			throw new ConfigurationException("folder " + folderName + " does not exist for this account");
		}
	}
	
	@Override
	public List<Email> getEmails(String folderName, int firstMessageIndex, LocalDateTime firstMessageTime) throws Exception {
		List<Email> result = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		try {
			String folderId = findFolder(folderName);
			params.put("folder", folderId);
			params.put("fromDate", firstMessageTime);
			params.put("select", "subject,body,from,receivedDateTime,hasAttachments");
			HttpResponse<JsonNode> readMailResult = readMails(params);
			JSONArray mails = readMailResult.getBody().getObject().getJSONArray("value");
			if (!mails.isEmpty()) {
				for (Object mail : mails) {
					//String subject, String content, String sender, LocalDateTime datetime, List<String> attachment
					JSONObject jsmail = (JSONObject) mail;
					String mailId = jsmail.getString("id");
					List<String> attachments = null;
					if (jsmail.getBoolean("hasAttachments")) {
						attachments = new ArrayList<>();
						JSONArray jsAttach = getAttachments(mailId).getBody().getObject().getJSONArray("value");
						for (Object attachment : jsAttach) {
							JSONObject jsatt = (JSONObject) attachment;
							attachments.add(jsatt.getString("name"));
						}
					}
					String time = jsmail.getString("receivedDateTime");
					String timeNoZ = time.substring(0, time.length() - 1);
					Email toto = new Email(jsmail.getString("subject"), jsmail.getJSONObject("body").getString("content"), jsmail.getJSONObject("from").getJSONObject("emailAddress").getString("address"), LocalDateTime.parse(timeNoZ), attachments);
					result.add(toto);
				}
			} else {
				throw new Exception("Retrieved mail list is empty.");
			}
		} catch (Exception e) {
			logger.info("Error while reading mails from Exchange Online : " + e.getMessage());
		}
		return result;
	}
	
	@Override
	public void disconnect() throws MessagingException {
		// Exchange Online revolves around API calls, not a connection
	}
	
	@Override
	public void setLastMessageIndex() throws Exception {
		// Exchange Online do not set index on the messages
	}
	
	@Override
	public void setLastMessageIndex(int messageIndex) {
		// Exchange Online do not set index on the messages
	}
	
	@Override
	public int getLastMessageIndex() {
		// Exchange Online do not set index on the messages
		return 0;
	}
	
	@Override
	public void sendMessage(List<String> to, String title, String body) throws Exception {
		sendMail(title, "Text", body, to);
	}
	
	@Override
	public void sendMessage(List<String> to, String title, String body, List<File> attachments) throws Exception {
		sendMail(title, "Text", body, to, attachments);
	}
}
