package com.wayapaychat.temporalwallet.service;

import static com.wayapaychat.temporalwallet.util.Constant.WAYA_SETTLEMENT_ACCOUNT_NO;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wayapaychat.temporalwallet.entity.Accounts;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.Users;
import com.wayapaychat.temporalwallet.enumm.TransactionType;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.TransactionResponse;
import com.wayapaychat.temporalwallet.repository.AccountRepository;
import com.wayapaychat.temporalwallet.repository.TransactionRepository;
import com.wayapaychat.temporalwallet.repository.UserRepository;
import com.wayapaychat.temporalwallet.security.AuthenticatedUserFacade;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.RandomGenerators;

@Service
public class TransactionNewService {
	
	@Autowired
    UserRepository userRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RandomGenerators randomGenerators;
    
    @Autowired
    private AuthenticatedUserFacade userFacade;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionNewService.class);
    
    
    
    public Page<Transactions> getWalletTransaction(int page, int size) {
    	try {
    		Pageable paging = PageRequest.of(page, size);
    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
//    		System.out.println(":::::::::user id::::::"+user.getId());
    		Optional<Users> mUser = userRepository.findByUserId(user.getId());
//    		Users user = (Users)  userFacade.getAuthentication().getPrincipal();
    		Accounts accnt = accountRepository.findByIsDefaultAndUser(true, mUser.get());
    		return transactionRepository.findByAccount(accnt,paging);
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public Page<Transactions> getWalletTransactionByUser(Long userId, int page, int size) {
    	try {
    		Pageable paging = PageRequest.of(page, size);
//    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
//    		System.out.println(":::::::::user id::::::"+user.getId());
    		Optional<Users> mUser = userRepository.findByUserId(userId);
//    		Users user = (Users)  userFacade.getAuthentication().getPrincipal();
    		Accounts accnt = accountRepository.findByIsDefaultAndUser(true, mUser.get());
    		return transactionRepository.findByAccount(accnt,paging);
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public Page<Transactions> findAllTransaction(int page, int size) {
    	try {
    		Pageable paging = PageRequest.of(page, size);
    		
    		return transactionRepository.findAll(paging);
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    public TransactionRequest makeTransaction(String command, TransactionRequest request) {
    	try {
    		System.out.println("Making Transaction.......");
    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
    		System.out.println(":::::::::user id::::::"+user.getId());
    		Optional<Users> mUser = userRepository.findByUserId(user.getId());
    		Accounts senderAccount = accountRepository.findByUserAndIsDefault(mUser.get(), true);
    		Optional<Accounts> wayaAccount = accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
    		if (senderAccount == null) {
    			throw new CustomException("Invalid Account", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            if (request.getAmount() < 1) {
            	throw new CustomException("Invalid amount", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            
            
            
         // Register Transaction

            String ref = randomGenerators.generateAlphanumeric(12);
            
            if (command == "CREDIT"){
            	
            	// Handle Credit User Account
                Transactions transaction = new Transactions();
                transaction.setTransactionType(command);
                transaction.setAccount(senderAccount);
                transaction.setAmount(request.getAmount());
                transaction.setRefCode(ref);

                transactionRepository.save(transaction);
                senderAccount.setBalance(senderAccount.getBalance() + request.getAmount());
                List<Transactions> transactionList = senderAccount.getTransactions();
                transactionList.add(transaction);
                accountRepository.save(senderAccount);
                
             // Handle Debit Waya Account
                Transactions transaction2 = new Transactions();
                transaction2.setTransactionType("DEBIT");
                transaction2.setAccount(wayaAccount.get());
                transaction2.setAmount(request.getAmount());
                transaction2.setRefCode(ref);

                transactionRepository.save(transaction2);
                wayaAccount.get().setBalance(wayaAccount.get().getBalance() - request.getAmount());
                List<Transactions> transactionList2 = wayaAccount.get().getTransactions();
                transactionList2.add(transaction2);
                
                TransactionRequest res = new TransactionRequest();
                res.setAmount(request.getAmount());
//                res.setCustomerWalletId(senderAccount.getId());
                res.setDescription(request.getDescription());
                res.setPaymentReference(ref);
                
                return res;
                
            }
            if (command == "DEBIT") {

                if (senderAccount.getBalance() < request.getAmount()) {
                	throw new CustomException("Insufficient Balance", HttpStatus.BAD_REQUEST);
                }

                // Handle Debit User Account
                Transactions transaction = new Transactions();
                transaction.setTransactionType("DEBIT");
                transaction.setAccount(senderAccount);
                transaction.setAmount(request.getAmount());
                transaction.setRefCode(ref);

                transactionRepository.save(transaction);
                senderAccount.setBalance(senderAccount.getBalance() - request.getAmount());
                List<Transactions> transactionList = senderAccount.getTransactions();
                transactionList.add(transaction);
                accountRepository.save(senderAccount);

                // Handle Debit Waya Account
                Transactions transaction2 = new Transactions();
                transaction2.setTransactionType("CREDIT");
                transaction2.setAccount(wayaAccount.get());
                transaction2.setAmount(request.getAmount());
                transaction2.setRefCode(ref);

                transactionRepository.save(transaction2);
                wayaAccount.get().setBalance(wayaAccount.get().getBalance() + request.getAmount());
                List<Transactions> transactionList2 = wayaAccount.get().getTransactions();
                transactionList2.add(transaction2);
                accountRepository.save(wayaAccount.get());
                
                TransactionRequest res = new TransactionRequest();
                res.setAmount(request.getAmount());
//                res.setCustomerWalletId(senderAccount.getId());
                res.setDescription(request.getDescription());
                res.setPaymentReference(ref);
                
                return res;
            }
            if (command == TransactionType.FUNDS_TRANSFER.name()) {
            	
            }
            throw new CustomException("Invalid Transaction", HttpStatus.UNPROCESSABLE_ENTITY);
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    
    @Transactional
    public TransactionRequest transferUserToUser(String command, TransactionRequest request) {
    	try {
    		MyData user = (MyData)  userFacade.getAuthentication().getPrincipal();
//    		System.out.println(":::::::::user id 2::::::"+user.getId());
    		Optional<Users> mUser = userRepository.findByUserId(user.getId());
    		Accounts userAccount = accountRepository.findByUserAndIsDefault(mUser.get(), true);
    		Optional<Accounts> wayaAccount = accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO);
    		if(wayaAccount.isPresent()) {
                String ref = randomGenerators.generateAlphanumeric(12);
                
                if (request.getAmount() < 1) {
                    
                    throw new CustomException("Invalid Amount", HttpStatus.BAD_REQUEST);
                }
                
                
             // Handle Debit User Account
                if (command.equals("DEBIT")) {
                	
                	if (userAccount.getBalance() < request.getAmount()) {
                        throw new CustomException("Insufficient Fund", HttpStatus.BAD_REQUEST);
                    }
                    Transactions transaction = new Transactions();
                    transaction.setTransactionType("DEBIT");
                    transaction.setAccount(userAccount);
                    transaction.setAmount(request.getAmount());
                    transaction.setRefCode(ref);

                    transactionRepository.save(transaction);
                    userAccount.setBalance(userAccount.getBalance() - request.getAmount());
                    userAccount.setLagerBalance(userAccount.getBalance() - request.getAmount());
                    List<Transactions> transactionList = userAccount.getTransactions();
                    transactionList.add(transaction);
                    accountRepository.save(userAccount);
                 // Handle Credit Waya Account
                    Transactions transaction2 = new Transactions();
                    transaction2.setTransactionType("CREDIT");
                    transaction2.setAccount(wayaAccount.get());
                    transaction2.setAmount(request.getAmount());
                    transaction2.setRefCode(ref);
                    
                    transactionRepository.save(transaction2);
                    wayaAccount.get().setBalance(wayaAccount.get().getBalance() + request.getAmount());
                    List<Transactions> transactionList2 = wayaAccount.get().getTransactions();
                    transactionList2.add(transaction2);
                    accountRepository.save(wayaAccount.get());
                    
                    
                    //Construct Response
                    TransactionRequest res = new TransactionRequest();
                    res.setAmount(request.getAmount());
//                    res.setCustomerWalletId(userAccount.getId());
                    res.setDescription(request.getDescription());
                    res.setPaymentReference(ref);
                    return res;
    			}
                
                
             // Handle Credit USER Account
                if (command.equals("CREDIT")) {
                	Transactions transaction = new Transactions();
                    transaction.setTransactionType("DEBIT");
                    transaction.setAccount(userAccount);
                    transaction.setAmount(request.getAmount());
                    transaction.setRefCode(ref);

                    transactionRepository.save(transaction);
                    userAccount.setBalance(userAccount.getBalance() + request.getAmount());
                    userAccount.setLagerBalance(userAccount.getBalance() + request.getAmount());
                    List<Transactions> transactionList = userAccount.getTransactions();
                    transactionList.add(transaction);
                    accountRepository.save(userAccount);
                    
                 // Handle Debit Waya Account
                    Transactions transaction3 = new Transactions();
                    transaction3.setTransactionType("DEBIT");
                    transaction3.setAccount(wayaAccount.get());
                    transaction3.setAmount(request.getAmount());
                    transaction3.setRefCode(ref);

                    transactionRepository.save(transaction3);
                    wayaAccount.get().setBalance(wayaAccount.get().getBalance() - request.getAmount());
                    List<Transactions> transactionList3 = wayaAccount.get().getTransactions();
                    transactionList3.add(transaction3);
                    accountRepository.save(wayaAccount.get());
                    
                  //Construct Response
                    TransactionRequest res = new TransactionRequest();
                    res.setAmount(request.getAmount());
//                    res.setCustomerWalletId(userAccount.getId());
                    res.setDescription(request.getDescription());
                    res.setPaymentReference(ref);
                    
                    return res;
    			}

    		} else {
    			throw new CustomException("Invalid User", HttpStatus.BAD_REQUEST);
    		}
            
            throw new CustomException("Error Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOGGER.info("Error::: {}, {} and {}", e.getMessage(),2,3);
			throw new CustomException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
		}
    }
    

}
