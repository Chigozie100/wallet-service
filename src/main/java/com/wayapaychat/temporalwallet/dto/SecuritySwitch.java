package com.wayapaychat.temporalwallet.dto;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecuritySwitch {

	private static final String ALGO = "AES";

	public static String decrypt(String strToDecrypt, String secret) {
		try {
			Key key = generateKey(secret);
			Cipher cipher = Cipher.getInstance(ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}

	private static Key generateKey(String secret) throws Exception {
		byte[] decoded = Base64.getDecoder().decode(secret.getBytes());
		Key key = new SecretKeySpec(decoded, ALGO);
		return key;
	}

	public static String decodeKey(String str) {
		byte[] decoded = Base64.getDecoder().decode(str.getBytes());
		return new String(decoded);
	}

}
