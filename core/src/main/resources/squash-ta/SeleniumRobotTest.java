/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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


