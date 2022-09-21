<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

  - [0 preparation](#0-preparation)
    - [Install](#install)
    - [Run](#run)
  - [1 Configurations](#1-configurations)
    - [Common params](#common-params)
    - [Proxy settings](#proxy-settings)
    - [Test execution settings](#test-execution-settings)
    - [Browser specific settings](#browser-specific-settings)
    - [Selenium server params](#selenium-server-params)
    - [Mobile params](#mobile-params)
    - [Reporting](#reporting)
      - [Test managers](#test-managers)
      - [Bugtrackers](#bugtrackers)
        - [Jira](#jira)
      - [Performance reporting](#performance-reporting)
      - [Snapshots](#snapshots)
    - [Deprecated](#deprecated)
    - [Minimal Configuration](#minimal-configuration)
    - [Centralized XML configuration](#centralized-xml-configuration)
  - [2 Test WebApp with desktop browser](#2-test-webapp-with-desktop-browser)
    - [Test locally](#test-locally)
    - [Test with SeleniumRobot Grid](#test-with-seleniumrobot-grid)
    - [Test with BrowserStack](#test-with-browserstack)
  - [3 Test with Appium locally](#3-test-with-appium-locally)
    - [Application test on android](#application-test-on-android)
    - [Application test on iOS](#application-test-on-ios)
  - [4 Test with SauceLabs](#4-test-with-saucelabs)
  - [5 Test with BrowserStack](#5-test-with-browserstack)
  - [6 Test with Perfecto](#6-test-with-perfecto)
  - [7 Test with SeleniumGrid](#7-test-with-seleniumgrid)
    - [Configure SeleniumRobot](#configure-seleniumrobot)
    - [Configure Grid hub](#configure-grid-hub)
    - [Configure Grid node](#configure-grid-node)
      - [Desktop node](#desktop-node)
      - [Appium nodes](#appium-nodes)
  - [6 Running tests in parallel](#6-running-tests-in-parallel)
    - [Test NG way](#test-ng-way)
    - [Jenkins way](#jenkins-way)
      - [Matrix Job](#matrix-job)
      - [Selenium Capability Axis](#selenium-capability-axis)
  - [8 The test results](#8-the-test-results)
    - [HTML result](#html-result)
    - [JUnit XML global result](#junit-xml-global-result)
    - [JUnit XML per test result](#junit-xml-per-test-result)
    - [JSON global result](#json-global-result)
    - [Custom result](#custom-result)
      - [Custom Test report](#custom-test-report)
      - [Custom Summary report](#custom-summary-report)
    - [Default TestNG reports](#default-testng-reports)
  - [9 Execute Selenium IDE Tests](#9-execute-selenium-ide-tests)
- [Troubleshooting](#troubleshooting)
  - [Cyclic dependency when executing TestNG Test](#cyclic-dependency-when-executing-testng-test)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


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

#### Common params ####

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| webDriverGrid 			| 			| Address of seleniumGrid server. It's possible to specify several URL `http://grid1.company.com,http://grid2.company.com`. This way, seleniumRobot will choose one of the available grid | 
| runMode 					| LOCAL		| `local`: current computer<br/>`grid`: seleniumGrid<br/>`sauceLabs`: run on sauceLabs device<br/> | 
| nodeTags					| null		| Commat seperated list of strings. Requests that this test should execute only on a node (grid mode only) announcing all of these tags. On grid, this is declared with option `-nodeTags <tag1>,<tag2>`. If no slot matches the requested tags, session is not created |
| browser 					| firefox	| Browser used to start test. Valid values are:<br/>`firefox`, `chrome`, `safari`, `iexplore`, `edge`, `iexploreEdge` for Edge in Internet Explorer mode, `htmlunit`, `opera`, `phantomjs`, `none` for no driver, `browser` for android default browser | 
| env 						| DEV		| Test environment for the SUT. Allow accessing param values defined in env.ini file  
| captureVideo				| onError	| If `true`, always capture video. Other possible values are: `onSuccess` (keep video when test is OK), `false` and `onError` (capture video when test is KO) |
| captureNetwork			| false		| If true, creates a HAR file which capture traffic. This is only available with MANUAL and DIRECT proxy settings because there is no way, when automatic mode is used, to know which proxy is used by browser and the authentication used. |
| testRetryCount			| 2			| Number of times a failed test is retried. Set to 0 for no retry. **This parameter is not accepted in XML file, only on command line**. This number can be increased dynamically inside test with `increaseMaxRetry()`|
| seleniumRobotServerActive	| false		| whether we use seleniumRobot server. If true, seleniumRobotServerUrl MUST be specified (in XML, command line or through env variable |
| seleniumRobotServerUrl	| 			| URL of the seleniumRobot server. Can be specified as an environment variable |
| seleniumRobotServerToken	|			| Token to use when connecting to seleniumRobot server API (by default with server >= 2.0) |
| outputDirectory 			| <exec folder>	| folder where HTML report will be written. By default, it's 'test-output' subfolder. If you want to write test in an other directory, use `test-output/myResult` to write them relative to SeleniumRobot root. An absolute path may also be specified. This will allow to execute several tests in parallel without overwritting existing results. If you want to set the current date and time in output directory folder name, see §7.18 | 

**In case eof Linux Chromium**: set chromeBinaryPath to '/snap/chromium/current/usr/lib/chromium-brower/chrome'

#### Proxy settings ####

The proxy settings below apply to the browser used to test your application

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| proxyType 				| AUTO		| Proxy type. Valid values are `AUTODETECT`, `MANUAL`, `DIRECT`, `PAC`, `SYSTEM` | 
| proxyAddress 				| 			| Proxy address, if MANUAL type is choosen | 
| proxyPort 				| 			| Proxy port, if MANUAL type is choosen | 
| proxyLogin 				| 			| Proxy login, if MANUAL type is choosen |  
| proxyPassword 			| 			| Proxy password, if MANUAL type is choosen | 
| proxyExclude 				|			| Proxy address exclusion, if MANUAL type is choosen | 
| proxyPac 					| 			| Automatic configuration address, if PAC type is choosen | 

#### Test execution settings ####

Settings for changing the test behavior 

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| debug 					| none		| The debug mode. 'gui', 'driver', 'core'. It's also possible to specify several debug modes at once: 'core,driver'. Details can be found in chapter §7.20 | 
| softAssertEnabled 		| true		| Test does not stop is an assertion fails. Only valid when using assertions defined in `CustomAssertion` class or assert methods in `BasePage` class | 
| loadIni					|			| comma separated list of path to ini formatted files to load. Their values will overwrite those from env.ini file if the same key is present. Path is relative to data/<app>/config path |
| overrideSeleniumNativeAction      | false | intercept driver.findElement and driver.frame operations so that seleniumRobot element operations can be use (replay, error handling, ...) even when using standard selenium code. Only findElement(By) and findElements(By) are supported, not findElementByxxx(String). Logging is also better |
| webDriverListener 		| 			| additional driver listener class. See chapter §7.21 for implementation details |
| actionDelay               | 200       | delay in millisecondes between 2 actions. It allows to speed up or slow down a test |


Cucumber options

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| cucumberTests 			|  			| Comma seperated list of tests to execute when using cucumber mode. Test name can be the feature name, the feature file name (without extension) or the scenario name. You can also give regex that will match String in java. e.g: scenario .* | 
| cucumberTags 				|  			| List of cucumber tags that will allow determining tests to execute. Format can be:<br/>`@new4 AND @new5` for filtering scenario containing tag new4 AND new5<br/>`@new,@new2` for filtering scenarios containing new OR new2<br/>`@new` for filtering scenario containing new tag | 
| cucumberPackage 			| 			| **Mandatory for cucumberTests:** name of the package where cucumber implementation class reside | 


#### Browser specific settings ####

Settings for customizing the default seleniumRobot driver features. By default, selenium looks at the installed drivers and browser and choose the best one

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| browserVersion 			|  			| Browser version to use. By default, it's the last one, or the installed one in local mode. This option has sense when using sauceLabs where browser version can be choosen | 
| betaBrowser				| false		| If true, we search for installed beta version of browsers (chrome, edge)  |
| headless					| false		| If true, start browser in headless mode. This is supported by chrome >= 60 and firefox >= 56  |
| firefoxUserProfilePath 	|  			| Firefox user profile if a specific one is defined. This path MUST exist. It can also take 'default' value. In this case, the current user profile is used. If not set, an empty temp profile is created | 
| chromeUserProfilePath 	|  			| Chrome user profile if a specific one is defined. This path MUST exist. It can also take 'default' value. In this case, the current user profile is used. If not set, an empty temp profile is created | 
| edgeUserProfilePath       |           | Edge user profile if a specific one is defined. This path MUST exist. It can also take 'default' value. In this case, the current user profile is used. If not set, an empty temp profile is created | 
| useFirefoxDefaultProfile	| true		| Use default firefox profile | 
| operaUserProfilePath 		| 			| Opera user profile if a specific one is defined | 
| firefoxBinaryPath 		| 			| Path to firefox binary if a specific one should be used (for example when using portable versions. Else, the default firefox installation is choosen | 
| geckoDriverPath 			| 			| Path to a different installation of geckodriver executable | 
| chromeDriverPath 			| 			| Path to a different installation of chromedriver executable | 
| chromeBinaryPath 			| 			| Path to chrome binary if using a portable installation (not detected by system). In case of Linux box, with chrome installed with snap package, set the path to '/snap/chromium/current/usr/lib/chromium-brower/chrome' | 
| ieDriverPath 				| 			| Path to a different ieDriverServer executable | 
| edgeDriverPath			| 			| Path to a different edge driver executable |
| userAgent 				| 			| Allow defining a specific user-agent in chrome and firefox only | 
| enableJavascript 			| true		| Javascript activation |
| browserDownloadDir 		| 			| Path where files are downloaded. Firefox only |
| viewPortWidth				|			| Width of the viewPort when doing web tests. No effect for mobile apps. If not set, window will be maximized |
| viewPortHeight			|			| Height of the viewPort when doing web tests. No effect for mobile apps. If not set, window will be maximized | 
| extensionX.path			|			| Path (absolute file path or HTTP URL) to the extension (.xpi or .crx). Replace X with extension number. E.g: `extension0.path=http://localhost:8000/myExt.crx`. See §7.17 |
| extensionX.options		|			| options to pass to extension (only for firefox). Options must have the format: <key1>=<value1>;<key2>=<value2> |
| chromeOptions				|			| allow to set any options to chrome executable. For example: to put chrome in "Dark mode", you will write: `-DchromeOptions="--force-dark-mode --enable-features=WebUIDarkMode"`. Other chrome options can be found here: https://peter.sh/experiments/chromium-command-line-switches/ |
| edgeOptions				|			| see 'chromeOptions' |




Settings for customizing timeouts

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| webSessionTimeOut 		| 90	 	| browser session timeout in seconds | 
| implicitWaitTimeOut 		| 5			| implicit wait of the browser, in seconds (selenium definition) | 
| explicitWaitTimeOut 		| 15		| explicit wait of the browser, in seconds. Used when checking if an element is present and no wait value is defined (`waitElementPresent` & `isElementPresent`). This value is also used when checking that browser is on the right page (PageObject constructor) | 
| pageLoadTimeout 			| 90		| Value defined in selenium driver. Wait delay for page loading | 
| replayTimeOut				| 30		| Delay during which an action is replayed
| pageLoadStrategy			| normal	| set page load strategy as defined in [https://www.w3.org/TR/webdriver/#dfn-table-of-page-load-strategies](https://www.w3.org/TR/webdriver/#dfn-table-of-page-load-strategies). Values are 'normal', 'eager', 'none'|

#### Selenium server params ####

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| seleniumRobotServerActive	| false		| whether we use seleniumRobot server. If true, seleniumRobotServerUrl MUST be specified (in XML, command line or through env variable |
| seleniumRobotServerUrl	| 			| URL of the seleniumRobot server. Can be specified as an environment variable |
| seleniumRobotServerCompareSnapshots	| false		| whether we should use the snapshots created by robot to compare them to a previous execution. This option only operates when SeleniumRobot server is connected. See chap6 documentation for details on connecting to server. For details, see [https://github.com/bhecquet/seleniumRobot/blob/master/doc/chap7_Howto.md#23-compare-snapshots](https://github.com/bhecquet/seleniumRobot/blob/master/doc/chap7_Howto.md#23-compare-snapshots) |
| seleniumRobotServerRecordResults		| false		| whether we should record test results to seleniumrobot server. This option only operates when SeleniumRobot server is connected. See chap6 documentation for details on connecting to server |
| snapshotComparisonResult	| displayOnly | What to do when comparing snapshots with option 'seleniumRobotServerCompareSnapshots' activated. 'displayOnly' will display the comparison result in HTML result but not change test result. 'changeTestResult' will set test result to KO if snapshot comparison failed. 'addTestResult' will add a new test result for each snapshot comparison. Functional test result will not be impacted. This last option helps separate functional tests and UX tests. |
| seleniumRobotServerVariablesOlderThan | 0			| whether we should get from server variables which were created at least X days ago |
| seleniumRobotServerVariablesReservation | -1      | Duration of reservation of variable in minutes. If not set, variable server reserves variable for 15 mins
| seleniumRobotServerToken 	|			| Token for connecting to seleniumRobot server through API (for getting variables, sending snapshots ...). This is only necessary if API security has been activated |
| seleniumRobotServerSnapshotsTtl |		| 30		| Number of days, snapshot comparison will be kept before being deleted. Only valid when `seleniumRobotServerCompareSnapshots` is set to `true` |

#### Mobile params ####

Params for mobile testing

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| app 						| 			| Path to the application file (local or remote) | 
| deviceName 				| 			| Name of the device to use for mobile tests. It's the Human readable name (e.g: Nexus 6 as given by `adb -s <id_device> shell getprop`, line [ro.product.model] property on Android or `instruments -s devices`), not it's id. SeleniumRobot will replace this name with id when communicating with Appium | 
| fullReset 				| true		| enable full reset capability for appium tests | 
| appPackage 				| 			| Package name of application (android only) | 
| appActivity 				| 			| Activity started by mobile application (Android) | 
| appWaitActivity 			| 			| In some cases, the first started activity is not the main app activity | 
| newCommandTimeout 		| 120		| Max wait (in seconds) between 2 appium commands in seconds. Increase this time when debugging | 
| version 					| 			| Platform version | 
| platform 					| 			| platform on which test should execute. Ex: Windows 7, Android 5.0, iOS 9.1, Linux, OS X 10.10. Defaults to the current platform |
| automationName			| Appium / XCUITest | Default is "Appium" for Android and "XCUITest" for iOS. The automationName to use. See http://appium.io/docs/en/writing-running-appium/caps/index.html 
| cloudApiKey 				| 			| Access key for service |  
| testConfig 				|  			| Additional configuration. This should contain common configuration through all TestNG files.<br/>See `exampleConfigGenericParams.xml` file for format | 

#### Reporting ####

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| customTestReports			| PERF::xml::reporter/templates/report.perf.vm | With this option, you can specify which files will be generated for each test. By default, it's the JMeter report. Format is a comma seperated list of <prefix>::<extension>::<template_file located in resources>. resources can be those from test application. Template is in the Velocity format. See chap7_Howto.md, §11 |
| customSummaryReports		| results::json::reporter/templates/report.summary.json.vm | With this option, you can specify which files will be generated for sumarizing test session. By default, it's a json report. Format is a comma seperated list of <prefix>::<extension>::<template_file located in resources>. resources can be those from test application. Template is in the Velocity format. See chap7_Howto.md, §11 |
| archiveToFile				| null		| If not specified, no archiving will be done. Else, provide a zip file path and the whole content of `outputDirectory` will be zipped to this file in case archive is enabled |
| archive					| never		| If `always` / `true`, always archive results to `archiveToFile` file. Other possible values are: `onSuccess` (archive when all tests are OK) and `onError` (archive when at least 1 test is KO) and `onSkip` (archive when at least 1 test is skipped). Multiple values can be specified if separated by comma. |
| keepAllResults			| false		| By default, when a test fails and is retried, the failed test data are overwritten. If true, will keep all result even if test is retried, allowing to analyze them |
| optimizeReports			| false		| If true, compress HTML, get HTML resources from internet so that logs are smaller |
| manualTestSteps			| false		| If true, it's possible to add test steps in Test and Page Object (`addTest("my step name")`). An error will be raised if manual steps are added when automatic steps are enabled |
| maskPassword 				| true		| Whether seleniumRobot should detect passwords in method calls and mask them in reports | 
| reporterPluginClasses     | null		| comma-seperated list of classes to call when a custom reporter needs to be added. See chap7_Howto.md, §19 |
| startedBy 				| null		| allow to tell who called seleniumRobot. May be any string but can be used to display the result URL when using bugtracker. In case this is set, BugTracker won't receive the detailed result as a zip file, to avoid sending too big files |

##### Test managers #####

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| tmsUrl					| null		| URL of the test manager  (e.g: Squash TM http://<squash_host>:<squash_port>) |
| tmsUser					| null		| User which will access Test manager  |
| tmsPassword				| null		| password of the user which will access Test Manager  |
| tmsType					| null		| Type of the Test Manager ('squash' or 'hp')  |
| tmsProject				| null		| The project to which this test application is linked in Test manager   |

##### Bugtrackers #####

Below are the common parameters to all parameters. When using Jira, look below for the mandatory jira parameters

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| bugtrackerUrl				| null		| URL of the bugtracker  (e.g: Jira http://<jira_host>:<jira_port>/jira) |
| bugtrackerUser			| null		| User which will access Bug tracker  |
| bugtrackerPassword		| null		| password of the user which will access Bug tracker  |
| bugtrackerType			| null		| Type of the Bug tracker ('jira')  |
| bugtrackerProject			| null		| The project to which this test application is linked in Bug tracker. For jira, it's the project key   |

Additional optional paramters may be added

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| bugtracker.assignee		| (optional)| Person who will be notified of the new issue |
| bugtracker.reporter		| (optional)| Person which has reported |

###### Jira ######

Jira needs more parameters

| Param name       				| Default 		| Description  |
| -------------------------		| -------------	| ------------ |
| bugtracker.priority			| 			 	| The priority set for the issue. |
| bugtracker.jira.issueType		|				| Type of the issue that will be added. May depend on project. It MUST be set |
| bugtracker.jira.components	| <empty>		| Comma seperated list of components that will be added to the issue. |
| bugtracker.jira.openStates	|				| Comma seperated list of states (or status) that says that an issue is still open. It helps searching for existing issues. e.g: 'Open,Todo'. It MUST be set |
| bugtracker.jira.closeTransition|				| '/' separated list of transition names that will be applied to an issue when closing it. It may depend on projects. MUST be set. These transitions are localized and must lead to "closed" or "done" state. e.g: 'Start review/Finished review/Done' |
| bugtracker.jira.field.<fieldName>|			| Set 'bugtracker.jira.field.application=myApp' to set the field 'application' to 'myApp' when creating the issue. It's useful when some custom fields are mandatory to issue creation|				

##### Performance reporting #####

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| neoloadUserPath 			| null		| a name to give to your Neoload recordings. See chap7 for details on how to use Neoload with SeleniumRobot |
| nl.selenium.proxy.mode    | null		| 'Design' or 'EndUserExperience'. It's the mode in which test will be run. See How to §16 |

##### Snapshots #####

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| captureSnapshot 			| true 		| Capture page snapshots. Captures are done only when a new page is opened |
| snapshotTopCropping		| null			| number of pixel that will be cropped from the top when capturing snapshot. This only applies to snapshots done with several captures (like from chrome) when a portion of the GUI is fixed when scrolling. Default value 'null' means 'automatic'. SeleniumRobot will detect fixed headers and footers and crop them. If an integer value is given, we crop the requested pixels. '0' will crop nothing |
| snapshotBottomCropping	| null			| same as snapshotTopCropping for bottom cropping |
| snapshotScrollDelay		| 0			| time in ms between the browser scrolling (when it's needed) and effective capture. A higher value means we have chance all picture have been loaded (with progressive loading) but capture take more time. This is only valid when captures are done for image comparison. See [https://github.com/bhecquet/seleniumRobot/blob/master/doc/chap7_Howto.md#23-compare-snapshots](https://github.com/bhecquet/seleniumRobot/blob/master/doc/chap7_Howto.md#23-compare-snapshots) |

#### Deprecated ####

| Param name       			| Default 	| Description  |
| -------------------------	| ------- 	| ------------ |
| testMethodSignature 		|  			| define a specific method signature for hashcodes |
| pluginConfigPath 			|  			| plugins to add |

#### Minimal Configuration ####

See §3.3 - 'TestNG file' for the minimal TestNG XML file to use to start a test

#### Centralized XML configuration ####

A test application may have several TestNG XML files but some of the parameters may be common.
To avoid maintaining several files with the same values, SeleniumRobot allows to centralize these parameters into one specific file.
This file is referenced in XML configuration with:

`<parameter name="testConfig" value="<filePath>.xml" />`

Below is an example of this file extending TestNG configuration

```xml
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
			<parameter name="webDriverGrid" value="http://localhost:4723/wd/hub" />
		</service>
		
		<service name="saucelabs">
			<parameter name="webDriverGrid" value="http://xxx:aaaaa-26d7-44fa-bbbb-b2c75cdccafd@ondemand.saucelabs.com:80/wd/hub" />
		</service>
		
	<parameters>
```
	
You can define:
 
- *parameters* like in main XML configuration.
- *device definition*: Allows to only specify the device name in parameter. Platform will then be get from device definition
_e.g_: in testNG.xml, you have <parameter name="deviceName" value="Samsung Galaxy Nexus SPH-L700 4.3" /><br/>
SeleniumRobot will translate that in:
`<parameter name="deviceName" value="Samsung Galaxy Nexus SPH-L700 4.3" />`
and
`<parameter name="platform" value="Android 4.3" />`

- *service definition* used for runMode option<br/>
You can define one service for each runMode, with the same name: local, grid, saucelabs<br/>
Under each service, you can then add any parameter needed to address this runMode as in the above example.
_e.g_: if user select "saucelabs" runMode, then the 1 parameter (webDriverGrid) will be added to configuration

### 2 Test WebApp with desktop browser ###

#### Test locally ####

The minimal parameters to pass to SeleniumRobot are:
`browser`: MUST be defined because default browser is None

#### Test with SeleniumRobot Grid ####

Test must be configured with `runMode` and `webDriverGrid` options (or use `-DrunMode=grid -DwebDriverGrid=http://127.0.0.1:4444/wd/hub`)
 
```xml
	<test name="MRH">
    	<parameter name="runMode" value="grid" />
    	<parameter name="webDriverGrid" value="http://127.0.0.1:4444/wd/hub" />
    	
        <packages> 
            <package name="com.seleniumtests.core.runner.*"/>
        </packages>
    </test>
```

#### Test with BrowserStack ####

BrowserStack is seen as a selenium grid.
You the MUST use options:
- `runMode=browserstack` to enable browserstack
- `DwebDriverGrid=http://<user>:<key>@hub-cloud.browserstack.com/wd/hub` => see browserstack documentation
- `platform=<platform>` => Use browserstack [capabilities generator](https://www.browserstack.com/automate/capabilities) to see which platforms are available. Platform to set here is the concatenation of `os` and `os_version` for desktop. E.g: "Windows 10"

If you run behind a proxy, also use the JVM options: `-Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port> -Dhttps.nonProxyHosts=<nonProxy>`

### 3 Test with Appium locally ###

For mobile tests, set the following environment variables on your local computer:
APPIUM_HOME: 
- On Windows, using .exe, it's the root path where Appium.exe is located
- On Mac, using .dmg, it will be `/Applications/Appium.app/Contents/Resources/app`
- On any platform using npm installation, it will be the path where root `node_modules` folder has been created. This folder should contain an `appium` subfolder
- `ANDROID_SDK_ROOT` / `ANDROID_HOME`: path to Android SDK (e.g: where SDK Manager resides). This is the root folder containing 'platform-tools', 'system-images', ... folders

Also check that there is only one version of ADB on computer. Otherwise, there may be conflicts and ADB client you provide may not get relevant information from devices

When using seleniumRobot-grid, these environment variables will be set on grid node
For cloud test, these variables are not needed

#### Application test on android ####

Define test as follows (minimal needed options)

```xml
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
```


`deviceName` reflects the local device used to automate the test
`app` is the path of the application file. It can be an URL. If access to URL is restricted, use the pattern "http://<user>:<password>@<host>:<port>/path"
`appPackage` and `appActivity` can be found in APK manifest file

#### Application test on iOS ####

Define test as follows (minimal needed options)

```xml
    <test name="tnr_appium_mobile_app" parallel="false">
    
    	<parameter name="browser" value="*safari" />
    	<parameter name="platform" value="iOS 10.3"/>
    	<parameter name="deviceName" value="iPhone SE"/>
    	<parameter name="app" value="<local_path_to_ipa_or_app.zip file>"/>
    
    	<packages>
    		<package name="com.seleniumtests.core.runner.*"/>
    	</packages>
    </test>
```


### 4 Test with SauceLabs ###

You the MUST use options:
- `runMode=saucelabs` to enable SauceLabs
- `DwebDriverGrid=http://<user>:<key>@ondemand.eu-central-1.saucelabs.com:443/wd/hub` => see saucelabs documentation
- `platform=<platform>` 
- `deviceName=<name>` => name of the device to use.
- `app=<application file>` => the application to test. By default, application is uploaded. If you want to avoid this step (application has already been uploaded), add 'NO_UPLOAD:' prefix to application path

Define test as follows

```xml
	<test name="tnr_sauce_mobile_app" parallel="false">
    	<parameter name="cucumberTests" value="Configuration" />
	    <parameter name="cucumberTags" value="" />
	    <parameter name="runMode" value="saucelabs" />
	    
        <parameter name="webDriverGrid" value="http://<user>:<key>@ondemand.saucelabs.com:80/wd/hub"/>
        <parameter name="deviceName" value="Android Emulator"/>
        <parameter name="platform" value="Android 5.1"/>
        
        <parameter name="app" value="<local_path_to_apk>"/>
    	<parameter name="appPackage" value="com.infotel.mobile.infolidays"/>
    	<parameter name="appActivity" value="com.infotel.mobile.mesconges.view.activity.StartActivity"/>

        <packages>
            <package name="com.seleniumtests.core.runner.*"/>
        </packages>
    </test>
```
    
### 5 Test with BrowserStack ####

BrowserStack is seen as a selenium grid.
You the MUST use options:
- `runMode=browserstack` to enable browserstack
- `DwebDriverGrid=http://<user>:<key>@hub-cloud.browserstack.com/wd/hub` => see browserstack documentation
- `platform=<platform>` => Use browserstack [capabilities generator](https://www.browserstack.com/automate/capabilities) to see which platforms are available. Platform to set here "Android X.Y" or "iOS X"
- `deviceName=<name>` => name of the device to use. Use browserstack [capabilities generator](https://www.browserstack.com/automate/capabilities) to get the list
- `app=<application file>` => the application to test. By default, application is uploaded. If you want to avoid this step (application has already been uploaded), add 'NO_UPLOAD:' prefix to application path

If you run behind a proxy, also use the JVM options: `-Dhttp.proxyHost=<host> -Dhttp.proxyPort=<port> -Dhttp.nonProxyHosts=<nonProxy> -Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port>`

### 6 Test with Perfecto ####

Perfecto is seen as a selenium grid.
You the MUST use options:
- `runMode=perfecto` to enable browserstack
- `DwebDriverGrid=https://<apiKey>@<cloudname>.perfectomobile.com/nexperience/perfectomobile/wd/hub` => see perfecto documentation
- `platform=<platform>` 
- `deviceName=<name>` => name of the device to use. 
- `app=<application file>` => the application to test. By default, application is uploaded. If you want to avoid this step (application has already been uploaded), add 'NO_UPLOAD:' prefix to application path

If you run behind a proxy, also use the JVM options: `-Dhttps.nonProxyHosts=<nonProxy> -Dhttps.proxyHost=<host> -Dhttps.proxyPort=<port>`
 
### 7 Test with SeleniumGrid ###

SeleniumGrid allows to address multiple selenium nodes from one central point
![](/images/seleniumGrid.png) 
In this mode, SeleniumRobot addresses the Hub and then, the hub dispatches browser creation on available nodes, for mobile or desktop tests.

For better features, prefer using seleniumRobot-grid which is based on standard grid

#### Configure SeleniumRobot ####

Test must be configured like the example below (or use `-DrunMode=grid -DwebDriverGrid=http://127.0.0.1:4444/wd/hub`)
 
 ```xml
	<test name="MRH">
    	<parameter name="runMode" value="grid" />
    	<parameter name="webDriverGrid" value="http://127.0.0.1:4444/wd/hub" />
    	
        <packages> 
            <package name="com.seleniumtests.core.runner.*"/>
        </packages>
    </test>
```

#### Configure Grid hub ####

**/!\ Useless with SeleniumRobot grid !!**

Hub configuration from command line or JSON is provided here: 
[https://github.com/SeleniumHQ/selenium/wiki/Grid2](https://github.com/SeleniumHQ/selenium/wiki/Grid2)

Hub configuration should use a browserTimeout of 60 seconds

#### Configure Grid node ####

**/!\ Useless with SeleniumRobot grid !!**

Node configuration from command line or JSON is provided here: 
[https://github.com/SeleniumHQ/selenium/wiki/Grid2](https://github.com/SeleniumHQ/selenium/wiki/Grid2)

You should use JSON configuration for nodes, to make it simpler to start

##### Desktop node #####

To add a browser / browser version, add following code (change info if necessary)

```json
	{
      "browserName": "firefox",
      "maxInstances": 5,
      "seleniumProtocol": "WebDriver",
	  "version": "last",
	  "firefox_binary": "C:/Program Files (x86)/Mozilla Firefox/firefox.exe"
    },
```

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

```json
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
```

From command line, use
`appium --nodeconfig /path/to/nodeconfig.json`

From GUI, node config file can be specified in appium options

### 6 Running tests in parallel ###
When testing among several browsers or devices is requested, it's easier to run these tests in parallel

#### Test NG way ####
Test NG is able to run tests in parallel using the XML configuration
To do so, configure several tests, one for each browser / mobile device
	
```xml
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
```
    
Then, in suite definition, in the XML, set parallel mode

```xml
	<suite name="My suite" parallel="tests" thread-count="5">
```

#### Jenkins way ####

##### Matrix Job #####

Create a Matrix job with an axis called "browser"
Then, add all browser you want to use to this axis
Finally, configure a shell / batch script to use the created variable "%BROWSER%"

##### Selenium Capability Axis #####

Jenkins offers Selenium Capability Axis to create a matrix configured through Selenium Grid

### 8 The test results ###

SeleniumRobot generates several test results after run
Results are written in the directory pointed by the option `-DoutputDirectory=<dir>` or by default in the `test-output` directory

#### HTML result ####

`SeleniumTestReport.html` file shows global result. It links to per-test file

#### JUnit XML global result ####

In `junitreports` for each Test class, an XML file TEST-xxx.xml is generated. It contains the list of tests and the error messages.
This can be used to publish results in Jenkins for example.

#### JUnit XML per test result ####

In each test sub-directory, a file `PERF-result.xml` is generated. In this case, the testsuite is the test itself and the testcase is a test step.
This can be used to build more detailed results from this data.
This can also be used by Jenkins Performance Publisher plugin

#### JSON global result ####

Only shows number of tests, skipped, failed, ...

#### Custom result ####

You can also build your own test result based on your template

##### Custom Test report #####

Option `customTestReports` allows to build a custom per-test report. See usage above

Reporter provides the following data to the template:
- *errors*: number of errors (0)
- *failures*: number of failures. It test is skipped, this number is -1
- *hostname*: computer which executes test
- *suiteName*: name of the test
- *className*: name of test class
- *tests*: number of steps
- *duration*: total duration of the test
- *time*: start time (milliseconds since epoch)
- *startDate*: start date
- *testSteps*: list of test steps (TestStep object)
- *browser*: used browser
- *version*: version of test application
- *parameters*: list of test parameters
- *stacktrace*: stacktrace if available
- *logs*: logs

Allowed output formats are: 'xml', 'json', 'html', 'csv'

##### Custom Summary report #####

Option `customSummaryReports` allows to build a summary report. See usage above

Reporter provides the following data to the template:

- *pass*: number of passed tests
- *fail*: number of failed tests
- *skip*: number of skipped tests
- *total*: total number of tests

#### Default TestNG reports ####

By default, TestNG produces its own reports
they are located in the folder pointed by `-d` option, by default `test-ouput` where test is launched.

If you do not want to generate these files, add `usedefaultlistener false` to your TestNG options

### 9 Execute Selenium IDE Tests ###

From version 4.10, it is possible to execute Selenium IDE tests.
- Create your test / suite with selenium IDE
- Export it with JUnit / Java as a .java file
- Run the following command: **don't forget to specify the browser parameter, else you will get a NullPointerException**

`java -cp seleniumRobot.jar -D<option1>=<value1> -D<option2>=<value2> -Dbrowser=<browser> -javaagent:aspectjweaver.jar com.seleniumtests.util.ide.SeleniumIdeLauncher -scripts test1.java,test2.java`

or, for local

`java -cp seleniumRobot.jar;lib/drivers/* -D<option1>=<value1> -D<option2>=<value2> -Dbrowser=<browser> -javaagent:aspectjweaver.jar com.seleniumtests.util.ide.SeleniumIdeLauncher -scripts test1.java,test2.java`

**/!\ AS we compile on the fly, a java JDK MUST be used, else you will get: java.lang.AssertionError: java.lang.ClassNotFoundException: com.sun.tools.javac.api.JavacTool**


aspectjweaver.jar is provided in seleniumRobot zip file

Options are the same as for any other SeleniumRobot test

Benefits to execute Selenium IDE tests this way are:

- Use the same framework for all tests
- Have full debug features (snapshot / video / network)
- Retry when searching elements
- ...

For specifics about Selenium IDE writing, see §3.14
- If you've added manual steps (as described in §3.14), add the option `-DmanualTestSteps=true`

## Troubleshooting ##

### Cyclic dependency when executing TestNG Test ###

This may be due to the fact that 2 test configuration methods (seen with @BeforeTest) have the same name when executed in the same TestNG <test> 
