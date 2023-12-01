package com.seleniumtests.util.osutility;

public class SystemUtility {

    /**
     * Wraps System class so that we can mock it
     * @param name
     * @return
     */
    public static String getenv(String name) {
        return System.getenv(name);
    }
}
