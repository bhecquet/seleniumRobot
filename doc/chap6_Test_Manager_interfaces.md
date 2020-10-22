<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [0 SeleniumRobot Server](#0-seleniumrobot-server)
- [Test managers](#test-managers)
  - [1 Squash TM/TA](#1-squash-tmta)
    - [Execution of test framework](#execution-of-test-framework)
    - [Squash TM configuration](#squash-tm-configuration)
    - [TA files generation](#ta-files-generation)
      - [Cucumber mode](#cucumber-mode)
      - [TestNG mode](#testng-mode)
    - [Squash TA job configuration](#squash-ta-job-configuration)
  - [2 Jenkins](#2-jenkins)
  - [3 Squash TM through API](#3-squash-tm-through-api)
    - [Enable Squash TM usage](#enable-squash-tm-usage)
    - [Configure Test to be linked with Squash TM](#configure-test-to-be-linked-with-squash-tm)
  - [4 HP ALM](#4-hp-alm)
    - [Configure environment to access HP ALM](#configure-environment-to-access-hp-alm)
    - [Configure test runner computer](#configure-test-runner-computer)
    - [Create test on ALM](#create-test-on-alm)
    - [Test script](#test-script)
    - [test parameters](#test-parameters)
    - [Run test](#run-test)
- [Bugtrackers](#bugtrackers)
  - [1 Jira](#1-jira)
    - [Configuration](#configuration)
    - [Find required fields and allowed values](#find-required-fields-and-allowed-values)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## 0 SeleniumRobot Server ##
SeleniumRobot server [https://github.com/bhecquet/seleniumRobot-server](https://github.com/bhecquet/seleniumRobot-server) is a server which aims to be used with SeleniumRobot, giving the following features:
- handle test variables: instead of setting test data into env.ini file, they can be set in server, which offers more features when sharing values among test projects
- compare snapshots: as Seleniumrobot takes snapshots, it's able to send them to server which can compare with the same step of the same test executed previously
- record test results: Even if test results are recorded in a test manager, the full result format with snapshots may not be available directly

To use this server, 
- first deploy it (see documentation of seleniumRobot-server project)
- create test application, environment on server
- create application version. This is the same version as `major.minor` version in your pom.xml. SeleniumRobot may create it automatically on start
- populate test variables with the one you placed in env.ini file.
- set a JVM parameter `seleniumRobotServerUrl` when starting the test, giving the root url of the server. E.g: `http://seleniumRobotServer:8000`. This can be replaced by a parameter inside XML testNG file
- parameter `seleniumRobotServerActive` must be set to true. By default, only variable fetching is done when server is active
- parameter `seleniumRobotServerCompareSnapshots` can be set to true for tests where snapshot comparison is required (defaults to false)
- parameter `seleniumRobotServerRecordResults` can be set to true when results should be recorded on server (defaults to false)
- run your tests. SeleniumRobot will automatically connect to server and send data

## Test managers ##

Test manager are softwares that help managing test campaigns, requirements and tests results. For selenium test, the aim is to execute selenium test instead of manual tests and record results

### 1 Squash TM/TA ###
SeleniumRobot can work with Squash TA by using an intermediate .java file. This file handles execution of test framework using a command line.
Moreover, SeleniumRobot can generate .ta, pom.xml and .java files automatically. See "TA files generation" part.
**Following instruction expect the use of generated files**

#### Execution of test framework ####
This needs some environment variable configuration:

- Add a `STF_HOME` environment variable pointing to the folder where STF is deployed
- Add a `JAVA_HOME_STF` environment variable pointing to a Java 8 installation. This is mandatory as for now (Squash TA 1.9), Squash only supports Java 7 whereas STF is built using Java 8.

**Execution platform must be installed with Java 8**

A typical command line would be (included in generated .java file):


    "%JAVA_HOME_STF%/bin/java\" -cp %STF_HOME%/seleniumRobot.jar:%STF_HOME%/plugins/${application}-tests.jar -Dbrowser=${IT_CUF_browser} ${TC_CUF_cucumberTest} -Denv=${IT_CUF_testEnvironment} org.testng.TestNG ${testngFile} -testnames ${testngName}

Each ${} is a variable passed by the .ta script when replacing variables by their actual value.

#### Squash TM configuration ####
Launching of SeleniumRobot with pre-configured test applications expects some variables passed to the script. These MUST be declared on Squash TM side:

- browser (iteration custom field)
- testEnvironment (iteration custom field)


![](images/squash_tm_cuf.png)

**Make sure your .ta script reflects this choice**
 
#### TA files generation ####
Squash TM needs a list of .ta files to detect which test scripts are available.
These .ta files can be written manually but, as they all use the same format, they can also be generated when building test application. To allow this generation, add the following in pom.xml of the test applications.

```xml
    <plugin>
		<groupId>org.codehaus.mojo</groupId>
		<artifactId>exec-maven-plugin</artifactId>
		<version>1.5.0</version>
		<executions>
			<execution>
				<id>squash-ta-generator</id>
				<phase>generate-sources</phase>
				<goals>
					<goal>java</goal>
				</goals>
				<configuration>
					<mainClass>com.seleniumtests.util.squashta.TaFolderStructureGenerator</mainClass>
					<arguments>
						<argument>${project.artifactId}</argument>
						<argument>${project.basedir}</argument>
						<argument>${project.build.directory}/data/${project.artifactId}/squash-ta</argument>
					</arguments>
				</configuration>
			</execution>
		</executions>
	</plugin>
```

Generation is done by copying a test file `resources/squash-ta/squash_generic.ta` where testNG file name and testNG name are replaced.
pom.xml and java files used by Squash TA to launch the test are also copied to the destination directory.
You get the following structure which is directly used by Squash TA

![](images/ta_generation.png)

It's possible to customize SeleniumRobotTest.java file or squash_generic.ta file. If so, create squash-ta specific folder structure inside `data` folder of the test application and put the files you modified. 
They will be used on next generation, instead of default ones

![](images/squash_ta_structure.png)

##### Cucumber mode #####
A .ta test script will be generated for each test (in testNG file) where `cucumberTests` or `cucumberTags` parameters are used. 
In case `cucumberRunner` package is mentioned but none of `cucumberTests` or `cucumberTags` is defined, then one .ta file will be generated for each available cucumber scenarion. When launching this type of .ta script, cucumber variables contained in the testNG.xml file will be overridden.

If some of the scenarios should not be available through Squash TA: precede them with the tag 


    @EXCLUDE_FROM_SQUASH_TA

![](images/excluded_scenario.png)
 
##### TestNG mode #####
A .ta test script will be generated for each TestNG test.
Below is an example of what has been generated for cucumber scenarios.

![](images/squashtm_select_test.png)
 
If some of the tests should not be available in Squash TA, add parameter inside test: 

```xml
    <parameter name="EXCLUDE_FROM_SQUASH_TA" value="" />
```

![](images/exclude_testng.png)
 
#### Squash TA job configuration ####
By default, Squash TM receives the Squash TA test report. But, it’s useless when executing tests with SeleniumRobot as it does not contain any details. So we have to configure the SeleniumRobot report to be sent to Squash TM
Add the following line below « Publish HTML reports »

![](images/squash_ta_config.png)
 
In maven command line (goals & options), replace

    -Dta.tmcallback.reportname= Squash_TA_HTML_Report
by

    -Dta.tmcallback.reportname=SeleniumRobot_Report
    
### 2 Jenkins ###

To execute SeleniumRobot using Jenkins, create a free-style job.

- In "Build" section, add a shell command: `${JENKINS_HOME}/tools/hudson.tasks.Maven_MavenInstallation/Maven_3/bin/mvn -U org.apache.maven.plugins:maven-dependency-plugin:2.8:unpack -Dartifact=com.infotel.seleniumRobot:core:RELEASE:zip -DoutputDirectory=${WORKSPACE}/tmp/seleniumRobot  -Dmdep.overWriteReleases=true`<br/>
This command will update core artifact to the latest version on maven
- In "Build" section, add a shell command: `${JENKINS_HOME}/tools/hudson.tasks.Maven_MavenInstallation/Maven_3/bin/mvn -gs ${MVN_SETTINGS} -U org.apache.maven.plugins:maven-dependency-plugin:2.8:unpack -Dartifact=<groupId>:<artifactId>:RELEASE:zip -DoutputDirectory=${WORKSPACE}/tmp/seleniumRobot  -Dmdep.overWriteReleases=true`<br/>
This command will update the test application to its latest release from maven repo
	- Replace `<groupId>` by the groupId of your test application artifact
	- Replace `<artifactId>` by the artifactId of your test application

- In "Build" section, add a shell command: `java -cp <sr_home>/seleniumRobot.jar;<sr_home>/plugins/<app_name>-tests.jar -DtestRetryCount=0 -Dbrowser=chrome -Denv=Integration org.testng.TestNG %STF_HOME%/data/<app_name>/testng/<testng.xml> -testnames <testnames>`<br/>
  - Replace `<sr_home>` by the folder where seleniumRobot is deployed
  - Replace `<app_name>` by the name of the test application. e.g 'jpetstore'
  - Replace `<testng.xml>` by the XML file to execute
  - Replace `<testnames>` by the names of tests to execute (they must be present in XML file)
  ![](images/jenkins_job_command.png)

- In "Post build actions", configure like this<br/>
  ![](images/jenkins_job_publish_html.png)

  ![](images/jenkins_job_publish_perf.png)

  ![](images/jenkins_job_publish_testng.png)
  
SeleniumRobot uses external fonts and javascript so Jenkins must be configured to relax CSP (Content-Security-policy) if it's acceptable to you: [https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy] (https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy)

e.g: `"default-src 'self' fonts.googleapis.com cdnjs.cloudflare.com fonts.gstatic.com 'unsafe-inline' 'unsafe-eval'"`

### 3 Squash TM through API ###

As of Squash TM 1.21, API is complete enough to allow to create campaigns, iteration, test results directly from it. So it's possible to execute a test from Jenkins and have the results directly sent to Squash TM

#### Enable Squash TM usage ####

Use parameters:

- `tmsType` => 'squash'
- `tmsUrl` => url of squash server. (e.g: http://localhost:8080/squash)
- `tmsUser` => user to connect 
- `tmsPassword` => password used to connect
- `tmsProject`=> project to which these tests belong

#### Configure Test to be linked with Squash TM ####

By default, test result won't be sent to Squash TM even if it's properly configured.
For this link to be active, we MUST give seleniumRobot the 'id' of the test in Squash

Test id can be found when clicking on any test, and hovering mouse on test title in the right pane => a link is displayed at the bottom left of the screen, giving the id of the test.

When you have this id, configure your java test

```java
@Test(attributes = {@CustomAttribute(name = "testId", values = "12")})
public void testMyFeature() {
	...
}
```

  
### 4 HP ALM ###
 
From ALM v11 HP ALM can run seleniumRobot tests using VBScript connector

**WARNING**: This connector is not currently fully fonctional as launch_SeleniumRobot.bat script does not exist anymore
VBS script should be updated, for example, using a direct java call with JVM options and TestNG parameters clearly identified

#### Configure environment to access HP ALM ####

- `tmsType` => 'hp'
- `tmsUrl` => url of ALM server. (e.g: http://myamlserver:8080)
- `tmsUser` => user to connect 
- `tmsPassword` => password used to connect
- `tmsProject`=> project to which these tests belong
- `tmsDomain` => Domain of the project
- `tmsRun` => id of the current run


This paramater is common to all tests and can be written in TestNG XML file or in a common configuration file loaded by TestNG XML file (param `testConfig`) 

Run information (specific to test running) must be put in `tmsRun`

 
#### Configure test runner computer ####
 
Create `SELENIUMROBOT_HOME` environment variable, pointing to the path where robot is available (unzipped, presence of launch.bat file)
 
#### Create test on ALM ####
 
In ALM, create a VAPI-XP test

![](images/alm_vapi.png)

Click 'OK'

![](images/alm_vbscript.png)

Keep 'VBSCript' and click 'Next'

![](images/alm_console.png)

Choose 'Console application' and click 'Finish'

#### Test script ####

In Test plan, go to newly created test, "Test script" tab and paste the following content
 	

	' ----------------------------------------------------
	' Main Test Function
	' Debug - Boolean. Equals to false if running in [Test Mode] : reporting to Quality Center
	' CurrentTestSet - [OTA COM Library].TestSet.
	' CurrentTSTest - [OTA COM Library].TSTest.
	' CurrentRun - [OTA COM Library].Run.
	' ----------------------------------------------------
	Sub Test_Main(Debug, CurrentTestSet, CurrentTSTest, CurrentRun)
	  ' *** VBScript Limitation ! ***
	  ' "On Error Resume Next" statement suppresses run-time script errors.
	  ' To handle run-time error in a right way, you need to put "If Err.Number <> 0 Then"
	  ' after each line of code that can cause such a run-time error.
	  On Error Resume Next
	
	  ' clear output window
	  TDOutput.Clear
	
	  seleniumRobotHome = CreateObject( "WScript.Shell" ).Environment( "SYSTEM" )("SELENIUMROBOT_HOME") & "\launch_seleniumRobot.bat"
	  
	  options = ""
	  With CurrentTSTest.Params
	    For i = 0 To .Count - 1
	      options = options & "-D" & Trim(.ParamName(i)) & "=" & .ParamValue(i) & " "
	    Next
	  End With
	
	
	  ' Run seleniumBot application
	  result = XTools.run(seleniumRobotHome , options & " -DtmsRun={'type':'hp','run':" & CurrentRun.ID & "}", -1)
	
	  If Err.Number <> 0 Or result <> 0 Then
	    TDOutput.Print "Run-time error [" & Err.Number & "] : " & Err.Description
	    ' update execution status in "Test" mode
	    If Not Debug Then
	      CurrentRun.Status = "Failed"
	      CurrentTSTest.Status = "Failed"
	    End If
	  End If
	End Sub
	
#### test parameters ####
	
In 'parameters' tab, add specific test parameters 

![](images/alm_parameters.png)

#### Run test ####

In test lab, create a test set with this automated test. Double click test instance and configure "execution settings"

![](images/alm_runtest.png)

Test run is possible when actual values are configured. You can use "copy default values" if they are correct. When test finishes, robot records test details as a test attachment

![](images/alm_result.png)

## Bugtrackers ##

Bugtrackers will store issues when a selenium test fails.
If the issue already exists for this failing test, it's not recreated.
If the issue exists and the test becomes sucessful, then issue is closed

### 1 Jira ###

#### Configuration ####

Many parameters drive the use of jira because this software is highly configurable

typical / minimal configuration would be

```
	# common bugtracker options
	-DbugtrackerUrl=https://my.jira.server/jira
	-DbugtrackerUser=myUser
	-DbugtrackerPassword=myPassword
	-DbugtrackerProject=PROJECTKEY
	-DbugtrackerType=jira
	
	# specific jira options
	-Dbugtracker.priority=Important				# name of the priority to set to issue
	-Dbugtracker.jira.issueType=Bug				# name of the issue type, as stated in GUI
	-Dbugtracker.jira.openStates=Open,Todo	# name of states that say the issue is not closed. It depends on the workflow
	-Dbugtracker.jira.closeTransition=Done	# name of the transition to go to "closed" state. 
```
Many transitions may be defined so that several steps of the workflow can be run through.
for example: `To Analyze/To resolve/Resolve`
SeleniumRobot will look for the current available transition of the issue and then run through all the remaining ones


Depending on jira project, you may need to specify additional options if the fields (components and custom fields) are mandatory

```
	-Dbugtracker.jira.components=Component1
	-Dbugtracker.jira.field.myFieldName1=myFieldValue1		# myFieldName is the name of the custom field as defined in GUI
	-Dbugtracker.jira.field.myFieldName1=myFieldValue1
```

#### Find required fields and allowed values ####

As it's not always easy to find which values to set to the issue, you can execute the class com.seleniumtests.connectors.bugtracker.jira.JiraConnector with the following arguments: `java -cp ... com.seleniumtests.connectors.bugtracker.jira.JiraConnector <jiraUrl> <projectKey> <user> <password> <issueType>`
It will print all required fields with allowed values if applicable
