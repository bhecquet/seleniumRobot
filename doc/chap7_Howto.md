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

SeleniumRobot exposes 2 elements to work with images

- `ImageElement` is used to handle HTML images `<img src=" ... `
- `PictureElement` is used to find arbitrary images in web page or application and interact with it (click, sendKeys). The later point is only available locally on web browsers

Behind the scene, PictureElement takes a screenshot, and then uses openCV to search the picture inside the screenshot. Rotation and resizing are supported.
The `intoElement` parameters helps scrolling in page. It must be an element right above the picture to search, or the element enclosing this picture. If not specified, no scrolling will be done

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
	Email emailFound = account.checkEmailPresence(<email_title>, new String[] {"attachment1"});
	
emailServer is an object created by 

	EmailServer server = new EmailServer("<mail_server_urs>", EmailServerTypes.EXCHANGE_EWS, "<domain_for_user>");
	
### 10 upload file ###

This should be avoided as much as possible, but some tests may require uploading a file to the tested application.

#### The selenium way (preferred) ####
This is the most reliable way as it's provided by selenium driver.

Conditions are:
- you have an `<input type="file" id="uploadFile" />` element
- this element is visible (not hidden by an other one)

Then, you can do either:
- `driver.findElement(By.id("uploadFile")).sendKeys(<some file path>);`
- `new FileUploadElement("upload", By.id("uploadFile")).sendKeys(<some file path);`

Selenium will do the rest for you, locally or in grid mode

#### The selenium robot way (if previous method is not possible) ####

Inside your PageObject:
- With selenium, click on the button to upload the file
- then you can call `uploadFile(<filePath>)` which will handle the modal opened by browser.

The drawbak of this method is that browser MUST have the focus and thus no other test should be executed at the same time because we are sending keyboard actions outside of selenium

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

