package com.seleniumtests.it.stubclasses;

import com.seleniumtests.it.core.aspects.CalcPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StubTestClassForAssertionLogging extends StubParentClass {

    @Test(groups="stub")
    public void testAssertionOKIsLogged() {
        new CalcPage()
                .assertActionOk()
                .add(1);
    }

    @Test(groups="stub")
    public void testAssertionOKIsLogged2() {
        new CalcPage()
                .add(1);
        Assert.assertEquals(1, 2, "values are different");
    }

    @Test(groups="stub")
    public void testAssertionKOIsLogged() {
        new CalcPage()
                .assertWithSubStep();
    }
}
