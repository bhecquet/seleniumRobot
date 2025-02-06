package com.seleniumtests.connectors.mails;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.graph.core.authentication.AzureIdentityAuthenticationProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody;
import jakarta.mail.MessagingException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ExchangeOnline extends EmailClientImpl {

    //public enum mailPart {
    //    SENDER,
    //    BODY,
    //    OBJECT
    //}
    /*if (searchWhere == mailPart.SENDER) {
        searchWhereFormat = "(sender/emailAddress/address)";
    } else if (searchWhere == mailPart.OBJECT) {
        searchWhereFormat = "subject";
    } else {
        searchWhereFormat = "(body/content)";
    }*/

    private String url = "";
    private String clientId;
    private String secret;
    private String tenantId;
    private String SCOPE = "Mail.Read Mail.Send";//à confirmer...
    //private String[] SCOPES; ?
    private ConfidentialClientApplication app;
    private String token;

    private ClientSecretCredential cred;
    private AzureIdentityAuthenticationProvider ap;

    /**
     *
     * @param url       ex: https://login.microsoftonline.com/Tenant_Info_Here/
     * @param clientId
     * @param secret
     * @param tenantId
     */
    public ExchangeOnline(String url, String clientId, String secret, String tenantId) {
        this.url = url;
        this.clientId = clientId;
        this.secret = secret;
        this.tenantId = tenantId;
        //call whatever method is best to directly authenticate the user
    }

    public void logInMicrosoftIdentity() throws Exception {
        app = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(secret)).authority(url).build();
        token = app.acquireToken(ClientCredentialParameters.builder(Collections.singleton(SCOPE)).build()).get().accessToken();
//        Capilo-tracté
//        IAuthenticationResult authentResult = app.acquireToken(ClientCredentialParameters.builder(Collections.singleton(SCOPE)).build()).get();
//        token = authentResult.accessToken();
//        OffsetDateTime odt = authentResult.expiresOnDate().toInstant().atOffset(ZoneOffset.UTC);
//        AccessToken at = new AccessToken(token, odt);
//        final GraphServiceClient graphClient = new GraphServiceClient((TokenCredential) at, SCOPE);
    }

    /**
     * if microsoft-graph-auth, try:
     * ClientCredentialProvider authProvider = new ClientCredentialProvider(CLIENT_ID, SCOPES, CLIENT_SECRET, TENANT_GUID, NATIONAL_CLOUD);
     * new GraphServiceClient(authProvider);
     */
    public void logInGraphCredentials() throws Exception {
        final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId).tenantId(tenantId).clientSecret(secret).build();
        final AzureIdentityAuthenticationProvider authenticationProvider =
                new AzureIdentityAuthenticationProvider(credential, null, SCOPE);

        if (null == SCOPE || null == credential) {
            throw new Exception("Unexpected error");
        }

        cred = credential;
        ap = authenticationProvider;

    }

    /**
     * Retourne une liste d'emails selon les critères de recherche fournis
     *
     * @param searchFolder le nom exact du dossier où chercher, s'il n'existe pas on cherche dans tous les messages
     * @param searchWhen   la date de réception à partir de laquelle on recherche les mails
     * @param searchValue  la valeur à rechercher dans l'élément fourni dans le paramètre searchWhere
     * @param isRead       true: on ne récupère que les mails lus ; false: on ne récupère que les mails non lus
     * @return une liste de mails
     * @throws Exception par défaut, à revoir
     */
    public List<Email> readMail(String searchFolder, String searchWhen, String searchValue, boolean isRead) throws Exception {
        final GraphServiceClient graphClient = new GraphServiceClient(ap);
        String searchFolderId = graphClient.me().mailFolders().get(folderReqConfig -> folderReqConfig.queryParameters.filter = String.format("displayName eq '%s'", searchFolder)).getValue().get(0).getId();

        if (searchFolderId == null) {
            return new ArrayList<>();
        }

        MessageCollectionResponse tempRes = graphClient.me().mailFolders().byMailFolderId(searchFolderId).messages().get(requestConfiguration -> {
            requestConfiguration.queryParameters.select = new String[]{"from", "sender", "subject", "body", "receivedDateTime"};
            requestConfiguration.queryParameters.filter = String.format("receivedDateTime ge %s and isRead eq %b", searchWhen, isRead);
            requestConfiguration.queryParameters.count = true;
            requestConfiguration.queryParameters.search = searchValue;
        });
        List<Email> emails = new ArrayList<>();
        if (tempRes != null) {
            for (Message message : Objects.requireNonNull(tempRes.getValue(), "Messages value is null")) {
                List<String> attachments = new ArrayList<>();
                if (message.getHasAttachments()) {
                    for (Attachment attachment : Objects.requireNonNull(message.getAttachments())) {
                        attachments.add(attachment.getName());
                    }
                }
                Email tempMail = new Email(message.getSubject(), message.getBody().getContent(), message.getSender().toString(), getLocalDateTimeFromOffset(message.getReceivedDateTime()), attachments);
                emails.add(tempMail);
            }
        }
        return emails;
    }

    public Email readLastMail(String folderName) throws Exception {
        final GraphServiceClient graphClient = new GraphServiceClient(ap);
        //id/displayName https://learn.microsoft.com/en-us/graph/api/resources/mailfolder?view=graph-rest-1.0
        //Peut-être plus propre comme ça mais faut quand même retrouver l'id du folder......
        //MailFolderCollectionResponse test = graphClient.me().mailFolders().get(reqConfig -> reqConfig.queryParameters.filter = String.format("displayName eq %s", folderName));
        //String testid = test.getValue().getFirst().getId();
        /*MessageCollectionResponse tempRes = graphClient.me().mailFolders().byMailFolderId("id").messages().get(requestConfiguration -> {
            requestConfiguration.queryParameters.top = 1;
        });*/
        MailFolderCollectionResponse folderByName = graphClient.me().mailFolders().get(reqConfig -> reqConfig.queryParameters.filter = String.format("displayName eq %s", folderName));
        Message message;
        List<String> attachments = new ArrayList<>();
        if (folderByName == null) {
            message = graphClient.me().mailFolders().byMailFolderId("inbox").messages().get().getValue().get(0);

        } else {
            message = folderByName.getValue().get(0).getMessages().get(0);
        }
        if (message.getHasAttachments()) {
            for (Attachment attachment : Objects.requireNonNull(message.getAttachments())) {
                attachments.add(attachment.getName());
            }
        }
        return new Email(message.getSubject(), message.getBody().getContent(), message.getSender().toString(), getLocalDateTimeFromOffset(message.getReceivedDateTime()), attachments);
    }

    public LocalDateTime getLocalDateTimeFromOffset(OffsetDateTime off) {
        LocalDateTime localDatetime = LocalDateTime.now();
        if (off != null) {
            ZonedDateTime zoned = off.atZoneSameInstant(ZoneId.systemDefault());
            localDatetime = zoned.toLocalDateTime();
        }
        return localDatetime;
    }

    @Override
    public List<Email> getEmails(String folderName, int firstMessageIndex, LocalDateTime firstMessageTime) throws Exception {
        return List.of();
    }

    @Override
    public void disconnect() throws MessagingException {

    }

    @Override
    public void setLastMessageIndex() throws Exception {

    }

    @Override
    public void setLastMessageIndex(int messageIndex) {
        lastMessageIndex = messageIndex;
    }

    @Override
    public int getLastMessageIndex() {
        return lastMessageIndex;
    }

    @Override
    public void sendMessage(List<String> to, String title, String body) throws Exception {
        sendMessage(to, title, body, new ArrayList<>());
    }

    @Override
    public void sendMessage(List<String> to, String title, String body, List<File> attachmentFiles) throws Exception {
        if (cred != null) {
            final GraphServiceClient graphClient = new GraphServiceClient(cred, SCOPE);
            Message message = new Message();
            //TO
            List<Recipient> listr = new ArrayList<>();
            for (String recipient : to) {
                EmailAddress ea = new EmailAddress();
                ea.setAddress(recipient);
                Recipient r = new Recipient();
                r.setEmailAddress(ea);
                listr.add(r);
            }
            message.setToRecipients(listr);
            message.setSubject(title);
            //BODY
            ItemBody b = new ItemBody();
            b.setContent(body);
            message.setBody(b);
            //ATTACHMENT
            if (!attachmentFiles.isEmpty()) {
                List<Attachment> attachments = new ArrayList<>();
                for (File pj : attachmentFiles) {
                    FileAttachment fa = new FileAttachment();
                    fa.setName(pj.getName());
                    fa.setContentBytes(FileUtils.readFileToByteArray(pj));
                    attachments.add(fa);
                }
                message.setAttachments(attachments);
            }
            //SEND
            SendMailPostRequestBody smprb = new SendMailPostRequestBody();
            smprb.setMessage(message);
            graphClient.me().sendMail().post(smprb);
        } else {
            throw new Exception("Not authorized");
        }
    }
}
