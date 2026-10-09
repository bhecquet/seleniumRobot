package com.seleniumtests.util;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MobileUtility {

    public static String getPlatformName() {
        WebDriver driver = WebUIDriver.getWebDriver(false);
        if (driver == null) {
            throw new IllegalStateException("MobilePlatformUtils has not capabilities");
        }
        Object platformName = ((HasCapabilities) driver).getCapabilities().getCapability("platformName");
        if (platformName == null) {
            throw new IllegalStateException("PlatformName is empty");
        }
        return platformName.toString();
    }

    public static boolean isAndroid() {
        return "Android".equalsIgnoreCase(getPlatformName());
    }

    public static boolean isIOS() {
        return "iOS".equalsIgnoreCase(getPlatformName());
    }


    public void clickAlertButton(WebDriver driver, String buttonLabel) {
        Map<String, Object> args = new HashMap<>();
        args.put("action", "accept");
        args.put("buttonLabel", buttonLabel);
        CustomEventFiringWebDriver cefwd = new CustomEventFiringWebDriver(driver);
        cefwd.executeScript("mobile: alert", args);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAlertButtons(WebDriver driver) {
        CustomEventFiringWebDriver cefwd = new CustomEventFiringWebDriver(driver);
        Object result = cefwd.executeScript("mobile: alert",
                Map.of("action", "getButtons"));
        return (List<String>) result;
    }

    private static void enrollFaceId() {
        getIosDriver().executeScript("mobile: enrollBiometric", Map.of("isEnable", true));
    }

    private static CustomEventFiringWebDriver getIosDriver() {
        WebDriver driver = WebUIDriver.getWebDriver(false);
        if (!MobileUtility.isIOS()) {
            throw new IllegalStateException("Actual driver isn't an iOS driver");
        }
        return (CustomEventFiringWebDriver) driver;
    }

    public void faceIdSuccess(WebDriver driver) {
        CustomEventFiringWebDriver cefwd = new CustomEventFiringWebDriver(driver);
        cefwd.executeScript("mobile: sendBiometricMatch",
                Map.of("type", "faceId", "match", true));
    }

    public void faceIdFailure(WebDriver driver) {
        CustomEventFiringWebDriver cefwd = new CustomEventFiringWebDriver(driver);
        cefwd.executeScript("mobile: sendBiometricMatch",
                Map.of("type", "faceId", "match", false));
    }

    private static void simulateFingerprint(String deviceId, String fingerprintId) {
        if (isEmulator(deviceId)) {
            throw new IllegalStateException("Fingerprint simulation is only available on an Android emulator. Current device: " + deviceId);
        }
        try {
            Process process = new ProcessBuilder("adb", "-s", deviceId, "emu", "finger", "touch", fingerprintId)
                    .redirectErrorStream(true).inheritIO().start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Fingerprint simulation failed. Code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Fingerprint simulation interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Unable to simulate fingerprint: " + fingerprintId, e);
        }
    }

    private void _selectFirstIOSPhoto(WebDriver driver) {
        List<WebElement> photos = driver.findElements(AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeImage' " + "AND label BEGINSWITH 'Photo,'"));
        if (photos.isEmpty()) {
            throw new NoSuchElementException("No picture found in the Picker iOS");
        }
        WebElement firstPhoto = photos.getFirst();
        Rectangle rect = firstPhoto.getRect();
        int x = rect.getX() + rect.getWidth() / 2;
        int y = rect.getY() + rect.getHeight() / 2;
        CustomEventFiringWebDriver cefwd = new CustomEventFiringWebDriver(driver);
        cefwd.executeScript("mobile: tap", Map.of("x", x, "y", y));
    }

    //TOCHECK
    private static boolean isEmulator(String deviceId) {
        try {
            Process process = new ProcessBuilder("adb", "-s", deviceId, "shell", "getprop", "ro.build.characteristics")
                    .redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Unable to determine if the device is an emulator. Code: " + exitCode);
            }
            return output.contains("emulator");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Emulator check interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Unable to check if the device is an emulator: " + deviceId, e);
        }
    }

    public static void restartApplication(String appId) {
        AppiumDriver driver = (AppiumDriver) WebUIDriver.getNativeWebDriver();
        assert driver != null;
        ((InteractsWithApps) driver).terminateApp(appId);
        ((InteractsWithApps) driver).activateApp(appId);
    }

    public static void prepareTestFiles(String platform, String deviceId, String dcimPath, String bundleId) {
        Path sourceDirectory = Path.of(SeleniumTestsContextManager.getApplicationDataPath(), "files");
        if (!Files.exists(sourceDirectory)) {
            throw new RuntimeException("The test files folder does not exist: " + sourceDirectory);
        }
        String normalizedPlatform = platform.toLowerCase(Locale.ROOT);
        if (normalizedPlatform.startsWith("android")) {
            prepareAndroidFiles(deviceId, sourceDirectory, dcimPath);
        } else if (normalizedPlatform.startsWith("ios")) {
            prepareIosFiles(deviceId, bundleId, sourceDirectory);
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }
    }

    private static void prepareAndroidFiles(String deviceId, Path sourceDirectory, String androidDcimPath) {
        try {
            execute("adb", "-s", deviceId, "shell", "mkdir", "-p", androidDcimPath);
            try (var files = Files.list(sourceDirectory)) {
                files.filter(Files::isRegularFile)
                        .filter(file -> !file.getFileName().toString().startsWith("."))
                        .forEach(file -> pushAndroidFile(deviceId, file, androidDcimPath));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while preparing Android files", e);
        }
    }

    private static void pushAndroidFile(String deviceId, Path file, String androidDcimPath) {
        String fileName = file.getFileName().toString();
        execute("adb", "-s", deviceId, "shell", "rm", "-f", androidDcimPath + fileName);
        execute("adb", "-s", deviceId, "push", file.toAbsolutePath().toString(), androidDcimPath); // send the file to DCIM
        execute("adb", "-s", deviceId, "shell", "am", "broadcast", "-a",
                "android.intent.action.MEDIA_SCANNER_SCAN_FILE", "-d",
                "file://" + androidDcimPath + fileName);
    }

    private static void prepareIosFiles(String deviceId, String bundleId, Path sourceDirectory) {
        Path documentsDirectory = getDocumentsDirectory(deviceId, bundleId);
        try {
            Files.createDirectories(documentsDirectory);
            importMissingIosImages(deviceId, sourceDirectory); // Images -> Pictures app
            copyIosDocuments(sourceDirectory, documentsDirectory); // Documents -> App's Documents folder
        } catch (IOException e) {
            throw new RuntimeException("Error while preparing iOS files", e);
        }
    }

    private static void importMissingIosImages(String deviceId, Path sourceDirectory) throws IOException {
        Set<String> existingPhotoNames = getExistingIosPhotoNames(deviceId);
        try (var files = Files.list(sourceDirectory)) {
            files.filter(Files::isRegularFile).filter(file -> !file.getFileName().toString().startsWith("."))
                    .filter(MobileUtility::isImage)
                    .filter(file -> {
                        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
                        return !existingPhotoNames.contains(fileName);
                    }).forEach(file -> {
                        execute("xcrun", "simctl", "addmedia", deviceId, file.toAbsolutePath().toString());
                    });
        }
    }

    private static Set<String> getExistingIosPhotoNames(String deviceId) {
        Path photosDatabase = Path.of(System.getProperty("user.home"), "Library", "Developer", "CoreSimulator", "Devices", deviceId, "data", "Media", "PhotoData", "Photos.sqlite");
        if (!Files.exists(photosDatabase)) {
            return Set.of();
        }
        String query = """
                SELECT attributes.ZORIGINALFILENAME
                FROM ZADDITIONALASSETATTRIBUTES attributes
                JOIN ZASSET asset
                ON attributes.ZASSET = asset.Z_PK
                WHERE attributes.ZORIGINALFILENAME IS NOT NULL
                AND COALESCE(asset.ZTRASHEDSTATE, 0) = 0;
                """;
        try {
            String output = executeAndGetOutput("sqlite3", "-readonly", photosDatabase.toString(), query);
            return output.lines().map(String::trim).filter(name -> !name.isBlank()).map(name -> name.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        } catch (RuntimeException e) {
            return Set.of();
        }
    }

    private static void copyIosDocuments(Path sourceDirectory, Path documentsDirectory) throws IOException {
        try (var files = Files.list(sourceDirectory)) {
            files.filter(Files::isRegularFile).filter(MobileUtility::isDocument).forEach(file -> {
                Path destination = documentsDirectory.resolve(file.getFileName());
                try {
                    Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                    Files.setLastModifiedTime(destination, FileTime.from(Instant.now()));
                } catch (IOException e) {
                    throw new RuntimeException("Impossible to copy the file: " + file.getFileName(), e);
                }
            });
        }
    }

    private static boolean isImage(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".tiff") || name.endsWith(".heic");
    }

    private static boolean isDocument(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".pdf");
    }

    private static Path getDocumentsDirectory(String deviceId, String bundleId) {
        String dataContainer = executeAndGetOutput("xcrun", "simctl", "get_app_container", deviceId, bundleId, "data").trim();
        Path documentsDirectory = Path.of(dataContainer, "Documents");
        return documentsDirectory;
    }

    private static void execute(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).inheritIO().start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("The command failed: " + String.join(" ", command) + " | code=" + exitCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible to execute: " + String.join(" ", command), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Command interrupted: " + String.join(" ", command), e);
        }
    }

    private static String executeAndGetOutput(String... command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("The command failed: " + String.join(" ", command) + "\n" + output);
            }
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Impossible to execute: " + String.join(" ", command), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Command interrupted: " + String.join(" ", command), e);
        }
    }

}
