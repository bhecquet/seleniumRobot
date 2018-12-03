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
