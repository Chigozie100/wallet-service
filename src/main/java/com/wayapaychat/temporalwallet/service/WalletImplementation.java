package com.wayapaychat.temporalwallet.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.Users;
import com.wayapaychat.temporalwallet.enumm.AccountType;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.CreateAccountResponse;
import com.wayapaychat.temporalwallet.pojo.CreateAccountPojo;
import com.wayapaychat.temporalwallet.pojo.CreateWalletResponse;
import com.wayapaychat.temporalwallet.pojo.MainWalletResponse;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.ResponsePojo;
import com.wayapaychat.temporalwallet.pojo.WalletCurrency;
import com.wayapaychat.temporalwallet.pojo.WalletStatus;
import com.wayapaychat.temporalwallet.pojo.WalletSummary;
import com.wayapaychat.temporalwallet.pojo.WalletTimeLine;
import com.wayapaychat.temporalwallet.repository.AccountRepository;
import com.wayapaychat.temporalwallet.repository.UserRepository;
import com.wayapaychat.temporalwallet.security.AuthenticatedUserFacade;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.RandomGenerators;
import com.wayapaychat.temporalwallet.util.Constant;

@Service
public class WalletImplementation {
	
	@Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RandomGenerators randomGenerators;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WalletImplementation.class);
    
    @Autowired
    private AuthenticatedUserFacade userFacade;
    
    
    @Transactional
    public CreateAccountResponse createAccount(CreateAccountPojo createWallet) {
    	try {
    		System.out.println("::::::::::::::account creation:::::::::::");
    		Users us = new Users();
			us.setCreatedAt(new Date());
			us.setEmailAddress(createWallet.getEmailAddress());
			us.setFirstName(createWallet.getFirstName());
			us.setId(0L);
			us.setLastName(createWallet.getLastName());
			us.setMobileNo(createWallet.getMobileNo());
			us.setSavingsProductId(1);
			us.setUserId(createWallet.getExternalId());
			Users mu = userRepository.save(us);
			Accounts account = new Accounts();
	        account.setUser(mu);
	        account.setProductId(1L);
	        account.setActive(true);
            account.setApproved(true);
            account.setDefault(true);
            account.setClosed(false);
//            account.setU
            account.setCode("savingsAccountStatusType.active");
            account.setValue("Active");
	        account.setAccountName(us.getFirstName()+" "+us.getLastName());
	        if(createWallet.getSavingsProductId() == 1) {
            	account.setAccountType(AccountType.SAVINGS);
            }
            if(createWallet.getSavingsProductId() == 2) {
            	account.setAccountType(AccountType.CURRENT);
            }
	        account.setAccountNo(randomGenerators.generateAlphanumeric(10));
	        Accounts mAccount = accountRepository.save(account);
//            userRepository.save(user);
            List<Accounts> userAccount = new ArrayList<>();
            userAccount.add(account);
            mu.setAccounts(userAccount);
            Users uu = userRepository.save(mu);
            CreateAccountResponse res = new CreateAccountResponse(us.getId(), us.getEmailAddress(),us.getMobileNo(),mAccount.getId());
            
			return res;
    		
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    @Transactional
    public ResponsePojo createWayaWallet() {
    	try {
    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
    		Optional<Users> mUser = userRepository.findByUserId(user.getId());
    		mUser.get().setEmailAddress(user.getEmail());
    		mUser.get().setFirstName(user.getFirstName());
    		mUser.get().setLastName(user.getSurname());
    		mUser.get().setMobileNo(user.getPhoneNumber());
    		mUser.get().setUserId(user.getId());
    		Accounts account = new Accounts();
          account.setAccountNo(Constant.WAYA_SETTLEMENT_ACCOUNT_NO);
          account.setAccountType(AccountType.SAVINGS);
          account.setUser(mUser.get());
          account.setBalance(1000000);
          account.setAccountName(user.getFirstName()+" "+user.getSurname());
          accountRepository.save(account);

          // Commission Account
          Accounts account2 = new Accounts();
          account2.setAccountNo(Constant.WAYA_COMMISSION_ACCOUNT_NO);
          account2.setAccountType(AccountType.COMMISSION);
          account2.setUser(mUser.get());
          account2.setBalance(1000000);
          account2.setAccountName("Waya Commissions");
          accountRepository.save(account2);
          return ResponsePojo.response(true, "Created Successfully", HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    @Transactional
    public CreateAccountResponse createCooperateUserAccount(CreateAccountPojo createWallet) {
    	try {
    		System.out.println("::::::::::::::account creation:::::::::::");
    		System.out.println("::::User Email::::"+createWallet.getEmailAddress());
    		System.out.println("::Size:::"+userRepository.findAll().size());
    		Optional<Users> mainUser = userRepository.findByEmailAddress(createWallet.getEmailAddress());
    		userRepository.findAll().forEach(user -> {
    			System.out.println("::::::"+user.getEmailAddress());
    		});
    		if(mainUser.isPresent()) {
    			System.out.println(":::User is present::::");
    			LOGGER.info("Error::: {}, {} and {}", "User already Exist",2,3);
    			throw new CustomException("User already Exist", HttpStatus.BAD_REQUEST);
    		}
    		Users us = new Users();
			us.setCreatedAt(new Date());
			us.setEmailAddress(createWallet.getEmailAddress());
			us.setFirstName(createWallet.getFirstName());
//			us.setId(0L);
			us.setLastName(createWallet.getLastName());
			us.setMobileNo(createWallet.getMobileNo());
			us.setSavingsProductId(1);
			us.setUserId(createWallet.getExternalId());
			Users mu = userRepository.save(us);
			
			//Create Cooperate default account
			Accounts account = new Accounts();
	        account.setUser(mu);
	        account.setProductId(1L);
	        account.setActive(true);
            account.setApproved(true);
            account.setDefault(true);
            account.setClosed(false);
//            account.setU
            account.setCode("savingsAccountStatusType.active");
            account.setValue("Active");
	        account.setAccountName(us.getFirstName()+" "+us.getLastName());
	        if(createWallet.getSavingsProductId() == 1) {
            	account.setAccountType(AccountType.SAVINGS);
            }
            if(createWallet.getSavingsProductId() == 2) {
            	account.setAccountType(AccountType.CURRENT);
            }
	        account.setAccountNo(randomGenerators.generateAlphanumeric(10));
	        Accounts mAccount = accountRepository.save(account);
	        //Create Cooperate user commission account
	        
	        Accounts commissionAccount = new Accounts();
	        commissionAccount.setUser(mu);
	        commissionAccount.setProductId(1L);
	        commissionAccount.setActive(false);
	        commissionAccount.setApproved(false);
	        commissionAccount.setDefault(false);
	        commissionAccount.setClosed(false);
//            account.setU
	        commissionAccount.setCode("savingsAccountStatusType.active");
	        commissionAccount.setValue("Active");
	        commissionAccount.setAccountName(us.getFirstName()+" "+us.getLastName());
	        commissionAccount.setAccountType(AccountType.COMMISSION);
	        commissionAccount.setAccountNo(randomGenerators.generateAlphanumeric(10));
	        accountRepository.save(commissionAccount);
	        //Generate Response
	        CreateAccountResponse res = new CreateAccountResponse(us.getId(), us.getEmailAddress(),us.getMobileNo(),mAccount.getId());
			return res;
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    @Transactional
    public CreateWalletResponse createWallet(Integer productId) {
    	try {
//    		System.out.println(":::::::::Adding wallet::::::::");
    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
    		Optional<Users> mUser = userRepository.findByUserId(user.getId());
    		mUser.get().setEmailAddress(user.getEmail());
    		mUser.get().setFirstName(user.getFirstName());
    		mUser.get().setLastName(user.getSurname());
    		mUser.get().setMobileNo(user.getPhoneNumber());
    		mUser.get().setUserId(user.getId());
//    		System.out.println(":::::::usss:::::");
        	Accounts account = new Accounts();
            account.setUser(mUser.get());
            account.setProductId(Long.valueOf(productId));
            account.setAccountName(mUser.get().getFirstName()+" "+mUser.get().getLastName());
//            System.out.println("::::::wallet creation:::::");
            if(productId == 1) {
            	account.setAccountType(AccountType.SAVINGS);
            }
            if(productId == 2) {
            	account.setAccountType(AccountType.CURRENT);
            }
            
            account.setActive(true);
            account.setApproved(true);
            account.setClosed(false);
            account.setDefault(false);
            account.setCode("savingsAccountStatusType.active");
            account.setValue("Active");
            account.setAccountNo(randomGenerators.generateAlphanumeric(10));
            Accounts mAccount = accountRepository.save(account);
//            System.out.println("::::::Account Saved:::::"+mAccount.getAccountName());
//            userRepository.save(user);
            List<Accounts> userAccount = new ArrayList<>();
            userAccount.add(account);
            mUser.get().setAccounts(userAccount);
            Users mUser2 = userRepository.save(mUser.get());
        	CreateWalletResponse res = new CreateWalletResponse(mUser.get().getId(),account.getProductId(),Long.valueOf(mUser2.getSavingsProductId()),mAccount.getId());
        	return res;
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public List<MainWalletResponse> findWalletByExternalId(Long externalId) {
    	return walletResponse(externalId);
    }
    
    public MainWalletResponse getUserCommissionList(Long externalId) {
    	return getSingleWallet(externalId);
    }
    
    public ResponsePojo makeDefaultWallet(long externalId, String accountNo) {
    	try {
			return userRepository.findById(externalId).map(user -> {
				Optional<Accounts> account = accountRepository.findByAccountNo(accountNo);
		        if (!account.isPresent()) {
		        	//ResponseEntity<>(new ErrorResponse("Invalid Account No"), HttpStatus.BAD_REQUEST)
		            return ResponsePojo.response(true, "Invalid Account No", HttpStatus.BAD_REQUEST.value()) ;
		        }
		     // Check if account belongs to user
		        if (account.get().getUser() != user){
//		            return new ResponseEntity<>(new ErrorResponse("Invalid Account Access"), HttpStatus.BAD_REQUEST);
		        	return ResponsePojo.response(true, "Invalid Account Access", HttpStatus.BAD_REQUEST.value()) ;
		        }
		     // Get Default Wallet
		        Accounts defAccount = accountRepository.findByIsDefaultAndUser(true, user);
		        if (defAccount != null){
		            defAccount.setDefault(false);
		            accountRepository.save(defAccount);
		        }
		        account.get().setDefault(true);
		        accountRepository.save(account.get());
		        return ResponsePojo.response(true, "Default Account set successfully", HttpStatus.OK.value()) ;
			}).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public MainWalletResponse getAccountInfo(String accountNum) {
    	try {
			return accountRepository.findByAccountNo(accountNum).map(accnt -> {
				MainWalletResponse mainWallet = new MainWalletResponse();
				Users user = accnt.getUser();
				WalletStatus status = new WalletStatus();
				status.setActive(accnt.isActive());
				status.setApproved(accnt.isApproved());
				status.setClosed(accnt.isClosed());
				status.setCode(accnt.getCode());
				status.setId(accnt.getId());
				status.setRejected(accnt.isRejected());
				status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval());
				status.setValue(accnt.getValue());
				status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
				status.setClosed(accnt.isClosed());
				
				WalletTimeLine timeLine = new WalletTimeLine();
				timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
					      .atZone(ZoneId.systemDefault())
					      .toLocalDate());
				
				WalletCurrency currency = new WalletCurrency();
				currency.setCode("NGN");
				currency.setDecimalPlaces(2);
				currency.setDisplayLabel("Nigerian Naira [NGN]");
				currency.setDisplaySymbol(null);
				currency.setName("Nigerian Naira");
				currency.setNameCode("currency.NGN");
				
				WalletSummary summary = new WalletSummary();
				summary.setAccountBalance(accnt.getBalance());
				summary.setAvailableBalance(accnt.getLagerBalance());
				summary.setCurrency(currency);
				
				
				mainWallet.setAccountNo(accnt.getAccountNo());
				mainWallet.setClientId(user.getSavingsProductId());
				mainWallet.setClientName(accnt.getAccountName());
				mainWallet.setId(accnt.getId());
				mainWallet.setNominalAnnualInterestRate(0.0);
				mainWallet.setSavingsProductId(accnt.getProductId());
				mainWallet.setSavingsProductName(accnt.getAccountType().name());
				mainWallet.setStatus(status);
				mainWallet.setSummary(summary);
				mainWallet.setTimeline(timeLine);
				mainWallet.setCurrency(currency);
				mainWallet.setFieldOfficerId(user.getId());
				mainWallet.setDefaultWallet(accnt.isDefault());
				
				return mainWallet;
			}).orElseThrow(() -> new CustomException("Account Number provided not found", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public Accounts editAccountName(String accountNo, String newName) {
    	try {
    		return accountRepository.findByAccountNo(accountNo).map(account -> {
    			account.setAccountName(newName);
    			Accounts mAccount = accountRepository.save(account);
    			return mAccount;
    		}).orElseThrow(() -> new CustomException("Account Number provided not found", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public List<MainWalletResponse> getCommissionAccountListByArray(List<Long> ids) {
    	try {
    		List<MainWalletResponse> walletResList = new ArrayList<>();
			ids.forEach(id -> {
				Optional<Users> user = userRepository.findByUserId(id);
				if(user.isPresent()) {
					Accounts accnt = accountRepository.findByUserAndAccountType(user.get(), AccountType.COMMISSION);
					MainWalletResponse mainWallet = new MainWalletResponse();
//					Users user = accnt.getUser();
					WalletStatus status = new WalletStatus();
					status.setActive(accnt.isActive());
					status.setApproved(accnt.isApproved());
					status.setClosed(accnt.isClosed());
					status.setCode(accnt.getCode());
					status.setId(accnt.getId());
					status.setRejected(accnt.isRejected());
					status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval());
					status.setValue(accnt.getValue());
					status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
					status.setClosed(accnt.isClosed());
					
					WalletTimeLine timeLine = new WalletTimeLine();
					timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
						      .atZone(ZoneId.systemDefault())
						      .toLocalDate());
					
					WalletCurrency currency = new WalletCurrency();
					currency.setCode("NGN");
					currency.setDecimalPlaces(2);
					currency.setDisplayLabel("Nigerian Naira [NGN]");
					currency.setDisplaySymbol(null);
					currency.setName("Nigerian Naira");
					currency.setNameCode("currency.NGN");
					
					WalletSummary summary = new WalletSummary();
					summary.setAccountBalance(accnt.getBalance());
					summary.setAvailableBalance(accnt.getLagerBalance());
					summary.setCurrency(currency);
					
					
					mainWallet.setAccountNo(accnt.getAccountNo());
					mainWallet.setClientId(user.get().getSavingsProductId());
					mainWallet.setClientName(accnt.getAccountName());
					mainWallet.setId(accnt.getId());
					mainWallet.setNominalAnnualInterestRate(0.0);
					mainWallet.setSavingsProductId(accnt.getProductId());
					mainWallet.setSavingsProductName(accnt.getAccountType().name());
					mainWallet.setStatus(status);
					mainWallet.setSummary(summary);
					mainWallet.setTimeline(timeLine);
					mainWallet.setCurrency(currency);
					mainWallet.setFieldOfficerId(user.get().getId());
					mainWallet.setDefaultWallet(accnt.isDefault());
					walletResList.add(mainWallet);
				}
				
			});
			return walletResList;
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    @Transactional
    public ResponsePojo makeDefaultWallet(Long walletId) {
    	try {
    		//Retrieve Logged in user Details
    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
    		Optional<Users> mUser = userRepository.findByUserId(user.getId());
    		//Get user default Account/wallet
    		Accounts userDefaultAccount = accountRepository.findByUserAndIsDefault(mUser.get(), true);
    		//Check if valid id and set new default wallet, else throw not found error 404
    		return accountRepository.findById(walletId).map(accnt -> {
    			userDefaultAccount.setDefault(false);
    			accountRepository.save(userDefaultAccount);
    			accnt.setDefault(true);
    			accountRepository.save(accnt);
    			return ResponsePojo.response(false, "New Default account set successfully", HttpStatus.OK.value());
    		}).orElseThrow(() -> new CustomException("Wallet/Account Id provided is not found", HttpStatus.NOT_FOUND));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public MainWalletResponse getDefaultWallet() {
    	try {
    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
//    		System.out.println(":::::::::user id::::::"+user.getId());
    		Optional<Users> mUser = userRepository.findByUserId(user.getId());
    		Accounts accnt = accountRepository.findByIsDefaultAndUser(true, mUser.get());
//    		System.out.println(":::later userId::::"+mUser.get().getId());
			MainWalletResponse mainWallet = new MainWalletResponse();
			WalletStatus status = new WalletStatus();
			status.setActive(accnt.isActive());
			status.setApproved(accnt.isApproved());
			status.setClosed(accnt.isClosed());
			status.setCode(accnt.getCode());
			status.setId(accnt.getId());
			status.setRejected(accnt.isRejected());
			status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval());
			status.setValue(accnt.getValue());
			status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
			status.setClosed(accnt.isClosed());
			
			WalletTimeLine timeLine = new WalletTimeLine();
			timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
				      .atZone(ZoneId.systemDefault())
				      .toLocalDate());
			
			WalletCurrency currency = new WalletCurrency();
			currency.setCode("NGN");
			currency.setDecimalPlaces(2);
			currency.setDisplayLabel("Nigerian Naira [NGN]");
			currency.setDisplaySymbol(null);
			currency.setName("Nigerian Naira");
			currency.setNameCode("currency.NGN");
			
			WalletSummary summary = new WalletSummary();
			summary.setAccountBalance(accnt.getBalance());
			summary.setAvailableBalance(accnt.getLagerBalance());
			summary.setCurrency(currency);
			
			
			mainWallet.setAccountNo(accnt.getAccountNo());
			mainWallet.setClientId(mUser.get().getSavingsProductId());
			mainWallet.setClientName(accnt.getAccountName());
			mainWallet.setId(accnt.getId());
			mainWallet.setNominalAnnualInterestRate(0.0);
			mainWallet.setSavingsProductId(accnt.getProductId());
			mainWallet.setSavingsProductName(accnt.getAccountType().name());
			mainWallet.setStatus(status);
			mainWallet.setSummary(summary);
			mainWallet.setTimeline(timeLine);
			mainWallet.setCurrency(currency);
			mainWallet.setFieldOfficerId(user.getId());
			mainWallet.setDefaultWallet(accnt.isDefault());
			
			return mainWallet;
    		
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public List<MainWalletResponse> findAll() {
    	try {
    		List<Accounts> accountList = accountRepository.findAll();
    		List<MainWalletResponse> walletResList = new ArrayList<>();
    		for(Accounts accnt : accountList) {
				MainWalletResponse mainWallet = new MainWalletResponse();
				Users user = accnt.getUser();
				WalletStatus status = new WalletStatus();
				status.setActive(accnt.isActive());
				status.setApproved(accnt.isApproved());
				status.setClosed(accnt.isClosed());
				status.setCode(accnt.getCode());
				status.setId(accnt.getId());
				status.setRejected(accnt.isRejected());
				status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval());
				status.setValue(accnt.getValue());
				status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
				status.setClosed(accnt.isClosed());
				
				WalletTimeLine timeLine = new WalletTimeLine();
				timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
					      .atZone(ZoneId.systemDefault())
					      .toLocalDate());
				
				WalletCurrency currency = new WalletCurrency();
				currency.setCode("NGN");
				currency.setDecimalPlaces(2);
				currency.setDisplayLabel("Nigerian Naira [NGN]");
				currency.setDisplaySymbol(null);
				currency.setName("Nigerian Naira");
				currency.setNameCode("currency.NGN");
				
				WalletSummary summary = new WalletSummary();
				summary.setAccountBalance(accnt.getBalance());
				summary.setAvailableBalance(accnt.getLagerBalance());
				summary.setCurrency(currency);
				
				
				mainWallet.setAccountNo(accnt.getAccountNo());
				mainWallet.setClientId(user.getSavingsProductId());
				mainWallet.setClientName(accnt.getAccountName());
				mainWallet.setId(accnt.getId());
				mainWallet.setNominalAnnualInterestRate(0.0);
				mainWallet.setSavingsProductId(accnt.getProductId());
				mainWallet.setSavingsProductName(accnt.getAccountType().name());
				mainWallet.setStatus(status);
				mainWallet.setSummary(summary);
				mainWallet.setTimeline(timeLine);
				mainWallet.setCurrency(currency);
				mainWallet.setFieldOfficerId(user.getId());
				mainWallet.setDefaultWallet(accnt.isDefault());
				walletResList.add(mainWallet);
			}
			return walletResList;
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    private List<MainWalletResponse> walletResponse(Long externalId) {
    	try {
    		List<MainWalletResponse> walletResList = new ArrayList<>();
			return userRepository.findByUserId(externalId).map(user -> {
				List<Accounts> accountList = accountRepository.findByUser(user);
				
				
				for(Accounts accnt : accountList) {
					MainWalletResponse mainWallet = new MainWalletResponse();
					WalletStatus status = new WalletStatus();
					status.setActive(accnt.isActive());
					status.setApproved(accnt.isApproved());
					status.setClosed(accnt.isClosed());
					status.setCode(accnt.getCode());
					status.setId(accnt.getId());
					status.setRejected(accnt.isRejected());
					status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval());
					status.setValue(accnt.getValue());
					status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
					status.setClosed(accnt.isClosed());
					
					WalletTimeLine timeLine = new WalletTimeLine();
					timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
						      .atZone(ZoneId.systemDefault())
						      .toLocalDate());
					
					WalletCurrency currency = new WalletCurrency();
					currency.setCode("NGN");
					currency.setDecimalPlaces(2);
					currency.setDisplayLabel("Nigerian Naira [NGN]");
					currency.setDisplaySymbol(null);
					currency.setName("Nigerian Naira");
					currency.setNameCode("currency.NGN");
					
					WalletSummary summary = new WalletSummary();
					summary.setAccountBalance(accnt.getBalance());
					summary.setAvailableBalance(accnt.getLagerBalance());
					summary.setCurrency(currency);
					
					
					mainWallet.setAccountNo(accnt.getAccountNo());
					mainWallet.setClientId(user.getSavingsProductId());
					mainWallet.setClientName(accnt.getAccountName());
					mainWallet.setId(accnt.getId());
					mainWallet.setNominalAnnualInterestRate(0.0);
					mainWallet.setSavingsProductId(accnt.getProductId());
					mainWallet.setSavingsProductName(accnt.getAccountType().name());
					mainWallet.setStatus(status);
					mainWallet.setSummary(summary);
					mainWallet.setTimeline(timeLine);
					mainWallet.setCurrency(currency);
					mainWallet.setFieldOfficerId(user.getId());
					walletResList.add(mainWallet);
					mainWallet.setDefaultWallet(accnt.isDefault());
				}
				return walletResList;
			}).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    private MainWalletResponse getSingleWallet(Long externalId) {
    	try {
    		return userRepository.findById(externalId).map(user -> {
        		Accounts accnt = accountRepository.findByUserAndAccountType(user, AccountType.SAVINGS);
        		MainWalletResponse mainWallet = new MainWalletResponse();
    			WalletStatus status = new WalletStatus();
    			status.setActive(accnt.isActive());
    			status.setApproved(accnt.isApproved());
    			status.setClosed(accnt.isClosed());
    			status.setCode(accnt.getCode());
    			status.setId(accnt.getId());
    			status.setRejected(accnt.isRejected());
    			status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval());
    			status.setValue(accnt.getValue());
    			status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
    			status.setClosed(accnt.isClosed());
    			
    			WalletTimeLine timeLine = new WalletTimeLine();
    			timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
    				      .atZone(ZoneId.systemDefault())
    				      .toLocalDate());
    			
    			WalletCurrency currency = new WalletCurrency();
    			currency.setCode("NGN");
    			currency.setDecimalPlaces(2);
    			currency.setDisplayLabel("Nigerian Naira [NGN]");
    			currency.setDisplaySymbol(null);
    			currency.setName("Nigerian Naira");
    			currency.setNameCode("currency.NGN");
    			
    			WalletSummary summary = new WalletSummary();
    			summary.setAccountBalance(accnt.getBalance());
    			summary.setAvailableBalance(accnt.getLagerBalance());
    			summary.setCurrency(currency);
    			
    			
    			mainWallet.setAccountNo(accnt.getAccountNo());
    			mainWallet.setClientId(user.getSavingsProductId());
    			mainWallet.setClientName(accnt.getAccountName());
    			mainWallet.setId(accnt.getId());
    			mainWallet.setNominalAnnualInterestRate(0.0);
    			mainWallet.setSavingsProductId(accnt.getProductId());
    			mainWallet.setSavingsProductName(accnt.getAccountType().name());
    			mainWallet.setStatus(status);
    			mainWallet.setSummary(summary);
    			mainWallet.setTimeline(timeLine);
    			mainWallet.setCurrency(currency);
    			mainWallet.setFieldOfficerId(user.getId());
    			mainWallet.setDefaultWallet(accnt.isDefault());
    			
    			return mainWallet;
    			
        	}).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    
    public List<MainWalletResponse> allUserWallet() {
    	try {
    		
    		MyData mUser = (MyData)  userFacade.getAuthentication().getPrincipal();
    		List<MainWalletResponse> walletResList = new ArrayList<>();
			return userRepository.findByUserId(mUser.getId()).map(user -> {
				List<Accounts> accountList = accountRepository.findByUser(user);
				
				
				for(Accounts accnt : accountList) {
					MainWalletResponse mainWallet = new MainWalletResponse();
					WalletStatus status = new WalletStatus();
					status.setActive(accnt.isActive());
					status.setApproved(accnt.isApproved());
					status.setClosed(accnt.isClosed());
					status.setCode(accnt.getCode());
					status.setId(accnt.getId());
					status.setRejected(accnt.isRejected());
					status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval());
					status.setValue(accnt.getValue());
					status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
					status.setClosed(accnt.isClosed());
					
					WalletTimeLine timeLine = new WalletTimeLine();
					timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
						      .atZone(ZoneId.systemDefault())
						      .toLocalDate());
					
					WalletCurrency currency = new WalletCurrency();
					currency.setCode("NGN");
					currency.setDecimalPlaces(2);
					currency.setDisplayLabel("Nigerian Naira [NGN]");
					currency.setDisplaySymbol(null);
					currency.setName("Nigerian Naira");
					currency.setNameCode("currency.NGN");
					
					WalletSummary summary = new WalletSummary();
					summary.setAccountBalance(accnt.getBalance());
					summary.setAvailableBalance(accnt.getLagerBalance());
					summary.setCurrency(currency);
					
					
					mainWallet.setAccountNo(accnt.getAccountNo());
					mainWallet.setClientId(user.getSavingsProductId());
					mainWallet.setClientName(accnt.getAccountName());
					mainWallet.setId(accnt.getId());
					mainWallet.setNominalAnnualInterestRate(0.0);
					mainWallet.setSavingsProductId(accnt.getProductId());
					mainWallet.setSavingsProductName(accnt.getAccountType().name());
					mainWallet.setStatus(status);
					mainWallet.setSummary(summary);
					mainWallet.setTimeline(timeLine);
					mainWallet.setCurrency(currency);
					mainWallet.setFieldOfficerId(user.getId());
					walletResList.add(mainWallet);
					mainWallet.setDefaultWallet(accnt.isDefault());
				}
				return walletResList;
			}).orElseThrow(() -> new CustomException("Id provided not found", HttpStatus.UNPROCESSABLE_ENTITY));
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    

}
