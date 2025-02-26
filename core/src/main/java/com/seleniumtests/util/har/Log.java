package com.seleniumtests.util.har;

import java.util.ArrayList;
import java.util.List;

public class Log {
    private String version;
    private Creator creator;

    private List<Entry> entries;
    private List<Page> pages;

    public Log(String version, Creator creator) {
        this.version = version;
        this.creator = creator;
        this.entries = new ArrayList<>();
        this.pages = new ArrayList<>();
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public String getVersion() {
        return version;
    }

    public Creator getCreator() {
        return creator;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public List<Page> getPages() {
        return pages;
    }
}
