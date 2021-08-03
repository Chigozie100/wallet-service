package com.wayapaychat.temporalwallet.service;

import java.text.ParseException;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.dto.BulkTransactionCreationDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.OfficeTransferDTO;
import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.WalletToWalletDto;
import com.wayapaychat.temporalwallet.response.ApiResponse;

public interface TransAccountService {
	
	ApiResponse<?> makeTransaction(String command, TransactionRequest request);
	
	ApiResponse<TransactionRequest> walletToWalletTransfer(String command, WalletToWalletDto walletDto);
	
	ApiResponse<?> adminTransferForUser(String command, AdminUserTransferDTO adminTranser);
	
	ApiResponse<?> cashTransferByAdmin(String command, WalletAdminTransferDTO adminTranser);
	
	ApiResponse<TransactionRequest> transferUserToUser(String command, TransactionRequest request);
	
	ApiResponse<Page<WalletTransaction>> findAllTransaction(int page, int size);
	
	ApiResponse<Page<WalletTransaction>> getTransactionByWalletId(int page, int size, Long walletId);
	
	ApiResponse<Page<Transactions>> getTransactionByType(int page, int size, String transactionType);
	
	ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber);
	
	ApiResponse<?> makeWalletTransaction(String command, TransferTransactionDTO transactionPojo);
	
	ApiResponse<?> sendMoney(TransferTransactionDTO transfer);
	
	ApiResponse<?> OfficialMoneyTransfer(OfficeTransferDTO transfer);
	
	ApiResponse<?> OfficialUserTransfer(OfficeUserTransferDTO transfer);
	
	ApiResponse<?> createBulkTransaction(BulkTransactionCreationDTO bulk);
	
	ApiResponse<?> createBulkExcelTrans(MultipartFile file);
	
	ApiResponse<?> AdminsendMoney(AdminLocalTransferDTO transfer);
	
	ApiResponse<?> sendMoneyCustomer(WalletTransactionDTO transfer);
	
	ApiResponse<?> getStatement(String accountNumber);
	
	ApiResponse<?> EventTransferPayment(EventPaymentDTO eventPay);
	
	ApiResponse<?> sendMoneyCharge(WalletTransactionChargeDTO transfer);
	
	ApiResponse<?> TranReversePayment(ReverseTransactionDTO reverseDto) throws ParseException;
	
	ApiResponse<?> TranRevALLReport(Date date, Date todate);
	
	ApiResponse<?> BankTransferPayment(BankPaymentDTO transfer);
	

}
