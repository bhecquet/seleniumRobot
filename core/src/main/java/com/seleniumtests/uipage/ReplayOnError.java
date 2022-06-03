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
package com.seleniumtests.uipage;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used in ReplayAction.aj so that any HtmlElement method annotated by ReplayOnError
 * will be replayed during 30 secs if any error occurs
 * Moreover, ReplayAction is in charge of entering iframe when action on element is performed
 * e.g: clicking on an element raises an error, seleniumRobot will retry several times until really failing
 *      or until click is successful
 *      
 * Do not annotate all HtmlElement methods because ReplayAction would be called several times for the same action
 * e.g: use HtmlElement.isTextPresent() on an element existing in an iframe
 *      HtmlElement.isTextPresent() calls HtmlElement.getText() and assume both are annotated
 *      When calling isTextPresent(), ReplayAction.aj would be activated one time, entering iframes
 *      As isTextPresent() calls getText(), seleniumRobot tries to enter iframes and fails because driver is already in iframe
 *      
 * Only methods calling findElement or any of the similar methods should be annotated because these ones will 
 * act directly on element
 * @author behe
 *
 */

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface ReplayOnError {
	public int replayDelayMs() default 300;
	public int replayTimes() default -1;
	public boolean waitAfterAction() default false; // do we wait after the action is performed
}
