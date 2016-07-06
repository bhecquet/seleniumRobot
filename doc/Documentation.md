# SeleniumRobot documentation #

## Introduction ##
SeleniumRobot is a test framework based on Selenium & Appium API to ease Web and Mobile testing. It's based on the seleniumtestsframework open-source project [https://github.com/tarun3kumar/seleniumtestsframework](https://github.com/tarun3kumar/seleniumtestsframework)

Main features are:

- Test using browsers (Desktop & Mobile)
	- Firefox
	- Chrome
	- Internet Explorer
	- Safari
	- Opera
	- HTMLUnit (a headless browser)
- Test of mobile applications on
	- Android
	- iOS
- Tests in
	- Local (on current computer)
	- Using SeleniumGrid
	- SauceLabs environment
	- Testdroid environment
- Snapshot on steps
- HTML & xUnit report
- Gherkin / Cucumber compatibility
- Replay on failure
- Selenium error recovery

## Installation ##

### Development environment ###
SeleniumRobot is developped using eclipse IDE. Following plugins are mandatory:

- aspectj plugin (AJDT): use the dev version are older versions are not compatible with recent eclipse versions
- m2e plugin (maven)
- subeclipse (if using SVN for test application)
- TestNG (execute testNG tests)

SeleniumRobot needs Java 8 to be compiled.

#### Sign artifact for deploy phase ####
OSS needs artifacts to be signed before being deployed
Therefore, generated a GPG key (it will ask a name and a password)

	gpg --gen-key

Copy key to a public key server

	gpg2 --keyserver hkp://pgp.mit.edu --send-key <key_name>

#### Git key for release ####
In order for maven to push tags on release, a key must be generated for SSH connection

    ssh-keygen -t rsa -C '<key name>’
Copy the generated public key to Github
Check connection (it should reply: You've successfuly authenticated)

    ssh git@github.com

#### Maven configuration ####
Maven 3 is required

Following configurations should be placed in user `settings.xml` file:

For GPG

	<profile>
		<id>gpg</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
      <properties>
        <gpg.executable>gpg2</gpg.executable>
        <gpg.passphrase><your key password></gpg.passphrase>
      </properties>
    </profile>

For Sonar analysis

	<profile>
		<id>sonar</id>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
		<properties>
			<sonar.host.url>http://server:9000</sonar.host.url>
		</properties>
	</profile>

For publishing artifacts to OSS Sonatype server

	<server>
      <id>ossrh</id>
      <username><your_user></username>
      <password><your_password></password>
    </server>

### Execution environment ###
Execution environment needs at least Java 8. SeleniumRobot is compatible with Windows, Mac OS and Linux.
Depending on your tests, you should consider install:

- A browser 
- Appium
- Android SDK / Genymotion to test on android simulator / emulator
- XCode (Mac OS X) to test on iPhone Simulator 

## Test writting ##

### Create a new test application ###
A "test application" is the code specific to the Web or Mobile application under test. It consists of testNG files, Cucumber feature files, configurations and Java implementation files.

Use seleniumRobot-example project as a base to develop your own test application
[https://github.com/bhecquet/seleniumRobot-example](https://github.com/bhecquet/seleniumRobot-example)

#### Requirements are #### 

- Test data are in `data/<app_name>/`
	- `features` contains feature files (mandatory if cucumber mode is used)
	- `testng` contains testng files to start tests (mandatory)
	- `config` contains centralized env test configuration (optional)
	- `squash-ta` contains ta files that should override the default ones (optional). See Squash-TA section for details 

- Test code must be in `src/test/java`
- package name is free but following structure should be used
	- `cucumber` subpackage contains cucumber implementation files. As cucumber annotations can be written directly in test page object, it should at least contain code for `@Then` (checks) and `@Given` (initial state).
If pure TestNG mode is used, this package should not exist.
	- `tests` subpackage contains code for pure TestNG tests (not cucumber). If cucumber mode is used, this package should not exist
	- `webpage` subpackage is mandatory as it contains PageObject implementation

![](/images/package_structure.png)

### PageObject ###
PageObject is a design pattern that helps writing maintainable selenium applications. Each screen of the website or mobile application is a class.
This class contains:

- fields which are the elements on the page
	- links
	- buttons
	- text fields
	- ...
- methods which are the available actions in the page
	- validate form
	- fill form
	- access an other page

Example of a shopping cart class:

	public class ShoppingCart extends HeaderAndFooter {
	
		private static LinkElement proceed = new LinkElement("Checkout", By.linkText("Proceed to Checkout"));
		private static LinkElement updateCart = new LinkElement("updateCart", By.name("updateCartQuantities"));
		private static Table cart = new Table("cart", By.tagName("table"));
	
		public ShoppingCart() throws Exception {
			super(proceed);
		}
		
		public ShoppingCart changeQuantity(String item, String newQuantity) {
			new TextFieldElement("quantity", By.name(item)).sendKeys(newQuantity);
			updateCart.click();
			return this;
		}
		
		public String getTotalAmount() {
			String amount = cart.getContent(cart.getRowCount() - 1, 0).replace("Sub Total: $", "");
			return amount;
		}
		
		public SignIn checkout() throws Exception {
			proceed.click();
			return new SignIn(); 
		}
		
		public PaymentDetails checkoutSignedIn() throws Exception {
			proceed.click();
			return new PaymentDetails(); 
		} 
	}
	



### Write a test ###
TBD
#### TestNG file ####

### Write a cucumber test ###
TBD
#### TestNG file ####

### Configure test scripts ###
There are several ways to make values in test script change according to test environment, executed test, ...

#### XML configuration ####
XML testing file handles many technical configurations: server address, used tools and related configuration.
 
Business configuration can be done through the “unknown” parameters. These are parameters which are not known from the framework. They are added to a list of business parameters.

#### Confg.ini configuration ####
XML configurations are done statically and must be duplicated through all the test suites (or using “testConfiguration” parameter). It’s not possible to have a centralized configuration which depends on test environment.

*Example:* the server URL depends on testing phase. They are not the same in production and in integration phase.

That’s why the “config.ini” file is made for. Each tested application can embed a config.ini file whose format is: 

![](images/config_ini_example.png)
 
“General“ section is for common configuration (e.g: a database user name which does not depends on environment) and other sections are specific to named test environments. Here, we define a “Dev” environment. Then, when launching test, user MUST define the environment on which test will be run with the option `-Denv=Dev`

Keys defined in environment sections override the ones in “General” section.
This file must be located in "<<t>application root>/data/<<t>application name>/config" folder.

These configurations are also stored in the business configuration.

#### Using configurations (aka business configuration) in test scripts ####
Each webpage can use the configurations defined above using (getting variable “text” from configuration):

![](images/get_param_example.png)

### Test script configuration mapping ###
#### Mapping files utility ####

Mapping file give possibility to call an element in a web page with a more accessible and understandable word. This way your code gain clarity for a non technical user.
Every element can be redefined for any platform and version, you just have to create a hierarchical architecture of files :
example :

![](images/mapping_architecture.png)

#### objectMapping.ini configuration ####

Each file can define new elements, and it inherits parents files.
The structure of an objectMapping.ini file looks like that :

![](images/objectMapping_example.png)

between [ ] you define the web page where to use the following definitions. Next, there is the word definitions : caller_word:technical_word

#### Mapping data use ####

In the corresponding pageObject you can use mapping words to search elements using : locateBy(map:caller_word) or by.(map:caller_word). It will search the element in the page which is defined by the technical word. 

## Run tests ##

### Configurations ###
Below is the list of all parameters accepted in testing xml file.
**TBD**

### Test with Appium locally ###
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
    
    	<parameter name="app" value="C:\Users\behe\Documents\Tests\Tests mobiles\infolidays-1.0.0-release.apk"/>
    	<parameter name="appPackage" value="com.infotel.mobile.infolidays"/>
    	<parameter name="appActivity" value="com.infotel.mobile.mesconges.view.activity.StartActivity"/>
    	<parameter name="newCommandTimeout" value="120"/>
    
    	<packages>
    		<package name="com.seleniumtests.core.runner.*"/>
    	</packages>
    </test>


deviceName reflects the local device used to automate the test

### Test with SauceLabs ###
TBD

### Test with Testdroid ###
TBD

## Development ##

### Import project ###
eclipse project file is provided to avoid eclipse project configuration
Import this project into eclipse.

In case eclipse does not weave aspects (.aj files do not have the 'A' icon), check in build path parameters that **/*.aj files are included to "sources on build path)

### Build ###
Create build (for core and test apps):

	mvn clean package

Build creates a file named seleniumBot-core.zip. 

### Test ###
SeleniumRobot contains Unit tests, executed through the maven "test" phase. They tend to be quick and do not use any browser. They are configured using the `data/core/testng/tu.xml` file.

Integration tests are executed in the maven "verify" phase. They are configured using the `data/core/testng/ti.xml` file. These tests can start browser to check specific features.

### Deploy ###
Unzip this file to any folder.

Also unzip the test app and you should get this folder structure:

![](images/folder_structure.png)

### Release code ###
When a SNAPSHOT version is ready to be released, use

    mvn release:prepare release:perform

Check installation requirements to perform a release


## Interfacing with tests managers ##
### Squash TM/TA ###
STF can work with Squash TA by using an intermediate .java file. This file handles:

- Execution of test framework using a command line
- Generation of .ta files
- Update of test list

#### Execution of test framework ####
This needs some environment variable configuration:

- Add a `STF_HOME` environment variable pointing to the folder where STF is deployed
- Add a `JAVA_HOME_STF` environment variable pointing to a Java 8 installation. This is mandatory as for now (Squash TA 1.9), Squash only supports Java 7 whereas STF is built using Java 8.

**Execution platform must be installed with Java 8**

A typical command line would be:


    %JAVA_HOME_STF%/bin/java -cp %STF_HOME%/seleniumtestsframework.jar;%STF_HOME%/plugins/${application}-tests.jar -Dbrowser=${TC_CUF_browser} -DcucumberTests=\"${TC_CUF_cucumberTest}\" -DcucumberTags=${TC_CUF_cucumberTags} -Dvariables=${TC_CUF_testVariables} -Denv=${TC_CUF_testEnvironment} org.testng.TestNG ${testngFile} -testnames ${testngName}
Each ${} is a variable passed by the .ta script when replacing variables by their actual value.
#### Squash TM configuration ####
Launching of STF expects some variables passed to the script. These MUST be declared on Squash TM side (as Test Case custom fields or Test Execution custom field):

- browser
- testEnvironment

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
![](/images/ta_generation.png)

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

