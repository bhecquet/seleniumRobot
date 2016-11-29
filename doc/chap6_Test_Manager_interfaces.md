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

- In "Build" section, add a shell command: `java -cp <sr_home>/seleniumRobot.jar;<sr_home>/plugins/<app_name>-tests.jar -DtestRetryCount=0 -Dbrowser=chrome -Denv=Integration org.testng.TestNG %STF_HOME%/data/<app_name>/testng/<testng.xml> -testnames <testnames>`</br>
  - Replace `<sr_home>` by the folder where seleniumRobot is deployed
  - Replace `<app_name>` by the name of the test application. e.g 'jpetstore'
  - Replace `<testng.xml>` by the XML file to execute
  - Replace `<testnames>` by the names of tests to execute (they must be present in XML file)
  ![](images/jenkins_job_command.png)

- In "Post build actions", configure like this<br/>
  ![](images/jenkins_job_publish_html.png)

  ![](images/jenkins_job_publish_perf.png)

  ![](images/jenkins_job_publish_testng.png)
