package com.seleniumtests.reporter.logger;

import org.json.JSONObject;

public class Check extends TestMessage {

    public Check(String name, Boolean failed) {
        super(name, Boolean.TRUE.equals(failed) ? MessageType.ERROR : MessageType.INFO, failed);
    }


    @Override
    public JSONObject toJson() {
        JSONObject actionJson = super.toJson();

        actionJson.put("type", "check");
        actionJson.put("failed", getFailed());

        return actionJson;
    }

    @Override
    public Check encodeTo(String format) {
        Check messageToEncode = new Check(name, failed);
        return encode(format, messageToEncode);
    }

    private Check encode(String format, Check checkToEncode) {
        super.encode(format, checkToEncode);
        return checkToEncode;
    }
}
