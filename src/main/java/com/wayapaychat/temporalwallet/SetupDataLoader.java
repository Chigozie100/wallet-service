package com.wayapaychat.temporalwallet;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wayapaychat.temporalwallet.dto.WalletEventAccountDTO;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.service.UserAccountService;

@Component
public class SetupDataLoader{

	boolean alreadySetup = false;

	@Autowired
	WalletEventRepository walletEventRepo;

	@Autowired
	UserAccountService userAccountService;
 
	@EventListener
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (alreadySetup)
			return;

		//Setup Internal account CFID
		userAccountService.setupSystemUser();
		
		List<WalletEventCharges> eventchg = walletEventRepo.findAll();
		if (eventchg != null) {
			for (WalletEventCharges mEvent : eventchg) {
				if (!mEvent.isProcessflg()) {
					WalletEventAccountDTO account = new WalletEventAccountDTO(mEvent.getPlaceholder(),
							mEvent.getCrncyCode(), "OABAS", mEvent.getTranNarration(), "11104",mEvent.getEventId(), "SAVINGS", "SAVINGS ACCOUNT");
					userAccountService.createEventAccount(account);
				}
			}
		}

		//userAccountService.setupExternalCBA();

		alreadySetup = true;

	}

}
