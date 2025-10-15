package com.seleniumtests.util.har;

public class Content {
    private final String mimeType;
    private final int size;
    private final String text;

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
