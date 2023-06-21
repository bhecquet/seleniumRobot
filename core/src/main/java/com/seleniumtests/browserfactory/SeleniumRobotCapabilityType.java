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
package com.seleniumtests.browserfactory;

public interface SeleniumRobotCapabilityType {


	String APPIUM_PREFIX = "appium:";
	
	/**
	 * parameter to force a test to execute on a specific node or set of nodes
	 * value must be a list of strings
	 */
	String NODE_TAGS = "sr:nodeTags"; 
	
	
	/**
	 * parameter to route browser creation on a specific node on grid even if maxSession is reached
	 */
	String ATTACH_SESSION_ON_NODE = "sr:attachSessionOnNode"; 
	
	/**
	 * name of the running test
	 */
	String TEST_NAME = "sr:testName";
	
	/**
	 * Capability giving information on the beta status of the browser
	 */
	String BETA_BROWSER = "sr:beta";
	
	/**
	 * Whether Edge should be started in IE mode
	 */
	String EDGE_IE_MODE = "sr:ieMode";
	
	/**
	 * Path to the chrome profile (or "default" to use the default user profile)
	 */
	String CHROME_PROFILE = "sr:chromeProfile";
	
	/**
	 * Path to the edge profile (or "default" to use the default user profile)
	 */
	String EDGE_PROFILE = "sr:edgeProfile";
	
	/**
	 * Path to the edge profile (or "default" to use the default user profile)
	 */
	String FIREFOX_PROFILE = "sr:firefoxProfile";
	
	/**
	 * Any string representing who started the test
	 */
	String STARTED_BY = "sr:startedBy";
	
	/**
	 * Informations for driver usage
	 */
	String GRID_HUB = "sr:gridHub";
	String GRID_NODE = "sr:gridNode";
	String GRID_NODE_URL = "sr:gridNodeUrl";
	String START_TIME = "sr:startTime";
	String DURATION = "sr:duration";
	String SESSION_ID = "sr:sessionId";
	String BROWSER = "sr:browser";
	String STARTUP_DURATION = "sr:startupDuration";
}
