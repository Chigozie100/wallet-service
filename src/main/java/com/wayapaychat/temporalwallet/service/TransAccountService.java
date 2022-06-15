package com.wayapaychat.temporalwallet.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.pojo.TransWallet;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.pojo.TransactionRequest;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;
import com.wayapaychat.temporalwallet.response.ApiResponse;

public interface TransAccountService {

	ResponseEntity<?> adminTransferForUser(HttpServletRequest request, String command, AdminUserTransferDTO adminTranser);

	ApiResponse<?> cashTransferByAdmin(HttpServletRequest request, String command, WalletAdminTransferDTO adminTranser);

	ApiResponse<TransactionRequest> transferUserToUser(HttpServletRequest request, String command, TransactionRequest transfer);

	ApiResponse<Page<WalletTransaction>> findAllTransaction(int page, int size);

	ApiResponse<List<WalletTransaction>> findClientTransaction(String tranId);

	ApiResponse<List<AccountStatementDTO>> ReportTransaction(String accountNo);

	ApiResponse<Page<WalletTransaction>> getTransactionByWalletId(int page, int size, Long walletId);

	ApiResponse<Page<Transactions>> getTransactionByType(int page, int size, String transactionType);

	ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber);

	ResponseEntity<?> makeWalletTransaction(HttpServletRequest request, String command, TransferTransactionDTO transactionPojo);

	ResponseEntity<?> sendMoney(HttpServletRequest request, TransferTransactionDTO transfer);

	ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request, DirectTransactionDTO transfer);

	ResponseEntity<?> PostExternalMoney(HttpServletRequest request, CardRequestPojo transfer, Long userId);

	ApiResponse<?> OfficialMoneyTransfer(HttpServletRequest request, OfficeTransferDTO transfer);

	ApiResponse<?> OfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer);

	ApiResponse<?> createBulkTransaction(HttpServletRequest request, BulkTransactionCreationDTO bulk);

	ApiResponse<?> createBulkExcelTrans(HttpServletRequest request, MultipartFile file);

	ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer);

	ApiResponse<?> AdminSendMoneyMultiple(HttpServletRequest request, List<AdminLocalTransferDTO> transfer);

	ApiResponse<?> AdminCommissionMoney(HttpServletRequest request, CommissionTransferDTO transfer);

	ApiResponse<?> ClientCommissionMoney(HttpServletRequest request, ClientComTransferDTO transfer);

	ApiResponse<?> sendMoneyCustomer(HttpServletRequest request, WalletTransactionDTO transfer);

	ApiResponse<?> AdminSendMoneyCustomer(HttpServletRequest request, AdminWalletTransactionDTO transfer);

	ApiResponse<?> ClientSendMoneyCustomer(HttpServletRequest request, ClientWalletTransactionDTO transfer);

	ApiResponse<?> getStatement(String accountNumber);

	ResponseEntity<?> EventTransferPayment(HttpServletRequest request, EventPaymentDTO eventPay);
	
	ResponseEntity<?> EventOfficePayment(HttpServletRequest request, EventOfficePaymentDTO eventPay);

	ResponseEntity<?> TemporalWalletToOfficialWallet(HttpServletRequest request, TemporalToOfficialWalletDTO transfer);

	//ResponseEntity<?> TemporalWalletToOfficialWalletMutiple(HttpServletRequest request, TemporalToOfficialWalletDTO transfer);


	ApiResponse<?> EventBuySellPayment(HttpServletRequest request, WayaTradeDTO eventPay);

	ResponseEntity<?> EventCommissionPayment(HttpServletRequest request, EventPaymentDTO eventPay);

	ApiResponse<?> sendMoneyCharge(HttpServletRequest request, WalletTransactionChargeDTO transfer);

	ResponseEntity<?> TranReversePayment(HttpServletRequest request, ReverseTransactionDTO reverseDto) throws ParseException;

	ApiResponse<?> VirtuPaymentReverse(HttpServletRequest request, ReversePaymentDTO reverseDto) throws ParseException;

	ApiResponse<?> TranRevALLReport(Date date, Date todate);

	ApiResponse<?> PaymentTransAccountReport(Date date, Date todate, String accountNo);

	ApiResponse<?> PaymentAccountTrans(Date date, Date todate, String wayaNo);

	ApiResponse<?> PaymentOffTrans();

	ApiResponse<?> TranALLReverseReport();

	ApiResponse<?> statementReport(Date fromdate, Date todate, String acctNo);

	List<TransWallet> statementReport2(Date fromdate, Date todate, String acctNo);

	ApiResponse<List<AccountStatementDTO>> ReportTransaction2(String accountNo);

	ApiResponse<?> PaymentTransFilter(String acctNo);

	ResponseEntity<?> BankTransferPayment(HttpServletRequest request, BankPaymentDTO transfer);

	ResponseEntity<?> BankTransferPaymentOfficial(HttpServletRequest request, BankPaymentOfficialDTO transfer);

	ApiResponse<?> EventNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer);

	ResponseEntity<?> getListOfNonWayaTransfers(HttpServletRequest request, String userId, int page, int  size);

	ApiResponse<?> EventNonRedeem(HttpServletRequest request, NonWayaPaymentDTO transfer);

	ApiResponse<?> TranChargeReport();

	ApiResponse<?> CommissionPaymentHistory();

	ResponseEntity<?> TransferNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer);

	ResponseEntity<?> NonWayaPaymentRedeem(HttpServletRequest request, NonWayaRedeemDTO transfer);

	ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request, NonWayaPayPIN transfer);
	
	ResponseEntity<?> WayaQRCodePayment(HttpServletRequest request, WayaPaymentQRCode transfer);
	
	ResponseEntity<?> WayaQRCodePaymentRedeem(HttpServletRequest request, WayaRedeemQRCode transfer);
	
	ResponseEntity<?> WayaPaymentRequestUsertoUser(HttpServletRequest request, WayaPaymentRequest transfer);
	
    ResponseEntity<?> PostOTPGenerate(HttpServletRequest request, String emailPhone);
	
	ResponseEntity<?> PostOTPVerify(HttpServletRequest request, WalletRequestOTP transfer);

	ResponseEntity<?>  getPendingNoneWayaPaymentRequest(String userId);

	ResponseEntity<?>  getPayoutNoneWayaPaymentRequest(String userId);

	ResponseEntity<?>  getReservedNoneWayaPaymentRequest(String userId);

	ResponseEntity<?>  getTotalNoneWayaPaymentRequest(String userId);

	ResponseEntity<?>  getExpierdNoneWayaPaymentRequest(String userId);

}
