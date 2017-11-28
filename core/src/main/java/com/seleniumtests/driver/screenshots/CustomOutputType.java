package com.seleniumtests.driver.screenshots;

import java.util.Base64;

import org.openqa.selenium.OutputType;

public interface CustomOutputType<T> extends OutputType<T> {

	OutputType<String> DESKTOP_BASE64 = new OutputType<String>() {
	    public String convertFromBase64Png(String base64Png) {
	      return base64Png;
	    }

	    public String convertFromPngBytes(byte[] png) {
	      return Base64.getEncoder().encodeToString(png);
	    }

	    public String toString() {
	      return "OutputType.DESKTOP_BASE64";
	    }
	  };
}
