package com.seleniumtests.reporter.info;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.StringUtility;

public class StringInfo extends Info {

    public StringInfo(String info) {
        super(info);
    }

    public String encode(String format) {
       // if (format == null) {
       //     logger.error("format cannot be null");
       //     return description;
       // } else {
            try {
                return StringUtility.encodeString(description, format);
            } catch (CustomSeleniumTestsException e) {
                return description;
            }
        //}
    }
}

