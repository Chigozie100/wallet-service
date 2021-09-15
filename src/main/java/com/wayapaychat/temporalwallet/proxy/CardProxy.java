package com.wayapaychat.temporalwallet.proxy;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.wayapaychat.temporalwallet.pojo.CardPojo;
import com.wayapaychat.temporalwallet.pojo.CardResponse;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;


@FeignClient(name = "${waya.card.service}", url = "${waya.card.cardurl}", configuration = CardProxy.Configuration.class)
public interface CardProxy {
	
	//@PostMapping("/card/charge")
	//@Headers("Content-Type: application/x-www-form-urlencoded")
	//ResponseEntity<CardResponse> cardPayment(@RequestParam("amount") String amount, @RequestParam("cardNumber") String cardNumber, @RequestParam("ref") String ref, @RequestParam("userId") String userId, @RequestParam("walletAccountNo") String walletAccountNo, @RequestHeader("authorization") String token);
	@PostMapping(value = "/card/charge", consumes = APPLICATION_FORM_URLENCODED_VALUE)
	ResponseEntity<CardResponse> cardPayment(@RequestBody Map<String, ?> form, @RequestHeader("authorization") String token);
	
	@PostMapping("/bankAccount/payWithCheckOut/{userId}")
	ResponseEntity<CardResponse> payCheckOut(@RequestBody CardPojo cardPojo,@PathVariable("userId") Long userId, @RequestHeader("authorization") String token);
	
	class Configuration {

        @Bean
        Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
            return new SpringFormEncoder(new SpringEncoder(converters));
        }
    }
}
