package com.seleniumtests.util.har;

import java.util.ArrayList;
import java.util.List;

public class Log {
    private String version;
    private Creator creator;
    private List<Entry> entries;

    public Log(String version, Creator creator) {
        this.version = version;
        this.creator = creator;
        this.entries = new ArrayList<>();
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }
}
