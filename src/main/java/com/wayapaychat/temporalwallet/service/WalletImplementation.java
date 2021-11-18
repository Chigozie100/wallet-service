/*
 * package com.wayapaychat.temporalwallet.service;
 * 
 * import static
 * com.wayapaychat.temporalwallet.util.Constant.WAYA_SETTLEMENT_ACCOUNT_NO;
 * 
 * import java.time.LocalDate; import java.time.LocalDateTime; import
 * java.time.ZoneId; import java.util.ArrayList; import java.util.Date; import
 * java.util.List; import java.util.Optional;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.http.HttpStatus; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.stereotype.Service; import
 * org.springframework.transaction.annotation.Transactional;
 * 
 * import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO; import
 * com.wayapaychat.temporalwallet.entity.Accounts; import
 * com.wayapaychat.temporalwallet.entity.Users; import
 * com.wayapaychat.temporalwallet.enumm.AccountType; import
 * com.wayapaychat.temporalwallet.exception.CustomException; import
 * com.wayapaychat.temporalwallet.pojo.CreateAccountResponse; import
 * com.wayapaychat.temporalwallet.pojo.CreateAccountPojo; import
 * com.wayapaychat.temporalwallet.pojo.CreateWalletResponse; import
 * com.wayapaychat.temporalwallet.pojo.MainWalletResponse; import
 * com.wayapaychat.temporalwallet.pojo.MyData; import
 * com.wayapaychat.temporalwallet.pojo.ResponsePojo; import
 * com.wayapaychat.temporalwallet.pojo.WalletCurrency; import
 * com.wayapaychat.temporalwallet.pojo.WalletStatus; import
 * com.wayapaychat.temporalwallet.pojo.WalletSummary; import
 * com.wayapaychat.temporalwallet.pojo.WalletTimeLine; import
 * com.wayapaychat.temporalwallet.repository.AccountRepository; import
 * com.wayapaychat.temporalwallet.repository.UserRepository; import
 * com.wayapaychat.temporalwallet.security.AuthenticatedUserFacade; import
 * com.wayapaychat.temporalwallet.util.ErrorResponse; import
 * com.wayapaychat.temporalwallet.util.RandomGenerators; import
 * com.wayapaychat.temporalwallet.util.ApiResponse; import
 * com.wayapaychat.temporalwallet.util.Constant;
 * 
 * @Service public class WalletImplementation {
 * 
 * @Autowired UserRepository userRepository;
 * 
 * @Autowired AccountRepository accountRepository;
 * 
 * @Autowired RandomGenerators randomGenerators;
 * 
 * @Autowired AuthUserServiceDAO authService;
 * 
 * private static final Logger LOGGER =
 * LoggerFactory.getLogger(WalletImplementation.class);
 * 
 * @Autowired private AuthenticatedUserFacade userFacade;
 * 
 * 
 * @Transactional public ApiResponse<CreateAccountResponse>
 * createAccount(CreateAccountPojo createWallet) { try { Users us = new Users();
 * us.setCreatedAt(new Date());
 * us.setEmailAddress(createWallet.getEmailAddress());
 * us.setFirstName(createWallet.getFirstName()); us.setId(0L);
 * us.setLastName(createWallet.getLastName());
 * us.setMobileNo(createWallet.getMobileNo()); us.setSavingsProductId(1);
 * us.setUserId(createWallet.getExternalId()); Users mu =
 * userRepository.save(us); Accounts account = new Accounts();
 * account.setUser(mu); account.setProductId(1L); account.setActive(true);
 * account.setApproved(true); account.setDefault(true);
 * account.setClosed(false); // account.setU
 * account.setCode("savingsAccountStatusType.active");
 * account.setValue("Active");
 * account.setAccountName(us.getFirstName()+" "+us.getLastName());
 * 
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10)); Accounts
 * mAccount = accountRepository.save(account); // userRepository.save(user);
 * List<Accounts> userAccount = new ArrayList<>(); userAccount.add(account);
 * mu.setAccounts(userAccount); Users uu = userRepository.save(mu);
 * CreateAccountResponse res = new CreateAccountResponse(us.getId(),
 * us.getEmailAddress(),us.getMobileNo(),mAccount.getId());
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build();
 * 
 * } catch (Exception e) { LOGGER.info("Error::: {}, {} and {}",
 * e.getMessage(),2,3); throw new CustomException(e.getMessage(),
 * HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * @Transactional public ApiResponse createWayaWallet() { try { MyData user =
 * (MyData) userFacade.getAuthentication().getPrincipal(); Optional<Users>
 * mUserx = userRepository.findById(user.getId()); if (!mUserx.isPresent()) {
 * return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); mUser.setEmailAddress(user.getEmail());
 * mUser.setFirstName(user.getFirstName());
 * mUser.setLastName(user.getSurname());
 * mUser.setMobileNo(user.getPhoneNumber()); mUser.setUserId(user.getId());
 * Accounts account = new Accounts();
 * account.setAccountNo(Constant.WAYA_SETTLEMENT_ACCOUNT_NO);
 * account.setAccountType(AccountType.SAVINGS); account.setUser(mUser);
 * account.setBalance(1000000); account.setDefault(false);
 * account.setAccountName("WAYA COMMISSION ACCOUNT");
 * accountRepository.save(account);
 * 
 * // Commission Account Accounts account2 = new Accounts();
 * account2.setAccountNo(Constant.WAYA_COMMISSION_ACCOUNT_NO);
 * account2.setAccountType(AccountType.COMMISSION); account2.setUser(mUser);
 * account2.setBalance(1000000); account2.setAccountName("Waya Commissions");
 * accountRepository.save(account2); return new ApiResponse.Builder<>()
 * .setStatus(true) .setCode(ApiResponse.Code.SUCCESS)
 * .setMessage("Created Successfully") .build(); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * @Transactional public ApiResponse<CreateAccountResponse>
 * createCooperateUserAccount(CreateAccountPojo createWallet) { try {
 * 
 * Optional<Users> mainUser =
 * userRepository.findByEmailAddress(createWallet.getEmailAddress());
 * 
 * if(mainUser.isPresent()) { LOGGER.info("Error::: {}, {} and {}",
 * "User already Exist",2,3); throw new CustomException("User already Exist",
 * HttpStatus.BAD_REQUEST); } Optional<Users> mainPhone =
 * userRepository.findByEmailOrPhoneNumber(createWallet.getMobileNo());
 * if(mainPhone.isPresent()) { LOGGER.info("Error::: {}, {} and {}",
 * "User already Exist",2,3); throw new CustomException("User already Exist",
 * HttpStatus.BAD_REQUEST); } Users us = new Users(); us.setCreatedAt(new
 * Date()); us.setEmailAddress(createWallet.getEmailAddress());
 * us.setFirstName(createWallet.getFirstName()); us.setId(0L);
 * us.setLastName(createWallet.getLastName());
 * us.setMobileNo(createWallet.getMobileNo()); us.setSavingsProductId(1);
 * us.setUserId(createWallet.getExternalId());
 * 
 * Optional<Users> mUser =
 * userRepository.findByEmailAddress(createWallet.getEmailAddress());
 * 
 * if(mUser.isPresent()) { LOGGER.info("Error::: {}, {} and {}",
 * "User already Exist",2,3); throw new CustomException("User already Exist",
 * HttpStatus.BAD_REQUEST); }
 * 
 * Users mu = userRepository.save(us); //Create Cooperate default account
 * Accounts account = new Accounts(); account.setUser(mu);
 * account.setProductId(1L); account.setActive(true); account.setApproved(true);
 * account.setDefault(true); account.setClosed(false); // account.setU
 * account.setCode("savingsAccountStatusType.active");
 * account.setValue("Active");
 * account.setAccountName(us.getFirstName()+" "+us.getLastName());
 * 
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10)); Accounts
 * mAccount = accountRepository.save(account); //Create Cooperate user
 * commission account
 * 
 * Accounts commissionAccount = new Accounts(); commissionAccount.setUser(mu);
 * commissionAccount.setProductId(1L); commissionAccount.setActive(false);
 * commissionAccount.setApproved(false); commissionAccount.setDefault(false);
 * commissionAccount.setClosed(false); // account.setU
 * commissionAccount.setCode("savingsAccountStatusType.active");
 * commissionAccount.setValue("In-Active");
 * commissionAccount.setAccountName(us.getFirstName()+" "+us.getLastName());
 * commissionAccount.setAccountType(AccountType.COMMISSION);
 * commissionAccount.setAccountNo(randomGenerators.generateAlphanumeric(10));
 * accountRepository.save(commissionAccount); //Generate Response
 * CreateAccountResponse res = new CreateAccountResponse(us.getId(),
 * us.getEmailAddress(),us.getMobileNo(),mAccount.getId());
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); } catch (Exception e) { LOGGER.info("Error::: {}, {} and {}",
 * e.getMessage(),2,3); throw new CustomException(e.getMessage(),
 * HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * @Transactional public ApiResponse<CreateWalletResponse> createWallet(Integer
 * productId) { try { // System.out.println(":::::::::Adding wallet::::::::");
 * MyData user = (MyData) userFacade.getAuthentication().getPrincipal();
 * Optional<Users> mUserx = userRepository.findById(user.getId()); if
 * (!mUserx.isPresent()) { return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); mUser.setEmailAddress(user.getEmail());
 * mUser.setFirstName(user.getFirstName());
 * mUser.setLastName(user.getSurname());
 * mUser.setMobileNo(user.getPhoneNumber()); mUser.setUserId(user.getId()); //
 * System.out.println(":::::::usss:::::"); Accounts account = new Accounts();
 * account.setUser(mUser); account.setProductId(Long.valueOf(productId));
 * account.setAccountName(mUser.getFirstName()+" "+mUser.getLastName()); //
 * System.out.println("::::::wallet creation:::::");
 * 
 * account.setActive(true); account.setApproved(true); account.setClosed(false);
 * 
 * account.setCode("savingsAccountStatusType.active");
 * account.setValue("Active");
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10));
 * account.setDefault(false); Accounts mAccount =
 * accountRepository.save(account); //
 * System.out.println("::::::Account Saved:::::"+mAccount.getAccountName()); //
 * userRepository.save(user); List<Accounts> userAccount = new ArrayList<>();
 * userAccount.add(account); mUser.setAccounts(userAccount); Users mUser2 =
 * userRepository.save(mUser); CreateWalletResponse res = new
 * CreateWalletResponse(mUser.getId(),account.getProductId(),Long.valueOf(mUser2
 * .getSavingsProductId()),mAccount.getId());
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); } catch (Exception e) { LOGGER.info("Error::: {}, {} and {}",
 * e.getMessage(),2,3); throw new CustomException(e.getMessage(),
 * HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * public ApiResponse<List<MainWalletResponse>> findWalletByExternalId(Long
 * externalId) { return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(walletResponse(externalId)) .build(); }
 * 
 * public ApiResponse<MainWalletResponse> getUserCommissionList(Long externalId)
 * { return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(getSingleWallet(externalId)) .build(); }
 * 
 * public ApiResponse makeDefaultWallet(long externalId, String accountNo) { try
 * { return userRepository.findById(externalId).map(user -> { Optional<Accounts>
 * account = accountRepository.findByAccountNo(accountNo); if
 * (!account.isPresent()) { //ResponseEntity<>(new
 * ErrorResponse("Invalid Account No"), HttpStatus.BAD_REQUEST) return new
 * ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.INVALID_ACCOUNT) .setMessage("Invalid Account No")
 * .build(); } // Check if account belongs to user if (account.get().getUser()
 * != user){ // return new ResponseEntity<>(new
 * ErrorResponse("Invalid Account Access"), HttpStatus.BAD_REQUEST); return new
 * ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.INVALID_PERMISSION_REQUEST_STATUS)
 * .setMessage("Invalid Account Access") .build(); } // Get Default Wallet
 * Accounts defAccount = accountRepository.findByIsDefaultAndUser(true, user);
 * if (defAccount != null){ defAccount.setDefault(false);
 * accountRepository.save(defAccount); } account.get().setDefault(true);
 * accountRepository.save(account.get()); return new ApiResponse.Builder<>()
 * .setStatus(true) .setCode(ApiResponse.Code.SUCCESS)
 * .setMessage("Default Account set successfully") .build(); }).orElseThrow(()
 * -> new CustomException("Id provided not found",
 * HttpStatus.UNPROCESSABLE_ENTITY)); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * public ApiResponse<MainWalletResponse> getAccountInfo(String accountNum) {
 * try { return accountRepository.findByAccountNo(accountNum).map(accnt -> {
 * MainWalletResponse mainWallet = new MainWalletResponse(); Users user =
 * accnt.getUser(); WalletStatus status = new WalletStatus();
 * status.setActive(accnt.isActive()); status.setApproved(accnt.isApproved());
 * status.setClosed(accnt.isClosed()); status.setCode(accnt.getCode());
 * status.setId(accnt.getId()); status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(user.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(user.getId());
 * mainWallet.setDefaultWallet(accnt.isDefault());
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(mainWallet) .build(); }).orElseThrow(() -> new
 * CustomException("Account Number provided not found",
 * HttpStatus.UNPROCESSABLE_ENTITY)); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * public ApiResponse<Accounts> editAccountName(String accountNo, String
 * newName) { try { return
 * accountRepository.findByAccountNo(accountNo).map(account -> {
 * account.setAccountName(newName); Accounts mAccount =
 * accountRepository.save(account); return new ApiResponse.Builder<>()
 * .setStatus(true) .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(mAccount) .build(); }).orElseThrow(() -> new
 * CustomException("Account Number provided not found",
 * HttpStatus.UNPROCESSABLE_ENTITY)); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * public ApiResponse<List<MainWalletResponse>>
 * getCommissionAccountListByArray(List<Long> ids) { try {
 * List<MainWalletResponse> walletResList = new ArrayList<>(); ids.forEach(id ->
 * { Optional<Users> userx = userRepository.findById(id); if(userx.isPresent())
 * { Users user = userx.get(); Accounts accnt =
 * accountRepository.findByUserAndAccountType(user, AccountType.COMMISSION);
 * MainWalletResponse mainWallet = new MainWalletResponse(); // Users user =
 * accnt.getUser(); WalletStatus status = new WalletStatus();
 * status.setActive(accnt.isActive()); status.setApproved(accnt.isApproved());
 * status.setClosed(accnt.isClosed()); status.setCode(accnt.getCode());
 * status.setId(accnt.getId()); status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(user.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(user.getId());
 * mainWallet.setDefaultWallet(accnt.isDefault());
 * walletResList.add(mainWallet); }
 * 
 * }); return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(walletResList) .build(); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * @Transactional public ApiResponse makeDefaultWallet(Long walletId) { try {
 * //Retrieve Logged in user Details MyData user = (MyData)
 * userFacade.getAuthentication().getPrincipal(); Optional<Users> mUserx =
 * userRepository.findById(user.getId()); if (!mUserx.isPresent()) { return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND)
 * .setMessage("Failed to create default account") .build(); } Users mUser =
 * mUserx.get(); //Get user default Account/wallet Accounts userDefaultAccount =
 * accountRepository.findByUserAndIsDefault(mUser, true); //Check if valid id
 * and set new default wallet, else throw not found error 404 return
 * accountRepository.findById(walletId).map(accnt -> {
 * userDefaultAccount.setDefault(false);
 * accountRepository.save(userDefaultAccount); accnt.setDefault(true);
 * accountRepository.save(accnt); return new ApiResponse.Builder<>()
 * .setStatus(true) .setCode(ApiResponse.Code.SUCCESS)
 * .setMessage("New Default account set successfully") .build();
 * }).orElseThrow(() -> new
 * CustomException("Wallet/Account Id provided is not found",
 * HttpStatus.NOT_FOUND)); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * public ApiResponse<MainWalletResponse> getDefaultWallet() { try { MyData user
 * = (MyData) userFacade.getAuthentication().getPrincipal(); //
 * System.out.println(":::::::::user id::::::"+user.getId()); Optional<Users>
 * mUserx = userRepository.findById(user.getId()); if (!mUserx.isPresent()) {
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); Accounts accnt =
 * accountRepository.findByIsDefaultAndUser(true, mUser); //
 * System.out.println(":::later userId::::"+mUser.get().getId());
 * MainWalletResponse mainWallet = new MainWalletResponse(); WalletStatus status
 * = new WalletStatus(); status.setActive(accnt.isActive());
 * status.setApproved(accnt.isApproved()); status.setClosed(accnt.isClosed());
 * status.setCode(accnt.getCode()); status.setId(accnt.getId());
 * status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(mUser.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(user.getId());
 * mainWallet.setDefaultWallet(accnt.isDefault());
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(mainWallet) .build();
 * 
 * } catch (Exception e) { LOGGER.info("Error::: {}, {} and {}",
 * e.getMessage(),2,3); throw new CustomException(e.getMessage(),
 * HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * public ApiResponse<MainWalletResponse> getDefaultWalletOpen(Long userId) {
 * try { return userRepository.findById(userId).map(mUser -> { Accounts accnt =
 * accountRepository.findByIsDefaultAndUser(true, mUser); //
 * System.out.println(":::later userId::::"+mUser.get().getId());
 * MainWalletResponse mainWallet = new MainWalletResponse(); WalletStatus status
 * = new WalletStatus(); status.setActive(accnt.isActive());
 * status.setApproved(accnt.isApproved()); status.setClosed(accnt.isClosed());
 * status.setCode(accnt.getCode()); status.setId(accnt.getId());
 * status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(mUser.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(mUser.getId());
 * mainWallet.setDefaultWallet(accnt.isDefault());
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(mainWallet) .build(); }).orElseThrow(() -> new
 * CustomException("Id provided not found", HttpStatus.NOT_FOUND));
 * 
 * 
 * } catch (Exception e) { LOGGER.info("Error::: {}, {} and {}",
 * e.getMessage(),2,3); throw new CustomException(e.getMessage(),
 * HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * 
 * public ApiResponse<List<MainWalletResponse>> findAll() { try { List<Accounts>
 * accountList = accountRepository.findAll(); List<MainWalletResponse>
 * walletResList = new ArrayList<>(); for(Accounts accnt : accountList) {
 * MainWalletResponse mainWallet = new MainWalletResponse(); Users user =
 * accnt.getUser(); WalletStatus status = new WalletStatus();
 * status.setActive(accnt.isActive()); status.setApproved(accnt.isApproved());
 * status.setClosed(accnt.isClosed()); status.setCode(accnt.getCode());
 * status.setId(accnt.getId()); status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(user.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(user.getId());
 * mainWallet.setDefaultWallet(accnt.isDefault());
 * walletResList.add(mainWallet); } return new ApiResponse.Builder<>()
 * .setStatus(true) .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(walletResList) .build(); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * private ApiResponse<List<MainWalletResponse>> walletResponse(Long externalId)
 * { try { //citymanjoe //int id = externalId.intValue(); //int userId =
 * authService.getId(id); Long userId =
 * Long.valueOf(authService.getId(externalId.intValue()));
 * List<MainWalletResponse> walletResList = new ArrayList<>(); return
 * userRepository.findById(userId).map(user -> { List<Accounts> accountList =
 * accountRepository.findByUser(user);
 * 
 * 
 * for(Accounts accnt : accountList) { MainWalletResponse mainWallet = new
 * MainWalletResponse(); WalletStatus status = new WalletStatus();
 * status.setActive(accnt.isActive()); status.setApproved(accnt.isApproved());
 * status.setClosed(accnt.isClosed()); status.setCode(accnt.getCode());
 * status.setId(accnt.getId()); status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(user.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(user.getId()); walletResList.add(mainWallet);
 * mainWallet.setDefaultWallet(accnt.isDefault()); } return new
 * ApiResponse.Builder<>() .setStatus(true) .setCode(ApiResponse.Code.SUCCESS)
 * .setMessage("Success") .setData(walletResList) .build(); }).orElseThrow(() ->
 * new CustomException("Id provided not found",
 * HttpStatus.UNPROCESSABLE_ENTITY)); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * private ApiResponse<MainWalletResponse> getSingleWallet(Long externalId) {
 * try { return userRepository.findById(externalId).map(user -> { Accounts accnt
 * = accountRepository.findByUserAndAccountType(user, AccountType.SAVINGS);
 * MainWalletResponse mainWallet = new MainWalletResponse(); WalletStatus status
 * = new WalletStatus(); status.setActive(accnt.isActive());
 * status.setApproved(accnt.isApproved()); status.setClosed(accnt.isClosed());
 * status.setCode(accnt.getCode()); status.setId(accnt.getId());
 * status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(user.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(user.getId());
 * mainWallet.setDefaultWallet(accnt.isDefault());
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(mainWallet) .build();
 * 
 * }).orElseThrow(() -> new CustomException("Id provided not found",
 * HttpStatus.UNPROCESSABLE_ENTITY)); } catch (Exception e) {
 * LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3); throw new
 * CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * 
 * public ApiResponse<List<MainWalletResponse>> allUserWallet() { try {
 * 
 * MyData mUser = (MyData) userFacade.getAuthentication().getPrincipal();
 * List<MainWalletResponse> walletResList = new ArrayList<>(); return
 * userRepository.findById(mUser.getId()).map(user -> { List<Accounts>
 * accountList = accountRepository.findByUser(user);
 * 
 * 
 * for(Accounts accnt : accountList) { MainWalletResponse mainWallet = new
 * MainWalletResponse(); WalletStatus status = new WalletStatus();
 * status.setActive(accnt.isActive()); status.setApproved(accnt.isApproved());
 * status.setClosed(accnt.isClosed()); status.setCode(accnt.getCode());
 * status.setId(accnt.getId()); status.setRejected(accnt.isRejected());
 * status.setSubmittedAndPendingApproval(accnt.isSetSubmittedAndPendingApproval(
 * )); status.setValue(accnt.getValue());
 * status.setWithdrawnByApplicant(accnt.isWithdrawnByApplicant());
 * status.setClosed(accnt.isClosed());
 * 
 * WalletTimeLine timeLine = new WalletTimeLine();
 * timeLine.setSubmittedOnDate(accnt.getCreatedAt().toInstant()
 * .atZone(ZoneId.systemDefault()) .toLocalDate());
 * 
 * WalletCurrency currency = new WalletCurrency(); currency.setCode("NGN");
 * currency.setDecimalPlaces(2);
 * currency.setDisplayLabel("Nigerian Naira [NGN]");
 * currency.setDisplaySymbol(null); currency.setName("Nigerian Naira");
 * currency.setNameCode("currency.NGN");
 * 
 * WalletSummary summary = new WalletSummary();
 * summary.setAccountBalance(accnt.getBalance());
 * summary.setAvailableBalance(accnt.getLagerBalance());
 * summary.setCurrency(currency);
 * 
 * 
 * mainWallet.setAccountNo(accnt.getAccountNo());
 * mainWallet.setClientId(user.getSavingsProductId());
 * mainWallet.setClientName(accnt.getAccountName());
 * mainWallet.setId(accnt.getId());
 * mainWallet.setNominalAnnualInterestRate(0.0);
 * mainWallet.setSavingsProductId(accnt.getProductId());
 * mainWallet.setSavingsProductName(accnt.getAccountType().name());
 * mainWallet.setStatus(status); mainWallet.setSummary(summary);
 * mainWallet.setTimeline(timeLine); mainWallet.setCurrency(currency);
 * mainWallet.setFieldOfficerId(user.getId()); walletResList.add(mainWallet);
 * mainWallet.setDefaultWallet(accnt.isDefault()); }
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(walletResList) .build(); }).orElseThrow(() -> new
 * CustomException("Id provided not found", HttpStatus.UNPROCESSABLE_ENTITY)); }
 * catch (Exception e) { LOGGER.info("Error::: {}, {} and {}",
 * e.getMessage(),2,3); throw new CustomException(e.getMessage(),
 * HttpStatus.UNPROCESSABLE_ENTITY); } }
 * 
 * 
 * public ApiResponse getWayaCommissionWallet() { try { return
 * accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO).map(wayaAccount
 * -> { return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(wayaAccount) .build(); }).orElse( new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.NOT_FOUND)
 * .setMessage("Waya Account not found") .build() ); } catch (Exception e) {
 * return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } }
 * 
 * 
 * }
 */
