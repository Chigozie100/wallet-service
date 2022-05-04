package com.wayapaychat.temporalwallet.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CryptoUtils {

	private static final String DATE_FORMAT = "dd-M-yyyy hh:mm:ss a";

	public static byte[] getRandomNonce(int numBytes) {
		byte[] nonce = new byte[numBytes];
		new SecureRandom().nextBytes(nonce);
		return nonce;
	}

	// AES secret key
	public static SecretKey getAESKey(int keysize) throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(keysize, SecureRandom.getInstanceStrong());
		return keyGen.generateKey();
	}

	// Password derived AES 256 bits secret key
	public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		// iterationCount = 65536
		// keyLength = 256
		KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
		SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		return secret;

	}

	// hex representation
	public static String hex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (byte b : bytes) {
			result.append(String.format("%02x", b));
		}
		return result.toString();
	}

	// print hex with block size split
	public static String hexWithBlockSize(byte[] bytes, int blockSize) {

		String hex = hex(bytes);

		// one hex = 2 chars
		blockSize = blockSize * 2;

		// better idea how to print this?
		List<String> result = new ArrayList<>();
		int index = 0;
		while (index < hex.length()) {
			result.add(hex.substring(index, Math.min(index + blockSize, hex.length())));
			index += blockSize;
		}
		return result.toString();

	}

	public static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}


	public static String getNigeriaDate(String dateInString) { 
 
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		try {
			Date date = formatter.parse(dateInString); 

			SimpleDateFormat sdNGN = new SimpleDateFormat(DATE_FORMAT);
			TimeZone tzNGN = TimeZone.getTimeZone("Africa/Lagos");
			sdNGN.setTimeZone(tzNGN);

			String sDateInNGN = sdNGN.format(date); // Convert to String first
        	Date dateInNGN = formatter.parse(sDateInNGN); // Create a new Date object

			return formatter.format(dateInNGN);
		} catch (ParseException e) { 
			return dateInString;
		}
    }

}
