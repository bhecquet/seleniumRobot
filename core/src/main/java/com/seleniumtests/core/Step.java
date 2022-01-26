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
package com.seleniumtests.core;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to document PageObject methods
 *
 */

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface Step {
	
	public enum RootCause {
		NONE,
		TECHNICAL,
		DEPENDENCIES,	// error raised due to an other system failing / not present
		SCRIPTING,
		REGRESSION,		// may be used when no dependency is implied in this particular step

	}
	
	public String name() default "";
	
	// type of the most probable error causing this step to fail
	public RootCause errorCause() default RootCause.NONE;
	
	// a String giving details about error cause
	public String errorCauseDetails() default "";
	public boolean disableBugTracker() default false;
	

}
