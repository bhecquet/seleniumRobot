package com.seleniumtests.browserfactory.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.Platform;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

/**
 * Class for managing iOS devices
 * @author behe
 *
 */
public class InstrumentsWrapper {
	
	// examples
	// Apple TV 1080p (10.2) [6444F65D-DA15-4505-8307-4520FD346ACE] (Simulator)
	// Mac mini [CBFA063D-2535-5FD8-BA05-CE8D3683D6BA]
	private static final Pattern REG_DEVICE = Pattern.compile("([a-zA-Z0-9 ]+).*?\\((\\d+\\.\\d+\\.?\\d?)\\).*?\\[(.*?)\\].*");
		

	public InstrumentsWrapper() {
		checkInstallation();
	}
	
	private void checkInstallation() {
		if (OSUtility.getCurrentPlatorm() != Platform.MAC) {
			throw new ConfigurationException("InstrumentsWrapper can only be used on Mac");
		}
		
		String out = OSCommand.executeCommandAndWait("instruments");
		if (!out.contains("instruments, version")) {
			throw new ConfigurationException("Instruments is not installed");
		}
	}
	
	/**
	 * Use 'instruments -s devices' to know which devices are available for test
	 */
	public List<MobileDevice> parseIosDevices() {
		String output = OSCommand.executeCommandAndWait("instruments -s devices");
		List<MobileDevice> devList = new ArrayList<>();
		
		for (String line: output.split("\n")) {
			line = line.trim();
			Matcher matcher = REG_DEVICE.matcher(line);

			if (matcher.matches()) {
				MobileDevice dev = new MobileDevice(matcher.group(1).trim(), 
						matcher.group(3), 
						"iOS", 
						matcher.group(2), 
						Arrays.asList(new BrowserInfo[] {new BrowserInfo(BrowserType.SAFARI, "latest", null)}));
				devList.add(dev);
			}
		}
		return devList;
	}
}
