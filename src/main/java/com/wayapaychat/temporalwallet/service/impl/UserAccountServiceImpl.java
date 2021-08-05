package com.wayapaychat.temporalwallet.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.AccountStatementDTO;
import com.wayapaychat.temporalwallet.dto.AccountToggleDTO;
import com.wayapaychat.temporalwallet.dto.AdminAccountRestrictionDTO;
import com.wayapaychat.temporalwallet.dto.UserAccountDTO;
import com.wayapaychat.temporalwallet.dto.UserDTO;
import com.wayapaychat.temporalwallet.dto.WalletCashAccountDTO;
import com.wayapaychat.temporalwallet.dto.WalletEventAccountDTO;
import com.wayapaychat.temporalwallet.dto.WalletUserDTO;
import com.wayapaychat.temporalwallet.entity.WalletAccount;
import com.wayapaychat.temporalwallet.entity.WalletProduct;
import com.wayapaychat.temporalwallet.entity.WalletProductCode;
import com.wayapaychat.temporalwallet.entity.WalletTeller;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.pojo.AccountPojo2;
import com.wayapaychat.temporalwallet.pojo.UserDetailPojo;
import com.wayapaychat.temporalwallet.repository.WalletAccountRepository;
import com.wayapaychat.temporalwallet.repository.WalletProductCodeRepository;
import com.wayapaychat.temporalwallet.repository.WalletProductRepository;
import com.wayapaychat.temporalwallet.repository.WalletTellerRepository;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import com.wayapaychat.temporalwallet.util.ErrorResponse;
import com.wayapaychat.temporalwallet.util.ParamDefaultValidation;
import com.wayapaychat.temporalwallet.util.ReqIPUtils;
import com.wayapaychat.temporalwallet.util.SuccessResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

	@Autowired
	WalletUserRepository walletUserRepository;

	@Autowired
	WalletAccountRepository walletAccountRepository;

	@Autowired
	WalletProductRepository walletProductRepository;

	@Autowired
	WalletProductCodeRepository walletProductCodeRepository;

	@Autowired
	AuthUserServiceDAO authService;

	@Autowired
	ReqIPUtils reqUtil;

	@Autowired
	ParamDefaultValidation paramValidation;

	@Autowired
	WalletTellerRepository walletTellerRepository;

	@Autowired
	TemporalWalletDAO tempwallet;

	@Value("${waya.wallet.productcode}")
	private String wayaProduct;

	@Value("${waya.wallet.commissioncode}")
	private String wayaProductCommission;

	@Value("${waya.wallet.wayaglCode}")
	private String wayaGLCode;

	@Value("${waya.wallet.wayacommglCode}")
	private String wayaCommGLCode;

	public ResponseEntity<?> createUser(UserDTO user) {
		WalletUser existingUser = walletUserRepository.findByUserId(user.getUserId());
		if (existingUser != null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet User already exists"), HttpStatus.BAD_REQUEST);
		}
		int userId = (int) user.getUserId();
		UserDetailPojo wallet = authService.AuthUser(userId);
		if (wallet == null) {
			return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exists"), HttpStatus.BAD_REQUEST);
		}
		// Default Wallet
		// WalletUser userInfo = new ModelMapper().map(wallet, WalletUser.class);
		String acct_name = wallet.getFirstName().toUpperCase() + " " + wallet.getSurname().toUpperCase();
		WalletUser userInfo = new WalletUser("0000", user.getUserId(), wallet.getFirstName().toUpperCase(),
				wallet.getSurname().toUpperCase(), wallet.getEmail(), wallet.getPhoneNo(), acct_name, "", "",
				new Date(), "", new Date(), LocalDate.now(), 50000);

		WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
		WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);
		String acctNo = null;
		Integer rand = reqUtil.getAccountNo();
		if (rand == 0) {
			return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"), HttpStatus.BAD_REQUEST);
		}
		String acct_ownership = null;
		if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
				|| product.getProduct_type().equals("ODA")) && !product.isStaff_product_flg()) {
			acct_ownership = "C";
			if (product.getProduct_type().equals("SBA")) {
				acctNo = "201" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("CAA")) {
				acctNo = "501" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("ODA")) {
				acctNo = "401" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			}
		} else if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
				|| product.getProduct_type().equals("ODA")) && product.isStaff_product_flg()) {
			acct_ownership = "E";
			if (product.getProduct_type().equals("SBA")) {
				acctNo = "291" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("CAA")) {
				acctNo = "591" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("ODA")) {
				acctNo = "491" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			}
		} else if ((product.getProductCode() == "OAB")) {
			acct_ownership = "O";
			acctNo = product.getCrncy_code() + "0000" + rand;
		}

		try {
			String hashed_no = reqUtil
					.WayaEncrypt(userId + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());
			WalletUser userx = walletUserRepository.save(userInfo);

			WalletAccount account = new WalletAccount();
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA"))) {
				account = new WalletAccount("0000", "", acctNo, acct_name, userx, code.getGlSubHeadCode(), wayaProduct,
						acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
						LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
						product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
						product.getXfer_cr_limit(), false);
			}
			walletAccountRepository.save(account);
			WalletAccount caccount = new WalletAccount();

			// Commission Wallet
			if (user.isCorporate() && wallet.is_corporate()) {
				Optional<WalletAccount> acct = walletAccountRepository.findByProductCode(wayaProductCommission);
				if (!acct.isPresent()) {
					code = walletProductCodeRepository.findByProductGLCode(wayaProductCommission, wayaCommGLCode);
					product = walletProductRepository.findByProductCode(wayaProductCommission, wayaCommGLCode);
					acctNo = "901" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
					log.info(acctNo);
					hashed_no = reqUtil.WayaEncrypt(
							userId + "|" + acctNo + "|" + wayaProductCommission + "|" + product.getCrncy_code());
					acct_name = acct_name + " " + "COMMISSION ACCOUNT";
					if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
							|| product.getProduct_type().equals("ODA"))) {
						caccount = new WalletAccount("0000", "", acctNo, acct_name, userx, code.getGlSubHeadCode(),
								wayaProductCommission, acct_ownership, hashed_no, product.isInt_paid_flg(),
								product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
								product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
								product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(),
								false);
					}
					walletAccountRepository.save(caccount);
				}

			}

			return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> createUserAccount(WalletUserDTO user) {
		WalletUser existingUser = walletUserRepository.findByUserId(user.getUserId());
		if (existingUser != null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet User already exists"), HttpStatus.BAD_REQUEST);
		}
		int userId = user.getUserId().intValue();
		UserDetailPojo wallet = authService.AuthUser(userId);
		
		if (wallet == null) {
			return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exists"), HttpStatus.BAD_REQUEST);
		}
		log.info("Is it a corporate User: {}",wallet.is_corporate());
		// Default Wallet
		String acct_name = user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase();
		WalletUser userInfo = new WalletUser(user.getSolId(), user.getUserId(), user.getFirstName().toUpperCase(),
				user.getLastName().toUpperCase(), user.getEmailId(), user.getMobileNo(), acct_name,
				user.getCustTitleCode().toUpperCase(), user.getCustSex().toUpperCase(), user.getDob(),
				user.getCustIssueId(), user.getCustExpIssueDate(), LocalDate.now(), user.getCustDebitLimit());

		WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
		WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);
		String acctNo = null;
		Integer rand = reqUtil.getAccountNo();
		if (rand == 0) {
			return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"), HttpStatus.BAD_REQUEST);
		}
		String acct_ownership = null;
		if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
				|| product.getProduct_type().equals("ODA")) && !product.isStaff_product_flg()) {
			acct_ownership = "C";
			if (product.getProduct_type().equals("SBA")) {
				acctNo = "201" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("CAA")) {
				acctNo = "501" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("ODA")) {
				acctNo = "401" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			}
		} else if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
				|| product.getProduct_type().equals("ODA")) && product.isStaff_product_flg()) {
			acct_ownership = "E";
			if (product.getProduct_type().equals("SBA")) {
				acctNo = "291" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("CAA")) {
				acctNo = "591" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			} else if (product.getProduct_type().equals("ODA")) {
				acctNo = "491" + rand;
				if (acctNo.length() < 10) {
					acctNo = StringUtils.rightPad(acctNo, 10, "0");
				}
			}
		} else if ((product.getProductCode() == "OAB")) {
			acct_ownership = "O";
			acctNo = product.getCrncy_code() + "0000" + rand;
		}

		try {
			String hashed_no = reqUtil
					.WayaEncrypt(userId + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());
			WalletUser userx = walletUserRepository.save(userInfo);

			WalletAccount account = new WalletAccount();
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA"))) {
				account = new WalletAccount("0000", "", acctNo, acct_name, userx, code.getGlSubHeadCode(), wayaProduct,
						acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
						LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
						product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
						product.getXfer_cr_limit(), true);
			}
			walletAccountRepository.save(account);
			WalletAccount caccount = new WalletAccount();
			// Commission Wallet
			if (wallet.is_corporate()) {
				Optional<WalletAccount> acct = walletAccountRepository.findByAccountUser(userx);
				if (!acct.isPresent()) {
					code = walletProductCodeRepository.findByProductGLCode(wayaProductCommission, wayaCommGLCode);
					product = walletProductRepository.findByProductCode(wayaProductCommission, wayaCommGLCode);
					acctNo = "901" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
					log.info(acctNo);
					hashed_no = reqUtil.WayaEncrypt(
							userId + "|" + acctNo + "|" + wayaProductCommission + "|" + product.getCrncy_code());
					acct_name = acct_name + " " + "COMMISSION ACCOUNT";
					if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
							|| product.getProduct_type().equals("ODA"))) {
						caccount = new WalletAccount("0000", "", acctNo, acct_name, userx, code.getGlSubHeadCode(),
								wayaProductCommission, acct_ownership, hashed_no, product.isInt_paid_flg(),
								product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
								product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
								product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(),
								false);
					}
					walletAccountRepository.save(caccount);
				}

			}
			return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> modifyUserAccount(UserAccountDTO user) {
		WalletUser existingUser = walletUserRepository.findByUserId(user.getUserId());
		if (existingUser == null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet User does not exists"), HttpStatus.NOT_FOUND);
		}
		int userId = user.getUserId().intValue();
		UserDetailPojo wallet = authService.AuthUser(userId);
		if (wallet.is_deleted()) {
			return new ResponseEntity<>(new ErrorResponse("Auth User has been deleted"), HttpStatus.BAD_REQUEST);
		}
		if (!user.getNewEmailId().isBlank() && !user.getNewEmailId().isEmpty()) {
			WalletUser existingEmail = walletUserRepository.findByEmailAddress(user.getNewEmailId());
			if (existingEmail != null) {
				return new ResponseEntity<>(new ErrorResponse("Email already used on Wallet User Account"),
						HttpStatus.NOT_FOUND);
			}
			existingUser.setEmailAddress(user.getNewEmailId());
		}
		if (!user.getNewMobileNo().isBlank() && !user.getNewMobileNo().isEmpty()) {
			WalletUser existingPhone = walletUserRepository.findByMobileNo(user.getNewMobileNo());
			if (existingPhone != null) {
				return new ResponseEntity<>(new ErrorResponse("PhoneNo already used on Wallet User Account"),
						HttpStatus.NOT_FOUND);
			}
			existingUser.setMobileNo(user.getNewMobileNo());
		}
		// User Update
		if (user.getNewCustIssueId().isBlank() && user.getNewCustIssueId().isEmpty()) {
			existingUser.setCust_debit_limit(user.getNewCustDebitLimit());
		}
		if (user.getNewCustExpIssueDate() != null) {
			existingUser.setCust_exp_issue_date(user.getNewCustExpIssueDate());
		}
		if (user.getNewCustDebitLimit() != 0) {
			existingUser.setCust_issue_id(user.getNewCustIssueId());
		}
		// Default Wallet
		walletUserRepository.save(existingUser);
		if ((!user.getOldDefaultAcctNo().isBlank() || !user.getOldDefaultAcctNo().isEmpty())
				&& (!user.getNewDefaultAcctNo().isEmpty() || !user.getNewDefaultAcctNo().isBlank())) {
			try {
				WalletAccount account = walletAccountRepository.findByAccountNo(user.getOldDefaultAcctNo());
				if (account == null) {
					return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
							HttpStatus.NOT_FOUND);
				}
				account.setWalletDefault(false);
				walletAccountRepository.save(account);
				WalletAccount caccount = walletAccountRepository.findByAccountNo(user.getNewDefaultAcctNo());
				if (caccount == null) {
					return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
							HttpStatus.NOT_FOUND);
				}
				caccount.setWalletDefault(true);
				walletAccountRepository.save(caccount);
				return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
						HttpStatus.CREATED);
			} catch (Exception e) {
				return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(new SuccessResponse("Successfully Update Without No Account Affected"),
				HttpStatus.OK);
	}
	
	public ResponseEntity<?> ToggleAccount(AccountToggleDTO user) {
		WalletUser existingUser = walletUserRepository.findByUserId(user.getUserId());
		if (existingUser == null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet User does not exists"), HttpStatus.NOT_FOUND);
		}
		int userId = user.getUserId().intValue();
		UserDetailPojo wallet = authService.AuthUser(userId);
		if (wallet.is_deleted()) {
			return new ResponseEntity<>(new ErrorResponse("Auth User has been deleted"), HttpStatus.BAD_REQUEST);
		}
		// Default Wallet
		walletUserRepository.save(existingUser);
		if ((!user.getNewDefaultAcctNo().isEmpty() && !user.getNewDefaultAcctNo().isBlank())) {
			try {
				Optional<WalletAccount> accountDef = walletAccountRepository.findByDefaultAccount(existingUser);
				if (!accountDef.isPresent()) {
					return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
							HttpStatus.NOT_FOUND);
				}
				WalletAccount account = accountDef.get();
				account.setWalletDefault(false);
				walletAccountRepository.save(account);
				WalletAccount caccount = walletAccountRepository.findByAccountNo(user.getNewDefaultAcctNo());
				if (caccount == null) {
					return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
							HttpStatus.NOT_FOUND);
				}
				caccount.setWalletDefault(true);
				walletAccountRepository.save(caccount);
				return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
						HttpStatus.CREATED);
			} catch (Exception e) {
				return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(new SuccessResponse("Successfully Update Without No Account Affected"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> UserAccountAccess(AdminAccountRestrictionDTO user) {
		WalletUser userDelete = null;
		// Default Wallet
		try {
			WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNumber());
			if (account == null) {
				return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"), HttpStatus.NOT_FOUND);
			}
			userDelete = walletUserRepository.findByAccount(account);
			if (account.isAcct_cls_flg() && userDelete.isDel_flg()) {
				return new ResponseEntity<>(new SuccessResponse("Wallet Account Deleted Successfully"), HttpStatus.OK);
			}
			if (user.isAcctfreez()) {
				if (user.getFreezCode().equalsIgnoreCase("D")) {
					account.setFrez_code(user.getFreezCode());
					account.setFrez_reason_code(user.getFreezReason());
				} else if (user.getFreezCode().equalsIgnoreCase("C")) {
					account.setFrez_code(user.getFreezCode());
					account.setFrez_reason_code(user.getFreezReason());
				} else if (user.getFreezCode().equalsIgnoreCase("T")) {
					account.setFrez_code(user.getFreezCode());
					account.setFrez_reason_code(user.getFreezReason());
				} else {
					return new ResponseEntity<>(new ErrorResponse("Enter Correct Code"), HttpStatus.NOT_FOUND);
				}
			}
			if (user.isAcctClosed()) {
				if (account.getClr_bal_amt() == 0) {
					account.setAcct_cls_date(LocalDate.now());
					account.setAcct_cls_flg(true);
					String email = userDelete.getEmailAddress() + userDelete.getId();
					String phone = userDelete.getMobileNo() + userDelete.getId();
					Long userId = 1000000000L + userDelete.getUserId() + userDelete.getId();
					userDelete.setEmailAddress(email);
					userDelete.setMobileNo(phone);
					userDelete.setUserId(userId);
					userDelete.setDel_flg(true);
				} else {
					return new ResponseEntity<>(
							new ErrorResponse("Account balance must be equal to zero before it can be closed"),
							HttpStatus.NOT_FOUND);
				}
			}
			if (user.isAmountRestrict()) {
				double acctAmt = account.getLien_amt() + user.getLienAmount().doubleValue();
				account.setLien_amt(acctAmt);
				account.setLien_reason(user.getLienReason());
			}
			walletAccountRepository.save(account);
			walletUserRepository.save(userDelete);
			return new ResponseEntity<>(new SuccessResponse("Account updated successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> createCashAccount(WalletCashAccountDTO user) {

		boolean validate = paramValidation.validateDefaultCode(user.getCashAccountCode(), "Batch Account");
		if (!validate) {
			return new ResponseEntity<>(new ErrorResponse("Batch Account Validation Failed"), HttpStatus.BAD_REQUEST);
		}

		boolean validate2 = paramValidation.validateDefaultCode(user.getCrncyCode(), "Currency");
		if (!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}

		UserDetailPojo userd = authService.AuthUser((user.getUserId().intValue()));
		if (!userd.is_admin()) {
			return new ResponseEntity<>(new ErrorResponse("User Not Admin"), HttpStatus.BAD_REQUEST);
		}

		WalletProduct product = walletProductRepository.findByProductCode(user.getProductCode(), user.getProductGL());
		if ((!product.getProduct_type().equals("OAB"))) {
			return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
		}

		WalletProductCode code = walletProductCodeRepository.findByProductGLCode(user.getProductCode(),
				user.getProductGL());
		if ((!code.getProductType().equals("OAB"))) {
			return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
		}

		Optional<WalletTeller> tellerx = walletTellerRepository.findByUserCashAcct(user.getUserId(),
				user.getCrncyCode(), user.getCashAccountCode());
		if (!tellerx.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("User Cash Account Does Not Exist"), HttpStatus.BAD_REQUEST);
		}

		WalletTeller teller = tellerx.get();
		String acctNo = teller.getCrncyCode() + teller.getSol_id() + teller.getAdminCashAcct();
		String acct_ownership = "O";

		try {
			String hashed_no = reqUtil.WayaEncrypt(
					user.getUserId() + "|" + acctNo + "|" + user.getProductCode() + "|" + product.getCrncy_code());

			WalletAccount account = new WalletAccount();
			account = new WalletAccount(teller.getSol_id(), teller.getAdminCashAcct(), acctNo, user.getAccountName(),
					null, code.getGlSubHeadCode(), product.getProductCode(), acct_ownership, hashed_no,
					product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(),
					product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
					product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
					product.getXfer_cr_limit(), false);
			walletAccountRepository.save(account);
			return new ResponseEntity<>(new SuccessResponse("Office Account created successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> createEventAccount(WalletEventAccountDTO user) {

		boolean validate = paramValidation.validateDefaultCode(user.getPlaceholderCode(), "Batch Account");
		if (!validate) {
			return new ResponseEntity<>(new ErrorResponse("Batch Account Validation Failed"), HttpStatus.BAD_REQUEST);
		}

		boolean validate2 = paramValidation.validateDefaultCode(user.getCrncyCode(), "Currency");
		if (!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}

		WalletProduct product = walletProductRepository.findByProductCode(user.getProductCode(), user.getProductGL());
		if ((!product.getProduct_type().equals("OAB"))) {
			return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
		}

		WalletProductCode code = walletProductCodeRepository.findByProductGLCode(user.getProductCode(),
				user.getProductGL());
		if ((!code.getProductType().equals("OAB"))) {
			return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
		}

		String acctNo = product.getCrncy_code() + "0000" + user.getPlaceholderCode();
		String acct_ownership = "O";

		try {
			String hashed_no = reqUtil
					.WayaEncrypt(0L + "|" + acctNo + "|" + user.getProductCode() + "|" + product.getCrncy_code());

			WalletAccount account = new WalletAccount();
			account = new WalletAccount("0000", user.getPlaceholderCode(), acctNo, user.getAccountName(), null,
					code.getGlSubHeadCode(), product.getProductCode(), acct_ownership, hashed_no,
					product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(),
					product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
					product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
					product.getXfer_cr_limit(), false);
			walletAccountRepository.save(account);
			return new ResponseEntity<>(new SuccessResponse("Office Account created successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> createAccount(AccountPojo2 accountPojo) {
		int userId = accountPojo.getUserId().intValue();
		UserDetailPojo user = authService.AuthUser(userId);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exist"), HttpStatus.BAD_REQUEST);
		}
		WalletUser y = walletUserRepository.findByUserId(accountPojo.getUserId());
		WalletUser x = walletUserRepository.findByEmailAddress(user.getEmail());
		if (x == null && y == null) {
			return new ResponseEntity<>(new ErrorResponse("Default Wallet Not Created"), HttpStatus.BAD_REQUEST);
		}
		if (!y.getEmailAddress().equals(x.getEmailAddress())) {
			return new ResponseEntity<>(new ErrorResponse("Wallet Data Integity.please contact Admin"),
					HttpStatus.BAD_REQUEST);
		} else if (y.getEmailAddress().equals(x.getEmailAddress())) {
			WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
			WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);
			String acctNo = null;
			String acct_name = y.getFirstName().toUpperCase() + " " + y.getLastName().toUpperCase();
			Integer rand = reqUtil.getAccountNo();
			if (rand == 0) {
				return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"),
						HttpStatus.BAD_REQUEST);
			}
			String acct_ownership = null;
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA")) && !product.isStaff_product_flg()) {
				acct_ownership = "C";
				if (product.getProduct_type().equals("SBA")) {
					acctNo = "201" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("CAA")) {
					acctNo = "501" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("ODA")) {
					acctNo = "401" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				}
			} else if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA")) && product.isStaff_product_flg()) {
				acct_ownership = "E";
				if (product.getProduct_type().equals("SBA")) {
					acctNo = "291" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("CAA")) {
					acctNo = "591" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("ODA")) {
					acctNo = "491" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				}
			} else if ((product.getProduct_type().equals("OAB"))) {
				acct_ownership = "O";
				acctNo = product.getCrncy_code() + "0000" + rand;
			}

			try {
				String hashed_no = reqUtil
						.WayaEncrypt(userId + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());

				WalletAccount account = new WalletAccount();
				if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
						|| product.getProduct_type().equals("ODA"))) {
					account = new WalletAccount("0000", "", acctNo, acct_name, y, code.getGlSubHeadCode(), wayaProduct,
							acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
							LocalDate.now(), product.getCrncy_code(), product.getProduct_type(),
							product.isChq_book_flg(), product.getCash_dr_limit(), product.getXfer_dr_limit(),
							product.getCash_cr_limit(), product.getXfer_cr_limit(), false);
				}
				walletAccountRepository.save(account);
				return new ResponseEntity<>(new SuccessResponse("Account Created Successfully.", account),
						HttpStatus.CREATED);
			} catch (Exception e) {
				return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>(new ErrorResponse("Default Wallet has not been created.please contact Admin"),
					HttpStatus.NOT_FOUND);
		}

	}

	@Override
	public ApiResponse<?> findCustWalletById(Long walletId) {
		Optional<WalletUser> wallet = walletUserRepository.findById(walletId);
		if (!wallet.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Failed", null);
		}
		ApiResponse<?> resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "Success", wallet.get());
		return resp;
	}

	@Override
	public ApiResponse<?> findAcctWalletById(Long walletId) {
		Optional<WalletUser> wallet = walletUserRepository.findById(walletId);
		if (!wallet.isPresent()) {
			return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Failed", null);
		}
		List<WalletAccount> list = walletAccountRepository.findByUser(wallet.get());
		ApiResponse<?> resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "Success", list);
		return resp;
	}

	@Override
	public ResponseEntity<?> getListCommissionAccount(List<Integer> ids) {
		List<WalletAccount> accounts = new ArrayList<>();
		for (int id : ids) {
			Optional<WalletAccount> commissionAccount = null;
			Long l = Long.valueOf(id);
			Optional<WalletUser> userx = walletUserRepository.findById(l);
			if (!userx.isPresent()) {
				return new ResponseEntity<>(new ErrorResponse("Invalid User"), HttpStatus.BAD_REQUEST);
			}
			WalletUser user = userx.get();
			if (user != null) {
				commissionAccount = walletAccountRepository.findByAccountUser(user);
			}
			accounts.add(commissionAccount.get());
		}
		return new ResponseEntity<>(new SuccessResponse("Account name changed", accounts), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAccountInfo(String accountNo) {
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
		return new ResponseEntity<>(new SuccessResponse("Success.", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getUserAccountList(long userId) {
		int uId = (int) userId;
		UserDetailPojo ur = authService.AuthUser(uId);
		if (ur == null) {
			return new ResponseEntity<>(new ErrorResponse("User Id is Invalid"), HttpStatus.NOT_FOUND);
		}
		WalletUser x = walletUserRepository.findByEmailAddress(ur.getEmail());
		if (x == null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet User does not exist"), HttpStatus.NOT_FOUND);
		}
		List<WalletAccount> accounts = walletAccountRepository.findByUser(x);
		return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllAccount() {
		List<WalletAccount> pagedResult = walletAccountRepository.findAll();
		return new ResponseEntity<>(new SuccessResponse("Success.", pagedResult), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getUserCommissionList(long userId) {
		WalletUser userx = walletUserRepository.findByUserId(userId);
		if (userx == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
		}
		Optional<WalletAccount> accounts = walletAccountRepository.findByAccountUser(userx);
		if (!accounts.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("No Commission Account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> makeDefaultWallet(String accountNo) {
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
		if (account == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Account No"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Default wallet set", account), HttpStatus.OK);

	}

	@Override
	public ResponseEntity<?> UserWalletLimit(long userId) {
		WalletUser user = walletUserRepository.findByUserId(userId);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("User Wallet Info", user), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getALLCommissionAccount() {
		Optional<WalletAccount> account = walletAccountRepository.findByProductCode(wayaProductCommission);
		if (!account.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Wallet Commissions", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAccountCommission(String accountNo) {
		Optional<WalletAccount> account = walletAccountRepository.findByAccountProductCode(wayaProductCommission,
				accountNo);
		if (!account.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Wallet Commissions", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAccountDefault(Long user_id) {
		WalletUser user = walletUserRepository.findByUserId(user_id);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
		}
		Optional<WalletAccount> account = walletAccountRepository.findByDefaultAccount(user);
		if (!account.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to fetch default account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Wallet Default", account), HttpStatus.OK);
	}

	public ResponseEntity<?> searchAccount(String search) {
		List<WalletUser> user = walletUserRepository.findAll();
		List<WalletUser> matchingAcct = new ArrayList<WalletUser>();
		List<WalletUser> matchingAcct2 = new ArrayList<WalletUser>();
		List<WalletAccount> account = new ArrayList<WalletAccount>();
		Collection<WalletAccount> accountColl = new ArrayList<WalletAccount>();
		for(WalletUser col : user) {
			if(col.getUserId() == Long.valueOf(search)) {
				matchingAcct2.add(col);
			}
		}
		if (!matchingAcct2.isEmpty()) {
			for (WalletUser x : matchingAcct2) {
				account = walletAccountRepository.findByUser(x);
			}
			return new ResponseEntity<>(new SuccessResponse("Account Wallet Search", account), HttpStatus.OK);
		}
		
		matchingAcct = user.stream().filter(str -> str.getMobileNo().trim().equalsIgnoreCase(search))
				.collect(Collectors.toList());
		if (!matchingAcct.isEmpty()) {
			for (WalletUser x : matchingAcct) {
				account = walletAccountRepository.findByUser(x);
				accountColl.addAll(account);
			}
			return new ResponseEntity<>(new SuccessResponse("Account Wallet Search", accountColl), HttpStatus.OK);
		}
		
		matchingAcct = user.stream().filter(str -> str.getEmailAddress().trim().equalsIgnoreCase(search))
				.collect(Collectors.toList());
		if (!matchingAcct.isEmpty()) {
			for (WalletUser x : matchingAcct) {
				account = walletAccountRepository.findByUser(x);
				accountColl.addAll(account);
			}
			return new ResponseEntity<>(new SuccessResponse("Account Wallet Search", accountColl), HttpStatus.OK);
		}
		return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.NOT_FOUND);
	}

	@Override
	public ApiResponse<?> fetchTransaction(String acctNo) {
		List<AccountStatementDTO> account = tempwallet.fetchTransaction(acctNo);
		if (account.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "IMBALANCE TRANSACTION", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESSFUL TRANSACTION STATEMENT", account);
	}

	@Override
	public ResponseEntity<?> getListWayaAccount() {
		List<WalletAccount> account = walletAccountRepository.findByWayaAccount();
		if (account.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("NO WAYA ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("LIST WAYA ACCOUNT", account), HttpStatus.OK);
	}

}
