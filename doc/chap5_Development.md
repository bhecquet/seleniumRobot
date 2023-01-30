<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [1 Import project](#1-import-project)
  - [Install requirements for angular test application](#install-requirements-for-angular-test-application)
- [2 Build](#2-build)
- [3 Test](#3-test)
- [4 Deploy](#4-deploy)
- [5 Release code](#5-release-code)
- [6 Development considerations](#6-development-considerations)
  - [Aspect handling](#aspect-handling)
    - [aspect and dynamic compilation](#aspect-and-dynamic-compilation)
  - [Test context](#test-context)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

### 1 Import project ###
eclipse project file is provided to avoid eclipse project configuration
Import this project into eclipse.

In case eclipse does not weave aspects (.aj files do not have the 'A' icon), check in build path parameters that **/*.aj files are included to "sources on build path)

#### Install requirements for angular test application ####

Integration tests embed an angular test application (version 5) for testing interactions with angular-material [https://material.angular.io/](https://material.angular.io/)
To modify this application, do the following

- get source code at: [https://github.com/bhecquet/seleniumRobot-angular-demoapp](https://github.com/bhecquet/seleniumRobot-angular-demoapp)
- install node (>= 6.11.5) + npm ( >= 3.10.10)
- install angular-cli (1.5.0 for angular 5) [https://cli.angular.io/](https://cli.angular.io/): `npm install -g @angular/cli@1.5.0`
- `cd <dir_where_app_is_cloned>`
- install dependencies: `npm install`: this will read packages.json and fetch the required dependencies
- go to `src/app/` and modify html. Doc for material components can be found here [https://material.angular.io/components/categories](https://material.angular.io/components/categories)

Test your modifications with `ng serve`
Build the updated application with `ng build --prod --output-hashing=none --output-path=angularApp --base-href=.`
Replace the built application into seleniumRobot resources


### 2 Build ###
Create build (for core and test apps):

	mvn clean package

Build creates a file named seleniumBot-core.zip. 

### 3 Test ###
SeleniumRobot contains Unit tests, executed through the maven "test" phase. They tend to be quick and do not use any browser. They are configured using the `data/core/testng/tu.xml` file.

Integration tests are executed in the maven "verify" phase. They are configured using the `data/core/testng/ti.xml` file. These tests can start browser to check specific features.
The integration tests can be customized with several maven-failsafe-plugin option plus seleniumRobot specific options

- do execute only integration tests: use `-DskipUTs=true`
- execute specific integration tests: use `-Dit.test=<test lists>`. Refer to failsafe plugin documentation for details
- debug maven integration tests: use `-Dmaven.failsafe.debug=true`. Refer to failsafe plugin documentation for details
- display / do not display driver logs (they can be quite big). This corresponds to the d option of seleniumRobot: `-DseleniumRobot.debug=driver` or `-DseleniumRobot.debug=none`

**Test in Internet Explorer**: Beware to unckeck "Display intranet sites in compatibility mode". Else, integration tests with Internet Explorer will fail

In case surefire plugin crashes without information, add `<forkCount>0</forkCount>` to its configuration which will help showing the real problem

### 4 Deploy ###
Unzip this file to any folder.

Also unzip the test app and you should get this folder structure:

![](images/folder_structure.png)

### 5 Release code ###
When a SNAPSHOT version is ready to be released, use

    mvn release:prepare release:perform

Check installation requirements to perform a release

### 6 Development considerations ###

#### Aspect handling ####

SeleniumRobot uses AspectJ extensively to avoid writing too many times the same code. Aspects allow
- logging called methods
- replay actions (for standard and composite actions)
- display selenium actions and steps to report
- transform standard selenium actions to seleniumRobot actions (with replay)

This is directly handled by maven during build (and by your favourite IDE with appropriate plugins)

for debugging aspects inside eclipse, go to 'Window' -> 'Preferences' -> 'AspectJ compiler' and tick 'no inline'

##### aspect and dynamic compilation #####

For Selenium IDE execution feature, the generated code had to be compiled and woven with seleniumRobot aspects so that no feature are lost. For this to work, 
- java program MUST be launched with `-javaagent:/path/to/aspectjweaver.jar`
- META-INF/aop.xml file MUST be created in resources, detailing aspects to weave. At least one aspect must be present, else, weaver is deactivated 
- weaver tracing can be enabled with options `-Dorg.aspectj.weaving.messages=true -Dorg.aspectj.tracing.debug=true -Dorg.aspectj.tracing.enabled=true -Dorg.aspectj.tracing.messages=true -Djava.util.logging.config.file=/path/to/logging.properties`
- logging.properties file contains (see https://www.eclipse.org/aspectj/doc/released/pdguide/trace.html)

```properties
	handlers= java.util.logging.FileHandler

	.level= FINER
	
	java.util.logging.FileHandler.pattern = %h/java%u.log
	java.util.logging.FileHandler.count = 1
	java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
	java.util.logging.FileHandler.level = FINER
	
	org.aspectj.weaver.loadtime.level = FINER
```

doc: 
- https://stackoverflow.com/questions/21544446/how-do-you-dynamically-compile-and-load-external-java-classes
- https://www.eclipse.org/aspectj/doc/released/pdguide/trace.html
- https://stackoverflow.com/questions/10733247/aspectj-weaving-with-custom-classloader-at-runtime
- https://stackoverflow.com/questions/16777015/can-weavingurlclassloader-only-weave-aspects-of-local-jars
- https://www.baeldung.com/aspectj

#### Test context ####

SeleniumRobot uses a context for each test method, storing information about the parameters of the test, dataset etc ...
Everything is done inside "SeleniumTestContext" and classes from core/context package
The rules are the following:
- if a parameter is a launch option (i.e: browser, proxy, ...), so, common to all tests, it's stored in `contextDataMap` and documentation references this launch option
- if a parameter is different from test to test (bugtracker reporter, testId, url, ...), it's called a variable and can be provided through env.ini, seleniumRobot variable server or command line. It will be accessible through `getConfiguration` method