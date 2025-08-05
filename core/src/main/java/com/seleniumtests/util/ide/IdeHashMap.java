package com.seleniumtests.util.ide;

import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.reporter.logger.TestStep;

import java.util.HashMap;

public class IdeHashMap<K,V> extends HashMap<K,V> {
    
    @Override
    public V put(K key, V value) {
        TestStep currentStep = TestStepManager.getCurrentRootTestStep();
        if (currentStep != null
                && key != null
                && value != null
                && (key.toString().toLowerCase().contains("pwd")
        || key.toString().toLowerCase().contains("password")
        || key.toString().toLowerCase().contains("passwd")
        )) {
            currentStep.addPasswordToReplace(value.toString());
        }
        
        return super.put(key, value);
    }
}
