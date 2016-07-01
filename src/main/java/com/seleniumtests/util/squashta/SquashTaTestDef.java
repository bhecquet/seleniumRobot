package com.seleniumtests.util.squashta;

import java.io.File;

/**
 * Class defining a test from Squash TA search
 * @author behe
 *
 */
public class SquashTaTestDef {
		
	File file;
	String name;
	boolean isCucumber;
	String cucumberTestName;
	boolean isCucumberGeneric;
	
	public SquashTaTestDef(File testFile, String testName, boolean cucumber, String cucumberName) {
		file = testFile;
		name = testName;
		isCucumber = cucumber;
		cucumberTestName = cucumberName;
		if (cucumberName.isEmpty() && isCucumber) {
			isCucumberGeneric = true;
		} else {
			isCucumberGeneric = false;
		}
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
        	return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        SquashTaTestDef other;
        try {
        	other = (SquashTaTestDef) obj;
        } catch (Exception e) {
        	return false;
        }

        return this.toString() !=null ? this.toString().equals(other.toString()) : this.toString() == other.toString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    @Override
    public String toString() {
        return name + "_" + cucumberTestName;
    }
}
