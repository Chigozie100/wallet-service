package com.wayapaychat.temporalwallet.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.pojo.TransWallet;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
import com.wayapaychat.temporalwallet.pojo.WalletRequestOTP;
import com.wayapaychat.temporalwallet.response.ApiResponse;

public interface TransAccountService {

	ResponseEntity<?> adminTransferForUser(HttpServletRequest request, String command,
			AdminUserTransferDTO adminTranser);

	ResponseEntity<?> cashTransferByAdmin(HttpServletRequest request, String command,
			WalletAdminTransferDTO adminTranser);

	ApiResponse<Page<WalletTransaction>> findAllTransaction(int page, int size);

	ApiResponse<List<WalletTransaction>> findClientTransaction(String tranId);

	ApiResponse<List<AccountStatementDTO>> ReportTransaction(String accountNo);

	ApiResponse<Page<WalletTransaction>> getTransactionByWalletId(int page, int size, Long walletId);

	ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber);

	ResponseEntity<?> makeWalletTransaction(HttpServletRequest request, String command,
			TransferTransactionDTO transactionPojo);

	ResponseEntity<?> sendMoney(HttpServletRequest request, TransferTransactionDTO transfer);

	ResponseEntity<?> sendMoneyToSimulatedUser(HttpServletRequest request, List<TransferSimulationDTO> transfer);

	ResponseEntity<?> VirtuPaymentMoney(HttpServletRequest request, DirectTransactionDTO transfer);

	ResponseEntity<?> PostExternalMoney(HttpServletRequest request, CardRequestPojo transfer, Long userId);

	ResponseEntity<?> OfficialMoneyTransferSw(HttpServletRequest request, OfficeTransferDTO transfer);

	ResponseEntity<?> doOfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer);

	ApiResponse<?> OfficialUserTransfer(HttpServletRequest request, OfficeUserTransferDTO transfer, boolean isMifos);

	ResponseEntity<?> OfficialUserTransferSystemSwitch(Map<String, String> mapp, String token,
			HttpServletRequest request, OfficeUserTransferDTO transfer);

	ResponseEntity<?> OfficialUserTransferMultiple(HttpServletRequest request, List<OfficeUserTransferDTO> transfer);

	ResponseEntity<?> createBulkTransaction(HttpServletRequest request, BulkTransactionCreationDTO bulk);

	ResponseEntity<?> createBulkExcelTrans(HttpServletRequest request, MultipartFile file);
        
        ResponseEntity<?> createBulkDebitTransaction(HttpServletRequest request, BulkTransactionCreationDTO bulk);

	ResponseEntity<?> createBulkDebitExcelTrans(HttpServletRequest request, MultipartFile file);

	ApiResponse<?> AdminsendMoney(HttpServletRequest request, AdminLocalTransferDTO transfer);

	ApiResponse<?> AdminSendMoneyMultiple(HttpServletRequest request, List<AdminLocalTransferDTO> transfer);

	ResponseEntity<?> AdminCommissionMoney(HttpServletRequest request, CommissionTransferDTO transfer);

	ResponseEntity<?> ClientCommissionMoney(HttpServletRequest request, ClientComTransferDTO transfer);

	ResponseEntity<?> sendMoneyCustomer(HttpServletRequest request, WalletTransactionDTO transfer);

	ApiResponse<?> AdminSendMoneyCustomer(HttpServletRequest request, AdminWalletTransactionDTO transfer);

	ResponseEntity<?> ClientSendMoneyCustomer(HttpServletRequest request, ClientWalletTransactionDTO transfer);

	ApiResponse<?> getStatement(String accountNumber);

	ResponseEntity<?> EventReversePaymentRequest(HttpServletRequest request, EventPaymentRequestReversal eventPay);

	ResponseEntity<?> EventTransferPayment(HttpServletRequest request, EventPaymentDTO eventPay, boolean isMifos);
  
	ResponseEntity<?> EventOfficePayment(HttpServletRequest request, EventOfficePaymentDTO eventPay);

	ResponseEntity<?> TemporalWalletToOfficialWallet(HttpServletRequest request, TemporalToOfficialWalletDTO transfer);

	ResponseEntity<?> TemporalWalletToOfficialWalletMutiple(HttpServletRequest request,
			List<TemporalToOfficialWalletDTO> transfer);

	ApiResponse<?> EventBuySellPayment(HttpServletRequest request, WayaTradeDTO eventPay);

	ResponseEntity<?> EventCommissionPayment(HttpServletRequest request, EventPaymentDTO eventPay);

	ResponseEntity<?> sendMoneyCharge(HttpServletRequest request, WalletTransactionChargeDTO transfer);

	ApiResponse<?> VirtuPaymentReverse(HttpServletRequest request, ReversePaymentDTO reverseDto) throws ParseException;

	ApiResponse<?> TranRevALLReport(Date date, Date todate);

	ApiResponse<?> PaymentTransAccountReport(Date date, Date todate, String accountNo);

	ApiResponse<?> PaymentAccountTrans(Date date, Date todate, String wayaNo);

	ApiResponse<?> viewTransActivities(String userId);

	ApiResponse<?> PaymentOffTrans(int page, int size, String filter);

	ApiResponse<?> getAllTransactions(int page, int size, String filter, LocalDate fromdate, LocalDate todate);

	ApiResponse<?> getAllTransactionsByAccountNo(int page, int size, String filter, LocalDate fromdate,
			LocalDate todate, String accountNo);

	ApiResponse<?> TranALLReverseReport();

	ApiResponse<?> statementReport(Date fromdate, Date todate, String acctNo);

	List<TransWallet> statementReport2(Date fromdate, Date todate, String acctNo);

	ApiResponse<List<AccountStatementDTO>> ReportTransaction2(String accountNo);

	ApiResponse<?> PaymentTransFilter(String acctNo);

	ResponseEntity<?> BankTransferPayment(HttpServletRequest request, BankPaymentDTO transfer);

	ResponseEntity<?> BankTransferPaymentOfficial(HttpServletRequest request, BankPaymentOfficialDTO transfer);

	ResponseEntity<?> BankTransferPaymentOfficialMultiple(HttpServletRequest request,
			List<BankPaymentOfficialDTO> transfer);

	ResponseEntity<?> EventNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer);

	ResponseEntity<?> EventNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer);

	ResponseEntity<?> getListOfNonWayaTransfers(HttpServletRequest request, String userId, int page, int size);

	ResponseEntity<?> listOfNonWayaTransfers(HttpServletRequest request, int page, int size);

	ResponseEntity<?> EventNonRedeem(HttpServletRequest request, NonWayaPaymentDTO transfer);

	ResponseEntity<?> EventNonRedeemMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer);

	ResponseEntity<?> transferToNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer);

	ApiResponse<?> TranChargeReport();

	ApiResponse<?> CommissionPaymentHistory();

	ResponseEntity<?> TransferNonPaymentMultiple(HttpServletRequest request, List<NonWayaPaymentDTO> transfer);

	ResponseEntity<?> TransferNonPaymentMultipleUpload(HttpServletRequest request, MultipartFile file);

	ResponseEntity<?> TransferNonPaymentSingleWayaOfficial(HttpServletRequest request,
			NonWayaPaymentMultipleOfficialDTO transfer);

	ResponseEntity<?> TransferNonPaymentMultipleWayaOfficial(HttpServletRequest request,
			List<NonWayaPaymentMultipleOfficialDTO> transfer);

	ResponseEntity<?> TransferNonPaymentWayaOfficialExcel(HttpServletRequest request, MultipartFile file);

	ByteArrayInputStream createExcelSheet(String isNoneWaya);

	ResponseEntity<?> NonWayaPaymentRedeem(HttpServletRequest request, NonWayaRedeemDTO transfer);

	ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request, NonWayaPayPIN transfer);

	ResponseEntity<?> WayaQRCodePayment(HttpServletRequest request, WayaPaymentQRCode transfer);

	ResponseEntity<?> WayaQRCodePaymentRedeem(HttpServletRequest request, WayaRedeemQRCode transfer);

	ResponseEntity<?> WayaPaymentRequestUsertoUser(HttpServletRequest request, WayaPaymentRequest transfer);

	ResponseEntity<?> PostOTPGenerate(HttpServletRequest request, String emailPhone);

	ResponseEntity<?> PostOTPVerify(HttpServletRequest request, WalletRequestOTP transfer);

	ResponseEntity<?> getPendingNoneWayaPaymentRequest(String userId);

	ResponseEntity<?> getPayoutNoneWayaPaymentRequest(String userId);

	ResponseEntity<?> getReservedNoneWayaPaymentRequest(String userId);

	ResponseEntity<?> getTotalNoneWayaPaymentRequest(String userId);

	ResponseEntity<?> getExpierdNoneWayaPaymentRequest(String userId);

	ResponseEntity<?> getTotalNoneWayaPaymentRequestAmount(String userId);

	ResponseEntity<?> getReservedNoneWayaPaymentRequestAmount(String userId);

	ResponseEntity<?> getPayoutNoneWayaPaymentRequestAmount(String userId);

	ResponseEntity<?> getPendingNoneWayaPaymentRequestAmount(String userId);

	ResponseEntity<?> getExpierdNoneWayaPaymentRequestAmount(String userId);

	ResponseEntity<?> debitTransactionAmount();

	ResponseEntity<?> creditTransactionAmount();

	ResponseEntity<?> debitAndCreditTransactionAmount();

	ResponseEntity<?> debitTransactionAmountOffical();

	ResponseEntity<?> creditTransactionAmountOffical();

	ResponseEntity<?> debitAndCreditTransactionAmountOfficial();

	BigDecimal computeTransFee(String accountDebit, BigDecimal amount, String eventId);

	ResponseEntity<?> getSingleAccountByEventID(String eventId);

	WalletAccount findByEmailOrPhoneNumberOrId(Boolean isAccountNumber, String value, String userId, String accountNo);

	ApiResponse<?> OfficialAccountReports(int page, int size, LocalDate fromdate, LocalDate todate, String fillter);
}
