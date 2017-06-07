package com.seleniumtests.connectors.selenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.util.FileUtility;

import io.appium.java_client.remote.MobileCapabilityType;

public class SeleniumRobotGridConnector extends SeleniumGridConnector {

	public SeleniumRobotGridConnector(String url) {
		super(url);
	}
	
	/**
	 * In case an app is required on the node running the test, upload it to the grid
	 */
	public void uploadMobileApp(Capabilities caps) {
		
		String appPath = (String)caps.getCapability(MobileCapabilityType.APP);
		
		// check whether app is given and app path is a local file
		if (appPath != null && new File(appPath).isFile()) {
			
			try {
				// zip file
				List<File> appFiles = new ArrayList<>();
				appFiles.add(new File(appPath));
				File zipFile = FileUtility.createZipArchiveFromFiles(appFiles);
				
				CloseableHttpClient client = HttpClients.createDefault();
				
				HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
				URIBuilder builder = new URIBuilder();
	        	builder.setPath("/grid/admin/FileServlet/");
	        	builder.addParameter("output", "app");
	        	HttpPost httpPost = new HttpPost(builder.build());
		        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString());
		        FileInputStream fileInputStream = new FileInputStream(zipFile);
	            InputStreamEntity entity = new InputStreamEntity(fileInputStream);
	            httpPost.setEntity(entity);
		        
		        CloseableHttpResponse response = client.execute(serverHost, httpPost);
		        if (response.getStatusLine().getStatusCode() != 200) {
		        	throw new SeleniumGridException("could not upload application file: " + response.getStatusLine().getReasonPhrase());
		        } else {
		        	((DesiredCapabilities)caps).setCapability(MobileCapabilityType.APP, IOUtils.toString(response.getEntity().getContent()) + "/" + appFiles.get(0).getName());
		        }
		        
			} catch (IOException | URISyntaxException e) {
				throw new SeleniumGridException("could not upload application file", e);
			}
		}
	}

}
