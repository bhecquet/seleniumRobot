package com.seleniumtests;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class LogUnitTest {
	
	private static final Logger logger = Logger.getRootLogger();

	@Before("execution(@org.testng.annotations.Test public * com.seleniumtests..* (..))")
	public void logTestStart(JoinPoint joinPoint)  { 
		logger.info("executing " + joinPoint.getSignature());
	}
}
