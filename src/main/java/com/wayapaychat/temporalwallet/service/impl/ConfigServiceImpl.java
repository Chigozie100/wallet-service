package com.wayapaychat.temporalwallet.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.entity.*;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.RecurrentConfigPojo;
import com.wayapaychat.temporalwallet.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;
import com.wayapaychat.temporalwallet.service.ConfigService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
import com.wayapaychat.temporalwallet.util.SuccessResponse;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

	private final WalletConfigRepository walletConfigRepo;
	private final WalletBankConfigRepository walletBankConfigRepo;
	private final WalletProductCodeRepository walletProductCodeRepo;
	private final WalletProductRepository walletProductRepo;
	private final ParamDefaultValidation paramValidation;
	private final WalletInterestRepository walletInterestRepo;
	private final WalletGLAccountRepository walletGLAccountRepo;
	private final WalletTellerRepository walletTellerRepository;
	private final AuthUserServiceDAO authUserService;
	private final WalletEventRepository walletEventRepository;
	private final WalletTransactionChargeRepository walletTransactionChargeRepository;
	private final RecurrentConfigRepository recurrentConfigRepository;
	private final ChannelProviderRepository channelProviderRepository;

	@Autowired
	public ConfigServiceImpl(WalletConfigRepository walletConfigRepo, WalletBankConfigRepository walletBankConfigRepo, WalletProductCodeRepository walletProductCodeRepo, WalletProductRepository walletProductRepo, ParamDefaultValidation paramValidation, WalletInterestRepository walletInterestRepo, WalletGLAccountRepository walletGLAccountRepo, WalletTellerRepository walletTellerRepository, AuthUserServiceDAO authUserService, WalletEventRepository walletEventRepository, WalletTransactionChargeRepository walletTransactionChargeRepository, RecurrentConfigRepository recurrentConfigRepository, ChannelProviderRepository channelProviderRepository) {
		this.walletConfigRepo = walletConfigRepo;
		this.walletBankConfigRepo = walletBankConfigRepo;
		this.walletProductCodeRepo = walletProductCodeRepo;
		this.walletProductRepo = walletProductRepo;
		this.paramValidation = paramValidation;
		this.walletInterestRepo = walletInterestRepo;
		this.walletGLAccountRepo = walletGLAccountRepo;
		this.walletTellerRepository = walletTellerRepository;
		this.authUserService = authUserService;
		this.walletEventRepository = walletEventRepository;
		this.walletTransactionChargeRepository = walletTransactionChargeRepository;
		this.recurrentConfigRepository = recurrentConfigRepository;
		this.channelProviderRepository = channelProviderRepository;
	}

	@Override
	public ResponseEntity<?> createDefaultCode(WalletConfigDTO configPojo) {
		WalletConfig config = walletConfigRepo.findByCodeName(configPojo.getCodeName());
        if (config != null) {
        	WalletBankConfig bank = new WalletBankConfig(configPojo.getCodeDesc(),configPojo.getCodeValue(),configPojo.getCodeSymbol(),config);
        	walletBankConfigRepo.save(bank);
            return new ResponseEntity<>(new SuccessResponse("Default Code Created Successfully.", bank), HttpStatus.CREATED);
        }
        WalletConfig wallet;
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
		if (wallet.isEmpty()) {
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
		if (config.isEmpty()) {
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
			if(product.isEmpty()) {
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
	
	public ResponseEntity<?> ListAccountProductCode() {
		List<WalletProductCode> wallet = walletProductCodeRepo.findByAllProduct();
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
		slabVersion = version.map(walletInterest -> "0000" + (Integer.parseInt(walletInterest.getInt_version_num()) + 1)).orElse("00001");
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
	public ResponseEntity<?> createdTeller(HttpServletRequest request,WalletTellerDTO teller) {
		boolean validate = paramValidation.validateDefaultCode(teller.getCrncyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		boolean validate2 = paramValidation.validateDefaultCode(teller.getCashAccountCode(),"Batch Account");
		if(!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Batch Account Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		UserDetailPojo user = authUserService.AuthUser(request,(teller.getUserId().intValue()));
		if(!user.isAdmin()) {
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
		if(teller.isEmpty()) {
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
	public ResponseEntity<?> updateEvents(UpdateEventChargeDTO event, Long eventId) {
		boolean validate = paramValidation.validateDefaultCode(event.getCrncyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}

		Optional<WalletEventCharges> walletEventCharges = walletEventRepository.findById(eventId);
		if(walletEventCharges.isEmpty()){
			return new ResponseEntity<>(new ErrorResponse("Event Not Found"), HttpStatus.BAD_REQUEST);
		}
		WalletEventCharges eventCharges = walletEventCharges.get();
		eventCharges.setChargeCustomer(event.isChargeCustomer());
		eventCharges.setChargeWaya(event.isChargeWaya());
		eventCharges.setCrncyCode(event.getCrncyCode());
		eventCharges.setTaxable(event.isTaxable());
		eventCharges.setTaxAmt(event.getTaxAmt());
		eventCharges.setTranAmt(event.getTranAmt());

//
//		WalletEventCharges charge = new WalletEventCharges(event.getEventId(), event.getTranAmt(), event.getPlaceholder(),
//				event.isTaxable(), event.getTaxAmt(), event.getTranNarration(), event.isChargeCustomer(),
//				event.isChargeWaya(), event.getCrncyCode());
		try {
			walletEventRepository.save(eventCharges);
			return new ResponseEntity<>(new SuccessResponse("Event Updated Successfully.", eventCharges), HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> deleteEvent(Long eventId) {
		try {
			Optional<WalletEventCharges> walletEventCharges = walletEventRepository.findById(eventId);
			if(walletEventCharges.isEmpty()){
				return new ResponseEntity<>(new ErrorResponse("Event Not Found"), HttpStatus.BAD_REQUEST);
			}
			WalletEventCharges walletEventCharges1 = walletEventCharges.get();
			walletEventCharges1.setDel_flg(true);
			walletEventCharges1 = walletEventRepository.save(walletEventCharges1);
			return new ResponseEntity<>(new SuccessResponse("Event deleted Successfully.", walletEventCharges1), HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	private ResponseEntity<?> switchMode(ChargeDTO event, ModifyChargeDTO modifyChargeDTO){

		String mode;
		if (event !=null){
			mode = event.getChargePerMode();
		}else {
			mode = modifyChargeDTO.getChargePerMode();
		}


		switch (mode) {
			case "TRANSAC":
			case "MONTHLY":
			case "DAILY":
			case "QUATERLY":
			case "YEARLY":
				break;
			default:
				return new ResponseEntity<>(new ErrorResponse("Charge PER mode doesn't exist"), HttpStatus.BAD_REQUEST);
		}
		return null;
	}

	@Override
	public ResponseEntity<?> createCharge(ChargeDTO event) {
		
		if(event.getFixedAmount() != 0 && event.getFixedPercent() != 0) {
			return new ResponseEntity<>(new ErrorResponse("Both can't be maintained. Maintain either fixed Amount or Percent"), HttpStatus.BAD_REQUEST);
		}

		ResponseEntity<?> responseEntity = switchMode(event, null);
		log.info("switchMode" +responseEntity);
		boolean validate = paramValidation.validateDefaultCode(event.getCurrencyCode(),"Currency");
		if(!validate) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		
		Optional<WalletEventCharges> wCharge = walletEventRepository.findByEventId(event.getChargeEvent());
		if(wCharge.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("Event Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		
		WalletTransactionCharge EventCharge = new WalletTransactionCharge(event.getChargeName(), event.getCurrencyCode(), event.getFixedAmount(), event.getFixedPercent(),
				event.getChargePerMode(), event.isTaxable(), event.getChargeEvent());
		try {
			WalletTransactionCharge wc = walletTransactionChargeRepository.findByChargeName(event.getChargeName());
			if(wc !=null){
				return new ResponseEntity<>(new ErrorResponse("ChargeName Already Exist"), HttpStatus.BAD_REQUEST);
			}
			WalletTransactionCharge charge = walletTransactionChargeRepository.save(EventCharge);
            return new ResponseEntity<>(new SuccessResponse("Transaction Charge Created Successfully.", charge), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}
	
	@Override
	public ResponseEntity<?> updateTranCharge(ModifyChargeDTO event, Long chargeId) {
		
		if(event.getFixedAmount() != 0 && event.getFixedPercent() != 0) {
			return new ResponseEntity<>(new ErrorResponse("Both can't be maintained. Maintain either fixed Amount or Percent"), HttpStatus.BAD_REQUEST);
		}
		ResponseEntity<?> responseEntity = switchMode(null, event);
		log.info("switchMode" +responseEntity);
		Optional<WalletEventCharges> wCharge = walletEventRepository.findByEventId(event.getChargeEvent());
		if(wCharge.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("Event Validation Failed"), HttpStatus.BAD_REQUEST);
		}
		
		Optional<WalletTransactionCharge> EventCharge = walletTransactionChargeRepository.findById(chargeId);
		if(EventCharge.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Charge ID"), HttpStatus.BAD_REQUEST);
		}
		WalletTransactionCharge eventchg = EventCharge.get();
		try {
			eventchg.setChargeEvent(event.getChargeEvent());
			eventchg.setChargePerMode(event.getChargePerMode());
			eventchg.setFixedAmount(event.getFixedAmount());
			eventchg.setFixedPercent(event.getFixedPercent());
			eventchg.setDeleted(event.isTaxable());
			WalletTransactionCharge charge = walletTransactionChargeRepository.save(eventchg);
            return new ResponseEntity<>(new SuccessResponse("Transaction Charge Created Successfully.", charge), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}

	@Override
	public ResponseEntity<?> ListEvents() {
		List<WalletEventCharges> event = walletEventRepository.findByDel_flg(false);
		if(event == null) {
			return new ResponseEntity<>(new ErrorResponse("No Charge exist"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Success", event), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getSingleEvents(Long id) {
		try{
			Optional<WalletEventCharges> walletEventCharges = walletEventRepository.findById(id);
			if(walletEventCharges.isEmpty()){
				return new ResponseEntity<>(new ErrorResponse("Event Not Found"), HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(new SuccessResponse("Success", walletEventCharges.get()), HttpStatus.OK);
		}catch (Exception ex){
			throw new CustomException("error here " + ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}

	}
	
	@Override
	public ResponseEntity<?> ListTranCharge() {
		List<WalletTransactionCharge> event = walletTransactionChargeRepository.findAll();
		if(event.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("No event exist"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Success", event), HttpStatus.OK);
	}
	
	public ResponseEntity<?> findTranCharge(Long id) {
		try {
			Optional<WalletTransactionCharge> charge = walletTransactionChargeRepository.findById(id);
			if(charge.isEmpty()) {
				return new ResponseEntity<>(new ErrorResponse("Invalid Charge Id"), HttpStatus.BAD_REQUEST);
			}
            return new ResponseEntity<>(new SuccessResponse("Success.", charge.get()), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
	}

	@Override
	public ResponseEntity<?> AutoCreateTransAccount(AutoCreateAccount request) {
		try{
			WalletConfigDTO walletConfigDTO = new WalletConfigDTO();
			walletConfigDTO.setCodeDesc(request.getCodeDesc());
			walletConfigDTO.setCodeName(request.getCodeName());
			walletConfigDTO.setCodeSymbol(request.getCodeSymbol());
			walletConfigDTO.setCodeValue(request.getCodeValue());

			ResponseEntity<?> entity = createDefaultCode(walletConfigDTO);
			ResponseEntity<?> responseEntity = null;
			log.info(" ######### FINISH  CREATING createDefaultCode::: " + entity);
			if(entity.getStatusCode().is2xxSuccessful()){
				EventChargeDTO eventChargeDTO = new EventChargeDTO();
				eventChargeDTO.setChargeCustomer(request.isChargeCustomer());
				eventChargeDTO.setChargeWaya(request.isChargeWaya());
				eventChargeDTO.setCrncyCode(request.getCrncyCode());
				eventChargeDTO.setEventId(request.getEventId());
				eventChargeDTO.setPlaceholder(request.getCodeValue());
				eventChargeDTO.setTaxable(false);
				eventChargeDTO.setTaxAmt(BigDecimal.valueOf(0.00));
				eventChargeDTO.setTranAmt(BigDecimal.valueOf(0.00));
				eventChargeDTO.setTranNarration(request.getTranNarration());

				responseEntity = createdEvents(eventChargeDTO);
				log.info(" ######### FINISH CREATING  createdEvents::: " + responseEntity);
			}

			return responseEntity;
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}

	}


	//RecurrentConfigRepository recurrentConfigRepository;
	public ResponseEntity<?> createRecurrentPayment(RecurrentConfigPojo request) {
		try {
			RecurrentConfig recurrentConfig = new RecurrentConfig();
			return getResponseEntity(request, recurrentConfig);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	private ResponseEntity<?> getResponseEntity(RecurrentConfigPojo request, RecurrentConfig recurrentConfig) {
		recurrentConfig.setAmount(request.getAmount());
		recurrentConfig.setOfficialAccountNumber(request.getOfficialAccountNumber());
		recurrentConfig.setPayDate(request.getPayDate());
		recurrentConfig.setDuration(request.getDuration());
		recurrentConfig.setInterval(request.getInterval());
		recurrentConfig = recurrentConfigRepository.save(recurrentConfig);
		return new ResponseEntity<>(new SuccessResponse("Success.", recurrentConfig), HttpStatus.CREATED);
	}

	public ResponseEntity<?> updateRecurrentPayment(RecurrentConfigPojo request, Long id) {
		try {
			Optional<RecurrentConfig> recurrentConfig = recurrentConfigRepository.findById(id);
			if (recurrentConfig.isEmpty()){
				throw new CustomException("Record not Found !!", HttpStatus.BAD_REQUEST);
			}
				RecurrentConfig recurrentConfig1 = recurrentConfig.get();
			return getResponseEntity(request, recurrentConfig1);
		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> toggleRecurrentPayment(Long id) {
		try {
			Optional<RecurrentConfig> recurrentConfig = recurrentConfigRepository.findById(id);
			if (recurrentConfig.isEmpty()){
				throw new CustomException("Record not Found !!", HttpStatus.BAD_REQUEST);
			}
			RecurrentConfig recurrentConfig1 = recurrentConfig.get();
			recurrentConfig1.setActive(!recurrentConfig1.isActive());
			recurrentConfig1 = recurrentConfigRepository.save(recurrentConfig1);

			return new ResponseEntity<>(new SuccessResponse("Success.", recurrentConfig1), HttpStatus.CREATED);
		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> getAllRecurrentPayment() {
		try {
			List<RecurrentConfig> recurrentConfig = recurrentConfigRepository.findAll();
			if (recurrentConfig.isEmpty()){
				throw new CustomException("Record not Found !!", HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(new SuccessResponse("Success.", recurrentConfig), HttpStatus.CREATED);
		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}


	public ResponseEntity<?> getRecurrentPayment(Long id) {
		try {
			Optional<RecurrentConfig> recurrentConfig = recurrentConfigRepository.findById(id);
			if (recurrentConfig.isEmpty()){
				throw new CustomException("Record not Found !!", HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(new SuccessResponse("Success.", recurrentConfig.get()), HttpStatus.CREATED);
 		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> createChannel(String name) {
		try {
			channelProviderRepository.findByName(name).orElseThrow(()-> new CustomException("Record already exist", HttpStatus.BAD_REQUEST));

			ChannelProvider channelProvider = new ChannelProvider();
			channelProvider.setActive(false);
			channelProvider.setName(name);
			channelProviderRepository.save(channelProvider);
			return new ResponseEntity<>(new SuccessResponse("Success.", channelProvider), HttpStatus.CREATED);
		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> updateChannel(String name, Long id) {
		try {
			ChannelProvider channelProvider1 = channelProviderRepository.findById(id).orElseThrow(()-> new CustomException("Record already exist", HttpStatus.BAD_REQUEST));
			channelProvider1.setName(name);
			return new ResponseEntity<>(new SuccessResponse("Success.", channelProviderRepository.save(channelProvider1)), HttpStatus.CREATED);
		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> toggleChannel(Long id) {
		try {
			ChannelProvider channelProvider1 = channelProviderRepository.findById(id).orElseThrow(()-> new CustomException("Record already exist", HttpStatus.BAD_REQUEST));
			channelProvider1.setActive(!channelProvider1.isActive());
			channelProvider1 = channelProviderRepository.save(channelProvider1);

			return new ResponseEntity<>(new SuccessResponse("Success.", channelProvider1), HttpStatus.CREATED);
		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ChannelProvider findActiveChannel() {
		try {
			ChannelProvider channelProvider1 = channelProviderRepository.findByActive().orElseThrow(()-> new CustomException("Record not found", HttpStatus.NOT_FOUND));
			return channelProvider1;
		} catch (Exception e) {
			throw new CustomException("", HttpStatus.BAD_REQUEST);
		}
	}


}

