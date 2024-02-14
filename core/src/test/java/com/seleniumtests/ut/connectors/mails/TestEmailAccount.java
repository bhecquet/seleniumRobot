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
package com.seleniumtests.ut.connectors.mails;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.mails.*;
import com.seleniumtests.customexception.ScenarioException;
import jakarta.mail.MessagingException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.customexception.ConfigurationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

public class TestEmailAccount extends MockitoTest {

    private String serverUrl = "";
    private String emailAddress = "";
    private String login = "";
    private String password = "";

    private EmailServer server = new EmailServer(serverUrl, EmailServer.EmailServerTypes.EXCHANGE, "");

    @Mock
    private ImapClient emailClientMock;

    private MockedStatic mockedEmailSelector;

    @BeforeMethod(groups = {"ut"})
    public void init() throws Exception {

        mockedEmailSelector = mockStatic(EmailClientSelector.class);
        when(EmailClientSelector.routeEmail(any(EmailServer.class), anyString(), anyString(), anyString())).thenReturn(emailClientMock);
        when(emailClientMock.getEmail(anyString(), anyList())).thenCallRealMethod();
        when(emailClientMock.getEmail(anyString(), any(String[].class))).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), any(String[].class))).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), anyList())).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), any(String[].class), any(Email.class))).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), anyList(), any(Email.class))).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), any(String[].class), any(Email.class), anyInt())).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessages(anyString(), anyList(), any(Email.class), anyInt())).thenCallRealMethod();

        when(emailClientMock.checkMessagePresenceInLastMessagesByBody(anyString(), any(String[].class), any(Email.class), anyInt())).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessagesByBody(anyString(), anyList(), any(Email.class), anyInt())).thenCallRealMethod();
        when(emailClientMock.checkMessagePresenceInLastMessagesByBody(anyString(), any(String[].class), any(Email.class))).thenCallRealMethod();
        when(emailClientMock.getEmailsByContent(anyString())).thenCallRealMethod();

        when(emailClientMock.getLastEmails(nullable(String.class))).thenCallRealMethod();
        when(emailClientMock.getLastEmails()).thenCallRealMethod();
        when(emailClientMock.getEmails(anyString())).thenCallRealMethod();
        when(emailClientMock.getEmails(anyInt())).thenCallRealMethod();
        when(emailClientMock.getEmails(nullable(String.class), anyInt())).thenCallRealMethod();
        when(emailClientMock.getEmails(nullable(String.class), nullable(LocalDateTime.class))).thenCallRealMethod();
        when(emailClientMock.getEmails(nullable(LocalDateTime.class))).thenCallRealMethod();
        when(emailClientMock.getLastMessageIndex()).thenReturn(1);
        when(emailClientMock.isTestMode()).thenReturn(true);
    }

    @AfterMethod(groups={"ut"}, alwaysRun = true)
    private void closeMocks() {
        mockedEmailSelector.close();
    }


    @Test(groups = {"ut"})
    public void testCantConnectWithoutInfo() {
        EmailAccount account = new EmailAccount();
        Assert.assertFalse(account.canConnect(), "We should not be able to connect to default account");
    }

    @Test(groups = {"ut"})
    public void testCantConnectWithOnlyAddress() {
        EmailAccount account = new EmailAccount("email@free.fr", null, null);
        Assert.assertFalse(account.canConnect(), "We should not be able to connect to default account");
    }

    @Test(groups = {"ut"})
    public void testCantConnectWithDefaultAddress() {
        EmailAccount account = new EmailAccount("no.email@free.fr", "no.email@free.fr", "aaa");
        Assert.assertFalse(account.canConnect(), "We should not be able to connect to default account");
    }

    @Test(groups = {"ut"})
    public void testCanConnect() {
        EmailAccount account = new EmailAccount("email@free.fr", "email@free.fr", "aaa");
        Assert.assertTrue(account.canConnect(), "We should be able to connect");
    }

    @Test(groups = {"ut"})
    public void testFromJson() {
        EmailAccount account = EmailAccount.fromJson("{'email': 'someone@company.com', 'login': \"someone\", 'password': 'someone'}");
        Assert.assertEquals(account.getEmail(), "someone@company.com");
        Assert.assertEquals(account.getEmailLogin(), "someone");
        Assert.assertEquals(account.getEmailPassword(), "someone");
    }

    @Test(groups = {"ut"}, expectedExceptions = ConfigurationException.class)
    public void testFromJson2() {
        EmailAccount.fromJson("{'email': 'someone@company.com', 'password': 'someone'}");
    }

    @Test(groups = {"ut"})
    public void testToJson() {
        EmailAccount account = new EmailAccount("someone@company.com", "someone", "someone");
        String jsonString = account.toJson();
        Assert.assertTrue(jsonString.contains("\"email\":\"someone@company.com\""));
        Assert.assertTrue(jsonString.contains("\"login\":\"someone\""));
        Assert.assertTrue(jsonString.contains("\"password\":\"someone\""));
    }

    @Test
    public void testConfigureEmailAccount() {
        EmailAccount account = new EmailAccount("email@free.fr", "email@free.fr", "aaa", server);
        account.configureEmailAccount();
        Assert.assertEquals(account.getEmailClient(), emailClientMock);
    }

    @Test(groups = {"ut"})
    public void testCheckEmailPresence() throws IOException, MessagingException {
        EmailAccount account = new EmailAccount("email@free.fr", "email@free.fr", "aaa", server);
        List<Email> emails = new ArrayList<>();
        emails.add(new Email("Jellyfish", "stinging", "oursindemer@company.com", LocalDateTime.now(), Arrays.asList(new String[]{"medusa.png"})));
        EmailClientSelector.routeEmail(server, emailAddress, login, password);
        when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
        Email emailFound = account.checkEmailPresence("Jellyfish", new String[]{"medusa.png"}, 5);

        Assert.assertEquals(emailFound.getSubject(), "Jellyfish");
    }

    @Test(groups = {"ut"})
    public void testCheckEmailPresenceByBodyWithManyWords() throws Exception {
        EmailAccount account = new EmailAccount("email@free.fr", "email@free.fr", "aaa", server);
        List<Email> emails = new ArrayList<>();
        emails.add(new Email("Jellyfish", "Lorem ipsum dolor sit amet,", "oursindemer@company.com", LocalDateTime.now(), Arrays.asList(new String[]{"medusa.png"})));
        EmailClientSelector.routeEmail(server, emailAddress, login, password);
        when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);
        Email emailFound = account.checkEmailPresenceByBody("dolor", new String[]{"medusa.png"}, 5);

        Assert.assertEquals(emailFound.getSubject(), "Jellyfish");
    }

    @Test(groups = {"ut"}, expectedExceptions = ScenarioException.class)
    public void testCheckEmailPresenceByBodyWithoutSearchString() throws Exception {
        EmailAccount account = new EmailAccount("email@free.fr", "email@free.fr", "aaa", server);
        List<Email> emails = new ArrayList<>();
        emails.add(new Email("Jellyfish", "Lorem ipsum dolor sit amet,", "oursindemer@company.com", LocalDateTime.now(), Arrays.asList(new String[]{"medusa.png"})));
        EmailClientSelector.routeEmail(server, emailAddress, login, password);
        when(emailClientMock.getEmails(nullable(String.class), eq(1), nullable(LocalDateTime.class))).thenReturn(emails);

        account.checkEmailPresenceByBody("", new String[]{"medusa.png"}, 5);
    }

    @Test(groups = {"ut"}, expectedExceptions = ScenarioException.class)
    public void testCheckEmailPresenceByBodyNullContent() throws Exception {
        EmailAccount account = new EmailAccount("email@free.fr", "email@free.fr", "aaa", server);
        EmailClientSelector.routeEmail(server, emailAddress, login, password);

        doThrow(new ScenarioException("content can't be null"))
                .when(emailClientMock)
                .checkMessagePresenceInLastMessagesByBody(nullable(String.class), any(String[].class), any(Email.class), anyInt());

        account.checkEmailPresenceByBody(null, new String[]{"medusa.png"}, 5);
    }
}
