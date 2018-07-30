/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

public enum ArchiveMode {
	TRUE ("true"),
    FALSE ("false"),
    ON_SUCCESS ("onSuccess"),
    ON_ERROR ("onError");
	
	String[] mode;
	
	ArchiveMode(final String... archiveMode) {
        this.mode = archiveMode;
    }
	
	public static ArchiveMode fromString(String mode) {
		try {
			return ArchiveMode.valueOf(mode);
		} catch (IllegalArgumentException ex) {
			for (ArchiveMode archiveMode : ArchiveMode.values()) {
		        for (String matcher : archiveMode.mode) {
		          if (mode.equalsIgnoreCase(matcher)) {
		            return archiveMode;
		          }
		        }
		      }
		      throw new IllegalArgumentException("Unrecognized archive mode: " + mode);
		}
	}	
}
