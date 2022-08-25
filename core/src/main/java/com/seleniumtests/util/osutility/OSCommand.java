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
package com.seleniumtests.util.osutility;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Common methods for Windows and Unix systems.
 */
public class OSCommand {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(OSCommand.class);
	public static final String USE_PATH = "_USE_PATH_";	
	
	private OSCommand() {
		// class with static methods
	}
	
	public static Process executeCommand(final String cmd) {
		Process proc;
        try {
			proc = Runtime.getRuntime().exec(cmd);
			return proc;
        } catch (IOException e1) {
        	throw new CustomSeleniumTestsException("cannot start process: " + cmd, e1);
        }
	}
	
	public static Process executeCommand(final String[] cmd) {
		Process proc;
		try {
			proc = Runtime.getRuntime().exec(cmd);
			return proc;
		} catch (IOException e1) {
			throw new CustomSeleniumTestsException("cannot start process: " + cmd, e1);
		}
	}
	
	/**
     * Execute a command in command line terminal
     * @param cmd
     * @param wait for the end of the command execution
     * @return 
     */
    public static String executeCommandAndWait(final String[] cmd) {
    	return executeCommandAndWait(cmd, -1, null);
    }
    
    /**
     * Execute a command in command line terminal and wait at most 'timeout' seconds
     * @param cmd		An array containing the program to execute and its arguments
     * 					e.g: ["cmd.exe", "foo"]
     * 					If first item is "_USE_PATH_", then, on Windows, we will start cmd.exe first. This is because ProcessBuilder does not look for programs into the path
     * @param timeout 	number of seconds to wait for end of execution. A negative value means it will wait 30 secs
     * @return 
     */
   	public static String executeCommandAndWait(final String[] cmd, int timeout, Charset charset) {
        
   		List<String> newCmd = new ArrayList<>(Arrays.asList(cmd));
   		
   		if (newCmd.get(0).startsWith(USE_PATH)) {
   			newCmd.set(0, newCmd.get(0).replace(USE_PATH, ""));
   			
   	    	if (OSUtility.isWindows()) {
   	    		String out = executeCommandAndWait(new String[] {"where", newCmd.get(0)});
   	    		try {
   	    			String path = out.split("\n")[out.split("\n").length - 1].trim();
   	    			newCmd.set(0, path);
   	    		} catch (IndexOutOfBoundsException e) {
   	    			throw new ScenarioException(String.format("Program %s is not in the path", newCmd.get(0)));
   	    		}

   	    	}
   		}
   		
        try {
        	ProcessBuilder pb = new ProcessBuilder(newCmd);
        	Process proc = pb.start();
			return waitProcessTermination(proc, timeout, charset);
			
        } catch (IOException e) {
        	logger.error(e);
        } 
        
        return "";
    }
    
    /**
     * Execute a command in command line terminal
     * @param cmd
     * @param wait for the end of the command execution
     * @return 
     */
   	public static String executeCommandAndWait(final String cmd) {
   		return executeCommandAndWait(cmd, -1, null);
   	}
   	
    /**
     * Execute a command in command line terminal and wait at most 'timeout' seconds
     * @param cmd
     * @param timeout 	number of seconds to wait for end of execution. A negative value means it will wait 30 secs
     * @param charset	charset used to read program output. If set to null, default charset will be used
     * @return 
     */
    public static String executeCommandAndWait(final String cmd, int timeout, Charset charset) {

        try {
			Process proc = Runtime.getRuntime().exec(cmd);
			return waitProcessTermination(proc, timeout, charset);
			
        } catch (IOException e1) {
        	logger.error(e1);
        } 
        
        return "";
    }
    
    private static String waitProcessTermination(Process proc, int timeout, Charset charset) {

    	try {

			return readOutput(proc, timeout, charset);
    	} catch (InterruptedException e) {
        	logger.error("Interruption: " + e.getMessage());
        	Thread.currentThread().interrupt();

		} catch (IOException e1) {
        	logger.error(e1);
        } 
    	return "";
    }
    
    private static String readOutput(Process proc, int timeout, Charset charset) throws IOException, InterruptedException {
    	
    	if (charset == null) {
    		charset = OSUtility.getCharset();
    	}

    	Clock clock = Clock.systemUTC();
		Instant end = clock.instant().plusSeconds(timeout > 0 ? timeout: 30);
    	
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), charset);
		StreamGobbler outputGobbler  = new StreamGobbler(proc.getInputStream(), charset);
		errorGobbler.start();
        outputGobbler.start();
        
        boolean read = false;
        boolean terminated = false;
        while (end.isAfter(clock.instant()) && (!read || !terminated)) {
        	// be sure we read all logs produced by process, event after termination
        	if (!proc.isAlive()) {
        		terminated = true;
        	}
        	
        	read = true;
        	
        	Thread.sleep(100);
        }
        errorGobbler.halt();
        outputGobbler.halt();
        
        StringBuilder error = errorGobbler.getOutput();
        StringBuilder output = outputGobbler.getOutput();
        
        return output.toString() + '\n' + error.toString();
    }

}
