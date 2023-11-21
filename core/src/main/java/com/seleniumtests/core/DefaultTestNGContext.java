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
package com.seleniumtests.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.testng.IClass;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.internal.TestNGMethod;
import org.testng.xml.XmlTest;

import com.google.inject.Injector;
import com.google.inject.Module;

public class DefaultTestNGContext implements ITestContext {

    ISuite suite;

    public DefaultTestNGContext() {
        this.suite = new SeleniumTestsDefaultSuite();
    }
    
    public DefaultTestNGContext(ISuite suite) {
    	this.suite = suite;
    }

    @Override
    public Object getAttribute(final String name) {
        return null;
    }

    @Override
    public void setAttribute(final String name, final Object value) { 
    	//TODO
    }

    @Override
    public Set<String> getAttributeNames() {
        return new TreeSet<>();
    }

    @Override
    public Object removeAttribute(final String name) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Date getStartDate() {
        return null;
    }

    @Override
    public Date getEndDate() {
        return null;
    }

    @Override
    public IResultMap getPassedTests() {
        return null;
    }

    @Override
    public IResultMap getSkippedTests() {
        return null;
    }

    @Override
    public IResultMap getFailedButWithinSuccessPercentageTests() {
        return null;
    }

    @Override
    public IResultMap getFailedTests() {
        return null;
    }

    @Override
    public String[] getIncludedGroups() {
        return new String[] {};
    }

    @Override
    public String[] getExcludedGroups() {
        return new String[] {};
    }

    @Override
    public String getOutputDirectory() {
    	return suite.getOutputDirectory();
    }

    @Override
    public ISuite getSuite() {
        return suite;
    }

    @Override
    public ITestNGMethod[] getAllTestMethods() {
        return new TestNGMethod[] {};
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public Collection<ITestNGMethod> getExcludedMethods() {
        return new ArrayList<>();
    }

    @Override
    public IResultMap getPassedConfigurations() {
        return null;
    }

    @Override
    public IResultMap getSkippedConfigurations() {
        return null;
    }

    @Override
    public IResultMap getFailedConfigurations() {
        return null;
    }

    @Override
    public XmlTest getCurrentXmlTest() {
        return null;
    }

    public void addGuiceModule(final Class<? extends Module> cls, final Module module) { 
    	//TODO
    }


}
