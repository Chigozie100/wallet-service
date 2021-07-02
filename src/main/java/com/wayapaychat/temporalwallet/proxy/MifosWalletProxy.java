package com.wayapaychat.temporalwallet.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.wayapaychat.temporalwallet.config.WalletClientConfiguration;
import com.wayapaychat.temporalwallet.pojo.CreateAccountPojo;
import com.wayapaychat.temporalwallet.pojo.MifosTransactionPojo;
import com.wayapaychat.temporalwallet.util.ApiResponse;

@FeignClient(name = "${waya.wallet.mifos}", url = "${waya.wallet.mifosurl}", configuration = WalletClientConfiguration.class)
public interface MifosWalletProxy {

	//OPEN ENDPOINT, MIFOS ACCOUNT CREATION
	
	@PostMapping("/registration")
	public ApiResponse register(CreateAccountPojo createWallet);
	
	//WALLET API
	
	@PostMapping("/api/v1/wallets")
	public ApiResponse createWallet(@RequestParam("productId") int productId, @RequestParam("autoApproved") boolean autoApproved);
	
	@GetMapping("/api/v1/wallets")
	public ApiResponse getWallet(@RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallets/{id}")
	public ApiResponse getWalletById(@PathVariable("id") Long id, @RequestHeader("authorization") String token);
	
	@PutMapping("/api/v1/wallets/{id}")
	public ApiResponse editWallet(@PathVariable("id") Long id, @RequestParam("command") String command, @RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallets/{id}/transactions")
	public ApiResponse findWalletTransactions(@PathVariable("id") Long id, @RequestHeader("authorization") String token);
	
	
	//WALLET TRANSACTIONS
	
	@PostMapping("/api/v1/wallet-transactions")
	public ApiResponse walletTransaction(@RequestBody MifosTransactionPojo mifosTransactionPojo, @RequestParam("command") String command, @RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallet-transactions/{id}")
	public ApiResponse getTransactionById(@PathVariable("id") Long id, @RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallet-transactions/name-enquiry")
	public ApiResponse getByAccountNumber(@RequestParam("accountNo") String accountNo, @RequestHeader("authorization") String token);
}
