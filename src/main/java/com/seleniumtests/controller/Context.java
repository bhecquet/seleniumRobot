package com.seleniumtests.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.ITestContext;
import org.testng.ITestResult;

import com.seleniumtests.driver.web.ScreenShot;
import com.seleniumtests.reporter.PluginsUtil;

/**
 * This class defines TestNG context used in framework
 * 
 */
public class Context {
	// private static Logger logger = Logging.getLogger(Context.class);

	/* The config defined in testng.xml */
	public static final String TEST_CONFIG = "testConfig";
	/*public static final String POOL = "pool";
	public static final String POOL_TYPE = "poolType";*/
	public static final String SITE = "site";
	public static final String APP_URL = "appURL";	
	public static final String LOCALIZED_BUILD = "localizedBuild";
	public static final String COBRAND = "cobrand";
	public static final String POP3_HOST = "pop3Host";
	public static final String GBH_SITE = "gbhSite";
	public static final String IS_MOBILE_SITE = "isMobileSite";

	public static final String DCT_WS_END_POINT_URL = "dctEndPointURL";

	public static final String WEB_SESSION_TIME_OUT = "webSessionTimeOut";
	public static final String IMPLICIT_WAIT_TIME_OUT = "implicitWaitTimeOut";
	public static final String EXPLICIT_WAIT_TIME_OUT = "explicitWaitTimeOut";
	public static final String PAGE_LOAD_TIME_OUT = "pageLoadTimeout";
	public static final String WEB_DRIVER_GRID = "webDriverGrid";
	public static final String WEB_RUN_MODE = "webRunMode";
	public static final String WEB_RUN_BROWSER = "browser";
	public static final String WEB_RUN_BROWSER_VERSION = "browserVersion";
	public static final String WEB_RUN_PLATFORM = "platform";
	public static final String FIREFOX_USER_PROFILE_PATH = "firefoxUserProfilePath";
	public static final String USE_FIREFOX_DEFAULT_PROFILE = "useFirefoxDefaultProfile";
	public static final String OPERA_USER_PROFILE_PATH = "operaUserProfilePath";
	public static final String FIREFOX_BINARY_PATH = "firefoxBinaryPath";
	public static final String CHROME_DRIVER_PATH = "chromeDriverPath";
	public static final String CHROME_BINARY_PATH = "chromeBinaryPath";
	public static final String IE_DRIVER_PATH = "ieDriverPath";
	public static final String USER_AGENT = "userAgent";
	public static final String APP_NAME = "appName";
	public static final String APP_VERSION = "appVersion";

	public static final String Set_Assume_Untrusted_Certificate_Issuer = "setAssumeUntrustedCertificateIssuer";
	public static final String Set_Accept_Untrusted_Certificates = "setAcceptUntrustedCertificates";
	public static final String ENABLE_JAVASCRIPT = "enableJavascript";
	public static final String NTLM_AUTH_TRUSTED_URIS = "ntlmAuthTrustedUris";
	public static final String BROWSER_DOWNLOAD_DIR = "browserDownloadDir";
	public static final String BROWSER_WINDOW_SIZE = "browserWindowSize";
	public static final String ADD_JSERROR_COLLECTOR_EXTENSION = "addJSErrorCollectorExtension";

	public static final String WEB_PROXY_ENABLED = "webProxyEnabled";
	public static final String WEB_PROXY_TYPE = "webProxyType";
	public static final String WEB_PROXY_ADDRESS = "webProxyAddress";
	public static final String API_PROXY_HOST = "apiProxyHost";
	public static final String API_PROXY_PORT = "apiProxyPort";

	public static final String API_CONTEXT = "apiContext";
	public static final String TEST_OBJECT = "testObject";

	public static final String OPEN_REPORT_IN_BROWSER = "openReportInBrowser";
	public static final String CAPTURE_SNAPSHOT = "captureSnapshot";
	public static final String DB_LOG_ENABLED = "dbLogEnabled";
	public static final String CAL_LOG_ENABLED = "calLogEnabled";
	public static final String ENABLE_EXCEPTION_LISTENER = "enableExceptionListener";

	public static final String DP_TAGS_INCLUDE = "dpTagsInclude";
	public static final String DP_TAGS_EXCLUDE = "dpTagsExclude";

	public static final String SSH_COMMAND_WAIT = "sshCommandWait";
	public static final String SOFT_ASSERT_ENABLED = "softAssertEnabled";

	public static final String OUTPUT_DIRECTORY = "outputDirectory";
	public static final String EMAIL_RETENTION_POLICY = "emailRetentionPolicy";
	public static final String URL_CONVERT_CLASS = "urlConvertClass";
	public static final String WEB_DRIVER_LISTENER = "webDriverListener";

	public static final String VALIDATE_INTERNALS_END_POINT = "viEndPoint";

	/* running time context */
	public static final String TEST_METHOD_SIGNATURE = "testMethodSignature";
	public static final String TEST_METHOD_PC_MAP = "TestMethodPCMap";

	public static final String TEST_PC_MAP = "TestPCMap";
	public static final String PLUGIN_CONFIG_PATH = "pluginConfigPath";

	/* test data file */
	public static final String TEST_DATA_FILE = "testDataFile";
	private LinkedList<TearDownService> teardowns = new LinkedList<TearDownService>();
	private Map<ITestResult, List<Throwable>> verificationFailuresMap = new HashMap<ITestResult, List<Throwable>>();

	/* Data object to store all context data */
	private Map<String, Object> contextDataMap = Collections
			.synchronizedMap(new HashMap<String, Object>());

	private ITestContext testNGContext = null;

	/* data stored for exception web page */
	// private String webExceptionURL = null;
	// private String webExceptionMessage = null;
	// private String screenshotName = null;
	private LinkedList<ScreenShot> screenshots = new LinkedList<ScreenShot>();

	public LinkedList<ScreenShot> getScreenshots() {
		return screenshots;
	}

	public void addScreenShot(ScreenShot screenShot) {
		deleteExceptionSnapshots();
		screenshots.addLast(screenShot);
	}

	private void deleteExceptionSnapshots() {
		try{
			int size = screenshots.size();
			if (size == 0)
				return;
			ScreenShot screenShot = screenshots.get(size - 1);
			if (screenShot.isException() && screenShot.getFullImagePath()!=null) {
				new File(screenShot.getFullImagePath()).delete();
				screenshots.remove(size - 1);
			}
		}catch(Exception ex){}
	}

	public ScreenShot getExceptionScreenShot() {
		if (screenshots.size() > 0 && screenshots.getLast().isException())
			return screenshots.getLast();
		else
			return null;
	}
	
	public Context(ITestContext context) {
		this.testNGContext = context;

		setContextAttribute(context, TEST_DATA_FILE,
				System.getProperty(TEST_DATA_FILE), "testCase");
/*		setContextAttribute(context, POOL, System.getProperty(POOL), "staging");
		setContextAttribute(context, POOL_TYPE, System.getProperty(POOL_TYPE),
				"qa");*/
		setContextAttribute(context, SITE, System.getProperty(SITE), "US");
		setContextAttribute(context, LOCALIZED_BUILD,
				System.getProperty(LOCALIZED_BUILD), "true");
		setContextAttribute(context, POP3_HOST, System.getProperty(POP3_HOST),
				null);
		setContextAttribute(context, GBH_SITE, System.getProperty(GBH_SITE),
				null);
		setContextAttribute(context, IS_MOBILE_SITE,
				System.getProperty(IS_MOBILE_SITE), "false");

		setContextAttribute(context, DCT_WS_END_POINT_URL,
				System.getProperty(DCT_WS_END_POINT_URL),
				"http://dct.phx.qa.ebay.com:8088/DCTServiceWeb/DCTService");
		setContextAttribute(context, WEB_SESSION_TIME_OUT,
				System.getProperty(WEB_SESSION_TIME_OUT), "90000");
		setContextAttribute(context, IMPLICIT_WAIT_TIME_OUT,
				System.getProperty(IMPLICIT_WAIT_TIME_OUT), "5");
		setContextAttribute(context, EXPLICIT_WAIT_TIME_OUT,
				System.getProperty(EXPLICIT_WAIT_TIME_OUT), "15");
		setContextAttribute(context, PAGE_LOAD_TIME_OUT,
				System.getProperty(PAGE_LOAD_TIME_OUT), "90");

		setContextAttribute(context, WEB_DRIVER_GRID,
				System.getProperty(WEB_DRIVER_GRID), null);
		setContextAttribute(context, WEB_RUN_MODE,
				System.getProperty(WEB_RUN_MODE), "LocallyOnRC");
		setContextAttribute(context, WEB_RUN_BROWSER,
				System.getProperty(WEB_RUN_BROWSER), "*firefox");
		setContextAttribute(context, WEB_RUN_BROWSER_VERSION,
				System.getProperty(WEB_RUN_BROWSER_VERSION), null);
		setContextAttribute(context, WEB_RUN_PLATFORM,
				System.getProperty(WEB_RUN_PLATFORM), null);

		setContextAttribute(context, FIREFOX_USER_PROFILE_PATH,
				System.getProperty(FIREFOX_USER_PROFILE_PATH), null);
		setContextAttribute(context, USE_FIREFOX_DEFAULT_PROFILE,
				System.getProperty(USE_FIREFOX_DEFAULT_PROFILE), "true");

		setContextAttribute(context, OPERA_USER_PROFILE_PATH,
				System.getProperty(OPERA_USER_PROFILE_PATH), null);
		setContextAttribute(context, FIREFOX_BINARY_PATH,
				System.getProperty(FIREFOX_BINARY_PATH), null);
		setContextAttribute(context, CHROME_DRIVER_PATH,
				System.getProperty(CHROME_DRIVER_PATH), null);
		setContextAttribute(context, IE_DRIVER_PATH,
				System.getProperty(IE_DRIVER_PATH), null);
		setContextAttribute(context, USER_AGENT,
				System.getProperty(USER_AGENT), null);
		setContextAttribute(context, APP_NAME, System.getProperty(APP_NAME),
				"Safari");
		setContextAttribute(context, APP_VERSION, System.getProperty(APP_VERSION),
				null);

		setContextAttribute(context, Set_Assume_Untrusted_Certificate_Issuer,
				System.getProperty(Set_Assume_Untrusted_Certificate_Issuer),
				null);
		setContextAttribute(context, Set_Accept_Untrusted_Certificates,
				System.getProperty(Set_Accept_Untrusted_Certificates), null);
		setContextAttribute(context, ENABLE_JAVASCRIPT,
				System.getProperty(ENABLE_JAVASCRIPT), null);
		setContextAttribute(context, NTLM_AUTH_TRUSTED_URIS,
				System.getProperty(NTLM_AUTH_TRUSTED_URIS), null);
		setContextAttribute(context, BROWSER_DOWNLOAD_DIR,
				System.getProperty(BROWSER_DOWNLOAD_DIR), null);
		setContextAttribute(context, BROWSER_WINDOW_SIZE,
				System.getProperty(BROWSER_WINDOW_SIZE), null);
		setContextAttribute(context, ADD_JSERROR_COLLECTOR_EXTENSION,
				System.getProperty(ADD_JSERROR_COLLECTOR_EXTENSION), "false");

		setContextAttribute(context, WEB_PROXY_ENABLED,
				System.getProperty(WEB_PROXY_ENABLED), "false");
		setContextAttribute(context, WEB_PROXY_TYPE,
				System.getProperty(WEB_PROXY_TYPE), null);
		setContextAttribute(context, WEB_PROXY_ADDRESS,
				System.getProperty(WEB_PROXY_ADDRESS), null);

		setContextAttribute(context, API_PROXY_HOST,
				System.getProperty(API_PROXY_HOST), null);
		setContextAttribute(context, API_PROXY_PORT,
				System.getProperty(API_PROXY_PORT), null);

		setContextAttribute(context, OPEN_REPORT_IN_BROWSER,
				System.getProperty(OPEN_REPORT_IN_BROWSER), null);

		setContextAttribute(context, CAPTURE_SNAPSHOT,
				System.getProperty(CAPTURE_SNAPSHOT), null);
		setContextAttribute(context, ENABLE_EXCEPTION_LISTENER,
				System.getProperty(ENABLE_EXCEPTION_LISTENER), "true");

		setContextAttribute(context, DB_LOG_ENABLED,
				System.getProperty(DB_LOG_ENABLED), "true");
		setContextAttribute(context, CAL_LOG_ENABLED,
				System.getProperty(CAL_LOG_ENABLED), "true");

		setContextAttribute(context, DP_TAGS_INCLUDE,
				System.getProperty(DP_TAGS_INCLUDE), null);
		setContextAttribute(context, DP_TAGS_EXCLUDE,
				System.getProperty(DP_TAGS_EXCLUDE), null);

		setContextAttribute(context, SSH_COMMAND_WAIT,
				System.getProperty(SSH_COMMAND_WAIT), "5000");
		setContextAttribute(context, SOFT_ASSERT_ENABLED,
				System.getProperty(SOFT_ASSERT_ENABLED), "false");

		setContextAttribute(context, EMAIL_RETENTION_POLICY,
				System.getProperty(EMAIL_RETENTION_POLICY), "1");
		setContextAttribute(context, URL_CONVERT_CLASS,
				System.getProperty(URL_CONVERT_CLASS), null);
		setContextAttribute(context, WEB_DRIVER_LISTENER,
				System.getProperty(WEB_DRIVER_LISTENER), null);

		setContextAttribute(context, VALIDATE_INTERNALS_END_POINT,
				System.getProperty(VALIDATE_INTERNALS_END_POINT),
				"http://signin.qa.ebay.com/admin/v3console/ValidateInternals");

		if (context != null) {
			setContextAttribute(OUTPUT_DIRECTORY, null,
					context.getOutputDirectory(), null);

			// parse other parameters that defined in testng xml but not defined
			// in this context
			setContextAttribute(context);

			new File(context.getOutputDirectory() + "/screenshots").mkdirs();// KEEPME
			new File(context.getOutputDirectory() + "/htmls").mkdirs();// KEEPME
			new File(context.getOutputDirectory() + "/xmls").mkdirs();// KEEPME
			new File(context.getOutputDirectory() + "/textfiles/").mkdirs();// KEEPME

			String path = (String) getAttribute(PLUGIN_CONFIG_PATH);

			if (path != null && path.trim().length() > 0) {
				File configFile = new File(path);
				if (configFile.exists())
					PluginsUtil.getInstance().loadPlugins(configFile);
			}
		}
	}

	/**
	 * Adds the given tear down.
	 */
	public void addTearDownService(TearDownService tearDown) {
		teardowns.add(tearDown);
	}

	public void addVerificationFailures(ITestResult result,
			List<Throwable> failures) {

		this.verificationFailuresMap.put(result, failures);
	}

	public void addVerificationFailures(ITestResult result, Throwable failure) {

		if (verificationFailuresMap.get(result) != null)
			this.verificationFailuresMap.get(result).add(failure);
		else {
			ArrayList<Throwable> failures = new ArrayList<Throwable>();
			failures.add(failure);
			this.addVerificationFailures(result, failures);
		}
	}

	public String getAddJSErrorCollectorExtension() {
		return (String) getAttribute(ADD_JSERROR_COLLECTOR_EXTENSION);
	}

	public Object getApiContext() {
		return getAttribute(API_CONTEXT);
	}

	public String getApiProxyHost() {
		return (String) getAttribute(API_PROXY_HOST);
	}

	public String getApiProxyPort() {
		return (String) getAttribute(API_PROXY_PORT);
	}

	public String getAppName() {
		return (String) getAttribute(APP_NAME);
	}
	
	public String getAppVersion() {
		return (String) getAttribute(APP_VERSION);
	}

	public Object getAttribute(String name) {
		Object obj = contextDataMap.get(name);
		return obj == null ? null : obj;
	}

	public String getBrowserDownloadDir() {
		if (getAttribute(BROWSER_DOWNLOAD_DIR) != null)
			return (String) getAttribute(BROWSER_DOWNLOAD_DIR);
		else
			return this.getOutputDirectory() + "\\downloads\\";
	}

	public String getBrowserWindowSize() {
		return (String) getAttribute(BROWSER_WINDOW_SIZE);
	}

	public boolean getCaptureSnapshot() {
		if (getAttribute(CAPTURE_SNAPSHOT) == null) {
			// IE grid default value set to false
			if (this.getWebRunMode().equalsIgnoreCase("ExistingGrid")
					&& (this.getWebRunBrowser().contains("iexplore") || this
							.getWebRunBrowser().contains("safari"))) {
				this.setAttribute(CAPTURE_SNAPSHOT, "false");
			} else
				this.setAttribute(CAPTURE_SNAPSHOT, "true");
		}
		return Boolean.parseBoolean((String) getAttribute(CAPTURE_SNAPSHOT));
	}

	public boolean getEnableExceptionListener() {
		return Boolean
				.parseBoolean((String) getAttribute(ENABLE_EXCEPTION_LISTENER));
	}

	public String getChromeBinPath() {
		return (String) getAttribute(CHROME_BINARY_PATH);
	}

	public String getChromeDriverPath() {
		return (String) getAttribute(CHROME_DRIVER_PATH);
	}

	public int getCobrand() {
		int i = 2;// default is ebay
		try {
			i = Integer.parseInt((String) getAttribute(COBRAND));
			return i;
		} catch (Exception e) {
			throw new RuntimeException(
					"Please specify a numeric value for Cobrand.");
		}
	}

	public String getDCTEndPointURL() {
		return (String) getAttribute(DCT_WS_END_POINT_URL);
	}

	public String getDPTagsExclude() {
		return (String) getAttribute(DP_TAGS_EXCLUDE);
	}

	public String getDPTagsInclude() {
		return (String) getAttribute(DP_TAGS_INCLUDE);
	}

	public double getEmailRetentionPolicy() {
		try {
			return Double
					.parseDouble((String) getAttribute(EMAIL_RETENTION_POLICY));
		} catch (Exception e) {
			return 1;// Default is 1 day
		}
	}

	public int getExplicitWaitTimeout() {
		Integer timeout;
		try {
			timeout = Integer
					.parseInt((String) getAttribute(EXPLICIT_WAIT_TIME_OUT));
		} catch (Exception e) {
			timeout = 15; 
		}

		if (timeout < getImplicitWaitTimeout())
			return (int)getImplicitWaitTimeout();
		else
			return timeout;
	}

	public String getFirefoxBinPath() {
		return (String) getAttribute(FIREFOX_BINARY_PATH);
	}

	public String getFirefoxUserProfilePath() {
		return (String) getAttribute(FIREFOX_USER_PROFILE_PATH);
	}

	public String getGBHSite() {
		return (String) getAttribute(GBH_SITE);
	}

	public String getIEDriverPath() {
		return (String) getAttribute(IE_DRIVER_PATH);
	}

	public double getImplicitWaitTimeout() {
		try {
			return Double
					.parseDouble((String) getAttribute(IMPLICIT_WAIT_TIME_OUT));
		} catch (Exception e) {
			return 5; 
		}
	}

	public String getNtlmAuthTrustedUris() {
		return (String) getAttribute(NTLM_AUTH_TRUSTED_URIS);
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
			return Integer
					.parseInt((String) getAttribute(PAGE_LOAD_TIME_OUT));
		} catch (Exception e) {
			return 90; 
		}
	}

	public String getPCSettings() {
		Context ctx = ContextManager.getThreadContext();
		@SuppressWarnings("unchecked")
		Map<String, Boolean> map = (Map<String, Boolean>) ctx
				.getAttribute(Context.TEST_PC_MAP);

		if (map != null) {
			StringBuffer sb = new StringBuffer();
			// Site site = Site.valueOf(ctx.getSite());
			for (Entry<String, Boolean> entry : map.entrySet()) {
				sb.append(entry.getKey() + "=" + (entry.getValue() ? "t" : "f")
						+ ",");
			}
			if (sb.length() > 0)
				sb.deleteCharAt(sb.length() - 1);

			return "PC<<" + sb.toString() + ">>";
		}

		return null;
	}

	public String getPlatform() {
		return (String) getAttribute(WEB_RUN_PLATFORM);
	}

/*	public String getPool() {
		return (String) getAttribute(POOL);
	}

	public String getPoolType() {
		return (String) getAttribute(POOL_TYPE);
	}*/

	public String getPop3Host() {
		return (String) getAttribute(POP3_HOST);
	}

	public String getSite() {
		return (String) getAttribute(SITE);
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
	 * Get TestNG suite parameter from testng.xml. Return System value for CI
	 * job.
	 * 
	 * @param key
	 * @return
	 */
	public String getSuiteParameter(String key) {
		if (System.getProperty(key) != null)
			return System.getProperty(key);
		else
			return getTestNGContext().getSuite().getParameter(key);
	}

	/**
	 * Returns the tear down list.
	 */
	public LinkedList<TearDownService> getTearDownServices() {
		return teardowns;
	}

	public String getTestDataFile() {
		return (String) getAttribute(TEST_DATA_FILE);
	}

	public String getTestMethodSignature() {
		return (String) getAttribute(TEST_METHOD_SIGNATURE);
	}

	public ITestContext getTestNGContext() {
		return testNGContext;
	}

	public Object getTestObject() {
		return getAttribute(TEST_OBJECT);
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Boolean> getTestPCMap() {
		return (Map<Integer, Boolean>) getAttribute(TEST_PC_MAP);
	}

	public String getUrlConvertClass() {
		return (String) getAttribute(URL_CONVERT_CLASS);
	}

	public String getWebDriverListener() {
		return (String) getAttribute(WEB_DRIVER_LISTENER);
	}

	public String getUserAgent() {
		return (String) getAttribute(USER_AGENT);
	}

	public List<Throwable> getVerificationFailures(ITestResult result) {
		List<Throwable> verificationFailures = verificationFailuresMap
				.get(result);
		return verificationFailures == null ? new ArrayList<Throwable>()
				: verificationFailures;

	}

	public String getWebBrowserVersion() {
		return (String) getAttribute(WEB_RUN_BROWSER_VERSION);
	}

	public String getWebDriverGrid() {
		return (String) getAttribute(WEB_DRIVER_GRID);
	}

	// public String getWebExceptionMessage() {
	// return webExceptionMessage;
	// }
	//
	//
	//
	// public String getScreenshotName() {
	// return screenshotName;
	// }
	//
	// public void setScreenshotName(String screenshotName) {
	// this.screenshotName = screenshotName;
	// }
	//
	// public String getWebExceptionURL() {
	// return webExceptionURL;
	// }

	public String getWebProxyAddress() {
		return (String) getAttribute(WEB_PROXY_ADDRESS);
	}

	public String getWebProxyType() {
		return (String) getAttribute(WEB_PROXY_TYPE);
	}

	public String getWebRunBrowser() {
		return (String) getAttribute(WEB_RUN_BROWSER);
	}

	public String getWebRunMode() {
		return (String) getAttribute(WEB_RUN_MODE);
	}

	public int getWebSessionTimeout() {
		try {
			return Integer
					.parseInt((String) getAttribute(WEB_SESSION_TIME_OUT));
		} catch (Exception e) {
			return 90000; // Default
		}
	}

	public boolean isCALLogEnabled() {
		try {
			return Boolean.parseBoolean((String) getAttribute(CAL_LOG_ENABLED));
		} catch (Exception e) {
			return true; // Default
		}
	}

	public boolean isDBLogEnabled() {
		try {
			return Boolean.parseBoolean((String) getAttribute(DB_LOG_ENABLED));
		} catch (Exception e) {
			return true; // Default
		}
	}

	public boolean isMobileSite() {
		try {
			return Boolean.parseBoolean((String) getAttribute(IS_MOBILE_SITE));
		} catch (Exception e) {
			return false; // Default
		}
	}

	public boolean isUseFirefoxDefaultProfile() {
		try {
			return Boolean
					.parseBoolean((String) getAttribute(USE_FIREFOX_DEFAULT_PROFILE));
		} catch (Exception e) {
			return true; // Default
		}

	}

	public boolean isSoftAssertEnabled() {
		try {
			return Boolean
					.parseBoolean((String) getAttribute(SOFT_ASSERT_ENABLED));
		} catch (Exception e) {
			return false; // Default
		}
	}

	public boolean isWebProxyEnabled() {
		try {
			return Boolean
					.parseBoolean((String) getAttribute(WEB_PROXY_ENABLED));
		} catch (Exception e) {
			return false; // Default
		}
	}

	public void setAttribute(String name, Object value) {
		contextDataMap.put(name, value);
	}

	public void setCobrand(int cobrand) {
		setAttribute(COBRAND, cobrand);
	}

	private void setContextAttribute(ITestContext context) {
		if (context != null) {
			Map<String, String> testParameters = context.getSuite()
					.getXmlSuite().getParameters();

			for (Entry<String, String> entry : testParameters.entrySet()) {
				String attributeName = entry.getKey();

				if (contextDataMap.get(entry.getKey()) == null) {
					String sysPropertyValue = System
							.getProperty(entry.getKey());
					String suiteValue = entry.getValue();
					setContextAttribute(attributeName, sysPropertyValue,
							suiteValue, null);
				}

			}
		}

	}

	/**
	 * Set Suite Context Attributes. This is eCAF Platform internal. Should not
	 * be accessed from outside.
	 * 
	 * @param context
	 * @param attributeName
	 * @param sysPropertyValue
	 * @param defaultValue
	 */

	private void setContextAttribute(ITestContext context,
			String attributeName, String sysPropertyValue, String defaultValue) {
		String suiteValue = null;
		if (context != null) {
			suiteValue = context.getSuite().getParameter(attributeName);
		}
		contextDataMap.put(attributeName,
				(sysPropertyValue != null ? sysPropertyValue
						: (suiteValue != null ? suiteValue : defaultValue)));
	}

	private void setContextAttribute(String attributeName,
			String sysPropertyValue, String suiteValue, String defaultValue) {

		contextDataMap.put(attributeName,
				(sysPropertyValue != null ? sysPropertyValue
						: (suiteValue != null ? suiteValue : defaultValue)));

	}

	public void setExplicitWaitTimeout(double timeout) {
		setAttribute(EXPLICIT_WAIT_TIME_OUT, timeout);
	}

	public void setImplicitWaitTimeout(double timeout) {
		setAttribute(IMPLICIT_WAIT_TIME_OUT, timeout);
	}

	public void setPageLoadTimeout(int timeout) {
		setAttribute(PAGE_LOAD_TIME_OUT, timeout);
	}

/*	public void setPool(String pool) {
		setAttribute(POOL, pool);
	}*/

	public void setSite(String site) {
		setAttribute(SITE, site);
	}

	public void setTestDataFile(String testDataFile) {
		setAttribute(TEST_DATA_FILE, testDataFile);
	}

	// public void setWebExceptionMessage(String webExceptionMessage) {
	// this.webExceptionMessage = webExceptionMessage;
	// }
	//
	// public void setWebExceptionURL(String webExceptionURL) {
	// this.webExceptionURL = webExceptionURL;
	// }

}
