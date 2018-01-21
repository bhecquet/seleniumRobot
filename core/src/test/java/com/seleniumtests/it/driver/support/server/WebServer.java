package com.seleniumtests.it.driver.support.server;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.apache.http.HttpHost;
import org.seleniumhq.jetty9.server.Server;
import org.seleniumhq.jetty9.server.ServerConnector;
import org.seleniumhq.jetty9.servlet.ServletContextHandler;
import org.seleniumhq.jetty9.servlet.ServletHolder;

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
        Server server = new Server(0);

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
		fileServer = startServerForServlet();
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
