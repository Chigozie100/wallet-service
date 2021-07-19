package com.wayapaychat.temporalwallet.service;

import org.springframework.data.domain.Page;

import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.pojo.AdminUserTransferDto;
import com.wayapaychat.temporalwallet.pojo.MifosTransactionPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.WalletToWalletDto;
import com.wayapaychat.temporalwallet.response.ApiResponse;

public interface TransAccountService {
	
	ApiResponse<TransactionRequest> makeTransaction(String command, TransactionRequest request);
	
	ApiResponse<TransactionRequest> walletToWalletTransfer(String command, WalletToWalletDto walletDto);
	
	ApiResponse<TransactionRequest> adminTransferForUser(String command, AdminUserTransferDto adminTranser);
	
	ApiResponse<TransactionRequest> transferUserToUser(String command, TransactionRequest request);
	
	ApiResponse<Page<Transactions>> getTransactionByWalletId(int page, int size, Long walletId);
	
	ApiResponse<Page<Transactions>> getTransactionByType(int page, int size, String transactionType);
	
	ApiResponse<Page<Transactions>> findByAccountNumber(int page, int size, String accountNumber);
	
	ApiResponse<?> makeWalletTransaction(String command, MifosTransactionPojo transactionPojo);
	
	ApiResponse<?> sendMoney(TransferTransactionDTO transfer);
	
	ApiResponse<?> getStatement(String accountNumber);
	
	

}
