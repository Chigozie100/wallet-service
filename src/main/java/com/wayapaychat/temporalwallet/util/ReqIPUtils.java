package com.wayapaychat.temporalwallet.util;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class ReqIPUtils {

	private final String[] IP_HEADER_CANDIDATES = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
			"HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
			"HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" };

	public final String getClientIP(HttpServletRequest request) {
		String ip = null;
		if (request == null) {
			if (RequestContextHolder.getRequestAttributes() == null) {
				return null;
			}
			request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		}

		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!ObjectUtils.isEmpty(ip))
			return ip;

		for (String header : IP_HEADER_CANDIDATES) {
			String ipList = request.getHeader(header);
			if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
				return ipList.split(",")[0];
			}
		}
		return request.getRemoteAddr();
	}

	public Integer getAccountNo() {
		Random r = new Random();
		return r.nextInt((10000000 - 0) + 1) + 0;
	}

	private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
	private static final int TAG_LENGTH_BIT = 128;
	private static final int IV_LENGTH_BYTE = 12;
	private static final int AES_KEY_BIT = 256;

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	// AES-GCM needs GCMParameterSpec
	public static byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

		Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
		cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
		byte[] encryptedText = cipher.doFinal(pText);
		return encryptedText;

	}

	public static byte[] encryptWithPrefixIV(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

		byte[] cipherText = encrypt(pText, secret, iv);

		byte[] cipherTextWithIv = ByteBuffer.allocate(iv.length + cipherText.length).put(iv).put(cipherText).array();
		return cipherTextWithIv;

	}

	public static String decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

		Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
		cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
		byte[] plainText = cipher.doFinal(cText);
		return new String(plainText, UTF_8);

	}

	public static String decryptWithPrefixIV(byte[] cText, SecretKey secret) throws Exception {

		ByteBuffer bb = ByteBuffer.wrap(cText);

		byte[] iv = new byte[IV_LENGTH_BYTE];
		bb.get(iv);
		// bb.get(iv, 0, iv.length);

		byte[] cipherText = new byte[bb.remaining()];
		bb.get(cipherText);

		String plainText = decrypt(cipherText, secret, iv);
		return plainText;

	}

	public String FinEncrypt(String pText) throws Exception {
		SecretKey secretKey = CryptoUtils.getAESKey(AES_KEY_BIT);
		byte[] iv = CryptoUtils.getRandomNonce(IV_LENGTH_BYTE);
		byte[] encryptedText = ReqIPUtils.encryptWithPrefixIV(pText.getBytes(UTF_8), secretKey, iv);
		return CryptoUtils.hex(encryptedText);
	}
	
	public String WayaEncrypt(String pText) throws Exception {
		String authHash = Base64Utils.encodeToString(pText.getBytes(StandardCharsets.UTF_8));
		return authHash;
	}

	public String FinDecrypt(String encryptText) throws Exception {
		byte[] encryptedText = CryptoUtils.hexStringToByteArray(encryptText);
		System.out.println(CryptoUtils.hex(encryptedText));
		SecretKey secretKey = CryptoUtils.getAESKey(AES_KEY_BIT);
		String decryptedText = ReqIPUtils.decryptWithPrefixIV(encryptedText, secretKey);
		return decryptedText;
	}
	
	public String WayaDecrypt(String encryptText) throws Exception {
		byte[] asBytes = Base64Utils.decodeFromString(encryptText);
		String output = new String(asBytes);
		return output;
	}

	public String encrypt(String input) throws NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		SecretKey key = generateKey(128);
		IvParameterSpec iv = generateIv();
		String algorithm = "AES/CBC/PKCS5Padding";

		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] cipherText = cipher.doFinal(input.getBytes());
		return Base64.getEncoder().encodeToString(cipherText);
	}

	public String decrypt(String cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		SecretKey key = generateKey(128);
		IvParameterSpec iv = generateIv();
		String algorithm = "AES/CBC/PKCS5Padding";

		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] encryptBytes = Base64.getDecoder().decode(cipherText);
		byte[] plainText = cipher.doFinal(encryptBytes);

		return new String(plainText);
	}

	public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(n);
		SecretKey key = keyGenerator.generateKey();
		return key;
	}

	public static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}



}
