package com.wayapaychat.temporalwallet.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import com.wayapaychat.temporalwallet.dto.WayaTradeDTO;
import com.wayapaychat.temporalwallet.entity.Transactions;
import com.wayapaychat.temporalwallet.entity.WalletTransaction;
import com.wayapaychat.temporalwallet.pojo.CardRequestPojo;
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

	ApiResponse<List<WalletTransaction>> findClientTransaction(String tranId);

	ApiResponse<List<AccountStatementDTO>> ReportTransaction(String accountNo);

	ApiResponse<Page<WalletTransaction>> getTransactionByWalletId(int page, int size, Long walletId);

	ApiResponse<Page<Transactions>> getTransactionByType(int page, int size, String transactionType);

	ApiResponse<Page<WalletTransaction>> findByAccountNumber(int page, int size, String accountNumber);

	ApiResponse<?> makeWalletTransaction(String command, TransferTransactionDTO transactionPojo);

	ApiResponse<?> sendMoney(TransferTransactionDTO transfer);

	ApiResponse<?> VirtuPaymentMoney(DirectTransactionDTO transfer);

	ResponseEntity<?> PostExternalMoney(HttpServletRequest request, CardRequestPojo transfer, Long userId);

	ApiResponse<?> OfficialMoneyTransfer(OfficeTransferDTO transfer);

	ApiResponse<?> OfficialUserTransfer(OfficeUserTransferDTO transfer);

	ApiResponse<?> createBulkTransaction(BulkTransactionCreationDTO bulk);

	ApiResponse<?> createBulkExcelTrans(MultipartFile file);

	ApiResponse<?> AdminsendMoney(AdminLocalTransferDTO transfer);

	ApiResponse<?> AdminCommissionMoney(CommissionTransferDTO transfer);

	ApiResponse<?> ClientCommissionMoney(ClientComTransferDTO transfer);

	ApiResponse<?> sendMoneyCustomer(WalletTransactionDTO transfer);

	ApiResponse<?> AdminSendMoneyCustomer(AdminWalletTransactionDTO transfer);

	ApiResponse<?> ClientSendMoneyCustomer(ClientWalletTransactionDTO transfer);

	ApiResponse<?> getStatement(String accountNumber);

	ApiResponse<?> EventTransferPayment(EventPaymentDTO eventPay);

	ApiResponse<?> EventBuySellPayment(WayaTradeDTO eventPay);

	ApiResponse<?> EventCommissionPayment(EventPaymentDTO eventPay);

	ApiResponse<?> sendMoneyCharge(WalletTransactionChargeDTO transfer);

	ApiResponse<?> TranReversePayment(ReverseTransactionDTO reverseDto) throws ParseException;

	ApiResponse<?> VirtuPaymentReverse(ReversePaymentDTO reverseDto) throws ParseException;

	ApiResponse<?> TranRevALLReport(Date date, Date todate);

	ApiResponse<?> PaymentTransAccountReport(Date date, Date todate, String accountNo);

	ApiResponse<?> PaymentAccountTrans(Date date, Date todate, String wayaNo);

	ApiResponse<?> PaymentOffTrans();

	ApiResponse<?> TranALLReverseReport();

	ApiResponse<?> statementReport(Date fromdate, Date todate, String acctNo);

	ApiResponse<?> BankTransferPayment(BankPaymentDTO transfer);

	ApiResponse<?> EventNonPayment(NonWayaPaymentDTO transfer);

	ApiResponse<?> EventNonRedeem(NonWayaPaymentDTO transfer);

	ApiResponse<?> TranChargeReport();

	ApiResponse<?> CommissionPaymentHistory();

	ResponseEntity<?> TransferNonPayment(HttpServletRequest request, NonWayaPaymentDTO transfer);

	ResponseEntity<?> NonWayaPaymentRedeem(HttpServletRequest request, NonWayaRedeemDTO transfer);

	ResponseEntity<?> NonWayaRedeemPIN(HttpServletRequest request, NonWayaPayPIN transfer);

}
