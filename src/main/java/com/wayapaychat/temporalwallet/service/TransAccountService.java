package com.wayapaychat.temporalwallet.service;

import org.springframework.data.domain.Page;

import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.WalletToWalletDto;
import com.wayapaychat.temporalwallet.response.ApiResponse;

public interface TransAccountService {
	
	ApiResponse<?> makeTransaction(String command, TransactionRequest request);
	
	ApiResponse<TransactionRequest> walletToWalletTransfer(String command, WalletToWalletDto walletDto);
	
	ApiResponse<?> adminTransferForUser(String command, AdminUserTransferDTO adminTranser);
	
	ApiResponse<TransactionRequest> transferUserToUser(String command, TransactionRequest request);
	
	ApiResponse<Page<WalletTransaction>> findAllTransaction(int page, int size);
	
	ApiResponse<Page<WalletTransaction>> getTransactionByWalletId(int page, int size, Long walletId);
	
	ApiResponse<Page<Transactions>> getTransactionByType(int page, int size, String transactionType);
	
	ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber);
	
	ApiResponse<?> makeWalletTransaction(String command, TransferTransactionDTO transactionPojo);
	
	ApiResponse<?> sendMoney(TransferTransactionDTO transfer);
	
	ApiResponse<?> getStatement(String accountNumber);
	
	ApiResponse<?> EventTransferPayment(EventPaymentDTO eventPay);
	
	

}
