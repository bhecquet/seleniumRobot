package com.seleniumtests.util.har;

public class Har {
    private Log log;

    public Har() {
        log = new Log("1.2", new Creator());
    }

    public Log getLog() {
        return log;
    }
}
