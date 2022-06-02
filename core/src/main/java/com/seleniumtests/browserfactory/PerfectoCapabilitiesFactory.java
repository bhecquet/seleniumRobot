package com.seleniumtests.browserfactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.StopWatch;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.MobileCapabilityType;

public class PerfectoCapabilitiesFactory extends ICloudCapabilityFactory {

	private static final Pattern CLOUD_NAME_PATTERN = Pattern.compile("https://([^:@]++)@(\\w+).perfectomobile.com/nexperience.*");
	
	protected PerfectoCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	public MutableCapabilities createCapabilities() {
		
		String apiKey = extractApiKey();
		
		DesiredCapabilities  capabilities = new DesiredCapabilities ();
		capabilities.setCapability("enableAppiumBehavior", true);
		capabilities.setCapability("autoLaunch", true);
		capabilities.setCapability("securityToken", apiKey);

		
		if(ANDROID_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
        	Capabilities androidCaps = new AndroidCapabilitiesFactory(webDriverConfig).createCapabilities();
        	capabilities.merge(androidCaps);
            
        } else if (IOS_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
        	Capabilities iosCaps = new IOsCapabilitiesFactory(webDriverConfig).createCapabilities();
        	capabilities.merge(iosCaps);
        } 
		
		// we need to upload something
		if (capabilities.getCapability(MobileCapabilityType.APP) != null) {
			boolean uploadApp = isUploadApp(capabilities);
			
			String appName = new File((String) capabilities.getCapability(MobileCapabilityType.APP)).getName();
			String repositoryKey = String.format("PUBLIC:%s", appName);
			
			String cloudName = extractCloudName();
			
			if (uploadApp) {
				try {
					uploadFile(cloudName, apiKey, (String)capabilities.getCapability(MobileCapabilityType.APP), repositoryKey);
					
				} catch (URISyntaxException | IOException e) {
					throw new ScenarioException("Could not upload file", e);
				}
			}
			capabilities.setCapability(MobileCapabilityType.APP, repositoryKey);
		}

		return capabilities;
	}
	
	private String extractApiKey() {
		List<String> hubUrls = webDriverConfig.getHubUrl();
		if (hubUrls.isEmpty() || !hubUrls.get(0).contains(".perfectomobile.com/nexperience/perfectomobile/wd/hub")) {
			throw new ConfigurationException("Perfecto usage needs configuring 'webDriverGrid' parameter with the format 'https://<apikey>@<cloudName>.perfectomobile.com/nexperience/perfectomobile/wd/hub'");
		}
		String hubUrl = hubUrls.get(0);
		
		Matcher apiKeyMatcher = CLOUD_NAME_PATTERN.matcher(hubUrl);
		if (apiKeyMatcher.matches()) {
			return apiKeyMatcher.group(1);
		} else {
			throw new ConfigurationException("Api key no provided");
		}
	}
	private String extractCloudName() {
		List<String> hubUrls = webDriverConfig.getHubUrl();
		if (hubUrls.isEmpty() || !hubUrls.get(0).contains(".perfectomobile.com/nexperience/perfectomobile/wd/hub")) {
			throw new ConfigurationException("Perfecto usage needs configuring 'webDriverGrid' parameter with the format 'https://<apikey>@<cloudName>.perfectomobile.com/nexperience/perfectomobile/wd/hub'");
		}
		String hubUrl = hubUrls.get(0);
		
		Matcher cloudNameMatcher = CLOUD_NAME_PATTERN.matcher(hubUrl);
		if (cloudNameMatcher.matches()) {
			return cloudNameMatcher.group(2);
		}
		return "";
	}
	

	public static void uploadFile(String cloudName, String securityToken, String path, String artifactLocator) throws URISyntaxException, IOException {
		
		String proxyHost = System.getProperty("https.proxyHost");
		String proxyPort = System.getProperty("https.proxyPort");
		
		
		StopWatch stopwatch = new StopWatch();
		stopwatch.start();

		System.out.println("Upload Started");		  
		URIBuilder taskUriBuilder = new URIBuilder("https://" + cloudName + ".app.perfectomobile.com/repository/api/v1/artifacts");
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		if (proxyHost != null && proxyPort != null) {
			httpClientBuilder.setProxy(new HttpHost(proxyHost, Integer.valueOf(proxyPort)));
		}
		
		HttpClient httpClient = httpClientBuilder.build();
		HttpPost httppost = new HttpPost(taskUriBuilder.build());
		httppost.setHeader("Perfecto-Authorization", securityToken);
		
		MultipartEntityBuilder mpEntity = MultipartEntityBuilder.create();
		File packagedFile = new File(path);
		ContentBody inputStream = new FileBody(packagedFile, ContentType.APPLICATION_OCTET_STREAM);

		JSONObject req = new JSONObject();
		req.put("artifactLocator", artifactLocator);
		req.put("override", true);
		String rp = req.toString();

		ContentBody requestPart = new StringBody(rp, ContentType.APPLICATION_JSON);
		mpEntity.addPart("inputStream", inputStream);
		mpEntity.addPart("requestPart", requestPart);
		httppost.setEntity(mpEntity.build());
		HttpResponse response = httpClient.execute(httppost);
		int statusCode = response.getStatusLine().getStatusCode();

		stopwatch.stop();
		long x = stopwatch.getTime();
		System.out.println("Status Code = " + statusCode);
		System.out.println("Upload Time = " + Long.toString(x));
	}

}
