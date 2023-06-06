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
	
	private List<String> cmdList;
	private String cmdString;
	private int timeout;
	private Charset charset;
	private ProcessBuilder pb;
	private Runtime runtime;
	
	public OSCommand(List<String> cmdList) {
		this(cmdList, -1, null);
	}
	
	public OSCommand(List<String> cmdList, int timeout, Charset charset) {
		this(cmdList, timeout, charset, new ProcessBuilder());
	}
	
	public OSCommand(List<String> cmdList, int timeout, Charset charset, ProcessBuilder pb) {
		
		this.timeout = timeout;
		this.charset = charset;
		this.pb = pb;
		this.cmdList = cmdList;
	}
	
	/**
	 * Execute a process, giving the full command as a String (useful when command uses pipe, for example)
	 * @param cmdString
	 * @param timeout
	 * @param charset
	 */
	public OSCommand(String cmdString, int timeout, Charset charset) {
		this(cmdString, timeout, charset, Runtime.getRuntime());
	}
	public OSCommand(String cmdString, int timeout, Charset charset, Runtime runtime) {
		
		this.timeout = timeout;
		this.charset = charset;
		this.cmdString = cmdString;
		this.runtime = runtime;
	}
	
	/**
	 * Search a program in windows path
	 * Throw an exception if it cannot be found
	 * @param command
	 * @return
	 */
	public String searchInWindowsPath(String command) {
		String out = new OSCommand(Arrays.asList("where", command), timeout, charset, pb).execute(); 
		try {
			return out.split("\n")[out.split("\n").length - 1].trim();
			
		} catch (IndexOutOfBoundsException e) {
			throw new ScenarioException(String.format("Program %s is not in the path", command));
		}
	}

	/**
	 * Update command list
	 * @param cmd
	 * @return
	 */
	private List<String> updateCommand(List<String> cmd) {
		List<String> newCmd = new ArrayList<>(cmd);
		
		if (newCmd.get(0).startsWith(USE_PATH)) {
			newCmd.set(0, newCmd.get(0).replace(USE_PATH, ""));
			
			if (OSUtility.isWindows()) {
				String path = searchInWindowsPath(newCmd.get(0));
				newCmd.set(0, path);
			}
		}
		return newCmd;
	}
	
	/**
	 * Execute the process and wait for termination
	 * @return
	 */
	public String execute() {
		Process proc = executeNoWait();
		return waitProcessTermination(proc, timeout, charset);
	}
	
	
	/**
	 * Executes the command and returns the process
	 * @return
	 */
	public Process executeNoWait() {
		if (cmdList != null) {
			cmdList = updateCommand(cmdList);
			pb.command(cmdList);
			
			try {
				return pb.start();
			} catch (IOException e) {
				throw new CustomSeleniumTestsException("cannot start process: " + cmdList.get(0), e);
			}
		} else {
			try {
				return runtime.exec(cmdString);
			} catch (IOException e) {
				throw new CustomSeleniumTestsException("cannot execute command: " + cmdString, e);
			}
		}
		
	}
	
	public static Process executeCommand(final String cmd) {
		return new OSCommand(cmd, -1, null).executeNoWait();
	}
	
	public static Process executeCommand(final String[] cmd) {
		return new OSCommand(Arrays.asList(cmd), -1, null).executeNoWait();		
	}
	
	/**
     * Execute a command in command line terminal
     * @param cmd for the end of the command execution
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
        return new OSCommand(Arrays.asList(cmd), timeout, charset).execute();
    }
    
    /**
     * Execute a command in command line terminal
     * @param cmd for the end of the command execution
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
    	return new OSCommand(cmd, timeout, charset).execute();
    }
    
    public String waitProcessTermination(Process proc, int timeout, Charset charset) {

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
        
        try {
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
        } catch (Exception e) {
        	// in case something gets wrong, stop the threads
	        errorGobbler.halt();
	        outputGobbler.halt();
	        throw e;
        }
    }

}
