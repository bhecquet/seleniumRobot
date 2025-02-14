package com.seleniumtests.util.har;

public class Entry {
    private String startedDateTime;
    private Request request;
    private Response response;
    private Timing timings;
    private int time;

    public Entry(String startedDateTime, Request request, Response response, Timing timings, int time) {
        this.startedDateTime = startedDateTime;
        this.request = request;
        this.response = response;
        this.timings = timings;
        this.time = time;

    }
}
