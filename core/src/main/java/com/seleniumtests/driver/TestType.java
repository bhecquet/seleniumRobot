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
package com.seleniumtests.driver;

/**
 * @author  tbhadauria: tarun.kumar.bhadauria@zalando.de
 */
public enum TestType {

    WEB("web") {
    	@Override
    	public boolean isMobile() {
    		return false;
    	}
    	@Override
    	public TestType family() {
        	return WEB;
        }
    },
    APP("app") {
    	@Override
    	public boolean isMobile() {
    		return true;
    	}
    	@Override
    	public TestType family() {
        	return APP;
        }
    },
    NON_GUI("NonGUI") {
    	@Override
    	public boolean isMobile() {
    		return false;
    	}
    	@Override
    	public TestType family() {
        	return NON_GUI;
        }
    },
    APPIUM_WEB_ANDROID("appium_web_android") {
    	@Override
    	public boolean isMobile() {
    		return true;
    	}
    	@Override
    	public TestType family() {
        	return WEB;
        }
    },
    APPIUM_WEB_IOS("appium_web_ios") {
    	@Override
    	public boolean isMobile() {
    		return true;
    	}
    	@Override
    	public TestType family() {
        	return WEB;
        }
    },
	APPIUM_APP_ANDROID("appium_app_android") {
    	@Override
    	public boolean isMobile() {
    		return true;
    	}
    	@Override
    	public TestType family() {
        	return APP;
        }
    },
	APPIUM_APP_IOS("appium_app_ios") {
    	@Override
    	public boolean isMobile() {
    		return true;
    	}
    	@Override
    	public TestType family() {
        	return APP;
        }
    },
	APPIUM_APP_WINDOWS("appium_app_windows") {
		@Override
		public boolean isMobile() {
			return false;
		}
		@Override
		public TestType family() {
			return APP;
		}
	};

    String testType;

    TestType(final String testType) {
        this.testType = testType;
    }

    public boolean isMobile() {
		return false;
	}
    public TestType family() {
    	return WEB;
    }

	public String getTestType() {
        return testType;
    }

}
