package com.seleniumtests.util.har;

public class Header {
    private String name;
    private String value;

    public Header(String name, String value) {
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
