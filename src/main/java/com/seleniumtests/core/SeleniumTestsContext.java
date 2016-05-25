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
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.config.ConfigReader;
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
	public static final String DATA_FOLDER_NAME = "data";

    /* configuration defined in testng.xml */
    public static final String TEST_CONFIGURATION = "testConfig"; 			// configuration annexe
    public static final String APP_URL = "appURL";					// url pour se connecter au site web
    public static final String WEB_SESSION_TIME_OUT = "webSessionTimeOut";		// timeout de la session du navigateur
    public static final String IMPLICIT_WAIT_TIME_OUT = "implicitWaitTimeOut";		// attente implicite du navigateur
    public static final String EXPLICIT_WAIT_TIME_OUT = "explicitWaitTimeOut";		// attente explicite du navigateur
    public static final String PAGE_LOAD_TIME_OUT = "pageLoadTimeout";			// temps d'attente de chargement d'une page
    public static final String WEB_DRIVER_GRID = "webDriverGrid";			// adresse du serveur seleniumGrid
    public static final String RUN_MODE = "runMode";					// local ou grid. Pourrait également contenir sauceLabs / testDroid
    public static final String BROWSER = "browser";					// navigateur utilisé
    public static final String BROWSER_VERSION = "browserVersion";			// version de navigateur utilisé
    public static final String WEB_PLATFORM = "webPlatform";				// objet Platform de selenium => Windows / Linux / ... TODO: a supprimer ?
    public static final String FIREFOX_USER_PROFILE_PATH = "firefoxUserProfilePath";	// profile utilisateur firefox
    public static final String USE_DEFAULT_FIREFOX_PROFILE = "useFirefoxDefaultProfile";// utilisation du profile firefox par défaut
    public static final String OPERA_USER_PROFILE_PATH = "operaUserProfilePath";	// profile utilisateur opéra
    public static final String FIREFOX_BINARY_PATH = "firefoxBinaryPath";		// chemin vers le binaire firefox (firefox portable ou pour utiliser une version spécifique
    public static final String CHROME_DRIVER_PATH = "chromeDriverPath";			// chemin vers chromeDriver si on souhaite utiliser une version différente
    public static final String CHROME_BINARY_PATH = "chromeBinaryPath";			// chemin vers le binaire chrome lorsque celui-ci n'est pas installé de manière normale
    public static final String IE_DRIVER_PATH = "ieDriverPath";				// chemin vers le driver Internet Explorer
    public static final String USER_AGENT = "userAgent";				// user agent utilisé pour les tests. Permet d'écraser le user-agent par défaut du navigateur, sur firefox et chrome uniquement

    public static final String Set_Assume_Untrusted_Certificate_Issuer = "setAssumeUntrustedCertificateIssuer"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides TODO: mettre à true par défaut
    public static final String Set_Accept_Untrusted_Certificates = "setAcceptUntrustedCertificates"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides TODO: mettre à true par défaut
    public static final String ENABLE_JAVASCRIPT = "enableJavascript";			// activation du javascrit dans le navigateur. TODO: true par défaut. TODO: voir si ce n'est pas uniquement pour HtmlUnit
    public static final String NTLM_AUTH_TRUSTED_URIS = "ntlmAuthTrustedUris";		// Firefox uniquement
    public static final String BROWSER_DOWNLOAD_DIR = "browserDownloadDir";		// répertoire où seront enregistrés les fichiers
    public static final String BROWSER_WINDOW_SIZE = "browserWindowSize";		// TODO: passer en plein écran par défaut
    public static final String ADD_JS_ERROR_COLLECTOR_EXTENSION = "addJSErrorCollectorExtension"; // Firefox uniquement

    public static final String WEB_PROXY_ENABLED = "webProxyEnabled";			// activation du serveur proxy pour le navigateur
    public static final String WEB_PROXY_TYPE = "webProxyType";				// type de proxy. TODO: à compléter pour prendre en charge les proxy auto et manuels
    public static final String WEB_PROXY_ADDRESS = "webProxyAddress";			// adresse du proxy. TODO: quel est le format

    public static final String TEST_ENTITY = "testEntity";				// Jamais utilisé

    public static final String REPORT_GENERATION_CONFIG = "reportGenerationConfig";
    public static final String OPEN_REPORT_IN_BROWSER = "openReportInBrowser";
    public static final String CAPTURE_SNAPSHOT = "captureSnapshot";
    public static final String ENABLE_EXCEPTION_LISTENER = "enableExceptionListener";	// TODO: voir son effet, activé par défaut

    public static final String DP_TAGS_INCLUDE = "dpTagsInclude";			// 
    public static final String DP_TAGS_EXCLUDE = "dpTagsExclude";			// Utilisé pour la lecture de fichiers CSV/XLS des DataProvider TODO: a étudier comment cela fonctionne

    public static final String SSH_COMMAND_WAIT = "sshCommandWait";
    public static final String SOFT_ASSERT_ENABLED = "softAssertEnabled";		// le test ne s'arrête pas lorsqu'une assertion est rencontrée

    public static final String OUTPUT_DIRECTORY = "outputDirectory";     		// folder where HTML report will be written
    public static final String WEB_DRIVER_LISTENER = "webDriverListener";

    public static final String TEST_METHOD_SIGNATURE = "testMethodSignature";
    public static final String PLUGIN_CONFIG_PATH = "pluginConfigPath";

    public static final String TEST_DATA_FILE = "testDataFile";

    public static final String TEST_TYPE = "testType";
    public static final String TEST_NAME = "testName";
    
    public static final String CUCUMBER_TESTS = "cucumberTests";			// liste des tests en mode cucumber
    public static final String CUCUMBER_TAGS = "cucumberTags";				// liste des tags cucumber
    public static final String VARIABLES = "variables";					// TODO: à supprimer
    public static final String TEST_CONFIG = "currentTestConfig"; 			// configuration used for the current test. It is not updated via XML file
    public static final String TEST_ENV = "env";					// environnement de test pour le SUT. Permet d'accéder aux configurations spécifiques du fichier config.ini
    public static final String CUCUMBER_IMPLEMENTATION_PKG = "cucumberPackage";		// nom du package java pour les classes cucumber, car celui-ci n'est pas accessible par testNG
    
    // Appium specific properties
    public static final String APP = "app";						// Nom de l'application utilisée en mobile
    public static final String APPIUM_SERVER_URL = "appiumServerURL";			// URL du serveur appium en local. TODO: voir s'il n'est pas possible de mutualiser avec une autre variable pour l'accès distant
    public static final String AUTOMATION_NAME = "automationName";
    public static final String MOBILE_PLATFORM_NAME = "platformName";			// Android ou iOS
    public static final String MOBILE_PLATFORM_VERSION = "mobilePlatformVersion";	// Version de l'OS mobile
    public static final String DEVICE_NAME = "deviceName";				// Nom du terminal utilisé pour le test

    public static final String BROWSER_NAME = "browserName";				// non du navigateur pour l'automatisation de tests mobiles. TODO: mutualiser avec la variable browser
    public static final String APP_PACKAGE = "appPackage";				// package de l'application
    public static final String APP_ACTIVITY = "appActivity";				// activité à démarrer (Android)
    public static final String APP_WAIT_ACTIVITY = "appWaitActivity";			// dans certains cas, l'activité qui démarre l'application n'est pas l'activité principale. C'est celle-ci qu'on attend
    public static final String NEW_COMMAND_TIMEOUT = "newCommandTimeout";		// Attente pour les commandes appium. TODO: à mutualiser avec le newSessionTiemout ?

    // Cloud specific properties
    public static final String VERSION = "version";					// TODO: différence par rapport à la version du navigateur ?
    public static final String PLATFORM = "platform";					// Platform sur laquelle se déroule un test Desktop en cloud. TODO: à mutualiser avec platformName	
    public static final String CLOUD_URL = "cloudURL";					// URL d'accès au service Cloud
    public static final String CLOUD_API_KEY = "cloudApiKey";				// clé d'accès (dépend des services)
    
    // Testdroid specific properties
    public static final String PROJECT_NAME = "projectName";				// TestDroid nécessite un nom de projet dans lequel l'automatisation aura lieu	

    private LinkedList<TearDownService> tearDownServices = new LinkedList<TearDownService>();
    private Map<ITestResult, List<Throwable>> verificationFailuresMap = new HashMap<ITestResult, List<Throwable>>();

    /* Data object to store all context data */
    private Map<String, Object> contextDataMap = Collections.synchronizedMap(new HashMap<String, Object>());

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
        	setAttribute(TEST_NAME, context.getCurrentXmlTest().getName());
        }

        setContextAttribute(context, TEST_DATA_FILE, System.getProperty(TEST_DATA_FILE), "testCase");
        setContextAttribute(context, TEST_TYPE, System.getProperty(TEST_TYPE), TestType.WEB.toString());

        setContextAttribute(context, WEB_SESSION_TIME_OUT, System.getProperty(WEB_SESSION_TIME_OUT), "90000");
        setContextAttribute(context, IMPLICIT_WAIT_TIME_OUT, System.getProperty(IMPLICIT_WAIT_TIME_OUT), "5");
        setContextAttribute(context, EXPLICIT_WAIT_TIME_OUT, System.getProperty(EXPLICIT_WAIT_TIME_OUT), "15");
        setContextAttribute(context, PAGE_LOAD_TIME_OUT, System.getProperty(PAGE_LOAD_TIME_OUT), "90");

        setContextAttribute(context, WEB_DRIVER_GRID, System.getProperty(WEB_DRIVER_GRID), null);
        setContextAttribute(context, RUN_MODE, System.getProperty(RUN_MODE), "LOCAL");
        setContextAttribute(context, BROWSER, System.getProperty(BROWSER), "*firefox");
        setContextAttribute(context, BROWSER_VERSION, System.getProperty(BROWSER_VERSION), null);
        setContextAttribute(context, WEB_PLATFORM, System.getProperty(WEB_PLATFORM), null);

        setContextAttribute(context, FIREFOX_USER_PROFILE_PATH, System.getProperty(FIREFOX_USER_PROFILE_PATH), null);
        setContextAttribute(context, USE_DEFAULT_FIREFOX_PROFILE, System.getProperty(USE_DEFAULT_FIREFOX_PROFILE),
            "true");

        setContextAttribute(context, OPERA_USER_PROFILE_PATH, System.getProperty(OPERA_USER_PROFILE_PATH), null);
        setContextAttribute(context, FIREFOX_BINARY_PATH, System.getProperty(FIREFOX_BINARY_PATH), null);
        setContextAttribute(context, CHROME_DRIVER_PATH, System.getProperty(CHROME_DRIVER_PATH), null);
        setContextAttribute(context, IE_DRIVER_PATH, System.getProperty(IE_DRIVER_PATH), null);
        setContextAttribute(context, USER_AGENT, System.getProperty(USER_AGENT), null);
        setContextAttribute(context, Set_Assume_Untrusted_Certificate_Issuer,
            System.getProperty(Set_Assume_Untrusted_Certificate_Issuer), null);
        setContextAttribute(context, Set_Accept_Untrusted_Certificates,
            System.getProperty(Set_Accept_Untrusted_Certificates), null);
        setContextAttribute(context, ENABLE_JAVASCRIPT, System.getProperty(ENABLE_JAVASCRIPT), null);
        setContextAttribute(context, NTLM_AUTH_TRUSTED_URIS, System.getProperty(NTLM_AUTH_TRUSTED_URIS), null);
        setContextAttribute(context, BROWSER_DOWNLOAD_DIR, System.getProperty(BROWSER_DOWNLOAD_DIR), null);
        setContextAttribute(context, BROWSER_WINDOW_SIZE, System.getProperty(BROWSER_WINDOW_SIZE), null);
        setContextAttribute(context, ADD_JS_ERROR_COLLECTOR_EXTENSION,
            System.getProperty(ADD_JS_ERROR_COLLECTOR_EXTENSION), "false");

        setContextAttribute(context, WEB_PROXY_ENABLED, System.getProperty(WEB_PROXY_ENABLED), "false");
        setContextAttribute(context, WEB_PROXY_TYPE, System.getProperty(WEB_PROXY_TYPE), null);
        setContextAttribute(context, WEB_PROXY_ADDRESS, System.getProperty(WEB_PROXY_ADDRESS), null);

        // Set default to summaryPerSuite, by default it would generate a summary report per suite for tests in SeleniumTestReport.html
        // if set to summaryAllSuites, only one summary report section would be generated.
        setContextAttribute(context, REPORT_GENERATION_CONFIG, System.getProperty(REPORT_GENERATION_CONFIG), "summaryPerSuite");

        setContextAttribute(context, OPEN_REPORT_IN_BROWSER, System.getProperty(OPEN_REPORT_IN_BROWSER), null);

        setContextAttribute(context, CAPTURE_SNAPSHOT, System.getProperty(CAPTURE_SNAPSHOT), null);
        setContextAttribute(context, ENABLE_EXCEPTION_LISTENER, System.getProperty(ENABLE_EXCEPTION_LISTENER), "true");

        setContextAttribute(context, DP_TAGS_INCLUDE, System.getProperty(DP_TAGS_INCLUDE), null);
        setContextAttribute(context, DP_TAGS_EXCLUDE, System.getProperty(DP_TAGS_EXCLUDE), null);

        setContextAttribute(context, SSH_COMMAND_WAIT, System.getProperty(SSH_COMMAND_WAIT), "5000");
        setContextAttribute(context, SOFT_ASSERT_ENABLED, System.getProperty(SOFT_ASSERT_ENABLED), "false");

        setContextAttribute(context, WEB_DRIVER_LISTENER, System.getProperty(WEB_DRIVER_LISTENER), null);

        setContextAttribute(context, APPIUM_SERVER_URL, System.getProperty(APPIUM_SERVER_URL), null);
        setContextAttribute(context, AUTOMATION_NAME, System.getProperty(AUTOMATION_NAME), "Appium");
        setContextAttribute(context, MOBILE_PLATFORM_NAME, System.getProperty(MOBILE_PLATFORM_NAME), "Android");
        setContextAttribute(context, MOBILE_PLATFORM_VERSION, System.getProperty(MOBILE_PLATFORM_VERSION), null);
        setContextAttribute(context, DEVICE_NAME, System.getProperty(DEVICE_NAME), null);

        setContextAttribute(context, APP, System.getProperty(APP), "");
       
        setContextAttribute(context, CUCUMBER_TAGS, System.getProperty(CUCUMBER_TAGS), "");
        setContextAttribute(context, CUCUMBER_TESTS, System.getProperty(CUCUMBER_TESTS), "");
        setContextAttribute(context, CUCUMBER_IMPLEMENTATION_PKG, System.getProperty(CUCUMBER_IMPLEMENTATION_PKG), null);
        setContextAttribute(context, VARIABLES, System.getProperty(VARIABLES), "{}");
        setContextAttribute(context, TEST_ENV, System.getProperty(TEST_ENV), "DEV");

        // By default test is assumed to be executed on default browser on android emulator
        setContextAttribute(context, BROWSER_NAME, System.getProperty(BROWSER_NAME), "Browser");
        setContextAttribute(context, APP_PACKAGE, System.getProperty(APP_PACKAGE), null);
        setContextAttribute(context, APP_ACTIVITY, System.getProperty(APP_ACTIVITY), null);
        setContextAttribute(context, APP_WAIT_ACTIVITY, System.getProperty(APP_WAIT_ACTIVITY), null);
        setContextAttribute(context, NEW_COMMAND_TIMEOUT, System.getProperty(NEW_COMMAND_TIMEOUT), "120");

        setContextAttribute(context, VERSION, System.getProperty(VERSION), null);
        setContextAttribute(context, PLATFORM, System.getProperty(PLATFORM), null);
        setContextAttribute(context, CLOUD_URL, System.getProperty(CLOUD_URL), null);
        setContextAttribute(context, CLOUD_API_KEY, System.getProperty(CLOUD_API_KEY), null);
        setContextAttribute(context, PROJECT_NAME, System.getProperty(PROJECT_NAME), null);

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

    public String getAddJSErrorCollectorExtension() {
        return (String) getAttribute(ADD_JS_ERROR_COLLECTOR_EXTENSION);
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

    public String getBrowserWindowSize() {
        return (String) getAttribute(BROWSER_WINDOW_SIZE);
    }

    public boolean getCaptureSnapshot() {
        if (getAttribute(CAPTURE_SNAPSHOT) == null) {

            // IE grid default value set to false
            if (this.getWebRunMode().equalsIgnoreCase("ExistingGrid")
                    && (this.getWebRunBrowser().contains("iexplore") || this.getWebRunBrowser().contains("safari"))) {
                this.setAttribute(CAPTURE_SNAPSHOT, "false");
            } else {
                this.setAttribute(CAPTURE_SNAPSHOT, "true");
            }
        }

        return Boolean.parseBoolean((String) getAttribute(CAPTURE_SNAPSHOT));
    }

    public boolean getEnableExceptionListener() {
        return Boolean.parseBoolean((String) getAttribute(ENABLE_EXCEPTION_LISTENER));
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
            timeout = Integer.parseInt((String) getAttribute(EXPLICIT_WAIT_TIME_OUT));
        } catch (Exception e) {
            timeout = 15;
        }

        if (timeout < getImplicitWaitTimeout()) {
            return (int) getImplicitWaitTimeout();
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

    public double getImplicitWaitTimeout() {
        try {
            return Double.parseDouble((String) getAttribute(IMPLICIT_WAIT_TIME_OUT));
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

    public String getOperaUserProfilePath() {
        return (String) getAttribute(OPERA_USER_PROFILE_PATH);
    }

    public String getOutputDirectory() {
        return (String) getAttribute(OUTPUT_DIRECTORY);
    }

    public int getPageLoadTimeout() {
        try {
            return Integer.parseInt((String) getAttribute(PAGE_LOAD_TIME_OUT));
        } catch (Exception e) {
            return 90;
        }
    }

    public String getWebPlatform() {
        return (String) getAttribute(WEB_PLATFORM);
    }

    public String getAppURL() {
        return (String) getAttribute(APP_URL);
    }

    public int getSshCommandWait() {
        try {
            return Integer.parseInt((String) getAttribute(SSH_COMMAND_WAIT));
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

    public String getTestType() {
        return (String) getAttribute(TEST_TYPE);
    }
    
    public String getTestName() {
    	return (String) getAttribute(TEST_NAME);
    }

    public String getCucumberTags() {
    	return (String) getAttribute(CUCUMBER_TAGS);
    }
    
    public List<String> getCucmberTests() {
    	List<String> tests = new ArrayList<String>();
    	for (String test: ((String)getAttribute(CUCUMBER_TESTS)).replace("\"", "").replace("&nbsp;", " ").split(",")) {
    		tests.add(test.trim());
    	}
    	return tests;
    }
    
    public String getCucmberPkg() {
    	return (String) getAttribute(CUCUMBER_IMPLEMENTATION_PKG);
    }
    
    public String getTestEnvironment() {
    	return (String) getAttribute(TEST_ENV);
    }
    
    public String getVariables() {
    	return (String) getAttribute(VARIABLES);
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

    public String getWebRunBrowser() {
        return (String) getAttribute(BROWSER);
    }

    public String getWebRunMode() {
        return (String) getAttribute(RUN_MODE);
    }

    public int getWebSessionTimeout() {
        try {
            return Integer.parseInt((String) getAttribute(WEB_SESSION_TIME_OUT));
        } catch (Exception e) {
            return 90000; // Default
        }
    }

    public String getAppiumServerURL() {
        return (String) getAttribute(APPIUM_SERVER_URL);
    }

    public String getAutomationName() {
        return (String) getAttribute(AUTOMATION_NAME);
    }

    public String getMobilePlatformName() {
        return (String) getAttribute(MOBILE_PLATFORM_NAME);
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

    public String getBrowserName() {
        return (String) getAttribute(BROWSER_NAME);
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

    public String getNewCommandTimeout() {
        return (String) getAttribute(NEW_COMMAND_TIMEOUT);
    }

    public String getVersion() {
        return (String) getAttribute(VERSION);
    }

    public String getPlatform() {
        return (String) getAttribute(PLATFORM);
    }

    public String getCloudURL() {
        return (String) getAttribute(CLOUD_URL);
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

    public boolean isUseFirefoxDefaultProfile() {
        try {
            return Boolean.parseBoolean((String) getAttribute(USE_DEFAULT_FIREFOX_PROFILE));
        } catch (Exception e) {
            return true; // Default
        }

    }

    public boolean isSoftAssertEnabled() {
        try {
            return Boolean.parseBoolean((String) getAttribute(SOFT_ASSERT_ENABLED));
        } catch (Exception e) {
            return false; // Default
        }
    }

    public boolean isWebProxyEnabled() {
        try {
            return Boolean.parseBoolean((String) getAttribute(WEB_PROXY_ENABLED));
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
            Map<String, String> testParameters = context.getSuite().getXmlSuite().getParameters();

            for (Entry<String, String> entry : testParameters.entrySet()) {
                String attributeName = entry.getKey();

                
                if (!contextDataMap.containsKey(entry.getKey())) {
                    String sysPropertyValue = System.getProperty(entry.getKey());
                    String suiteValue = entry.getValue();
                    setContextAttribute(attributeName, sysPropertyValue, suiteValue, null);
                }

            }
        }

    }

    /**
     * Set Suite SeleniumTestsContext Attributes.
     *
     * @param  context
     * @param  attributeName
     * @param  sysPropertyValue
     * @param  defaultValue
     */

    private void setContextAttribute(final ITestContext context, final String attributeName,
            final String sysPropertyValue, final String defaultValue) {
        String suiteValue = null;
        if (context != null && context.getCurrentXmlTest() != null) {
        	String testValue = context.getCurrentXmlTest().getParameter(attributeName);
        	if (testValue == null) {
        		suiteValue = context.getCurrentXmlTest().getSuite().getParameter(attributeName);
        	} else {
        		suiteValue = testValue;
        	}
        }

        contextDataMap.put(attributeName,
            (sysPropertyValue != null ? sysPropertyValue : (suiteValue != null ? suiteValue : defaultValue)));
    }

    private void setContextAttribute(final String attributeName, final String sysPropertyValue, final String suiteValue,
            final String defaultValue) {

        contextDataMap.put(attributeName,
            (sysPropertyValue != null ? sysPropertyValue : (suiteValue != null ? suiteValue : defaultValue)));

    }

    public void setExplicitWaitTimeout(final double timeout) {
        setAttribute(EXPLICIT_WAIT_TIME_OUT, timeout);
    }

    public void setImplicitWaitTimeout(final double timeout) {
        setAttribute(IMPLICIT_WAIT_TIME_OUT, timeout);
    }

    public void setPageLoadTimeout(final int timeout) {
        setAttribute(PAGE_LOAD_TIME_OUT, timeout);
    }

    public void setTestDataFile(final String testDataFile) {
        setAttribute(TEST_DATA_FILE, testDataFile);
    }

    public void setTestType(final String testType) {
        setAttribute(TEST_TYPE, testType);
    }
    
    @SuppressWarnings("unchecked")
	public void setTestConfiguration() {
    	Map<String, String> testConfig;
		try {
			testConfig = new ConfigReader().readConfig(FileUtils.openInputStream(new File(CONFIG_PATH + File.separator + "config.ini")));
		} catch (IOException e1) {
			TestLogging.warning("no valid config.ini file for this application");
			testConfig = new HashMap<String, String>();
		}
    	if (getVariables() != null) {
			try {
				JSONObject json = new JSONObject(getVariables());
				for (String key: (Set<String>)json.keySet()) {
					testConfig.put(key, json.getString(key));
				}
			} catch (JSONException e) {
				TestLogging.warning("le format des variables n'est pas une chaine JSON valide");
			}
		}	
    	setAttribute(TEST_CONFIG, testConfig);
    }

}
