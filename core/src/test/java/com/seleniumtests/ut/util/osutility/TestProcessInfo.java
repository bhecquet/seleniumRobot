package com.seleniumtests.ut.util.osutility;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.util.osutility.ProcessInfo;

public class TestProcessInfo extends GenericTest {

    @Test(groups = {"ut"})
    public void testToString() {
        ProcessInfo pi = new ProcessInfo();
        pi.setPid("8");
        pi.setName("pi");
        String piString = pi.toString();
        assertEquals(piString, "Process #8 = pi", "ProcessInfo non String");
    }

    @Test(groups = {"ut"})
    public void testIfToStringComplete() {
        ProcessInfo pi = new ProcessInfo();
        pi.setPid("8");
        pi.setName("pi");
        pi.setSessionName("meduse");
        pi.setSessionNumber(5);
        pi.setMemUsage(8);
        pi.setStatus("true");
        pi.setUsername("medusa");
        pi.setCpuTime("11/11/11");
        pi.setTitle("C:/");
        String piString = pi.toString();
        assertEquals(piString, "Process #8 = pi ; session=meduse, #5 ;  8 Ko ; status=true ; username=medusa ; 11/11/11 ; title=\"C:/\"");
    }

    @Test(groups = {"ut"})
    public void testIfToString() {
        ProcessInfo pi = new ProcessInfo();
        pi.setMemUsage(2);
        String piString = pi.toString();
        assertEquals(piString, "Process #null = null ; session=null, #0 ;  2 Ko ; status=null ; username=null ; null ; title=\"null\"");
    }

    @Test(groups = {"ut"})
    public void testToStringNull() {
        ProcessInfo pi = new ProcessInfo();
        pi.setPid(null);
        pi.setName(null);
        String piString = pi.toString();
        assertEquals(piString, "Process #null = null");
    }

    @Test(groups = {"ut"})
    public void testObjectEquals() {
        ProcessInfo pi = new ProcessInfo();
        ProcessInfo info = new ProcessInfo();
        assertEquals(pi, info);
    }

    @Test(groups = {"ut"})
    public void testObjectNotEquals() {
        ProcessInfo pi = new ProcessInfo();
        Integer huit = 8;
        assertNotEquals(pi, huit);
    }

    @Test(groups = {"ut"})
    public void testObjectStringNotEquals() {
        ProcessInfo pi = new ProcessInfo();
        pi.setPid("8");
        pi.setName("pi");
        pi.setSessionName("meduse");
        pi.setSessionNumber(5);
        pi.setMemUsage(8);
        pi.setStatus("true");
        pi.setUsername("medusa");
        pi.setCpuTime("11/11/11");
        pi.setTitle("C:/");
        ProcessInfo info = new ProcessInfo();
        info.setPid("8");
        info.setName("info");
        info.setSessionName("meduse");
        info.setSessionNumber(4);
        info.setMemUsage(8);
        info.setStatus("false");
        info.setUsername("medusa");
        info.setCpuTime("11/11/11");
        info.setTitle("C:/");
        assertNotEquals(pi, info);
    }
}
