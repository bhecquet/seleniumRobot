package com.seleniumtests.core.contexts;

import java.util.HashMap;
import java.util.Map;

import com.seleniumtests.connectors.bugtracker.BugTracker;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.TestVariable;

public class BugTrackerContext {

	public static final String BUGTRACKER_TYPE = BugTracker.BUGTRACKER_TYPE;
	public static final String BUGTRACKER_URL = BugTracker.BUGTRACKER_URL;
	public static final String BUGTRACKER_PROJECT = BugTracker.BUGTRACKER_PROJECT;
	public static final String BUGTRACKER_USER = BugTracker.BUGTRACKER_USER;
	public static final String BUGTRACKER_PASSWORD = BugTracker.BUGTRACKER_PASSWORD;
	
	public static final String DEFAULT_BUGTRACKER_URL = null;
	public static final String DEFAULT_BUGTRACKER_TYPE = null;
	   
    private SeleniumTestsContext context;
    
    public BugTrackerContext(SeleniumTestsContext context) {
    	this.context = context;
    }
    
   	/**
   	 * Init context with parameters
   	 */
   	public void init() {

        setBugtrackerType(context.getValueForTest(BUGTRACKER_TYPE, System.getProperty(BUGTRACKER_TYPE)));
        setBugtrackerUrl(context.getValueForTest(BUGTRACKER_URL, System.getProperty(BUGTRACKER_URL)));
        setBugtrackerUser(context.getValueForTest(BUGTRACKER_USER, System.getProperty(BUGTRACKER_USER)));
        setBugtrackerPassword(context.getValueForTest(BUGTRACKER_PASSWORD, System.getProperty(BUGTRACKER_PASSWORD)));
        setBugtrackerProject(context.getValueForTest(BUGTRACKER_PROJECT, System.getProperty(BUGTRACKER_PROJECT)));
   	}
   	

	public BugTracker createBugtracker() {
		if (getBugtrackerType() != null && getBugtrackerUrl() != null) {
			
			// any options specific to this bugtracker may be given in the form 'bugtracker.xxx'
			Map<String, String> bugtrackerOptions = new HashMap<>();
			for (TestVariable variable: context.getConfiguration().values()) {
				if (variable.getName().startsWith(BugTracker.BUGTRACKER_PREFIX)) {
					bugtrackerOptions.put(variable.getName(), variable.getValue());
				}
			}
			
			return BugTracker.getInstance(getBugtrackerType(), getBugtrackerUrl(), getBugtrackerProject(), getBugtrackerUser(), getBugtrackerPassword(), bugtrackerOptions);
		}
		return null;
	}
	

    public String getBugtrackerType() {
    	return (String) context.getAttribute(BUGTRACKER_TYPE);
    }
    
    public String getBugtrackerUrl() {
    	return (String) context.getAttribute(BUGTRACKER_URL, true);
    }
    
    public String getBugtrackerUser() {
    	return (String) context.getAttribute(BUGTRACKER_USER, true);
    }
    
    public String getBugtrackerPassword() {
    	return (String) context.getAttribute(BUGTRACKER_PASSWORD, true);
    }
    
    public String getBugtrackerProject() {
    	return (String) context.getAttribute(BUGTRACKER_PROJECT, true);
    }

    public void setBugtrackerUrl(String url) {
    	if (url != null) {
    		context.setAttribute(BUGTRACKER_URL, url);
    	} else {
    		context.setAttribute(BUGTRACKER_URL, DEFAULT_BUGTRACKER_URL);
    	}
    }
    
    public void setBugtrackerType(String type) {
    	if (type != null) {
    		context.setAttribute(BUGTRACKER_TYPE, type);
    	} else {
    		context.setAttribute(BUGTRACKER_TYPE, DEFAULT_BUGTRACKER_TYPE);
    	}
    }
    
    public void setBugtrackerUser(String user){
    	context.setAttribute(BUGTRACKER_USER, user);
    }
    
    public void setBugtrackerPassword(String password){
    	context.setAttribute(BUGTRACKER_PASSWORD, password);
    }
    
    public void setBugtrackerProject(String project){
    	context.setAttribute(BUGTRACKER_PROJECT, project);
    }
    
}
