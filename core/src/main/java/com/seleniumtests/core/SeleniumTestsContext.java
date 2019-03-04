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
package com.seleniumtests.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestRunner;
import org.testng.internal.TestResult;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.core.config.ConfigReader;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverExceptionListener;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.screenshots.VideoCaptureMode;
import com.seleniumtests.reporter.PluginsHelper;
import com.seleniumtests.reporter.logger.ArchiveMode;
import com.seleniumtests.reporter.reporters.ReportInfo;
import com.seleniumtests.uipage.htmlelements.ElementInfo;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;

/**
 * Defines TestNG context used in STF.
 */
public class SeleniumTestsContext {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumTestsContext.class);
	private static Map<String, String> outputFolderNames = Collections.synchronizedMap(new HashMap<>());

    /* configuration defined in testng.xml */
    public static final String TEST_CONFIGURATION = "testConfig"; 				// parameter name for additional configuration to load (should only be used in XML)
    public static final String LOAD_INI = "loadIni";							// comma separated list of files to load. They are searched in data/<app>/config folder. They will append to env.ini file with variable overwriting. Last file will overwrite previous ones
    
    public static final String DEVICE_LIST = "deviceList"; 						// List of known devices in json format (internal use only)
    public static final String WEB_SESSION_TIME_OUT = "webSessionTimeOut";		// timeout de la session du navigateur
    public static final String IMPLICIT_WAIT_TIME_OUT = "implicitWaitTimeOut";	// attente implicite du navigateur
    public static final String EXPLICIT_WAIT_TIME_OUT = "explicitWaitTimeOut";	// attente explicite du navigateur
    public static final String HEADLESS_BROWSER = "headless";
    public static final String REPLAY_TIME_OUT = "replayTimeOut";				// time during which an action is replayed. By default 30 secs
    public static final String PAGE_LOAD_TIME_OUT = "pageLoadTimeout";			// temps d'attente de chargement d'une page
    public static final String WEB_DRIVER_GRID = "webDriverGrid";				// adresse du serveur seleniumGrid
    public static final String RUN_MODE = "runMode";							// local ou grid. Pourrait également contenir sauceLabs / testDroid
    public static final String NODE_TAGS = "nodeTags";							// Comma seperated list of strings. Requests that this test should execute only on a node (grid mode only) announcing all of these tags (issue #190)
    public static final String MASK_PASSWORD = "maskPassword";					// whether seleniumRobot should hide passwords or not
    public static final String MANUAL_TEST_STEPS = "manualTestSteps";			// set test steps manual (default is false) for creating them inside tests
    public static final String DEBUG = "debug";									// whether to debug test (logs from browser / core). Valid values are: 'none', 'core', 'driver' or 'core,driver'
    public static final String INTERNAL_DEBUG = "internalDebug";
    public static final String BROWSER = "browser";								// navigateur utilisé. Sur Android, le navigateur par défaut est "Browser"
    public static final String BROWSER_VERSION = "browserVersion";				// version de navigateur utilisé
    public static final String FIREFOX_USER_PROFILE_PATH = "firefoxUserProfilePath";	// profile utilisateur firefox
    public static final String USE_DEFAULT_FIREFOX_PROFILE = "useFirefoxDefaultProfile";// utilisation du profile firefox par défaut
    public static final String OPERA_USER_PROFILE_PATH = "operaUserProfilePath";	// profile utilisateur opéra
    public static final String FIREFOX_BINARY_PATH = "firefoxBinaryPath";		// chemin vers le binaire firefox (firefox portable ou pour utiliser une version spécifique
    public static final String CHROME_DRIVER_PATH = "chromeDriverPath";			// chemin vers chromeDriver si on souhaite utiliser une version différente
    public static final String GECKO_DRIVER_PATH = "geckoDriverPath";			// chemin vers chromeDriver si on souhaite utiliser une version différente
    public static final String EDGE_DRIVER_PATH = "edgeDriverPath";				// path to Edge driver binary if we want to use an other version than the provided one
    public static final String CHROME_BINARY_PATH = "chromeBinaryPath";			// chemin vers le binaire chrome lorsque celui-ci n'est pas installé de manière normale
    public static final String IE_DRIVER_PATH = "ieDriverPath";					// chemin vers le driver Internet Explorer
    public static final String USER_AGENT = "userAgent";						// user agent utilisé pour les tests. Permet d'écraser le user-agent par défaut du navigateur, sur firefox et chrome uniquement

    public static final String VIEWPORT_WIDTH = "viewPortWidth";					// width of viewport	
    public static final String VIEWPORT_HEIGHT = "viewPortHeight";					// height of viewport
    
    public static final String ADVANCED_ELEMENT_SEARCH = "advancedElementSearch";	// 'false' (default), 'full', 'dom'. if 'dom', store and possibly use found element information to adapt to changes in DOM, using only DOM information. If element is not found, it will try to use other element information to find it
    																				// if 'full', search will also be done using element picture, if available
    
    // selenium robot server parameters
    public static final String SELENIUMROBOTSERVER_URL = "seleniumRobotServerUrl";
    public static final String SELENIUMROBOTSERVER_ACTIVE = "seleniumRobotServerActive";
    public static final String SELENIUMROBOTSERVER_COMPARE_SNAPSHOT = "seleniumRobotServerCompareSnapshots";			// whether we should use the snapshots created by robot to compare them to a previous execution. This option only operates when SeleniumRobot server is connected
    public static final String SELENIUMROBOTSERVER_RECORD_RESULTS = "seleniumRobotServerRecordResults";				// whether we should record test results to server. This option only operates when SeleniumRobot server is connected
    public static final String SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN = "seleniumRobotServerVariablesOlderThan";	// whether we should get from server variables which were created at least X days ago
    
    public static final String SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER = "setAssumeUntrustedCertificateIssuer"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides 
    public static final String SET_ACCEPT_UNTRUSTED_CERTIFICATES = "setAcceptUntrustedCertificates"; // Firefox uniquement pour qu'il ne prenne pas en compte les certificats invalides
    public static final String ENABLE_JAVASCRIPT = "enableJavascript";			// activation du javascrit dans le navigateur.
    public static final String NTLM_AUTH_TRUSTED_URIS = "ntlmAuthTrustedUris";	// Firefox uniquement
    public static final String BROWSER_DOWNLOAD_DIR = "browserDownloadDir";		// répertoire où seront enregistrés les fichiers

    public static final String SNAPSHOT_TOP_CROPPING = "snapshotTopCropping";
    public static final String SNAPSHOT_BOTTOM_CROPPING = "snapshotBottomCropping";
    
    public static final String WEB_PROXY_TYPE = "proxyType";					// type de proxy. AUTO, MANUAL, NO
    public static final String WEB_PROXY_TYPE_FROM_USER = "proxyTypeFromUser";	// issue #158: proxy type as requested by user. Store it 
    public static final String WEB_PROXY_ADDRESS = "proxyAddress";				// adresse du proxy. 
    public static final String WEB_PROXY_PORT = "proxyPort";					// port du proxy
    public static final String WEB_PROXY_LOGIN = "proxyLogin";					// login du proxy (si nécessaire)
    public static final String WEB_PROXY_PASSWORD = "proxyPassword";			// mot de passe du proxy (si nécessaire)
    public static final String WEB_PROXY_EXCLUDE = "proxyExclude";				// exclusion des adresse de proxy
    public static final String WEB_PROXY_PAC = "proxyPac";						// adresse de configuration automatique du proxy

    public static final String TEST_ENTITY = "testEntity";						// Jamais utilisé

    public static final String CAPTURE_SNAPSHOT = "captureSnapshot";
    public static final String CAPTURE_NETWORK = "captureNetwork";
    public static final String VIDEO_CAPTURE = "captureVideo";
    public static final String ENABLE_EXCEPTION_LISTENER = "enableExceptionListener";	// TODO: voir son effet, activé par défaut

    public static final String DP_TAGS_INCLUDE = "dpTagsInclude";				// 
    public static final String DP_TAGS_EXCLUDE = "dpTagsExclude";				// Utilisé pour la lecture de fichiers CSV/XLS des DataProvider TODO: a étudier comment cela fonctionne

    public static final String SOFT_ASSERT_ENABLED = "softAssertEnabled";		// le test ne s'arrête pas lorsqu'une assertion est rencontrée
    public static final String TEST_RETRY_COUNT = "testRetryCount";				// number of times the test can be retried
    
    public static final String OVERRIDE_SELENIUM_NATIVE_ACTION = "overrideSeleniumNativeAction";	// intercept driver.findElement and driver.frame operations to move to HtmlElement methods 
    
    public static final String OUTPUT_DIRECTORY = "outputDirectory";     		// folder where HTML report will be written
    public static final String DEFAULT_OUTPUT_DIRECTORY = "defaultOutputDirectory";    // folder where TestNG would write it's results if not overwritten
    public static final String CUSTOM_TEST_REPORTS = "customTestReports";
    public static final String CUSTOM_SUMMARY_REPORTS = "customSummaryReports";
    public static final String ARCHIVE_TO_FILE = "archiveToFile";				// path to the file where archive will be done.
    public static final String ARCHIVE = "archive";								// whether archiving is done. DEfault is false, other values are 'true', 'onSuccess', 'onError'
    
    public static final String WEB_DRIVER_LISTENER = "webDriverListener";
    public static final String OPTIMIZE_REPORTS = "optimizeReports";

    public static final String TEST_METHOD_SIGNATURE = "testMethodSignature";
    public static final String PLUGIN_CONFIG_PATH = "pluginConfigPath";

    public static final String TEST_TYPE = "testType";							// configured automatically
    public static final String TMS_RUN = "tmsRun";									// option for configuring test (from test management system point of view) currently running, like HP ALM or Squash TM
    public static final String TMS_CONNECT = "tmsConnect";						// option for configuring test management system, like HP ALM or Squash TM (url, user, password, ...)
    
    public static final String CUCUMBER_TESTS = "cucumberTests";				// liste des tests en mode cucumber
    public static final String CUCUMBER_TAGS = "cucumberTags";					// liste des tags cucumber
    public static final String TEST_ENV = "env";								// environnement de test pour le SUT. Permet d'accéder aux configurations spécifiques du fichier env.ini
    public static final String CUCUMBER_IMPLEMENTATION_PKG = "cucumberPackage";	// nom du package java pour les classes cucumber, car celui-ci n'est pas accessible par testNG
    
    // Appium specific properties
    public static final String APP = "app";										// Chemin de l'application mobile (local ou distant)
    public static final String MOBILE_PLATFORM_VERSION = "mobilePlatformVersion";// Mobile OS version. It's deduced from platform name and not read directly from parameters
    public static final String DEVICE_NAME = "deviceName";						// Nom du terminal utilisé pour le test
    public static final String FULL_RESET = "fullReset";						// whether we should do a full reset (default is true)

    public static final String APP_PACKAGE = "appPackage";						// package de l'application
    public static final String APP_ACTIVITY = "appActivity";					// activité à démarrer (Android)
    public static final String APP_WAIT_ACTIVITY = "appWaitActivity";			// dans certains cas, l'activité qui démarre l'application n'est pas l'activité principale. C'est celle-ci qu'on attend
    public static final String NEW_COMMAND_TIMEOUT = "newCommandTimeout";		// Attente maximale entre 2 commandes envoyées à appium

    // Cloud specific properties
    public static final String VERSION = "version";								// browser version
    public static final String PLATFORM = "platform";							// platform on which test should execute. Ex: Windows 7, Android, iOS, Linux, OS X 10.10. 	
    public static final String CLOUD_API_KEY = "cloudApiKey";					// clé d'accès (dépend des services)

    public static final String TEST_NAME = "testName";
    public static final String RELATIVE_OUTPUT_DIR = "relativeOutputDir";
    
    // Neoload specific properties
    public static final String NEOLOAD_USER_PATH = "neoloadUserPath";			// name of the neoload "user path" that will be created in Design mode
    
    // internal storage
    public static final String TEST_VARIABLES = "testVariables"; 				// configuration (aka variables, get via 'param()' method) used for the current test. It is not updated via XML file
    																		
    // default values
    public static final List<ReportInfo> DEFAULT_CUSTOM_TEST_REPORTS = Arrays.asList(new ReportInfo("PERF::xml::reporter/templates/report.perf.vm"));
    public static final List<ReportInfo> DEFAULT_CUSTOM_SUMMARY_REPORTS = Arrays.asList(new ReportInfo("results::json::reporter/templates/report.summary.json.vm"));
	public static final int DEFAULT_NEW_COMMAND_TIMEOUT = 120;
	public static final String DEFAULT_TEST_ENV = "DEV";
	public static final String DEFAULT_CUCUMBER_TESTS = "";
	public static final String DEFAULT_CUCUMBER_TAGS = "";
	public static final String DEFAULT_APP = "";
	public static final String DEFAULT_DEVICE_LIST = "{}";
	public static final boolean DEFAULT_SOFT_ASSERT_ENABLED = true;
	public static final boolean DEFAULT_ENABLE_EXCEPTION_LISTENER = true;
	public static final boolean DEFAULT_CAPTURE_SNAPSHOT = true;
	public static final boolean DEFAULT_CAPTURE_NETWORK = false;
	public static final String DEFAULT_VIDEO_CAPTURE = "onError";
	public static final int DEFAULT_SNAPSHOT_TOP_CROPPING = 0;
	public static final int DEFAULT_SNAPSHOT_BOTTOM_CROPPING = 0;
	public static final boolean DEFAULT_ENABLE_JAVASCRIPT = true;
	public static final boolean DEFAULT_SET_ACCEPT_UNTRUSTED_CERTIFICATES = true;
	public static final boolean DEFAULT_SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER = true;
	public static final boolean DEFAULT_USE_DEFAULT_FIREFOX_PROFILE = true;
	public static final String DEFAULT_BROWSER = "none";
	public static final boolean DEFAULT_MANUAL_TEST_STEPS = false;
	public static final boolean DEFAULT_HEADLESS_BROWSER = false;
	public static final boolean DEFAULT_MASK_PASSWORD = true;
	public static final String DEFAULT_RUN_MODE = "LOCAL";
	public static final boolean DEFAULT_OVERRIDE_SELENIUM_NATIVE_ACTION = false;
	public static final boolean DEFAULT_SELENIUMROBOTSERVER_RECORD_RESULTS = false;
	public static final boolean DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT = false;
	public static final boolean DEFAULT_SELENIUMROBOTSERVER_ACTIVE = false;
	public static final int DEFAULT_SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN = 0;
	public static final int DEFAULT_PAGE_LOAD_TIME_OUT = 90;
	public static final int DEFAULT_EXPLICIT_WAIT_TIME_OUT = 15;
	public static final int DEFAULT_IMPLICIT_WAIT_TIME_OUT = 5;
	public static final int DEFAULT_WEB_SESSION_TIMEOUT = 90000;
	public static final int DEFAULT_TEST_RETRY_COUNT = 2;
	public static final String DEFAULT_SELENIUMROBOTSERVER_URL = null;
	public static final JSONObject DEFAULT_TMS_RUN = new JSONObject();
	public static final JSONObject DEFAULT_TMS_CONNECT = new JSONObject();
	public static final ProxyType DEFAULT_WEB_PROXY_TYPE = ProxyType.AUTODETECT;
	public static final boolean DEFAULT_OPTIMIZE_REPORTS = false;
	public static final String DEFAULT_ARCHIVE= "false";
	public static final String DEFAULT_NODE_TAGS = "";
	public static final String DEFAULT_DEBUG = "none";
	public static final ElementInfo.Mode DEFAULT_ADVANCED_ELEMENT_SEARCH = ElementInfo.Mode.FALSE;
    
    public static final int DEFAULT_REPLAY_TIME_OUT = 30;

	

	// group of fields below must be copied in SeleniumTestsContext constructor because they are not rediscovered with 'configureContext' method
    // Data object to store all context data
    private Map<String, Object> contextDataMap = Collections.synchronizedMap(new HashMap<String, Object>());
    private String baseOutputDirectory; // the 'test-output' folder if not overridden
    private ITestContext testNGContext = null;
    private ITestResult testNGResult = null;
    private Map<ITestResult, List<Throwable>> verificationFailuresMap = new HashMap<>();
    
    private SeleniumRobotVariableServerConnector variableServer;
    private SeleniumGridConnector seleniumGridConnector;
    private List<SeleniumGridConnector> seleniumGridConnectors;
    private TestManager testManagerIntance;
    private boolean driverCreationBlocked = false;		// if true, inside this thread, driver creation will be forbidden
    
    // folder config
 	private Map<String, HashMap<String,String>> idMapping;
    
    public SeleniumTestsContext() {
    	// for test purpose only
    	variableServer = null;
    	seleniumGridConnector = null;
    	seleniumGridConnectors = new ArrayList<>();
    	testManagerIntance = null;
    }
    
    /**
     * Create a new context from this one. This does copy only TestNG context and data map / variables
     * @param toCopy		the context to copy in this one
     */
    public SeleniumTestsContext(SeleniumTestsContext toCopy) {
    	contextDataMap = new HashMap<>(toCopy.contextDataMap); 
    	testNGContext = toCopy.testNGContext;
    	testNGResult = toCopy.testNGResult;
    	baseOutputDirectory = toCopy.baseOutputDirectory;
    	verificationFailuresMap = new HashMap<>(toCopy.verificationFailuresMap);
    }
    
    public SeleniumTestsContext(final ITestContext context) {
        testNGContext = context;
        buildContextFromConfig();
    }
    
    private void buildContextFromConfig() {
    	setConfiguration(new HashMap<>());
    	
        setSeleniumRobotServerUrl(getValueForTest(SELENIUMROBOTSERVER_URL, System.getProperty(SELENIUMROBOTSERVER_URL)));
        setSeleniumRobotServerActive(getBoolValueForTest(SELENIUMROBOTSERVER_ACTIVE, System.getProperty(SELENIUMROBOTSERVER_ACTIVE)));
        setSeleniumRobotServerCompareSnapshot(getBoolValueForTest(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, System.getProperty(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT)));
        setSeleniumRobotServerRecordResults(getBoolValueForTest(SELENIUMROBOTSERVER_RECORD_RESULTS, System.getProperty(SELENIUMROBOTSERVER_RECORD_RESULTS)));
        setSeleniumRobotServerVariableOlderThan(getIntValueForTest(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN, System.getProperty(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN)));
        
        setWebDriverGrid(getValueForTest(WEB_DRIVER_GRID, System.getProperty(WEB_DRIVER_GRID)));
        setRunMode(getValueForTest(RUN_MODE, System.getProperty(RUN_MODE)));   
        setNodeTags(getValueForTest(NODE_TAGS, System.getProperty(NODE_TAGS)));   
        
        setMaskPassword(getBoolValueForTest(MASK_PASSWORD, System.getProperty(MASK_PASSWORD)));       
        setLoadIni(getValueForTest(LOAD_INI, System.getProperty(LOAD_INI)));
        setWebSessionTimeout(getIntValueForTest(WEB_SESSION_TIME_OUT, System.getProperty(WEB_SESSION_TIME_OUT)));
        setImplicitWaitTimeout(getIntValueForTest(IMPLICIT_WAIT_TIME_OUT, System.getProperty(IMPLICIT_WAIT_TIME_OUT)));
        setExplicitWaitTimeout(getIntValueForTest(EXPLICIT_WAIT_TIME_OUT, System.getProperty(EXPLICIT_WAIT_TIME_OUT)));
        setReplayTimeout(getIntValueForTest(REPLAY_TIME_OUT, System.getProperty(REPLAY_TIME_OUT)));
        setPageLoadTimeout(getIntValueForTest(PAGE_LOAD_TIME_OUT, System.getProperty(PAGE_LOAD_TIME_OUT)));
        setDebug(getValueForTest(DEBUG, System.getProperty(DEBUG)));
        setManualTestSteps(getBoolValueForTest(MANUAL_TEST_STEPS, System.getProperty(MANUAL_TEST_STEPS)));
        setBrowser(getValueForTest(BROWSER, System.getProperty(BROWSER)));
        setHeadlessBrowser(getBoolValueForTest(HEADLESS_BROWSER, System.getProperty(HEADLESS_BROWSER)));
        setBrowserVersion(getValueForTest(BROWSER_VERSION, System.getProperty(BROWSER_VERSION)));
        setFirefoxUserProfilePath(getValueForTest(FIREFOX_USER_PROFILE_PATH, System.getProperty(FIREFOX_USER_PROFILE_PATH)));
        setUseDefaultFirefoxProfile(getBoolValueForTest(USE_DEFAULT_FIREFOX_PROFILE, System.getProperty(USE_DEFAULT_FIREFOX_PROFILE)));
        setOperaUserProfilePath(getValueForTest(OPERA_USER_PROFILE_PATH, System.getProperty(OPERA_USER_PROFILE_PATH)));
        setFirefoxBinary(getValueForTest(FIREFOX_BINARY_PATH, System.getProperty(FIREFOX_BINARY_PATH)));
        setChromeBinary(getValueForTest(CHROME_BINARY_PATH, System.getProperty(CHROME_BINARY_PATH)));
        setChromeDriverPath(getValueForTest(CHROME_DRIVER_PATH, System.getProperty(CHROME_DRIVER_PATH)));
        setGeckoDriverPath(getValueForTest(GECKO_DRIVER_PATH, System.getProperty(GECKO_DRIVER_PATH)));
        setEdgeDriverPath(getValueForTest(EDGE_DRIVER_PATH, System.getProperty(EDGE_DRIVER_PATH)));
        setIEDriverPath(getValueForTest(IE_DRIVER_PATH, System.getProperty(IE_DRIVER_PATH)));
        setUserAgent(getValueForTest(USER_AGENT, System.getProperty(USER_AGENT)));
        setAssumeUntrustedCertificateIssuer(getBoolValueForTest(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER, System.getProperty(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER)));
        setAcceptUntrustedCertificates(getBoolValueForTest(SET_ACCEPT_UNTRUSTED_CERTIFICATES, System.getProperty(SET_ACCEPT_UNTRUSTED_CERTIFICATES)));
        setJavascriptEnabled(getBoolValueForTest(ENABLE_JAVASCRIPT, System.getProperty(ENABLE_JAVASCRIPT)));
        setNtlmAuthTrustedUris(getValueForTest(NTLM_AUTH_TRUSTED_URIS, System.getProperty(NTLM_AUTH_TRUSTED_URIS)));
        setBrowserDownloadDir(getValueForTest(BROWSER_DOWNLOAD_DIR, System.getProperty(BROWSER_DOWNLOAD_DIR)));
   
        setOverrideSeleniumNativeAction(getBoolValueForTest(OVERRIDE_SELENIUM_NATIVE_ACTION, System.getProperty(OVERRIDE_SELENIUM_NATIVE_ACTION)));

        setAdvancedElementSearch(getValueForTest(ADVANCED_ELEMENT_SEARCH, System.getProperty(ADVANCED_ELEMENT_SEARCH)));
        
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
        setCaptureNetwork(getBoolValueForTest(CAPTURE_NETWORK, System.getProperty(CAPTURE_NETWORK)));
        setVideoCapture(getValueForTest(VIDEO_CAPTURE, System.getProperty(VIDEO_CAPTURE)));
        setEnableExceptionListener(getBoolValueForTest(ENABLE_EXCEPTION_LISTENER, System.getProperty(ENABLE_EXCEPTION_LISTENER)));

        setDpTagsInclude(getValueForTest(DP_TAGS_INCLUDE, System.getProperty(DP_TAGS_INCLUDE)));
        setDpTagsExclude(getValueForTest(DP_TAGS_EXCLUDE, System.getProperty(DP_TAGS_EXCLUDE)));

        setSoftAssertEnabled(getBoolValueForTest(SOFT_ASSERT_ENABLED, System.getProperty(SOFT_ASSERT_ENABLED)));
        setTestRetryCount(getIntValueForTest(TEST_RETRY_COUNT, System.getProperty(TEST_RETRY_COUNT)));

        setWebDriverListener(getValueForTest(WEB_DRIVER_LISTENER, System.getProperty(WEB_DRIVER_LISTENER)));
        setTmsRun(getValueForTest(TMS_RUN, System.getProperty(TMS_RUN)));
        setTmsConnect(getValueForTest(TMS_CONNECT, System.getProperty(TMS_CONNECT)));

        setDeviceName(getValueForTest(DEVICE_NAME, System.getProperty(DEVICE_NAME)));
        setDeviceList(getValueForTest(DEVICE_LIST, null));
        setFullReset(getBoolValueForTest(FULL_RESET, System.getProperty(FULL_RESET)));

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
        
        setCustomTestReports(getValueForTest(CUSTOM_TEST_REPORTS, System.getProperty(CUSTOM_TEST_REPORTS)));
        setCustomSummaryReports(getValueForTest(CUSTOM_SUMMARY_REPORTS, System.getProperty(CUSTOM_SUMMARY_REPORTS)));
        setArchiveToFile(getValueForTest(ARCHIVE_TO_FILE, System.getProperty(ARCHIVE_TO_FILE)));
        setArchive(getValueForTest(ARCHIVE, System.getProperty(ARCHIVE)));
        setOptimizeReports(getBoolValueForTest(OPTIMIZE_REPORTS, System.getProperty(OPTIMIZE_REPORTS)));
        
        setViewPortWidth(getIntValueForTest(VIEWPORT_WIDTH, System.getProperty(VIEWPORT_WIDTH)));
        setViewPortHeight(getIntValueForTest(VIEWPORT_HEIGHT, System.getProperty(VIEWPORT_HEIGHT)));
        
        setNeoloadUserPath(getValueForTest(NEOLOAD_USER_PATH, System.getProperty(NEOLOAD_USER_PATH)));
        
        if (testNGContext != null) {
        	
        	// this value will be overwritten for thread context by a call to "configureContext"
        	setOutputDirectory(getValueForTest(OUTPUT_DIRECTORY, System.getProperty(OUTPUT_DIRECTORY)), testNGContext, true);
        	baseOutputDirectory = getOutputDirectory();

            // parse other parameters that are defined in testng xml or as user parameters but not defined
            // in this context. Called here so that user defined parameters can be accessed inside @Before / @After annotated methods
            setTestConfiguration();
        }
    }
    
    /**
     * If chrome or firefox binary is redefined in parameter, update list of installed browser so
     * that it matches those really used
     */
    private void updateInstalledBrowsers() {
    	Map<BrowserType, List<BrowserInfo>> installedBrowsers = OSUtility.getInstalledBrowsersWithVersion();
    	
    	if (getFirefoxBinPath() != null) {
    		String version = OSUtility.getFirefoxVersion(getFirefoxBinPath());
    		installedBrowsers.get(BrowserType.FIREFOX).add(new BrowserInfo(BrowserType.FIREFOX, version, getFirefoxBinPath()));
    	}
    	
    	if (getChromeBinPath() != null) {
    		String version = OSUtility.getChromeVersion(getChromeBinPath());
    		installedBrowsers.get(BrowserType.CHROME).add(new BrowserInfo(BrowserType.CHROME, version, getChromeBinPath()));
    	}
    }
    
    /**
     * update test data according to platform
     * @param platform
     */
    public void updateTestAndMobile(String platform) {
    	
    	setPlatform(platform);
    	
    	 // determines test_type according to input configuration
        configureTestType();

        // get mobile platform version if one is defined in device list and a deviceName is set in parameters
        updateDeviceMobileVersion();
        
        // get mobile platform version
        updatePlatformVersion();
    }
    
    /**
     * From platform name, in case of Desktop platform, do nothing and in case of mobile, extract OS version from name
     * as platformName will be 'Android 5.0' for example
     *
     * @throws ConfigurationException 	in mobile, if version is not present
     */
    private void updatePlatformVersion() {
    	try {
	    	Platform currentPlatform = Platform.fromString(getPlatform());
	    	if (currentPlatform.is(Platform.WINDOWS) 
	    		|| currentPlatform.is(Platform.MAC) 
	    		|| currentPlatform.is(Platform.UNIX)
	    		|| currentPlatform.is(Platform.ANY) && getRunMode() == DriverMode.GRID) {
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
		    		return;
	    		} catch (IndexOutOfBoundsException x) {
	    			setMobilePlatformVersion(null);
	    			logger.warn("For mobile platform, platform name should contain version. Ex: 'Android 5.0' or 'iOS 9.1'. Else, first found device is used");
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
    		setPlatform(deviceList.get(getDeviceName()));
    	}
    }
    
    // TODO: this call should be moved into postInit method as SeleniumRobotVariableServerConnector calls SeleniumTestsContextManager.getThreadContext() which may not be initialized
    private SeleniumRobotVariableServerConnector connectSeleniumRobotServer() {
    	
    	if (testNGResult == null) {
    		return null;
    	}
    	
    	// in case we find the url of variable server and it's marked as active, use it
		if (getSeleniumRobotServerActive() != null && getSeleniumRobotServerActive() && getSeleniumRobotServerUrl() != null) {
			logger.info(String.format("%s key found, and set to true, trying to get variable from variable server %s [%s]", 
						SELENIUMROBOTSERVER_ACTIVE, 
						getSeleniumRobotServerUrl(),
						SELENIUMROBOTSERVER_URL));
			SeleniumRobotVariableServerConnector vServer = new SeleniumRobotVariableServerConnector(getSeleniumRobotServerActive(), getSeleniumRobotServerUrl(), TestNGResultUtils.getTestName(testNGResult));
			
			if (!vServer.isAlive()) {
				throw new ConfigurationException(String.format("Variable server %s could not be contacted", getSeleniumRobotServerUrl()));
			}
			
			return vServer;
			
		} else {
			logger.info(String.format("%s key not found or set to false, or url key %s has not been set", SELENIUMROBOTSERVER_ACTIVE, SELENIUMROBOTSERVER_URL));
			return null;
		}
    }
    
    /**
     * returns the selenium grid connector if mode requests it
     * @return
     */
    private List<SeleniumGridConnector> connectGrid() {
    	if (getRunMode() == DriverMode.GRID) {
    		if (getWebDriverGrid() != null && !getWebDriverGrid().isEmpty()) {
    			return SeleniumGridConnectorFactory.getInstances(getWebDriverGrid());
    		} else {
    			throw new ConfigurationException("Test should be executed with Selenium Grid but URL is not set");
    		}
    	} else {
    		return null;
    	}
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

       
    /**
     * Get all JVM properties and filters the java one so that only user defined JVM arguments are returned
     * @return
     */
    public Map<String, String> getCommandLineProperties() {
	    Map<String, String> props = new HashMap<>();
	    for (Entry<Object,Object> entry: System.getProperties().entrySet()) {
	    	String key = entry.getKey().toString();
	    	if (key.startsWith("java.") 
	    			|| key.startsWith("sun.")
	    			|| key.startsWith("user.")
	    			|| key.startsWith("os.")
	    			|| key.startsWith("file.")
	    			|| key.startsWith("awt.")
	    			|| "line.separator".equals(key)
	    			|| "jnidispatch.path".equals(key)
	    			) {
	    		continue;
	    	}
	    	props.put(key, System.getProperty(key));
	    }
	    return props;
    }
    
    /**
     * Returns list of variables defined by user from command line
     * @return
     */
    private Map<String, TestVariable> getUserDefinedVariablesFromCommandLine() {
    	return extractCustomVariables(getCommandLineProperties());
    }
    
    /**
     * Returns list of variables defined by user from TestNG XML file
     * @return
     */
    private Map<String, TestVariable> getUserDefinedVariablesFromXMLFile() {
    	if (testNGContext != null) {
        	Map<String, String> testParameters;
        	if (testNGContext.getCurrentXmlTest() == null) {
        		testParameters = testNGContext.getSuite().getXmlSuite().getParameters();
        	} else {
        		testParameters = testNGContext.getCurrentXmlTest().getAllParameters();
        	}

            return extractCustomVariables(testParameters);
        }
    	return new HashMap<>();
    }
    
    /**
     * from a list of variables, extract those who are variables created by user (not seleniumRobot technical variables)
     * If parameter is already known in contextDataMap (technical parameters defined in this class), it's not added 
     * @param parameters
     */
    private Map<String, TestVariable> extractCustomVariables(Map<String, String> parameters) {
    	Map<String, TestVariable> variables = new HashMap<>();
    	
    	for (Entry<String, String> entry : parameters.entrySet()) {
            String attributeName = entry.getKey();

            // contextDataMap already contains all technical parameters
            if (!contextDataMap.containsKey(entry.getKey())) {
                variables.put(attributeName, new TestVariable(attributeName, entry.getValue()));
            }
        }
    	
    	return variables;
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
    	String value = null;
        if (testNGContext != null) {
        	
        	// default suite value, even if currentXmlTest is null
        	value = testNGContext.getSuite().getXmlSuite().getParameter(attributeName);
        	
        	if (testNGContext.getCurrentXmlTest() != null) {
        	
	        	// priority given to test parameter
	        	String testValue = testNGContext.getCurrentXmlTest().getParameter(attributeName);
	        	
	        	if (testValue == null) {
	        		
	        		// if test parameter does not exist, look at suite parameter
	        		value = testNGContext.getCurrentXmlTest().getSuite().getParameter(attributeName);
	        		
	        	} else {
	        		value = testValue;
	        	}
        	}
        }
        
        return sysPropertyValue != null ? sysPropertyValue : value;
    }
    
    /**
     * Return an int value from test parameters
     * @param attributeName
     * @param sysPropertyValue
     * @return
     */
    private Integer getIntValueForTest(final String attributeName, final String sysPropertyValue) {
    	String value = getValueForTest(attributeName, sysPropertyValue);
    	try {
    		return value == null ? null: Integer.parseInt(value);
    	} catch (NumberFormatException e) {
    		throw new ConfigurationException(String.format("Option [%s] value should be integer or null, found [%s]", attributeName, value));
    	}
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
   
    /**
     * Connect all servers (grid, seleniumRobot server) to this context
     */
    private void createContextConnectors() {

        // create seleniumRobot server instance
        variableServer = connectSeleniumRobotServer();

        // create selenium grid connectors. They will be created if it's null
        // in this phase, we chek that grid is alive
        getSeleniumGridConnectors();
        
        // create Test Manager connector
    	testManagerIntance = initTestManager();
    }
    
    /**
     * Created the directory specific to this test. It must be unique even if the same test is executed twice
     * So the created directory is 'test-ouput/<test_name>-<index>'
     */
    private void createTestSpecificOutputDirectory(final ITestResult testNGResult) {
    	String testOutputFolderName = hashTest(testNGResult);
    	
    	// use base directory as it's fixed along the life of the test
		Path outputDir = Paths.get(baseOutputDirectory, testOutputFolderName);
		setRelativeOutputDir(testOutputFolderName);
		setOutputDirectory(outputDir.toString(), testNGContext, false);

    }
    
    /**
     * Returns the name of the folder where all files (result, snapshots) will be stored inside 'test-output' folder
     * By default, folder name is the name of the test. But if the same test is executed twice, with for example DataProvider or through different suites / tests, 
     * then increment a suffix so that there is no collision between test results
     * @param testNGResult
     * @return
     */
    private String hashTest(final ITestResult testNGResult) {
    	String uniqueIdentifier = TestNGResultUtils.getHashForTest(testNGResult);
    	String testNameModified = StringUtility.replaceOddCharsFromFileName(TestNGResultUtils.getTestName(testNGResult));
    	
    	if (!outputFolderNames.containsKey(uniqueIdentifier)) {
    		if (!outputFolderNames.values().contains(testNameModified)) {
    			outputFolderNames.put(uniqueIdentifier, testNameModified);
    		} else {
	    		int i = 0;
	    		while (i++ < 1000) {
	    			if (!outputFolderNames.values().contains(testNameModified + "-" + i)) {
	        			outputFolderNames.put(uniqueIdentifier, testNameModified + "-" + i);
	        			break;
	    			}
	    		}
    		}
    	}
    	return outputFolderNames.get(uniqueIdentifier);
    }
    
    /**
     * post configuration of the context
     * This should be done only inside the test method as we need the 'Test' method result and not an 'Before' or 'After' method result
     */
    public void configureContext(final ITestResult testNGResult) {
    	
    	// to do before creating connectors because seleniumRobot server needs it
    	this.testNGResult = testNGResult; 
    	
    	// context may be missing from testNgResult, so add it to avoid problems when getting hash for test
    	if (testNGResult.getTestContext() == null) {
    		((TestResult)testNGResult).setContext(testNGContext);
    	}

        updateTestAndMobile(getPlatform());
        
        // update browser version: replace installed one with those given in parameters
        updateInstalledBrowsers();
        
        // update ouput directory
        createTestSpecificOutputDirectory(testNGResult);
      
        // load pageloading plugins
        String path = (String) getAttribute(PLUGIN_CONFIG_PATH);
        if (path != null && path.trim().length() > 0) {
            File configFile = new File(path);
            if (configFile.exists()) {
                PluginsHelper.getInstance().loadPlugins(configFile);
            }
        }

    	createContextConnectors();
    	
        // read and set test configuration from env.ini file and from seleniumRobot server
    	setTestConfiguration();
    	updateProxyConfig();
    }
    
    /**
     * Extract proxy settings from environment configuration (env.ini) and write them to context if they are not already present in XML file or on command line
     */
    public void updateProxyConfig() {
    	Map<String, TestVariable> envConfig = getConfiguration();
    	for (Entry<String, TestVariable> entry: envConfig.entrySet()) {
    		String key = entry.getKey();
    		switch (key) {
    			case WEB_PROXY_TYPE:
    				setWebProxyType(getAttribute(WEB_PROXY_TYPE_FROM_USER) == null ? envConfig.get(key).getValue(): getWebProxyType().name());
    				break;
    			case WEB_PROXY_ADDRESS:
    				setWebProxyAddress(getWebProxyAddress() == null ? envConfig.get(key).getValue(): getWebProxyAddress());
    				break;
    			case WEB_PROXY_PORT:
    				setWebProxyPort(getWebProxyPort() == null ? Integer.valueOf(envConfig.get(key).getValue()): getWebProxyPort());
    				break;
    			case WEB_PROXY_LOGIN:
    				setWebProxyLogin(getWebProxyLogin() == null ? envConfig.get(key).getValue(): getWebProxyLogin());
    				break;
    			case WEB_PROXY_PASSWORD:
    				setWebProxyPassword(getWebProxyPassword() == null ? envConfig.get(key).getValue(): getWebProxyPassword());
    				break;
    			case WEB_PROXY_PAC:
    				setWebProxyPac(getWebProxyPac() == null ? envConfig.get(key).getValue(): getWebProxyPac());
    				break;
    			case WEB_PROXY_EXCLUDE:
    				setWebProxyExclude(getWebProxyExclude() == null ? envConfig.get(key).getValue(): getWebProxyExclude());
    				break;
    			default:
    				continue;
    		}
    	}
    	
    	// set default value for proxy type if none as been set before
    	if (getWebProxyType() == null) {
    		setWebProxyType(DEFAULT_WEB_PROXY_TYPE.toString());
    	}
    	
    	// exclude browserMobProxy if proxy type is set to PAC
    	if (getCaptureNetwork() && getWebProxyType() != ProxyType.DIRECT && getWebProxyType() != ProxyType.MANUAL) {
    		throw new ConfigurationException("Browsermob proxy (captureNetwork option) is only compatible with DIRECT and MANUAL");
    	}
    }
    
    /**
     * Read configuration from environment specific data and undefined parameters present un testng xml file
     * these configurations will be overiden by server if it's present
     */
	public void setTestConfiguration() {
		
		// get variables from XML
    	getConfiguration().putAll(getUserDefinedVariablesFromXMLFile());
		
		// get variables from env.ini
    	getConfiguration().putAll(new ConfigReader(getTestEnv(), getLoadIni()).readConfig());
    	
    	// get variables from command line
    	getConfiguration().putAll(getUserDefinedVariablesFromCommandLine());
    	
    	if (variableServer != null) {
    		// get variable from server
			getConfiguration().putAll(variableServer.getVariables(getSeleniumRobotServerVariableOlderThan()));
			
			// give priority to command line parameters over those from server, so overwrite variable server if overlapping
			getConfiguration().putAll(getUserDefinedVariablesFromCommandLine());
    	}
    }
	
	public TestManager initTestManager() {
		if (getTmsRun() != null && getTmsRun().length() > 0) {
			TestManager tms = TestManager.getInstance(getTmsRun());
			
			if (getTmsConnect() != null) {
				tms.init(getTmsConnect());
			}
			return tms;
		}
		return null;
	}
	
	
	
	// ------------------------- accessors ------------------------------------------------------
	
	// getters from contextManager
	public String getApplicationName() {
		return SeleniumTestsContextManager.getApplicationName();
	}

	public String getApplicationNameWithVersion() {
		return SeleniumTestsContextManager.getApplicationNameWithVersion();
	}

	public String getApplicationVersion() {
		return SeleniumTestsContextManager.getApplicationVersion();
	}

	public String getCoreVersion() {
		return SeleniumTestsContextManager.getCoreVersion();
	}
	
	/**
     * Returns application root path
     * @return
     */
    public String getRootPath() {
		return SeleniumTestsContextManager.getRootPath();
	}

    /**
     * Returns location of feature files
     * /<root>/data/<app>/features/
     * @return
     */
	public String getFeaturePath() {
		return SeleniumTestsContextManager.getFeaturePath();
	}

	/**
	 * Returns location of config files: /<root>/data/<app>/config/
	 * @return
	 */
	public String getConfigPath() {
		return SeleniumTestsContextManager.getConfigPath();
	}
	
	/**
	 * Returns location of data folder: /<root>/data/
	 * @return
	 */
	public String getDataPath() {
		return SeleniumTestsContextManager.getDataPath();
	}
	
	/**
	 * Returns location of application specific data: /<root>/data/<app>/
	 * @return
	 */
	public String getApplicationDataPath() {
		return SeleniumTestsContextManager.getApplicationDataPath();
	}
	
	// getters for this object
    public Boolean getFullReset() {
        return (Boolean) getAttribute(FULL_RESET);
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

            // safari grid default value set to false
            if (this.getRunMode() == DriverMode.GRID
                    && this.getBrowser() == BrowserType.SAFARI) {
                this.setAttribute(CAPTURE_SNAPSHOT, false);
            } else {
                this.setAttribute(CAPTURE_SNAPSHOT, DEFAULT_CAPTURE_SNAPSHOT);
            }
        }

        return (Boolean) getAttribute(CAPTURE_SNAPSHOT);
    }
    
    public boolean getCaptureNetwork() {    	
    	return (Boolean) getAttribute(CAPTURE_NETWORK);
    }
    
    public VideoCaptureMode getVideoCapture() {    	
    	return (VideoCaptureMode) getAttribute(VIDEO_CAPTURE);
    }
 
    public boolean getEnableExceptionListener() {
        return (Boolean) getAttribute(ENABLE_EXCEPTION_LISTENER);
    }

    public String getChromeBinPath() {
        return (String) getAttribute(CHROME_BINARY_PATH);
    }

    public String getChromeDriverPath() {
        return (String) getAttribute(CHROME_DRIVER_PATH);
    }
    
    public String getGeckoDriverPath() {
    	return (String) getAttribute(GECKO_DRIVER_PATH);
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

    public String getNeoloadUserPath() {
    	return (String) getAttribute(NEOLOAD_USER_PATH);
    }

    public int getExplicitWaitTimeout() {
        Integer timeout;
        try {
            timeout = (Integer) getAttribute(EXPLICIT_WAIT_TIME_OUT);
        } catch (Exception e) {
            timeout = DEFAULT_EXPLICIT_WAIT_TIME_OUT;
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
    
    public String getSeleniumRobotServerUrl() {
    	return (String) getAttribute(SELENIUMROBOTSERVER_URL);
    }
    
    public Boolean getSeleniumRobotServerActive() {
    	return (Boolean) getAttribute(SELENIUMROBOTSERVER_ACTIVE);
    }
    
    public boolean getSeleniumRobotServerCompareSnapshot() {
    	return (Boolean) getAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
    }
    
    public Integer getSeleniumRobotServerVariableOlderThan() {
    	return (Integer) getAttribute(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN);
    }
    
    public boolean getSeleniumRobotServerRecordResults() {
    	return (Boolean) getAttribute(SELENIUMROBOTSERVER_RECORD_RESULTS);
    }
    
    public boolean getOverrideSeleniumNativeAction() {
    	return (Boolean) getAttribute(OVERRIDE_SELENIUM_NATIVE_ACTION);
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
    		return DEFAULT_REPLAY_TIME_OUT;
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
    
    public String getDefaultOutputDirectory() {
    	return (String) getAttribute(DEFAULT_OUTPUT_DIRECTORY);
    }
    
	public Boolean getMaskedPassword() {
		return (Boolean) getAttribute(MASK_PASSWORD);
	}
    
    public String getLoadIni() {
        return (String) getAttribute(LOAD_INI);
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

    @SuppressWarnings("unchecked")
	public List<ReportInfo> getCustomTestReports() {
    	return (List<ReportInfo>) getAttribute(CUSTOM_TEST_REPORTS);
    }
    
    @SuppressWarnings("unchecked")
	public List<ReportInfo> getCustomSummaryReports() {
    	return (List<ReportInfo>) getAttribute(CUSTOM_SUMMARY_REPORTS);
    }
    
    public boolean getOptimizeReports() {
        return (Boolean) getAttribute(OPTIMIZE_REPORTS);
    }
    
    public String getArchiveToFile() {
    	return (String) getAttribute(ARCHIVE_TO_FILE);
    }
    
    public ArchiveMode getArchive() {
    	return (ArchiveMode) getAttribute(ARCHIVE);
    }
    
    public ElementInfo.Mode getAdvancedElementSearch() {
    	return (ElementInfo.Mode) getAttribute(ADVANCED_ELEMENT_SEARCH);
    }

    public TestType getTestType() {
        return (TestType) getAttribute(TEST_TYPE);
    }

    public boolean isDriverCreationBlocked() {
		return driverCreationBlocked;
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

    @SuppressWarnings("unchecked")
	public List<String> getWebDriverListener() {
        return (List<String>) getAttribute(WEB_DRIVER_LISTENER);
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
    
    public JSONObject getTmsRun() {
    	return (JSONObject) getAttribute(TMS_RUN);
    }
    
    public JSONObject getTmsConnect() {
    	return (JSONObject) getAttribute(TMS_CONNECT);
    }

    public List<String> getWebDriverGrid() {
        return (List<String>) getAttribute(WEB_DRIVER_GRID);
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
    
    public List<String> getNodeTags() {
    	List<String> extTools = (List<String>) getAttribute(NODE_TAGS);
    	if (extTools == null) {
    		return new ArrayList<>();
    	} else {
    		return extTools;
    	}
    }

    public List<DebugMode> getDebug() {
    	return (List<DebugMode>) getAttribute(DEBUG);
    }
    
    public boolean isHeadlessBrowser() {
    	return (Boolean) getAttribute(HEADLESS_BROWSER);
    }
    
    public boolean isManualTestSteps() {
    	return (Boolean) getAttribute(MANUAL_TEST_STEPS);
    }
    
	public Map<String, String> getDeviceList() {
    	HashMap<String, String> deviceList = new HashMap<>();
    	if (getAttribute(DEVICE_LIST) == null || DEFAULT_DEVICE_LIST.equals(getAttribute(DEVICE_LIST))) {
    		return deviceList;
    	}
    	
    	JSONObject devList = new JSONObject((String)getAttribute(DEVICE_LIST));
    	for (String key: (Set<String>)devList.keySet()) {
    		deviceList.put(key, devList.getString(key));
    	}
    	return deviceList;
    }
	
	public Integer getTestRetryCount() {
    	return (Integer) getAttribute(TEST_RETRY_COUNT);
    }

    public int getWebSessionTimeout() {
        return (Integer) getAttribute(WEB_SESSION_TIME_OUT);
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

    public String getRelativeOutputDir() {
    	return (String) getAttribute(RELATIVE_OUTPUT_DIR);
    }
    
    public Integer getViewPortWidth() {
		return (Integer) getAttribute(VIEWPORT_WIDTH);
	}
    
    public Integer getViewPortHeight() {
    	return (Integer) getAttribute(VIEWPORT_HEIGHT);
    }
    
    @SuppressWarnings("unchecked")
	public Map<String, TestVariable> getConfiguration() {
    	
    	Map<String, TestVariable> config = (HashMap<String, TestVariable>) getAttribute(TEST_VARIABLES);
    	if (config == null) {
    		return new HashMap<>();
    	} else {
    		return config;
    	}
    }
    
    public SeleniumRobotVariableServerConnector getVariableServer() {
		return variableServer;
	}
    
    public List<SeleniumGridConnector> getSeleniumGridConnectors() {
    	if (seleniumGridConnectors == null) {
    		seleniumGridConnectors = connectGrid();
    	}
    	return seleniumGridConnectors;
    }
    
    /**
     * from the list of all grid connectors, returns the first one where a session has been created
     * It means that the test runs on it because sessionId is get once driver is created
     * @return
     */
    public SeleniumGridConnector getSeleniumGridConnector() {
    	if (seleniumGridConnector == null && seleniumGridConnectors != null) {
    		for (SeleniumGridConnector gridConnector: seleniumGridConnectors) {
    			if (gridConnector.getSessionId() != null) {
    				seleniumGridConnector = gridConnector;
    				break;
    			}
    		}
    	}
    	return seleniumGridConnector;
    }
    
    public TestManager getTestManagerInstance() {
    	return testManagerIntance;
    }
    

	public Map<String, Object> getContextDataMap() {
		return contextDataMap;
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
     * 
     * @param timeout  timeout of driver session in seconds
     */
    public void setWebSessionTimeout(Integer timeoutInSecs) {
    	if (timeoutInSecs != null) {
    		setAttribute(WEB_SESSION_TIME_OUT, timeoutInSecs * 1000);
    	} else {
    		setAttribute(WEB_SESSION_TIME_OUT, DEFAULT_WEB_SESSION_TIMEOUT);
    	}
    }

    public void setImplicitWaitTimeout(Integer timeoutInSecs) {
    	if (timeoutInSecs != null) {
    		setAttribute(IMPLICIT_WAIT_TIME_OUT, timeoutInSecs);
    	} else {
    		setAttribute(IMPLICIT_WAIT_TIME_OUT, DEFAULT_IMPLICIT_WAIT_TIME_OUT);
    	}
    }
    
    public void setExplicitWaitTimeout(Integer timeoutInSecs) {
    	if (timeoutInSecs != null) {
    		setAttribute(EXPLICIT_WAIT_TIME_OUT, timeoutInSecs);
    	} else {
    		setAttribute(EXPLICIT_WAIT_TIME_OUT, DEFAULT_EXPLICIT_WAIT_TIME_OUT);
    	}
    }

    public void setPageLoadTimeout(Integer timeoutInSecs) {
    	if (timeoutInSecs != null) {
    		setAttribute(PAGE_LOAD_TIME_OUT, timeoutInSecs);
    	} else {
    		setAttribute(PAGE_LOAD_TIME_OUT, DEFAULT_PAGE_LOAD_TIME_OUT);
    	}
    }
    
    public void setCustomTestReports(String customReportsStr) {
    	if (customReportsStr != null) {
    		List<ReportInfo> reports = new ArrayList<>();
    		
    		// check if report is available in resources
    		for (String customReport: customReportsStr.split(",")) {
    			reports.add(new ReportInfo(customReport));
    		}
    		setAttribute(CUSTOM_TEST_REPORTS, reports);
    	} else {
    		setAttribute(CUSTOM_TEST_REPORTS, DEFAULT_CUSTOM_TEST_REPORTS);
    	}
    }
    
    public void setCustomSummaryReports(String customReportsStr) {
    	if (customReportsStr != null) {
    		List<ReportInfo> reports = new ArrayList<>();
    		
    		// check if report is available in resources
    		for (String customReport: customReportsStr.split(",")) {
    			reports.add(new ReportInfo(customReport));
    		}
    		setAttribute(CUSTOM_SUMMARY_REPORTS, reports);
    	} else {
    		setAttribute(CUSTOM_SUMMARY_REPORTS, DEFAULT_CUSTOM_SUMMARY_REPORTS);
    	}
    }
    
    public void setReplayTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(REPLAY_TIME_OUT, timeout);
    	} else {
    		setAttribute(REPLAY_TIME_OUT, DEFAULT_REPLAY_TIME_OUT);
    	}
    }
    
    public void setArchiveToFile(String filePath) {
    	if (filePath != null) {
    		if (!filePath.endsWith(".zip")) {
    			throw new ConfigurationException("You must specify a zip file");
    		}
    		new File(filePath).getParentFile().mkdirs();

    	}
    	setAttribute(ARCHIVE_TO_FILE, filePath);
    }
    
    public void setArchive(String archive) {
    	if (archive == null) {
    		setAttribute(ARCHIVE, ArchiveMode.FALSE);
    	} else {
    		try {
    			setAttribute(ARCHIVE, ArchiveMode.fromString(archive));
    		} catch (IllegalArgumentException e) {
    			throw new ConfigurationException(e.getMessage());
    		}
    	}
    }
    public void setAdvancedElementSearch(String advanceElementSearchMode) {
    	if (advanceElementSearchMode == null) {
    		setAttribute(ADVANCED_ELEMENT_SEARCH, DEFAULT_ADVANCED_ELEMENT_SEARCH);
    	} else {
    		try {
    			setAttribute(ADVANCED_ELEMENT_SEARCH, ElementInfo.Mode.valueOf(advanceElementSearchMode.toUpperCase()));
    		} catch (IllegalArgumentException e) {
    			throw new ConfigurationException(e.getMessage());
    		}
    	}
    }
    
    public void setSeleniumRobotServerUrl(String url) {
    	if (url != null) {
    		setAttribute(SELENIUMROBOTSERVER_URL, url);
    	} else if (System.getenv(SELENIUMROBOTSERVER_URL) != null) {
    		setAttribute(SELENIUMROBOTSERVER_URL, System.getenv(SELENIUMROBOTSERVER_URL));
    	} else {
    		setAttribute(SELENIUMROBOTSERVER_URL, DEFAULT_SELENIUMROBOTSERVER_URL);
    	}
    }
    
    public void setSeleniumRobotServerActive(Boolean active) {
    	if (active != null) {
    		setAttribute(SELENIUMROBOTSERVER_ACTIVE, active);
    	} else {
    		setAttribute(SELENIUMROBOTSERVER_ACTIVE, DEFAULT_SELENIUMROBOTSERVER_ACTIVE);
    	}
    	
    	if (getSeleniumRobotServerUrl() == null && getSeleniumRobotServerActive()) {
    		throw new ConfigurationException("SeleniumRobot server is requested but URL is not found, either in parameters, command line or through environment variable");
    	}
    }
    
    public void setSeleniumRobotServerCompareSnapshot(Boolean capture) {
    	if (capture != null) {
    		setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, capture);
    	} else {
    		setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
    	}
    }
    
    public void setSeleniumRobotServerVariableOlderThan(Integer olderThan) {
    	if (olderThan != null) {
    		setAttribute(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN, olderThan);
    	} else {
    		setAttribute(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN, DEFAULT_SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN);
    	}
    }
    
    public void setSeleniumRobotServerRecordResults(Boolean record) {
    	if (record != null) {
    		setAttribute(SELENIUMROBOTSERVER_RECORD_RESULTS, record);
    	} else {
    		setAttribute(SELENIUMROBOTSERVER_RECORD_RESULTS, DEFAULT_SELENIUMROBOTSERVER_RECORD_RESULTS);
    	}
    }
    
    public void setOverrideSeleniumNativeAction(Boolean record) {
    	if (record != null) {
    		setAttribute(OVERRIDE_SELENIUM_NATIVE_ACTION, record);
    	} else {
    		setAttribute(OVERRIDE_SELENIUM_NATIVE_ACTION, DEFAULT_OVERRIDE_SELENIUM_NATIVE_ACTION);
    	}
    }
    
    public void setWebDriverGrid(final String driverGrid) {
    	if (driverGrid == null) {
    		setAttribute(WEB_DRIVER_GRID, new ArrayList<>());
    	} else {
    		String[] gridList = driverGrid.split(",");
    		for (String gridAddress: gridList) {
    			if (!gridAddress.startsWith("http")) {
    				throw new ConfigurationException("grid address should be http://<host>:<port>/wd/hub");
    			}
    		}
    		setAttribute(WEB_DRIVER_GRID, Arrays.asList(driverGrid.split(",")));
    	}
    }
    
    public void setNeoloadUserPath(final String userPath) {
    	setAttribute(NEOLOAD_USER_PATH, userPath);
    }
    
    public void setConfiguration(Map<String, TestVariable> variables){
    	setAttribute(TEST_VARIABLES, variables);
    }
    
    public void setTmsRun(final String tms) {
    	if (tms == null) {
    		setAttribute(TMS_RUN, DEFAULT_TMS_RUN);
    	} else {
	    	try {
	    		JSONObject tmsJson = new JSONObject(tms);
	    		setAttribute(TMS_RUN, tmsJson);
	    	} catch (JSONException e) {
	    		throw new ConfigurationException("tms option must have the JSON format like {'type': 'hp', 'run': '3'}");
	    	}
    	}
    }
    
    public void setTmsConnect(final String tms) {
    	if (tms == null) {
    		setAttribute(TMS_CONNECT, DEFAULT_TMS_CONNECT);
    	} else {
    		try {
    			JSONObject tmsJson = new JSONObject(tms);
    			setAttribute(TMS_CONNECT, tmsJson);
    		} catch (JSONException e) {
    			throw new ConfigurationException("tms option must have the JSON format like {'hpAlmServerUrl': 'http://myamlserver:8080', 'hpAlmProject': '12', 'hpAlmDomain': 'mydomain', 'hpAlmUser': 'user', 'hpAlmPassword': 'pass'}");
    		}
    	}
    }
    
    public void setRunMode(String runMode) {
    	String newRunMode = runMode == null ? DEFAULT_RUN_MODE: runMode;
        setAttribute(RUN_MODE, DriverMode.fromString(newRunMode));
	}
    
    public void setNodeTags(String nodeTags) {
    	if (nodeTags == null || nodeTags.isEmpty()) {
    		setAttribute(NODE_TAGS, new ArrayList<>());
    	} else {
	    	setAttribute(NODE_TAGS, Arrays.asList(nodeTags.split(","))
										.stream()
										.map(String::trim)
										.collect(Collectors.toList())
						);
    	}
    }
    
    public void setMaskPassword(Boolean maskPassword) {
    	if (maskPassword != null) {
    		setAttribute(MASK_PASSWORD, maskPassword);
    	} else {
    		setAttribute(MASK_PASSWORD, DEFAULT_MASK_PASSWORD);
    	}
    }
    
    /**
     * Record DEBUG
     * also store an INTERNAL_DEBUG System property to be used internally with SeleniumRobotLogger class
     * @param debug
     */
    public void setDebug(String debug) {
    	if (debug != null) {
    		setAttribute(DEBUG, DebugMode.fromString(debug));
    		System.setProperty(INTERNAL_DEBUG, debug);
    	} else {
    		// default value depends on who starts test. If start is done through jar execution, deployed mode will be true (devMode set to false)
    		setAttribute(DEBUG, DebugMode.fromString(DEFAULT_DEBUG));
    		System.setProperty(INTERNAL_DEBUG, DEFAULT_DEBUG);
    	}
    }
    
    public void setHeadlessBrowser(Boolean headless) {
    	if (headless != null) {
    		setAttribute(HEADLESS_BROWSER, headless);
    	} else {
    		setAttribute(HEADLESS_BROWSER, DEFAULT_HEADLESS_BROWSER);
    	}
    }
    
    public void setManualTestSteps(Boolean manualTestSteps) {
    	if (manualTestSteps != null) {
    		setAttribute(MANUAL_TEST_STEPS, manualTestSteps);
    	} else {
    		setAttribute(MANUAL_TEST_STEPS, DEFAULT_MANUAL_TEST_STEPS);
    	}
    }

    public void setBrowser(String browser) {
    	String newBrowser = browser == null ? DEFAULT_BROWSER: browser;
    	setAttribute(BROWSER, BrowserType.getBrowserType(newBrowser));
    	
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
			setAttribute(USE_DEFAULT_FIREFOX_PROFILE, DEFAULT_USE_DEFAULT_FIREFOX_PROFILE);
    	}
    }
    
    public void setOperaUserProfilePath(String path) {
    	setAttribute(OPERA_USER_PROFILE_PATH, path);
    }
    
    public void setFirefoxBinary(String path) {
    	if (path != null && !new File(path).exists()) {
    		throw new ConfigurationException("Firefox path does not exist: " + path);
    	}
    	setAttribute(FIREFOX_BINARY_PATH, path);
    }
    
    public void setChromeBinary(String path) {
    	if (path != null && !new File(path).exists()) {
    		throw new ConfigurationException("Chrome path does not exist: " + path);
    	}
    	setAttribute(CHROME_BINARY_PATH, path);
    }
    
    public void setChromeDriverPath(String path) {
    	setAttribute(CHROME_DRIVER_PATH, path);
    }
    
    public void setGeckoDriverPath(String path) {
    	setAttribute(GECKO_DRIVER_PATH, path);
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
    		setAttribute(SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER, DEFAULT_SET_ASSUME_UNTRUSTED_CERTIFICATE_ISSUER);
    	}
    }
    
    public void setAcceptUntrustedCertificates(Boolean accept) {
    	if (accept != null) {
    		setAttribute(SET_ACCEPT_UNTRUSTED_CERTIFICATES, accept);
    	} else {
    		setAttribute(SET_ACCEPT_UNTRUSTED_CERTIFICATES, DEFAULT_SET_ACCEPT_UNTRUSTED_CERTIFICATES);
    	}
    }
    
    public void setJavascriptEnabled(Boolean enabled) {
    	if (enabled != null) {
    		setAttribute(ENABLE_JAVASCRIPT, enabled);
    	} else {
    		setAttribute(ENABLE_JAVASCRIPT, DEFAULT_ENABLE_JAVASCRIPT);
    	}
    }
   
    public void setNtlmAuthTrustedUris(String uris) {
    	setAttribute(NTLM_AUTH_TRUSTED_URIS, uris);
    }
    
    public void setBrowserDownloadDir(String downloadDir) {
    	setAttribute(BROWSER_DOWNLOAD_DIR, downloadDir);
    }
    
    public void setFullReset(Boolean enabled) {
    	if (enabled != null) {
    		setAttribute(FULL_RESET, enabled);
    	} else {
    		setAttribute(FULL_RESET, true);
    	}
    }
    

    public void setWebProxyType(String proxyType) {
    	try {
    		setAttribute(WEB_PROXY_TYPE, ProxyType.valueOf(proxyType.toUpperCase()));
    		setAttribute(WEB_PROXY_TYPE_FROM_USER, proxyType.toUpperCase());
    	} catch (NullPointerException | IllegalArgumentException e) {
    		setAttribute(WEB_PROXY_TYPE, DEFAULT_WEB_PROXY_TYPE);
    		setAttribute(WEB_PROXY_TYPE_FROM_USER, null);
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
    		setAttribute(SNAPSHOT_BOTTOM_CROPPING, DEFAULT_SNAPSHOT_BOTTOM_CROPPING);
    	}
    }
    
    public void setSnapshotTopCropping(Integer crop) {
    	if (crop != null) {
    		setAttribute(SNAPSHOT_TOP_CROPPING, crop);
    	} else {
    		setAttribute(SNAPSHOT_TOP_CROPPING, DEFAULT_SNAPSHOT_TOP_CROPPING);
    	}
    }
    
    public void setCaptureSnapshot(Boolean capture) {
    	if (capture != null) {
    		setAttribute(CAPTURE_SNAPSHOT, capture);
    	} else {
    		setAttribute(CAPTURE_SNAPSHOT, DEFAULT_CAPTURE_SNAPSHOT);
    	}
    }
    
    public void setCaptureNetwork(Boolean capture) {
    	if (capture != null) {
    		setAttribute(CAPTURE_NETWORK, capture);
    	} else {
    		setAttribute(CAPTURE_NETWORK, DEFAULT_CAPTURE_NETWORK);
    	}
    }
    
    public void setVideoCapture(String capture) {
    	String newCapture = capture == null ? DEFAULT_VIDEO_CAPTURE: capture;
    	try {
    		setAttribute(VIDEO_CAPTURE, VideoCaptureMode.fromString(newCapture));
    	} catch (IllegalArgumentException e) {
    		throw new ConfigurationException("Only 'true', 'false', 'onSuccess', 'onError' are supported for video capture");
    	}
    }
    
    public void setEnableExceptionListener(Boolean enable) {
    	if (enable != null) {
    		setAttribute(ENABLE_EXCEPTION_LISTENER, enable);
    	} else {
    		setAttribute(ENABLE_EXCEPTION_LISTENER, DEFAULT_ENABLE_EXCEPTION_LISTENER);
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
    		setAttribute(SOFT_ASSERT_ENABLED, DEFAULT_SOFT_ASSERT_ENABLED);
    	}
    }
    
    public void setWebDriverListener(String listener) {
    	List<String> listeners = new ArrayList<>();
		listeners.add(DriverExceptionListener.class.getName());
    	if (listener != null && !listener.isEmpty()) {
    		listeners.addAll(Arrays.asList(listener.split(",")));
    	}
    		
		setAttribute(WEB_DRIVER_LISTENER, listeners);
    }
    
    public void setDeviceName(String name) {
    	setAttribute(DEVICE_NAME, name);
    }

    public void setDeviceList(String list) {
    	if (list != null) {
    		setAttribute(DEVICE_LIST, list);
    	} else {
    		setAttribute(DEVICE_LIST, DEFAULT_DEVICE_LIST);
    	}
    }
    
    public void setApp(String app) {
    	if (app != null) {
    		setAttribute(APP, app);
    	} else {
    		setAttribute(APP, DEFAULT_APP);
    	}
    }
    
    public void setCucumberTags(String tags) {
    	if (tags != null) {
    		setAttribute(CUCUMBER_TAGS, tags);
    	} else {
    		setAttribute(CUCUMBER_TAGS, DEFAULT_CUCUMBER_TAGS);
    	}
    }
    
    public void setCucumberTests(String tests) {
    	if (tests != null) {
    		setAttribute(CUCUMBER_TESTS, tests);
    	} else {
    		setAttribute(CUCUMBER_TESTS, DEFAULT_CUCUMBER_TESTS);
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
    		setAttribute(TEST_ENV, DEFAULT_TEST_ENV);
    	}
    }
    
    public void setLoadIni(String iniFiles) {
    	setAttribute(LOAD_INI, iniFiles);
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

    public void setTestRetryCount(Integer retry) {
    	if (retry != null) {
    		setAttribute(TEST_RETRY_COUNT, retry);
    	} else {
    		setAttribute(TEST_RETRY_COUNT, DEFAULT_TEST_RETRY_COUNT);
    	}
    }
    
    public void setNewCommandTimeout(Integer timeout) {
    	if (timeout != null) {
    		setAttribute(NEW_COMMAND_TIMEOUT, timeout);
    	} else {
    		setAttribute(NEW_COMMAND_TIMEOUT, DEFAULT_NEW_COMMAND_TIMEOUT);
    	}
    }
    
    public void setOutputDirectory(String outputDir, ITestContext context, boolean configureTestNg) {
    	setDefaultOutputDirectory(context);
    	if (outputDir == null) {
    		setAttribute(OUTPUT_DIRECTORY, new File(context.getOutputDirectory()).getParent());
    	} else {
    		if (context instanceof TestRunner && configureTestNg) {
    			((TestRunner)context).setOutputDirectory(outputDir);
    		}
    		setAttribute(OUTPUT_DIRECTORY, new File(outputDir).getAbsolutePath().replace(File.separator, "/"));
    		try {
    			new File((String)getAttribute(OUTPUT_DIRECTORY)).mkdirs();
    		} catch (Exception e) {}
    	}
    }
    
    public void setDefaultOutputDirectory(ITestContext context) {
    	setAttribute(DEFAULT_OUTPUT_DIRECTORY, new File(context.getOutputDirectory()).getParent());
    }
    
    public void setOptimizeReports(Boolean optimize) {
    	if (optimize != null) {
    		setAttribute(OPTIMIZE_REPORTS, optimize);
    	} else {
    		setAttribute(OPTIMIZE_REPORTS, DEFAULT_OPTIMIZE_REPORTS);
    	}
    }
    
    public void setVersion(String version) {
    	setAttribute(VERSION, version);
    }
    
    /**
     * Set platform on which we request to execute the test
     * In mobile, platform parameter will be given (iOS xx or Android yy)
     * In desktop, if we run the test locally, we should get the current platform, else, let user decide on which platform
     * test will be run. It may be any if underlying OS does not matter (for grid)
     * @param platform
     */
    public void setPlatform(String platform) {
    	if (platform != null) {
    		setAttribute(PLATFORM, platform);
    	} else {
    		if (getRunMode() == DriverMode.LOCAL) {
    			setAttribute(PLATFORM, OSUtility.getCurrentPlatorm().toString());
    		} else {
    			setAttribute(PLATFORM, Platform.ANY.toString());
    		}
    	}
    }
    
    public void setCloudApiKey(String key) {
    	setAttribute(CLOUD_API_KEY, key);
    }
    
    public void setTestType(final TestType testType) {
        setAttribute(TEST_TYPE, testType);
    }
   
    public void setRelativeOutputDir(String name) {
    	setAttribute(RELATIVE_OUTPUT_DIR, name);
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
    
    public void setDriverCreationBlocked(boolean driverCreationBlocked) {
		this.driverCreationBlocked = driverCreationBlocked;
	}

	/**
     * To be used by unit tests exclusively
     */
    public static void resetOutputFolderNames() {
    	outputFolderNames.clear();
    }

	public static Map<String, String> getOutputFolderNames() {
		return outputFolderNames;
	}
}
