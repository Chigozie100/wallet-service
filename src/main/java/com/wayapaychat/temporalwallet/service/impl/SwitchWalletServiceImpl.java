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
import com.wayapaychat.temporalwallet.entity.SwitchWallet;
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

	@Override
	public ResponseEntity<?> ListAllSwitches() {
		List<SwitchWallet> checkSwitch = switchWalletRepository.findAll();
		return new ResponseEntity<>(new SuccessResponse("SWITCH TOGGLE", checkSwitch), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> GetSwitch(String ident) {
		List<SwitchWallet> checkSwitch = switchWalletRepository.findBySwitchIdent(ident);
		if (checkSwitch.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("IDENTITY DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
		}
		try {
			List<SwitchWallet> walletSwt = checkSwitch;
			return new ResponseEntity<>(new SuccessResponse("SWITCH TOGGLE", walletSwt), HttpStatus.OK);
		} catch (Exception e) {
			log.error("UNABLE TO DELETE: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> UpdateSwitche(ToggleSwitchDTO toggle) {
		
		Optional<SwitchWallet> checkSwitchPrev = switchWalletRepository.findBySwitchCode(toggle.getPrevSwitchCode());
		if (!checkSwitchPrev.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("PREVIOUS SWITCH DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
		}
		Optional<SwitchWallet> checkSwitchNew = switchWalletRepository.findBySwitchCode(toggle.getNewSwitchCode());
		if (!checkSwitchPrev.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("NEW SWITCH DOES NOT EXIST"), HttpStatus.BAD_REQUEST);
		}
		try {
			SwitchWallet walletSwtPrev = checkSwitchPrev.get();
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
			return new ResponseEntity<>(new SuccessResponse("TOGGLE SWITCH", checkNew.get()), HttpStatus.OK);
		} catch (Exception e) {
			log.error("UNABLE TO CREATE: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> DeleteSwitches(Long id) {
		Optional<SwitchWallet> checkSwitchExist = switchWalletRepository.findById(id);
		if (checkSwitchExist.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to duplicate switch code"), HttpStatus.BAD_REQUEST);
		}
		SwitchWallet walletSwt = checkSwitchExist.get();
		try {
			switchWalletRepository.delete(walletSwt);
			return new ResponseEntity<>(new SuccessResponse("SWITCH CODE DELETED", walletSwt), HttpStatus.OK);
		} catch (Exception e) {
			log.error("UNABLE TO DELETE: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> CreateWalletOperator(CreateSwitchDTO offno) {
		Optional<SwitchWallet> checkSwitchExist = switchWalletRepository.findBySwitchCode(offno.getSwitchCode());
		if (checkSwitchExist.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to duplicate switch code"), HttpStatus.BAD_REQUEST);
		}
		try {
			SwitchWallet walletSwt = new SwitchWallet(LocalDateTime.now(), offno.getSwitchIdentity(),
					offno.getSwitchCode());
			switchWalletRepository.saveAndFlush(walletSwt);
			return new ResponseEntity<>(new SuccessResponse("SWITCH CODE CREATED", walletSwt), HttpStatus.OK);
		} catch (Exception e) {
			log.error("UNABLE TO CREATE: {}", e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

}
