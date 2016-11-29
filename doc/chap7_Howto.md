In this section, we will describe how to add some useful features to test applications (file comparison, log reading, ...)

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