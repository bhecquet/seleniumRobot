/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.config.ConfigReader;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.ScreenShot;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.reporter.PluginsHelper;

/**
 * Defines TestNG context used in STF.
 */
public class SeleniumTestsContext {
	
	// folder config
	public static String ROOT_PATH;
	public static String DATA_PATH;
	public static String FEATURES_PATH;
	public static String CONFIG_PATH;
	public static String APPLICATION_NAME;
	public static HashMap<String, HashMap<String,String>> ID_MAPPING;
	public static final String DATA_FOLDER_NAME = "data";

    /* configuration defined in testng.xml */
    public static final String TEST_CONFIGURATION = "testConfig"; 				// configuration annexe
    public static final String DEVICE_LIST = "deviceList"; 						// List of known devices in json format (internal use only)
    public static final String WEB_SESSION_TIME_OUT = "webSessionTimeOut";		// timeout de la session du navigateur
    public static final String IMPLICIT_WAIT_TIME_OUT = "implicitWaitTimeOut";	// attente implicite du navigateur
    public static final String EXPLICIT_WAIT_TIME_OUT = "explicitWaitTimeOut";	// attente explicite du navigateur
    public static final String PAGE_LOAD_TIME_OUT = "pageLoadTimeout";			// temps d'attente de chargement d'une page
    public static final String WEB_DRIVER_GRID = "webDriverGrid";				// adresse du serveur seleniumGrid
    public static final String RUN_MODE = "runMode";							// local ou grid. Pourrait également contenir sauceLabs / testDroid
    public static final String BROWSER = "browser";								// navigateur utilisé. Sur Android, le navigateur par défaut est "Browser"
    public static final String BROWSER_VERSION = "browserVersion";				// version de navigateur utilisé
    public static final String FIREFOX_USER_PROFILE_PATH = "firefoxUserProfilePath";	// profile utilisateur firefox
    public static final String USE_DEFAULT_FIREFOX_PROFILE = "useFirefoxDefaultProfile";// utilisation du profile firefox par défaut
    public static final String OPERA_USER_PROFILE_PATH = "operaUserProfilePath";	// profile utilisateur opéra
    public static final String FIREFOX_BINARY_PATH = "firefoxBinaryPath";		// chemin vers le binaire firefox (firefox portable ou pour utiliser une version spécifique
    public static final String CHROME_DRIVER_PATH = "chromeDriverPath";			// chemin vers chromeDriver si on souhaite utiliser une version différente
    public static final String CHROME_BINARY_PATH = "chromeBinaryPath";			// chemin vers le binaire chrome lorsque celui-ci n'est pas installé de manière normale
    public static final String IE_DRIVER_PATH = "ieDriverPath";					// chemin vers le driver Internet Explorer
    public static final String USER_AGENT = "userAgent";						// user agent utilisé pour les tests. Permet d'écraser le user-agent par défaut du navigateur, sur firefox et chrome uniquement

    public static final String Set_Assume_Untrusted_Certificate_Issuer = "setAssumeUntrustedCertificateIssuer"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides 
    public static final String Set_Accept_Untrusted_Certificates = "setAcceptUntrustedCertificates"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides
    public static final String ENABLE_JAVASCRIPT = "enableJavascript";			// activation du javascrit dans le navigateur.
    public static final String NTLM_AUTH_TRUSTED_URIS = "ntlmAuthTrustedUris";	// Firefox uniquement
    public static final String BROWSER_DOWNLOAD_DIR = "browserDownloadDir";		// répertoire où seront enregistrés les fichiers
    public static final String ADD_JS_ERROR_COLLECTOR_EXTENSION = "addJSErrorCollectorExtension"; // Firefox uniquement

    public static final String WEB_PROXY_ENABLED = "webProxyEnabled";			// activation du serveur proxy pour le navigateur
    public static final String WEB_PROXY_TYPE = "webProxyType";					// type de proxy. TODO: à compléter pour prendre en charge les proxy auto et manuels
    public static final String WEB_PROXY_ADDRESS = "webProxyAddress";			// adresse du proxy. TODO: quel est le format

    public static final String TEST_ENTITY = "testEntity";						// Jamais utilisé

    public static final String REPORT_GENERATION_CONFIG = "reportGenerationConfig";
    public static final String OPEN_REPORT_IN_BROWSER = "openReportInBrowser";
    public static final String CAPTURE_SNAPSHOT = "captureSnapshot";
    public static final String ENABLE_EXCEPTION_LISTENER = "enableExceptionListener";	// TODO: voir son effet, activé par défaut

    public static final String DP_TAGS_INCLUDE = "dpTagsInclude";				// 
    public static final String DP_TAGS_EXCLUDE = "dpTagsExclude";				// Utilisé pour la lecture de fichiers CSV/XLS des DataProvider TODO: a étudier comment cela fonctionne

    public static final String SSH_COMMAND_WAIT = "sshCommandWait";
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
    public static final String TEST_ENV = "env";								// environnement de test pour le SUT. Permet d'accéder aux configurations spécifiques du fichier config.ini
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
    public static final String VERSION = "version";								// TODO: différence par rapport à la version du navigateur ?
    public static final String PLATFORM = "platform";							// platform on which test should execute. Ex: Windows 7, Android, iOS, Linux, OS X 10.10. TODO: parse platform to add version for mobile	
    public static final String CLOUD_API_KEY = "cloudApiKey";					// clé d'accès (dépend des services)
    
    // Testdroid specific properties
    public static final String PROJECT_NAME = "projectName";					// TestDroid nécessite un nom de projet dans lequel l'automatisation aura lieu	

    private LinkedList<TearDownService> tearDownServices = new LinkedList<TearDownService>();
    private Map<ITestResult, List<Throwable>> verificationFailuresMap = new HashMap<ITestResult, List<Throwable>>();

    /* Data object to store all context data */
    private Map<String, Object> contextDataMap = Collections.synchronizedMap(new HashMap<String, Object>());
    private Map<String, String> testVariables = Collections.synchronizedMap(new HashMap<String, String>());

    private ITestContext testNGContext = null;

    private LinkedList<ScreenShot> screenshots = new LinkedList<ScreenShot>();
    

    public LinkedList<ScreenShot> getScreenshots() {
        return screenshots;
    }

    public void addScreenShot(final ScreenShot screenShot) {
        deleteExceptionSnapshots();
        screenshots.addLast(screenShot);
    }

    private void deleteExceptionSnapshots() {
        try {
            int size = screenshots.size();
            if (size == 0) {
                return;
            }

            ScreenShot screenShot = screenshots.get(size - 1);
            if (screenShot.isException() && screenShot.getFullImagePath() != null) {
                new File(screenShot.getFullImagePath()).delete();
                screenshots.remove(size - 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ScreenShot getExceptionScreenShot() {
        if (screenshots.size() > 0 && screenshots.getLast().isException()) {
            return screenshots.getLast();
        } else {
            return null;
        }
    }
    
    /**
     * Build the root path of STF 
     * method for guessing it is different if we are inside a jar (built mode) or in development
     * @param clazz
     * @param path
     * @return
     */
    private static Boolean getPathFromClass(Class clazz, StringBuilder path) {
		Boolean jar = false;
		
		try {
			String url = URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8" );
			if (url.endsWith(".jar")) {
				path.append((new File(url).getParentFile().getAbsoluteFile().toString() + "/").replace(File.separator, "/"));
				jar = true;
			} else {				
				path.append((new File(url).getParentFile().getParentFile().getAbsoluteFile().toString() + "/").replace(File.separator, "/"));
				jar = false;
			}
		} catch (UnsupportedEncodingException e) {}
		
		return jar;
	}
	
    
	private static void generateApplicationPath(XmlSuite xmlSuite) {

		StringBuilder path = new StringBuilder();
		getPathFromClass(SeleniumTestsContext.class, path);
		
		ROOT_PATH = path.toString();
		
		// in case launching unit test from eclipse, a temp file is generated outside the standard folder structure
		// APPLICATION_NAME and DATA_PATH must be rewritten
		try {
			APPLICATION_NAME = xmlSuite.getFileName().replace(File.separator, "/").split("/"+ DATA_FOLDER_NAME + "/")[1].split("/")[0];
			DATA_PATH = xmlSuite.getFileName().replace(File.separator, "/").split("/"+ DATA_FOLDER_NAME + "/")[0] + "/" + DATA_FOLDER_NAME + "/";
		} catch (IndexOutOfBoundsException e) {
			APPLICATION_NAME = "core";
			DATA_PATH = Paths.get(ROOT_PATH, "data").toString();
		}
		
		FEATURES_PATH = Paths.get(DATA_PATH, APPLICATION_NAME, "features").toString();
		CONFIG_PATH = Paths.get(DATA_PATH, APPLICATION_NAME, "config").toString();
		
		// create data folder if it does not exist (it should already exist)
		if (!new File(DATA_PATH).isDirectory()) {
			new File(DATA_PATH).mkdirs();
		}
	}

    public SeleniumTestsContext(final ITestContext context) {
        this.testNGContext = context;
        
        // initialize folders
        if (context != null && context.getCurrentXmlTest() != null) {
        	generateApplicationPath(context.getCurrentXmlTest().getSuite());
        }

        setTestDataFile(getValueForTest(TEST_DATA_FILE, System.getProperty(TEST_DATA_FILE)));
        setWebSessionTimeout(getIntValueForTest(WEB_SESSION_TIME_OUT, System.getProperty(WEB_SESSION_TIME_OUT)));
        setImplicitWaitTimeout(getIntValueForTest(IMPLICIT_WAIT_TIME_OUT, System.getProperty(IMPLICIT_WAIT_TIME_OUT)));
        setExplicitWaitTimeout(getIntValueForTest(EXPLICIT_WAIT_TIME_OUT, System.getProperty(EXPLICIT_WAIT_TIME_OUT)));
        setPageLoadTimeout(getIntValueForTest(PAGE_LOAD_TIME_OUT, System.getProperty(PAGE_LOAD_TIME_OUT)));
        setWebDriverGrid(getValueForTest(WEB_DRIVER_GRID, System.getProperty(WEB_DRIVER_GRID)));
        setRunMode(getValueForTest(RUN_MODE, System.getProperty(RUN_MODE)));
        setBrowser(getValueForTest(BROWSER, System.getProperty(BROWSER)));
        setBrowserVersion(getValueForTest(BROWSER_VERSION, System.getProperty(BROWSER_VERSION)));
        setFirefoxUserProfilePath(getValueForTest(FIREFOX_USER_PROFILE_PATH, System.getProperty(FIREFOX_USER_PROFILE_PATH)));
        setUseDefaultFirefoxProfile(getBoolValueForTest(USE_DEFAULT_FIREFOX_PROFILE, System.getProperty(USE_DEFAULT_FIREFOX_PROFILE)));
        setOperaUserProfilePath(getValueForTest(OPERA_USER_PROFILE_PATH, System.getProperty(OPERA_USER_PROFILE_PATH)));
        setFirefoxBinary(getValueForTest(FIREFOX_BINARY_PATH, System.getProperty(FIREFOX_BINARY_PATH)));
        setChromeDriverPath(getValueForTest(CHROME_DRIVER_PATH, System.getProperty(CHROME_DRIVER_PATH)));
        setIEDriverPath(getValueForTest(IE_DRIVER_PATH, System.getProperty(IE_DRIVER_PATH)));
        setUserAgent(getValueForTest(USER_AGENT, System.getProperty(USER_AGENT)));
        setAssumeUntrustedCertificateIssuer(getBoolValueForTest(Set_Assume_Untrusted_Certificate_Issuer, System.getProperty(Set_Assume_Untrusted_Certificate_Issuer)));
        setAcceptUntrustedCertificates(getBoolValueForTest(Set_Accept_Untrusted_Certificates, System.getProperty(Set_Accept_Untrusted_Certificates)));
        setJavascriptEnabled(getBoolValueForTest(ENABLE_JAVASCRIPT, System.getProperty(ENABLE_JAVASCRIPT)));
        setNtlmAuthTrustedUris(getValueForTest(NTLM_AUTH_TRUSTED_URIS, System.getProperty(NTLM_AUTH_TRUSTED_URIS)));
        setBrowserDownloadDir(getValueForTest(BROWSER_DOWNLOAD_DIR, System.getProperty(BROWSER_DOWNLOAD_DIR)));
   
        setJsErrorCollectorExtension(getBoolValueForTest(ADD_JS_ERROR_COLLECTOR_EXTENSION, System.getProperty(ADD_JS_ERROR_COLLECTOR_EXTENSION)));

        setWebProxyEnabled(getBoolValueForTest(WEB_PROXY_ENABLED, System.getProperty(WEB_PROXY_ENABLED)));
        setProxyType(getValueForTest(WEB_PROXY_TYPE, System.getProperty(WEB_PROXY_TYPE)));
        setProxyAddress(getValueForTest(WEB_PROXY_ADDRESS, System.getProperty(WEB_PROXY_ADDRESS)));

        // Set default to summaryPerSuite, by default it would generate a summary report per suite for tests in SeleniumTestReport.html
        // if set to summaryAllSuites, only one summary report section would be generated.
        setReportGenerationConfig(getValueForTest(REPORT_GENERATION_CONFIG, System.getProperty(REPORT_GENERATION_CONFIG)));

        setOpenReportInBrowser(getValueForTest(OPEN_REPORT_IN_BROWSER, System.getProperty(OPEN_REPORT_IN_BROWSER)));

        setCaptureSnapshot(getBoolValueForTest(CAPTURE_SNAPSHOT, System.getProperty(CAPTURE_SNAPSHOT)));
        setEnableExceptionListener(getBoolValueForTest(ENABLE_EXCEPTION_LISTENER, System.getProperty(ENABLE_EXCEPTION_LISTENER)));

        setDpTagsInclude(getValueForTest(DP_TAGS_INCLUDE, System.getProperty(DP_TAGS_INCLUDE)));
        setDpTagsExclude(getValueForTest(DP_TAGS_EXCLUDE, System.getProperty(DP_TAGS_EXCLUDE)));

        setSshCommandWait(getIntValueForTest(SSH_COMMAND_WAIT, System.getProperty(SSH_COMMAND_WAIT)));
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
        
        // determines test_type according to input configuration
        configureTestType();

        // get mobile platform version if one is defined in device list
        updateDeviceMobileVersion();
        
        // get mobile platform version
        updatePlatformVersion();
        
        if (context != null) {
            setContextAttribute(OUTPUT_DIRECTORY, null, context.getOutputDirectory(), null);

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
	    	Platform currentPlatform = Platform.fromString(getPlatform()).family();
	    	if (currentPlatform.equals(Platform.WINDOWS) 
	    		|| currentPlatform.equals(Platform.MAC) 
	    		|| currentPlatform.equals(Platform.UNIX) ) {
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
    			throw new ConfigurationException(String.format("Platform %s has not been recognized as a valide platform", getPlatform()));
    		}
    	}
    }
    
    /**
     * Configure test type according to platform, browser and app parameters
     */
    private void configureTestType() {
    	if (getPlatform().toLowerCase().startsWith("android")) {
        	if (getApp().isEmpty()) { // a browser name should be defined
        		setAttribute(TEST_TYPE, TestType.APPIUM_WEB_ANDROID);
        	} else {
        		setAttribute(TEST_TYPE, TestType.APPIUM_APP_ANDROID);
        	}
        } else if (getPlatform().toLowerCase().startsWith("ios")) {
        	if (getApp().isEmpty()) { // a browser name should be defined
        		setAttribute(TEST_TYPE, TestType.APPIUM_WEB_IOS);
        	} else {
        		setAttribute(TEST_TYPE, TestType.APPIUM_APP_IOS);
        	}
        } else {
        	if (getBrowser().isEmpty()) {
        		setAttribute(TEST_TYPE, TestType.NON_GUI);
        	} else {
        		setAttribute(TEST_TYPE, TestType.WEB);
        	}
        }
    }
    
    /**
     * Search mobile platform version according to device name if one has been defined in testConfig file
     */
    private void updateDeviceMobileVersion() {
    	HashMap<String, String> deviceList = getDeviceList();
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
            ArrayList<Throwable> failures = new ArrayList<Throwable>();
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

    public boolean getCaptureSnapshot() {
        if (getAttribute(CAPTURE_SNAPSHOT) == null) {

            // IE grid default value set to false
            if (this.getRunMode().equalsIgnoreCase("ExistingGrid")
                    && (this.getBrowser().contains("iexplore") || this.getBrowser().contains("safari"))) {
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

    public String getNtlmAuthTrustedUris() {
        return (String) getAttribute(NTLM_AUTH_TRUSTED_URIS);
    }

    public String getReportGenerationConfig() {
        return (String) getAttribute(REPORT_GENERATION_CONFIG);
    }

    public String getOpenReportInBrowser() {
        return (String) getAttribute(OPEN_REPORT_IN_BROWSER);
    }
	
	public Boolean getAssumeUntrustedCertificateIssuer() {
        return (Boolean) getAttribute(Set_Assume_Untrusted_Certificate_Issuer);
    }
	
	public Boolean getJavascriptEnabled() {
		return (Boolean) getAttribute(ENABLE_JAVASCRIPT);
	}

	public Boolean getAcceptUntrustedCertificates() {
        return (Boolean) getAttribute(Set_Accept_Untrusted_Certificates);
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

    public int getSshCommandWait() {
        try {
            return (Integer) getAttribute(SSH_COMMAND_WAIT);
        } catch (Exception e) {
            return 5000; // Default
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
    public LinkedList<TearDownService> getTearDownServices() {
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
    	List<String> tests = new ArrayList<String>();
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
        return verificationFailures == null ? new ArrayList<Throwable>() : verificationFailures;

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

    public String getWebProxyType() {
        return (String) getAttribute(WEB_PROXY_TYPE);
    }

    public String getBrowser() {
        return (String) getAttribute(BROWSER);
    }

    public String getRunMode() {
        return (String) getAttribute(RUN_MODE);
    }
    
    @SuppressWarnings("unchecked")
	public HashMap<String, String> getDeviceList() {
    	HashMap<String, String> deviceList = new HashMap<String, String>();
    	if (getAttribute(DEVICE_LIST) == null || getAttribute(DEVICE_LIST).equals("{}")) {
    		return deviceList;
    	}
    	
    	JSONObject devList = new JSONObject((String)getAttribute(DEVICE_LIST));
    	for (String key: ((Set<String>)devList.keySet())) {
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
    
    public HashMap<String, String> getConfiguration() {
    	return (HashMap<String, String>) getAttribute(TEST_CONFIG);
    }
    
    //Methods for ID_Mapping
    //get
    public HashMap<String, HashMap<String, String>> getIdMapping() {
    	return (HashMap<String, HashMap<String,String>>) ID_MAPPING;
    }
    
    //set
    public void setIdMapping(HashMap<String, HashMap<String,String>> conf){
    	ID_MAPPING = conf;
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

    public boolean isWebProxyEnabled() {
        try {
            return (Boolean) getAttribute(WEB_PROXY_ENABLED);
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
        		
        		// if test parameter does not exist, loot at suite parameter
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

    private void setContextAttribute(final String attributeName, final String sysPropertyValue, final String suiteValue,
            final String defaultValue) {

        contextDataMap.put(attributeName,
            (sysPropertyValue != null ? sysPropertyValue : (suiteValue != null ? suiteValue : defaultValue)));

    }

    public void setTestDataFile(String testDataFile) {
    	if (testDataFile == null) {
    		testDataFile = "testCase";
    	}
        setAttribute(TEST_DATA_FILE, testDataFile);
    }
    
    public void setWebSessionTimeout(Integer timeout) {
    	if (timeout == null) {
    		timeout = 90000;
    	}
    	setAttribute(WEB_SESSION_TIME_OUT, timeout);
    }

    public void setImplicitWaitTimeout(Integer timeout) {
    	if (timeout == null) {
    		timeout = 5;
    	}
        setAttribute(IMPLICIT_WAIT_TIME_OUT, timeout);
    }
    
    public void setExplicitWaitTimeout(Integer timeout) {
    	if (timeout == null) {
    		timeout = 15;
    	}
        setAttribute(EXPLICIT_WAIT_TIME_OUT, timeout);
    }

    public void setPageLoadTimeout(Integer timeout) {
    	if (timeout == null) {
    		timeout = 90;
    	}
        setAttribute(PAGE_LOAD_TIME_OUT, timeout);
    }
    
    public void setWebDriverGrid(final String driverGrid) {
        setAttribute(WEB_DRIVER_GRID, driverGrid);
    }
    
    public void setRunMode(String runMode) {
    	if (runMode == null) {
    		runMode = "LOCAL";
    	} 
    	DriverMode.fromString(runMode);

        setAttribute(RUN_MODE, runMode);
    }

    public void setBrowser(String browser) {
    	if (browser == null) {
    		browser = "*firefox";
    	} 
    	BrowserType.getBrowserType(browser);
    	setAttribute(BROWSER, browser);
    }
    
    public void setBrowserVersion(String browserVersion) {
    	setAttribute(BROWSER_VERSION, browserVersion);
    }
    
    public void setFirefoxUserProfilePath(String ffPath) {
    	setAttribute(FIREFOX_USER_PROFILE_PATH, ffPath);
    }
    
    public void setUseDefaultFirefoxProfile(Boolean useDefaultffProfile) {
		if (useDefaultffProfile == null) {
			useDefaultffProfile = true;
    	}
    	setAttribute(USE_DEFAULT_FIREFOX_PROFILE, useDefaultffProfile);
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
    
    public void setIEDriverPath(String path) {
    	setAttribute(IE_DRIVER_PATH, path);
    }
    
    public void setUserAgent(String path) {
    	setAttribute(USER_AGENT, path);
    }
    
    public void setAssumeUntrustedCertificateIssuer(Boolean assume) {
    	if (assume == null) {
    		assume = true;
    	}
    	setAttribute(Set_Assume_Untrusted_Certificate_Issuer, assume);
    }
    
    public void setAcceptUntrustedCertificates(Boolean accept) {
    	if (accept == null) {
    		accept = true;
    	}
    	setAttribute(Set_Accept_Untrusted_Certificates, accept);
    }
    
    public void setJavascriptEnabled(Boolean enabled) {
    	if (enabled == null) {
    		enabled = true;
    	}
    	setAttribute(ENABLE_JAVASCRIPT, enabled);
    }
   
    public void setNtlmAuthTrustedUris(String uris) {
    	setAttribute(NTLM_AUTH_TRUSTED_URIS, uris);
    }
    
    public void setBrowserDownloadDir(String downloadDir) {
    	setAttribute(BROWSER_DOWNLOAD_DIR, downloadDir);
    }
    
    public void setJsErrorCollectorExtension(Boolean enabled) {
    	if (enabled == null) {
    		enabled = false;
    	}
    	setAttribute(ADD_JS_ERROR_COLLECTOR_EXTENSION, enabled);
    }
   
    public void setWebProxyEnabled(Boolean enabled) {
    	if (enabled == null) {
    		enabled = false;
    	}
    	setAttribute(WEB_PROXY_ENABLED, enabled);
    }
    
    public void setProxyType(String proxyType) {
    	setAttribute(WEB_PROXY_TYPE, proxyType);
    }
    
    public void setProxyAddress(String proxyAddress) {
    	setAttribute(WEB_PROXY_ADDRESS, proxyAddress);
    }
    
    public void setReportGenerationConfig(String config) {
    	if (config == null) {
    		config = "summaryPerSuite";
    	}
    	setAttribute(REPORT_GENERATION_CONFIG, config);
    }
    
    public void setOpenReportInBrowser(String browserName) {
    	setAttribute(OPEN_REPORT_IN_BROWSER, browserName);
    }
    
    public void setCaptureSnapshot(Boolean capture) {
    	if (capture == null) {
    		capture = true;
    	}
    	setAttribute(CAPTURE_SNAPSHOT, capture);
    }
    
    public void setEnableExceptionListener(Boolean enable) {
    	if (enable == null) {
    		enable = true;
    	}
    	setAttribute(ENABLE_EXCEPTION_LISTENER, enable);
    }
    
    public void setDpTagsInclude(String tags) {
    	setAttribute(DP_TAGS_INCLUDE, tags);
    }
    
    public void setDpTagsExclude(String tags) {
    	setAttribute(DP_TAGS_EXCLUDE, tags);
    }
    
    public void setSshCommandWait(Integer waitInMs) {
    	if (waitInMs == null) {
    		waitInMs = 5000;
    	}
    	setAttribute(SSH_COMMAND_WAIT, waitInMs);
    }
    
    public void setSoftAssertEnabled(Boolean enable) {
    	if (enable == null) {
    		enable = true;
    	}
    	setAttribute(SOFT_ASSERT_ENABLED, enable);
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
    	if (list == null) {
    		list = "{}";
    	}
    	setAttribute(DEVICE_LIST, list);
    }
    
    public void setApp(String app) {
    	if (app == null) {
    		app = "";
    	}
    	setAttribute(APP, app);
    }
    
    public void setCucumberTags(String tags) {
    	if (tags == null) {
    		tags = "";
    	}
    	setAttribute(CUCUMBER_TAGS, tags);
    }
    
    public void setCucumberTests(String tests) {
    	if (tests == null) {
    		tests = "";
    	}
    	setAttribute(CUCUMBER_TESTS, tests);
    }
    
    public void setCucumberImplementationPackage(String pkg) {
    	if (pkg == null && (!getCucumberTests().isEmpty() || !getCucumberTags().isEmpty())) {
    		throw new ConfigurationException("cucumberPackage parameter not defined whereas cucumberTests or cucumberTags are defined");
    	}
    	setAttribute(CUCUMBER_IMPLEMENTATION_PKG, pkg);
    }
    
    public void setTestEnv(String tests) {
    	if (tests == null) {
    		tests = "DEV";
    	}
    	setAttribute(TEST_ENV, tests);
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
    	if (timeout == null) {
    		timeout = 120;
    	}
        setAttribute(NEW_COMMAND_TIMEOUT, timeout);
    }
    
    public void setVersion(String version) {
    	setAttribute(VERSION, version);
    }
    
    public void setPlatform(String platform) {
    	if (platform == null) {
    		platform = Platform.getCurrent().toString();
    	}
        setAttribute(PLATFORM, platform);
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
    
    /**
     * Read configuration from environment specific data and undefined parameters present un testng xml file
     */
	public void setTestConfiguration() {
    	Map<String, String> testConfig;
		try {
			testConfig = new ConfigReader().readConfig(FileUtils.openInputStream(new File(CONFIG_PATH + File.separator + "config.ini")));
		} catch (IOException e1) {
			TestLogging.warning("no valid config.ini file for this application");
			testConfig = new HashMap<String, String>();
		}
		
		testConfig.putAll(testVariables);
    	setAttribute(TEST_CONFIG, testConfig);
    }

}
