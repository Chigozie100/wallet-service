//package com.wayapaychat.temporalwallet.config;
//
//import static com.wayapaychat.temporalwallet.util.Constant.WAYA_SETTLEMENT_ACCOUNT_NO;
//
//import java.time.LocalDateTime;
//import java.util.Date;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import com.wayapaychat.temporalwallet.entity.Accounts;
//import com.wayapaychat.temporalwallet.entity.Users;
//import com.wayapaychat.temporalwallet.enumm.AccountType;
//import com.wayapaychat.temporalwallet.repository.AccountRepository;
//import com.wayapaychat.temporalwallet.util.Constant;
//
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//@Component
//@EnableSwagger2
//public class BootstrapConfig implements CommandLineRunner {
//	
//	@Autowired
//	private AccountRepository accountRepository;
//
//	@Override
//	public void run(String... args) throws Exception {
//		Optional<Accounts> wayaAccount = accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
//		if(!wayaAccount.isPresent()) {
//			Users user = new Users();
//			user.setCreatedAt(new Date());
//			user.setEmailAddress("admin@wayapaychat.com");
//			user.setFirstName("WAYA");
//			user.setLastName("ACCOUNT");
//			user.setMobileNo("2347080972321");
//			user.setSavingsProductId(1);
//			user.setUserId(1L);
//			Accounts account = new Accounts();
//	          account.setAccountNo(Constant.WAYA_SETTLEMENT_ACCOUNT_NO);
//	          account.setAccountType(AccountType.SAVINGS);
//	          account.setUser(user);
//	          account.setBalance(1000000);
//	          account.setAccountName(user.getFirstName()+" "+user.getLastName());
//	          accountRepository.save(account);
//
//	          // Commission Account
//	          Accounts account2 = new Accounts();
//	          account2.setAccountNo(Constant.WAYA_COMMISSION_ACCOUNT_NO);
//	          account2.setAccountType(AccountType.COMMISSION);
//	          account2.setUser(user);
//	          account2.setBalance(1000000);
//	          account2.setAccountName("Waya Commissions");
//	          accountRepository.save(account2);
//		}
//	}
//
//}
