package com.seleniumtests.reporter.logger;

import com.seleniumtests.uipage.PageObject;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;

public class PageLoadTime extends TestAction {

    private final String url;
    private final double loadTime;
    private final PageObject pageObject;

    public PageLoadTime(String url, @NonNull PageObject pageObject, double loadTime) {
        super(String.format("loading of %s took %.3f", pageObject.getClass().getSimpleName(), loadTime), false, new ArrayList<>());
        this.url = url;
        this.loadTime = loadTime;
        this.pageObject = pageObject;
    }

    @Override
    public JSONObject toJson() {
        JSONObject actionJson = new JSONObject();

        actionJson.put("url", url);
        actionJson.put("name", name);
        actionJson.put("loadTime", loadTime);
        actionJson.put("page", pageObject.getClass().getSimpleName());

        return actionJson;
    }

    @Override
    public PageLoadTime encode(String format) {
        PageLoadTime pageLoadTime = new PageLoadTime(encodeString(url, format), pageObject, loadTime);

        if (format == null) {
            pageLoadTime.encoded = encoded;
        } else {
            pageLoadTime.encoded = true;
        }

        return pageLoadTime;
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
