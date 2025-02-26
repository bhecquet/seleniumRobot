package com.seleniumtests.util.har;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private String startedDateTime;

    private String id;
    private String title;
    private List<PageTiming> pageTimings = new ArrayList<>();

    public Page(String startedDateTime, String id, String title) {
        this.startedDateTime = startedDateTime;
        this.id = id;
        this.title = title;
    }


    public String getId() {
        return id;
    }

    public String getStartedDateTime() {
        return startedDateTime;
    }

    public String getTitle() {
        return title;
    }

    public List<PageTiming> getPageTimings() {
        return pageTimings;
    }
}
