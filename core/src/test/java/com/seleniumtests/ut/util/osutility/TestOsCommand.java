package com.seleniumtests.ut.util.osutility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.mockito.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

import static org.mockito.Mockito.*;

public class TestOsCommand extends MockitoTest {


	@Mock
	private DefaultExecutor.Builder builder;

	@Mock
	private DefaultExecutor executor;


	
	@Test(groups={"ut"})
	public void testExecuteCommandAndWait() throws IOException {
		try (MockedStatic<DefaultExecutor> mockedExecutor = mockStatic(DefaultExecutor.class);
		 ) {
			mockedExecutor.when(DefaultExecutor::builder).thenReturn(builder);
			when(builder.get()).thenReturn(executor);

			ArgumentCaptor<CommandLine> commandLineArgumentCaptor = ArgumentCaptor.forClass(CommandLine.class);
			new OSCommand(List.of("myCmd", "firstArg"), 5, StandardCharsets.UTF_8).execute();

			verify(executor).execute(commandLineArgumentCaptor.capture());
			String[] arguments = commandLineArgumentCaptor.getValue().getArguments();
			Assert.assertEquals(arguments.length, 1);
			Assert.assertEquals(arguments[0], "firstArg");
			Assert.assertEquals(commandLineArgumentCaptor.getValue().getExecutable(), "myCmd");
		}

	}

	/**
	 * Check we search in path if requested, on windows
	 */
	@Test(groups={"ut"})
	public void testExecuteCommandAndWaitInPathWindows() throws IOException {

		try (MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
			 MockedStatic<DefaultExecutor> mockedExecutor = mockStatic(DefaultExecutor.class);
		) {
			mockedExecutor.when(DefaultExecutor::builder).thenReturn(builder);
			when(builder.get()).thenReturn(executor);
			mockedOsUtility.when(OSUtility::isWindows).thenReturn(true);

			OSCommand osCommand = spy(new OSCommand(List.of(OSCommand.USE_PATH + "myCmd", "firstArg"), 5, StandardCharsets.UTF_8));
			doReturn("C:\\bin\\myCmd.bat").when(osCommand).searchInWindowsPath("myCmd");
			ArgumentCaptor<CommandLine> commandLineArgumentCaptor = ArgumentCaptor.forClass(CommandLine.class);
			osCommand.execute();

			verify(executor).execute(commandLineArgumentCaptor.capture());
			String[] arguments = commandLineArgumentCaptor.getValue().getArguments();
			Assert.assertEquals(arguments.length, 1);
			Assert.assertEquals(arguments[0], "firstArg");
			Assert.assertEquals(commandLineArgumentCaptor.getValue().getExecutable(), "C:\\bin\\myCmd.bat");
		}
	}
	
	/**
	 * On Linux, we assume we already search in path
	 */
	@Test(groups={"ut"})
	public void testExecuteCommandAndWaitInPathLinux() throws IOException {
		try (MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class, Mockito.CALLS_REAL_METHODS);
			 MockedStatic<DefaultExecutor> mockedExecutor = mockStatic(DefaultExecutor.class);
		) {
			mockedExecutor.when(DefaultExecutor::builder).thenReturn(builder);
			when(builder.get()).thenReturn(executor);

			mockedOsUtility.when(OSUtility::isWindows).thenReturn(false);

			OSCommand osCommand = spy(new OSCommand(List.of(OSCommand.USE_PATH + "myCmd", "firstArg"), 5, StandardCharsets.UTF_8));
			doReturn("C:\\bin\\myCmd.bat").when(osCommand).searchInWindowsPath("myCmd");
			ArgumentCaptor<CommandLine> commandLineArgumentCaptor = ArgumentCaptor.forClass(CommandLine.class);
			osCommand.execute();

			verify(executor).execute(commandLineArgumentCaptor.capture());
			Assert.assertEquals(commandLineArgumentCaptor.getValue().getExecutable(), "myCmd");
		}
	}
	
	/**
	 * Check that command is stopped before end
	 */
	@Test(groups={"it"})
	public void testExecutePingCommandAndWait() {
		if (OSUtility.isWindows()) {
			String out = OSCommand.executeCommandAndWait(new String[] {"ping", "localhost", "-n", "10"}, 5, null);
			System.out.println(out);
			Assert.assertEquals(out.split("\n").length, 7);
		}
	}
	
	/**
	 * Check that command is stopped after end
	 */
	@Test(groups={"it"})
	public void testExecutePingCommandAndWaitEnd() {
		if (OSUtility.isWindows()) {
			String out = OSCommand.executeCommandAndWait(new String[] {"ping", "localhost", "-n", "4"}, 10, null);
			System.out.println(out);
			Assert.assertEquals(out.split("\n").length, 11); // we went to the end of the program, so more result lines are added
		}
	}

}
