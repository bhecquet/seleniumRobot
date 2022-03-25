/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.connectors.remote;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import com.seleniumtests.util.logging.SeleniumRobotLogger;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class Ssh {
	

	private static final Logger logger = SeleniumRobotLogger.getLogger(Ssh.class);
	
	private enum ConnectMethod {
		PASSWORD, KEY;
	}
	
	private String host;
	private String user;
	private String password;
	private String keyFile;
	protected SSHClient ssh;
	private ConnectMethod connectMethod;
	
	/**
	 * Configure SSH connection
	 * @param host				host to connect to
	 * @param user				user
	 * @param password			password
	 * @param serverFingerPrint server identity. may be null if already stored in known_hosts file or if checkIdentity is false
	 * @param checkIdentity		if true, server identity will be checked
	 * @throws IOException
	 */
	public Ssh(String host, String user, String password, String serverFingerPrint, Boolean checkIdentity) throws IOException {
		this.user = user;
		this.password = password;
		this.host = host;
		connectMethod = ConnectMethod.PASSWORD;
		configureConnection(serverFingerPrint, checkIdentity);
	}	

	/**
	 * Configure SSH connection with key file
	 * @param host				host to connect to
	 * @param user				user
	 * @param keyFile			key file to use to connect to server
	 * @param serverFingerPrint server identity. may be null if already stored in known_hosts file or if checkIdentity is false
	 * @param checkIdentity		if true, server identity will be checked
	 * @throws IOException 
	 */
	public Ssh(String host, String user, File keyFile, String serverFingerPrint, Boolean checkIdentity) throws IOException {
		this.host = host;
		this.user = user;
		this.keyFile = keyFile.getAbsolutePath();
		connectMethod = ConnectMethod.KEY;
		configureConnection(serverFingerPrint, checkIdentity);		
	}
	
	private void configureConnection(String serverFingerPrint, Boolean checkIdentity) throws IOException {
		createKnownHosts();
		ssh = new SSHClient();
		ssh.loadKnownHosts();
	
		if (!Boolean.TRUE.equals(checkIdentity)) {
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
		} else if (serverFingerPrint != null) {
			ssh.addHostKeyVerifier(serverFingerPrint);
		} else {
			File khFile = new File(OpenSSHKnownHosts.detectSSHDir(), "known_hosts");
			ssh.addHostKeyVerifier(new OpenSSHKnownHosts(khFile));
		}
	}
	
	public void connect() throws IOException {
		ssh.connect(host);
		
		if (connectMethod.equals(ConnectMethod.PASSWORD)) {
			ssh.authPassword(user, password);
		} else {
			ssh.authPublickey(user, keyFile);
		}
	}
	
	public void disconnect() throws IOException {
		ssh.disconnect();
	}
	
	/**
	 * create known_hosts file
	 * @throws IOException 
	 */
	private void createKnownHosts() throws IOException {
		Paths.get(System.getProperty("user.home"), ".ssh").toFile().mkdirs();
		if (!(Paths.get(System.getProperty("user.home"), ".ssh", "known_hosts").toFile().createNewFile())) {
			logger.info("known_hosts file has not bee created");
		}
	}
	
	/**
	 * Execute a command on remote host
	 * @param command		command to execute
	 * @param wait			do we wait for command termination
	 * @return				output
	 * @throws IOException
	 */
	public String executeCommand(String command, Integer wait) throws IOException {
		Session session = ssh.startSession();
		String result = "NO_RESULT";
        try {
        	Command cmd = session.exec(command);
        	result = IOUtils.readFully(cmd.getInputStream()).toString();
        	cmd.join(wait, TimeUnit.SECONDS);
        } finally {
        	session.close();
        }
        return result;
	}
}
