package com.seleniumtests.it.connector.exttools;

import java.security.InvalidKeyException;

import org.testng.annotations.Test;

import com.seleniumtests.connectors.extools.Totp;

public class TestTotp {
	
	@Test(enabled = true)
	public void testGenerateKey() {
		try {
			String code = Totp.generateCode("xxxx");
			System.out.println(code);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
