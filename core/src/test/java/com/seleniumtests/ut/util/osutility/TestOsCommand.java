package com.seleniumtests.ut.util.osutility;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

//@PrepareForTest({ProcessBuilder.class, OSCommand.class, OSUtility.class, Runtime.class})
public class TestOsCommand extends MockitoTest {
	
	@Mock
	private Process process;
	
	@Mock
	private Process processSearchInPath;
	
	@Mock
	private Runtime runtime;
	
	@BeforeMethod(groups={"ut"}, alwaysRun = true)
	public void init() {
//		PowerMockito.mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
	}
	
	@Test(groups={"ut"})
	public void testExecuteCommandAndWait() throws IOException {
		
//		ProcessBuilder processBuilder = PowerMockito.mock(ProcessBuilder.class);
//
//		when(processBuilder.start()).thenReturn(process);
//		new OSCommand(Arrays.asList("myCmd"), 5, StandardCharsets.UTF_8, processBuilder).execute();
//
//		verify(processBuilder).command(Arrays.asList("myCmd"));
	}

	@Test(groups={"ut"})
	public void testExecuteCommandAndWaitInPathWindows() throws IOException {
		
//		ProcessBuilder processBuilder = PowerMockito.mock(ProcessBuilder.class);
//		PowerMockito.when(OSUtility.isWindows()).thenReturn(true);
//
////		when(processBuilder.start()).thenReturn(processSearchInPath, process);
//		OSCommand osCommand = spy(new OSCommand(Arrays.asList(OSCommand.USE_PATH + "myCmd"), 5, StandardCharsets.UTF_8, processBuilder));
		
//		doReturn("C:\\bin\\myCmd.bat").when(osCommand).searchInWindowsPath("myCmd");
//		osCommand.execute();
		
//		verify(processBuilder).command(Arrays.asList("C:\\bin\\myCmd.bat"));
	}
	
	/**
	 * On Linux, we assume we already search in path
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testExecuteCommandAndWaitInPathLinux() throws IOException {
		
//		ProcessBuilder processBuilder = PowerMockito.mock(ProcessBuilder.class);
//		PowerMockito.when(OSUtility.isWindows()).thenReturn(false);
		
//		when(processBuilder.start()).thenReturn(processSearchInPath, process);
//		OSCommand osCommand = spy(new OSCommand(Arrays.asList(OSCommand.USE_PATH + "myCmd"), 5, StandardCharsets.UTF_8, processBuilder));
//
//		doReturn("C:\\bin\\myCmd.bat").when(osCommand).searchInWindowsPath("myCmd");
//		osCommand.execute();
		
//		verify(processBuilder).command(Arrays.asList("myCmd"));
	}
	

	@Test(groups={"ut"})
	public void testExecuteCommand() throws IOException {
		
		Runtime runtime = spy(Runtime.getRuntime());

		doReturn(process).when(runtime).exec("myCmd");
		new OSCommand("myCmd", 5, StandardCharsets.UTF_8, runtime).executeNoWait();
		
		verify(runtime).exec("myCmd");
	}

	
	/**
	 * Check that command is stopped before end
	 */
	@Test(groups={"it"})
	public void testExecutePingCommandAndWait() {
		if (OSUtility.isWindows()) {
			String out = OSCommand.executeCommandAndWait("ping localhost -n 10", 5, null);
			Assert.assertEquals(out.split("\n").length, 7);
		}
	}
	
	/**
	 * Check that command is stopped after end
	 */
	@Test(groups={"it"})
	public void testExecutePingCommandAndWaitEnd() {
		if (OSUtility.isWindows()) {
			String out = OSCommand.executeCommandAndWait("ping localhost -n 4", 10, null);
			Assert.assertEquals(out.split("\n").length, 11); // we went to the end of the program, so more result lines are added
		}
	}
}
