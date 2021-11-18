
/*
 * package com.wayapaychat.temporalwallet.service.impl;
 * 
 * import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO; import
 * com.wayapaychat.temporalwallet.entity.Accounts; import
 * com.wayapaychat.temporalwallet.entity.Users; import
 * com.wayapaychat.temporalwallet.enumm.AccountType; import
 * com.wayapaychat.temporalwallet.pojo.AccountPojo; import
 * com.wayapaychat.temporalwallet.pojo.AccountPojo2; import
 * com.wayapaychat.temporalwallet.pojo.UserDetailPojo; import
 * com.wayapaychat.temporalwallet.repository.AccountRepository; import
 * com.wayapaychat.temporalwallet.repository.UserRepository; import
 * com.wayapaychat.temporalwallet.service.AccountService; import
 * com.wayapaychat.temporalwallet.util.ErrorResponse; import
 * com.wayapaychat.temporalwallet.util.RandomGenerators; import
 * com.wayapaychat.temporalwallet.util.SuccessResponse;
 * 
 * import lombok.extern.slf4j.Slf4j;
 * 
 * import org.modelmapper.ModelMapper; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.data.domain.Page; import
 * org.springframework.data.domain.PageRequest; import
 * org.springframework.data.domain.Pageable; import
 * org.springframework.http.HttpStatus; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.stereotype.Service;
 * 
 * import java.util.ArrayList; import java.util.Date; import java.util.List;
 * import java.util.Optional;
 * 
 * @Service
 * 
 * @Slf4j public class AccountServiceImpl implements AccountService {
 * 
 * @Autowired UserRepository userRepository;
 * 
 * @Autowired AccountRepository accountRepository;
 * 
 * @Autowired RandomGenerators randomGenerators;
 * 
 * @Autowired AuthUserServiceDAO authUserService;
 * 
 * @Override public ResponseEntity createAccount(AccountPojo2 accountPojo) { int
 * userId = accountPojo.getUserId().intValue(); UserDetailPojo user =
 * authUserService.AuthUser(userId); if (user == null) { return new
 * ResponseEntity<>(new ErrorResponse("User Id is Invalid"),
 * HttpStatus.BAD_REQUEST); } Optional<Users> y =
 * userRepository.findByEmailAddress(user.getEmail()); if(y.isPresent()) {
 * //duplicate key value violates unique constraint return new
 * ResponseEntity<>(new ErrorResponse("Duplicate Email Violated"),
 * HttpStatus.BAD_REQUEST); } Optional<Users> x =
 * userRepository.findByUserId(user.getId()); if (x.isPresent()) { Users v =
 * x.get(); Accounts account = new Accounts(); account.setUser(v);
 * account.setAccountName(v.getFirstName() + " " + v.getLastName());
 * account.setAccountType(AccountType.SAVINGS); account.setDefault(true);
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10)); try {
 * accountRepository.save(account); userRepository.save(v); List<Accounts>
 * userAccount = v.getAccounts(); userAccount.add(account);
 * v.setAccounts(userAccount); userRepository.save(v); return new
 * ResponseEntity<>(new SuccessResponse("Account Created Successfully.",
 * account), HttpStatus.CREATED); } catch (Exception e) { return new
 * ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()),
 * HttpStatus.BAD_REQUEST); } } // =============================== try {
 * 
 * Users us = new Users(user.getId(), new Date(), user.getFirstName(),
 * user.getSurname(), 1, user.getEmail(), user.getPhoneNo());
 * us.setCreatedAt(new Date()); us.setEmailAddress(user.getEmail());
 * us.setFirstName(user.getFirstName()); us.setUserId(user.getId());
 * us.setId(0L); us.setLastName(user.getSurname());
 * us.setMobileNo(user.getPhoneNo()); us.setSavingsProductId(1);
 * us.setUserId(0L); Users mu = userRepository.save(us); Accounts account = new
 * Accounts(); account.setUser(mu); account.setProductId(1L);
 * account.setActive(true); account.setApproved(true); account.setDefault(true);
 * account.setClosed(false); // account.setU
 * account.setCode("savingsAccountStatusType.active");
 * account.setValue("Active"); account.setAccountName(user.getFirstName() + " "
 * + user.getSurname());
 * 
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10)); Accounts
 * mAccount = accountRepository.save(account); // userRepository.save(user);
 * List<Accounts> userAccount = new ArrayList<>(); userAccount.add(account);
 * mu.setAccounts(userAccount); Users uu = userRepository.save(mu); //
 * ===================================
 * 
 * Accounts account = new Accounts(); account.setUser(user);
 * account.setAccountName(user.getFirstName());
 * account.setAccountType(AccountType.SAVINGS); account.setDefault(true);
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10)); try {
 * accountRepository.save(account); userRepository.save(user); List<Accounts>
 * userAccount = user.getAccounts(); userAccount.add(account);
 * user.setAccounts(userAccount); userRepository.save(user);
 * 
 * return new ResponseEntity<>(new
 * SuccessResponse("Account Created Successfully.", account),
 * HttpStatus.CREATED); } catch (Exception e) { return new ResponseEntity<>(new
 * ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST); } }
 * 
 * @Override // Citymanjoe public ResponseEntity getUserAccountList(long userId)
 * { int uId = (int)userId; UserDetailPojo ur = authUserService.AuthUser(uId);
 * if (ur == null) { return new ResponseEntity<>(new
 * ErrorResponse("User Id is Invalid"), HttpStatus.BAD_REQUEST); }
 * Optional<Users> x = userRepository.findByEmailAddress(ur.getEmail()); if
 * (!x.isPresent()) { return new ResponseEntity<>(new
 * ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST); } Users user =
 * x.get(); List<Accounts> accounts = accountRepository.findByUser(user); return
 * new ResponseEntity<>(new SuccessResponse("Success.", accounts),
 * HttpStatus.OK); }
 * 
 * @Override public ResponseEntity getUserCommissionList(long userId) {
 * Optional<Users> userx = userRepository.findById(userId); if
 * (!userx.isPresent()) { return new ResponseEntity<>(new
 * ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST); } Users user =
 * userx.get(); Accounts accounts =
 * accountRepository.findByUserAndAccountType(user, AccountType.COMMISSION);
 * return new ResponseEntity<>(new SuccessResponse("Success.", accounts),
 * HttpStatus.OK); }
 * 
 * @Override public ResponseEntity getAllAccount() { Pageable paging =
 * PageRequest.of(0, 10); Page<Accounts> pagedResult =
 * accountRepository.findAll(paging); // List<Accounts> accountList =
 * accountRepository.findAll(); return new ResponseEntity<>(new
 * SuccessResponse("Success.", pagedResult), HttpStatus.OK); }
 * 
 * @Override public ResponseEntity getDefaultWallet(long userId) {
 * Optional<Users> userx = userRepository.findById(userId); if (userx == null) {
 * return new ResponseEntity<>(new ErrorResponse("Invalid User"),
 * HttpStatus.BAD_REQUEST); } Users user = userx.get(); Accounts account =
 * accountRepository.findByIsDefaultAndUser(true, user); return new
 * ResponseEntity<>(new SuccessResponse("Default Wallet", account),
 * HttpStatus.OK); }
 * 
 * @Override public ResponseEntity makeDefaultWallet(long userId, String
 * accountNo) { Optional<Users> userx = userRepository.findByUserId(userId); if
 * (!userx.isPresent()) { return new ResponseEntity<>(new
 * ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST); } Users user =
 * userx.get(); Optional<Accounts> account =
 * accountRepository.findByAccountNo(accountNo); if (!account.isPresent()) {
 * return new ResponseEntity<>(new ErrorResponse("Invalid Account No"),
 * HttpStatus.BAD_REQUEST); } List<Accounts> acct =
 * accountRepository.findByUser(user); boolean userexist = false; for(Accounts
 * xy : acct) { if(xy.getAccountNo().equals(account.get().getAccountNo())) {
 * userexist = true; } } // Check if account belongs to user if (!userexist) {
 * return new ResponseEntity<>(new ErrorResponse("Invalid Account Access"),
 * HttpStatus.BAD_REQUEST); } // Get Default Wallet Accounts defAccount =
 * accountRepository.findByIsDefaultAndUser(true, user); if (defAccount != null)
 * { defAccount.setDefault(false); accountRepository.save(defAccount); }
 * account.get().setDefault(true); accountRepository.save(account.get()); return
 * new ResponseEntity<>(new SuccessResponse("Default wallet set",
 * account.get()), HttpStatus.OK);
 * 
 * }
 * 
 * @Override public ResponseEntity getAccountInfo(String accountNo) {
 * Optional<Accounts> account = accountRepository.findByAccountNo(accountNo);
 * return new ResponseEntity<>(new SuccessResponse("Success.", account.get()),
 * HttpStatus.OK); }
 * 
 * @Override public ResponseEntity editAccountName(String accountNo, String
 * newName) { Optional<Accounts> account =
 * accountRepository.findByAccountNo(accountNo);
 * account.get().setAccountName(newName); try {
 * accountRepository.save(account.get()); return new ResponseEntity<>(new
 * SuccessResponse("Account name changed", account), HttpStatus.OK); } catch
 * (Exception e) { return new ResponseEntity<>(new ErrorResponse(),
 * HttpStatus.BAD_REQUEST); } }
 * 
 * @Override public ResponseEntity getCommissionAccountListByArray(List<Integer>
 * ids) { List<Accounts> accounts = new ArrayList<>(); for (int id : ids) {
 * Accounts commissionAccount = null; Long l = Long.valueOf(id); Optional<Users>
 * userx = userRepository.findById(l); if (!userx.isPresent()) { return new
 * ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
 * } Users user = userx.get(); if (user != null) { commissionAccount =
 * accountRepository.findByUserAndAccountType(user, AccountType.COMMISSION); }
 * accounts.add(commissionAccount); } return new ResponseEntity<>(new
 * SuccessResponse("Account name changed", accounts), HttpStatus.OK); }
 * 
 * }
 */
