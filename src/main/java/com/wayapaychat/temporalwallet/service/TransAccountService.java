package com.wayapaychat.temporalwallet.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.wayapaychat.temporalwallet.pojo.TransWallet;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.dto.AdminLocalTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.AdminWalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.BankPaymentDTO;
import com.wayapaychat.temporalwallet.dto.BulkTransactionCreationDTO;
import com.wayapaychat.temporalwallet.dto.ClientComTransferDTO;
import com.wayapaychat.temporalwallet.dto.ClientWalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.CommissionTransferDTO;
import com.wayapaychat.temporalwallet.dto.DirectTransactionDTO;
import com.wayapaychat.temporalwallet.dto.EventOfficePaymentDTO;
import com.wayapaychat.temporalwallet.dto.EventPaymentDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaPayPIN;
import com.wayapaychat.temporalwallet.dto.NonWayaPaymentDTO;
import com.wayapaychat.temporalwallet.dto.NonWayaRedeemDTO;
import com.wayapaychat.temporalwallet.dto.OfficeTransferDTO;
import com.wayapaychat.temporalwallet.dto.OfficeUserTransferDTO;
import com.wayapaychat.temporalwallet.dto.ReversePaymentDTO;
import com.wayapaychat.temporalwallet.dto.ReverseTransactionDTO;
import com.wayapaychat.temporalwallet.dto.TransferTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WalletAdminTransferDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionChargeDTO;
import com.wayapaychat.temporalwallet.dto.WalletTransactionDTO;
import com.wayapaychat.temporalwallet.dto.WayaPaymentQRCode;
import com.wayapaychat.temporalwallet.dto.WayaPaymentRequest;
import com.wayapaychat.temporalwallet.dto.WayaRedeemQRCode;
import com.wayapaychat.temporalwallet.dto.WayaTradeDTO;
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

	ApiResponse<?> AdminCommissionMoney(HttpServletRequest request, CommissionTransferDTO transfer);

	ApiResponse<?> ClientCommissionMoney(HttpServletRequest request, ClientComTransferDTO transfer);

	ApiResponse<?> sendMoneyCustomer(HttpServletRequest request, WalletTransactionDTO transfer);

	ApiResponse<?> AdminSendMoneyCustomer(HttpServletRequest request, AdminWalletTransactionDTO transfer);

	ApiResponse<?> ClientSendMoneyCustomer(HttpServletRequest request, ClientWalletTransactionDTO transfer);

	ApiResponse<?> getStatement(String accountNumber);

	ResponseEntity<?> EventTransferPayment(HttpServletRequest request, EventPaymentDTO eventPay);
	
	ResponseEntity<?> EventOfficePayment(HttpServletRequest request, EventOfficePaymentDTO eventPay);

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
