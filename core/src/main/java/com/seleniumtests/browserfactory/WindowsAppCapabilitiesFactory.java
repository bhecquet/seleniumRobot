package com.seleniumtests.browserfactory;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.osutility.OSCommand;
import org.openqa.selenium.MutableCapabilities;

import java.util.Map;

public class WindowsAppCapabilitiesFactory extends ICapabilitiesFactory {

	 private static final int HANDLE_RETRIES = 5;
	 private static final long HANDLE_RETRY_DELAY_MS = 1000;

	
    public WindowsAppCapabilitiesFactory(DriverConfig webDriverConfig) {
        super(webDriverConfig);
    }

    @Override
    public MutableCapabilities createCapabilities() {
        MutableCapabilities capabilities = new MutableCapabilities((Map.of("platformName", "windows",
                "appium:automationName", "FlaUI")));

        // path to the app that will be started
        if (webDriverConfig.getApp() != null && !webDriverConfig.getApp().isEmpty()) {
            capabilities.setCapability("appium:app", webDriverConfig.getApp());

        // name of the application window
        } else if (webDriverConfig.getAppActivity() != null && !webDriverConfig.getAppActivity().isEmpty()) {
            capabilities.setCapability("appium:appTopLevelWindow", webDriverConfig.getAppActivity());            capabilities.setCapability("appium:appTopLevelWindow", resolveTopLevelWindowHandle(webDriverConfig.getAppActivity()));
        } else {
            throw new ConfigurationException("Either 'app' or 'appActivity' must be provided");
        }

        return capabilities;
    }

    private String resolveTopLevelWindowHandle(String appActivity) {
        if (isHandle(appActivity)) {
            return appActivity;
        }

        String escapedRegex = appActivity.replace("'", "''");
        for (int attempt = 1; attempt <= HANDLE_RETRIES; attempt++) {
            String command = String.format("$wins=Get-Process -ErrorAction SilentlyContinue | Where-Object {$_.MainWindowHandle -ne 0}; "
                    + "if($wins){$m=$wins | Where-Object {$_.MainWindowTitle -match '%s'} | Sort-Object StartTime -Descending | Select-Object -First 1; "
                    + "if($m){$m.MainWindowHandle}}", escapedRegex);
            String output = OSCommand.executeCommandAndWait(new String[]{"powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-Command", command});
            String handle = extractHandle(output);
            if (handle != null) {
                return handle;
            }

            if (attempt < HANDLE_RETRIES) {
                sleep(HANDLE_RETRY_DELAY_MS);
            }
        }

        throw new ConfigurationException(String.format("Unable to resolve top-level window handle for pattern: %s", appActivity));
    }

    private String extractHandle(String output) {
        if (output == null || output.isBlank()) {
            return null;
        }

        String[] lines = output.split("\\R");
        for (int index = lines.length - 1; index >= 0; index--) {
            String candidate = lines[index].trim();
            if (isHandle(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isHandle(String value) {
        return value != null && (value.matches("0x[0-9A-Fa-f]+") || value.matches("\\d+"));
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConfigurationException("Interrupted while resolving top-level window handle");
        }
    }
}
