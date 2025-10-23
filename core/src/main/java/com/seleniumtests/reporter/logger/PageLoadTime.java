package com.seleniumtests.reporter.logger;

import com.seleniumtests.uipage.PageObject;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class PageLoadTime extends TestAction {

    private String url;
    private final long loadTime;
    private final PageObject pageObject;

    /**
     *
     * @param url           URL of the loaded page
     * @param pageObject    the page object
     * @param loadTime      load time of the page in milliseconds
     *
     * Timestamp will be the Instant of page load start
     */
    public PageLoadTime(String url, @NonNull PageObject pageObject, long loadTime) {
        super(String.format("loading of %s took %d ms", pageObject.getClass().getSimpleName(), loadTime), false, new ArrayList<>());
        this.url = url;
        this.loadTime = loadTime;
        this.pageObject = pageObject;
        this.timestamp = this.timestamp.minus(loadTime, ChronoUnit.MILLIS);
    }

    @Override
    public JSONObject toJson() {
        JSONObject actionJson = new JSONObject();

        actionJson.put("url", url);
        actionJson.put("name", getName());
        actionJson.put("loadTime", loadTime);
        actionJson.put("page", pageObject.getClass().getSimpleName());
        actionJson.put("timestamp", timestamp.toInstant().toEpochMilli());

        return actionJson;
    }

    @Override
    public PageLoadTime encodeTo(String format) {
        PageLoadTime pageLoadTimeToEncode = new PageLoadTime(url, pageObject, loadTime);
        return encode(format, pageLoadTimeToEncode);
    }

    private PageLoadTime encode(String format, PageLoadTime pageLoadTimeToEncode) {
        super.encode(format, pageLoadTimeToEncode);
        pageLoadTimeToEncode.url = encodeString(url, format);
        return pageLoadTimeToEncode;
    }

    public String getUrl() {
        return url;
    }

    public double getLoadTime() {
        return loadTime;
    }

    public PageObject getPageObject() {
        return pageObject;
    }
}
