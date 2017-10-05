### 0 SeleniumRobot Server ###
SeleniumRobot server [https://github.com/bhecquet/seleniumRobot-server](https://github.com/bhecquet/seleniumRobot-server) is a server which aims to be used with SeleniumRobot, giving the following features:
- handle test variables: instead of setting test data into env.ini file, they can be set in server, which offers more features when sharing values among test projects
- compare snapshots: as Seleniumrobot takes snapshots, it's able to send them to server which can compare with the same step of the same test executed previously
- record test results: Even if test results are recorded in a test manager, the full result format with snapshots may not be available directly

To use this server, 
- first deploy it (see documentation of seleniumRobot-server project)
- set an environment variable `seleniumRobotServerUrl` on the seleniumRobot executor, giving the root url of the server. E.g: `http://seleniumRobotServer:8000`. This can be replaced by a parameter inside XML testNG file
- parameter `seleniumRobotServerActive` must be set to true. By default, only variable fetching is done when server is active
- parameter `seleniumRobotServerCompareSnapshots` can be set to true for tests where snapshot comparison is required (defaults to false)
- parameter `seleniumRobotServerRecordResults` can be set to true when results should be recorded on server (defaults to false)
- run your tests. SeleniumRobot will automatically connect to server and send data

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

    <parameter name="EXCLUDE_FROM_SQUASH_TA" value="" />

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
  
### 3 HP ALM ###
 
From ALM v11 HP ALM can run seleniumRobot tests using VBScript connector

*WARNING*: This connector is not currently fully fonctional as launch_SeleniumRobot.bat script does not exist anymore
VBS script should be updated, for example, using a direct java call with JVM options and TestNG parameters lcearly identified

#### 3.0 Configure environment to access HP ALM ####

Put connection information into env.ini file:
- hpAlmServerUrl: url to server. It should not contain the '/qcbin' part
- hpAlmProject: project to connect to
- hpAlmDomain: domain of the project
- hpAlmUser: user
- hpAlmPassword: password 
 
#### 3.1 Configure test runner computer ####
 
Create `SELENIUMROBOT_HOME` environment variable, pointing to the path where robot is available (unzipped, presence of launch.bat file)
 
#### 3.2 Create test on ALM ####
 
In ALM, create a VAPI-XP test

![](images/alm_vapi.png)

Click 'OK'

![](images/alm_vbscript.png)

Keep 'VBSCript' and click 'Next'

![](images/alm_console.png)

Choose 'Console application' and click 'Finish'

#### 3.3 Test script ####

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
	  result = XTools.run(seleniumRobotHome , options & " -Dtms={'type':'hp','run':" & CurrentRun.ID & "}", -1)
	
	  If Err.Number <> 0 Or result <> 0 Then
	    TDOutput.Print "Run-time error [" & Err.Number & "] : " & Err.Description
	    ' update execution status in "Test" mode
	    If Not Debug Then
	      CurrentRun.Status = "Failed"
	      CurrentTSTest.Status = "Failed"
	    End If
	  End If
	End Sub
	
#### 3.4 test parameters ####
	
In 'parameters' tab, add specific test parameters 

![](images/alm_parameters.png)

#### 3.5 Run test ####

In test lab, create a test set with this automated test. Double click test instance and configure "execution settings"

![](images/alm_runtest.png)

Test run is possible when actual values are configured. You can use "copy default values" if they are correct. When test finishes, robot records test details as a test attachment

![](images/alm_result.png)


