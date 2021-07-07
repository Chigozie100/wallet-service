package com.wayapaychat.temporalwallet.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dto.ProductCodeDTO;
import com.wayapaychat.temporalwallet.dto.WalletConfigDTO;
import com.wayapaychat.temporalwallet.entity.WalletBankConfig;
import com.wayapaychat.temporalwallet.entity.WalletConfig;
import com.wayapaychat.temporalwallet.repository.WalletBankConfigRepository;
import com.wayapaychat.temporalwallet.repository.WalletConfigRepository;
import com.wayapaychat.temporalwallet.service.ConfigService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
import com.wayapaychat.temporalwallet.util.SuccessResponse;

@Service
public class ConfigServiceImpl implements ConfigService {
	
	
	@Autowired
	WalletConfigRepository walletConfigRepo;
	
	@Autowired
	WalletBankConfigRepository WalletBankConfigRepo;
	
	@Autowired
	ParamDefaultValidation paramValidation;

	@Override
	public ResponseEntity<?> createDefaultCode(WalletConfigDTO configPojo) {
		WalletConfig config = walletConfigRepo.findByCodeName(configPojo.getCodeName());
        if (config != null) {
        	WalletBankConfig bank = new WalletBankConfig(configPojo.getCodeDesc(),configPojo.getCodeValue(),configPojo.getCodeSymbol(),config);
            WalletBankConfigRepo.save(bank);
            return new ResponseEntity<>(new SuccessResponse("Default Code Created Successfully.", bank), HttpStatus.CREATED);
        }
        WalletConfig wallet = null;
        WalletBankConfig bank = new WalletBankConfig(configPojo.getCodeDesc(),configPojo.getCodeValue(),configPojo.getCodeSymbol());
        Collection<WalletBankConfig> bankConfig = new ArrayList<>();
        bankConfig.add(bank);
        wallet = new WalletConfig(configPojo.getCodeName(), bankConfig);
        try {
        	walletConfigRepo.save(wallet);
            return new ResponseEntity<>(new SuccessResponse("Default Code Created Successfully.", wallet), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}

	@Override
	public ResponseEntity<?> getListDefaultCode() {
		List<WalletConfig> wallet = walletConfigRepo.findAll();
		return new ResponseEntity<>(new SuccessResponse("Success", wallet), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getListCodeValue(Long id) {
		Optional<WalletBankConfig> wallet = WalletBankConfigRepo.findById(id);
		if (!wallet.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Code Id"), HttpStatus.BAD_REQUEST);
        }
		return new ResponseEntity<>(new SuccessResponse("Success", wallet.get()), HttpStatus.OK);
	}
	
	public ResponseEntity<?> getAllCodeValue(String name) {
		WalletBankConfig wallet = WalletBankConfigRepo.findByCodeValue(name);
		if (wallet == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Code Id"), HttpStatus.BAD_REQUEST);
        }
		return new ResponseEntity<>(new SuccessResponse("Success", wallet), HttpStatus.OK);
	}
	
	public ResponseEntity<?> getCode(Long id) {
		Optional<WalletConfig> config = walletConfigRepo.findById(id);
		if (!config.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Code Id"), HttpStatus.BAD_REQUEST);
        }
		List<WalletBankConfig> wallet = WalletBankConfigRepo.findByConfig(config.get());
		if (wallet == null) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Code Id"), HttpStatus.BAD_REQUEST);
        }
		return new ResponseEntity<>(new SuccessResponse("Success", wallet), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createProduct(ProductCodeDTO product) {
		boolean validate = paramValidation.validateDefaultCode(product.getCurrencyCode());
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		boolean validate2 = paramValidation.validateDefaultCode(product.getProductType());
		if(!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Product Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		
		return null;
	}
}

