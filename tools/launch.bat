CD /d "%~dp0"

rem example: java -cp seleniumRobot.jar;plugins\googleTest-tests.jar org.testng.TestNG data\googleTest\testng\testng_google2.xml -testnames tnr
java -cp seleniumRobot.jar;plugins\%app%-tests.jar org.testng.TestNG %* 
EXIT /B %ERRORLEVEL%