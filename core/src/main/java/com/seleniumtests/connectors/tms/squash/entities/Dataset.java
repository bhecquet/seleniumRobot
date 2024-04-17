package com.seleniumtests.connectors.tms.squash.entities;

import com.seleniumtests.customexception.ScenarioException;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class Dataset extends Entity {

    private static final String DATASET_URL = "datasets/%s";


    private TestCase testCase;

    public Dataset(String url, int id, String name) {
        this(url, id, name, null);
    }

    public Dataset(String url, int id, String name, TestCase testCase) {
        super(url, id, name);
        this.testCase = testCase;
    }

    public static Dataset fromJson(JSONObject json) {
        try {
            Dataset dataset = new Dataset(json.getJSONObject("_links").getJSONObject("self").getString("href"),
                json.getInt(FIELD_ID),
                json.optString(FIELD_NAME));

            // add information about the related test case if we have it
            if (json.optJSONObject("test_case") != null) {
                dataset.testCase = TestCase.fromJson(json.getJSONObject("test_case"));
            }

            return dataset;
        } catch (JSONException e) {
            throw new ScenarioException(String.format("Cannot create Dataset from JSON [%s] data: %s", json.toString(), e.getMessage()));
        }
    }

    public static Dataset get(int id) {
        try {
            return fromJson(getJSonResponse(buildGetRequest(apiRootUrl + String.format(DATASET_URL, id))));
        } catch (UnirestException e) {
            throw new ScenarioException(String.format("Dataset %d does not exist", id));
        }
    }

    public TestCase getTestCase() {
        return testCase;
    }


}
