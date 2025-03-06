package com.seleniumtests.it.connector.tms;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import org.jetbrains.annotations.NotNull;
import org.testng.IClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.util.Set;

public class TestSquashTMConnector extends GenericTest {

    /**
     * Method allowing to test connection to Squash TM
     * provide following properties
     * url
     * token
     * project
     *
     * Then check in Squash TM that result has been recorded =>
     * - campaign "Selenium core-selenium4"
     * - iteration "5.1" (or any core version")
     * - a single test case in list
     */
    @Test(groups="squash", enabled = false)
    public void testConnection(ITestContext context) {

        SeleniumTestsContextManager.getThreadContext().getConfiguration().put("tms.testId", new TestVariable("tms.testId", "928126"));
        SquashTMConnector connector = new SquashTMConnector(System.getProperty("url"), null, System.getProperty("token"), System.getProperty("project"));

        ITestResult result = new LocalTestResult(context);
        connector.recordResult(result);
    }

    class LocalTestResult implements ITestResult {

        ITestContext context;
        public LocalTestResult(ITestContext context) {
            this.context = context;
        }

        @Override
        public int getStatus() {
            return 0;
        }

        @Override
        public void setStatus(int status) {

        }

        @Override
        public ITestNGMethod getMethod() {
            return context.getAllTestMethods()[0];
        }

        @Override
        public Object[] getParameters() {
            return new Object[0];
        }

        @Override
        public void setParameters(Object[] parameters) {

        }

        @Override
        public IClass getTestClass() {
            return null;
        }

        @Override
        public Throwable getThrowable() {
            return null;
        }

        @Override
        public void setThrowable(Throwable throwable) {

        }

        @Override
        public long getStartMillis() {
            return 0;
        }

        @Override
        public long getEndMillis() {
            return 0;
        }

        @Override
        public void setEndMillis(long millis) {

        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getHost() {
            return "";
        }

        @Override
        public Object getInstance() {
            return null;
        }

        @Override
        public Object[] getFactoryParameters() {
            return new Object[0];
        }

        @Override
        public String getTestName() {
            return "";
        }

        @Override
        public String getInstanceName() {
            return "";
        }

        @Override
        public ITestContext getTestContext() {
            return context;
        }

        @Override
        public void setTestName(String name) {

        }

        @Override
        public boolean wasRetried() {
            return false;
        }

        @Override
        public void setWasRetried(boolean wasRetried) {

        }

        @Override
        public String id() {
            return "";
        }

        @Override
        public int compareTo(@NotNull ITestResult o) {
            return 0;
        }

        @Override
        public Object getAttribute(String name) {
            if ("testContext".equals(name)) {
                return SeleniumTestsContextManager.getThreadContext();
            } else {
                return null;
            }
        }

        @Override
        public void setAttribute(String name, Object value) {

        }

        @Override
        public Set<String> getAttributeNames() {
            return Set.of();
        }

        @Override
        public Object removeAttribute(String name) {
            return null;
        }
    }
}
