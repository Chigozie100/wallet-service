package com.wayapaychat.temporalwallet.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dto.CreateSwitchDTO;
import com.wayapaychat.temporalwallet.dto.ToggleSwitchDTO;
import com.wayapaychat.temporalwallet.entity.Provider;
import com.wayapaychat.temporalwallet.entity.SwitchWallet;
import com.wayapaychat.temporalwallet.repository.ProviderRepository;
import com.wayapaychat.temporalwallet.repository.SwitchWalletRepository;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.SuccessResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SwitchWalletServiceImpl implements SwitchWalletService {

	@Autowired
	SwitchWalletRepository switchWalletRepository;

	@Autowired
	ProviderRepository providerRepository;

	@Override
	public ResponseEntity<?> ListAllSwitches() {
		log.info("Fetching all switch wallets");
		List<SwitchWallet> checkSwitch = switchWalletRepository.findAll();
		log.info("Switch wallets retrieved successfully");
		return new ResponseEntity<>(new SuccessResponse("SWITCH TOGGLE", checkSwitch), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> GetSwitch(String ident) {
		log.info("Fetching switch wallet with identity: {}", ident);
		List<SwitchWallet> checkSwitch = switchWalletRepository.findBySwitchIdent(ident);
		if (checkSwitch.isEmpty()) {
			log.error("Identity {} does not exist", ident);
			return new ResponseEntity<>(new ErrorResponse("IDENTITY DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
		}
		try {
			log.info("Switch wallet with identity {} retrieved successfully", ident);
			return new ResponseEntity<>(new SuccessResponse("SWITCH TOGGLE", checkSwitch), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while fetching switch wallet: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> UpdateSwitche(ToggleSwitchDTO toggle) {
		log.info("Updating switch with new switch code: {}", toggle.getNewSwitchCode());
		Optional<SwitchWallet> checkSwitchNew = switchWalletRepository.findBySwitchCode(toggle.getNewSwitchCode());
		if (!checkSwitchNew.isPresent()) {
			log.error("New switch code {} does not exist", toggle.getNewSwitchCode());
			return new ResponseEntity<>(new ErrorResponse("NEW SWITCH DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
		}
		SwitchWallet walletSwt = null;
		List<SwitchWallet> checkSwitchPrev = switchWalletRepository.findBySwitchIdent(checkSwitchNew.get().getSwitchIdentity());
		if (!checkSwitchPrev.isEmpty()) {
			for(SwitchWallet checkSwt : checkSwitchPrev) {
				if(!checkSwt.getSwitchCode().equalsIgnoreCase(checkSwitchNew.get().getSwitchCode())) {
					walletSwt = checkSwt;
				}
			}
		}
		try {
			SwitchWallet walletSwtPrev = walletSwt;
			if(walletSwtPrev.getSwitchCodeTime() != null) {
				walletSwtPrev.setLastSwitchTime(walletSwtPrev.getSwitchCodeTime());
			}
			walletSwtPrev.setSwitched(false);
			switchWalletRepository.saveAndFlush(walletSwtPrev);

			SwitchWallet walletSwtNew = checkSwitchNew.get();
			walletSwtNew.setSwitched(true);
			walletSwtNew.setSwitchCodeTime(LocalDateTime.now());
			switchWalletRepository.saveAndFlush(walletSwtNew);
			Optional<SwitchWallet> checkNew = switchWalletRepository.findBySwitchCode(toggle.getNewSwitchCode());
			log.info("Switch updated successfully with new switch code: {}", toggle.getNewSwitchCode());
			return new ResponseEntity<>(new SuccessResponse("TOGGLE SWITCH", checkNew.get()), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while updating switch: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> DeleteSwitches(Long id) {
		log.info("Deleting switch with ID: {}", id);
		Optional<SwitchWallet> checkSwitchExist = switchWalletRepository.findById(id);
		if (!checkSwitchExist.isPresent()) {
			log.error("Switch with ID {} does not exist", id);
			return new ResponseEntity<>(new ErrorResponse("Unable to duplicate switch code"), HttpStatus.BAD_REQUEST);
		}
		SwitchWallet walletSwt = checkSwitchExist.get();
		try {
			switchWalletRepository.delete(walletSwt);
			log.info("Switch with ID {} deleted successfully", id);
			return new ResponseEntity<>(new SuccessResponse("SWITCH CODE DELETED", walletSwt), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while deleting switch: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> CreateWalletOperator(CreateSwitchDTO offno) {
		log.info("Creating switch wallet operator with switch code: {}", offno.getSwitchCode());
		Optional<SwitchWallet> checkSwitchExist = switchWalletRepository.findBySwitchCode(offno.getSwitchCode());
		if (checkSwitchExist.isPresent()) {
			log.error("Unable to create switch wallet operator. Duplicate switch code: {}", offno.getSwitchCode());
			return new ResponseEntity<>(new ErrorResponse("Unable to duplicate switch code"), HttpStatus.BAD_REQUEST);
		}
		try {
			SwitchWallet walletSwt = new SwitchWallet(LocalDateTime.now(), offno.getSwitchIdentity(),
					offno.getSwitchCode());
			switchWalletRepository.saveAndFlush(walletSwt);
			log.info("Switch wallet operator created successfully with switch code: {}", offno.getSwitchCode());
			return new ResponseEntity<>(new SuccessResponse("SWITCH CODE CREATED", walletSwt), HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error occurred while creating switch wallet operator: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> getProvider() {
		log.info("Fetching all providers");
		try {
			List<Provider> providers = providerRepository.findAll();
			log.info("Providers fetched successfully");
			return new ResponseEntity<>(new SuccessResponse("LIST PROVIDERS", providers), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("Error occurred while fetching providers: {}", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> enableProvider(long providerId) {
		log.info("Enabling provider with ID: {}", providerId);
		if (providerId < 0L) {
			log.error("Invalid provider ID provided: {}", providerId);
			return new ResponseEntity<>(new ErrorResponse("Please provide a valid provider ID"), HttpStatus.BAD_REQUEST);
		}

		Provider provider = providerRepository.findById(providerId).orElse(null);

		if (provider == null) {
			log.error("Provider with ID {} not found", providerId);
			return new ResponseEntity<>(new ErrorResponse("providerId not found"), HttpStatus.BAD_REQUEST);
		}
		try {
			List<Provider> providers = providerRepository.findAll();

			for (Provider mprovider : providers) {
				if (mprovider.getId() == providerId) {
					mprovider.setActive(true);
				} else {
					mprovider.setActive(false);
				}
				providerRepository.save(provider);
			}
			log.info("Provider with ID {} enabled successfully", providerId);
			return new ResponseEntity<>(new SuccessResponse("PROVIDER ENABLED", null), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("Error occurred while enabling provider: {}", ex.getMessage());
			return new ResponseEntity<>(new ErrorResponse(ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public Provider getActiveProvider() {
		log.info("Fetching active provider");
		List<Provider> providers = providerRepository.findByIsActive(true);
		Provider provider = new Provider();
		if (providers.size() > 0) {
			provider = providers.get(0);
			log.info("Active provider fetched successfully");
			return provider;
		}
		log.info("No active provider found");
		return null;
	}

}
