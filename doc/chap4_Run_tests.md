### 1 Configurations ###
Below is the list of all parameters accepted in testing xml file. These parameters may also be passed java properties (-D<paramName>=<value>)

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| testConfig 				|  			| Additional configuration. This should contain common configuration through all TestNG files.<br/>See `exampleConfigGenericParams.xml` file for format | 
| webSessionTimeOut 		| 90000 	| browser session timeout in milliseconds | 
| implicitWaitTimeOut 		| 5			| implicit wait of the browser, in seconds (selenium definition) | 
| explicitWaitTimeOut 		| 15		| explicit wait of the browser, in seconds. Used when checking is an element is present and no wait value is defined (`waitElementPresent` & `isElementPresent`). This value is also used when checking that browser is on the right page (PageObject constructor) | 
| pageLoadTimeout 			| 90		| Value defined in selenium driver. Wait delay for page loading | 
| webDriverGrid 			| 			| Address of seleniumGrid server | 
| runMode 					| LOCAL		| `local`: current computer<br/>`grid`: seleniumGrid<br/>`sauceLabs`: run on sauceLabs device<br/>`testDroid`: run on testdroid device | 
| devMode 					| false		| The development mode allow all existing browsers to remain. In case test is run from any IDE, devMode will be defaulted to true | 
| browser 					| firefox	| Browser used to start test. Valid values are:<br/>`firefox`, `chrome`, `safari`, `iexplore`, `htmlunit`, `opera`, `phantomjs`, `none` for no driver, `browser` for android default browser | 
| browserVersion 			|  			| Browser version to use. By default, it's the last one, or the installed one in local mode. This option has sense when using sauceLabs where browser version can be choosen | 
| firefoxUserProfilePath 	|  			| Firefox user profile if a specific one is defined | 
| useFirefoxDefaultProfile	| true		| Use default firefox profile | 
| operaUserProfilePath 		| 			| Opera user profile if a specific one is defined | 
| firefoxBinaryPath 		| 			| Path to firefox binary if a specific one should be used (for example when using portable versions. Else, the default firefox installation is choosen | 
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
| captureSnapshot 			| true 		| Capture page snapshots |
| snapshotTopCropping		| 0			| number of pixel that will be cropped from the top when capturing snapshot. This only applies to snapshots done with several captures (like from chrome) when a portion of the GUI is fixed when scrolling |
| snapshotBottomCropping	| 0			| same as snapshotTopCropping for bottom cropping |
| softAssertEnabled 		| true		| Test does not stop is an assertion fails. Only valid when using assertions defined in `CustomAssertion` class or assert methods in `BasePage` class | 
| outputDirectory 			| <exec folder>	| folder where HTML report will be written | 
| webDriverListener 		| 			| additional driver listener class |
| testMethodSignature 		|  			| define a specific method signature for hashcodes |
| pluginConfigPath 			|  			| plugins to add |
| testDataFile 				|  			| Datafile to read and inject inside tests | 
| cucumberTests 			|  			| List of tests to execute when using cucumber mode. Test name can be the feature name, the feature file name or the scenario name | 
| cucumberTags 				|  			| List of cucumber tags that will allow determining tests to execute. Format can be:<br/>`@new4 AND @new5` for filtering scenario containing tag new4 AND new5<br/>`@new,@new2` for filtering scenarios containing new OR new2<br/>`@new` for filtering scenario containing new tag | 
| env 						| DEV		| Test environment for the SUT. Allow accessing param values defined in env.ini file  
| cucumberPackage 			| 			| **Mandatory for cucumberTests:** name of the package where cucumber implementation class reside | 
| app 						| 			| Path to the application file (local or remote) | 
| appiumServerURL 			| 			| Appium server url. May be local or remote | 
| deviceName 				| 			| Name of the device to use for mobils tests | 
| appPackage 				| 			| Package name of application (android only) | 
| appActivity 				| 			| Activity started by mobile application (Android) | 
| appWaitActivity 			| 			| In some cases, the first started activity is not the main app activity | 
| newCommandTimeout 		| 120		| Max wait between 2 appium commands in seconds | 
| version 					| 			| Platform version | 
| platform 					| 			| platform on which test should execute. Ex: Windows 7, Android 5.0, iOS 9.1, Linux, OS X 10.10. Defaults to the current platform | 
| cloudApiKey 				| 			| Access key for service | 
| projectName 				| 			| Project name for Testdroid tests only | 


Other parameters, not accepted in XML file but allowed on command line

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| testRetryCount			| 2			| Number of times a failed test is retried. Set to 0 for no retry

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


### 2 Test with Appium locally ###
#### Application test on android ####

    <test name="tnr_appium_mobile_app" parallel="false">
    
    	<!-- cucumber part -->
    	<parameter name="cucumberTests" value="Infolidays" />
    	<parameter name="cucumberTags" value="" />
    	
    	<parameter name="browser" value="*android" />
    	<parameter name="testType" value="appium_app_android" />
    	<parameter name="appiumServerURL" value="http://localhost:4723/wd/hub"/>
    	<parameter name="platform" value="Android 6.0"/>
    	<parameter name="deviceName" value="192.168.56.101:5555"/>
    
    	<parameter name="app" value="<local_path_to_apk>"/>
    	<parameter name="appPackage" value="com.infotel.mobile.infolidays"/>
    	<parameter name="appActivity" value="com.infotel.mobile.mesconges.view.activity.StartActivity"/>
    	<parameter name="newCommandTimeout" value="120"/>
    
    	<packages>
    		<package name="com.seleniumtests.core.runner.*"/>
    	</packages>
    </test>


`deviceName` reflects the local device used to automate the test
`app` is the path of the application file. It can be an URL. If access to URL is restricted, use the pattern "http://<user>:<password>@<host>:<port>/path"


### 3 Test with SauceLabs ###

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

Test must be configured like the example below (or use `-DrunMode=grid`)
 
 	<test name="MRH">
    	<parameter name="runMode" value="grid" />
    	
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

