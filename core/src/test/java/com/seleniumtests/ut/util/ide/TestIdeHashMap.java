package com.seleniumtests.ut.util.ide;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.TestStep;
import com.seleniumtests.util.ide.IdeHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TestIdeHashMap extends GenericTest {
    
    @Test(groups = {"ut"})
    public void testPut() {
        Map<String, Object> m = new IdeHashMap<>();
        TestStepManager.logTestStep(new TestStep("step"));
        m.put("foo", "bar");
        Assert.assertEquals(m.get("foo"), "bar");
    }
    
    /**
     * if key contains 'pwd', password should be stored
     */
    @Test(groups = {"ut"})
    public void testPutPassword() {
        TestStepManager.setCurrentRootTestStep(new TestStep("step"));
        Map<String, Object> m = new IdeHashMap<>();
        m.put("pWd", "barbar");
        Assert.assertEquals(m.get("pWd"), "barbar");
        Assert.assertTrue(TestStepManager.getCurrentRootTestStep().getPwdToReplace().contains("barbar"));
    }
    /**
     * if key contains 'password', password should be stored
     */
    @Test(groups = {"ut"})
    public void testPutPassword2() {
        TestStepManager.setCurrentRootTestStep(new TestStep("step"));
        Map<String, Object> m = new IdeHashMap<>();
        m.put("myPassword", "barbar");
        Assert.assertEquals(m.get("myPassword"), "barbar");
        Assert.assertTrue(TestStepManager.getCurrentRootTestStep().getPwdToReplace().contains("barbar"));
    }
    /**
     * if key contains 'password', password should be stored
     */
    @Test(groups = {"ut"})
    public void testPutPassword3() {
        TestStepManager.setCurrentRootTestStep(new TestStep("step"));
        Map<String, Object> m = new IdeHashMap<>();
        m.put("myPasswd", "barbar");
        Assert.assertEquals(m.get("myPasswd"), "barbar");
        Assert.assertTrue(TestStepManager.getCurrentRootTestStep().getPwdToReplace().contains("barbar"));
    }
    /**
     * if key contains 'pwd', short password should not be stored
     */
    @Test(groups = {"ut"})
    public void testPutShortPassword() {
        TestStepManager.setCurrentRootTestStep(new TestStep("step"));
        Map<String, Object> m = new IdeHashMap<>();
        m.put("pwd", "barba");
        Assert.assertEquals(m.get("pwd"), "barba");
        Assert.assertTrue(TestStepManager.getCurrentRootTestStep().getPwdToReplace().isEmpty());
    }
    /**
     * Null key should not raise exception
     */
    @Test(groups = {"ut"})
    public void testPutKeyNull() {
        TestStepManager.setCurrentRootTestStep(new TestStep("step"));
        Map<String, Object> m = new IdeHashMap<>();
        m.put(null, "bar");
        Assert.assertEquals(m.get(null), "bar");
        Assert.assertTrue(TestStepManager.getCurrentRootTestStep().getPwdToReplace().isEmpty());
    }
    
    /**
     * Null value should not raise exception, password 'null' won't be stored
     */
    @Test(groups = {"ut"})
    public void testPutValueNull() {
        TestStepManager.setCurrentRootTestStep(new TestStep("step"));
        Map<String, Object> m = new IdeHashMap<>();
        m.put("pwd", null);
        Assert.assertNull(m.get("pwd"));
        Assert.assertTrue(TestStepManager.getCurrentRootTestStep().getPwdToReplace().isEmpty());
    }
    /**
     * if no TestStep is available, do not raise error
     */
    @Test(groups = {"ut"})
    public void testPutPasswordNoStep() {
        Map<String, Object> m = new IdeHashMap<>();
        m.put("pWd", "barbar");
        Assert.assertEquals(m.get("pWd"), "barbar");
    }
}
