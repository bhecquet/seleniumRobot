package com.seleniumtests.util.har;

public class Content {
    private String mimeType;
    private int size;
    private String text;

    public Content(String mimeType, int size, String text) {
        this.mimeType = mimeType;
        this.size = size;
        this.text = text;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getSize() {
        return size;
    }

    public String getText() {
        return text;
    }
}
