package com.seleniumtests.util.har;

public class QueryString {


    private final String name;
    private final String value;

    public QueryString(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
