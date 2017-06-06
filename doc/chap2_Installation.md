### 1 Development environment ###

#### eclipse ####
SeleniumRobot is developped using eclipse IDE. Following plugins are mandatory:

- aspectj plugin (AJDT): use the dev version are older versions are not compatible with recent eclipse versions
- m2e plugin (maven)
- subeclipse (if using SVN for test application)
- TestNG (execute testNG tests)

SeleniumRobot needs Java 8 to be compiled.

It has also been tested with IntelliJ, however this requires the AspectJ compiler plugin which is not available in community version. A solution may be to compile seleniumRobot using maven.

Sometimes, eclipse does not get the same environment variables than the system. This can block mobile testing when it search for `node` installed on system. Simply add the path to node installation to `PATH` environment variable (e.g: `$PATH:/usr/local/bin`)

#### Sign artifact for deploy phase ####
OSS needs artifacts to be signed before being deployed
Therefore, generated a GPG key (it will ask a name and a password)

	gpg --gen-key

Copy key to a public key server

	gpg2 --keyserver hkp://pgp.mit.edu --send-key <key_name>

#### Git key for release ####
In order for maven to push tags on release, a key must be generated for SSH connection

    ssh-keygen -t rsa -C '<key name>�
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

### 2 Execution environment ###
Execution environment needs at least Java 8. SeleniumRobot is compatible with Windows, Mac OS and Linux.
Depending on your tests, you should consider install:

- A browser 
- Appium
- Android SDK / Genymotion to test on android simulator / emulator
- XCode (Mac OS X) to test on iPhone Simulator 

#### Installing Appium ####
Either install it with .dmg on Mac or .exe on Windows or through npm `npm install -g appium`
Set `APPIUM_HOME` environment variable to point to path where appium has been installed:
- On Windows, using .exe, it's the root path where Appium.exe is located
- On Mac, using .dmg, it will be `/Applications/Appium.app/Contents/Resources/app`
- On any platform using npm installation, it will be the path where root `node_modules` folder has been created. This folder should contain an `appium` subfolder

On Linux/Mac systems, you can add `export APPIUM_HOME=<path to appium>` to the `.bash_profile` file in home directory

When running seleniumRobot from eclipse, it may not inherit the user environment variables, so set it in Run Configuration

#### Configuring Android for tests ####
- Install android SDK (the zip/command line tools version is enough) : [https://developer.android.com/studio/index.html#downloads](https://developer.android.com/studio/index.html#downloads)
- Install Intel HAXM driver to allow Virtual machine speedup. This can also be installed through Android SDK Manager
- Open SDK Manager and select the android images corresponding to the versions you wish to use (select only x86 Atom 64 bits). Install the components
- In AVD Manager, create a virtual machine (activate graphic acceleration) and start it. You should be able to use your Android virtual device.
- Add `ANDROID_HOME` environment variable pointing to root of android tools. This is the directory where AVDManager is copied

#### Configuring iOS for tests ####
Follow appium instruction here [http://appium.io/slate/en/master/?ruby#running-appium-on-mac-os-x] (http://appium.io/slate/en/master/?ruby#running-appium-on-mac-os-x)
As of June 2017, it asks for (simulator and real device):
- install xcode
- install xcode-select
- `npm install -g authorize-ios`
- `sudo authorize-ios`
- install homebrew (see homebrew website)
- `brew install carthage`

Check installation with appium-doctor:
- `npm install appium-doctor`
- in node_modules/appium-doctor, `node . --ios`

For use with a real iOS device, follow instructions here: [http://appium.io/slate/en/master/?ruby#appium-on-real-ios-devices] (http://appium.io/slate/en/master/?ruby#appium-on-real-ios-devices)
For real device only:
- `brew install libimobiledevice`
- `npm install -g ios-deploy`

TODO: 
https://discuss.appium.io/t/xcodebuild-failed-with-code-65-warning-the-server-did-not-provide-any-stacktrace-information-command-duration-or-timeout-32-63-seconds/12756/4
https://github.com/appium/appium/issues/7747
 