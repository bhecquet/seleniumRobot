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
package com.seleniumtests.reporter.logger;

import java.util.ArrayList;
import java.util.List;

public enum ArchiveMode {
	
    ALWAYS("always", "true"),
    NEVER("never", "false"),
    ON_SUCCESS ("onSuccess"),
    ON_ERROR ("onError"),
	ON_SKIP ("onSkip"),
	@Deprecated
	TRUE (""),
	@Deprecated
	FALSE ("");
	
	
	String[] mode;
	
	ArchiveMode(final String... archiveMode) {
        this.mode = archiveMode;
    }
	
	public static List<ArchiveMode> fromString(String modes) {
		List<ArchiveMode> archiveModes = new ArrayList<>();
		for (String mode : modes.split(",")) {
			try {
				archiveModes.add(ArchiveMode.valueOf(modes));
			} catch (IllegalArgumentException ex) {
				ArchiveMode foundMode = null;
				for (ArchiveMode archiveMode : ArchiveMode.values()) {
					for (String matcher : archiveMode.mode) {
						if (mode.equalsIgnoreCase(matcher)) {
							foundMode = archiveMode;
						}
					}
				}
				if (foundMode != null) {
					archiveModes.add(foundMode);
				} else {
					throw new IllegalArgumentException("Unrecognized archive mode: " + mode);
				}
			}
		}
		return archiveModes;
	}
	
}
