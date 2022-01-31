package com.seleniumtests.ut.util.helper;

import com.seleniumtests.GenericTest;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.helper.IniHelper;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class TestIniHelper extends GenericTest {

    @Test(groups = {"ut"})
    public void testReadIniFile() throws IOException {
        Map<String, Map<String, String>> hashMapToComplete = new HashMap<>();
        File envIni = createFileFromResource("tu/env.ini");
        Map<String, Map<String, String>> hashMapComplete = IniHelper.readIniFile(envIni, hashMapToComplete);
        assertTrue(hashMapComplete.containsKey("DEV"));
    }

    @Test(groups = {"ut"})
    public void testFileAlreadyInside() throws IOException {
        Map<String, Map<String, String>> hashMapWithOne = new HashMap<>();
        Map<String, String> mapKeyValue = new HashMap<>();
        mapKeyValue.put("key1", "value1");
        hashMapWithOne.put("General", mapKeyValue);
        File envIni = createFileFromResource("tu/env.ini");
        Map<String, Map<String, String>> hashMapComplete = IniHelper.readIniFile(envIni, hashMapWithOne);
        assertTrue(hashMapComplete.containsKey("General"));
    }

    @Test(groups = {"ut"}, expectedExceptions = ConfigurationException.class)
    public void testFileNull() {
        Map<String, Map<String, String>> hashMapToComplete = new HashMap<>();
        Map<String, Map<String, String>> hashMapComplete = IniHelper.readIniFile(null, hashMapToComplete);
        assertNull(hashMapComplete);
    }

    @Test(groups = {"ut"}, expectedExceptions = ConfigurationException.class)
    public void testBadFile() throws IOException {
        Map<String, Map<String, String>> hashMapToComplete = new HashMap<>();
        File envIni = new File("tu/meduse.mer");
        Map<String, Map<String, String>> hashMapComplete = IniHelper.readIniFile(envIni, hashMapToComplete);
    }
}
