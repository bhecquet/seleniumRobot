package com.seleniumtests.core.contexts;

import com.seleniumtests.util.osutility.SystemUtility;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.screenshots.SnapshotComparisonBehaviour;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class SeleniumRobotServerContext {

	private static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumRobotServerContext.class);

	public static final String SELENIUMROBOTSERVER_URL = "seleniumRobotServerUrl";
    public static final String SELENIUMROBOTSERVER_ACTIVE = "seleniumRobotServerActive";
    public static final String SELENIUMROBOTSERVER_TOKEN = "seleniumRobotServerToken";
	public static final String SELENIUMROBOTSERVER_TOKEN_ENV_VAR = "SELENIUM_ROBOT_SERVER_TOKEN";
    public static final String SELENIUMROBOTSERVER_COMPARE_SNAPSHOT = "seleniumRobotServerCompareSnapshots";			// whether we should use the snapshots created by robot to compare them to a previous execution. This option only operates when SeleniumRobot server is connected
    public static final String SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL = "seleniumRobotServerSnapshotsTtl";			// Time to live of the test session on seleniumRobot server
    public static final String SELENIUMROBOTSERVER_RECORD_RESULTS = "seleniumRobotServerRecordResults";				// whether we should record test results to server. This option only operates when SeleniumRobot server is connected
    public static final String SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN = "seleniumRobotServerVariablesOlderThan";	// whether we should get from server variables which were created at least X days ago
    public static final String SELENIUMROBOTSERVER_VARIABLES_RESERVATION = "seleniumRobotServerVariablesReservation"; // duration of reservation of variable in minutes. By default, variable server reserves variable for 15 mins
    public static final String SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR = "snapshotComparisonResult";

    public static final boolean DEFAULT_SELENIUMROBOTSERVER_RECORD_RESULTS = false;
	public static final boolean DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT = false;
	public static final int DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL = 30;
	public static final int DEFAULT_SELENIUMROBOTSERVER_VARIABLES_RESERVATION = -1;
	public static final SnapshotComparisonBehaviour DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR = SnapshotComparisonBehaviour.DISPLAY_ONLY;
	public static final boolean DEFAULT_SELENIUMROBOTSERVER_ACTIVE = false;
	public static final String DEFAULT_SELENIUMROBOTSERVER_TOKEN = null;
	public static final int DEFAULT_SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN = 0;
	public static final String DEFAULT_SELENIUMROBOTSERVER_URL = null;
    
    private SeleniumTestsContext context;
    
    public SeleniumRobotServerContext(SeleniumTestsContext context) {
    	this.context = context;
    }
    
    /**
	 * Init context with parameters
	 */
	public void init() {
		setSeleniumRobotServerUrl(context.getValueForTest(SELENIUMROBOTSERVER_URL, System.getProperty(SELENIUMROBOTSERVER_URL)));
        setSeleniumRobotServerActive(context.getBoolValueForTest(SELENIUMROBOTSERVER_ACTIVE, System.getProperty(SELENIUMROBOTSERVER_ACTIVE)));

		// first read environment variable for token, then, override it if properties are set
		setSeleniumRobotServerToken(System.getenv(SELENIUMROBOTSERVER_TOKEN_ENV_VAR));
		String tokenFromConfig = context.getValueForTest(SELENIUMROBOTSERVER_TOKEN, System.getProperty(SELENIUMROBOTSERVER_TOKEN));
		if (tokenFromConfig != null) { // token set in configuration has priority over environment variable
			setSeleniumRobotServerToken(tokenFromConfig);
		}
		setSeleniumRobotServerCompareSnapshot(context.getBoolValueForTest(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, System.getProperty(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT)));
        setSeleniumRobotServerVariableReservationDuration(context.getIntValueForTest(SELENIUMROBOTSERVER_VARIABLES_RESERVATION, System.getProperty(SELENIUMROBOTSERVER_VARIABLES_RESERVATION)));
        setSeleniumRobotServerCompareSnapshotTtl(context.getIntValueForTest(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL, System.getProperty(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL)));
        setSeleniumRobotServerCompareSnapshotBehaviour(context.getValueForTest(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, System.getProperty(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR)));
        setSeleniumRobotServerRecordResults(context.getBoolValueForTest(SELENIUMROBOTSERVER_RECORD_RESULTS, System.getProperty(SELENIUMROBOTSERVER_RECORD_RESULTS)));
        setSeleniumRobotServerVariableOlderThan(context.getIntValueForTest(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN, System.getProperty(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN)));
     
	}
	

    // TODO: this call should be moved into postInit method as SeleniumRobotVariableServerConnector calls SeleniumTestsContextManager.getThreadContext() which may not be initialized
    public SeleniumRobotVariableServerConnector createSeleniumRobotServer(ITestResult testNGResult) {
    	
    	if (testNGResult == null) {
    		return null;
    	}
    	
    	// in case we find the url of variable server and it's marked as active, use it
		if (getSeleniumRobotServerActive() != null 
				&& getSeleniumRobotServerActive() 
				&& getSeleniumRobotServerUrl() != null) {
			if (System.getProperty(SeleniumRobotLogger.MAVEN_EXECUTION) == null || System.getProperty(SeleniumRobotLogger.MAVEN_EXECUTION).equals("false")) {
				logger.info(String.format("%s key found, and set to true, trying to get variable from variable server %s [%s]", 
							SELENIUMROBOTSERVER_ACTIVE, 
							getSeleniumRobotServerUrl(),
							SELENIUMROBOTSERVER_URL));
			}
			SeleniumRobotVariableServerConnector vServer = new SeleniumRobotVariableServerConnector(getSeleniumRobotServerActive(), getSeleniumRobotServerUrl(), TestNGResultUtils.getTestName(testNGResult).replaceAll("^before-", ""), getSeleniumRobotServerToken());
			
			if (!vServer.isAlive()) {
				throw new ConfigurationException(String.format("Variable server %s could not be contacted", getSeleniumRobotServerUrl()));
			}
			
			return vServer;
			
		} else {
			if (System.getProperty(SeleniumRobotLogger.MAVEN_EXECUTION) == null || System.getProperty(SeleniumRobotLogger.MAVEN_EXECUTION).equals("false")) {
				logger.info(String.format("%s key not found or set to false, or url key %s has not been set", SELENIUMROBOTSERVER_ACTIVE, SELENIUMROBOTSERVER_URL));
			}
			return null;
		}
		
    }
    
    public void setSeleniumRobotServerUrl(String url) {
    	if (url != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_URL, url);
    	} else if (SystemUtility.getenv(SELENIUMROBOTSERVER_URL) != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_URL, SystemUtility.getenv(SELENIUMROBOTSERVER_URL));
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_URL, DEFAULT_SELENIUMROBOTSERVER_URL);
    	}
    }
    
    public void setSeleniumRobotServerActive(Boolean active) {
    	if (active != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_ACTIVE, active);
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_ACTIVE, DEFAULT_SELENIUMROBOTSERVER_ACTIVE);
    	}
    	
    	if (getSeleniumRobotServerUrl() == null && getSeleniumRobotServerActive()) {
    		throw new ConfigurationException("SeleniumRobot server is requested but URL is not found, either in parameters, command line or through environment variable");
    	}
    }
    
    public void setSeleniumRobotServerToken(String token) {
    	if (token != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_TOKEN, token);
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_TOKEN, DEFAULT_SELENIUMROBOTSERVER_TOKEN);
    	}
    }
    
    public void setSeleniumRobotServerCompareSnapshot(Boolean capture) {
    	if (capture != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, capture);
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT, DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
    	}
    }
    
    public void setSeleniumRobotServerCompareSnapshotBehaviour(String compareSnapshotBehaviour) {
    	if (compareSnapshotBehaviour != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, SnapshotComparisonBehaviour.fromString(compareSnapshotBehaviour));
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR, DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
    	}
    }
    
    public void setSeleniumRobotServerVariableOlderThan(Integer olderThan) {
    	if (olderThan != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN, olderThan);
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN, DEFAULT_SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN);
    	}
    }
    
    /**
     * set time to live for snapshot comparison session
     * @param timeToLive
     */
    public void setSeleniumRobotServerCompareSnapshotTtl(Integer timeToLive) {
    	if (timeToLive != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL, timeToLive);
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL, DEFAULT_SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL);
    	}
    }
    
    public void setSeleniumRobotServerVariableReservationDuration(Integer reservationDuration) {
    	if (reservationDuration != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_VARIABLES_RESERVATION, reservationDuration);
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_VARIABLES_RESERVATION, DEFAULT_SELENIUMROBOTSERVER_VARIABLES_RESERVATION);
    	}
    }
    
    public void setSeleniumRobotServerRecordResults(Boolean recordResult) {
    	if (recordResult != null) {
    		context.setAttribute(SELENIUMROBOTSERVER_RECORD_RESULTS, recordResult);
    	} else {
    		context.setAttribute(SELENIUMROBOTSERVER_RECORD_RESULTS, DEFAULT_SELENIUMROBOTSERVER_RECORD_RESULTS);
    	}
    }
	   
    public String getSeleniumRobotServerUrl() {
    	return (String) context.getAttribute(SELENIUMROBOTSERVER_URL);
    }
    
    public Boolean getSeleniumRobotServerActive() {
    	return (Boolean) context.getAttribute(SELENIUMROBOTSERVER_ACTIVE);
    }
    
    public String getSeleniumRobotServerToken() {
    	return (String) context.getAttribute(SELENIUMROBOTSERVER_TOKEN);
    }
    
    public boolean getSeleniumRobotServerCompareSnapshot() {
    	return (Boolean) context.getAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT);
    }
    
    public SnapshotComparisonBehaviour getSeleniumRobotServerCompareSnapshotBehaviour() {
    	return (SnapshotComparisonBehaviour) context.getAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_BEHAVIOUR);
    }
    
    public Integer getSeleniumRobotServerVariableOlderThan() {
    	return (Integer) context.getAttribute(SELENIUMROBOTSERVER_VARIABLES_OLDER_THAN);
    }
    
    public Integer getSeleniumRobotServerCompareSnapshotTtl() {
    	return (Integer) context.getAttribute(SELENIUMROBOTSERVER_COMPARE_SNAPSHOT_TTL);
    }
    
    public Integer getSeleniumRobotServerVariableReservationDuration() {
    	return (Integer) context.getAttribute(SELENIUMROBOTSERVER_VARIABLES_RESERVATION);
    }
    
    public boolean getSeleniumRobotServerRecordResults() {
    	return (Boolean) context.getAttribute(SELENIUMROBOTSERVER_RECORD_RESULTS);
    }
    
}
