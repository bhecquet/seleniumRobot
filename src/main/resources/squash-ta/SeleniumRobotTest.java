

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Assert;
import org.junit.Test;

public class SeleniumRobotTest {
	

	@Test
	public void test() throws Exception {
		String command = "\"%JAVA_HOME_STF%/bin/java\" -cp %STF_HOME%/seleniumRobot.jar:%STF_HOME%/plugins/${application}-tests.jar -Dbrowser=${IT_CUF_browser} ${TC_CUF_cucumberTest} -Denv=${IT_CUF_testEnvironment} org.testng.TestNG ${testngFile} -testnames ${testngName}";
		
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			command = command.replace("seleniumRobot.jar:", "seleniumRobot.jar;");
		}
		
		command = command.replace("%STF_HOME%", System.getenv("STF_HOME"));
		command = command.replace("%JAVA_HOME_STF%", System.getenv("JAVA_HOME_STF"));
		
		String line;
		StringBuilder output = new StringBuilder();
		
		System.out.println("Starting STF with: " + command);
	    Process p = Runtime.getRuntime().exec(command);
		BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((line = bri.readLine()) != null) {
		    System.out.println(line);
		    output.append(line);
		}
		bri.close();
		
		p.waitFor();
		
		Assert.assertTrue(output.toString().contains("Failures: 0"));
		
	}

}


