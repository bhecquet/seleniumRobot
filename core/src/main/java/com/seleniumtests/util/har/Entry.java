package com.seleniumtests.util.har;

import java.util.HashMap;
import java.util.Map;

public class Entry {
    private final String startedDateTime;
    private final Request request;
    private final Response response;
    private final Timing timings;
    private final int time;
    private final String pageref;
    private final Map<String, String> cache = new HashMap<>();

    public Entry(String pageref, String startedDateTime, Request request, Response response, Timing timings, int time) {
        this.pageref = pageref;
        this.startedDateTime = startedDateTime;
        this.request = request;
        this.response = response;
        this.timings = timings;
        this.time = time;

    }

    public String getStartedDateTime() {
        return startedDateTime;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public Timing getTimings() {
        return timings;
    }

    public int getTime() {
        return time;
    }

    public String getPageref() {
        return pageref;
    }

    public Map<String, String> getCache() {
        return cache;
    }
}
