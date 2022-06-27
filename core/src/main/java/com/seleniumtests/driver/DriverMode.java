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
package com.seleniumtests.driver;

public enum DriverMode {
    LOCAL ("local"),
    GRID ("grid"),
    BROWSERSTACK ("browserstack"),
    SAUCELABS ("saucelabs"),
	PERFECTO ("perfecto");
	
	String[] dMode;
	
	DriverMode(final String... driverMode) {
        this.dMode = driverMode;
    }
	
	public static DriverMode fromString(String mode) {
		try {
			return DriverMode.valueOf(mode);
		} catch (IllegalArgumentException ex) {
			for (DriverMode drvMode : DriverMode.values()) {
		        for (String matcher : drvMode.dMode) {
		          if (mode.equalsIgnoreCase(matcher)) {
		            return drvMode;
		          }
		        }
		      }
		      throw new IllegalArgumentException("Unrecognized driver mode: " + mode);
		}
	}	
}
