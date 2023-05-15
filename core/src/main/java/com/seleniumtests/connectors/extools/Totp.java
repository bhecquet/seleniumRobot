package com.seleniumtests.connectors.extools;

import java.security.InvalidKeyException;
import java.time.Instant;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

public class Totp {
	
	private Totp() {
		// nothing to do
	}
	
	private static final TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
	
	/**
	 * Generates a one time password of 6 characters length
	 * HMAC-SHA1 is assumed to be the algorithm used to generate the key. It is the default algorithm for TOTP
	 * @param secretKey
	 * @return
	 * @throws InvalidKeyException 
	 */
	public static String generateCode(String secretKey) throws InvalidKeyException {
		Base32 base32 = new Base32();
	    byte[] bytes = base32.decode(secretKey);
		
		return totp.generateOneTimePasswordString(new SecretKeySpec(bytes, "SHA-1"), Instant.now());
	}

}
