package com.seleniumtests.ut.util.osutility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

import static org.mockito.Mockito.*;

public class TestOsCommand extends MockitoTest {
	
	@Mock
	private Process process;
	
	@Mock
	private Process processSearchInPath;
	
	@Mock
	private Runtime runtime;

	
	@Test(groups={"ut"})
	public void testExecuteCommandAndWait() throws IOException {
		try (MockedConstruction mockedProcessBuilder = mockConstruction(ProcessBuilder.class, (processBuilder, context) -> {
			when(processBuilder.start()).thenReturn(process);
		})) {
			ProcessBuilder processBuilder = new ProcessBuilder();
			new OSCommand(Arrays.asList("myCmd"), 5, StandardCharsets.UTF_8, processBuilder, true).execute();

			verify(processBuilder).command(Arrays.asList("myCmd"));
		}
	}

	@Test(groups={"ut"})
	public void testExecuteCommandAndWaitInPathWindows() throws IOException {
		try (MockedStatic mockedOsUtility = mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
			 MockedConstruction mockedProcessBuilder = mockConstruction(ProcessBuilder.class);
			 ) {
			ProcessBuilder processBuilder = new ProcessBuilder();
			mockedOsUtility.when(() -> OSUtility.isWindows()).thenReturn(true);

			when(processBuilder.start()).thenReturn(processSearchInPath, process);
			OSCommand osCommand = spy(new OSCommand(Arrays.asList(OSCommand.USE_PATH + "myCmd"), 5, StandardCharsets.UTF_8, processBuilder, true));

			doReturn("C:\\bin\\myCmd.bat").when(osCommand).searchInWindowsPath("myCmd");
			osCommand.execute();

			verify(processBuilder).command(Arrays.asList("C:\\bin\\myCmd.bat"));
		}
	}
	
	/**
	 * On Linux, we assume we already search in path
	 * @throws IOException
	 */
	@Test(groups={"ut"})
	public void testExecuteCommandAndWaitInPathLinux() throws IOException {
		try (MockedStatic mockedOsUtility = mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
			 MockedConstruction mockedProcessBuilder = mockConstruction(ProcessBuilder.class);
			 ) {

			ProcessBuilder processBuilder = new ProcessBuilder();
			mockedOsUtility.when(() -> OSUtility.isWindows()).thenReturn(false);

			when(processBuilder.start()).thenReturn(processSearchInPath, process);
			OSCommand osCommand = spy(new OSCommand(Arrays.asList(OSCommand.USE_PATH + "myCmd"), 5, StandardCharsets.UTF_8, processBuilder, true));

			doReturn("C:\\bin\\myCmd.bat").when(osCommand).searchInWindowsPath("myCmd");
			osCommand.execute();

			verify(processBuilder).command(Arrays.asList("myCmd"));
		}
	}
	

	@Test(groups={"ut"})
	public void testExecuteCommand() throws IOException {
		
		Runtime runtime = spy(Runtime.getRuntime());

		doReturn(process).when(runtime).exec("myCmd");
		new OSCommand("myCmd", 5, StandardCharsets.UTF_8, runtime, false).executeNoWait();
		
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
