package com.seleniumtests.util.har;

import java.util.List;

public class Response {
    private int status;
    private String statusText;
    private String httpVersion;
    private List<Header> headers;
    private List<Cookie> cookies;
    private Content content;
    private String redirectURL;
    private int headerSize;
    private int bodySize;

    public Response(int status, String statusText, String httpVersion, List<Header> headers, List<Cookie> cookies, Content content, String redirectURL, int headerSize, int bodySize) {
        this.status = status;
        this.statusText = statusText;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.cookies = cookies;
        this.content = content;
        this.redirectURL = redirectURL;
        this.headerSize = headerSize;
        this.bodySize = bodySize;
    }
}
