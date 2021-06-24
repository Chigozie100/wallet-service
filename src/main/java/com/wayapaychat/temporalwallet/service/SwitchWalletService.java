package com.wayapaychat.temporalwallet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.entity.SwitchWallet;
import com.wayapaychat.temporalwallet.repository.SwitchWalletRepository;
import com.wayapaychat.temporalwallet.util.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SwitchWalletService {

	@Autowired
	private SwitchWalletRepository switchWalletRepo;
	
	
	public ApiResponse switchWalletOperator(SwitchWallet switchWallet) {
		try {
			List<SwitchWallet> allList = switchWalletRepo.findAll();
			if(allList.size() == 0) {
				if(switchWallet.isMifosWallet() && switchWallet.isTemporalWallet()) {
					return new ApiResponse.Builder<>()
			                .setStatus(false)
			                .setCode(ApiResponse.Code.BAD_CREDENTIALS)
			                .setMessage("Both Wallets cannot be used the same time, select once and continue")
			                .build();
				}
				
				if(!switchWallet.isMifosWallet() && !switchWallet.isTemporalWallet()) {
					return new ApiResponse.Builder<>()
			                .setStatus(false)
			                .setCode(ApiResponse.Code.BAD_CREDENTIALS)
			                .setMessage("select one wallet and continue")
			                .build();
				}
				switchWallet.setSwitchTime(LocalDateTime.now());;
				switchWalletRepo.save(switchWallet);
				return new ApiResponse.Builder<>()
		                .setStatus(true)
		                .setCode(ApiResponse.Code.SUCCESS)
		                .setMessage("Switched successfully")
		                .build();
			} else {
				return switchWalletRepo.findById(allList.get(0).getId()).map(mSwitchWallet -> {
					mSwitchWallet.setMifosWallet(switchWallet.isMifosWallet());
					mSwitchWallet.setSwitchTime(LocalDateTime.now());
					mSwitchWallet.setTemporalWallet(switchWallet.isTemporalWallet());
					switchWalletRepo.save(mSwitchWallet);
					
					return new ApiResponse.Builder<>()
			                .setStatus(true)
			                .setCode(ApiResponse.Code.SUCCESS)
			                .setMessage("Switched Successfully")
			                .build();
				}).orElse(
	                    new ApiResponse.Builder<>()
                        .setStatus(false)
                        .setCode(ApiResponse.Code.NOT_FOUND)
                        .setMessage("Id provided not found")
                        .build()
						);
			}
		} catch (Exception e) {
			log.info(e.getMessage());
			return new ApiResponse.Builder<>()
	                .setStatus(false)
	                .setCode(ApiResponse.Code.UNKNOWN_ERROR)
	                .setMessage("Error Occurred")
	                .build();
		}
	}
}
