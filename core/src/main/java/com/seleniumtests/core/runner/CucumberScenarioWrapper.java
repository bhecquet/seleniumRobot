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
package com.seleniumtests.core.runner;

import cucumber.runtime.model.CucumberScenario;

/**
 * The only purpose of this class is to provide custom {@linkplain #toString()},
 * making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#feature(cucumber.api.testng.CucumberFeatureWrapper)
 */
public class CucumberScenarioWrapper {

	
    private final CucumberScenario cucumberScenario;

    public CucumberScenarioWrapper(CucumberScenario cucumberScenario) {
        this.cucumberScenario = cucumberScenario;
    }

    public CucumberScenario getCucumberScenario() {
        return cucumberScenario;
    }

    @Override
    public String toString() {
        return cucumberScenario.getGherkinModel().getName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
        	return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        CucumberScenarioWrapper other;
        try {
        	other = (CucumberScenarioWrapper) obj;
        } catch (Exception e) {
        	return false;
        }

        return this.toString() !=null ? this.toString().equals(other.toString()) : this.toString() == other.toString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
