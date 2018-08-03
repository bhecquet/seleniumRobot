package com.seleniumtests.core.utils;

/**
 * As selenium SystemClock class is deprecated, copy it here
 * @author s047432
 *
 */
public class SystemClock {
	
	public long laterBy(long durationInMillis) {
		return System.currentTimeMillis() + durationInMillis;
	}

	public boolean isNowBefore(long endInMillis) {
		return System.currentTimeMillis() < endInMillis;
	}
}
