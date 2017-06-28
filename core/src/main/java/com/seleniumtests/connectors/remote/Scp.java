package com.seleniumtests.connectors.remote;

import java.io.File;
import java.io.IOException;

import net.schmizz.sshj.xfer.FileSystemFile;

public class Scp extends Ssh {
	
	public Scp(String host, String user, String password, String serverFingerPrint, Boolean checkIdentity) throws IOException {
		super(host, user, password, serverFingerPrint, checkIdentity);
	}
	
	public Scp(String host, String user, File keyFile, String serverFingerPrint, Boolean checkIdentity) throws IOException {
		super(host, user, keyFile, serverFingerPrint, checkIdentity);
	}
	
	public void transfertFile(String remoteFile, File localFile) throws IOException {
		ssh.newSCPFileTransfer().download(remoteFile, new FileSystemFile(localFile));
	}
	
}
