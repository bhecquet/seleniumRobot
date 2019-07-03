In this section, we will describe how to add some useful features to test applications (file comparison, log reading, ...)

### 0 Troubleshooting ###

#### Clicking on an element makes a new window display but browser returns to previous one ####

This behaviour is caused by seleniumRobot when doing the following

	testPage.link.click();
	
	// this is the PageObject corresponding to the new window
	DriverSubTestPage subTestPage = new DriverSubTestPage(false);
	
	// go to new opened window
	mainHandle = testPage.selectNewWindow();
	
The call to new PageObject is performing snapshot of the current window. But, driver did not already switched to this window so 
snapshot is taken from the first one.

To resolve, do instead

	testPage.link.click();
	
	// go to new opened window
	mainHandle = testPage.selectNewWindow();
	
	// this is the PageObject corresponding to the new window
	DriverSubTestPage subTestPage = new DriverSubTestPage(false);

### 1 Compare 2 XML files ###
Use the XMLUnit api: https://github.com/xmlunit/user-guide/wiki

Add dependency to pom.xml
	
	<dependency>
		<groupId>org.xmlunit</groupId>
		<artifactId>xmlunit-core</artifactId>
		<version>2.2.1</version>
	</dependency>
	
Use the following java code
	
	Source source = Input.fromStream(getClass().getResourceAsStream("/tu/xmlFileToTest.xml")).build();
    Source source2 = Input.fromStream(getClass().getResourceAsStream("/tu/xmlFileToTest2.xml")).build();
    Diff diff = DiffBuilder.compare(source).withTest(source2).build();
    System.out.println(diff);
    
### 2 Write working unit tests ###
By default, SeleniumTestsContext enables SoftAssertions, so any unit test with assertion failure will not really fail. To prevent this behaviour, subclass all Unit-Test class from 

- `GenericTest` : parent class for all unit tests
- `GenericDriverTest`: parent class for real driver tests. It cleans driver after each test
- `MockitoTest`: parent of all tests using Mockito / PowerMock

depending on test type

If you need to reinit the SeleniumTestContext using `SeleniumTestsContextManager.initThreadContext(testNGCtx)`, call `initThreadContext(testNGCtx)` of one of these classes instead

### 3 Core: making an action on an HTMLElement replay on error ###

By default, actions in HtmlElements are done only once.<br/>
For better reliability, all actions currently implemented in SeleniumRobot are made to retry on error

    @ReplayOnError
    public void click() {
        findElement(true);
        element.click();   
    }
    
/!\ *annotate only direct actions (where no other HtmlElement method, except `findElement` is called)

### 4 Working with images ###

SeleniumRobot exposes 3 elements to work with images

- `ImageElement` is used to handle HTML images `<img src=" ... `
- `PictureElement` is used to find arbitrary images in web page or application and interact with it (click, sendKeys). The later point is only available on web browsers
- `ScreenZone` is used to interact with desktop only. You can write text, send keys (e.g: windows key), clic at coordinate. 

Behind the scene, PictureElement and ScreenZone take a screenshot, and then uses openCV to search the picture inside the screenshot. Rotation and resizing are supported.
The `intoElement` (for PictureElement only) parameters helps scrolling in page. It must be an element right above the picture to search, or the element enclosing this picture. If not specified, no scrolling will be done and the `<body>` element will be used. This may cause problem if you are in a page where no body is present.
In this case, provide an other top level element. 

**/!\**: resource name is case-sensitive. It may not be obvious in IntelliJ, but with standart execution, if the resource name you provide and its name in src/test/resources is not exactly the same, you will get a `class java.lang.ExceptionInInitializerError: null` or `class java.lang.NoClassDefFoundError: Could not initialize class` when creating the PictureElement

#### Using PictureElement ####

Search for picture in browser, without the need to scroll down and click on it. It will then search for the "body" element. If you search the picture inside a frame, and main document has no "body" tag, then you should specify an element inside frame, or the frame itself

	PictureElement googlePicture = new PictureElement("picture", "tu/googleSearch.png", null);
	googlePicture.click()
	
	
or search for picture (physically located inside src/test/resources/tu/images/logo_text_field.png) in browser, being placed after the table element and click on it

	Table table = new Table("table", By.id("table"));
	PictureElement picture = new PictureElement("picture", "tu/images/logo_text_field.png", table);
	picture.click()
	
or search for picture (physically located inside /data/<application>/images/googleSearch.png)  in browser and click on it. No scrolling will be performed

	PictureElement googlePictureWithFile = new PictureElement("picture", Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "images", "googleSearch.png").toFile(), null);
	googlePictureWithFile.click()


#### Using ScreenZone ####

ScreenZone represents the computer desktop

Search for picture (physically located inside src/test/resources/tu/googleSearch.png) on desktop and click on it

	ScreenZone googleForDesktop = new ScreenZone("picture", "tu/googleSearch.png");
	googleForDesktop.click();
	
or interract with desktop directly without specifying a picture. Thus, click coordinates will be absolute ones

	new ScreenZone("image").clickAt(rectangle.x + 10, rectangle.y + 10);
	
or send text

	ScreenZone firefoxForDesktop = new ScreenZone("picture", "tu/images/logo_text_field.png");
	firefoxForDesktop.sendKeys("hello", 0, 40);
	
or send keys (will write "ab")

	ScreenZone firefoxForDesktop = new ScreenZone("picture", "tu/images/logo_text_field.png");
	firefoxForDesktop.sendKeys(0, 40, KeyEvent.VK_A, KeyEvent.VK_B);
	
#### Check if image is matching ####

`java -cp seleniumRobot.jar com.seleniumtests.util.imaging.ImageDetector <scene_file_path> <object_to_detect_file_path> <threshold=0.1>`

### 5 Working with PDF files ###

Sometimes, it's useful to read PDF files and extract content. Formatting is lost but text remains using the following code

	PDDocument document = PDDocument.load(fichierPdf);
	if (document.isEncrypted()) {
		document.decrypt("");
	}
	document.setAllSecurityToBeRemoved(true);
	PDFTextStripper s = new PDFTextStripper();
	s.getText(document);
			
PDDocument is available through maven dependencies

	<dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId> 
        <version>1.8.9</version>
    </dependency>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcmail-jdk15</artifactId> 
        <version>1.44</version>
    </dependency> 
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk15</artifactId> 
        <version>1.44</version>
    </dependency>
    
### 6 Accessing remote computer through SSH or SCP ###

To retrieve file from remote to local

	Scp scp = new Scp(<sshHost>, <sshUser>, <sshPassword>, null, false);
	scp.connect();  
	scp.transfertFile(new File(<remote file>), new File(<local file>));
	scp.disconnect();
	
To execute command on remte

	Ssh ssh = new Ssh(<sshHost>, <sshUser>, <sshPassword>, null, false);
	ssh.connect();  
	ssh.executeCommand(<my command>);
	ssh.disconnect();
	
### 7 execute requests via SOAP UI ###

When someone already created SOAP UI request to test a service, reuse can be time saving
Either use directly the project file, or change content to adapt it to your data set or environment and execute project string

		SoapUi soapUi = new SoapUi();
		String reply = soapUi.executeWithProjectString(<project_content>, "myProject");
		
### 8 Using database ###

For now, only Oracle database is supported
You must provide the ojdb6.jar file into src/lib folder so that it can be automatically installed in maven local repository when doing `mvn clean`
connection and disconnection are done automatically

	Oracle db = new Oracle(<dbName>, <dbHost>, <dbPort>, <dbUser>, <dbPassword>);
	db.executeParamQuery("SELECT * FROM TAB1 WHERE id=?", id);
	
or

	Oracle db = new Oracle(<dbName>, <dbUser>, <dbPassword>, <tnsNamesPath>);
	db.executeParamQuery("SELECT * FROM TAB1 WHERE id=?", id);
	
### 9 Using emails ###

SeleniumRobot provides several email clients to allow reading email content and attachments

	EmailAccount account = EmailAccount(<email_address>, <login>, <password>, <emailServer>);
	...
	some actions that send an email
	...
	Email emailFound = account.checkEmailPresence(<email_title>, new String[] {"attachment1"});
	
Email title and attachment names can be regular expression as String.matches() is used to search for the right emails
**WARN** create your email server connection before email is sent so that `checkEmailPresence` can look at the last received emails (it keeps an index on already seen mails)
	
emailServer is an object created by 

	EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_EWS, "<domain_for_user>");
	
Using variables, it's also possible to write:

	EmailAccount emailAccount = EmailAccount.fromJson(param("emailAccount"));
	emailAccount.setEmailServer(EmailServer.fromJson(param("emailServer")));
	
where `emailAccount` is `{'email': 'mymail@compmail.com', 'login': 'login', 'password': 'passwd'}` and `emailServer` is `	{'url': 'msg.compmail.com', 'type': 'EXCHANGE_EWS', 'domain': 'compmail.com'}`

	
	
### 10 upload file ###

This should be avoided as much as possible, but some tests may require uploading a file to the tested application.

#### The selenium way (preferred) ####
This is the most reliable way as it's provided by selenium driver.

Conditions are:
- you have an `<input type="file" id="uploadFile" />` element
- this element is visible (not hidden by an other one)

Then, you can do either (the later is advised:
- `driver.findElement(By.id("uploadFile")).sendKeys(<some file path>);`
- `new FileUploadElement("upload", By.id("uploadFile")).sendKeys(<some file path);`

Selenium will do the rest for you, locally or in grid mode

#### The selenium robot way (if previous method is not possible) ####

This may happen if the button / text field on which you click to upload is not an `input` element of type `file`

Inside your PageObject:
- With selenium, click on the button to upload the file
- then you can call `uploadFile(<filePath>)` inside your PageObject which will handle the modal opened by browser.

The drawback of this method is that browser MUST have the focus and thus no other test should be executed at the same time because we are sending keyboard actions outside of selenium

### 11 Write custom reports ###

Through `customTestReports` and `customSummaryReports`, you can add or replace some of the reports SeleniumRobot generates

Option is a comma seperated list of `<prefix>::<extension>::<template_file located in resources>`.
Template file has the Velocity format and must be located in any of the resources seleniumRobot can access (those from test application or from core)

#### available data in test report ####

One file is generated for each test. Name is `<prefix>-<classname>.<methodName>.<extension>`

- `errors`: number of steps in error in test
- `failures`: number of steps in failure in test
- `hostname`: host running the test
- `suiteName`: name of the test method
- `className`: class where method is located
- `tests`: number of steps
- `duration`: overall duration of test
- `time`: start time of the test
- `testSteps`: list of TestStep objects. see javadoc for details	
- `browser`: browser used for test
- `logs`: logs in raw format (content of seleniumRobot.log file)  

#### available data in summary report ####

One file is generated for the overall session. Name is `<prefix>.<extension>`

- `pass`: number of passed tests during session
- `fail`: number of failed tests during session
- `skip`: number of skip tests during session
- `total`: number of tests during session

### 12 Monitor error level ###

If you have a script file (shell / batch / ...), and want to know if test went wrong or not, you can look at error level `echo %errorlevel%` for batch and `$?` for shell

### 13 Access remote servers through HTTPS ###

When accessing remote server, you can use Unirest API which ease sending HTTP requests.
In case access to HTTPS is done inside a dependency, you may encounter problems with certificate chain (e.g: accessing an exchange server through EWS).
You then need to add the remote certificate to a truststore and give it to the JVM (which uses by default it's internal truststore ($JAVA_HOME$/jre/lib/security/cacerts)

To override the default truststore, use `-Djavax.net.ssl.trustStore=<path_to_truststore>`

To debug, use `-Djavax.net.debug=ssl` 

### 14 Inheritance between test applications ###

Originally, a test application corresponds to a full web or mobile application. But if your application is big, or can easily be split into parts that have a few things in common, you can create several test application which will be easier to maintain. E.g: an application with a front-office and a back-office.
You can split a big test application into several ones

	parent-app (generic features)
		|--- child-app1 (app1)
		|--- child-app2 (app2)
		
**Beware**:
- each test application MUST contain all variables of the generic features it uses
- when executing test outside your IDE, classpath MUST contain the test application jar and the parent one: `java -cp seleniumRobot.jar;plugins/<app>-tests.jar;plugins/<parent-app>-tests.jar;lib/drivers/* -D<option1>=<value1> -D<option2>=<value2> org.testng.TestNG <path_to_TestNG_xml_file>"`

pom.xml of each child application will declare the parent-app as dependency, not the core, which is held by parent-app.
	  
### 15 Record network traffic ###

Developpers sometimes use network traffic monitor inside browsers to debug display errors (latency, ...)
For debugging test, this can also be helpful so you can activate network capture by setting the `captureNetwork` parameter to `true` when launching test
Then, an HAR file is recorded.

**INFO**: Network capture is only available with DIRECT and MANUAL proxy settings. Other settings are forbidden because the recording proxy cannot know the address and port of your corporate proxy in automatic mode

**WARN**: If using manual steps with network capture, beware that test steps are only recorded once the driver is created. This means that with the code below, step "Write" will not be displayed in HAR capture. Traffic will still be recorded in the init step (named with the test name). Driver is created with the call to `new DriverTestPage(true)`.
	
	addStep("Write");
	DriverTestPage page = new DriverTestPage(true)
		._writeSomething();
	addStep("Reset");
	page._reset();
	  
### 16 Use Neoload tool to design and record End User Experience ###

Neoload is a tool that performs load testing on application. It also interfaces with Selenium to record test session timing using a real browser
Official documentation is there: [https://www.neotys.com/documents/doc/neoload/latest/en/html/#24373.htm](https://www.neotys.com/documents/doc/neoload/latest/en/html/#24373.htm)

Selenium can be used for 2 purposes: create project details from a browsing session (design mode) and measure browsing session timing (End User Experience)

#### Design mode ####
	  
To enable Design mode, you have to add `-Dnl.selenium.proxy.mode=Design` to the options given to SeleniumRobot. Moreover, add the option `-DneoloadUserPath=<userPath>` so that design mode is active. UserPath is the name of the scenario being recorded

In this mode, driver is configured with a proxy pointing to the neoload API (localhost:8090 by default). Other design API URL can be configured with option `-Dnl.design.api.url=<URL>`. see Configuration options [here](https://www.neotys.com/documents/doc/neoload/latest/en/html/#8278.htm) .
<br/>
So any other configured proxy will be overridden (BrowsermobProxy or manual proxy settings). If you need to go through a proxy for your tests (e.g: a corporate proxy), this proxy needs to be set into Neoload itself.

For test to start, prerequisites are:
- Neoload is started somewhere
- if not local, option `-Dnl.design.api.url=<URL>` is set to the remote neoload instance
- remote neoload instance has a loaded project
- some licences are reserved to neoload instance

#### End User Experience ####

To enable End User Experience, you have to add `-Dnl.selenium.proxy.mode=EndUserExperience` and `-DneoloadUserPath=<userPath>`

Test MUST be started from Neoload itself, either through a java test script action or a command line action. Launching options are the same as for any other test launching. See chap4_Run_tests for details. .
	  
### 17 Add extension to the browser ###

It's possible to add extension to browser provided you have downloaded it and it's located locally, or remotely through http/s server.
Example: `-Dextension0.path=http://localhost:8000/myExt.crx -Dextension0.options="key=value"`

Options are only supported for Firefox.

To download extension file, you can:
- on chrome, use the "Get CRX" extension
- on firefox, go to addons.mozilla.org, search your extension and right click on "add to firefox" button. It will give you the extension path
	  
	  
### 18 Add current date in test-output folder

#### for windows ####

    for /f %%a in ('powershell -Command "Get-Date -format yyyy_MM_dd__HH_mm_ss"') do set datetime=%%a
	
and then, in command line

    -DoutputDirectory=test-output_%datetime%
 	
### 19 Create a custom reporter ###

For textual reports, you can look at §11 which uses Velocity template engine to generate summary and per test reports
For more complex reports (send errors to Jira, send results to Squash, ...), you can write a custom reporter which implements the `IReporter` interface.

This way, you can scan test results and do some actions based on results. Look at `CustomReporter.java` for example
To enable this report, use parameter `-DreporterPluginClasses=my.reporter.CustomClass` 
	  
	  