package com.seleniumtests.util.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;

public class LoggerWrapper  extends Logger {
    
    private org.apache.logging.log4j.Logger rootLogger;

    protected LoggerWrapper(org.apache.logging.log4j.Logger rootLogger) {
        super((LoggerContext) LogManager.getContext(false), "wrapper", null);
        this.rootLogger = rootLogger;
    }
    
    @Override
    protected void log(final Level level, final Marker marker, final String fqcn, final StackTraceElement location,
                       final Message message, final Throwable throwable) {
        
        super.log(level, marker, fqcn, location, message, throwable);
    
        org.apache.logging.log4j.Logger testLogger = SeleniumRobotLogger.getLoggerForTest();
    
        if (testLogger != null) {
            //Logger tLogger = new Logger(testLogger."testWrapper", testLogger.getMessageFactory());
            testLogger.logMessage(level, marker, fqcn, location, message, throwable);
        }
    }

}
