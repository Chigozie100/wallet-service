package com.wayapaychat.temporalwallet;

import com.wayapaychat.temporalwallet.entity.Account;
import com.wayapaychat.temporalwallet.entity.Users;
import com.wayapaychat.temporalwallet.enumm.AccountType;
import com.wayapaychat.temporalwallet.repository.AccountRepository;
import com.wayapaychat.temporalwallet.repository.UserRepository;
import com.wayapaychat.temporalwallet.util.RandomGenerators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.wayapaychat.temporalwallet.util.Constant.WAYA_COMMISSION_ACCOUNT_NO;
import static com.wayapaychat.temporalwallet.util.Constant.WAYA_SETTLEMENT_ACCOUNT_NO;

//@Configuration
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RandomGenerators randomGenerators;

    @Transactional
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {


        // Settlement Account
        Users user = new Users();
        user.setUserId(786567886789L);
        System.out.println(user.getUserId());
        userRepository.save(user);

        Account account = new Account();
        account.setAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
        account.setAccountType(AccountType.SAVINGS);
        account.setUser(user);
        account.setBalance(1000000);
        account.setAccountName("Waya Settlement");
        accountRepository.save(account);

        // Commission Account
        user.setUserId(786567886784L);
        userRepository.save(user);

        Account account2 = new Account();
        account2.setAccountNo(WAYA_COMMISSION_ACCOUNT_NO);
        account2.setAccountType(AccountType.COMMISSION);
        account2.setUser(user);
        account2.setBalance(1000000);
        account2.setAccountName("Waya Commissions");
        accountRepository.save(account2);

        List<Account> accountList = user.getAccounts();
        accountList.add(account);
        accountList.add(account2);
        user.setAccounts(accountList);
        userRepository.save(user);

    }
}
