package com.seleniumtests.core.contexts;

import org.json.JSONObject;

import com.seleniumtests.connectors.tms.TestManager;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.TestVariable;

/**
 * Class for setting / gettings test manager specific contexts
 *
 */
public class TestManagerContext {
	
	// parameters defined on startup, they sould not be modified during test
    public static final String TMS_URL = TestManager.TMS_SERVER_URL;								// URL of the test manager  (e.g: Squash TM http://<squash_host>:<squash_port>)
    public static final String TMS_USER = TestManager.TMS_USER;							// User which will access Test manager
    public static final String TMS_PASSWORD = TestManager.TMS_PASSWORD;					// password of the user which will access Test Manager
    public static final String TMS_PROJECT = TestManager.TMS_PROJECT;						// The project to which this test application is linked in Test manager    
    public static final String TMS_TYPE = TestManager.TMS_TYPE;							// Type of the Test Manager ('squash' or 'hp')

    private static final String DEFAULT_TMS_TYPE = null;
    private static final String DEFAULT_TMS_URL = null;
    
    private SeleniumTestsContext context;
    
    public TestManagerContext(SeleniumTestsContext context) {
    	this.context = context;
    }
    
	/**
	 * Init context with parameters
	 */
	public void init() {
        setTmsType(context.getValueForTest(TMS_TYPE, System.getProperty(TMS_TYPE)));
        setTmsUrl(context.getValueForTest(TMS_URL, System.getProperty(TMS_URL)));
        setTmsUser(context.getValueForTest(TMS_USER, System.getProperty(TMS_USER)));
        setTmsPassword(context.getValueForTest(TMS_PASSWORD, System.getProperty(TMS_PASSWORD)));
        setTmsProject(context.getValueForTest(TMS_PROJECT, System.getProperty(TMS_PROJECT)));
	}
	
	public void setTestId(Integer id) {
		context.getConfiguration().put(TestManager.TMS_TEST_ID, new TestVariable(TestManager.TMS_TEST_ID, id.toString()));
	}
	
	public void setCampaignName(String name) {
		if (context.getTestManagerInstance() instanceof SquashTMConnector) {
			context.getConfiguration().put(SquashTMConnector.SQUASH_CAMPAIGN, new TestVariable(SquashTMConnector.SQUASH_CAMPAIGN, name));
		} else {
			throw new UnsupportedOperationException("Setting campaign name is only possible when Test manager is of type 'squash' ");
		}
	}
	
	public String getCampaignName() {
		try {
			return context.getConfiguration().get(SquashTMConnector.SQUASH_CAMPAIGN).getValue();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public void setIterationName(String name) {
		if (context.getTestManagerInstance() instanceof SquashTMConnector) {
			context.getConfiguration().put(SquashTMConnector.SQUASH_ITERATION, new TestVariable(SquashTMConnector.SQUASH_ITERATION, name));
		} else {
			throw new UnsupportedOperationException("Setting campaign name is only possible when Test manager is of type 'squash' ");
		}
	}
	
	public String getIterationName() {
		try {
			return context.getConfiguration().get(SquashTMConnector.SQUASH_ITERATION).getValue();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * Creates the TestManager connector
	 * @return
	 */
	public TestManager createTestManagerConnector() {
		if (getTmsType() != null && getTmsUrl() != null) {
			
			// build configuration
			JSONObject jsonConfig = new JSONObject();
			jsonConfig.put(TMS_TYPE, getTmsType());
			jsonConfig.put(TMS_URL, getTmsUrl());
			jsonConfig.put(TMS_USER, getTmsUser());
			jsonConfig.put(TMS_PASSWORD, getTmsPassword());
			jsonConfig.put(TMS_PROJECT, getTmsProject());
			
			// add non standard configurations
			for (String key: context.getConfiguration().keySet()) {
				if (key.startsWith("tms")) {
					jsonConfig.put(key, context.getConfiguration().get(key).getValue());
				}
			}
			
			TestManager tms = TestManager.getInstance(jsonConfig);
			
			tms.init(jsonConfig);
			return tms;
		}
		return null;
	}
	

    public String getTmsType() {
    	return (String) context.getAttribute(TMS_TYPE);
    }
    
    public String getTmsUrl() {
    	return (String) context.getAttribute(TMS_URL);
    }
    
    public String getTmsUser() {
    	return (String) context.getAttribute(TMS_USER);
    }
    
    public String getTmsPassword() {
    	return (String) context.getAttribute(TMS_PASSWORD);
    }
    
    public String getTmsProject() {
    	return (String) context.getAttribute(TMS_PROJECT);
    }		

    public void setTmsUrl(String url) {
    	if (url != null) {
    		context.setAttribute(TMS_URL, url);
    	} else {
    		context.setAttribute(TMS_URL, DEFAULT_TMS_URL);
    	}
    }
    
    public void setTmsType(String type) {
    	if (type != null) {
    		context.setAttribute(TMS_TYPE, type);
    	} else {
    		context.setAttribute(TMS_TYPE, DEFAULT_TMS_TYPE);
    	}
    }
    
    public void setTmsUser(String user){
    	context.setAttribute(TMS_USER, user);
    }
    
    public void setTmsPassword(String password){
    	context.setAttribute(TMS_PASSWORD, password);
    }
    
    public void setTmsProject(String project){
    	context.setAttribute(TMS_PROJECT, project);
    }
}
