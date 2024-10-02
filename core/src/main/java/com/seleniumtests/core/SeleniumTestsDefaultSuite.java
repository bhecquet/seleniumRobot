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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.*;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlSuite;

import com.google.inject.Injector;

public class SeleniumTestsDefaultSuite implements ISuite {

    private XmlSuite xmlSuite;

    public SeleniumTestsDefaultSuite() {
        this.xmlSuite = new DefaultXmlSuite();
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
        return Collections.emptySet();
    }

    @Override
    public Object removeAttribute(final String name) {
        return null;
    }

    @Override
    public String getName() {
        return "Default suite";
    }

    @Override
    public Map<String, ISuiteResult> getResults() {
        return Collections.emptyMap();
    }

    @Override
    public ITestObjectFactory getObjectFactory() {
        return null;
    }

    @Override
    public String getOutputDirectory() {
        return null;
    }

    @Override
    public String getParallel() {
        return null;
    }

    @Override
    public String getParameter(final String parameterName) {
        return null;
    }

    @Override
    public Map<String, Collection<ITestNGMethod>> getMethodsByGroups() {
        return Collections.emptyMap();
    }

    @Override
    public List<IInvokedMethod> getAllInvokedMethods() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ITestNGMethod> getExcludedMethods() {
        return Collections.emptyList();
    }

    @Override
    public void run() { 
    	//TODO
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public SuiteRunState getSuiteState() {
        return null;
    }

    @Override
    public IAnnotationFinder getAnnotationFinder() {
        return null;
    }

    @Override
    public XmlSuite getXmlSuite() {
        return xmlSuite;
    }

    @Override
    public void addListener(final ITestNGListener listener) {
    	//TODO
    }

    @Override
    public List<ITestNGMethod> getAllMethods() {
        return Collections.emptyList();
    }

    @Override
	public String getParentModule() {
		return null;
	}

    @Override
	public String getGuiceStage() {
		return null;
	}

    @Override
	public Injector getParentInjector() {
		return null;
	}

    @Override
	public void setParentInjector(Injector injector) {
    	//TODO
	}

}
