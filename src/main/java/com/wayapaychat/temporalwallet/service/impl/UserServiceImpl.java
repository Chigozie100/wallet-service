/*
 * package com.wayapaychat.temporalwallet.service.impl;
 * 
 * import java.util.Optional;
 * 
 * import org.modelmapper.ModelMapper; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.http.HttpStatus; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.stereotype.Service;
 * 
 * import com.wayapaychat.temporalwallet.entity.Accounts; import
 * com.wayapaychat.temporalwallet.entity.Users; import
 * com.wayapaychat.temporalwallet.enumm.AccountType; import
 * com.wayapaychat.temporalwallet.pojo.UserPojo; import
 * com.wayapaychat.temporalwallet.repository.AccountRepository; import
 * com.wayapaychat.temporalwallet.repository.UserRepository; import
 * com.wayapaychat.temporalwallet.service.UserService; import
 * com.wayapaychat.temporalwallet.util.ErrorResponse; import
 * com.wayapaychat.temporalwallet.util.RandomGenerators; import
 * com.wayapaychat.temporalwallet.util.SuccessResponse;
 * 
 * @Service public class UserServiceImpl implements UserService {
 * 
 * @Autowired UserRepository userRepository;
 * 
 * @Autowired RandomGenerators randomGenerators;
 * 
 * @Autowired AccountRepository accountRepository;
 * 
 * 
 * 
 * @Override public ResponseEntity<?> createUser(UserPojo userPojo) {
 * Optional<Users> existingUser = userRepository.findById(userPojo.getUserId());
 * if (existingUser.isPresent()) { return new ResponseEntity<>(new
 * ErrorResponse("User already exists"), HttpStatus.BAD_REQUEST); } Users user =
 * new ModelMapper().map(userPojo, Users.class); try { Users u =
 * userRepository.save(user); // Optional<Users> u =
 * userRepository.findByUserId(user.getUserId()); Accounts account = new
 * Accounts(); account.setUser(u);
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10));
 * account.setAccountName("Default Wallet");
 * account.setAccountType(AccountType.SAVINGS); account.setDefault(true);
 * accountRepository.save(account);
 * 
 * if (userPojo.isCorporate()){ Accounts caccount = new Accounts();
 * caccount.setUser(u);
 * caccount.setAccountNo(randomGenerators.generateAlphanumeric(10));
 * caccount.setAccountName("Commission Wallet");
 * caccount.setAccountType(AccountType.COMMISSION);
 * accountRepository.save(caccount); }
 * 
 * return new ResponseEntity<>(new
 * SuccessResponse("Account created successfully.", null), HttpStatus.CREATED);
 * } catch (Exception e) { return new ResponseEntity<>(new
 * ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST); } }
 * 
 * @SuppressWarnings("unused") private void createAccount(Users user, boolean
 * corporate) { Accounts account = new Accounts(); account.setUser(user);
 * account.setAccountNo(randomGenerators.generateAlphanumeric(10));
 * account.setAccountName("Default Wallet");
 * account.setAccountType(AccountType.SAVINGS); accountRepository.save(account);
 * 
 * 
 * if (corporate){ Accounts caccount = new Accounts(); account.setUser(user);
 * caccount.setAccountNo(randomGenerators.generateAlphanumeric(10));
 * caccount.setAccountName("Commission Wallet");
 * caccount.setAccountType(AccountType.COMMISSION);
 * accountRepository.save(caccount); } userRepository.save(user); } }
 */
