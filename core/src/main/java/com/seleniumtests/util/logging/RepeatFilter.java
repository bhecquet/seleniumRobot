package com.seleniumtests.util.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

@Plugin(name = "RepeatFilter", category = "Core", elementType = "filter", printObject = true)
public final class RepeatFilter extends AbstractFilter {
 
    private final Level level;
    private ThreadLocal<String> lastLog = new ThreadLocal<>();
    private ThreadLocal<Integer> lastLogRepeat = new ThreadLocal<>();
 
    private RepeatFilter(Level level, Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
        this.level = level;
    }
 
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object[] params) {
        return filter(msg);
    }
 
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return filter(msg.getFormattedMessage());
    }
 
    @Override
    public Result filter(LogEvent event) {
        return filter(event.getMessage().getFormattedMessage());
    }
 
    private Result filter(String message) {
    	
    	boolean discard = false;
    	if (lastLog.get() != null) {
    		
    		// same message than previous, increment repeat
    		if (lastLog.get().equals(message)) {
    			if (lastLogRepeat.get() == null) {
    				lastLogRepeat.set(1);
    			}
    			lastLogRepeat.set(lastLogRepeat.get() + 1);
    			discard = true;
    		} else {
    			Integer repeatTime = lastLogRepeat.get();
    			lastLogRepeat.set(0);
    			if (repeatTime != null && repeatTime > 1) {
    				System.out.println(String.format("... repeated %d times ...", repeatTime));
    			}
    			lastLogRepeat.set(1);
    		}
    	} else {
    		lastLogRepeat.set(1);
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
     * @param loggerLevel The log Level.
     * @param match The action to take on a match.
     * @param mismatch The action to take on a mismatch.
     * @return The created ThresholdFilter.
     */
    @PluginFactory
    public static RepeatFilter createFilter(@PluginAttribute(value = "level", defaultString = "ERROR") Level level,
                                               @PluginAttribute(value = "onMatch", defaultString = "NEUTRAL") Result onMatch,
                                               @PluginAttribute(value = "onMismatch", defaultString = "DENY") Result onMismatch) {
        return new RepeatFilter(level, onMatch, onMismatch);
    }
}