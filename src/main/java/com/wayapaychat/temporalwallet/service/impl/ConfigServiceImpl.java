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
import com.wayapaychat.temporalwallet.dto.ProductDTO;
import com.wayapaychat.temporalwallet.dto.WalletConfigDTO;
import com.wayapaychat.temporalwallet.entity.WalletBankConfig;
import com.wayapaychat.temporalwallet.entity.WalletConfig;
import com.wayapaychat.temporalwallet.entity.WalletProduct;
import com.wayapaychat.temporalwallet.entity.WalletProductCode;
import com.wayapaychat.temporalwallet.repository.WalletBankConfigRepository;
import com.wayapaychat.temporalwallet.repository.WalletConfigRepository;
import com.wayapaychat.temporalwallet.repository.WalletProductCodeRepository;
import com.wayapaychat.temporalwallet.repository.WalletProductRepository;
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
	WalletProductCodeRepository WalletProductCodeRepo;
	
	@Autowired
	WalletProductRepository WalletProductRepo;
	
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
		boolean validate = paramValidation.validateDefaultCode(product.getCurrencyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		boolean validate2 = paramValidation.validateDefaultCode(product.getProductType(),"Product Type");
		if(!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Product Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		WalletProductCode prodx = new WalletProductCode(product.getProductCode(), product.getProductName(), product.getProductType(),
				product.getCurrencyCode());
		try {
			WalletProductCodeRepo.save(prodx);
            return new ResponseEntity<>(new SuccessResponse("Product Code Created Successfully.", prodx), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> ListProduct() {
		try {
			List<WalletProductCode> product = WalletProductCodeRepo.findAll();
            return new ResponseEntity<>(new SuccessResponse("Success.", product), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> findProduct(Long id) {
		try {
			Optional<WalletProductCode> product = WalletProductCodeRepo.findById(id);
			if(!product.isPresent()) {
				return new ResponseEntity<>(new ErrorResponse("Invalid Product Id"), HttpStatus.BAD_REQUEST);
			}
            return new ResponseEntity<>(new SuccessResponse("Success.", product.get()), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> getProduct(String schm) {
		try {
			WalletProductCode product = WalletProductCodeRepo.findByProductCode(schm);
            return new ResponseEntity<>(new SuccessResponse("Success.", product), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> ListProductCode() {
		List<WalletProductCode> wallet = WalletProductCodeRepo.findAll();
		return new ResponseEntity<>(new SuccessResponse("Success", wallet), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createProductParameter(ProductDTO product) {
		boolean validate = paramValidation.validateDefaultCode(product.getFrequency(),"Frequency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Frequency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		boolean validate2 = paramValidation.validateDefaultCode(product.getInterestCode(),"Interest");
		if(!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Interest Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		WalletProductCode xyz = WalletProductCodeRepo.findByProductCode(product.getProductCode());
		WalletProduct prodx = new WalletProduct(product.getProductCode(), xyz.getProductName(), product.isSysGenerate(),
				xyz.getProductType(), product.isPaidInterest(), product.isCollectInterest(),product.isStaffEnabled(), 
				product.getFrequency(), product.isPaidCommission(), xyz.getCurrencyCode(), 9999999.99, 
				9999999.99, 9999999.99, 9999999.99, product.getInterestCode(), product.getProductMinBalance());
		try {
			WalletProductRepo.save(prodx);
            return new ResponseEntity<>(new SuccessResponse("Product Parameter Created Successfully.", prodx), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
}

