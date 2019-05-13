package com.seleniumtests.util.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Classes for distinguishing System.out and System.err logs
 * @author S047432
 *
 */
public class Sys {

	
	public static class Out extends LoggingOutputStream {
		public Out(final Logger log) {
			super(log, Level.INFO);
		}

		@Override
		protected void log(String str) {
			log.log(level, str);
		}
	}
	
	public static class Error extends LoggingOutputStream {
		public Error(final Logger log) {
			super(log, Level.INFO);
		}

		@Override
		protected void log(String str) {
			log.log(level, str);
		}
	}
}
