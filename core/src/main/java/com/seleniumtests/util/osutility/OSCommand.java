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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.exec.*;
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
	private final int timeout;
	private final Charset charset;
	
	public OSCommand(List<String> cmdList) {
		this(cmdList, -1, null);
	}

	/**
	 *
	 * @param cmdList		List of commands / arguments to the process
	 * @param timeout		timeout to wait for process, then we return
	 * @param charset		charset to read output
	 */
	public OSCommand(List<String> cmdList, int timeout, Charset charset) {
		
		this.timeout = timeout;
		if (charset == null) {
			this.charset = OSUtility.getCharset();
		} else {
			this.charset = charset;
		}
		this.cmdList = cmdList;
	}

	
	/**
	 * Search a program in windows path
	 * Throw an exception if it cannot be found
	 * @param command	The command to execute
	 * @return the new command with path
	 */
	public String searchInWindowsPath(String command) {
		String out = new OSCommand(Arrays.asList("where", command), timeout, charset).execute();
		try {
			return out.split("\n")[out.split("\n").length - 1].trim();
			
		} catch (IndexOutOfBoundsException e) {
			throw new ScenarioException(String.format("Program %s is not in the path", command));
		}
	}

	/**
	 * Update command list
	 * @param cmd	command to update
	 * @return	the updated command
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
	 * @return	the output of the program
	 */
	public String execute() {
		if (cmdList != null && !cmdList.isEmpty()) {
			cmdList = updateCommand(cmdList);
			CommandLine commandLine = new CommandLine(cmdList.get(0));
			for (String arg: cmdList.subList(1, cmdList.size())) {
				commandLine.addArgument(arg);
			}
			DefaultExecutor executor = DefaultExecutor.builder().get();
			ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(Duration.ofSeconds(timeout > 0 ? timeout: 30)).get();
			executor.setWatchdog(watchdog);
			// we accept all exit codes from -1 to 256
			executor.setExitValues(java.util.stream.IntStream.rangeClosed(-1, 256).toArray());

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
			executor.setStreamHandler(streamHandler);

			try {
				executor.execute(commandLine);
				return outputStream.toString(charset);
			} catch (IOException e) {
				logger.error("Error executing {}: {}", cmdList.get(0), e.getMessage());
				return "no output";
			}
		} else {
			return "no output";
		}
	}
	
	
	/**
	 * Executes the command and returns the process
	 * @return	created process
	 */
	public Process executeNoWait() {
		if (cmdList != null) {
			cmdList = updateCommand(cmdList);
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(cmdList);
			pb.redirectErrorStream(true);
			
			try {
				return pb.start();
			} catch (IOException e) {
				throw new CustomSeleniumTestsException("cannot start process: " + cmdList.get(0), e);
			}
		}
		return null;
		
	}

	/**
	 * Execute a process and do not wait for termination
	 * @param cmd	command to execute
	 * @return		the created process
	 */
	public static Process executeCommand(final String[] cmd) {
		return new OSCommand(Arrays.asList(cmd), -1, null).executeNoWait();		
	}
	
	/**
     * Execute a command in command line terminal. Wait for the end of the command execution
     * @param cmd	command to execute, with arguments
     * @return 	output of the executed command
     */
	public static String executeCommandAndWait(final String[] cmd) {
    	return executeCommandAndWait(cmd, 30, null);
    }
    

    /**
     * Execute a command in command line terminal and wait at most 'timeout' seconds
     * @param cmd		An array containing the program to execute and its arguments
     * 					e.g: ["cmd.exe", "foo"]
     * 					If first item is "_USE_PATH_", then, on Windows, we will start cmd.exe first. This is because ProcessBuilder does not look for programs into the path
     * @param timeout 	number of seconds to wait for end of execution. A negative value means it will wait 30 secs
	 * @return 	output of the executed command
     */
   	public static String executeCommandAndWait(final String[] cmd, int timeout, Charset charset) {
        return new OSCommand(Arrays.asList(cmd), timeout, charset).execute();
    }

}
