package com.seleniumtests.noreg.support;

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
	private String resourceName;
	
	public WebServer(String resourceName) {
		this.resourceName = resourceName;
	}

	protected Server startServerForServlet(HttpServlet servlet, String path) throws Exception {
        Server server = new Server(0);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(servlet), path);
        server.start();

        return server;
    }
	
	public void expose() throws Exception {
		fileServer = startServerForServlet(new DemoPageServlet(), resourceName);
        serverHost = new HttpHost("localhost", ((ServerConnector)fileServer.getConnectors()[0]).getLocalPort());
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
