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
package com.seleniumtests.it.driver.support.server;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Expose one web resource stored in src/test/resources on localhost
 * @author behe
 *
 */
public class WebServer {

	private Server fileServer;
	private HttpHost serverHost;
	private Map<String, String> pageMapping;
	private String hostIp;
	
	public WebServer(String hostIp, Map<String, String> pageMapping) {
		this.hostIp = hostIp;
		this.pageMapping = pageMapping;
	}

	protected Server startServerForServlet() throws Exception {
		return startServerForServlet(0);
	}
	protected Server startServerForServlet(int port) throws Exception {
        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        for (Entry<String, String> entry: pageMapping.entrySet()) {
        	context.addServlet(new ServletHolder(new PageServlet(entry.getKey())), entry.getValue());
        }
        server.start();

        return server;
    }
	
	public void expose() throws Exception {
		expose(0);
	}
	public void expose(int port) throws Exception {
		fileServer = startServerForServlet(port);
        serverHost = new HttpHost(hostIp, ((ServerConnector)fileServer.getConnectors()[0]).getLocalPort());
	}
	
	public void stop() throws Exception {
		if (fileServer != null) {
			fileServer.stop();
		}
	}

	public HttpHost getServerHost() {
		return serverHost;
	}
	
	
}
