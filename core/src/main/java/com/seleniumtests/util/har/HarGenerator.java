package com.seleniumtests.util.har;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HarGenerator {

    JSONObject har;
    List<Entry> entries;

    public HarGenerator() {
        har = new JSONObject();
        entries = new ArrayList<>();
        har.put("log", new JSONObject(Map.of("version", "1.2", "creator", Map.of("name", "seleniumRobot", "version", "latest"))));
    }

    public void generate(File harFile) {

    }
}
