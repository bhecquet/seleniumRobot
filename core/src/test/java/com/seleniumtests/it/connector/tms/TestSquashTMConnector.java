package com.seleniumtests.it.connector.tms;

import com.seleniumtests.GenericTest;
import com.seleniumtests.connectors.tms.squash.SquashTMConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestVariable;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.reporter.logger.Snapshot;
import com.seleniumtests.reporter.logger.TestStep;

import org.jetbrains.annotations.NotNull;
import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

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
    

    /**
     * Method allowing to test updateTestCase on Squash TM
     * provide following properties:
     * url
     * token
     * project
     * testCaseId: the ID of an existing test case in Squash TM
     * <p>
     * Then check in Squash TM that the test case has been updated =>
     * - description updated to "Integration test for updateTestCase"
     * - old steps deleted
     * - 2 new steps created with appropriate action/expectedResult
     * @throws IOException 
     */
    @Test(groups = "squash", enabled = true)
    public void testUpdateTestCase(ITestContext context) throws IOException {

        String testCaseId = System.getProperty("testCaseId", "928126");
        SeleniumTestsContextManager.getThreadContext().getConfiguration().put("tms.testId", new TestVariable("tms.testId", testCaseId));

        // Add test steps to the context so that updateTestCase can push them to Squash TM
        TestStep step1 = new TestStep("Step 1", "Step 1", null, null, List.of(), true,
                com.seleniumtests.core.Step.RootCause.NONE, null, false,
                "Open the application", "Application is displayed and the previous step has been deleted");

        TestStep step2 = new TestStep("Step 2", "Step 2", null, null, List.of(), true,
                com.seleniumtests.core.Step.RootCause.NONE, null, false,
                "Login with valid credentials", "User is logged in successfully");

        TestStep step3 = new TestStep("Step 3", "Step 3", null, null, List.of(), true,
                com.seleniumtests.core.Step.RootCause.NONE, null, false,
                "Step with a screenshot", "Check the attachments");
        File screenshot = File.createTempFile("screenshot", ".png");
        Files.copy(Paths.get("src/test/resources/ti/form_picture.png"), screenshot.toPath(), StandardCopyOption.REPLACE_EXISTING);
        step3.addSnapshot(new Snapshot(new ScreenShot(screenshot), "toto.png", SnapshotCheckType.NONE), 2, "toto");

        SeleniumTestsContextManager.getThreadContext().getTestStepManager().getTestSteps().addAll(List.of(step1, step2, step3));

        SquashTMConnector connector = new SquashTMConnector(System.getProperty("url"), null, System.getProperty("token"), System.getProperty("project"));

        ITestResult result = new UpdateTestCaseResult(context);
        connector.updateTestCase(result);
    }

    /**
     * Custom ITestResult that provides the updateTestManager attribute set to true
     * and a description for the test method
     */
    class UpdateTestCaseResult extends LocalTestResult {

        public UpdateTestCaseResult(ITestContext context) {
            super(context);
        }

        @Override
        public ITestNGMethod getMethod() {
            ITestNGMethod originalMethod = context.getAllTestMethods()[0];
            return new DelegatingTestNGMethod(originalMethod);
        }
    }

    /**
     * Wrapper around ITestNGMethod that adds updateTestManager and testId custom attributes
     * and provides a description
     */
    class DelegatingTestNGMethod implements ITestNGMethod {

        private final ITestNGMethod delegate;

        DelegatingTestNGMethod(ITestNGMethod delegate) {
            this.delegate = delegate;
        }

        @Override
        public CustomAttribute[] getAttributes() {
            CustomAttribute updateTestManagerAttr = new CustomAttribute() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return CustomAttribute.class;
                }

                @Override
                public String[] values() {
                    return new String[]{"true"};
                }

                @Override
                public String name() {
                    return "updateTestManager";
                }
            };

            CustomAttribute testIdAttr = new CustomAttribute() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return CustomAttribute.class;
                }

                @Override
                public String[] values() {
                    return new String[]{System.getProperty("testCaseId", "123456")};
                }

                @Override
                public String name() {
                    return "testId";
                }
            };

            return new CustomAttribute[]{updateTestManagerAttr, testIdAttr};
        }

        @Override
        public String getDescription() {
            return "Integration test for updateTestCase";
        }

        // --- All other methods delegate to original ---

        @Override
        public Class getRealClass() {
            return delegate.getRealClass();
        }

        @Override
        public ITestClass getTestClass() {
            return delegate.getTestClass();
        }

        @Override
        public void setTestClass(ITestClass cls) {
            delegate.setTestClass(cls);
        }

        @Override
        public String getMethodName() {
            return delegate.getMethodName();
        }

        @Override
        public Object getInstance() {
            return delegate.getInstance();
        }

        @Override
        public long[] getInstanceHashCodes() {
            return delegate.getInstanceHashCodes();
        }

        @Override
        public String[] getGroups() {
            return delegate.getGroups();
        }

        @Override
        public String[] getGroupsDependedUpon() {
            return delegate.getGroupsDependedUpon();
        }

        @Override
        public String getMissingGroup() {
            return delegate.getMissingGroup();
        }

        @Override
        public void setMissingGroup(String group) {
            delegate.setMissingGroup(group);
        }

        @Override
        public String[] getBeforeGroups() {
            return delegate.getBeforeGroups();
        }

        @Override
        public String[] getAfterGroups() {
            return delegate.getAfterGroups();
        }

        @Override
        public String[] getMethodsDependedUpon() {
            return delegate.getMethodsDependedUpon();
        }

        @Override
        public void addMethodDependedUpon(String methodName) {
            delegate.addMethodDependedUpon(methodName);
        }

        @Override
        public boolean isTest() {
            return delegate.isTest();
        }

        @Override
        public boolean isBeforeMethodConfiguration() {
            return delegate.isBeforeMethodConfiguration();
        }

        @Override
        public boolean isAfterMethodConfiguration() {
            return delegate.isAfterMethodConfiguration();
        }

        @Override
        public boolean isBeforeClassConfiguration() {
            return delegate.isBeforeClassConfiguration();
        }

        @Override
        public boolean isAfterClassConfiguration() {
            return delegate.isAfterClassConfiguration();
        }

        @Override
        public boolean isBeforeSuiteConfiguration() {
            return delegate.isBeforeSuiteConfiguration();
        }

        @Override
        public boolean isAfterSuiteConfiguration() {
            return delegate.isAfterSuiteConfiguration();
        }

        @Override
        public boolean isBeforeTestConfiguration() {
            return delegate.isBeforeTestConfiguration();
        }

        @Override
        public boolean isAfterTestConfiguration() {
            return delegate.isAfterTestConfiguration();
        }

        @Override
        public boolean isBeforeGroupsConfiguration() {
            return delegate.isBeforeGroupsConfiguration();
        }

        @Override
        public boolean isAfterGroupsConfiguration() {
            return delegate.isAfterGroupsConfiguration();
        }

        @Override
        public long getTimeOut() {
            return delegate.getTimeOut();
        }

        @Override
        public void setTimeOut(long timeOut) {
            delegate.setTimeOut(timeOut);
        }

        @Override
        public int getInvocationCount() {
            return delegate.getInvocationCount();
        }

        @Override
        public void setInvocationCount(int count) {
            delegate.setInvocationCount(count);
        }

        @Override
        public int getSuccessPercentage() {
            return delegate.getSuccessPercentage();
        }

        @Override
        public String getId() {
            return delegate.getId();
        }

        @Override
        public void setId(String id) {
            delegate.setId(id);
        }

        @Override
        public long getDate() {
            return delegate.getDate();
        }

        @Override
        public void setDate(long date) {
            delegate.setDate(date);
        }

        @Override
        public boolean canRunFromClass(IClass testClass) {
            return delegate.canRunFromClass(testClass);
        }

        @Override
        public boolean isAlwaysRun() {
            return delegate.isAlwaysRun();
        }

        @Override
        public int getThreadPoolSize() {
            return delegate.getThreadPoolSize();
        }

        @Override
        public void setThreadPoolSize(int threadPoolSize) {
            delegate.setThreadPoolSize(threadPoolSize);
        }

        @Override
        public boolean getEnabled() {
            return delegate.getEnabled();
        }

        @Override
        public void setDescription(String description) {
            delegate.setDescription(description);
        }

        @Override
        public void incrementCurrentInvocationCount() {
            delegate.incrementCurrentInvocationCount();
        }

        @Override
        public int getCurrentInvocationCount() {
            return delegate.getCurrentInvocationCount();
        }

        @Override
        public void setParameterInvocationCount(int n) {
            delegate.setParameterInvocationCount(n);
        }

        @Override
        public int getParameterInvocationCount() {
            return delegate.getParameterInvocationCount();
        }

        @Override
        public void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker) {

        }

        @Override
        public boolean hasMoreInvocation() {
            return false;
        }

        @Override
        public ITestNGMethod clone() {
            return delegate.clone();
        }

        @Override
        public IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
            return delegate.getRetryAnalyzer(result);
        }

        @Override
        public void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> clazz) {
            delegate.setRetryAnalyzerClass(clazz);
        }

        @Override
        public Class<? extends IRetryAnalyzer> getRetryAnalyzerClass() {
            return delegate.getRetryAnalyzerClass();
        }

        @Override
        public boolean skipFailedInvocations() {
            return delegate.skipFailedInvocations();
        }

        @Override
        public void setSkipFailedInvocations(boolean skip) {
            delegate.setSkipFailedInvocations(skip);
        }

        @Override
        public long getInvocationTimeOut() {
            return delegate.getInvocationTimeOut();
        }

        @Override
        public boolean ignoreMissingDependencies() {
            return delegate.ignoreMissingDependencies();
        }

        @Override
        public void setIgnoreMissingDependencies(boolean ignore) {
            delegate.setIgnoreMissingDependencies(ignore);
        }

        @Override
        public List<Integer> getInvocationNumbers() {
            return delegate.getInvocationNumbers();
        }

        @Override
        public void setInvocationNumbers(List<Integer> numbers) {
            delegate.setInvocationNumbers(numbers);
        }

        @Override
        public void addFailedInvocationNumber(int number) {
            delegate.addFailedInvocationNumber(number);
        }

        @Override
        public List<Integer> getFailedInvocationNumbers() {
            return delegate.getFailedInvocationNumbers();
        }

        @Override
        public int getPriority() {
            return delegate.getPriority();
        }

        @Override
        public void setPriority(int priority) {
            delegate.setPriority(priority);
        }

        @Override
        public int getInterceptedPriority() {
            return delegate.getInterceptedPriority();
        }

        @Override
        public void setInterceptedPriority(int priority) {
            delegate.setInterceptedPriority(priority);
        }

        @Override
        public XmlTest getXmlTest() {
            return delegate.getXmlTest();
        }

        @Override
        public org.testng.internal.ConstructorOrMethod getConstructorOrMethod() {
            return delegate.getConstructorOrMethod();
        }

        @Override
        public Map<String, String> findMethodParameters(XmlTest test) {
            return delegate.findMethodParameters(test);
        }

        @Override
        public String getQualifiedName() {
            return delegate.getQualifiedName();
        }

    }
}
