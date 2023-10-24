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
package com.seleniumtests.util.logging;

import java.util.ArrayList;
import java.util.List;

public enum DebugMode {

	NONE("none"), DRIVER("driver"), CORE("core"), GUI("gui"), NETWORK("network");

	String[] dMode;

	DebugMode(final String... debugMode) {
		this.dMode = debugMode;
	}

	/**
	 * Convert a string to list of DebugMode comma separated list of values are
	 * allowed
	 * 
	 * @param mode
	 * @return
	 */
	public static List<DebugMode> fromString(String modes) {
		List<DebugMode> dbgModes = new ArrayList<>();
		for (String mode : modes.split(",")) {
			try {
				dbgModes.add(DebugMode.valueOf(modes));
			} catch (IllegalArgumentException ex) {
				DebugMode foundMode = null;
				for (DebugMode dbgMode : DebugMode.values()) {
					for (String matcher : dbgMode.dMode) {
						if (mode.equalsIgnoreCase(matcher)) {
							foundMode = dbgMode;
						}
					}
				}
				if (foundMode != null) {
					dbgModes.add(foundMode);
				} else {
					throw new IllegalArgumentException("Unrecognized debug mode: " + mode);
				}
			}
		}
		return dbgModes;
	}

}
