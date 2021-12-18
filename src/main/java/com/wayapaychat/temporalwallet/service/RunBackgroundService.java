package com.wayapaychat.temporalwallet.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.entity.WalletAcountVirtual;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.pojo.AccountData;
import com.wayapaychat.temporalwallet.pojo.AccountResponse;
import com.wayapaychat.temporalwallet.proxy.AccountProxy;
import com.wayapaychat.temporalwallet.repository.WalletAcountVirtualRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RunBackgroundService {

	@Autowired
	AccountProxy accountProxy;

	@Autowired
	WalletUserRepository walletUserRepository;
	
	@Autowired
	WalletAcountVirtualRepository walletAcountVirtualRepository;

	//@Scheduled(cron = "*/5 * * * * *")
	/*public void VirtualAccountProcess() {
		// ResponseEntity<AccountResponse> resp = accountProxy.fetchAllVirtualAccount();
		// AccountResponse account = resp.getBody();

		List<WalletUser> user = walletUserRepository.findUserVirtual();
		for (WalletUser mUser : user) {
			ResponseEntity<AccountResponse> resp = accountProxy.fetchVirtualAccount(mUser.getUserId());
			if (resp != null) {
				AccountResponse account = resp.getBody();
				AccountData acct = account.getData();
				if (acct != null) {
					if (!acct.getAccountNumber().isEmpty() && !acct.getAccountNumber().isBlank() && !acct.isDeleted()) {
						log.info("Virtual Account: " + acct.toString());
						WalletAcountVirtual mAccount = new WalletAcountVirtual(acct.getId(), acct.getBankName(),
								acct.getBankCode(), acct.getAccountNumber(), acct.getAccountName(), acct.getUserId(),
								acct.isDeleted());
						walletAcountVirtualRepository.save(mAccount);
						mUser.setVirtualAccount(true);
						walletUserRepository.save(mUser);
					}
				}
			}
		}
	}*/
}
