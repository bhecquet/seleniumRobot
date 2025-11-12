package com.seleniumtests.util.har;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Har {
    private final Log log;

    public Har() {
        log = new Log("1.2", new Creator());
    }

    public Log getLog() {
        return log;
    }

    public void writeTo(File file) throws IOException {
        FileUtils.write(file, new kong.unirest.core.json.JSONObject(this).toString(), StandardCharsets.UTF_8);
    }
}
