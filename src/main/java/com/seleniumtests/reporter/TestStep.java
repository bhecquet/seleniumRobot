package com.seleniumtests.reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Group of test actions
 * Represent each method call inside a PageObject during the test
 * @author behe
 *
 */
public class TestStep extends TestAction {
	private List<TestAction> stepActions;
	
	public TestStep(String name) {
		super(name, false);
		stepActions = new ArrayList<>();
	}
	
	public List<TestAction> getStepActions() {
		return stepActions;
	}

	/**
	 * Return true if this step or one of its actions / sub-step is failed
	 */
	public Boolean getFailed() {
		if (super.getFailed()) {
			return true;
		} 
		for (TestAction action: stepActions) {
			if (action.getFailed()) {
				return true;
			}
		}
		return false;
	}

	public void addAction(TestAction action) {
		stepActions.add(action);
	}
	public void addStep(TestStep step) {
		stepActions.add(step);
	}
	
	
}
