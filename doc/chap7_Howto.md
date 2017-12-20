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
	
Oracle DB needs `tnsnamePath` variable: path to folder where tnsnames.ora file
	
### 9 Using emails ###

SeleniumRobot provides several email clients to allow reading email content and attachments

	EmailAccount account = EmailAccount(<email_address>, <login>, <password>);
	Email emailFound = account.checkEmailPresence(<email_title>, new String[] {"attachment1"});
	
Email needs `mailServer` variable which is the URL to the server
	
### 10 upload file ###

This should be avoided as much as possible, but some tests may require uploading a file to the tested application.
With selenium, click on the button to upload the file, then you can call `uploadFile(<filePath>)` which will handle the modal opened by browser.
The drawbak of this method is that browser MUST have the focus and thus no other test should be executed at the same time.