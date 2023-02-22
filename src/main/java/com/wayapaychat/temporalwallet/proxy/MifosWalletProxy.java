package com.wayapaychat.temporalwallet.proxy;

import com.wayapaychat.temporalwallet.dto.ExternalCBAAccountDetailsResponse;
import com.wayapaychat.temporalwallet.dto.ExternalCBAResponse;
import com.wayapaychat.temporalwallet.dto.MifosTransaction;
import com.wayapaychat.temporalwallet.dto.MifosTransfer;
import com.wayapaychat.temporalwallet.pojo.MifosBlockAccount;
import com.wayapaychat.temporalwallet.pojo.CreateAccountData;
import com.wayapaychat.temporalwallet.response.ExternalCBAAccountCreationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.wayapaychat.temporalwallet.config.WalletClientConfiguration;
import com.wayapaychat.temporalwallet.pojo.CreateAccountPojo;
import com.wayapaychat.temporalwallet.pojo.MifosTransactionPojo;
import com.wayapaychat.temporalwallet.response.ApiResponse;

@FeignClient(name = "MIFOS-WALLET-SERVICE", url = "${waya.wallet.mifosurl}", configuration = WalletClientConfiguration.class)
public interface MifosWalletProxy {

	//OPEN ENDPOINT, MIFOS ACCOUNT CREATION
	
	@PostMapping("/registration")
	ApiResponse register(CreateAccountPojo createWallet);
	
	//WALLET API
	
	@PostMapping("/api/v1/wallets")
	ApiResponse createWallet(@RequestParam("productId") int productId, @RequestParam("autoApproved") boolean autoApproved);
	
	@GetMapping("/api/v1/wallets")
	ApiResponse getWallet(@RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallets/{id}")
	ApiResponse getWalletById(@PathVariable("id") Long id, @RequestHeader("authorization") String token);
	
	@PutMapping("/api/v1/wallets/{id}")
	ApiResponse editWallet(@PathVariable("id") Long id, @RequestParam("command") String command, @RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallets/{id}/transactions")
	ApiResponse findWalletTransactions(@PathVariable("id") Long id, @RequestHeader("authorization") String token);
	
	
	//WALLET TRANSACTIONS
	
	@PostMapping("/api/v1/wallet-transactions")
	ApiResponse walletTransaction(@RequestBody MifosTransactionPojo mifosTransactionPojo, @RequestParam("command") String command, @RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallet-transactions/{id}")
	ApiResponse getTransactionById(@PathVariable("id") Long id, @RequestHeader("authorization") String token);
	
	@GetMapping("/api/v1/wallet-transactions/name-enquiry")
	ApiResponse getByAccountNumber(@RequestParam("accountNo") String accountNo, @RequestHeader("authorization") String token);

	@PostMapping("/wallet/create/user")
	ExternalCBAAccountCreationResponse createAccount(@RequestBody CreateAccountData reAccount);

	@DeleteMapping("/wallet/account/block")
	ApiResponse<?> blockAccount(@RequestHeader("authorization") String token, @RequestBody MifosBlockAccount reAccount);

	@PutMapping("/wallet/account/unblock")
	ApiResponse<?> unblockAccount(@RequestHeader("authorization") String token, @RequestBody MifosBlockAccount reAccount);

	@GetMapping("/wallet/inquiry/balance/{accountNumber}")
	ExternalCBAAccountDetailsResponse fetchBalance(@PathVariable("accountNumber") String accountNumber);

	@PostMapping("/wallet/transfer")
	ExternalCBAResponse transferMoney(@RequestBody MifosTransfer transfer);

	@PostMapping("/wallet/glEntry")
    ExternalCBAResponse applyGLEntry(MifosTransfer mifosTransfer);

	@PostMapping("/wallet/transaction")
    ExternalCBAResponse processTransaction(MifosTransaction mifosTransfer);

}
