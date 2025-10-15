package com.seleniumtests.util.har;

import java.util.List;

public class Request {

    private final int bodySize;
    private final String method;
    private final String url;
    private final String httpVersion;
    private final List<Header> headers;
    private final List<Cookie> cookies;
    private final List<QueryString> queryString;
    private final int headersSize;

    public Request(int bodySize, String method, String url, String httpVersion, List<Header> headers, List<Cookie> cookies, List<QueryString> queryString, int headersSize) {
        this.bodySize = bodySize;
        this.method = method;
        this.url = url;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.cookies = cookies;
        this.queryString = queryString;
        this.headersSize = headersSize;
    }

    public int getBodySize() {
        return bodySize;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
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

    public List<QueryString> getQueryString() {
        return queryString;
    }

    public int getHeadersSize() {
        return headersSize;
    }
}
