package com.wayapaychat.temporalwallet.proxy;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.wayapaychat.temporalwallet.pojo.CardResponse;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name = "${waya.contact.service}", url = "${waya.card.contacturl}", configuration = CardProxy.Configuration.class)
public interface ContactProxy {
	
	@PostMapping("/account/service/send/money/userWallet/to/wallet{senderId}/{senderAcctNo}/{beneficialAcctNo}")
	ResponseEntity<CardResponse> localTransfer(@PathVariable("senderAcctNo") String senderAcctNo, @PathVariable("beneficialAcctNo") String beneficialAcctNo, @PathVariable("senderId") Long senderId, @RequestParam("amount") String amount, @RequestHeader("authorization") String token);
	
	class Configuration {

        @Bean
        Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> converters) {
            return new SpringFormEncoder(new SpringEncoder(converters));
        }
    }

}
