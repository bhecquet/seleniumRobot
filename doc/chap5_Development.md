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

### 4 Deploy ###
Unzip this file to any folder.

Also unzip the test app and you should get this folder structure:

![](images/folder_structure.png)

### 5 Release code ###
When a SNAPSHOT version is ready to be released, use

    mvn release:prepare release:perform

Check installation requirements to perform a release