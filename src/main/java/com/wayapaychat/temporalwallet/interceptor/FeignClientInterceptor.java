package com.wayapaychat.temporalwallet.interceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.wayapaychat.temporalwallet.dto.SecuritySwitch;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.enumm.ProviderType;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

	@Value("${waya.service.keytemporal}")
	private String keytemporal;

	@Value("${waya.service.keymifos}")
	private String keymifos;

	@Value("${waya.service.keysecret}")
	private String keysecret;

	@Autowired
	private SwitchWalletService switchWalletService;

	/*
	 * private static final String AUTHORIZATION_HEADER = "Authorization";
	 * 
	 * public static String getBearerTokenHeader() { return
	 * ((ServletRequestAttributes)
	 * RequestContextHolder.getRequestAttributes()).getRequest().getHeader(
	 * "Authorization"); }
	 * 
	 * @Override public void apply(RequestTemplate requestTemplate) {
	 * requestTemplate.header(AUTHORIZATION_HEADER, getBearerTokenHeader());
	 * 
	 * }
	 */

	private static final String AUTHORIZATION_HEADER = "Authorization";

	public static String getBearerTokenHeader() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) attrs).getRequest().getHeader("Authorization");
		}
		return null;
	}

	@Override
	public void apply(RequestTemplate requestTemplate) {
		/*
		Provider provider = switchWalletService.getActiveProvider();
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
		String strDate = formatter.format(date);
		switch (provider.getName()) {
		case ProviderType.MAINMIFO:
			String secretDate = SecuritySwitch.decrypt((SecuritySwitch.decodeKey(keymifos)), keysecret);
			System.out.println("Decryption Value = " + secretDate);
			String[] keyDecrypt = secretDate.split(Pattern.quote(","));
			String keyDate = keyDecrypt[0];
			String[] keyval = keyDate.split(Pattern.quote(":"));
			String compareKey = keyval[1];
			if ((Integer.parseInt(strDate)) > (Integer.parseInt(compareKey))) {
				throw new CustomException("migration checksum mismatch", HttpStatus.UNPROCESSABLE_ENTITY);
			}
		case ProviderType.TEMPORAL:
			secretDate = SecuritySwitch.decrypt((SecuritySwitch.decodeKey(keytemporal)), keysecret);
			System.out.println("Decryption Value = " + secretDate);
			keyDecrypt = secretDate.split(Pattern.quote(","));
			keyDate = keyDecrypt[0];
			keyval = keyDate.split(Pattern.quote(":"));
			compareKey = keyval[1];
			if ((Integer.parseInt(strDate)) > (Integer.parseInt(compareKey))) {
				throw new CustomException("migration checksum mismatch", HttpStatus.UNPROCESSABLE_ENTITY);
			}
		default:

		}
        */
		String token = getBearerTokenHeader();
		if (token != null && !token.isBlank()) {
			requestTemplate.header(AUTHORIZATION_HEADER, token);
		}
	}
}