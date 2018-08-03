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
package com.seleniumtests.core.aspects;

import java.lang.reflect.Field;
import java.net.URL;
import java.time.Duration;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.internal.OkHttpClient;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

@Aspect
public class SocketTimeout {
	
	private static boolean socketTimeoutUpdated = false;
	private static final Logger logger = SeleniumRobotLogger.getLogger(SocketTimeout.class);

	/**
	 * Change timeout after HttpCommandExecutor creation
	 * 
	 * HttpCommandExecutor is responsible for sending commands to browser
	 * Sometimes, browser is stuck. Default behaviour is a 3 hours wait
	 * Change this to avoid test to be stuck during this time
	 * 
	 * @param joinPoint
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@After("initialization(org.openqa.selenium.remote.HttpCommandExecutor.new (..))")
	public void changeTimeout2(JoinPoint joinPoint) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		HttpClient.Factory httpClientFactory;
		if (SeleniumTestsContextManager.isMobileTest()) {
			httpClientFactory = new OkHttpClient.Factory(Duration.ofMinutes(6), Duration.ofMinutes(6));
		} else {
			httpClientFactory = new OkHttpClient.Factory(Duration.ofMinutes(2), Duration.ofMinutes(2));
		}
		
		HttpCommandExecutor commandExecutor = (HttpCommandExecutor)joinPoint.getThis();
		URL url = (URL) joinPoint.getArgs()[0];
		
		HttpClient client = httpClientFactory.createClient(url);
		
		Field clientField = HttpCommandExecutor.class.getDeclaredField("client");
		clientField.setAccessible(true);
		clientField.set(commandExecutor, client);
		
		logger.info("Socket timeout for driver communication updated");
		
		socketTimeoutUpdated = true;
	}

	public static boolean isSocketTimeoutUpdated() {
		return socketTimeoutUpdated;
	}
	
}
