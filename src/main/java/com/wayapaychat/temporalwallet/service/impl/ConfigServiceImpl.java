package com.wayapaychat.temporalwallet.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dto.AccountGLDTO;
import com.wayapaychat.temporalwallet.dto.EventChargeDTO;
import com.wayapaychat.temporalwallet.dto.InterestDTO;
import com.wayapaychat.temporalwallet.dto.ProductCodeDTO;
import com.wayapaychat.temporalwallet.dto.ProductDTO;
import com.wayapaychat.temporalwallet.dto.WalletConfigDTO;
import com.wayapaychat.temporalwallet.dto.WalletTellerDTO;
import com.wayapaychat.temporalwallet.entity.WalletBankConfig;
import com.wayapaychat.temporalwallet.entity.WalletConfig;
import com.wayapaychat.temporalwallet.entity.WalletEventCharges;
import com.wayapaychat.temporalwallet.entity.WalletGLAccount;
import com.wayapaychat.temporalwallet.entity.WalletInterest;
import com.wayapaychat.temporalwallet.entity.WalletProduct;
import com.wayapaychat.temporalwallet.entity.WalletProductCode;
import com.wayapaychat.temporalwallet.entity.WalletTeller;
import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;
import com.wayapaychat.temporalwallet.repository.WalletBankConfigRepository;
import com.wayapaychat.temporalwallet.repository.WalletConfigRepository;
import com.wayapaychat.temporalwallet.repository.WalletEventRepository;
import com.wayapaychat.temporalwallet.repository.WalletGLAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletInterestRepository;
import com.wayapaychat.temporalwallet.repository.WalletProductCodeRepository;
import com.wayapaychat.temporalwallet.repository.WalletProductRepository;
import com.wayapaychat.temporalwallet.repository.WalletTellerRepository;
import com.wayapaychat.temporalwallet.service.ConfigService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
import com.wayapaychat.temporalwallet.util.SuccessResponse;

@Service
public class ConfigServiceImpl implements ConfigService {
	
	
	@Autowired
	WalletConfigRepository walletConfigRepo;
	
	@Autowired
	WalletBankConfigRepository walletBankConfigRepo;
	
	@Autowired
	WalletProductCodeRepository walletProductCodeRepo;
	
	@Autowired
	WalletProductRepository walletProductRepo;
	
	@Autowired
	ParamDefaultValidation paramValidation;
	
	@Autowired
	WalletInterestRepository walletInterestRepo;
	
	@Autowired
	WalletGLAccountRepository walletGLAccountRepo;
	
	@Autowired
	WalletTellerRepository walletTellerRepository;
	
	@Autowired
	AuthUserServiceDAO authUserService;
	
	@Autowired
	WalletEventRepository walletEventRepository;

	@Override
	public ResponseEntity<?> createDefaultCode(WalletConfigDTO configPojo) {
		WalletConfig config = walletConfigRepo.findByCodeName(configPojo.getCodeName());
        if (config != null) {
        	WalletBankConfig bank = new WalletBankConfig(configPojo.getCodeDesc(),configPojo.getCodeValue(),configPojo.getCodeSymbol(),config);
        	walletBankConfigRepo.save(bank);
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
		Optional<WalletBankConfig> wallet = walletBankConfigRepo.findById(id);
		if (!wallet.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("Invalid Code Id"), HttpStatus.BAD_REQUEST);
        }
		return new ResponseEntity<>(new SuccessResponse("Success", wallet.get()), HttpStatus.OK);
	}
	
	public ResponseEntity<?> getAllCodeValue(String name) {
		WalletBankConfig wallet = walletBankConfigRepo.findByCodeValue(name);
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
		List<WalletBankConfig> wallet = walletBankConfigRepo.findByConfig(config.get());
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
		WalletGLAccount validate3 = walletGLAccountRepo.findByGlSubHeadCode(product.getGlSubHeadCode());
		if(!validate3.isEntity_cre_flg()) {
			return new ResponseEntity<>(new ErrorResponse("GL Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		if(!validate3.getCrncyCode().equals(product.getCurrencyCode())) {
			return new ResponseEntity<>(new ErrorResponse("GL Code currency mismatch"), HttpStatus.BAD_REQUEST);
		}
		WalletProductCode prodx = new WalletProductCode(product.getProductCode(), product.getProductName(), product.getProductType(),
				product.getCurrencyCode(), product.getGlSubHeadCode());
		try {
			walletProductCodeRepo.save(prodx);
            return new ResponseEntity<>(new SuccessResponse("Product Code Created Successfully.", prodx), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> ListProduct() {
		try {
			List<WalletProductCode> product = walletProductCodeRepo.findAll();
            return new ResponseEntity<>(new SuccessResponse("Success.", product), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> findProduct(Long id) {
		try {
			Optional<WalletProductCode> product = walletProductCodeRepo.findById(id);
			if(!product.isPresent()) {
				return new ResponseEntity<>(new ErrorResponse("Invalid Product Id"), HttpStatus.BAD_REQUEST);
			}
            return new ResponseEntity<>(new SuccessResponse("Success.", product.get()), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> getProduct(String schm, String gl) {
		try {
			WalletProductCode product = walletProductCodeRepo.findByProductGLCode(schm,gl);
            return new ResponseEntity<>(new SuccessResponse("Success.", product), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> ListProductCode() {
		List<WalletProductCode> wallet = walletProductCodeRepo.findAll();
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
		
		WalletProductCode xyz = walletProductCodeRepo.findByProductGLCode(product.getProductCode(), product.getGlCode());
		if(xyz == null) {
			return new ResponseEntity<>(new ErrorResponse("ProductCode and GLCode Does Not Exist"), HttpStatus.BAD_REQUEST);
		}
		WalletProduct prodx = new WalletProduct(product.getProductCode(), xyz.getProductName(), product.isSysGenerate(),
				xyz.getProductType(), product.isPaidInterest(), product.isCollectInterest(),product.isStaffEnabled(), 
				product.getFrequency(), product.isPaidCommission(), xyz.getCurrencyCode(), 9999999999.99, 
				9999999999.99, 9999999999.99, 9999999999.99, product.getInterestCode(), 
				product.getProductMinBalance(),product.isChqAllowedFlg(),product.getGlCode());
		try {
			walletProductRepo.save(prodx);
            return new ResponseEntity<>(new SuccessResponse("Product Parameter Created Successfully.", prodx), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> createInterestParameter(InterestDTO intL) {
		boolean validate = paramValidation.validateDefaultCode(intL.getCrncyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		boolean validate2 = paramValidation.validateDefaultCode(intL.getInterestCode(),"Interest");
		if(!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Interest Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		if(intL.isCreditInterest() && intL.isDebitInterest()) {
			return new ResponseEntity<>(new ErrorResponse("Both Interest Slab Can't be the same"), HttpStatus.BAD_REQUEST);
		}
		if(!intL.isCreditInterest() && !intL.isDebitInterest()) {
			return new ResponseEntity<>(new ErrorResponse("Both Interest Slab Can't be the same"), HttpStatus.BAD_REQUEST);
		}
		String slabValue = null, slabVersion;
		if(intL.isCreditInterest()) {
			slabValue = "C";
		}else if(intL.isDebitInterest()) {
			slabValue = "D";
		}
		Optional<WalletInterest> version = walletInterestRepo.findByIntTblCodeIgnoreCase(intL.getInterestCode());
		if(version.isPresent()) {
			slabVersion = "0000" + (Integer.parseInt(version.get().getInt_version_num()) + 1);
		}else {
			slabVersion = "00001";
		}
		if(intL.getEndSlabAmt() <= intL.getBeginSlabAmt()) {
			return new ResponseEntity<>(new ErrorResponse("End amount slab can't be equal to zero or less than begin amount slab"), HttpStatus.BAD_REQUEST);
		}
		WalletInterest intx = new WalletInterest(intL.getCrncyCode(), intL.getInterestCode(), slabVersion, 
				intL.getIntRatePcnt(), slabValue, intL.getBeginSlabAmt(),
				intL.getEndSlabAmt(), intL.getPenalIntPcnt());
		try {
			walletInterestRepo.save(intx);
            return new ResponseEntity<>(new SuccessResponse("Interest Slab Created Successfully.", intx), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}

	@Override
	public ResponseEntity<?> createParamCOA(AccountGLDTO chat) {
		boolean validate = paramValidation.validateDefaultCode(chat.getCrncyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		WalletGLAccount account = new WalletGLAccount("0000",chat.getGlName(),chat.getGlCode(), chat.getGlSubHeadCode(), chat.getCrncyCode());
		try {
			walletGLAccountRepo.save(account);
            return new ResponseEntity<>(new SuccessResponse("Account GL Created Successfully.", account), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	public ResponseEntity<?> ListCOA() {
		List<WalletGLAccount> wallet = walletGLAccountRepo.findAll();
		return new ResponseEntity<>(new SuccessResponse("Success", wallet), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createdTeller(WalletTellerDTO teller) {
		boolean validate = paramValidation.validateDefaultCode(teller.getCrncyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		boolean validate2 = paramValidation.validateDefaultCode(teller.getCashAccountCode(),"Batch Account");
		if(!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Batch Account Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		UserDetailPojo user = authUserService.AuthUser((teller.getUserId().intValue()));
		if(!user.is_admin()) {
			return new ResponseEntity<>(new ErrorResponse("User Admin Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		WalletTeller account = new WalletTeller(teller.getSolId(), teller.getCrncyCode(), teller.getUserId(), teller.getCashAccountCode());
		try {
			walletTellerRepository.save(account);
            return new ResponseEntity<>(new SuccessResponse("Teller Till Created Successfully.", account), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}

	@Override
	public ResponseEntity<?> ListTellersTill() {
		List<WalletTeller> teller = walletTellerRepository.findAll();
		if(teller == null) {
			return new ResponseEntity<>(new ErrorResponse("No teller till exist"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Success", teller), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createdEvents(EventChargeDTO event) {
		boolean validate = paramValidation.validateDefaultCode(event.getCrncyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		boolean validate2 = paramValidation.validateDefaultCode(event.getPlaceholder(),"Batch Account");
		if(!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Placeholder Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		
		WalletEventCharges charge = new WalletEventCharges(event.getEventId(), event.getTranAmt(), event.getPlaceholder(),
				event.isTaxable(), event.getTaxAmt(), event.getTranNarration(), event.isChargeCustomer(), 
				event.isChargeWaya(), event.getCrncyCode());
		try {
			walletEventRepository.save(charge);
            return new ResponseEntity<>(new SuccessResponse("Event Created Successfully.", charge), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}

	@Override
	public ResponseEntity<?> ListEvents() {
		List<WalletEventCharges> event = walletEventRepository.findAll();
		if(event == null) {
			return new ResponseEntity<>(new ErrorResponse("No event exist"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Success", event), HttpStatus.OK);
	}
}

