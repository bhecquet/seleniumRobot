package com.seleniumtests.uipage.htmlelements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;

public class UiLibraryRegistry {
	
	private static List<String> uiLibraries = Collections.synchronizedList(new ArrayList<>());
	
	private UiLibraryRegistry() {
		// nothing to do
	}
	
	public synchronized static void register(String uiLibrary) {
		if (!uiLibraries.contains(uiLibrary)) {
			uiLibraries.add(uiLibrary);
		}
	}

	public static List<String> getUiLibraries() {
		return uiLibraries;
	}
	
}
