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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.ConfigurationMethod;
import org.testng.xml.XmlSuite;

import com.seleniumtests.core.runner.SeleniumRobotTestPlan;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * SeleniumTestsContextManager provides ways to manage global context, thread context and test level context.
 */
public class SeleniumTestsContextManager {

	private static final Logger logger = SeleniumRobotLogger.getLogger(SeleniumTestsContextManager.class);
	
	private static String rootPath;
	private static String dataPath;
	private static String cachePath;
	private static String appDataPath;
	private static String featuresPath;
	private static String configPath;
	private static String applicationName;
	private static String applicationNameWithVersion;
	private static String applicationVersion;
	private static String coreVersion;
	private static String applicationFullVersion;
	private static String coreFullVersion;
	private static Boolean deployedMode;

	public static final String DATA_FOLDER_NAME = "data";
	public static final String CACHE_FOLDER_NAME = "cache";
	public static final String SELENIUM_VERSION = "4.3.0";

    // global level context
    private static SeleniumTestsContext globalContext;

    // thread level SeleniumTestsContext
    private static ThreadLocal<SeleniumTestsContext> threadLocalContext = new ThreadLocal<>();
    
    // relationship between a SeleniumTestsContext and a TestNG test (<test> tag in XML)
    private static Map<String, SeleniumTestsContext> testContext = Collections.synchronizedMap(new HashMap<>());
    
    // relationship between a SeleniumTestsContext and a test class
    private static Map<String, SeleniumTestsContext> classContext = Collections.synchronizedMap(new HashMap<>());
    
    // relationship between a SeleniumTestsContext and a test method
    private static Map<String, SeleniumTestsContext> methodContext = Collections.synchronizedMap(new HashMap<>());
    
    // relationship between a ITestResult and its context so that we have a location where to search
    private static Map<ITestResult, SeleniumTestsContext> testResultContext = Collections.synchronizedMap(new LinkedHashMap<>());

    private SeleniumTestsContextManager() {
		// As a utility class, it is not meant to be instantiated.
	}

    public static SeleniumTestsContext getGlobalContext() {
        if (globalContext == null) {
        	throw new ConfigurationException("SeleniumTestsContextManager.getGlobalContext() MUST be called after SeleniumTestsContextManager.initGlobalContext()");
        }

        return globalContext;
    }

    public static SeleniumTestsContext getThreadContext() {
        if (threadLocalContext.get() == null) {
        	throw new ConfigurationException("SeleniumTestsContextManager.getThreadContext() MUST be called after SeleniumTestsContextManager.initThreadContext()");
        }

        return threadLocalContext.get();
    }
    
    public static void initGlobalContext(ISuite suiteContext) {
    	if (suiteContext != null ) {
        	generateApplicationPath(suiteContext.getXmlSuite());
        }
    	
    	ITestContext testNGCtx = new DefaultTestNGContext(suiteContext);
        globalContext = new SeleniumTestsContext(testNGCtx);
    }
    
    public static void initGlobalContext(ITestContext testNGCtx) {
    	
    	// generate all paths used by test application
    	if (testNGCtx != null && testNGCtx.getCurrentXmlTest() != null) {
        	generateApplicationPath(testNGCtx.getCurrentXmlTest().getSuite());
        }
    	
        globalContext = new SeleniumTestsContext(testNGCtx);
    }
    
    private static String getKeyForMethod(ITestContext testNGCtx, String className, String methodName) {
    	if (methodName != null && className != null) {
        	return getKeyForClass(testNGCtx, className) + "." + methodName;
    	} else {
    		return null;
    	}
    }
    private static String getKeyForClass(ITestContext testNGCtx, String className) {
    	if (className != null) {
    		return testNGCtx.getName() + "_" + className;
    	} else {
    		return null;
    	}
    }
    
    private static SeleniumTestsContext storeTestContext(ITestContext testNGCtx) {
    	SeleniumTestsContext tstContext = getOrCreateContext(testNGCtx, null, null, true);
    	setTestContext(testNGCtx, tstContext);
    	return tstContext;
    }
    
    private static void setTestContext(ITestContext testNGCtx, SeleniumTestsContext tstContext) {
    	if (testNGCtx != null) {
    		testContext.put(testNGCtx.getName(), tstContext);
    	}
    }
    
    /**
     * Returns test context if it exists. Else, create a new one
     * @param testNGCtx
     * @return
     */
    private static SeleniumTestsContext getTestContext(ITestContext testNGCtx) {
    	return getOrCreateContext(testNGCtx, null, null, false);
    }
    
    private static SeleniumTestsContext storeClassContext(ITestContext testNGCtx, String className) {
    	// unicity is on test + class because a class could be executed by 2 tests at the same time (e.g: ParallelMode.TESTS)
    	SeleniumTestsContext clsContext = getOrCreateContext(testNGCtx, className, null, true);
    	setClassContext(testNGCtx, className, clsContext);
    	return clsContext;
    }
    
    private static void setClassContext(ITestContext testNGCtx, String className, SeleniumTestsContext clsContext) {
    	classContext.put(getKeyForClass(testNGCtx, className), clsContext);
    }
    
    /**
     * Returns class context from test NG context and class name
     * @param testNGCtx
     * @param className
     * @return
     */
    private static SeleniumTestsContext getClassContext(ITestContext testNGCtx, String className) {
    	return getOrCreateContext(testNGCtx, className, null, false);
    }
    private static SeleniumTestsContext storeMethodContext(ITestContext testNGCtx, String className, String methodName) {
    	// unicity is on test + class + method because the same method name may exist in several classes or 2 testNG tests could execute the same test methods 
    	SeleniumTestsContext mtdContext = getMethodContext(testNGCtx, className, methodName, true);
    	setMethodContext(testNGCtx, className, methodName, mtdContext);
    	return mtdContext;
    }
    private static void setMethodContext(ITestContext testNGCtx, String className, String methodName, SeleniumTestsContext mtdContext) {
    	methodContext.put(getKeyForMethod(testNGCtx, className, methodName), mtdContext);
    }
    
    private static SeleniumTestsContext getMethodContext(ITestContext testNGCtx, String className, String methodName, boolean createCopy) {
    	return getOrCreateContext(testNGCtx, className, methodName, createCopy);
    }
    

    /**
     * Get the context that will be used for the test method
     * Search for (by order) a specific method context, class context, test context. If none is found, create one 
     * If found, returns a copy of the context if 'createCopy' is true
     * @param testNGCtx			
     * @param className			class name. May be null and in this case, search only by testNGCtx
     * @param methodName		method name. May be null and in this case, search by class name and testNGCtx
     * @param createCopy		if true and we find class or test context, returns a copy
     * @return
     */
    private static SeleniumTestsContext getOrCreateContext(ITestContext testNGCtx, String className, String methodName, boolean createCopy) {
    	
    	// unicity is on test + class + method because the same method name may exist in several classes or 2 testNG tests could execute the same test methods 
    	String keyMethod = getKeyForMethod(testNGCtx, className, methodName);
    	String keyClass = getKeyForClass(testNGCtx, className);
    	
    	if (keyMethod != null && methodContext.get(keyMethod) != null) {
    		return methodContext.get(keyMethod);
    		
    	} else if (keyClass != null && classContext.get(keyClass) != null) {
    		// we need a copy of class context when we search a method context but cannot find one. So we copy it from class context
    		return (createCopy && keyMethod != null) ? 
    				new SeleniumTestsContext(classContext.get(keyClass)): 
    				classContext.get(keyClass);
    				
    	} else if (testNGCtx != null && testContext.get(testNGCtx.getName()) != null) {
    		// we need a copy of test context when we search for method or class context but cannot find any of them. So we copy it from text context
    		return (createCopy && (keyMethod != null || keyClass != null)) ? 
    				new SeleniumTestsContext(testContext.get(testNGCtx.getName())): 
    				testContext.get(testNGCtx.getName());
    				
    	} else {
    		return new SeleniumTestsContext(testNGCtx);
    	}
    }
    
    /**
     * Save the thread context to it's location. 
     * If we are in {@BeforeTest} , save the thread context to testContext object
     * If we are in {@BeforeClass} , save the thread context to classContext object
     * If we are in {@BeforeMethod}, save the thread context to methodContext object
     * 
     * This way, any change made in the context in a configuration method can be reused in other configuration method or the test method itself
     * Changes made in {@AfterXXX} method are not saved
     * @param method
     * @param testResult
     * @param context
     */
    public static void saveThreadContext(IInvokedMethod method, ITestResult testResult, ITestContext context) {
    	ConfigurationMethod configMethod = (ConfigurationMethod)method.getTestMethod();
		
		// store the current thread context back to test/class/method context as it may have been modified in "Before" methods
		if (configMethod.isBeforeTestConfiguration()) {
			SeleniumTestsContextManager.setTestContext(context, getThreadContext());
			
		} else if (configMethod.isBeforeClassConfiguration()) {
			SeleniumTestsContextManager.setClassContext(context, method.getTestMethod().getTestClass().getName(), getThreadContext());
			
		} else if (configMethod.isBeforeMethodConfiguration()) {
			try {
				SeleniumTestsContextManager.setMethodContext(context, 
						getClassNameFromMethodConfiguration(testResult), 
						getMethodNameFromMethodConfiguration(testResult), 
						getThreadContext());
			} catch (Exception e) {
				// nothing
			}
		} else if (configMethod.isAfterMethodConfiguration()) {
			// issue #254: forget the variables got from server once test method is finished so that, on retry, variable can be get
			SeleniumTestsContextManager.setMethodContext(context, 
					getClassNameFromMethodConfiguration(testResult), 
					getMethodNameFromMethodConfiguration(testResult), 
					null);
		}
    }
    
    /**
     * {@BeforeMethod} and {@AfterMethod} must define a java.lang.reflect.Method object as first parameter, so read it to get test method name, associated to this configuration method
     * @param testResult
     * @return	the method name of the test method
     */
    private static String getMethodNameFromMethodConfiguration(ITestResult testResult) {
    	try {
    		return ((Method)(testResult.getParameters()[0])).getName();
    	} catch (Exception e) {
    		if (testResult.getMethod().getConstructorOrMethod().getMethod().getParameterCount() == 0 
    				|| !testResult.getMethod().getConstructorOrMethod().getMethod().getParameterTypes()[0].isAssignableFrom(Method.class) ) {
				throw new ScenarioException("When using @BeforeMethod / @AfterMethod in tests, this method MUST have a 'java.lang.reflect.Method' object as first argument. Example: \n\n"
						+ "@BeforeMethod\n" + 
						"public void beforeMethod(Method method) {\n"
						+ "    SeleniumTestsContextManager.getThreadContext().setAttribute(\"some attribute\", \"attribute value\");\n"
						+ "}\n\n");
    		} else {
    			throw e;
    		}
		}
    }
    
    /**
     * {@BeforeMethod} and {@AfterMethod} must define a java.lang.reflect.Method object as first parameter, so read it to get test class name, associated to this configuration method
     * @param testResult
     * @return the class name of the test method
     */
    private static String getClassNameFromMethodConfiguration(ITestResult testResult) {
    	try {
    		return ((Method)(testResult.getParameters()[0])).getDeclaringClass().getName();
    		
    	} catch (Exception e) {
    		if (testResult.getMethod().getConstructorOrMethod().getMethod().getParameterCount() == 0 
    				|| !testResult.getMethod().getConstructorOrMethod().getMethod().getParameterTypes()[0].isAssignableFrom(Method.class) ) {
				throw new ScenarioException("When using @BeforeMethod / @AfterMethod in tests, this method MUST have a 'java.lang.reflect.Method' object as first argument. Example: \n\n"
						+ "@BeforeMethod\n" + 
						"public void beforeMethod(Method method) {\n"
						+ "    SeleniumTestsContextManager.getThreadContext().setAttribute(\"some attribute\", \"attribute value\");\n"
						+ "}\n\n");
    		} else {
    			throw e;
    		}
		}
    }
    
    /**
     * Selects the right context to insert into thread context for use in the subsequent methods
     * This method shoul
     */
    public static void insertThreadContext(ITestNGMethod method, ITestResult testResult, ITestContext context) {
    	SeleniumTestsContext currentContext = null;
		boolean configureContext = false; // whether we should configure context (calling updateThreadContext) after inserting it
		
		// check if we already have a test context. Else, it will be created
		
		if (method.isBeforeTestConfiguration()) {
			currentContext = storeTestContext(context);
			
		// check if we already have a class context. Else, it will be copied from test context
		} else if (method.isBeforeClassConfiguration()) {
			currentContext = storeClassContext(context, method.getTestClass().getName());
			
		// check if we already have a method context. Else copy it from class or test context.
		} else if (method.isBeforeMethodConfiguration()) {
			currentContext = storeMethodContext(context, 
					getClassNameFromMethodConfiguration(testResult), 
					getMethodNameFromMethodConfiguration(testResult));
	
			// issue #137: be sure that driver created in @BeforeMethod has the same set of parameters as a driver created in @Test method
			// 			behavior is undefined if used inside a cucumber test
			if (!method.getConstructorOrMethod().getMethod().getDeclaringClass().equals(SeleniumRobotTestPlan.class)) {
				configureContext = true;
			}
			
		// handle some after methods. No change in context in after method will be recorded
		} else if (method.isAfterMethodConfiguration()) {
			// beforeMethod, testMethod and afterMethod run in the same thread, so it's safe to take the current context
			currentContext = getThreadContext();
			
			try {
				getMethodNameFromMethodConfiguration(testResult);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			
		} else if (method.isAfterClassConfiguration()) {
			currentContext = getClassContext(context, method.getTestClass().getName());
			
		} else if (method.isAfterTestConfiguration()) {
			currentContext = getTestContext(context);
			
		} else if (method.isTest()) {
			String className = method.getTestClass().getName();
			String methodName = TestNGResultUtils.getTestMethodName(testResult);
			if (methodName == null) { // happens when test is skipped (due to configuration error, we never start method execution)
				methodName = TestNGResultUtils.getTestName(testResult);
			}

			// when @BeforeMethod has been used, threadContext is already initialized and may have been updated. Do not overwrite options
			// only reconfigure it
			// create a new context from the method context so that the same test method with different data do not share the context (issue #115)
			currentContext = new SeleniumTestsContext(getMethodContext(context, 
					className, 
					methodName, 
					true), false);
			
			// allow driver to be created		
			currentContext.setDriverCreationBlocked(false);
			currentContext.setTestMethodSignature(methodName);
			
			// we will be in the test method, configure context, call variable server, ...
			configureContext = true;
		}
		
		
		if (currentContext != null) {
			SeleniumTestsContextManager.setThreadContext(currentContext);
		} else {
			SeleniumTestsContextManager.initThreadContext();
		}
		
		if (configureContext) {
			updateThreadContext(testResult);
		}
    	
    }

    public static Map<String, SeleniumTestsContext> getTestContext() {
		return testContext;
	}

	public static Map<String, SeleniumTestsContext> getClassContext() {
		return classContext;
	}

	public static Map<String, SeleniumTestsContext> getMethodContext() {
		return methodContext;
	}


    public static void initThreadContext() {
        initThreadContext(globalContext.getTestNGContext(), null);
    }

    public static void initThreadContext(ITestContext testNGCtx, ITestResult testResult) {

    	SeleniumTestsContext seleniumTestsCtx = new SeleniumTestsContext(testNGCtx);
        
        threadLocalContext.set(seleniumTestsCtx);
        
        // update some values after init. These init call the thread context previously created
        if (testResult != null) {
        	seleniumTestsCtx.configureContext(testResult);
        }
    }
    
    /**
     * Update the current thread context without recreating it
     * This is a correction for issue #94
     * @param testName
     */
    public static void updateThreadContext(ITestResult testResult) {
    	if (threadLocalContext.get() != null) {
    		threadLocalContext.get().configureContext(testResult);
    		
    		// issue #283: store test context as soon as we have it
    		TestNGResultUtils.setSeleniumRobotTestContext(testResult, getThreadContext());
    		
    		// issue #116: store each context related to a test method (through ITestResult object)
    		if (testResult.getMethod().isTest()) {
    			testResultContext.put(testResult, getThreadContext());
    		}
    	}
    }

    public static void setGlobalContext(final SeleniumTestsContext ctx) {
        globalContext = ctx;
    }

    public static void setThreadContext(final SeleniumTestsContext ctx) {
        threadLocalContext.set(ctx);
    }
    
    /**
     * Returns the best context for the state of the current running result
     * - if in BeforeMethod / TestMethod / AfterMethod => current Thread context
     * - if in AfterClass => search for the last test result method that belongs to this class
     * - if in AfterTest => search for the last test result method that belongs to this test
     */
    public static List<SeleniumTestsContext> getContextForCurrentTestState() {
    	List<SeleniumTestsContext> matchingContexts = new ArrayList<>();
    	
    	ITestResult testResult = Reporter.getCurrentTestResult();
    	if (testResult == null) {
    		return matchingContexts;
    	}
    	
    	ITestNGMethod method = testResult.getMethod();

    	// for all @BeforeXX configuration methods and test method, return the thread context as it will have been insterted before call to the configuration method (in SeleniumRobotTestListener)
		if (method.isBeforeTestConfiguration() || method.isBeforeClassConfiguration() || method.isBeforeMethodConfiguration() || method.isTest() || method.isAfterMethodConfiguration()) {
			matchingContexts.add(getThreadContext());
		} else if (method.isAfterClassConfiguration()) {
			synchronized (testResultContext) {
				for (Entry<ITestResult, SeleniumTestsContext> entry: testResultContext.entrySet()) {
					if (entry.getKey().getTestClass() != null && entry.getKey().getTestClass().getName().equals(testResult.getTestClass().getName())) {
						matchingContexts.add(entry.getValue());
					}
				}
			}
			
		} else if (method.isAfterTestConfiguration()) {
			synchronized (testResultContext) {
				for (Entry<ITestResult, SeleniumTestsContext> entry: testResultContext.entrySet()) {
					if (entry.getKey().getTestContext().equals(testResult.getTestContext())) {
						matchingContexts.add(entry.getValue());
					}
				}
			}
			
		} 
    	return matchingContexts;
    }
    
    /**
     * get SR context stored in test result if it exists. Else, create a new one (happens when a test method has been skipped for example)
     * called from reporters only
     * @param testNGCtx
     * @param testName
     * @param testResult
     */
    public static SeleniumTestsContext setThreadContextFromTestResult(ITestContext testNGCtx, ITestResult testResult) {
    	if (testResult == null) {
    		throw new ConfigurationException("Cannot set context from testResult as it is null");
    	}
    	if (TestNGResultUtils.getSeleniumRobotTestContext(testResult) != null) {
    		return TestNGResultUtils.getSeleniumRobotTestContext(testResult);
    	} else {
    		logger.error("Result did not contain thread context, initializing a new one");
        	SeleniumTestsContext seleniumTestsCtx = new SeleniumTestsContext(testNGCtx);
            seleniumTestsCtx.configureContext(testResult);

            TestNGResultUtils.setSeleniumRobotTestContext(testResult, seleniumTestsCtx);
            return seleniumTestsCtx;
    	}
    }
    
    public static void removeThreadContext() {
    	threadLocalContext.remove();
    }
    
    public static Map<ITestResult, SeleniumTestsContext> getTestResultContext() {
		return testResultContext;
	}

	/**
     * Build the root path of STF 
     * method for guessing it is different if we are inside a jar (built mode) or in development
     * @param clazz
     * @param path
     * @return
     */
    public static void getPathFromClass(Class<?> clazz, StringBuilder path) {
		
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
    
    
    private static String readApplicationVersion() {
    	return readApplicationVersion(String.format("%s-version.txt", applicationName));
    }
    private static String readCoreVersion() {
    	return readApplicationVersion("core-version.txt");
    }
    private static String readFullApplicationVersion() {
    	return readApplicationVersion(String.format("%s-version.txt", applicationName), true);
    }
    private static String readFullCoreVersion() {
    	return readApplicationVersion("core-version.txt", true);
    }
    public static String readApplicationVersion(String resourceName) {
    	return readApplicationVersion(resourceName, false);
    }
    
    /**
	 * reads <app>-version.txt file which should be available for all application
	 * It's generated by maven antrun task
	 * From the version found, generate an application version by removing SNAPSHOT (if any) and trailing build version
	 * 1.2.0-SNAPSHOT => 1.2
	 * 
	 * @param	resourceName	name of the resource file to read
	 * @param	fullVersion		if true, returns the version as is
	 * @return
	 */
    public static String readApplicationVersion(String resourceName, boolean fullVersion) {
    	try {
			String version = IOUtils.toString(SeleniumTestsContextManager.class.getClassLoader().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
			if (version.isEmpty()) {
				return "0.0";
			}
			
			if (fullVersion) {
				return version;
			}
			
			String[] versionParts = version.split("\\.", 3);
			if (versionParts.length > 1) {
				return String.format("%s.%s", versionParts[0], versionParts[1]);
			} else {
				return versionParts[0];
			}
		} catch (IOException | NullPointerException e) {
			logger.warn("application version has not been read. It may have not been generated. Execute maven build before launching test");
			return "0.0";
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
		// application name is get from the testNG file path (the subdir name after 'data')
		try {
			applicationNameWithVersion = xmlSuite.getFileName().replace(File.separator, "/").split("/"+ DATA_FOLDER_NAME + "/")[1].split("/")[0];
			Pattern appVersion = Pattern.compile("([a-zA-Z0-9-]+)(_.*)?");
			Matcher appVersionMatcher = appVersion.matcher(applicationNameWithVersion);
			if (appVersionMatcher.matches()) {
				applicationName = appVersionMatcher.group(1);
			} else {
				applicationName = applicationNameWithVersion;
			}
			dataPath = xmlSuite.getFileName().replace(File.separator, "/").split("/"+ DATA_FOLDER_NAME + "/")[0] + "/" + DATA_FOLDER_NAME + "/";
		} catch (IndexOutOfBoundsException | NullPointerException e) {
			applicationName = "core";
			applicationNameWithVersion = "core";
			dataPath = Paths.get(rootPath, DATA_FOLDER_NAME).toString() + "/";
		}
		
		featuresPath = Paths.get(dataPath, applicationNameWithVersion, "features").toString();
		configPath = Paths.get(dataPath, applicationNameWithVersion, "config").toString();
		appDataPath = Paths.get(dataPath, applicationNameWithVersion).toString();
		cachePath = Paths.get(rootPath, CACHE_FOLDER_NAME, applicationNameWithVersion).toString();
		
		if (applicationVersion == null) {
			applicationVersion = readApplicationVersion();
			applicationFullVersion = readFullApplicationVersion();
		}
		if (coreVersion == null) {
			coreVersion = readCoreVersion();
			coreFullVersion = readFullCoreVersion();
		}
		
		// create data folder if it does not exist (it should already exist)
		if (!new File(dataPath).isDirectory()) {
			new File(dataPath).mkdirs();
		}
		if (!new File(appDataPath).isDirectory()) {
			new File(appDataPath).mkdirs();
		}
		if (!new File(cachePath).isDirectory()) {
			new File(cachePath).mkdirs();
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

	public static String getApplicationName() {
		return applicationName;
	}

	public static String getApplicationNameWithVersion() {
		return applicationNameWithVersion;
	}

	public static String getApplicationVersion() {
		return applicationVersion;
	}

	public static String getCoreVersion() {
		return coreVersion;
	}

	public static String getApplicationFullVersion() {
		return applicationFullVersion;
	}

	public static String getCoreFullVersion() {
		return coreFullVersion;
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
	
	/**
	 * Returns location of data folder for this application
	 * @return
	 */
	public static String getApplicationDataPath() {
		return appDataPath;
	}
	
	/**
	 * Returns location of cache folder for this application
	 * @return
	 */
	public static String getCachePath() {
		return cachePath;
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
	
	public static boolean isMobileTest() {
		return getThreadContext().getTestType().isMobile();
	}
    
	public static boolean isNonGuiTest() {
		return getThreadContext().getTestType().family().equals(TestType.NON_GUI);
	}
	
    public static boolean isAppTest() {
    	return getThreadContext().getTestType().family().equals(TestType.APP);
    }
    
    public static boolean isMobileAppTest() {
    	return getThreadContext().getTestType().family().equals(TestType.APP) && getThreadContext().getTestType().isMobile();
    }
    
    public static boolean isMobileWebTest() {
    	return getThreadContext().getTestType().family().equals(TestType.WEB) && getThreadContext().getTestType().isMobile();
    }
    
    public static boolean isDesktopAppTest() {
    	return getThreadContext().getTestType().family().equals(TestType.APP) && !getThreadContext().getTestType().isMobile();
    }
    
    public static boolean isDesktopWebTest() {
    	return getThreadContext().getTestType().family().equals(TestType.WEB) && !getThreadContext().getTestType().isMobile();
    }
    
    public static String getSuiteName() {
    	return getGlobalContext().getTestNGContext().getSuite().getName();
    }
}
