/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
package com.seleniumtests.ut.reporter;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.reporter.logger.TestAction;
import com.seleniumtests.reporter.logger.TestMessage;
import com.seleniumtests.reporter.logger.TestMessage.MessageType;
import com.seleniumtests.reporter.logger.TestStep;

public class TestTestStep extends GenericTest {

	/**
	 * Checks getFailed correctly compute test step status if action is failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionKo() {
		TestStep step = new TestStep("step1");
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", true));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionOk() {
		TestStep step = new TestStep("step1");
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if action is not failed but test step is KO
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithStepKo() {
		TestStep step = new TestStep("step1");
		step.setFailed(true);
		step.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is  failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepKo() {
		TestStep step = new TestStep("step1");
		TestStep subStep = new TestStep("subStep");
		subStep.addAction(new TestAction("action1", true));
		step.addAction(new TestAction("action2", false));
		step.addAction(subStep);
		Assert.assertTrue(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 */
	@Test(groups={"ut"})
	public void testGetFailedWithActionSubStepOk() {
		TestStep step = new TestStep("step1");
		TestStep subStep = new TestStep("subStep");
		subStep.addAction(new TestAction("action1", false));
		step.addAction(new TestAction("action2", false));
		Assert.assertFalse(step.getFailed());
	}
	
	/**
	 * Checks getFailed correctly compute test step status if sub step is not failed
	 */
	@Test(groups={"ut"})
	public void testToJson() {
		TestStep step = new TestStep("step1");
		step.addMessage(new TestMessage("everything OK", MessageType.INFO));
		step.addAction(new TestAction("action2", false));
		
		TestStep subStep = new TestStep("subStep");
		subStep.addMessage(new TestMessage("everything in subStep almost OK", MessageType.WARNING));
		subStep.addAction(new TestAction("action1", false));
		step.addAction(subStep);
		
		JSONObject stepJson = step.toJson();
		
		Assert.assertEquals(stepJson.getString("type"), "step");
		Assert.assertEquals(stepJson.getString("name"), "step1");
		Assert.assertEquals(stepJson.getJSONArray("actions").length(), 3);
		
		// check actions order
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(0).getString("type"), "message");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(0).getString("messageType"), "INFO");
		
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getString("type"), "action");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getString("name"), "action2");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(1).getBoolean("failed"), false);
		
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("type"), "step");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getString("name"), "subStep");
		Assert.assertEquals(stepJson.getJSONArray("actions").getJSONObject(2).getJSONArray("actions").length(), 2);
		
	}
}
