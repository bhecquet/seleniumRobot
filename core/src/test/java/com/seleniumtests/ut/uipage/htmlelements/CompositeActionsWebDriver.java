package com.seleniumtests.ut.uipage.htmlelements;

import com.seleniumtests.ut.MockWebDriver;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Collection;

public class CompositeActionsWebDriver extends MockWebDriver {
    public CompositeActionsWebDriver() {
        super();
    }
    public CompositeActionsWebDriver(RemoteWebDriver mockedDriver) {
        super(mockedDriver);
    }
    @Override
    public void perform(Collection<Sequence> actions) {
    }

}
