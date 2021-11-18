/*
 * package com.wayapaychat.temporalwallet.service.impl;
 * 
 * import static
 * com.wayapaychat.temporalwallet.util.Constant.WAYA_SETTLEMENT_ACCOUNT_NO;
 * 
 * import java.util.List; import java.util.Optional;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.http.HttpStatus; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.stereotype.Service; import
 * org.springframework.transaction.annotation.Transactional;
 * 
 * import com.wayapaychat.temporalwallet.entity.Accounts; import
 * com.wayapaychat.temporalwallet.entity.Transactions; import
 * com.wayapaychat.temporalwallet.entity.Users; import
 * com.wayapaychat.temporalwallet.pojo.TransactionPojo; import
 * com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo; import
 * com.wayapaychat.temporalwallet.pojo.TransactionTransferPojo2; import
 * com.wayapaychat.temporalwallet.repository.AccountRepository; import
 * com.wayapaychat.temporalwallet.repository.TransactionRepository; import
 * com.wayapaychat.temporalwallet.repository.UserRepository; import
 * com.wayapaychat.temporalwallet.service.TransactionService; import
 * com.wayapaychat.temporalwallet.util.ErrorResponse; import
 * com.wayapaychat.temporalwallet.util.RandomGenerators; import
 * com.wayapaychat.temporalwallet.util.SuccessResponse;
 * 
 * 
 * @Service public class TransactionServiceImpl implements TransactionService {
 * 
 * @Autowired UserRepository userRepository;
 * 
 * @Autowired TransactionRepository transactionRepository;
 * 
 * @Autowired AccountRepository accountRepository;
 * 
 * @Autowired RandomGenerators randomGenerators;
 * 
 * @Override
 * 
 * @Transactional public ResponseEntity<?> transactAmount(TransactionPojo
 * transactionPojo) { Optional<Accounts> account =
 * accountRepository.findByAccountNo(transactionPojo.getAccountNo());
 * Optional<Accounts> wayaAccount =
 * accountRepository.findByAccountNo(WAYA_SETTLEMENT_ACCOUNT_NO); if
 * (!account.isPresent()) { return new ResponseEntity<>(new
 * ErrorResponse("Invalid Account"), HttpStatus.BAD_REQUEST); } if
 * (transactionPojo.getAmount() < 1) { return new ResponseEntity<>(new
 * ErrorResponse("Invalid Amount"), HttpStatus.BAD_REQUEST); } // Register
 * Transaction
 * 
 * String ref = randomGenerators.generateAlphanumeric(12); if
 * (transactionPojo.getTransactionType().equals("CREDIT")){
 * 
 * // Handle Credit User Account Transactions transaction = new Transactions();
 * transaction.setTransactionType(transactionPojo.getTransactionType());
 * transaction.setAccount(account.get());
 * transaction.setAmount(transactionPojo.getAmount());
 * transaction.setRefCode(ref);
 * 
 * transactionRepository.save(transaction);
 * account.get().setBalance(account.get().getBalance() +
 * transactionPojo.getAmount()); List<Transactions> transactionList =
 * account.get().getTransactions(); transactionList.add(transaction);
 * accountRepository.save(account.get());
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("DEBIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(transactionPojo.getAmount());
 * transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() -
 * transactionPojo.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2); }
 * else {
 * 
 * if (account.get().getBalance() < transactionPojo.getAmount()) { return new
 * ResponseEntity<>(new ErrorResponse("Insufficient Balance"),
 * HttpStatus.BAD_REQUEST); }
 * 
 * // Handle Debit User Account Transactions transaction = new Transactions();
 * transaction.setTransactionType("DEBIT");
 * transaction.setAccount(account.get());
 * transaction.setAmount(transactionPojo.getAmount());
 * transaction.setRefCode(ref);
 * 
 * transactionRepository.save(transaction);
 * account.get().setBalance(account.get().getBalance() -
 * transactionPojo.getAmount()); List<Transactions> transactionList =
 * account.get().getTransactions(); transactionList.add(transaction);
 * accountRepository.save(account.get());
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("CREDIT");
 * transaction2.setAccount(wayaAccount.get());
 * transaction2.setAmount(transactionPojo.getAmount());
 * transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * wayaAccount.get().setBalance(wayaAccount.get().getBalance() +
 * transactionPojo.getAmount()); List<Transactions> transactionList2 =
 * wayaAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(wayaAccount.get()); } return new ResponseEntity<>(new
 * SuccessResponse("Success.", null), HttpStatus.OK); }
 * 
 * @Override public ResponseEntity<?>
 * transferTransaction(TransactionTransferPojo transactionTransferPojo) {
 * 
 * Optional<Accounts> fromAccount =
 * accountRepository.findByAccountNo(transactionTransferPojo.getFromAccount());
 * Optional<Accounts> toAccount =
 * accountRepository.findByAccountNo(transactionTransferPojo.getToAccount());
 * String ref = randomGenerators.generateAlphanumeric(12); if (fromAccount ==
 * null || toAccount == null) { return new ResponseEntity<>(new
 * ErrorResponse("Possible Invalid Account"), HttpStatus.BAD_REQUEST); } if
 * (transactionTransferPojo.getAmount() < 1) { return new ResponseEntity<>(new
 * ErrorResponse("Invalid Amount"), HttpStatus.BAD_REQUEST); } if
 * (fromAccount.get().getBalance() < transactionTransferPojo.getAmount()) {
 * return new ResponseEntity<>(new ErrorResponse("Insufficient Fund"),
 * HttpStatus.BAD_REQUEST); }
 * 
 * // Handle Debit User Account Transactions transaction = new Transactions();
 * transaction.setTransactionType("DEBIT");
 * transaction.setAccount(fromAccount.get());
 * transaction.setAmount(transactionTransferPojo.getAmount());
 * transaction.setRefCode(ref);
 * 
 * transactionRepository.save(transaction);
 * fromAccount.get().setBalance(fromAccount.get().getBalance() -
 * transactionTransferPojo.getAmount()); List<Transactions> transactionList =
 * fromAccount.get().getTransactions(); transactionList.add(transaction);
 * accountRepository.save(fromAccount.get());
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("CREDIT");
 * transaction2.setAccount(toAccount.get());
 * transaction2.setAmount(transactionTransferPojo.getAmount());
 * transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * toAccount.get().setBalance(toAccount.get().getBalance() +
 * transactionTransferPojo.getAmount()); List<Transactions> transactionList2 =
 * toAccount.get().getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(toAccount.get());
 * 
 * return new ResponseEntity<>(new SuccessResponse("Transfer Completed.", null),
 * HttpStatus.OK);
 * 
 * }
 * 
 * @Override public ResponseEntity<?>
 * transferTransactionWithId(TransactionTransferPojo2 transactionTransferPojo2)
 * { Long tranId = Long.valueOf(transactionTransferPojo2.getFromId()); Long tId
 * = Long.valueOf(transactionTransferPojo2.getToId()); Optional<Users> fromUserx
 * = userRepository.findById(tranId); Optional<Users> toUserx =
 * userRepository.findById(tId);
 * 
 * if (!fromUserx.isPresent()){ return new ResponseEntity<>(new
 * ErrorResponse("Invalid Sender Id"), HttpStatus.BAD_REQUEST); } if
 * (!toUserx.isPresent()){ return new ResponseEntity<>(new
 * ErrorResponse("Invalid Receiver Id"), HttpStatus.BAD_REQUEST); } Users
 * fromUser = fromUserx.get(); Users toUser = toUserx.get(); Accounts
 * fromAccount = accountRepository.findByUserAndIsDefault(fromUser, true);
 * Accounts toAccount = accountRepository.findByUserAndIsDefault(toUser, true);
 * String ref = randomGenerators.generateAlphanumeric(12); if (fromAccount ==
 * null || toAccount == null) { return new ResponseEntity<>(new
 * ErrorResponse("Possible Invalid Account"), HttpStatus.BAD_REQUEST); } if
 * (transactionTransferPojo2.getAmount() < 1) { return new ResponseEntity<>(new
 * ErrorResponse("Invalid Amount"), HttpStatus.BAD_REQUEST); } if
 * (fromAccount.getBalance() < transactionTransferPojo2.getAmount()) { return
 * new ResponseEntity<>(new ErrorResponse("Insufficient Fund"),
 * HttpStatus.BAD_REQUEST); }
 * 
 * // Handle Debit User Account Transactions transaction = new Transactions();
 * transaction.setTransactionType("DEBIT"); transaction.setAccount(fromAccount);
 * transaction.setAmount(transactionTransferPojo2.getAmount());
 * transaction.setRefCode(ref);
 * 
 * transactionRepository.save(transaction);
 * fromAccount.setBalance(fromAccount.getBalance() -
 * transactionTransferPojo2.getAmount()); List<Transactions> transactionList =
 * fromAccount.getTransactions(); transactionList.add(transaction);
 * accountRepository.save(fromAccount);
 * 
 * // Handle Debit Waya Account Transactions transaction2 = new Transactions();
 * transaction2.setTransactionType("CREDIT");
 * transaction2.setAccount(toAccount);
 * transaction2.setAmount(transactionTransferPojo2.getAmount());
 * transaction2.setRefCode(ref);
 * 
 * transactionRepository.save(transaction2);
 * toAccount.setBalance(toAccount.getBalance() +
 * transactionTransferPojo2.getAmount()); List<Transactions> transactionList2 =
 * toAccount.getTransactions(); transactionList2.add(transaction2);
 * accountRepository.save(toAccount);
 * 
 * return new ResponseEntity<>(new SuccessResponse("Transfer Completed.", null),
 * HttpStatus.OK); } }
 */