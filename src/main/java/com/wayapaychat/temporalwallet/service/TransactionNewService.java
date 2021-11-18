/*
 * package com.wayapaychat.temporalwallet.service;
 * 
 * import static
 * com.wayapaychat.temporalwallet.util.Constant.WAYA_SETTLEMENT_ACCOUNT_NO;
 * 
 * import java.util.List; import java.util.Optional;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.data.domain.Page; import
 * org.springframework.data.domain.PageRequest; import
 * org.springframework.data.domain.Pageable; import
 * org.springframework.http.HttpStatus; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.stereotype.Service; import
 * org.springframework.transaction.annotation.Transactional;
 * 
 * import com.wayapaychat.temporalwallet.entity.Accounts; import
 * com.wayapaychat.temporalwallet.entity.Transactions; import
 * com.wayapaychat.temporalwallet.entity.Users; import
 * com.wayapaychat.temporalwallet.enumm.TransactionType; import
 * com.wayapaychat.temporalwallet.exception.CustomException; import
 * com.wayapaychat.temporalwallet.pojo.AdminUserTransferDto; import
 * com.wayapaychat.temporalwallet.pojo.MifosTransactionPojo; import
 * com.wayapaychat.temporalwallet.pojo.MyData; import
 * com.wayapaychat.temporalwallet.pojo.TransactionRequest; import
 * com.wayapaychat.temporalwallet.pojo.TransactionResponse; import
 * com.wayapaychat.temporalwallet.pojo.WalletToWalletDto; import
 * com.wayapaychat.temporalwallet.repository.AccountRepository; import
 * com.wayapaychat.temporalwallet.repository.TransactionRepository; import
 * com.wayapaychat.temporalwallet.repository.UserRepository; import
 * com.wayapaychat.temporalwallet.security.AuthenticatedUserFacade; import
 * com.wayapaychat.temporalwallet.util.ApiResponse; import
 * com.wayapaychat.temporalwallet.util.ErrorResponse; import
 * com.wayapaychat.temporalwallet.util.RandomGenerators;
 * 
 * import lombok.extern.slf4j.Slf4j;
 * 
 * @Service
 * 
 * @Slf4j public class TransactionNewService {
 * 
 * @Autowired UserRepository userRepository;
 * 
 * @Autowired TransactionRepository transactionRepository;
 * 
 * @Autowired AccountRepository accountRepository;
 * 
 * @Autowired RandomGenerators randomGenerators;
 * 
 * @Autowired private AuthenticatedUserFacade userFacade;
 * 
 * 
 * 
 * 
 * public ApiResponse<Page<Transactions>> getWalletTransaction(int page, int
 * size) { try { Pageable paging = PageRequest.of(page, size); MyData user =
 * (MyData) userFacade.getAuthentication().getPrincipal(); //
 * System.out.println(":::::::::user id::::::"+user.getId()); //Users mUser =
 * userRepository.findByUserId(user.getId()); Optional<Users> mUserx =
 * userRepository.findById(user.getId()); if (!mUserx.isPresent()) { return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); // Users user = (Users)
 * userFacade.getAuthentication().getPrincipal(); Accounts accnt =
 * accountRepository.findByIsDefaultAndUser(true, mUser); return new
 * ApiResponse.Builder<>() .setStatus(true) .setCode(ApiResponse.Code.SUCCESS)
 * .setMessage("Success")
 * .setData(transactionRepository.findByAccount(accnt,paging)) .build(); } catch
 * (Exception e) { log.info(e.getMessage()); return new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.UNKNOWN_ERROR)
 * .setMessage("Error Occurred") .build(); } }
 * 
 * public ApiResponse<Page<Transactions>> getWalletTransactionByUser(Long
 * userId, int page, int size) { try { Pageable paging = PageRequest.of(page,
 * size); // MyData user = (MyData)
 * userFacade.getAuthentication().getPrincipal(); //
 * System.out.println(":::::::::user id::::::"+user.getId()); //Users mUser =
 * userRepository.findById(userId); Optional<Users> mUserx =
 * userRepository.findById(userId); if (!mUserx.isPresent()) { return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); // Users user = (Users)
 * userFacade.getAuthentication().getPrincipal(); Accounts accnt =
 * accountRepository.findByIsDefaultAndUser(true, mUser); return new
 * ApiResponse.Builder<>() .setStatus(true) .setCode(ApiResponse.Code.SUCCESS)
 * .setMessage("Success")
 * .setData(transactionRepository.findByAccount(accnt,paging)) .build(); } catch
 * (Exception e) { log.info(e.getMessage()); return new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.UNKNOWN_ERROR)
 * .setMessage("Error Occurred") .build(); } }
 * 
 * public ApiResponse<Page<Transactions>> findAllTransaction(int page, int size)
 * { try { Pageable paging = PageRequest.of(page, size); return new
 * ApiResponse.Builder<>() .setStatus(true) .setCode(ApiResponse.Code.SUCCESS)
 * .setMessage("Success") .setData(transactionRepository.findAll(paging))
 * .build(); } catch (Exception e) { log.info(e.getMessage()); return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } }
 * 
 * public ApiResponse<TransactionRequest> makeTransaction(String command,
 * TransactionRequest request) { try {
 * System.out.println("Making Transaction......."); MyData user = (MyData)
 * userFacade.getAuthentication().getPrincipal();
 * System.out.println(":::::::::user id::::::"+user.getId()); //Users mUser =
 * userRepository.findById(user.getId()); Optional<Users> mUserx =
 * userRepository.findById(user.getId()); if (!mUserx.isPresent()) { return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); Accounts senderAccount =
 * accountRepository.findByUserAndIsDefault(mUser, true); Optional<Accounts>
 * wayaAccount = accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
 * if (senderAccount == null) { throw new CustomException("Invalid Account",
 * HttpStatus.UNPROCESSABLE_ENTITY); } if (request.getAmount() < 1) { throw new
 * CustomException("Invalid amount", HttpStatus.UNPROCESSABLE_ENTITY); }
 * 
 * 
 * 
 * // Register Transaction
 * 
 * String ref = randomGenerators.generateAlphanumeric(12);
 * 
 * if (command == "CREDIT"){
 * 
 * // Handle Credit User Account Transactions transaction = new Transactions();
 * transaction.setTransactionType(command);
 * transaction.setAccount(senderAccount);
 * transaction.setAmount(request.getAmount()); transaction.setRefCode(ref);
 * 
 * transactionRepository.save(transaction);
 * 
 * Double balance = senderAccount.getBalance() + request.getAmount(); Double
 * lagerBalance = senderAccount.getLagerBalance() + request.getAmount();
 * senderAccount.setBalance(balance);
 * senderAccount.setLagerBalance(lagerBalance); List<Transactions>
 * transactionList = senderAccount.getTransactions();
 * transactionList.add(transaction); accountRepository.save(senderAccount);
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("DEBIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(request.getAmount()); transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() -
 * request.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * 
 * TransactionRequest res = new TransactionRequest();
 * res.setAmount(request.getAmount()); //
 * res.setCustomerWalletId(senderAccount.getId());
 * res.setDescription(request.getDescription()); res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build();
 * 
 * } if (command == "DEBIT") { System.out.println("====Debit===="); if
 * (senderAccount.getBalance() < request.getAmount() ) {
 * System.out.println("=====insuficient funds====="); throw new
 * CustomException("Insufficient Balance", HttpStatus.BAD_REQUEST); }
 * 
 * // Handle Debit User Account
 * 
 * Transactions transaction = new Transactions();
 * transaction.setTransactionType("DEBIT");
 * transaction.setAccount(senderAccount);
 * transaction.setAmount(request.getAmount()); transaction.setRefCode(ref);
 * 
 * Double balance = senderAccount.getBalance() - request.getAmount(); Double
 * lagerBalance = senderAccount.getLagerBalance() - request.getAmount();
 * System.out.println("::::Balance after debit:::::"+balance);
 * transactionRepository.save(transaction); senderAccount.setBalance(balance);
 * senderAccount.setLagerBalance(lagerBalance); List<Transactions>
 * transactionList = senderAccount.getTransactions();
 * transactionList.add(transaction); accountRepository.save(senderAccount);
 * 
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("CREDIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(request.getAmount()); transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() +
 * request.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(wayaAccount.get());
 * 
 * TransactionRequest res = new TransactionRequest();
 * res.setAmount(request.getAmount()); //
 * res.setCustomerWalletId(senderAccount.getId());
 * res.setDescription(request.getDescription()); res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); }
 * 
 * log.info("Error Occurred"); return new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.UNKNOWN_ERROR)
 * .setMessage("Error Occurred") .build(); } catch (Exception e) {
 * log.info(e.getMessage()); return new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.UNKNOWN_ERROR)
 * .setMessage("Error Occurred") .build(); } }
 * 
 * @Transactional public ApiResponse<TransactionRequest>
 * walletToWalletTransfer(WalletToWalletDto walletDto, String command) { try {
 * 
 * return
 * accountRepository.findById(walletDto.getCustomerWalletId()).map(account -> {
 * 
 * MyData user = (MyData) userFacade.getAuthentication().getPrincipal();
 * 
 * //Users mUser = userRepository.findById(user.getId()); Optional<Users> mUserx
 * = userRepository.findById(user.getId()); if (!mUserx.isPresent()) { return
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); Accounts senderAccount =
 * accountRepository.findByUserAndIsDefault(mUser, true); Optional<Accounts>
 * wayaAccount = accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
 * String ref = randomGenerators.generateAlphanumeric(12);
 * if(walletDto.getAmount() < 1) { throw new
 * CustomException("Amount Cannot be a negetive value",
 * HttpStatus.UNPROCESSABLE_ENTITY); }
 * 
 * if(command.equals("DEBIT")) { if(senderAccount.getBalance() <
 * walletDto.getAmount()) { throw new CustomException("Insufficient Funds",
 * HttpStatus.UNPROCESSABLE_ENTITY); }
 * 
 * // Handle Debit User Account
 * 
 * Transactions transaction = new Transactions();
 * transaction.setTransactionType("DEBIT"); transaction.setAccount(account);
 * transaction.setAmount(walletDto.getAmount()); transaction.setRefCode(ref);
 * 
 * Double balance = account.getBalance() - walletDto.getAmount(); Double
 * lagerBalance = account.getBalance() - walletDto.getAmount();
 * System.out.println("::::Balance after debit:::::"+balance);
 * transactionRepository.save(transaction);
 * 
 * transactionRepository.save(transaction); account.setBalance(balance);
 * account.setLagerBalance(lagerBalance); List<Transactions> transactionList =
 * account.getTransactions(); transactionList.add(transaction);
 * accountRepository.save(account);
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("CREDIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(walletDto.getAmount()); transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() +
 * walletDto.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(wayaAccount.get());
 * 
 * TransactionRequest res = new TransactionRequest();
 * res.setAmount(walletDto.getAmount().floatValue()); //
 * res.setCustomerWalletId(senderAccount.getId());
 * res.setDescription(walletDto.getDescription()); res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); }
 * 
 * if(command.equals("CREDIT")) {
 * 
 * 
 * 
 * 
 * // Handle cREDIT User Account
 * 
 * Transactions transaction = new Transactions();
 * transaction.setTransactionType("CREDIT"); transaction.setAccount(account);
 * transaction.setAmount(walletDto.getAmount()); transaction.setRefCode(ref);
 * 
 * Double balance = account.getBalance() + walletDto.getAmount(); Double
 * lagerBalance = account.getBalance() + walletDto.getAmount();
 * transactionRepository.save(transaction);
 * 
 * transactionRepository.save(transaction); account.setBalance(balance);
 * account.setLagerBalance(lagerBalance); List<Transactions> transactionList =
 * account.getTransactions(); transactionList.add(transaction);
 * accountRepository.save(account);
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("DEBIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(walletDto.getAmount()); transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() -
 * walletDto.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(wayaAccount.get());
 * 
 * TransactionRequest res = new TransactionRequest();
 * res.setAmount(walletDto.getAmount().floatValue()); //
 * res.setCustomerWalletId(senderAccount.getId());
 * res.setDescription(walletDto.getDescription()); res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); } else { throw new CustomException("Error occurred",
 * HttpStatus.UNPROCESSABLE_ENTITY); }
 * 
 * }).orElse(
 * 
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Id not found") .build() );
 * } catch (Exception e) { log.info(e.getMessage()); return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } }
 * 
 * @Transactional public ApiResponse<TransactionRequest>
 * adminTransferForUser(String command, AdminUserTransferDto adminTranser) { try
 * { Optional<Users> user = userRepository.findById(adminTranser.getUserId());
 * if(user.isPresent()) { return
 * accountRepository.findByIdAndUser(adminTranser.getCustomerWalletId(),
 * user.get()).map(account -> { Optional<Accounts> wayaAccount =
 * accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO); String ref =
 * randomGenerators.generateAlphanumeric(12); if(adminTranser.getAmount() < 1) {
 * throw new CustomException("Amount Cannot be a negetive value",
 * HttpStatus.UNPROCESSABLE_ENTITY); }
 * 
 * if(command.equals("DEBIT")) { if(account.getBalance() <
 * adminTranser.getAmount()) { throw new CustomException("Insufficient Funds",
 * HttpStatus.UNPROCESSABLE_ENTITY); }
 * 
 * // Handle Debit User Account
 * 
 * Transactions transaction = new Transactions();
 * transaction.setTransactionType("DEBIT"); transaction.setAccount(account);
 * transaction.setAmount(adminTranser.getAmount()); transaction.setRefCode(ref);
 * 
 * Double balance = account.getBalance() - adminTranser.getAmount(); Double
 * lagerBalance = account.getBalance() - adminTranser.getAmount();
 * System.out.println("::::Balance after debit:::::"+balance);
 * transactionRepository.save(transaction);
 * 
 * transactionRepository.save(transaction); account.setBalance(balance);
 * account.setLagerBalance(lagerBalance); List<Transactions> transactionList =
 * account.getTransactions(); transactionList.add(transaction);
 * accountRepository.save(account);
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("CREDIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(adminTranser.getAmount());
 * transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() +
 * adminTranser.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(wayaAccount.get());
 * 
 * TransactionRequest res = new TransactionRequest();
 * res.setAmount(adminTranser.getAmount().floatValue()); //
 * res.setCustomerWalletId(senderAccount.getId());
 * res.setDescription(adminTranser.getDescription());
 * res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); }
 * 
 * if(command.equals("CREDIT")) {
 * 
 * // Handle cREDIT User Account
 * 
 * Transactions transaction = new Transactions();
 * transaction.setTransactionType("CREDIT"); transaction.setAccount(account);
 * transaction.setAmount(adminTranser.getAmount()); transaction.setRefCode(ref);
 * 
 * Double balance = account.getBalance() + adminTranser.getAmount(); Double
 * lagerBalance = account.getBalance() + adminTranser.getAmount();
 * System.out.println("::::Balance after debit:::::"+balance);
 * transactionRepository.save(transaction);
 * 
 * transactionRepository.save(transaction); account.setBalance(balance);
 * account.setLagerBalance(lagerBalance); List<Transactions> transactionList =
 * account.getTransactions(); transactionList.add(transaction);
 * accountRepository.save(account);
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("DEBIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(adminTranser.getAmount());
 * transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() -
 * adminTranser.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(wayaAccount.get());
 * 
 * TransactionRequest res = new TransactionRequest();
 * res.setAmount(adminTranser.getAmount().floatValue()); //
 * res.setCustomerWalletId(senderAccount.getId());
 * res.setDescription(adminTranser.getDescription());
 * res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); } else {
 * 
 * return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); }
 * 
 * }).orElse(
 * 
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build() ); } else {
 * 
 * return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("User not found") .build();
 * } } catch (Exception e) { log.info(e.getMessage()); return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } }
 * 
 * @Transactional public ApiResponse<TransactionRequest>
 * transferUserToUser(String command, TransactionRequest request) { try { MyData
 * user = (MyData) userFacade.getAuthentication().getPrincipal();
 * 
 * //Users mUser = userRepository.findById(user.getId()); Optional<Users> mUserx
 * = userRepository.findById(user.getId()); if (!mUserx.isPresent()) { return
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Failed") .build(); } Users
 * mUser = mUserx.get(); Accounts userAccount =
 * accountRepository.findByUserAndIsDefault(mUser, true); Optional<Accounts>
 * wayaAccount = accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
 * if(wayaAccount.isPresent()) { String ref =
 * randomGenerators.generateAlphanumeric(12);
 * 
 * if (request.getAmount() < 1 || request.getAmount() == 0) {
 * 
 * throw new CustomException("Invalid Amount", HttpStatus.BAD_REQUEST); }
 * 
 * 
 * // Handle Debit User Account if (command.equals("DEBIT")) { if
 * (userAccount.getBalance() < request.getAmount()) { throw new
 * CustomException("Insufficient Fund", HttpStatus.BAD_REQUEST); } Transactions
 * transaction = new Transactions(); transaction.setTransactionType("DEBIT");
 * transaction.setAccount(userAccount);
 * transaction.setAmount(request.getAmount()); transaction.setRefCode(ref);
 * 
 * transactionRepository.save(transaction);
 * userAccount.setBalance(userAccount.getBalance() - request.getAmount());
 * userAccount.setLagerBalance(userAccount.getBalance() - request.getAmount());
 * List<Transactions> transactionList = userAccount.getTransactions();
 * transactionList.add(transaction); accountRepository.save(userAccount); //
 * Handle Credit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("CREDIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(request.getAmount()); transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() +
 * request.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(wayaAccount.get());
 * 
 * 
 * //Construct Response TransactionRequest res = new TransactionRequest();
 * res.setAmount(request.getAmount()); //
 * res.setCustomerWalletId(userAccount.getId());
 * res.setDescription(request.getDescription()); res.setPaymentReference(ref);
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); }
 * 
 * 
 * // Handle Credit USER Account if (command.equals("CREDIT")) { Transactions
 * transaction = new Transactions(); transaction.setTransactionType("CREDIT");
 * transaction.setAccount(userAccount);
 * transaction.setAmount(request.getAmount()); transaction.setRefCode(ref);
 * System.out.println(":::Credit:::");
 * System.out.println(":::wallet Id:::"+userAccount.getId());
 * transactionRepository.save(transaction); Double total =
 * userAccount.getBalance() + request.getAmount();
 * System.out.println(":::Total:::"+total); userAccount.setBalance(total);
 * userAccount.setLagerBalance(total); List<Transactions> transactionList =
 * userAccount.getTransactions(); transactionList.add(transaction);
 * accountRepository.save(userAccount);
 * 
 * // Handle Debit Waya Account Transactions transaction3 = new Transactions();
 * transaction3.setTransactionType("DEBIT");
 * transaction3.setAccount(wayaAccount.get());
 * transaction3.setAmount(request.getAmount()); transaction3.setRefCode(ref);
 * 
 * transactionRepository.save(transaction3);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() -
 * request.getAmount()); List<Transactions> transactionList3 =
 * wayaAccount.get().getTransactions(); transactionList3.add(transaction3);
 * accountRepository.save(wayaAccount.get());
 * 
 * //Construct Response TransactionRequest res = new TransactionRequest();
 * res.setAmount(request.getAmount()); //
 * res.setCustomerWalletId(userAccount.getId());
 * res.setDescription(request.getDescription()); res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); }
 * 
 * } else {
 * 
 * return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Invalid User")
 * .build(); }
 * 
 * return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } catch (Exception e) { log.info(e.getMessage()); return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } }
 * 
 * public ApiResponse<Page<Transactions>> getTransactionByWalletId(int page, int
 * size, Long walletId) { try { return
 * accountRepository.findById(walletId).map(account -> { Pageable paging =
 * PageRequest.of(page, size); return new ApiResponse.Builder<>()
 * .setStatus(true) .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(transactionRepository.findByAccount(account,paging)) .build();
 * }).orElse(
 * 
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND)
 * .setMessage("Wallet Id provided not found") .build() ); } catch (Exception e)
 * { log.info(e.getMessage()); return new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.UNKNOWN_ERROR)
 * .setMessage("Error Occurred") .build(); } }
 * 
 * public ApiResponse<Page<Transactions>> getTransactionByType(int page, int
 * size, String transactionType) { try { Pageable paging = PageRequest.of(page,
 * size); return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(transactionRepository.findByTransactionType(transactionType,
 * paging)) .build(); } catch (Exception e) { log.info(e.getMessage()); return
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } }
 * 
 * public ApiResponse<Page<Transactions>> findByAccountNumber(int page, int
 * size, String accountNumber) { try { return
 * accountRepository.findByAccountNo(accountNumber).map(account -> { Pageable
 * paging = PageRequest.of(page, size); return new ApiResponse.Builder<>()
 * .setStatus(true) .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success")
 * .setData(transactionRepository.findByAccount(account, paging)) .build();
 * }).orElse(
 * 
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND)
 * .setMessage("Account Number provided not found") .build() ); } catch
 * (Exception e) { log.info(e.getMessage()); return new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.UNKNOWN_ERROR)
 * .setMessage("Error Occurred") .build(); } }
 * 
 * 
 * //THIS IS THE METHOD RESPONSIBLE FOR HANDLING THE TRANSACTION //CREATED ON
 * 6/24/2021 AFTER SEVERAL MODIFICATION ALL OTHER TRANSACTION //SHOULD BE
 * IGNORED.
 * 
 * @Transactional public ApiResponse makeWalletTransaction(MifosTransactionPojo
 * transactionPojo, String command) { try { if(transactionPojo.getAmount() < 1)
 * { return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.BAD_CREDENTIALS)
 * .setMessage("Wrong Amount, amount cannot be 0 or negetive") .build(); }
 * //Retrieve Account Optional<Accounts> beneficiaryAccount =
 * accountRepository.findById(transactionPojo.getBeneficiaryWalletId());
 * Optional<Accounts> senderAccount =
 * accountRepository.findById(transactionPojo.getCustomerWalletid());
 * Optional<Accounts> wayaAccount =
 * accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
 * if(beneficiaryAccount.isPresent() && senderAccount.isPresent() &&
 * wayaAccount.isPresent()) { String ref =
 * randomGenerators.generateAlphanumeric(12); //HANDLE CREDIT
 * if(command.equals("CREDIT")) {
 * 
 * if(transactionPojo.getAmount() > senderAccount.get().getBalance()) { return
 * new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.BAD_CREDENTIALS)
 * .setMessage("Insufficient Balance") .build(); }
 * 
 * //CREATE TRANSACTION HISTORY Transactions transaction = new Transactions();
 * transaction.setTransactionType("CREDIT");
 * transaction.setAccount(beneficiaryAccount.get());
 * transaction.setAmount(transactionPojo.getAmount());
 * transaction.setRefCode(ref);
 * transaction.setDescription(transactionPojo.getDescription());
 * 
 * transactionRepository.save(transaction);
 * beneficiaryAccount.get().setBalance(beneficiaryAccount.get().getBalance() +
 * transactionPojo.getAmount());
 * beneficiaryAccount.get().setLagerBalance(beneficiaryAccount.get().getBalance(
 * ) + transactionPojo.getAmount()); List<Transactions> transactionList =
 * beneficiaryAccount.get().getTransactions(); transactionList.add(transaction);
 * 
 * beneficiaryAccount.get().setBalance(beneficiaryAccount.get().getBalance() +
 * transactionPojo.getAmount()); // beneficiaryAccount.get().se
 * accountRepository.save(beneficiaryAccount.get());
 * 
 * 
 * // Handle Debit Waya Account Transactions transaction3 = new Transactions();
 * transaction3.setTransactionType("DEBIT");
 * transaction3.setAccount(wayaAccount.get());
 * transaction3.setAmount(transactionPojo.getAmount());
 * transaction3.setRefCode(ref);
 * transaction3.setDescription(transactionPojo.getDescription());
 * 
 * transactionRepository.save(transaction3);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() -
 * transactionPojo.getAmount()); List<Transactions> transactionList3 =
 * wayaAccount.get().getTransactions(); transactionList3.add(transaction3);
 * accountRepository.save(wayaAccount.get());
 * 
 * //////////////////////////////////////////////////////
 * 
 * //CREATE TRANSACTION HISTORY Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("DEBIT");
 * transaction2.setAccount(senderAccount.get());
 * transaction2.setAmount(transactionPojo.getAmount());
 * transaction2.setRefCode(ref);
 * transaction2.setDescription(transactionPojo.getDescription());
 * 
 * transactionRepository.save(transaction2);
 * senderAccount.get().setBalance(senderAccount.get().getBalance() -
 * transactionPojo.getAmount());
 * senderAccount.get().setLagerBalance(senderAccount.get().getBalance() -
 * transactionPojo.getAmount()); List<Transactions> transactionList2 =
 * senderAccount.get().getTransactions(); transactionList2.add(transaction2);
 * senderAccount.get().setBalance(senderAccount.get().getBalance() -
 * transactionPojo.getAmount()); accountRepository.save(senderAccount.get());
 * 
 * // Handle Debit Waya Account Transactions transaction4 = new Transactions();
 * transaction4.setTransactionType("CREDIT");
 * transaction4.setAccount(wayaAccount.get());
 * transaction4.setAmount(transactionPojo.getAmount());
 * transaction4.setRefCode(ref);
 * transaction4.setDescription(transactionPojo.getDescription());
 * 
 * transactionRepository.save(transaction4);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() +
 * transactionPojo.getAmount()); List<Transactions> transactionList4 =
 * wayaAccount.get().getTransactions(); transactionList4.add(transaction4);
 * accountRepository.save(wayaAccount.get());
 * 
 * //Construct Response TransactionRequest res = new TransactionRequest();
 * res.setAmount(transactionPojo.getAmount()); //
 * res.setCustomerWalletId(userAccount.getId());
 * res.setDescription(transactionPojo.getDescription());
 * res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build();
 * 
 * } //HANDLE DEBIT if(command.equals("DEBIT")) { if(transactionPojo.getAmount()
 * > senderAccount.get().getBalance()) { return new ApiResponse.Builder<>()
 * .setStatus(false) .setCode(ApiResponse.Code.BAD_CREDENTIALS)
 * .setMessage("Insufficient Balance") .build(); }
 * 
 * //CREATE TRANSACTION HISTORY Transactions transaction = new Transactions();
 * transaction.setTransactionType("DEBIT");
 * transaction.setAccount(beneficiaryAccount.get());
 * transaction.setAmount(transactionPojo.getAmount());
 * transaction.setRefCode(ref);
 * transaction.setDescription(transactionPojo.getDescription());
 * 
 * transactionRepository.save(transaction);
 * senderAccount.get().setBalance(senderAccount.get().getBalance() -
 * transactionPojo.getAmount());
 * senderAccount.get().setLagerBalance(senderAccount.get().getBalance() -
 * transactionPojo.getAmount()); List<Transactions> transactionList =
 * senderAccount.get().getTransactions(); transactionList.add(transaction);
 * senderAccount.get().setBalance(senderAccount.get().getBalance() -
 * transactionPojo.getAmount()); accountRepository.save(senderAccount.get());
 * 
 * // Handle Debit Waya Account Transactions transaction3 = new Transactions();
 * transaction3.setTransactionType("CREDIT");
 * transaction3.setAccount(wayaAccount.get());
 * transaction3.setAmount(transactionPojo.getAmount());
 * transaction3.setRefCode(ref);
 * transaction3.setDescription(transactionPojo.getDescription());
 * 
 * transactionRepository.save(transaction3);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() +
 * transactionPojo.getAmount()); List<Transactions> transactionList3 =
 * wayaAccount.get().getTransactions(); transactionList3.add(transaction3);
 * accountRepository.save(wayaAccount.get());
 * 
 * //Construct Response TransactionRequest res = new TransactionRequest();
 * res.setAmount(transactionPojo.getAmount()); //
 * res.setCustomerWalletId(userAccount.getId());
 * res.setDescription(transactionPojo.getDescription());
 * res.setPaymentReference(ref);
 * 
 * return new ApiResponse.Builder<>() .setStatus(true)
 * .setCode(ApiResponse.Code.SUCCESS) .setMessage("Success") .setData(res)
 * .build(); } return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND) .setMessage("Wrong command sent")
 * .build(); } else { return new ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.NOT_FOUND)
 * .setMessage("Either the customer wallet id or Beneficiary wallet id not found"
 * ) .build(); } } catch (Exception e) { log.info(e.getMessage()); return new
 * ApiResponse.Builder<>() .setStatus(false)
 * .setCode(ApiResponse.Code.UNKNOWN_ERROR) .setMessage("Error Occurred")
 * .build(); } }
 * 
 * 
 * }
 */
