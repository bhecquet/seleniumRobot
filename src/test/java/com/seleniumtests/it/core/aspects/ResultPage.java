package com.seleniumtests.it.core.aspects;

import java.io.IOException;

import com.seleniumtests.uipage.PageObject;

public class ResultPage extends PageObject {
	
	private int result;

	public ResultPage(int result) throws IOException {
		super();
		this.result = result;
	}
	
	public int getResult() {
		return result;
	}

}
