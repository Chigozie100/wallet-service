package com.wayapaychat.temporalwallet.util;

import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
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
		byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
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
