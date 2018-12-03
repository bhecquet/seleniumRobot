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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Common methods for Windows and Unix systems.
 */
public class OSCommand {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(OSCommand.class);
	
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
        
        Process proc;
        try {
			proc = Runtime.getRuntime().exec(cmd);
			return readOutput(proc);
	        
        } catch (IOException e1) {
        	logger.error(e1);
        }
        
        return "";
    }
    
    private static String readOutput(Process proc) throws IOException {
    	String output = "";
    	BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    	
    	BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        
        String s;
        
    	// read result output from command
        while ((s = stdInput.readLine()) != null) {
            output += s + "\n";
        }
        // read error output from command
        while ((s = stdError.readLine()) != null) {
            output += s + "\n";
        }
        
        return output;
    }

    /**
     * Execute a command in command line terminal
     * @param cmd
     * @param wait for the end of the command execution
     * @return 
     */
    public static String executeCommandAndWait(final String cmd) {
        Process proc;
        try {
			proc = Runtime.getRuntime().exec(cmd);
			return readOutput(proc);
			
        } catch (IOException e1) {
        	logger.error(e1);
        }
        
        return "";
    }
}
