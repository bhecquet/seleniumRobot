/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestRunner;

import com.seleniumtests.core.config.ConfigReader;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.reporter.PluginsHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Defines TestNG context used in STF.
 */
public class SeleniumTestsContext {
	
	// folder config
	private Map<String, HashMap<String,String>> idMapping;
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumTestsContext.class);

    /* configuration defined in testng.xml */
    public static final String TEST_CONFIGURATION = "testConfig"; 				// configuration annexe
    public static final String DEVICE_LIST = "deviceList"; 						// List of known devices in json format (internal use only)
    public static final String WEB_SESSION_TIME_OUT = "webSessionTimeOut";		// timeout de la session du navigateur
    public static final String IMPLICIT_WAIT_TIME_OUT = "implicitWaitTimeOut";	// attente implicite du navigateur
    public static final String EXPLICIT_WAIT_TIME_OUT = "explicitWaitTimeOut";	// attente explicite du navigateur
    public static final String REPLAY_TIME_OUT = "replayTimeOut";				// time during which an action is replayed. By default 30 secs
    public static final String PAGE_LOAD_TIME_OUT = "pageLoadTimeout";			// temps d'attente de chargement d'une page
    public static final String WEB_DRIVER_GRID = "webDriverGrid";				// adresse du serveur seleniumGrid
    public static final String RUN_MODE = "runMode";							// local ou grid. Pourrait également contenir sauceLabs / testDroid
    public static final String DEV_MODE = "devMode";							// The development mode allow all existing browsers to remain. It is set to "false" by default, which means it closes all existing browsers.
    public static final String BROWSER = "browser";								// navigateur utilisé. Sur Android, le navigateur par défaut est "Browser"
    public static final String BROWSER_VERSION = "browserVersion";				// version de navigateur utilisé
    public static final String FIREFOX_USER_PROFILE_PATH = "firefoxUserProfilePath";	// profile utilisateur firefox
    public static final String USE_DEFAULT_FIREFOX_PROFILE = "useFirefoxDefaultProfile";// utilisation du profile firefox par défaut
    public static final String OPERA_USER_PROFILE_PATH = "operaUserProfilePath";	// profile utilisateur opéra
    public static final String FIREFOX_BINARY_PATH = "firefoxBinaryPath";		// chemin vers le binaire firefox (firefox portable ou pour utiliser une version spécifique
    public static final String CHROME_DRIVER_PATH = "chromeDriverPath";			// chemin vers chromeDriver si on souhaite utiliser une version différente
    public static final String EDGE_DRIVER_PATH = "edgeDriverPath";				// path to Edge driver binary if we want to use an other version than the provided one
    public static final String CHROME_BINARY_PATH = "chromeBinaryPath";			// chemin vers le binaire chrome lorsque celui-ci n'est pas installé de manière normale
    public static final String IE_DRIVER_PATH = "ieDriverPath";					// chemin vers le driver Internet Explorer
    public static final String USER_AGENT = "userAgent";						// user agent utilisé pour les tests. Permet d'écraser le user-agent par défaut du navigateur, sur firefox et chrome uniquement

    public static final String VIEWPORT_WIDTH = "viewPortWidth";					// width of viewport	
    public static final String VIEWPORT_HEIGHT = "viewPortHeight";					// height of viewport	
    
    public static final String SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER = "setAssumeUntrustedCertificateIssuer"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides 
    public static final String SET_ACCEPT_UNTRUSTED_CERTIFICATES = "setAcceptUntrustedCertificates"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides
    public static final String ENABLE_JAVASCRIPT = "enableJavascript";			// activation du javascrit dans le navigateur.
    public static final String NTLM_AUTH_TRUSTED_URIS = "ntlmAuthTrustedUris";	// Firefox uniquement
    public static final String BROWSER_DOWNLOAD_DIR = "browserDownloadDir";		// répertoire où seront enregistrés les fichiers
    public static final String ADD_JS_ERROR_COLLECTOR_EXTENSION = "addJSErrorCollectorExtension"; // Firefox uniquement

    public static final String SNAPSHOT_TOP_CROPPING = "snapshotTopCropping";
    public static final String SNAPSHOT_BOTTOM_CROPPING = "snapshotBottomCropping";
    
    public static final String WEB_PROXY_TYPE = "proxyType";					// type de proxy. AUTO, MANUAL, NO
    public static final String WEB_PROXY_ADDRESS = "proxyAddress";				// adresse du proxy. 
    public static final String WEB_PROXY_PORT = "proxyPort";					// port du proxy
    public static final String WEB_PROXY_LOGIN = "proxyLogin";					// login du proxy (si nécessaire)
    public static final String WEB_PROXY_PASSWORD = "proxyPassword";			// mot de passe du proxy (si nécessaire)
    public static final String WEB_PROXY_EXCLUDE = "proxyExclude";				// exclusion des adresse de proxy
    public static final String WEB_PROXY_PAC = "proxyPac";						// adresse de configuration automatique du proxy

    public static final String TEST_ENTITY = "testEntity";						// Jamais utilisé

    public static final String CAPTURE_SNAPSHOT = "captureSnapshot";
    public static final String ENABLE_EXCEPTION_LISTENER = "enableExceptionListener";	// TODO: voir son effet, activé par défaut

    public static final String DP_TAGS_INCLUDE = "dpTagsInclude";				// 
    public static final String DP_TAGS_EXCLUDE = "dpTagsExclude";				// Utilisé pour la lecture de fichiers CSV/XLS des DataProvider TODO: a étudier comment cela fonctionne

    public static final String SOFT_ASSERT_ENABLED = "softAssertEnabled";		// le test ne s'arrête pas lorsqu'une assertion est rencontrée

    public static final String OUTPUT_DIRECTORY = "outputDirectory";     		// folder where HTML report will be written
    public static final String WEB_DRIVER_LISTENER = "webDriverListener";

    public static final String TEST_METHOD_SIGNATURE = "testMethodSignature";
    public static final String PLUGIN_CONFIG_PATH = "pluginConfigPath";

    public static final String TEST_DATA_FILE = "testDataFile";

    public static final String TEST_TYPE = "testType";							// configured automatically
    
    public static final String CUCUMBER_TESTS = "cucumberTests";				// liste des tests en mode cucumber
    public static final String CUCUMBER_TAGS = "cucumberTags";					// liste des tags cucumber
    public static final String TEST_CONFIG = "currentTestConfig"; 				// configuration used for the current test. It is not updated via XML file
    public static final String TEST_ENV = "env";								// environnement de test pour le SUT. Permet d'accéder aux configurations spécifiques du fichier env.ini
    public static final String CUCUMBER_IMPLEMENTATION_PKG = "cucumberPackage";	// nom du package java pour les classes cucumber, car celui-ci n'est pas accessible par testNG
    
    // Appium specific properties
    public static final String APP = "app";										// Chemin de l'application mobile (local ou distant)
    public static final String APPIUM_SERVER_URL = "appiumServerURL";			// URL du serveur appium en local ou à distance
    public static final String MOBILE_PLATFORM_VERSION = "mobilePlatformVersion";// Mobile OS version. It's deduced from platform name and not read directly from parameters
    public static final String DEVICE_NAME = "deviceName";						// Nom du terminal utilisé pour le test

    public static final String APP_PACKAGE = "appPackage";						// package de l'application
    public static final String APP_ACTIVITY = "appActivity";					// activité à démarrer (Android)
    public static final String APP_WAIT_ACTIVITY = "appWaitActivity";			// dans certains cas, l'activité qui démarre l'application n'est pas l'activité principale. C'est celle-ci qu'on attend
    public static final String NEW_COMMAND_TIMEOUT = "newCommandTimeout";		// Attente maximale entre 2 commandes envoyées à appium

    // Cloud specific properties
    public static final String VERSION = "version";								// 
    public static final String PLATFORM = "platform";							// platform on which test should execute. Ex: Windows 7, Android, iOS, Linux, OS X 10.10. 	
    public static final String CLOUD_API_KEY = "cloudApiKey";					// clé d'accès (dépend des services)
    
    // Testdroid specific properties
    public static final String PROJECT_NAME = "projectName";					// TestDroid nécessite un nom de projet dans lequel l'automatisation aura lieu	

    private static final int REPLAY_TIME_OUT_VALUE = 30;
    
    private LinkedList<TearDownService> tearDownServices = new LinkedList<>();
    private Map<ITestResult, List<Throwable>> verificationFailuresMap = new HashMap<>();

    /* Data object to store all context data */
    private Map<String, Object> contextDataMap = Collections.synchronizedMap(new HashMap<String, Object>());
    private Map<String, String> testVariables = Collections.synchronizedMap(new HashMap<String, String>());

    private ITestContext testNGContext = null;
    
    public SeleniumTestsContext() {
    	// for test purpose only
    }
    
    public SeleniumTestsContext(final ITestContext context) {
        this.testNGContext = context;

        setTestDataFile(getValueForTest(TEST_DATA_FILE, System.getProperty(TEST_DATA_FILE)));
        setWebSessionTimeout(getIntValueForTest(WEB_SESSION_TIME_OUT, System.getProperty(WEB_SESSION_TIME_OUT)));
        setImplicitWaitTimeout(getIntValueForTest(IMPLICIT_WAIT_TIME_OUT, System.getProperty(IMPLICIT_WAIT_TIME_OUT)));
        setExplicitWaitTimeout(getIntValueForTest(EXPLICIT_WAIT_TIME_OUT, System.getProperty(EXPLICIT_WAIT_TIME_OUT)));
        setReplayTimeout(getIntValueForTest(REPLAY_TIME_OUT, System.getProperty(REPLAY_TIME_OUT)));
        setPageLoadTimeout(getIntValueForTest(PAGE_LOAD_TIME_OUT, System.getProperty(PAGE_LOAD_TIME_OUT)));
        setWebDriverGrid(getValueForTest(WEB_DRIVER_GRID, System.getProperty(WEB_DRIVER_GRID)));
        setRunMode(getValueForTest(RUN_MODE, System.getProperty(RUN_MODE)));
        setDevMode(getBoolValueForTest(DEV_MODE, System.getProperty(DEV_MODE)));
        setBrowser(getValueForTest(BROWSER, System.getProperty(BROWSER)));
        setBrowserVersion(getValueForTest(BROWSER_VERSION, System.getProperty(BROWSER_VERSION)));
        setFirefoxUserProfilePath(getValueForTest(FIREFOX_USER_PROFILE_PATH, System.getProperty(FIREFOX_USER_PROFILE_PATH)));
        setUseDefaultFirefoxProfile(getBoolValueForTest(USE_DEFAULT_FIREFOX_PROFILE, System.getProperty(USE_DEFAULT_FIREFOX_PROFILE)));
        setOperaUserProfilePath(getValueForTest(OPERA_USER_PROFILE_PATH, System.getProperty(OPERA_USER_PROFILE_PATH)));
        setFirefoxBinary(getValueForTest(FIREFOX_BINARY_PATH, System.getProperty(FIREFOX_BINARY_PATH)));
        setChromeDriverPath(getValueForTest(CHROME_DRIVER_PATH, System.getProperty(CHROME_DRIVER_PATH)));
        setEdgeDriverPath(getValueForTest(EDGE_DRIVER_PATH, System.getProperty(EDGE_DRIVER_PATH)));
        setIEDriverPath(getValueForTest(IE_DRIVER_PATH, System.getProperty(IE_DRIVER_PATH)));
        setUserAgent(getValueForTest(USER_AGENT, System.getProperty(USER_AGENT)));
        setAssumeUntrustedCertificateIssuer(getBoolValueForTest(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER, System.getProperty(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER)));
        setAcceptUntrustedCertificates(getBoolValueForTest(SET_ACCEPT_UNTRUSTED_CERTIFICATES, System.getProperty(SET_ACCEPT_UNTRUSTED_CERTIFICATES)));
        setJavascriptEnabled(getBoolValueForTest(ENABLE_JAVASCRIPT, System.getProperty(ENABLE_JAVASCRIPT)));
        setNtlmAuthTrustedUris(getValueForTest(NTLM_AUTH_TRUSTED_URIS, System.getProperty(NTLM_AUTH_TRUSTED_URIS)));
        setBrowserDownloadDir(getValueForTest(BROWSER_DOWNLOAD_DIR, System.getProperty(BROWSER_DOWNLOAD_DIR)));
   
        setJsErrorCollectorExtension(getBoolValueForTest(ADD_JS_ERROR_COLLECTOR_EXTENSION, System.getProperty(ADD_JS_ERROR_COLLECTOR_EXTENSION)));

        setWebProxyType(getValueForTest(WEB_PROXY_TYPE, System.getProperty(WEB_PROXY_TYPE)));
        setWebProxyAddress(getValueForTest(WEB_PROXY_ADDRESS, System.getProperty(WEB_PROXY_ADDRESS)));
        setWebProxyLogin(getValueForTest(WEB_PROXY_LOGIN, System.getProperty(WEB_PROXY_LOGIN)));
        setWebProxyPassword(getValueForTest(WEB_PROXY_PASSWORD, System.getProperty(WEB_PROXY_PASSWORD)));
        setWebProxyPort(getIntValueForTest(WEB_PROXY_PORT, System.getProperty(WEB_PROXY_PORT)));
        setWebProxyExclude(getValueForTest(WEB_PROXY_EXCLUDE, System.getProperty(WEB_PROXY_EXCLUDE)));
        setWebProxyPac(getValueForTest(WEB_PROXY_PAC, System.getProperty(WEB_PROXY_PAC)));

        setSnapshotBottomCropping(getIntValueForTest(SNAPSHOT_BOTTOM_CROPPING, System.getProperty(SNAPSHOT_BOTTOM_CROPPING)));
        setSnapshotTopCropping(getIntValueForTest(SNAPSHOT_TOP_CROPPING, System.getProperty(SNAPSHOT_TOP_CROPPING)));
        setCaptureSnapshot(getBoolValueForTest(CAPTURE_SNAPSHOT, System.getProperty(CAPTURE_SNAPSHOT)));
        setEnableExceptionListener(getBoolValueForTest(ENABLE_EXCEPTION_LISTENER, System.getProperty(ENABLE_EXCEPTION_LISTENER)));

        setDpTagsInclude(getValueForTest(DP_TAGS_INCLUDE, System.getProperty(DP_TAGS_INCLUDE)));
        setDpTagsExclude(getValueForTest(DP_TAGS_EXCLUDE, System.getProperty(DP_TAGS_EXCLUDE)));

        setSoftAssertEnabled(getBoolValueForTest(SOFT_ASSERT_ENABLED, System.getProperty(SOFT_ASSERT_ENABLED)));

        setWebDriverListener(getValueForTest(WEB_DRIVER_LISTENER, System.getProperty(WEB_DRIVER_LISTENER)));

        setAppiumServerUrl(getValueForTest(APPIUM_SERVER_URL, System.getProperty(APPIUM_SERVER_URL)));
        setDeviceName(getValueForTest(DEVICE_NAME, System.getProperty(DEVICE_NAME)));
        setDeviceList(getValueForTest(DEVICE_LIST, null));

        setApp(getValueForTest(APP, System.getProperty(APP)));
       
        setCucumberTags(getValueForTest(CUCUMBER_TAGS, System.getProperty(CUCUMBER_TAGS)));
        setCucumberTests(getValueForTest(CUCUMBER_TESTS, System.getProperty(CUCUMBER_TESTS)));
        setCucumberImplementationPackage(getValueForTest(CUCUMBER_IMPLEMENTATION_PKG, System.getProperty(CUCUMBER_IMPLEMENTATION_PKG)));
        setTestEnv(getValueForTest(TEST_ENV, System.getProperty(TEST_ENV)));

        // By default test is assumed to be executed on default browser on android emulator
        setAppPackage(getValueForTest(APP_PACKAGE, System.getProperty(APP_PACKAGE)));
        setAppActivity(getValueForTest(APP_ACTIVITY, System.getProperty(APP_ACTIVITY)));
        setAppWaitActivity(getValueForTest(APP_WAIT_ACTIVITY, System.getProperty(APP_WAIT_ACTIVITY)));
        setNewCommandTimeout(getIntValueForTest(NEW_COMMAND_TIMEOUT, System.getProperty(NEW_COMMAND_TIMEOUT)));

        setVersion(getValueForTest(VERSION, System.getProperty(VERSION)));
        setPlatform(getValueForTest(PLATFORM, System.getProperty(PLATFORM)));
        setCloudApiKey(getValueForTest(CLOUD_API_KEY, System.getProperty(CLOUD_API_KEY)));
        setProjectName(getValueForTest(PROJECT_NAME, System.getProperty(PROJECT_NAME)));
        
        setViewPortWidth(getIntValueForTest(VIEWPORT_WIDTH, System.getProperty(VIEWPORT_WIDTH)));
        setViewPortHeight(getIntValueForTest(VIEWPORT_HEIGHT, System.getProperty(VIEWPORT_HEIGHT)));
        
        // determines test_type according to input configuration
        configureTestType();

        // get mobile platform version if one is defined in device list and a deviceName is set in parameters
        updateDeviceMobileVersion();
        
        // get mobile platform version
        updatePlatformVersion();
        
        if (context != null) {
        	setOutputDirectory(getValueForTest(OUTPUT_DIRECTORY, System.getProperty(OUTPUT_DIRECTORY)), context);

            // parse other parameters that are defined in testng xml but not defined
            // in this context
            setContextAttribute(context);

            new File(context.getOutputDirectory() + "/screenshots").mkdirs();
            new File(context.getOutputDirectory() + "/htmls").mkdirs();
            new File(context.getOutputDirectory() + "/xmls").mkdirs();
            new File(context.getOutputDirectory() + "/textfiles/").mkdirs();

            String path = (String) getAttribute(PLUGIN_CONFIG_PATH);

            if (path != null && path.trim().length() > 0) {
                File configFile = new File(path);
                if (configFile.exists()) {
                    PluginsHelper.getInstance().loadPlugins(configFile);
                }
            }
        }
    }
    
    /**
     * From platform name, in case of Desktop platform, do nothing and in case of mobile, extract OS version from name
     *
     * @throws ConfigurationException 	in mobile, if version is not present
     */
    private void updatePlatformVersion() {
    	try {
	    	Platform currentPlatform = Platform.fromString(getPlatform());
	    	if (currentPlatform.is(Platform.WINDOWS) 
	    		|| currentPlatform.is(Platform.MAC) 
	    		|| currentPlatform.is(Platform.UNIX) ) {
	    		return;
	    	} else {
	    		throw new WebDriverException("");
	    	}
	    } catch (WebDriverException e) {
	    	if (getPlatform().toLowerCase().startsWith("android") || getPlatform().toLowerCase().startsWith("ios")) {
	    		String[] pfVersion = getPlatform().split(" ", 2);
	    		try {
		    		setPlatform(pfVersion[0]);
		    		setMobilePlatformVersion(pfVersion[1]);
	    		} catch (IndexOutOfBoundsException x) {
	    			throw new ConfigurationException("For mobile platform, platform name should contain version. Ex: 'Android 5.0' or 'iOS 9.1'");
	    		}
			} else {
				throw new ConfigurationException(String.format("Platform %s has not been recognized as a valid platform", getPlatform()));
			}
    	}
    }
    
    /**
     * Configure test type according to platform, browser and app parameters
     */
    private void configureTestType() {
    	if (getPlatform().toLowerCase().startsWith("android")) {
        	if (getApp().isEmpty()) { // a browser name should be defined
        		setTestType(TestType.APPIUM_WEB_ANDROID);
        	} else {
        		setTestType(TestType.APPIUM_APP_ANDROID);
        	}
        } else if (getPlatform().toLowerCase().startsWith("ios")) {
        	if (getApp().isEmpty()) { // a browser name should be defined
        		setTestType(TestType.APPIUM_WEB_IOS);
        	} else {
        		setTestType(TestType.APPIUM_APP_IOS);
        	}
        } else {
        	if (getBrowser() == BrowserType.NONE) {
        		setTestType(TestType.NON_GUI);
        	} else {
        		setTestType(TestType.WEB);
        	}
        }
    }
    
    /**
     * Search mobile platform version according to device name if one has been defined in testConfig file
     */
    private void updateDeviceMobileVersion() {
    	Map<String, String> deviceList = getDeviceList();
    	if (getDeviceName() != null && !getDeviceName().isEmpty() && !deviceList.isEmpty()) {
    		setAttribute(PLATFORM, deviceList.get(getDeviceName()));
    	}
    }

    /**
     * Adds the given tear down.
     */
    public void addTearDownService(final TearDownService tearDown) {
        tearDownServices.add(tearDown);
    }

    public void addVerificationFailures(final ITestResult result, final List<Throwable> failures) {

        this.verificationFailuresMap.put(result, failures);
    }

    public void addVerificationFailures(final ITestResult result, final Throwable failure) {

        if (verificationFailuresMap.get(result) != null) {
            this.verificationFailuresMap.get(result).add(failure);
        } else {
            ArrayList<Throwable> failures = new ArrayList<>();
            failures.add(failure);
            this.addVerificationFailures(result, failures);
        }
    }

    public Boolean getAddJSErrorCollectorExtension() {
        return (Boolean) getAttribute(ADD_JS_ERROR_COLLECTOR_EXTENSION);
    }

    public Object getAttribute(final String name) {
        Object obj = contextDataMap.get(name);
        return obj == null ? null : obj;
    }

    public String getBrowserDownloadDir() {
        if (getAttribute(BROWSER_DOWNLOAD_DIR) != null) {
            return (String) getAttribute(BROWSER_DOWNLOAD_DIR);
        } else {
            return this.getOutputDirectory() + "\\downloads\\";
        }
    }
    
    public int getSnapshotBottomCropping() {
    	return (Integer) getAttribute(SNAPSHOT_BOTTOM_CROPPING);
    }
    
    public int getSnapshotTopCropping() {
    	return (Integer) getAttribute(SNAPSHOT_TOP_CROPPING);
    }

    public boolean getCaptureSnapshot() {
        if (getAttribute(CAPTURE_SNAPSHOT) == null) {

            // IE grid default value set to false
            if (this.getRunMode() == DriverMode.GRID
                    && (this.getBrowser() == BrowserType.INTERNET_EXPLORER 
                    || this.getBrowser() == BrowserType.SAFARI)) {
                this.setAttribute(CAPTURE_SNAPSHOT, false);
            } else {
                this.setAttribute(CAPTURE_SNAPSHOT, true);
            }
        }

        return (Boolean) getAttribute(CAPTURE_SNAPSHOT);
    }

    public boolean getEnableExceptionListener() {
        return (Boolean) getAttribute(ENABLE_EXCEPTION_LISTENER);
    }
    
    public boolean getJsErrorCollectorExtension() {
    	return (Boolean) getAttribute(ADD_JS_ERROR_COLLECTOR_EXTENSION);
    }

    public String getChromeBinPath() {
        return (String) getAttribute(CHROME_BINARY_PATH);
    }

    public String getChromeDriverPath() {
        return (String) getAttribute(CHROME_DRIVER_PATH);
    }
    
    public String getEdgeDriverPath() {
    	return (String) getAttribute(EDGE_DRIVER_PATH);
    }

    public String getDPTagsExclude() {
        return (String) getAttribute(DP_TAGS_EXCLUDE);
    }

    public String getDPTagsInclude() {
        return (String) getAttribute(DP_TAGS_INCLUDE);
    }

    public int getExplicitWaitTimeout() {
        Integer timeout;
        try {
            timeout = (Integer) getAttribute(EXPLICIT_WAIT_TIME_OUT);
        } catch (Exception e) {
            timeout = 15;
        }

        if (timeout < getImplicitWaitTimeout()) {
            return getImplicitWaitTimeout();
        } else {
            return timeout;
        }
    }

    public String getFirefoxBinPath() {
        return (String) getAttribute(FIREFOX_BINARY_PATH);
    }

    public String getFirefoxUserProfilePath() {
        return (String) getAttribute(FIREFOX_USER_PROFILE_PATH);
    }

    public String getIEDriverPath() {
        return (String) getAttribute(IE_DRIVER_PATH);
    }

    public int getImplicitWaitTimeout() {
        try {
            return (Integer) getAttribute(IMPLICIT_WAIT_TIME_OUT);
        } catch (Exception e) {
            return 5;
        }
    }
    
    public int getReplayTimeout() {
    	try {
    		return (Integer) getAttribute(REPLAY_TIME_OUT);
    	} catch (Exception e) {
    		return REPLAY_TIME_OUT_VALUE;
    	}
    }

    public String getNtlmAuthTrustedUris() {
        return (String) getAttribute(NTLM_AUTH_TRUSTED_URIS);
    }
	
	public Boolean getAssumeUntrustedCertificateIssuer() {
        return (Boolean) getAttribute(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER);
    }
	
	public Boolean getJavascriptEnabled() {
		return (Boolean) getAttribute(ENABLE_JAVASCRIPT);
	}

	public Boolean getAcceptUntrustedCertificates() {
        return (Boolean) getAttribute(SET_ACCEPT_UNTRUSTED_CERTIFICATES);
    }


    public String getOperaUserProfilePath() {
        return (String) getAttribute(OPERA_USER_PROFILE_PATH);
    }

    public String getOutputDirectory() {
        return (String) getAttribute(OUTPUT_DIRECTORY);
    }

    public int getPageLoadTimeout() {
        try {
            return (Integer) getAttribute(PAGE_LOAD_TIME_OUT);
        } catch (Exception e) {
            return 90;
        }
    }

    /**
     * Get TestNG suite parameter from testng.xml. Return System value for CI job.
     *
     * @param   key		name of the parameter
     *
     * @return value of the parameter
     */
    public String getSuiteParameter(final String key) {
        if (System.getProperty(key) != null) {
            return System.getProperty(key);
        } else {
            return getTestNGContext().getSuite().getParameter(key);
        }
    }

    /**
     * Returns the tear down list.
     * 
     * @return list of teardown services
     */
    public List<TearDownService> getTearDownServices() {
        return tearDownServices;
    }
    
    public String getTestDataFile() {
        return (String) getAttribute(TEST_DATA_FILE);
    }

    public TestType getTestType() {
        return (TestType) getAttribute(TEST_TYPE);
    }

    public String getCucumberTags() {
    	return (String) getAttribute(CUCUMBER_TAGS);
    }
    
    public List<String> getCucumberTests() {
    	List<String> tests = new ArrayList<>();
    	if (((String)getAttribute(CUCUMBER_TESTS)).isEmpty()) {
    		return tests;
    	}
    	for (String test: ((String)getAttribute(CUCUMBER_TESTS)).replace("\"", "").replace("&nbsp;", " ").split(",")) {
    		tests.add(test.trim());
    	}
    	return tests;
    }
    
    public String getCucmberPkg() {
    	return (String) getAttribute(CUCUMBER_IMPLEMENTATION_PKG);
    }
    
    public String getTestEnv() {
    	return (String) getAttribute(TEST_ENV);
    }
    
    public String getTestMethodSignature() {
        return (String) getAttribute(TEST_METHOD_SIGNATURE);
    }

    public ITestContext getTestNGContext() {
        return testNGContext;
    }

    public Object getTestEntity() {
        return getAttribute(TEST_ENTITY);
    }

    public String getWebDriverListener() {
        return (String) getAttribute(WEB_DRIVER_LISTENER);
    }

    public String getUserAgent() {
        return (String) getAttribute(USER_AGENT);
    }

    public List<Throwable> getVerificationFailures(final ITestResult result) {
        List<Throwable> verificationFailures = verificationFailuresMap.get(result);
        return verificationFailures == null ? new ArrayList<>() : verificationFailures;

    }

    public String getWebBrowserVersion() {
        return (String) getAttribute(BROWSER_VERSION);
    }

    public String getWebDriverGrid() {
        return (String) getAttribute(WEB_DRIVER_GRID);
    }

    public String getWebProxyAddress() {
        return (String) getAttribute(WEB_PROXY_ADDRESS);
    }

    public ProxyType getWebProxyType() {
        return (ProxyType) getAttribute(WEB_PROXY_TYPE);
    }

    public Integer getWebProxyPort() {
		return (Integer) getAttribute(WEB_PROXY_PORT);
	}

	public String getWebProxyLogin() {
		return (String) getAttribute(WEB_PROXY_LOGIN);
	}

	public String getWebProxyPassword() {
		return (String) getAttribute(WEB_PROXY_PASSWORD);
	}

	public String getWebProxyExclude() {
		return (String) getAttribute(WEB_PROXY_EXCLUDE);
	}

	public String getWebProxyPac() {
		return (String) getAttribute(WEB_PROXY_PAC);
	}

	public BrowserType getBrowser() {
        return (BrowserType) getAttribute(BROWSER);
    }

    public DriverMode getRunMode() {
        return (DriverMode) getAttribute(RUN_MODE);
    }
    
    public boolean isDevMode() {
        return (Boolean) getAttribute(DEV_MODE);
    }
    
    @SuppressWarnings("unchecked")
	public Map<String, String> getDeviceList() {
    	HashMap<String, String> deviceList = new HashMap<>();
    	if (getAttribute(DEVICE_LIST) == null || "{}".equals(getAttribute(DEVICE_LIST))) {
    		return deviceList;
    	}
    	
    	JSONObject devList = new JSONObject((String)getAttribute(DEVICE_LIST));
    	for (String key: (Set<String>)devList.keySet()) {
    		deviceList.put(key, devList.getString(key));
    	}
    	return deviceList;
    }

    public int getWebSessionTimeout() {
        return (Integer) getAttribute(WEB_SESSION_TIME_OUT);
    }

    public String getAppiumServerURL() {
        return (String) getAttribute(APPIUM_SERVER_URL);
    }

    public String getMobilePlatformVersion() {
        return (String) getAttribute(MOBILE_PLATFORM_VERSION);
    }

    public String getDeviceName() {
        return (String) getAttribute(DEVICE_NAME);
    }

    public String getApp() {
        return (String) getAttribute(APP);
    }

    public String getAppPackage() {
        return (String) getAttribute(APP_PACKAGE);
    }

    public String getAppActivity() {
        return (String) getAttribute(APP_ACTIVITY);
    }
    
    public String getAppWaitActivity() {
    	return (String) getAttribute(APP_WAIT_ACTIVITY);
    }

    public int getNewCommandTimeout() {
        return (Integer) getAttribute(NEW_COMMAND_TIMEOUT);
    }

    public String getVersion() {
        return (String) getAttribute(VERSION);
    }

    public String getPlatform() {
        return (String) getAttribute(PLATFORM);
    }
    
    public String getCloudApiKey() {
    	return (String) getAttribute(CLOUD_API_KEY);
    }
    
    public String getProjectName() {
    	return (String) getAttribute(PROJECT_NAME);
    }
    
    public Integer getViewPortWidth() {
		return (Integer) getAttribute(VIEWPORT_WIDTH);
	}
    
    public Integer getViewPortHeight() {
    	return (Integer) getAttribute(VIEWPORT_HEIGHT);
    }
    
    public Map<String, String> getConfiguration() {
    	return (HashMap<String, String>) getAttribute(TEST_CONFIG);
    }
    
    //Methods for ID_Mapping
    //get
    public Map<String, HashMap<String, String>> getIdMapping() {
    	return idMapping;
    }

	//set
    public void setIdMapping(Map<String, HashMap<String,String>> conf){
    	idMapping = conf;
    }
    
    
    public boolean isUseFirefoxDefaultProfile() {
        try {
            return (Boolean) getAttribute(USE_DEFAULT_FIREFOX_PROFILE);
        } catch (Exception e) {
            return true; // Default
        }

    }

    public boolean isSoftAssertEnabled() {
        try {
            return (Boolean) getAttribute(SOFT_ASSERT_ENABLED);
        } catch (Exception e) {
            return false; // Default
        }
    }

    public void setAttribute(final String name, final Object value) {
        contextDataMap.put(name, value);
    }

    /**
     * Add to contextMap parameters defined in testng file which are not known as technical parameters
     * For example, it's possible to add <parameter name="aNewParam" value="aValue" /> in context because it's unknown from 
     * constructor. Whereas <parameter name=browser value="*opera" /> will not be overriden as it's already known
     * @param context
     */
    private void setContextAttribute(final ITestContext context) {
        if (context != null) {
        	Map<String, String> testParameters;
        	if (context.getCurrentXmlTest() == null) {
        		testParameters = context.getSuite().getXmlSuite().getParameters();
        	} else {
        		testParameters = context.getCurrentXmlTest().getAllParameters();
        	}

            for (Entry<String, String> entry : testParameters.entrySet()) {
                String attributeName = entry.getKey();

                
                if (!contextDataMap.containsKey(entry.getKey())) {
                    String sysPropertyValue = System.getProperty(entry.getKey());
                    String suiteValue = entry.getValue();
                    setContextAttribute(attributeName, sysPropertyValue, suiteValue, null);
                    testVariables.put(attributeName, getAttribute(attributeName).toString());
                }

            }
        }

    }

    /**
     * Get (in order of importance) user value (if exist), test value (if exist), suite value (if exist) or null
     *
     * @param  context
     * @param  attributeName
     * @param  sysPropertyValue
     * @param  defaultValue
     */
    private String getValueForTest(final String attributeName, final String sysPropertyValue) {
    	String suiteValue = null;
        if (testNGContext != null && testNGContext.getCurrentXmlTest() != null) {
        	
        	// priority given to test parameter
        	String testValue = testNGContext.getCurrentXmlTest().getParameter(attributeName);
        	
        	if (testValue == null) {
        		
        		// if test parameter does not exist, look at suite parameter
        		suiteValue = testNGContext.getCurrentXmlTest().getSuite().getParameter(attributeName);
        		
        	} else {
        		suiteValue = testValue;
        	}
        }
        
        return sysPropertyValue != null ? sysPropertyValue : suiteValue;
    }
    
    /**
     * Return an int value from test parameters
     * @param attributeName
     * @param sysPropertyValue
     * @return
     */
    private Integer getIntValueForTest(final String attributeName, final String sysPropertyValue) {
    	String value = getValueForTest(attributeName, sysPropertyValue);
    	return value == null ? null: Integer.parseInt(value);
    }
    
    /**
     * Return a boolean value from test parameters
     * @param attributeName
     * @param sysPropertyValue
     * @return
     */
    private Boolean getBoolValueForTest(final String attributeName, final String sysPropertyValue) {
    	String value = getValueForTest(attributeName, sysPropertyValue);
    	return value == null ? null: Boolean.parseBoolean(value);
    }

    private void setContextAttribute(final String attributeName, final String sysPropertyValue, 
    									final String suiteValue, final String defaultValue) {

        contextDataMap.put(attributeName,
            sysPropertyValue != null ? sysPropertyValue : (suiteValue != null ? suiteValue : defaultValue));

    }

    public void setTestDataFile(String testDataFile) {
    	if (testDataFile != null) {
    		setAttribute(TEST_DATA_FILE, testDataFile);
    	} else {
    		setAttribute(TEST_DATA_FILE, "testCase");
    	}
    }
    
    public void setWebSessionTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(WEB_SESSION_TIME_OUT, timeout);
    	} else {
    		setAttribute(WEB_SESSION_TIME_OUT, 90000);
    	}
    }

    public void setImplicitWaitTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(IMPLICIT_WAIT_TIME_OUT, timeout);
    	} else {
    		setAttribute(IMPLICIT_WAIT_TIME_OUT, 5);
    	}
    }
    
    public void setExplicitWaitTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(EXPLICIT_WAIT_TIME_OUT, timeout);
    	} else {
    		setAttribute(EXPLICIT_WAIT_TIME_OUT, 15);
    	}
    }

    public void setPageLoadTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(PAGE_LOAD_TIME_OUT, timeout);
    	} else {
    		setAttribute(PAGE_LOAD_TIME_OUT, 90);
    	}
    }
    
    public void setReplayTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(REPLAY_TIME_OUT, timeout);
    	} else {
    		setAttribute(REPLAY_TIME_OUT, REPLAY_TIME_OUT_VALUE);
    	}
    }
    
    public void setWebDriverGrid(final String driverGrid) {
        setAttribute(WEB_DRIVER_GRID, driverGrid);
    }
    
    public void setRunMode(String runMode) {
    	String _runMode = runMode == null ? "LOCAL": runMode;
        setAttribute(RUN_MODE, DriverMode.fromString(_runMode));
	}
    
    public void setDevMode(Boolean devMode) {
    	if (devMode != null) {
    		setAttribute(DEV_MODE, devMode);
    	} else {
    		// default value depends on who starts test. If start is done through jar execution, deployed mode will be true (devMode set to false)
    		setAttribute(DEV_MODE, !SeleniumTestsContextManager.getDeployedMode());
    	}
    }

    public void setBrowser(String browser) {
    	String _browser = browser == null ? "*firefox": browser;
    	setAttribute(BROWSER, BrowserType.getBrowserType(_browser));
    	
    	// when reconfiguring browser, mostly from integration tests, change test type accordingly
    	if (getPlatform() != null) {
        	configureTestType();
    	}
    }
    
    public void setBrowserVersion(String browserVersion) {
    	setAttribute(BROWSER_VERSION, browserVersion);
    }
    
    public void setFirefoxUserProfilePath(String ffPath) {
    	setAttribute(FIREFOX_USER_PROFILE_PATH, ffPath);
    }
    
    public void setUseDefaultFirefoxProfile(Boolean useDefaultffProfile) {
		if (useDefaultffProfile != null) {
			setAttribute(USE_DEFAULT_FIREFOX_PROFILE, useDefaultffProfile);
		} else {
			setAttribute(USE_DEFAULT_FIREFOX_PROFILE, true);
    	}
    }
    
    public void setOperaUserProfilePath(String path) {
    	setAttribute(OPERA_USER_PROFILE_PATH, path);
    }
    
    public void setFirefoxBinary(String path) {
    	setAttribute(FIREFOX_BINARY_PATH, path);
    }
    
    public void setChromeDriverPath(String path) {
    	setAttribute(CHROME_DRIVER_PATH, path);
    }
    
    public void setEdgeDriverPath(String path) {
    	setAttribute(EDGE_DRIVER_PATH, path);
    }
    
    public void setIEDriverPath(String path) {
    	setAttribute(IE_DRIVER_PATH, path);
    }
    
    public void setUserAgent(String path) {
    	setAttribute(USER_AGENT, path);
    }
    
    public void setAssumeUntrustedCertificateIssuer(Boolean assume) {
    	if (assume != null) {
    		setAttribute(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER, assume);
    	} else {
    		setAttribute(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER, true);
    	}
    }
    
    public void setAcceptUntrustedCertificates(Boolean accept) {
    	if (accept != null) {
    		setAttribute(SET_ACCEPT_UNTRUSTED_CERTIFICATES, accept);
    	} else {
    		setAttribute(SET_ACCEPT_UNTRUSTED_CERTIFICATES, true);
    	}
    }
    
    public void setJavascriptEnabled(Boolean enabled) {
    	if (enabled != null) {
    		setAttribute(ENABLE_JAVASCRIPT, enabled);
    	} else {
    		setAttribute(ENABLE_JAVASCRIPT, true);
    	}
    }
   
    public void setNtlmAuthTrustedUris(String uris) {
    	setAttribute(NTLM_AUTH_TRUSTED_URIS, uris);
    }
    
    public void setBrowserDownloadDir(String downloadDir) {
    	setAttribute(BROWSER_DOWNLOAD_DIR, downloadDir);
    }
    
    public void setJsErrorCollectorExtension(Boolean enabled) {
    	if (enabled != null) {
    		setAttribute(ADD_JS_ERROR_COLLECTOR_EXTENSION, enabled);
    	} else {
    		setAttribute(ADD_JS_ERROR_COLLECTOR_EXTENSION, false);
    	}
    }
    
    public void setWebProxyType(String proxyType) {
    	try {
    		setAttribute(WEB_PROXY_TYPE, ProxyType.valueOf(proxyType.toUpperCase()));
    	} catch (NullPointerException | IllegalArgumentException e) {
    		setAttribute(WEB_PROXY_TYPE, null);
    	}
    	
    }
    
    public void setWebProxyAddress(String proxyAddress) {
    	setAttribute(WEB_PROXY_ADDRESS, proxyAddress);
    }
    
    public void setWebProxyPort(Integer port) {
    	setAttribute(WEB_PROXY_PORT, port);    	
    }
    
    public void setWebProxyLogin(String login) {
    	setAttribute(WEB_PROXY_LOGIN, login);
    }
    
    public void setWebProxyPassword(String password) {
    	setAttribute(WEB_PROXY_PASSWORD, password);
    }
    
    public void setWebProxyPac(String pacAddress) {
    	setAttribute(WEB_PROXY_PAC, pacAddress);
    }
    
    public void setWebProxyExclude(String proxyExclude) {
    	setAttribute(WEB_PROXY_EXCLUDE, proxyExclude);
    }
    
    
    
    public void setSnapshotBottomCropping(Integer crop) {
    	if (crop != null) {
    		setAttribute(SNAPSHOT_BOTTOM_CROPPING, crop);
    	} else {
    		setAttribute(SNAPSHOT_BOTTOM_CROPPING, 0);
    	}
    }
    
    public void setSnapshotTopCropping(Integer crop) {
    	if (crop != null) {
    		setAttribute(SNAPSHOT_TOP_CROPPING, crop);
    	} else {
    		setAttribute(SNAPSHOT_TOP_CROPPING, 0);
    	}
    }
    
    public void setCaptureSnapshot(Boolean capture) {
    	if (capture != null) {
    		setAttribute(CAPTURE_SNAPSHOT, capture);
    	} else {
    		setAttribute(CAPTURE_SNAPSHOT, true);
    	}
    }
    
    public void setEnableExceptionListener(Boolean enable) {
    	if (enable != null) {
    		setAttribute(ENABLE_EXCEPTION_LISTENER, enable);
    	} else {
    		setAttribute(ENABLE_EXCEPTION_LISTENER, true);
    	}
    }
    
    public void setDpTagsInclude(String tags) {
    	setAttribute(DP_TAGS_INCLUDE, tags);
    }
    
    public void setDpTagsExclude(String tags) {
    	setAttribute(DP_TAGS_EXCLUDE, tags);
    }
    
    public void setSoftAssertEnabled(Boolean enable) {
    	if (enable != null) {
    		setAttribute(SOFT_ASSERT_ENABLED, enable);
    	} else {
    		setAttribute(SOFT_ASSERT_ENABLED, true);
    	}
    }
    
    public void setWebDriverListener(String listener) {
    	setAttribute(WEB_DRIVER_LISTENER, listener);
    }
    
    public void setAppiumServerUrl(String url) {
    	setAttribute(APPIUM_SERVER_URL, url);
    }
    
    public void setDeviceName(String name) {
    	setAttribute(DEVICE_NAME, name);
    }

    public void setDeviceList(String list) {
    	if (list != null) {
    		setAttribute(DEVICE_LIST, list);
    	} else {
    		setAttribute(DEVICE_LIST, "{}");
    	}
    }
    
    public void setApp(String app) {
    	if (app != null) {
    		setAttribute(APP, app);
    	} else {
    		setAttribute(APP, "");
    	}
    }
    
    public void setCucumberTags(String tags) {
    	if (tags != null) {
    		setAttribute(CUCUMBER_TAGS, tags);
    	} else {
    		setAttribute(CUCUMBER_TAGS, "");
    	}
    }
    
    public void setCucumberTests(String tests) {
    	if (tests != null) {
    		setAttribute(CUCUMBER_TESTS, tests);
    	} else {
    		setAttribute(CUCUMBER_TESTS, "");
    	}
    }
    
    public void setCucumberImplementationPackage(String pkg) {
    	if (pkg == null && (!getCucumberTests().isEmpty() || !getCucumberTags().isEmpty())) {
    		throw new ConfigurationException("cucumberPackage parameter not defined whereas cucumberTests or cucumberTags are defined");
    	}
    	setAttribute(CUCUMBER_IMPLEMENTATION_PKG, pkg);
    }
    
    public void setTestEnv(String tests) {
    	if (tests != null) {
    		setAttribute(TEST_ENV, tests);
    	} else {
    		setAttribute(TEST_ENV, "DEV");
    	}
    	
    }

    public void setAppPackage(String pkg) {
    	setAttribute(APP_PACKAGE, pkg);
    }
    
    public void setTestMethodSignature(String signature) {
    	setAttribute(TEST_METHOD_SIGNATURE, signature);
    }

    public void setAppActivity(String name) {
    	setAttribute(APP_ACTIVITY, name);
    }

    public void setAppWaitActivity(String name) {
    	setAttribute(APP_WAIT_ACTIVITY, name);
    }
    
    public void setNewCommandTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(NEW_COMMAND_TIMEOUT, timeout);
    	} else {
    		setAttribute(NEW_COMMAND_TIMEOUT, 120);
    	}
    }
    
    public void setOutputDirectory(String outputDir, ITestContext context) {
    	if (outputDir == null) {
    		setAttribute(OUTPUT_DIRECTORY, new File(context.getOutputDirectory()).getParent());
    	} else {
    		((TestRunner)context).setOutputDirectory(outputDir);
    		setAttribute(OUTPUT_DIRECTORY, outputDir);
    	}
    }
    
    public void setVersion(String version) {
    	setAttribute(VERSION, version);
    }
    
    public void setPlatform(String platform) {
    	if (platform != null) {
    		setAttribute(PLATFORM, platform);
    	} else {
    		setAttribute(PLATFORM, Platform.getCurrent().family().toString());
    	}
    }
    
    public void setCloudApiKey(String key) {
    	setAttribute(CLOUD_API_KEY, key);
    }
    
    public void setTestType(final TestType testType) {
        setAttribute(TEST_TYPE, testType);
    }
    
    /**
     * For testdroid tests only
     * @param name
     */
    public void setProjectName(String name) {
    	setAttribute(PROJECT_NAME, name);
    }
    
    public void setMobilePlatformVersion(final String version) {
    	setAttribute(MOBILE_PLATFORM_VERSION, version);
    }
    
    public void setViewPortWidth(Integer width) {
    	setAttribute(VIEWPORT_WIDTH, width);    	
    }
    
    public void setViewPortHeight(Integer height) {
    	setAttribute(VIEWPORT_HEIGHT, height);    	
    }
    
    
    /**
     * post configuration of the context
     */
    public void postInit() {
    	postsetProxyConfig();
    	setTestConfiguration();
    }
    
    /**
     * Extract proxy settings from environment configuration and write them to context if 
     */
    public void postsetProxyConfig() {
    	Map<String, String> envConfig = new ConfigReader().readConfig();
    	for (String key: envConfig.keySet()) {
    		switch (key) {
    			case WEB_PROXY_TYPE:
    				setWebProxyType(getWebProxyType() == null ? envConfig.get(key): (getWebProxyType() == null ? null: getWebProxyType().name()));
    				break;
    			case WEB_PROXY_ADDRESS:
    				setWebProxyAddress(getWebProxyAddress() == null ? envConfig.get(key): getWebProxyAddress());
    				break;
    			case WEB_PROXY_PORT:
    				setWebProxyPort(getWebProxyPort() == null ? Integer.valueOf(envConfig.get(key)): getWebProxyPort());
    				break;
    			case WEB_PROXY_LOGIN:
    				setWebProxyLogin(getWebProxyLogin() == null ? envConfig.get(key): getWebProxyLogin());
    				break;
    			case WEB_PROXY_PASSWORD:
    				setWebProxyPassword(getWebProxyPassword() == null ? envConfig.get(key): getWebProxyPassword());
    				break;
    			case WEB_PROXY_PAC:
    				setWebProxyPac(getWebProxyPac() == null ? envConfig.get(key): getWebProxyPac());
    				break;
    			case WEB_PROXY_EXCLUDE:
    				setWebProxyExclude(getWebProxyExclude() == null ? envConfig.get(key): getWebProxyExclude());
    				break;
    			default:
    				continue;
    		}
    	}
    	
    	// set default value for proxy type if none as been set before
    	if (getWebProxyType() == null) {
    		setWebProxyType("AUTODETECT");
    	}
    }
    
    /**
     * Read configuration from environment specific data and undefined parameters present un testng xml file
     */
	public void setTestConfiguration() {
    	Map<String, String> envConfig = new ConfigReader().readConfig();
    	envConfig.putAll(testVariables);
    	setAttribute(TEST_CONFIG, envConfig);
    }

}
