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
package com.seleniumtests.uipage.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
/**
 * Aspect to intercept calls to methods from By. It change the argument of
 * methods when it use the keyWord : map
 * 
 * @author Sophie
 *
 */
@Aspect
public class InterceptBy {

	 @Around("call(* org.openqa.selenium.By.id (..) )")
	public Object changeArg(ProceedingJoinPoint joinPoint) throws Throwable {
		 Object[] args = joinPoint.getArgs();
		 
		 
		// deactivate aspect but do not remove it as it breaks existing test applications (they need to be recompiled)
		return joinPoint.proceed(args);

	}
	
	
}