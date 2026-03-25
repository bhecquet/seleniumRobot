package com.seleniumtests.util.osutility;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SystemUtility {
    private SystemUtility() {
        /* This utility class should not be instantiated */
    }

    private static final Map<String, String> customEnv = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Wraps System class so that we can mock it
     * @param name
     */
    public static String getenv(String name) {
        String value = System.getenv(name);
        if (value != null) {
            return value;
        } else {
            return customEnv.get(name);
        }
    }

    public static String setenv(String name, String value) {
        return customEnv.put(name, value);
    }

    public static void clear(String name) {
        customEnv.remove(name);
    }
}
