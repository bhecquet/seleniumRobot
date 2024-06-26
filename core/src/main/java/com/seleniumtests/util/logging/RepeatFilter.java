package com.seleniumtests.util.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Plugin(name = "RepeatFilter", category = "Core", elementType = "filter", printObject = true)
public final class RepeatFilter extends AbstractFilter {
	
	private org.apache.logging.log4j.Logger logger = null;
	private org.apache.logging.log4j.Logger testLogger = null;
 
    private final Level level;
    private final boolean logTests;
    private ThreadLocal<String> lastLog = new ThreadLocal<>();
    private ThreadLocal<Integer> lastLogRepeat = new ThreadLocal<>();
    private ThreadLocal<LocalDateTime> lastLogTime = new ThreadLocal<>();
    private ThreadLocal<LocalDateTime> firstLogTime = new ThreadLocal<>();
 
    private RepeatFilter(Level level, boolean logTests, Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
        this.level = level;
        this.logTests = logTests;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object[] params) {
        return filter(msg);
    }

    @Override
	public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return filter(msg.getFormattedMessage());
    }
 
    @Override
    public Result filter(LogEvent event) {
        return filter(event.getMessage().getFormattedMessage());
    }
 
    private Result filter(String message) {
    	
    	// delay logger creation so that we do not have recursive call on init
    	if (logger == null && !logTests) {
        	logger = LogManager.getRootLogger();
    	}
    	else if (logTests) {
    	    logger = SeleniumRobotLogger.getLoggerForTest();
        }
    	
    	boolean discard = false;
    	if (lastLog.get() != null) {
    		
    		// same message than previous, increment repeat
    		// when we do not have different message during more than 60 secs, print it anyway
    		if (lastLog.get().equals(message) 
    				&& firstLogTime.get() != null 
    				&& firstLogTime.get().plusSeconds(60).isAfter(LocalDateTime.now())) {
    			if (lastLogRepeat.get() == null) {
    				lastLogRepeat.set(1);
    				
    			}
    			lastLogRepeat.set(lastLogRepeat.get() + 1);
    			lastLogTime.set(LocalDateTime.now());
    			discard = true;
    			
    		// message different from previous. If message has been repeated, display it
    		} else {
    			Integer repeatTime = lastLogRepeat.get();
    			lastLogRepeat.set(0);
    			if (repeatTime != null && repeatTime > 1) {
    			    if (logger != null) {
    				    logger.info("... repeated {} times until {} ...", repeatTime, lastLogTime.get().format(DateTimeFormatter.ISO_LOCAL_TIME));
                    }
    			}
    			lastLogRepeat.remove();
    			firstLogTime.set(LocalDateTime.now()); // record the time of the first occurence of the message
    		}
    	} else {
    		lastLogRepeat.remove();
    		firstLogTime.set(LocalDateTime.now());
    	}
    	
    	lastLog.set(message);
    	
        return discard ? onMismatch: onMatch;
    }
 
    @Override
    public String toString() {
        return level.toString();
    }
 
    /**
     * Create a ThresholdFilter.
     * @param level The log Level.
     * @param onMatch The action to take on a match.
     * @param onMismatch The action to take on a mismatch.
     * @return The created ThresholdFilter.
     */
    @PluginFactory
    public static RepeatFilter createFilter(@PluginAttribute(value = "level", defaultString = "ERROR") Level level,
                                               @PluginAttribute(value = "onMatch", defaultString = "NEUTRAL") Result onMatch,
                                               @PluginAttribute(value = "onMismatch", defaultString = "DENY") Result onMismatch) {
        return new RepeatFilter(level, false, onMatch, onMismatch);
    }
    public static RepeatFilter createFilter(@PluginAttribute(value = "level", defaultString = "ERROR") Level level,
                                               @PluginAttribute(value = "logTests", defaultString = "FALSE") boolean logTests,
                                               @PluginAttribute(value = "onMatch", defaultString = "NEUTRAL") Result onMatch,
                                               @PluginAttribute(value = "onMismatch", defaultString = "DENY") Result onMismatch) {
        return new RepeatFilter(level, logTests, onMatch, onMismatch);
    }
}