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
    private int headersSize;
    private int bodySize;

    public Response(int status, String statusText, String httpVersion, List<Header> headers, List<Cookie> cookies, Content content, String redirectURL, int headersSize, int bodySize) {
        this.status = status;
        this.statusText = statusText;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.cookies = cookies;
        this.content = content;
        this.redirectURL = redirectURL;
        this.headersSize = headersSize;
        this.bodySize = bodySize;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public Content getContent() {
        return content;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public int getHeadersSize() {
        return headersSize;
    }

    public int getBodySize() {
        return bodySize;
    }
}
