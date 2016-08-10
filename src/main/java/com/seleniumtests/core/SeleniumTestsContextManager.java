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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.ITestContext;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.TestConfigurationParser;
import org.apache.log4j.Logger;
import com.seleniumtests.reporter.TestLogging;

/**
 * SeleniumTestsContextManager provides ways to manage global context, thread context and test level context.
 */
public class SeleniumTestsContextManager {

	private static final Logger logger = TestLogging.getLogger(SeleniumTestsContext.class);
	
	private static String rootPath;
	private static String dataPath;
	private static String featuresPath;
	private static String configPath;
	private static String applicationName;
	private static Boolean deployedMode;

	public static final String DATA_FOLDER_NAME = "data";

    // context listener
    private static List<IContextAttributeListener> contextAttributeListeners = Collections.synchronizedList(new ArrayList<IContextAttributeListener>());

    // global level context
    private static SeleniumTestsContext globalContext;

    // test level context
    private static Map<String, SeleniumTestsContext> testLevelContext = Collections.synchronizedMap(new HashMap<>());

    // thread level SeleniumTestsContext
    private static ThreadLocal<SeleniumTestsContext> threadLocalContext = new ThreadLocal<>();

    private SeleniumTestsContextManager() {
		// As a utility class, it is not meant to be instantiated.
	}
    
    public static void addContextAttributeListener(final IContextAttributeListener listener) {
        contextAttributeListeners.add(listener);
    }

    public static SeleniumTestsContext getGlobalContext() {
        if (globalContext == null) {
            logger.info("Initialize default GlobalContext");
            initGlobalContext(new DefaultTestNGContext());
        }

        return globalContext;
    }

    public static SeleniumTestsContext getTestLevelContext(final ITestContext testContext) {
        if (testContext != null && testContext.getCurrentXmlTest() != null) {
            if (testLevelContext.get(testContext.getCurrentXmlTest().getName()) == null) {
                initTestLevelContext(testContext, testContext.getCurrentXmlTest());
            }

            return testLevelContext.get(testContext.getCurrentXmlTest().getName());
        } else {
            return null;
        }
    }

    public static SeleniumTestsContext getTestLevelContext(final String testName) {
        return testLevelContext.get(testName);
    }

    public static SeleniumTestsContext getThreadContext() {
        if (threadLocalContext.get() == null) {
            logger.info("Initialize default ThreadContext");
            initThreadContext(null);
        }

        return threadLocalContext.get();
    }

    public static void initGlobalContext(ITestContext testNGCtx) {
    	
    	// generate all paths used by test application
    	if (testNGCtx != null && testNGCtx.getCurrentXmlTest() != null) {
        	generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
        }
    	
    	ITestContext newTestNGCtx = getContextFromConfigFile(testNGCtx);
        globalContext = new SeleniumTestsContext(newTestNGCtx);
        loadCustomizedContextAttribute(newTestNGCtx, globalContext);
    }

    /**
     * Get parameters from configuration file.
     * @param iTestContext
     * @param configParser
     * @return Map with parameters from the given file.
     */
    private static Map<String, String> getParametersFromConfigFile(final ITestContext iTestContext, 
    																TestConfigurationParser configParser) {
        Map<String, String> parameters;
        
        // get parameters
        if (iTestContext.getCurrentXmlTest() != null) {
        	parameters = iTestContext.getCurrentXmlTest().getSuite().getParameters();
        } else {
        	parameters = iTestContext.getSuite().getXmlSuite().getParameters();
        }
        // insert parameters
        for (Node node: configParser.getParameterNodes()) {
            parameters.put(node.getAttributes().getNamedItem("name").getNodeValue(),
            		       node.getAttributes().getNamedItem("value").getNodeValue());
        }
        return parameters;
    }
    
    /**
     * 
     * @param iTestContext
     * @return run mode corresponding to the given test context
     */
    private static String setRunMode(final ITestContext iTestContext){
    	String runMode;
		if (System.getProperty(SeleniumTestsContext.RUN_MODE) != null) {
			runMode = System.getProperty(SeleniumTestsContext.RUN_MODE);
		} else if (iTestContext.getSuite().getParameter(SeleniumTestsContext.RUN_MODE) != null) {
			runMode = iTestContext.getSuite().getParameter(SeleniumTestsContext.TEST_CONFIGURATION);
		} else {
			runMode = "LOCAL";
		}
		return runMode;
    }
    
    /**
     * Get service parameters from configuration file.
     * Only the parameters corresponding to the defined runMode.
     * @param parameters
     * @param runMode
     * @param iTestContext
     * @param configParser
     * @return Map with service parameters from the given file.
     */
    private static Map<String, String> getServiceParameters(Map<String, String> parameters, String runMode, TestConfigurationParser configParser) {
    	
    	for (Node node: configParser.getServiceNodes()) {
    		
        	if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase(runMode)) {
        		
        		NodeList nList = node.getChildNodes();
        		for (int i = 0; i < nList.getLength(); i++ ) {
        			Node paramNode = nList.item(i);
        			if ("parameter".equals(paramNode.getNodeName())) {
            			parameters.put(paramNode.getAttributes().getNamedItem("name").getNodeValue(),
            						   paramNode.getAttributes().getNamedItem("value").getNodeValue());
        			}
        		}
        	}
        }
    	return parameters;
    }
    
    /**
     * Set the parameters for the test with parameters from XML configuration file.
     * @param   iTestContext
     * @return  iTestContext set with parameters from external config file
     */
    public static ITestContext getContextFromConfigFile(final ITestContext iTestContext) {
        if (iTestContext != null
        	&& iTestContext.getSuite().getParameter(SeleniumTestsContext.TEST_CONFIGURATION) != null) {
            	
            	File suiteFile = new File(iTestContext.getSuite().getXmlSuite().getFileName());
                String configFile = suiteFile.getPath().replace(suiteFile.getName(), "") + iTestContext.getSuite().getParameter("testConfig");
                
                TestConfigurationParser configParser = new TestConfigurationParser(configFile);
            	
                Map<String, String> parameters = getParametersFromConfigFile(iTestContext, configParser);
                
                // get configuration for services.
	            String runMode = setRunMode(iTestContext);
	            parameters = getServiceParameters(parameters, runMode, configParser);
                
                // 
                parameters.put(SeleniumTestsContext.DEVICE_LIST, configParser.getDeviceNodesAsJson());
                    
                if (iTestContext.getCurrentXmlTest() != null) {
                	iTestContext.getCurrentXmlTest().getSuite().setParameters(parameters);
                } else {
                	iTestContext.getSuite().getXmlSuite().setParameters(parameters);
                }
        }

        return iTestContext;
    }

    public static void initTestLevelContext(final ITestContext testNGCtx, final XmlTest xmlTest) {
        SeleniumTestsContext seleniumTestsCtx = new SeleniumTestsContext(testNGCtx);
        if (xmlTest != null) {
            Map<String, String> testParameters = xmlTest.getTestParameters();

            // parse the test level parameters
            for (Entry<String, String> entry : testParameters.entrySet()) {
                seleniumTestsCtx.setAttribute(entry.getKey(), entry.getValue());
            }

            testLevelContext.put(xmlTest.getName(), seleniumTestsCtx);
            
            // update some values after init
            seleniumTestsCtx.postInit();
        }
    }

    public static void initTestLevelContext(final XmlTest xmlTest) {
        initTestLevelContext(globalContext.getTestNGContext(), xmlTest);
    }

    public static void initThreadContext() {
        initThreadContext(globalContext.getTestNGContext());
    }

    public static void initThreadContext(ITestContext testNGCtx) {
    	
    	// generate all paths used by test application
    	if (testNGCtx != null && testNGCtx.getCurrentXmlTest() != null) {
        	generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
        }
    	
    	ITestContext _testNGCtx = getContextFromConfigFile(testNGCtx);
    	SeleniumTestsContext seleniumTestsCtx = new SeleniumTestsContext(_testNGCtx);
        loadCustomizedContextAttribute(_testNGCtx, seleniumTestsCtx);
        
        threadLocalContext.set(seleniumTestsCtx);
        
        // update some values after init
        seleniumTestsCtx.postInit();

    }

    private static void loadCustomizedContextAttribute(final ITestContext testNGCtx, final SeleniumTestsContext seleniumTestsCtx) {
        for (int i = 0; i < contextAttributeListeners.size(); i++) {
            contextAttributeListeners.get(i).load(testNGCtx, seleniumTestsCtx);
        }
    }

    public static void setGlobalContext(final SeleniumTestsContext ctx) {
        globalContext = ctx;
    }

    public static void setThreadContext(final SeleniumTestsContext ctx) {
        threadLocalContext.set(ctx);
    }
    
    /**
     * Build the root path of STF 
     * method for guessing it is different if we are inside a jar (built mode) or in development
     * @param clazz
     * @param path
     * @return
     */
    private static void getPathFromClass(Class clazz, StringBuilder path) {
		
		try {
			String url = URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8" );
			if (url.endsWith(".jar")) {
				path.append((new File(url).getParentFile().getAbsoluteFile().toString() + "/").replace(File.separator, "/"));
				deployedMode = true;
			} else {				
				path.append((new File(url).getParentFile().getParentFile().getAbsoluteFile().toString() + "/").replace(File.separator, "/"));
				deployedMode = false;
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}
	}
    
    /**
     * Generate all applications path
     * - root
     * - data
     * - config
     * @param xmlSuite
     */
    public static void generateApplicationPath(XmlSuite xmlSuite) {

		StringBuilder path = new StringBuilder();
		getPathFromClass(SeleniumTestsContext.class, path);
		
		rootPath = path.toString();
		
		// in case launching unit test from eclipse, a temp file is generated outside the standard folder structure
		// APPLICATION_NAME and DATA_PATH must be rewritten
		try {
			applicationName = xmlSuite.getFileName().replace(File.separator, "/").split("/"+ DATA_FOLDER_NAME + "/")[1].split("/")[0];
			dataPath = xmlSuite.getFileName().replace(File.separator, "/").split("/"+ DATA_FOLDER_NAME + "/")[0] + "/" + DATA_FOLDER_NAME + "/";
		} catch (IndexOutOfBoundsException e) {
			applicationName = "core";
			dataPath = Paths.get(rootPath, "data").toString();
		}
		
		featuresPath = Paths.get(dataPath, applicationName, "features").toString();
		configPath = Paths.get(dataPath, applicationName, "config").toString();
		
		// create data folder if it does not exist (it should already exist)
		if (!new File(dataPath).isDirectory()) {
			new File(dataPath).mkdirs();
		}
	}
    
    /**
     * Returns application root path
     * @return
     */
    public static String getRootPath() {
		return rootPath;
	}

    /**
     * Returns location of feature files
     * @return
     */
	public static String getFeaturePath() {
		return featuresPath;
	}

	/**
	 * Returns location of config files
	 * @return
	 */
	public static String getConfigPath() {
		return configPath;
	}
	
	/**
	 * Returns location of data folder
	 * @return
	 */
	public static String getDataPath() {
		return dataPath;
	}

    public static Boolean getDeployedMode() {
    	if (deployedMode == null) {
    		getPathFromClass(SeleniumTestsContext.class, new StringBuilder());
    	}
		return deployedMode;
	}

	public static boolean isWebTest() {
        return getThreadContext().getTestType().family().equals(TestType.WEB);
    }
    
	public static boolean isNonGuiTest() {
		return getThreadContext().getTestType().family().equals(TestType.NON_GUI);
	}
	
    public static boolean isMobileAppTest() {
    	return getThreadContext().getTestType().family().equals(TestType.APP);
    }
}
