package com.seleniumtests.browserfactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.List;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.osutility.OSUtility;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.proxy.auth.AuthType;

public abstract class IDesktopCapabilityFactory extends ICapabilitiesFactory {

	protected BrowserInfo selectedBrowserInfo;
	
	public IDesktopCapabilityFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
    private BrowserInfo prepareBinaryAndDriver(final BrowserType browserType, final String binPath, final String driverPath, final String version) throws UnsupportedEncodingException {

    	// automatic list from OS + binary added as launch option (see SeleniumTestsContext.updateInstalledBrowsers())
    	List<BrowserInfo> browserInfos = OSUtility.getInstalledBrowsersWithVersion().get(browserType);
    	
    	if (version != null) { 
    		selectedBrowserInfo = BrowserInfo.getInfoFromVersion(version, browserInfos);
    	} else if (binPath != null) {
    		selectedBrowserInfo = BrowserInfo.getInfoFromBinary(binPath, browserInfos);
    		logger.info("Using user defined browser binary from: " + selectedBrowserInfo.getPath());
    	} else {
    		selectedBrowserInfo = BrowserInfo.getHighestDriverVersion(browserInfos);
    	}
    	
    	// in case of legacy firefox driverFileName is null
    	String newDriverPath = new DriverExtractor().extractDriver(selectedBrowserInfo.getDriverFileName());
    	if (driverPath != null) {
    		newDriverPath = driverPath;
    		logger.info("using user defined driver from: " + driverPath);
    	}
    	if (newDriverPath != null) {
    		System.setProperty(getDriverExeProperty(), newDriverPath);
    		
    		if (!OSUtility.isWindows()) {
                new File(newDriverPath).setExecutable(true);
            }
    	}
		
        return selectedBrowserInfo;
    }

	public BrowserInfo getSelectedBrowserInfo() {
		return selectedBrowserInfo;
	}

    public MutableCapabilities createCapabilities() {
    	MutableCapabilities options = getDriverOptions();
        options = options.merge(updateDefaultCapabilities());

        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
	        try {
				prepareBinaryAndDriver(getBrowserType(),
						getBrowserBinaryPath(), 
						getDriverPath(),
						webDriverConfig.getBrowserVersion());
				
				updateOptionsWithSelectedBrowserInfo(options);
			} catch (UnsupportedEncodingException e) {
			}
        }
        return options;
    }
    
    protected abstract MutableCapabilities getDriverOptions();
    
    protected abstract String getDriverPath();
    
    protected abstract BrowserType getBrowserType();
    
    protected abstract String getDriverExeProperty();
    
    protected abstract String getBrowserBinaryPath();
    
    protected abstract void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options);
 
    private MutableCapabilities updateDefaultCapabilities() {

    	DesiredCapabilities capability = new DesiredCapabilities();
        if (webDriverConfig.isEnableJavascript()) {
            capability.setJavascriptEnabled(true);
        } else {
            capability.setJavascriptEnabled(false);
        }

        capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
        capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

        if (webDriverConfig.getBrowserVersion() != null) {
            capability.setVersion(webDriverConfig.getBrowserVersion());
        }

        if (webDriverConfig.getWebPlatform() != null) {
            capability.setPlatform(webDriverConfig.getWebPlatform());
        }

        configureProxyCap(capability);

        return capability;
    }  
    
    /**
     * Add proxy capability
     * If network capture is enabled, start browsermob proxy and set it into browser
     * @param capability
     */
    private void configureProxyCap(MutableCapabilities capability) {
    	Proxy proxy = webDriverConfig.getProxy();

        if (webDriverConfig.getCaptureNetwork()) {
        	
        	if (webDriverConfig.getWebProxyType() == ProxyType.PAC) {
        		throw new ConfigurationException("PAC proxy cannot be used with browsermob");
        	}
        	
			BrowserMobProxy mobProxy = new BrowserMobProxyServer();
			
			if (webDriverConfig.getWebProxyType() == ProxyType.MANUAL) {
				mobProxy.setChainedProxy(new InetSocketAddress(webDriverConfig.getWebProxyAddress(), webDriverConfig.getWebProxyPort()));
				
				if (webDriverConfig.getWebProxyLogin() != null && webDriverConfig.getWebProxyPassword() != null) {
					mobProxy.chainedProxyAuthorization(webDriverConfig.getWebProxyLogin(), webDriverConfig.getWebProxyPassword(), AuthType.BASIC);
				}
			}
			mobProxy.setTrustAllServers(true);
			mobProxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT, CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_HEADERS);
			mobProxy.start(0);
		    Proxy seleniumProxy = ClientUtil.createSeleniumProxy(mobProxy);
	    
		    capability.setCapability(CapabilityType.PROXY, seleniumProxy);
		    webDriverConfig.setBrowserMobProxy(mobProxy);
        } else {
            capability.setCapability(CapabilityType.PROXY, proxy);
        }
    }
}
