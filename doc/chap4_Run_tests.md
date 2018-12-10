
### 0 preparation ###

#### Install ####
Unzip seleniumRobot-core.zip file (can be found on maven central or build it with maven) to any folder
Install drivers for robot (seleniumRobot > 3.14.x). Drivers are available as a jar file (seleniumRobot-windows-driver.jar / seleniumRobot-linux-driver.jar / seleniumRobot-mac-driver.jar)
Install openCV for robot (seleniumRobot > 3.14.x). This as been removed from seleniumRobot as it's a huge file (63 MB)
Unzip your test application .zip file to the same folder. It will create the correct folder structure

These operations can be done using maven:
- Install core: `mvn -U org.apache.maven.plugins:maven-dependency-plugin:2.8:unpack -Dartifact=com.infotel.seleniumRobot:core:RELEASE:zip -DoutputDirectory=<path_to_deployed_selenium_robot>  -Dmdep.overWriteReleases=true`
- Install driver according to operating system into /lib/drivers folder (optional if you plan to use your robot with a grid): `mvn -U org.apache.maven.plugins:maven-dependency-plugin:2.8:copy -Dartifact=com.infotel.seleniumRobot:seleniumRobot-windows-driver:RELEASE:jar -DoutputDirectory=<path_to_deployed_selenium_robot>/lib/drivers  -Dmdep.overWriteReleases=true -Dmdep.stripVersion=true`
- Install openCV into /lib folder: `mvn -U org.apache.maven.plugins:maven-dependency-plugin:2.8:copy -Dartifact=org.openpnp:opencv:3.2.0-1:jar -DoutputDirectory=<path_to_deployed_selenium_robot>/lib -Dmdep.overWriteReleases=true`
- Install test application: `mvn -U org.apache.maven.plugins:maven-dependency-plugin:2.8:unpack -Dartifact=<app_groupId>:<app_artifactId>:RELEASE:zip -DoutputDirectory=<path_to_deployed_selenium_robot>  -Dmdep.overWriteReleases=true`

Previous commands will take the last release by default, but you can replace 'RELEASE' by a specific version
 
#### Run ####
Tests are run using command line (`;lib/drivers/*` is mandatory for seleniumRobot > 3.14.x)  : `java -cp seleniumRobot.jar;plugins/<app>-tests.jar;lib/drivers/* -D<option1>=<value1> -D<option2>=<value2> org.testng.TestNG <path_to_TestNG_xml_file>"`</br>

Launch test from folder where seleniumRobot.jar is deployed!

After this command, you can specify TestNG options:
- `-testnames <comma_seperated_list_of_tests>`: list of tests (from XML) to execute
- `-usedefaultlisteners false`: remove all TestNG default reports. SeleniumRobot provide its own reports so TestNG ones are useless

In the above line, there are 2 option types:
- seleniumRobot parameters (table below) which are passed as JVM options `-Dkey=value`
- TestNG parameters ([http://testng.org/doc/documentation-main.html#running-testng]) which must contain at least the xml file to use. For example, if the XML `app-test.xml` file contains several test (testLogin, testCart), you can choose to start only one of them with the line `java -cp seleniumRobot.jar;plugins/<app>-tests.jar -D<option1>=<value1> -D<option2>=<value2> org.testng.TestNG app-test.xml -testnames testLogin`

Specify options from the table bellow
Classpath must define the seleniumRobot.jar file and the test application jar file. Separator is `:` on Linux and `;` on Windows


### 1 Configurations ###
Below is the list of all parameters accepted in testing xml file in `<suite>` or `<test>` tag. Format is: `<parameter name="myParam" value="myValue" />`<br/>
Test parameter will overwrite suite parameters

These parameters may also be passed java properties / JVM options (`-D<paramName>=<value>`).
In this case, this user passed value will overwrite test or suite parameters


| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| testConfig 				|  			| Additional configuration. This should contain common configuration through all TestNG files.<br/>See `exampleConfigGenericParams.xml` file for format | 
| webSessionTimeOut 		| 90	 	| browser session timeout in seconds | 
| implicitWaitTimeOut 		| 5			| implicit wait of the browser, in seconds (selenium definition) | 
| explicitWaitTimeOut 		| 15		| explicit wait of the browser, in seconds. Used when checking is an element is present and no wait value is defined (`waitElementPresent` & `isElementPresent`). This value is also used when checking that browser is on the right page (PageObject constructor) | 
| pageLoadTimeout 			| 90		| Value defined in selenium driver. Wait delay for page loading | 
| replayTimeOut				| 30		| Delay during which an action is replayed
| webDriverGrid 			| 			| Address of seleniumGrid server | 
| runMode 					| LOCAL		| `local`: current computer<br/>`grid`: seleniumGrid<br/>`sauceLabs`: run on sauceLabs device<br/>`testDroid`: run on testdroid device | 
| nodeTags					| null		| Commat seperated list of strings. Requests that this test should execute only on a node (grid mode only) announcing all of these tags. On grid, this is declared with option `-nodeTags <tag1>,<tag2>`. If no slot matches the requested tags, session is not created |
| devMode 					| false		| The development mode. If true, DEBUG logs are displayed, else, minimal log level is INFO. Driver logs are also displayed | 
| manualTestSteps			| false		| If true, it's possible to add test steps in Test and Page Object (`addTest("my step name")`). An error will be raised if manual steps are added when automatic steps are enabled |
| browser 					| firefox	| Browser used to start test. Valid values are:<br/>`firefox`, `chrome`, `safari`, `iexplore`, `htmlunit`, `opera`, `phantomjs`, `none` for no driver, `browser` for android default browser | 
| browserVersion 			|  			| Browser version to use. By default, it's the last one, or the installed one in local mode. This option has sense when using sauceLabs where browser version can be choosen | 
| headless					| false		| If true, start browser in headless mode. This is supported by chrome >= 60 and firefox >= 56  |
| firefoxUserProfilePath 	|  			| Firefox user profile if a specific one is defined | 
| useFirefoxDefaultProfile	| true		| Use default firefox profile | 
| operaUserProfilePath 		| 			| Opera user profile if a specific one is defined | 
| firefoxBinaryPath 		| 			| Path to firefox binary if a specific one should be used (for example when using portable versions. Else, the default firefox installation is choosen | 
| geckoDriverPath 			| 			| Path to a different installation of geckodriver executable | 
| chromeDriverPath 			| 			| Path to a different installation of chromedriver executable | 
| chromeBinaryPath 			| 			| Path to chrome binary if using a portable installation (not detected by system | 
| ieDriverPath 				| 			| Path to a different ieDriverServer executable | 
| edgeDriverPath			| 			| Path to a different edge driver executable |
| userAgent 				| 			| Allow defining a specific user-agent in chrome and firefox only | 
| enableJavascript 			| true		| Javascript activation |
| browserDownloadDir 		| 			| Path where files are downloaded. Firefox only | 
| proxyType 				| AUTO		| Proxy type. Valid values are `AUTODETECT`, `MANUAL`, `DIRECT`, `PAC`, `SYSTEM` | 
| proxyAddress 				| 			| Proxy address, if MANUAL type is choosen | 
| proxyPort 				| 			| Proxy port, if MANUAL type is choosen | 
| proxyLogin 				| 			| Proxy login, if MANUAL type is choosen |  
| proxyPassword 			| 			| Proxy password, if MANUAL type is choosen | 
| proxyExclude 				|			| Proxy address exclusion, if MANUAL type is choosen | 
| proxyPac 					| 			| Automatic configuration address, if PAC type is choosen | 
| reportGenerationConfig 	| summaryPerSuite | Type of report generation.
| captureSnapshot 			| true 		| Capture page snapshots. Captures are done only when a new page is opened |
| captureNetwork			| false		| If true, creates a HAR file which capture traffic. This is only available with MANUAL and DIRECT proxy settings because there is no way, when automatic mode is used, to know which proxy is used by browser and the authentication used. |
| snapshotTopCropping		| 0			| number of pixel that will be cropped from the top when capturing snapshot. This only applies to snapshots done with several captures (like from chrome) when a portion of the GUI is fixed when scrolling |
| snapshotBottomCropping	| 0			| same as snapshotTopCropping for bottom cropping |
| seleniumRobotServerActive	| false		| whether we use seleniumRobot server. If true, seleniumRobotServerUrl MUST be specified (in XML, command line or through env variable |
| seleniumRobotServerUrl	| 			| URL of the seleniumRobot server. Can be specified as an environment variable |
| seleniumRobotServerCompareSnapshots	| false		| whether we should use the snapshots created by robot to compare them to a previous execution. This option only operates when SeleniumRobot server is connected. See chap6 documentation for details on connecting to server |
| seleniumRobotServerRecordResults		| false		| whether we should record test results to seleniumrobot server. This option only operates when SeleniumRobot server is connected. See chap6 documentation for details on connecting to server |
| seleniumRobotServerVariablesOlderThan | 0			| whether we should get from server variables which were created at least X days ago |
| softAssertEnabled 		| true		| Test does not stop is an assertion fails. Only valid when using assertions defined in `CustomAssertion` class or assert methods in `BasePage` class | 
| outputDirectory 			| <exec folder>	| folder where HTML report will be written. By default, it's 'test-output' subfolder. If you want to write test in an other directory, use `test-output/myResult` to write them relative to SeleniumRobot root. An absolute path may also be specified. This will allow to execute several tests in parallel without overwritting existing results | 
| loadIni					|			| comma separated list of path to ini formatted files to load. Their values will overwrite those from env.ini file if the same key is present. Path is relative to data/<app>/config path |
| webDriverListener 		| 			| additional driver listener class |
| testMethodSignature 		|  			| define a specific method signature for hashcodes |
| pluginConfigPath 			|  			| plugins to add |
| cucumberTests 			|  			| Comma seperated list of tests to execute when using cucumber mode. Test name can be the feature name, the feature file name or the scenario name. You can also give regex that will match String in java. e.g: scenario .* | 
| cucumberTags 				|  			| List of cucumber tags that will allow determining tests to execute. Format can be:<br/>`@new4 AND @new5` for filtering scenario containing tag new4 AND new5<br/>`@new,@new2` for filtering scenarios containing new OR new2<br/>`@new` for filtering scenario containing new tag | 
| env 						| DEV		| Test environment for the SUT. Allow accessing param values defined in env.ini file  
| cucumberPackage 			| 			| **Mandatory for cucumberTests:** name of the package where cucumber implementation class reside | 
| app 						| 			| Path to the application file (local or remote) | 
| appiumServerURL 			| 			| Appium server url. May be local or remote | 
| deviceName 				| 			| Name of the device to use for mobile tests. It's the Human readable name (e.g: Nexus 6 as given by `adb -s <id_device> shell getprop`, line [ro.product.model] property on Android or `instruments -s devices`), not it's id. SeleniumRobot will replace this name with id when communicating with Appium | 
| fullReset 				| true		| enable full reset capability for appium tests | 
| maskPassword 				| true		| Whether seleniumRobot should detect passwords in method calls and mask them in reports | 
| appPackage 				| 			| Package name of application (android only) | 
| appActivity 				| 			| Activity started by mobile application (Android) | 
| appWaitActivity 			| 			| In some cases, the first started activity is not the main app activity | 
| newCommandTimeout 		| 120		| Max wait between 2 appium commands in seconds | 
| version 					| 			| Platform version | 
| platform 					| 			| platform on which test should execute. Ex: Windows 7, Android 5.0, iOS 9.1, Linux, OS X 10.10. Defaults to the current platform | 
| cloudApiKey 				| 			| Access key for service | 
| projectName 				| 			| Project name for Testdroid tests only | 
| viewPortWidth				|			| Width of the viewPort when doing web tests. No effect for mobile apps. If not set, window will be maximized |
| viewPortHeight			|			| Height of the viewPort when doing web tests. No effect for mobile apps. If not set, window will be maximized |
| overrideSeleniumNativeAction      | false | intercept driver.findElement and driver.frame operations so that seleniumRobot element operations can be use (replay, error handling, ...) even when using standard selenium code. Only findElement(By) and findElements(By) are supported, not findElementByxxx(String). Logging is also better |
| customTestReports			| PERF::xml::reporter/templates/report.perf.vm | With this option, you can specify which files will be generated for each test. By default, it's the JMeter report. Format is a comma seperated list of <prefix>::<extension>::<template_file located in resources>. resources can be those from test application. Template is in the Velocity format |
| customSummaryReports		| results::json::reporter/templates/report.summary.json.vm | With this option, you can specify which files will be generated for sumarizing test session. By default, it's a json report. Format is a comma seperated list of <prefix>::<extension>::<template_file located in resources>. resources can be those from test application. Template is in the Velocity format |
| archiveToFile				| null		| If not specified, no archiving will be done. Else, provide a zip file path and the whole content of `outputDirectory` will be zipped to this file in case archive is enabled |
| archive					| false		| If `true`, always archive results to `archiveToFile` file. Other possible values are: `onSuccess` (archive when test is OK) and `onError` (archive when test is KO) |
| captureVideo				| onError	| If `true`, always capture video. Other possible values are: `onSuccess` (keep video when test is OK), `false` and `onError` (capture video when test is KO) |
| tmsRun					| null		| Configuration string (JSON format) for identifying test that is run in test management system. E.g: {'type': 'hp', 'run': '3'} |
| tmsConnect				| null		| Configuration string (JSON format) for test management system if you plan to use it. E.g: {'hpAlmServerUrl': 'http://myamlserver:8080', 'hpAlmProject': '12', 'hpAlmDomain': 'mydomain', 'hpAlmUser': 'user', 'hpAlmPassword': 'pass'}  |
| optimizeReports			| false		| If true, compress HTML, get HTML resources from internet so that logs are smaller |
| neoloadUserPath 			| null		| a name to give to your Neoload recordings. See chap7 for details on how to use Neoload with SeleniumRobot |


Other parameters, not accepted in XML file but allowed on command line

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| testRetryCount			| 2			| Number of times a failed test is retried. Set to 0 for no retry

#### Minimal Configuration ####

See §3.3 for the minimal TestNG XML file

#### Centralized XML configuration ####

A test application may have several TestNG XML files but some of the parameters may be common.
To avoid maintaining several files with the same values, SeleniumRobot allows to centralize these parameters into one specific file.
This file is referenced in XML configuration with:

`<parameter name="testConfig" value="<filePath>.xml" />`

Below is an example of this file extending TestNG configuration

	<parameters>
	
		<!-- common parameters -->
		<parameter name="app" value="myMobileApp.apk" />
		
		<!-- mobile device definition -->
		<device name="Samsung Galaxy Nexus SPH-L700 4.3" platform="Android 4.3" />
		
		<!-- service definitions for run mode -->
		<service name="grid">
			<parameter name="webDriverGrid" value="http://localhost:4444/wd/hub" />
		</service>
		
		<service name="local">
			<parameter name="appiumServerURL" value="http://localhost:4723/wd/hub" />
		</service>
		
		<service name="saucelabs">
			<parameter name="appiumServerURL" value="http://xxx:aaaaa-26d7-44fa-bbbb-b2c75cdccafd@ondemand.saucelabs.com:80/wd/hub" />
		</service>
		
		<service name="testdroid">
			<parameter name="appiumServerURL" value="http://appium.testdroid.com/wd/hub" />
		    <parameter name="cloudApiKey" value="aaaaaaaaaa93Ua0uQNPxBktPSfZv"/>
			<parameter name="projectName" value="Test_testdroid" />
		</service>
	<parameters>
	
You can define:
 
- *parameters* like in main XML configuration.
- *device definition*: Allows to only specify the device name in parameter. Platform will then be get from device definition
_e.g_: in testNG.xml, you have <parameter name="deviceName" value="Samsung Galaxy Nexus SPH-L700 4.3" /><br/>
SeleniumRobot will translate that in:
`<parameter name="deviceName" value="Samsung Galaxy Nexus SPH-L700 4.3" />`
and
`<parameter name="platform" value="Android 4.3" />`

- *service definition* used for runMode option<br/>
You can define one service for each runMode, with the same name: local, grid, saucelabs, testdroid<br/>
Under each service, you can then add any parameter needed to address this runMode as in the above example.
_e.g_: if user select "testdroid" runMode, then the 3 parameters (appiumServerURL, cloudApiKey, projectName) will be added to configuration

### 2 Test WebApp with desktop browser ###

The minimal parameters to pass to SeleniumRobot are:
`browser`: MUST be defined because default browser is None

### 3 Test with Appium locally ###

For mobile tests, set the following environment variables on your local computer:
- APPIUM_HOME: path to Appium installation path (e.g: where Appium.exe/node.exe resides on Windows)
- ANDROID_HOME: path to Android SDK (e.g: where SDK Manager resides)

Also check that there is only one version of ADB on computer. Otherwise, there may be conflicts and ADB client you provide may not get relevant information from devices

When using seleniumRobot-grid, these environment variables will be set on grid node
For cloud test, these variables are not needed

#### Application test on android ####

Define test as follows (minimal needed options)

    <test name="tnr_appium_mobile_app" parallel="false">
    
    	<!-- cucumber part -->
    	<parameter name="browser" value="*android" />
    	<parameter name="platform" value="Android 6.0"/>
    	<parameter name="deviceName" value="Nexus 6"/>
    
    	<parameter name="app" value="<local_path_to_apk>"/>
    	<parameter name="appPackage" value="com.infotel.mobile.infolidays"/>
    	<parameter name="appActivity" value="com.infotel.mobile.mesconges.view.activity.StartActivity"/>
    
    	<packages>
    		<package name="com.seleniumtests.core.runner.*"/>
    	</packages>
    </test>


`deviceName` reflects the local device used to automate the test
`app` is the path of the application file. It can be an URL. If access to URL is restricted, use the pattern "http://<user>:<password>@<host>:<port>/path"
`appPackage` and `appActivity` can be found in APK manifest file

#### Application test on iOS ####

Define test as follows (minimal needed options)

    <test name="tnr_appium_mobile_app" parallel="false">
    
    	<parameter name="browser" value="*safari" />
    	<parameter name="platform" value="iOS 10.3"/>
    	<parameter name="deviceName" value="iPhone SE"/>
    	<parameter name="app" value="<local_path_to_ipa_or_app.zip file>"/>
    
    	<packages>
    		<package name="com.seleniumtests.core.runner.*"/>
    	</packages>
    </test>


### 3 Test with SauceLabs ###

Define test as follows

	<test name="tnr_sauce_mobile_app" parallel="false">
    	<parameter name="cucumberTests" value="Configuration" />
	    <parameter name="cucumberTags" value="" />
	    <parameter name="runMode" value="saucelabs" />
	    
        <parameter name="appiumServerURL" value="http://<user>:<key>@ondemand.saucelabs.com:80/wd/hub"/>
        <parameter name="deviceName" value="Android Emulator"/>
        <parameter name="platform" value="Android 5.1"/>
        
        <parameter name="app" value="<local_path_to_apk>"/>
    	<parameter name="appPackage" value="com.infotel.mobile.infolidays"/>
    	<parameter name="appActivity" value="com.infotel.mobile.mesconges.view.activity.StartActivity"/>

        <packages>
            <package name="com.seleniumtests.core.runner.*"/>
        </packages>
    </test>

### 4 Test with Testdroid ###

Define test as follows

	<test name="tnr_testdroid_mobile_app" parallel="false">
    	<parameter name="cucumberTests" value="Configuration" />
	    <parameter name="cucumberTags" value="" />
	    <parameter name="runMode" value="testdroid" />
	    
        <parameter name="appiumServerURL" value="http://appium.testdroid.com/wd/hub"/>
        <parameter name="cloudApiKey" value="<key>"/>
        <parameter name="deviceName" value="Samsung Galaxy Nexus SPH-L700 4.3"/>
        <parameter name="platform" value="Android 4.3"/>
        
        <parameter name="app" value="<local_path_to_apk>"/>
    	<parameter name="appPackage" value="com.infotel.mobile.infolidays"/>
    	<parameter name="appActivity" value="com.infotel.mobile.mesconges.view.activity.StartActivity"/>
        
        <parameter name="projectName" value="Test_testdroid"/>
        
        <packages>
            <package name="com.seleniumtests.core.runner.*"/>
        </packages>
    </test>
    
### 5 Test with SeleniumGrid ###

SeleniumGrid allows to address multiple selenium nodes from one central point
![](/images/seleniumGrid.png) 
In this mode, SeleniumRobot addresses the Hub and then, the hub dispatches browser creation on available nodes, for mobile or desktop tests.

For better features, prefer using seleniumRobot-grid which is based on standard grid

#### Configure SeleniumRobot ####

Test must be configured like the example below (or use `-DrunMode=grid -DwebDriverGrid=http://127.0.0.1:4444/wd/hub`)
 
 	<test name="MRH">
    	<parameter name="runMode" value="grid" />
    	<parameter name="webDriverGrid" value="http://127.0.0.1:4444/wd/hub" />
    	
        <packages> 
            <package name="com.seleniumtests.core.runner.*"/>
        </packages>
    </test>

#### Configure Grid hub ####
Hub configuration from command line or JSON is provided here: 
[https://github.com/SeleniumHQ/selenium/wiki/Grid2](https://github.com/SeleniumHQ/selenium/wiki/Grid2)

Hub configuration should use a browserTimeout of 60 seconds

#### Configure Grid node ####
Node configuration from command line or JSON is provided here: 
[https://github.com/SeleniumHQ/selenium/wiki/Grid2](https://github.com/SeleniumHQ/selenium/wiki/Grid2)

You should use JSON configuration for nodes, to make it simpler to start

##### Desktop node #####

To add a browser / browser version, add following code (change info if necessary)

	{
      "browserName": "firefox",
      "maxInstances": 5,
      "seleniumProtocol": "WebDriver",
	  "version": "last",
	  "firefox_binary": "C:/Program Files (x86)/Mozilla Firefox/firefox.exe"
    },

`version` may be any text string as soon as SeleniumRobot asks for it with the `browserVersion` parameter
`firefox_binary` is only necessary if you need to specify a version different from the default one

To configure IEDriverServer and ChromeDriver executables, add the following to JSON `configuration` dictionnary

	"Dwebdriver.chrome.driver=<path_to_driver>/chromedriver.exe": "",
    "Dwebdriver.ie.driver=<path_to_driver>/IEDriverServer.exe": ""

or as command line switches

Node configuration should use a timeout of 45 seconds

##### Appium nodes #####

To use mobile tests and SeleniumGrid, we use appium directly.<br/>
Create a node.json configuration file for this node. (In the example below, We have 1 mobile device in Android 6.0 version, supporting either chrome or default android browser)

	{
  	"capabilities":
      [
        {
			"browserName": "browser",
			"deviceName": "192.168.228.101:5555",
			 "version":"6.0",
			 "maxInstances": 1,
			 "platform":"android"
        },
		{
			"browserName": "chrome",
			"deviceName": "192.168.228.101:5555",
			 "version":"6.0",
			 "maxInstances": 1,
			 "platform":"android"
        }
      ],
  "configuration":
  {
    "proxy": "org.openqa.grid.selenium.proxy.DefaultRemoteProxy",
    "maxSession": 1,
    "register": true,
    "registerCycle": 5000,
    "hubPort": 4444,
    "hubHost": "172.22.2.2"
  }
}

From command line, use
`appium --nodeconfig /path/to/nodeconfig.json`

From GUI, node config file can be specified in appium options

### 6 Running tests in parallel ###
When testing among several browsers or devices is requested, it's easier to run these tests in parallel

#### Test NG way ####
Test NG is able to run tests in parallel using the XML configuration
To do so, configure several tests, one for each browser / mobile device
	
	<test name="tnr_chrome">
	     <parameter name="browser" value="*chrome" />
	     <classes>
	         <class name="com.infotel.seleniumTests.googleTest.tests.NextTest"/>
	     </classes>
	</test>
    
	<test name="tnr_firefox">
	     <parameter name="browser" value="*firefox" />
	     <classes>
	         <class name="com.infotel.seleniumTests.googleTest.tests.NextTest"/>
	     </classes>
	</test>
    
Then, in suite definition, in the XML, set parallel mode

	<suite name="My suite" parallel="tests" thread-count="5">
    

#### Jenkins way ####

##### Matrix Job #####

Create a Matrix job with an axis called "browser"
Then, add all browser you want to use to this axis
Finally, configure a shell / batch script to use the created variable "%BROWSER%"

##### Selenium Capability Axis #####

Jenkins offers Selenium Capability Axis to create a matrix configured through Selenium Grid

