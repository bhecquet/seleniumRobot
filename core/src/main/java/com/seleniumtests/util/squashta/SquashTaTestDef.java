/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
