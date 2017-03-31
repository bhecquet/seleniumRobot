/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.core.aspects;

import static java.util.concurrent.TimeUnit.SECONDS;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.internal.ApacheHttpClient;
import org.openqa.selenium.remote.internal.HttpClientFactory;

@Aspect
public class SocketTimeout {
	
	
	public static final Integer DEFAULT_TIMEOUT = 60000;
	private static final int TIMEOUT_TWO_MINUTES = (int) SECONDS.toMillis(60 * 2);
	
	private static HttpClient.Factory httpClientFactory = new ApacheHttpClient.Factory(new HttpClientFactory(TIMEOUT_TWO_MINUTES, TIMEOUT_TWO_MINUTES));
	
	/**
	 * 
	 * @param joinPoint
	 */
	@Around("execution(private * org.openqa.selenium.remote.HttpCommandExecutor.getDefaultClientFactory (..))")
    public HttpClient.Factory changetTimeout(ProceedingJoinPoint joinPoint) {
		return httpClientFactory;
	}
	
}
