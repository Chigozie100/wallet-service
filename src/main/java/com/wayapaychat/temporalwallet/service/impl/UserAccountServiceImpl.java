package com.wayapaychat.temporalwallet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.entity.*;
import com.wayapaychat.temporalwallet.enumm.ResponseCodes;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.pojo.*;
import com.wayapaychat.temporalwallet.repository.*;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.CoreBankingService;
import com.wayapaychat.temporalwallet.service.SwitchWalletService;
import com.wayapaychat.temporalwallet.service.UserAccountService;
import com.wayapaychat.temporalwallet.service.UserPricingService;
import com.wayapaychat.temporalwallet.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserAccountServiceImpl implements UserAccountService {

	private final WalletUserRepository walletUserRepository;
	private final WalletAccountRepository walletAccountRepository;
	private final WalletProductRepository walletProductRepository;
	private final WalletProductCodeRepository walletProductCodeRepository;
	private final AuthUserServiceDAO authService;
	private final ReqIPUtils reqUtil;
	private final ParamDefaultValidation paramValidation;
	private final WalletTellerRepository walletTellerRepository;
	private final TemporalWalletDAO tempwallet;
	private final WalletEventRepository walletEventRepo;
	private final TokenImpl tokenService;
	private final UserPricingService userPricingService;
	private final CoreBankingService coreBankingService;
	private final SwitchWalletService switchWalletService;
	@Autowired
	private ObjectMapper objectMapper;

	@Value("${waya.wallet.productcode}")
	private String wayaProduct;

	@Value("${waya.wallet.commissioncode}")
	private String wayaProductCommission;

	@Value("${waya.wallet.wayaglCode}")
	private String wayaGLCode;

	@Value("${waya.wallet.wayacommglCode}")
	private String wayaCommGLCode;

	@Value("${ofi.financialInstitutionCode}")
	private String financialInstitutionCode;

	@Value("${waya.wallet.systemuser.email:wayabanksystem@wayapaychat.com}")
	private String systemUserEmail;

	@Value("${waya.wallet.systemuser.phone:2340000000000}")
	private String systemUserMobileNumber;

	@Autowired
	public UserAccountServiceImpl(WalletUserRepository walletUserRepository,
			WalletAccountRepository walletAccountRepository, WalletProductRepository walletProductRepository,
			WalletProductCodeRepository walletProductCodeRepository, AuthUserServiceDAO authService, ReqIPUtils reqUtil,
			ParamDefaultValidation paramValidation,
			WalletTellerRepository walletTellerRepository, TemporalWalletDAO tempwallet,
			WalletEventRepository walletEventRepo,
			TokenImpl tokenService, UserPricingService userPricingService,
			CoreBankingService coreBankingService, SwitchWalletService switchWalletService) {
		this.walletUserRepository = walletUserRepository;
		this.walletAccountRepository = walletAccountRepository;
		this.walletProductRepository = walletProductRepository;
		this.walletProductCodeRepository = walletProductCodeRepository;
		this.authService = authService;
		this.reqUtil = reqUtil;
		this.paramValidation = paramValidation;
		this.walletTellerRepository = walletTellerRepository;
		this.tempwallet = tempwallet;
		this.walletEventRepo = walletEventRepo;
		this.tokenService = tokenService;
		this.userPricingService = userPricingService;
		this.coreBankingService = coreBankingService;
        this.switchWalletService = switchWalletService;
	}

	public String generateRandomNumber(int length) {

		int randNumOrigin = generateRandomNumber(58, 34);
		int randNumBound = generateRandomNumber(354, 104);

		SecureRandom random = new SecureRandom();
		return random.ints(randNumOrigin, randNumBound + 1)
				.filter(Character::isDigit)
				.limit(length)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint,
						StringBuilder::append)
				.toString();
	}

	public int generateRandomNumber(int max, int min) {
		return (int) (Math.random() * (max - min + 1) + min);
	}

	public ResponseEntity<?> createUser(UserDTO user) {
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

		WalletUser existingUser = walletUserRepository.findByUserId(user.getUserId());
		if (existingUser == null) {

			String code = generateRandomNumber(9);
			WalletUserDTO userInfow = new WalletUserDTO("0000", user.getUserId(), wallet.getFirstName().toUpperCase(),
					wallet.getSurname().toUpperCase(),
					wallet.getEmail(), wallet.getPhoneNo(), new Date(), new BigDecimal("0.0").doubleValue(), "MR",
					"M", code,
					new Date(), user.getAccountType(), false, user.getDescription());

			ResponseEntity<?> res = createUserAccount(userInfow);

			System.out.println("RES" + res);
			if (res.getStatusCode().is2xxSuccessful()) {
				return new ResponseEntity<>(new SuccessResponse("Wallet created successfully"), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new ErrorResponse("Wallet User Does not exists"), HttpStatus.BAD_REQUEST);
			}

		}

		WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
		WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);
		String acctNo = null;
		Integer rand = reqUtil.getAccountNo();
		if (rand == 0) {
			return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"), HttpStatus.BAD_REQUEST);
		}
		String acct_ownership = null;
		if (!existingUser.getCust_sex().equals("S")) {
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
		} else {
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA")) && !product.isStaff_product_flg()) {
				acct_ownership = "C";
				if (product.getProduct_type().equals("SBA")) {
					acctNo = "701" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("CAA")) {
					acctNo = "101" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("ODA")) {
					acctNo = "717" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				}
			}
		}

		if (user.getAccountType() == null) {
			user.setAccountType("SAVINGS");
		}

		if (user.getDescription() == null) {
			user.setDescription("SAVINGS ACCOUNT");
		}
		String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, user.getAccountType());
		try {
			String hashed_no = reqUtil
					.WayaEncrypt(userId + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());
			WalletUser userx = walletUserRepository.save(userInfo);

			WalletAccount account = new WalletAccount();
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA"))) {
						account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, userx,
						code.getGlSubHeadCode(), wayaProduct,
						acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
						LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
						product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
						product.getXfer_cr_limit(), false, user.getAccountType(), user.getDescription());
			}

			coreBankingService.createAccount(userx, account);

			WalletAccount caccount = new WalletAccount();
			// Commission Wallet
			if (user.isCorporate() && wallet.is_corporate()) {
				Optional<WalletAccount> acct = walletAccountRepository.findByProductCode(wayaProductCommission);
				if (!acct.isPresent()) {
					code = walletProductCodeRepository.findByProductGLCode(wayaProductCommission, wayaCommGLCode);
					product = walletProductRepository.findByProductCode(wayaProductCommission, wayaCommGLCode);
					if (!existingUser.getCust_sex().equals("S")) {
						acctNo = "901" + rand;
						if (acctNo.length() < 10) {
							acctNo = StringUtils.rightPad(acctNo, 10, "0");
						}
					} else {
						acctNo = "621" + rand;
						if (acctNo.length() < 10) {
							acctNo = StringUtils.rightPad(acctNo, 10, "0");
						}
					}
					log.info(acctNo);
					hashed_no = reqUtil.WayaEncrypt(
							userId + "|" + acctNo + "|" + wayaProductCommission + "|" + product.getCrncy_code());
					acct_name = acct_name + " " + "COMMISSION ACCOUNT";
					if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
							|| product.getProduct_type().equals("ODA"))) {
						caccount = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, userx,
								code.getGlSubHeadCode(),
								wayaProductCommission, acct_ownership, hashed_no, product.isInt_paid_flg(),
								product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
								product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
								product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(),
								false, user.getAccountType(), user.getDescription());
					}

					coreBankingService.createAccount(userx, caccount);
				}

			}

			return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public WalletAccount createNubanAccount(WalletUserDTO user) {
		WalletUser existingUser = walletUserRepository.findByUserId(user.getUserId());
		if (existingUser != null) {
			log.info("Wallet User already exists");
			return null;
		}

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
			log.info("Unable to generate Wallet Account");
			return null;
		}
		String acct_ownership = null;
		if (!user.getCustSex().equals("S")) {
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
		} else {
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA")) && !product.isStaff_product_flg()) {
				acct_ownership = "C";
				if (product.getProduct_type().equals("SBA")) {
					acctNo = "701" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("CAA")) {
					acctNo = "101" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("ODA")) {
					acctNo = "717" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				}
			}
		}

		if (user.getDescription() == null) {
			user.setDescription("SAVINGS ACCOUNT");
		}

		String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, user.getAccountType());
		try {
			String hashed_no = reqUtil
					.WayaEncrypt(user.getUserId() + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());
			WalletUser userx = walletUserRepository.save(userInfo);

			WalletAccount account = new WalletAccount();
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA"))) {
				account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, userx,
						code.getGlSubHeadCode(), wayaProduct,
						acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
						LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
						product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
						product.getXfer_cr_limit(), true, user.getAccountType(), user.getDescription());
			}

			account.setWalletDefault(true);
			coreBankingService.createAccount(userx, account);

			return account;
		} catch (Exception e) {
			throw new CustomException(e.getLocalizedMessage(), HttpStatus.EXPECTATION_FAILED);

		}
	}

	@Override
	public void setupSystemUser(){
		WalletUser walletUser = walletUserRepository.findByEmailAddress(systemUserEmail);
		if (ObjectUtils.isNotEmpty(walletUser)) {
			return;
		}

		WalletUser userInfo = new WalletUser("0000", 0L, "SYSTEM",
				"ACCOUNT", systemUserEmail, systemUserMobileNumber, "SYSTEM ACCOUNT",
				null, null, null,
				null, null, LocalDate.now(), 100000000.0);
		
		walletUserRepository.save(userInfo);

	}

	public WalletUser creatUserAccountUtil(UserDetailPojo userDetailPojo) {

		log.info("userDetailPojo :: " + userDetailPojo);
		WalletUserDTO user = new WalletUserDTO();
		// builderPOST(userDetailPojo);

		// Default Wallet
		String acct_name = user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase();
		WalletUser userInfo = new WalletUser(user.getSolId(), user.getUserId(), user.getFirstName().toUpperCase(),
				user.getLastName().toUpperCase(), user.getEmailId(), user.getMobileNo(), acct_name,
				user.getCustTitleCode().toUpperCase(), user.getCustSex().toUpperCase(), user.getDob(),
				user.getCustIssueId(), user.getCustExpIssueDate(), LocalDate.now(), user.getCustDebitLimit());
		WalletUser userx = walletUserRepository.save(userInfo);
		return userx;
	}

	// Call by Aut-service and others
	public ResponseEntity<?> createUserAccount(WalletUserDTO user) {
		log.info(" ###################### CALL TO CREATE WALLET ##################" + user);
		WalletUser existingUser = walletUserRepository.findByUserId(user.getUserId());
		if (existingUser != null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet User already exists"), HttpStatus.BAD_REQUEST);
		}

		int userId = user.getUserId().intValue();
		UserDetailPojo wallet = authService.AuthUser(userId);

		if (wallet == null) {
			return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exists"), HttpStatus.BAD_REQUEST);
		}
		log.info("Is it a corporate User: {}", wallet.is_corporate());

		// Default Wallet
		String acct_name = user.getFirstName().toUpperCase() + " " + user.getLastName().toUpperCase();
		WalletUser userInfo = new WalletUser(user.getSolId(), user.getUserId(), user.getFirstName().toUpperCase(),
				user.getLastName().toUpperCase(), user.getEmailId(), user.getMobileNo(), acct_name,
				user.getCustTitleCode().toUpperCase(), user.getCustSex().toUpperCase(), user.getDob(),
				user.getCustIssueId(), user.getCustExpIssueDate(), LocalDate.now(), user.getCustDebitLimit(),
				user.isCorporate());

		WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
		WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);

		String acctNo = null;
		Integer rand = reqUtil.getAccountNo();
		if (rand == 0) {
			return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"), HttpStatus.BAD_REQUEST);
		}

		String acct_ownership = null;
		if (!user.getCustSex().equals("S")) {
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
		} else {
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA")) && !product.isStaff_product_flg()) {
				acct_ownership = "C";
				if (product.getProduct_type().equals("SBA")) {
					acctNo = "701" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("CAA")) {
					acctNo = "101" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				} else if (product.getProduct_type().equals("ODA")) {
					acctNo = "717" + rand;
					if (acctNo.length() < 10) {
						acctNo = StringUtils.rightPad(acctNo, 10, "0");
					}
				}
			}
		}

		String accountType = user.getAccountType();
		switch (accountType) {
			case "ledger":
				accountType = Constant.LEDGER;
				break;
			case "fixed":
				accountType = Constant.FIXED;
				break;
			case "loan":
				accountType = Constant.LOAN;
				break;
			case "current 1":
				accountType = Constant.CURRENT;
				break;
			case "current 2":
				accountType = Constant.CURRENT_TWO;
				break;
			case "savings 6":
				accountType = Constant.SAVINGS_SIX;
				break;
			case "savings 5":
				accountType = Constant.SAVINGS_FIVE;
				break;
			case "savings 4":
				accountType = Constant.SAVINGS_FOUR;
				break;
			case "savings 3":
				accountType = Constant.SAVINGS_THREE;
				break;
			case "savings 2":
				accountType = Constant.SAVINGS_TWO;
				break;
			case "savings 1":
				accountType = Constant.SAVINGS;
				break;
			default:
				accountType = Constant.SAVINGS;
				break;
		}

		if (user.getDescription() == null) {
			user.setDescription("SAVINGS ACCOUNT");
		}

		String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, accountType);
		try {
			String hashed_no = reqUtil
					.WayaEncrypt(userId + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());
			WalletUser userx = walletUserRepository.save(userInfo);

			WalletAccount account = new WalletAccount();
			if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
					|| product.getProduct_type().equals("ODA"))) {
				account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, userx,
						code.getGlSubHeadCode(), wayaProduct,
						acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
						LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
						product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
						product.getXfer_cr_limit(), true, accountType, user.getDescription());
			}

			account.setWalletDefault(true);
			coreBankingService.createAccount(userInfo, account);
			
			WalletAccount caccount = new WalletAccount();
			// Commission Wallet
			if (wallet.is_corporate()) {
				Optional<WalletAccount> acct = walletAccountRepository.findByAccountUser(userx);
				String _nubanAccountNumber = Util.generateNuban(financialInstitutionCode, accountType);
				if (!acct.isPresent()) {
					code = walletProductCodeRepository.findByProductGLCode(wayaProductCommission, wayaCommGLCode);
					product = walletProductRepository.findByProductCode(wayaProductCommission, wayaCommGLCode);
					if (!user.getCustSex().equals("S")) {
						acctNo = "901" + rand;
						if (acctNo.length() < 10) {
							acctNo = StringUtils.rightPad(acctNo, 10, "0");
						}
					} else {
						acctNo = "621" + rand;
						if (acctNo.length() < 10) {
							acctNo = StringUtils.rightPad(acctNo, 10, "0");
						}
					}
					log.info(acctNo);
					hashed_no = reqUtil.WayaEncrypt(
							userId + "|" + acctNo + "|" + wayaProductCommission + "|" + product.getCrncy_code());
					acct_name = acct_name + " " + "COMMISSION ACCOUNT";
					user.setDescription("COMMISSION ACCOUNT");
					if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
							|| product.getProduct_type().equals("ODA"))) {
						caccount = new WalletAccount("0000", "", acctNo, _nubanAccountNumber, acct_name, userx,
								code.getGlSubHeadCode(),
								wayaProductCommission, acct_ownership, hashed_no, product.isInt_paid_flg(),
								product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
								product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
								product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(),
								false, accountType, user.getDescription());
					}

					coreBankingService.createAccount(userInfo, caccount);

				}
			}

			// call to create a user pricing
			CompletableFuture.runAsync(() -> userPricingService.createUserPricing(userx));

			// setup user pricing

			return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			System.out.println("Error Account Creation :::: " + e.getMessage());
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}


	public ResponseEntity<?> createNubbanAccountAuto() {
		return new ResponseEntity<>(new SuccessResponse("Successfully createAccountOnMIFOS"),
				HttpStatus.OK);
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

				List<WalletAccount> listAcct = walletAccountRepository.findByUser(existingUser);
				for (WalletAccount data : listAcct) {
					data.setWalletDefault(false);
					walletAccountRepository.save(data);
				}

				caccount.setWalletDefault(true);
				walletAccountRepository.save(caccount);
				return new ResponseEntity<>(new SuccessResponse("Account set as default successfully.", account),
						HttpStatus.CREATED);
			} catch (Exception e) {
				return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(new SuccessResponse("Successfully Update Without No Account Affected"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> UserAccountAccess(AdminAccountRestrictionDTO user) {
		if (!user.isAcctClosed()) {

		} else if (!user.isAcctfreez()) {
			if (!user.getFreezCode().isBlank() && !user.getFreezCode().isEmpty()) {
				return new ResponseEntity<>(new ErrorResponse("Freeze Code should not be entered"),
						HttpStatus.NOT_FOUND);
			}
			if (!user.getFreezReason().isBlank() && !user.getFreezReason().isEmpty()) {
				return new ResponseEntity<>(new ErrorResponse("Freeze Reason should not be entered"),
						HttpStatus.NOT_FOUND);
			}
		} else if (!user.isAmountRestrict()) {
			if (!user.getLienReason().isBlank() && !user.getLienReason().isEmpty()) {
				return new ResponseEntity<>(new ErrorResponse("Lien Reason should not be entered"),
						HttpStatus.NOT_FOUND);
			}
			if (user.getLienAmount().compareTo(BigDecimal.ZERO) != 0
					&& user.getLienAmount().compareTo(BigDecimal.ZERO) != 0) {
				return new ResponseEntity<>(new ErrorResponse("Lien Amount should not be entered"),
						HttpStatus.NOT_FOUND);
			}
		}

		WalletUser userDelete;
		List<String> accountL = new ArrayList<>();
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

			List<WalletAccount> accountList = walletAccountRepository.findByUser(userDelete);
			for (WalletAccount acct : accountList) {
				if (acct.isAcct_cls_flg() && acct.getClr_bal_amt() != 0) {
					accountL.add(acct.getAccountNo());
				}
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
			if (user.isAcctClosed() && accountL.isEmpty()) {
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
			} else {
				return new ResponseEntity<>(
						new ErrorResponse("All User Accounts balance must be equal to zero before it can be closed"),
						HttpStatus.NOT_FOUND);
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

		if (user.getAccountType() == null) {
			user.setAccountType("SAVINGS");
		}
		try {
			String hashed_no = reqUtil.WayaEncrypt(
					user.getUserId() + "|" + acctNo + "|" + user.getProductCode() + "|" + product.getCrncy_code());

			WalletAccount account;
			String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, "ledger");
			account = new WalletAccount(teller.getSol_id(), teller.getAdminCashAcct(), acctNo, nubanAccountNumber,
					user.getAccountName(),
					null, code.getGlSubHeadCode(), product.getProductCode(), acct_ownership, hashed_no,
					product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(),
					product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
					product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
					product.getXfer_cr_limit(), false, user.getAccountType(), user.getDescription());
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

		WalletEventCharges event = walletEventRepo.findByEventCurrency(user.getEventId(), user.getCrncyCode())
				.orElse(null);
		if (event == null) {
			return new ResponseEntity<>(new ErrorResponse("No Event created"), HttpStatus.BAD_REQUEST);
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

		WalletUser walletUser = walletUserRepository.findByEmailAddress(systemUserEmail);
		if (ObjectUtils.isEmpty(walletUser)) {
			return new ResponseEntity<>(new ErrorResponse("System config not completed"), HttpStatus.BAD_REQUEST);
		}

		String acctNo = product.getCrncy_code() + "0000" + user.getPlaceholderCode();
		String acct_ownership = "O";

		// call to generate nuban account
		String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, user.getAccountType());
		try {
			String hashed_no = reqUtil
					.WayaEncrypt(0L + "|" + acctNo + "|" + user.getProductCode() + "|" + product.getCrncy_code());

			WalletAccount _account = new WalletAccount("0000", user.getPlaceholderCode(), acctNo, nubanAccountNumber,
					user.getAccountName(), null,
					code.getGlSubHeadCode(), product.getProductCode(), acct_ownership, hashed_no,
					product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(),
					product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
					product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
					product.getXfer_cr_limit(), false, user.getAccountType(), user.getDescription());
			_account.setUser(walletUser);

			coreBankingService.createAccount(walletUser, _account);

			event.setProcessflg(true);
			walletEventRepo.save(event);
			return new ResponseEntity<>(new SuccessResponse("Office Account created successfully.", _account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> createAccount(AccountPojo2 accountPojo) {
		MyData tokenData = tokenService.getUserInformation();
		if (tokenData == null) {
			return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
		}

		if (!isAllowed(tokenData, accountPojo.getUserId())) {
			return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
		}

		int userId = accountPojo.getUserId().intValue();
		UserDetailPojo user = authService.AuthUser(userId);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exist"), HttpStatus.BAD_REQUEST);
		}
		WalletUser y = walletUserRepository.findByUserId(accountPojo.getUserId());
		WalletUser x = walletUserRepository.findByEmailAddress(user.getEmail());

		if (x == null && y == null && Long.compare(accountPojo.getUserId(), tokenData.getId()) == 0) {
			return createDefaultWallet(tokenData);
		} else if (x == null && y == null) {
			return new ResponseEntity<>(new ErrorResponse("Default Wallet Not Created"), HttpStatus.BAD_REQUEST);
		}

		if (!y.getEmailAddress().equals(x.getEmailAddress())) {
			return new ResponseEntity<>(new ErrorResponse("Wallet Data Integrity.please contact Admin"),
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

			if (accountPojo.getAccountType() == null) {
				accountPojo.setAccountType("SAVINGS");
			}

			if (accountPojo.getDescription().isEmpty()) {
				accountPojo.setDescription("SAVINGS ACCOUNT");
			}

			try {
				String hashed_no = reqUtil
						.WayaEncrypt(userId + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());

				if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
						|| product.getProduct_type().equals("ODA"))) {
					String nubanAccountNumber = Util.generateNuban(financialInstitutionCode,
							accountPojo.getAccountType());
					WalletAccount _account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, y,
							code.getGlSubHeadCode(), wayaProduct,
							acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
							LocalDate.now(), product.getCrncy_code(), product.getProduct_type(),
							product.isChq_book_flg(), product.getCash_dr_limit(), product.getXfer_dr_limit(),
							product.getCash_cr_limit(), product.getXfer_cr_limit(), false, accountPojo.getAccountType(),
							accountPojo.getDescription());

					coreBankingService.createAccount(y, _account);

					return new ResponseEntity<>(new SuccessResponse("Account Created Successfully.", _account),
							HttpStatus.CREATED);

				}
			} catch (Exception e) {
				return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
			}
		}

		return new ResponseEntity<>(new ErrorResponse("Default Wallet has not been created.please contact Admin"),
				HttpStatus.NOT_FOUND);

	}

	public ResponseEntity<?> createAccountProduct(AccountProductDTO accountPojo) {
		int userId = (int) accountPojo.getUserId();
		UserDetailPojo user = authService.AuthUser(userId);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exist"), HttpStatus.BAD_REQUEST);
		}
		WalletProductCode fProduct = walletProductCodeRepository.findByProduct(accountPojo.getProductCode());
		if (fProduct == null) {
			return new ResponseEntity<>(new ErrorResponse("Product Code does not exist"), HttpStatus.BAD_REQUEST);
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
			WalletProductCode code = walletProductCodeRepository.findByProductGLCode(fProduct.getProductCode(),
					fProduct.getGlSubHeadCode());
			WalletProduct product = walletProductRepository.findByProductCode(fProduct.getProductCode(),
					fProduct.getGlSubHeadCode());
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

			if (accountPojo.getAccountType() == null) {
				accountPojo.setAccountType("SAVINGS");
			}

			if (accountPojo.getDescription().isEmpty()) {
				accountPojo.setDescription("SAVINGS ACCOUNT");
			}
			try {
				String hashed_no = reqUtil.WayaEncrypt(
						userId + "|" + acctNo + "|" + fProduct.getProductCode() + "|" + product.getCrncy_code());

				WalletAccount account = new WalletAccount();
				String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, accountPojo.getAccountType());
				if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
						|| product.getProduct_type().equals("ODA"))) {
					account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, y, code.getGlSubHeadCode(),
							fProduct.getProductCode(), acct_ownership, hashed_no, product.isInt_paid_flg(),
							product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
							product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
							product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(), false,
							accountPojo.getAccountType(), accountPojo.getDescription());
				}

				coreBankingService.createAccount(y, account);

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
			Optional<WalletAccount> commissionAccount = Optional.empty();
			Long l = (long) id;
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
		securityWtihAccountNo(accountNo);
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
		if (account == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAccountInfoWithUserInfo(String accountNo) {
		// securityWtihAccountNo(accountNo);
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
		if (account == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.BAD_REQUEST);
		}
		WalletUser user = walletUserRepository.findByAccount(account);
		return new ResponseEntity<>(new SuccessResponse("Success", user), HttpStatus.OK);
	}

	//
	@Override
	public ResponseEntity<?> nameEnquiry(String accountNo) {
		WalletAccount acct = walletAccountRepository.findByAccountNo(accountNo);
		if (acct == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.NOT_FOUND);
		}

		AccountDetailDTO account = new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getAccountNo(),
				acct.getAcct_name(), acct.getProduct_code(), BigDecimal.valueOf(0.0),
				acct.getAcct_crncy_code(), acct.isWalletDefault());

		return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
	}

	public ResponseEntity<?> fetchAccountDetail(String accountNo, Boolean isAdmin) {
		if (!isAdmin) {
			securityWtihAccountNo(accountNo);
		}

		WalletAccount acct = walletAccountRepository.findByAccountNo(accountNo);
		if (acct == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.NOT_FOUND);
		}
		AccountDetailDTO account = new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getAccountNo(),
				acct.getAcct_name(), acct.getProduct_code(), BigDecimal.valueOf(acct.getClr_bal_amt()),
				acct.getAcct_crncy_code(), acct.isWalletDefault());
		return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> fetchVirtualAccountDetail(String accountNo) {
		WalletAccount acct = walletAccountRepository.findByNubanAccountNo(accountNo);
		if (acct == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.NOT_FOUND);
		}
		AccountDetailDTO account = new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getNubanAccountNo(),
				acct.getAcct_name(), acct.getProduct_code(), BigDecimal.valueOf(acct.getClr_bal_amt()),
				acct.getAcct_crncy_code());
		return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getUserAccountList(long userId) {
		MyData tokenData = (MyData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (tokenData == null) {
			return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
		}

		if (!isAllowed(tokenData, userId)) {
			return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
		}

		Optional<WalletUser> walletUser = walletUserRepository.findUserId(userId);

		if (!walletUser.isPresent() && Long.compare(userId, tokenData.getId()) == 0) {
			return createDefaultWallet(tokenData);
		} else if (!walletUser.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
		}

		List<WalletAccount> accounts = walletAccountRepository.findByUser(walletUser.get());

		CompletableFuture.runAsync(() -> updateTractionLimit(walletUser.get(), tokenData.getTransactionLimit()));

		return new ResponseEntity<>(new SuccessResponse("SUCCESS", accounts), HttpStatus.OK);

	}

	private void updateTractionLimit(WalletUser walletUser, String transactionLimit) {

		if (ObjectUtils.isEmpty(walletUser)) {
			return;
		}

		walletUser.setCust_debit_limit(Double.parseDouble(transactionLimit));
		walletUserRepository.save(walletUser);

	}

	private boolean isAllowed(MyData tokenData, long userId) {

		boolean isWriteAdmin = tokenData.getRoles().stream().anyMatch("ROLE_ADMIN_OWNER"::equalsIgnoreCase);
		isWriteAdmin = tokenData.getRoles().stream().anyMatch("ROLE_ADMIN_APP"::equalsIgnoreCase) ? true : isWriteAdmin;
		boolean isOwner = Long.compare(userId, tokenData.getId()) == 0;

		if (isOwner || isWriteAdmin) {
			log.error("owner check {} {}", isOwner, isWriteAdmin);
			return true;
		}

		return false;
	}

	public ResponseEntity<?> ListUserAccount(long userId) {
		// securityCheck(userId);
		int uId = (int) userId;
		UserDetailPojo ur = authService.AuthUser(uId);
		if (ur == null) {
			return new ResponseEntity<>(new ErrorResponse("User Id is Invalid"), HttpStatus.NOT_FOUND);
		}
		WalletUser x = walletUserRepository.findByEmailAddress(ur.getEmail());
		if (x == null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet User does not exist"), HttpStatus.NOT_FOUND);
		}
		List<NewWalletAccount> accounts = new ArrayList<>();
		List<WalletAccount> listAcct = walletAccountRepository.findByUser(x);
		if (listAcct == null) {
			return new ResponseEntity<>(new ErrorResponse("Account List Does Not Exist"), HttpStatus.NOT_FOUND);
		}
		for (WalletAccount wAcct : listAcct) {
			NewWalletAccount mAcct = new NewWalletAccount(wAcct, userId);
			accounts.add(mAcct);
		}

		return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAllAccount() {
		// List<WalletAccount> pagedResult = walletAccountRepository.findAll();
		List<WalletAccount> pagedResult = walletAccountRepository.findByWalletAccount();
		return new ResponseEntity<>(new SuccessResponse("Success.", pagedResult), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getUserCommissionList(long userId, Boolean isAdmin) {
		if (!isAdmin) {
			securityCheck(userId);
		}

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
		securityWtihAccountNo(accountNo);
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
		if (account == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid Account No"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Default wallet set", account), HttpStatus.OK);

	}

	private MyData getEmailFromToken(long userId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return (MyData) auth.getPrincipal();
	}

	private void securityWtihAccountNo(String accountNo) {
		try {
			WalletAccount walletAccount = walletAccountRepository.findByAccountNo(accountNo);
			WalletUser xUser = walletUserRepository.findByAccount(walletAccount);
			securityCheck(Long.valueOf(xUser.getUserId()));
		} catch (CustomException ex) {
			throw new CustomException("You Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
		}
	}

	public void securityCheck(long userId) {
		WalletUser user = walletUserRepository.findByUserId(userId);
		if(Objects.isNull(user))
			throw new CustomException("You Lack credentials to perform this action", HttpStatus.BAD_REQUEST);

		if(user.getUserId() != userId)
			throw new CustomException("You Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<?> UserWalletLimit(long userId) {
		// security check
		securityCheck(userId);

		WalletUser user = walletUserRepository.findByUserId(userId);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("User Wallet Info", user), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getALLCommissionAccount() {
		List<WalletAccount> account = walletAccountRepository.findByProductList(wayaProductCommission);
		if (account == null || account.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Wallet Commissions", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAccountCommission(String accountNo) {
		securityWtihAccountNo(accountNo);
		Optional<WalletAccount> account = walletAccountRepository.findByAccountProductCode(wayaProductCommission,
				accountNo);
		if (!account.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Wallet Commissions", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getAccountDetails(String accountNo, Boolean isAdmin) {
		if (!isAdmin) {
			securityWtihAccountNo(accountNo);
		}

		try {
			Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNo);
			if (!account.isPresent()) {
				return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(new SuccessResponse("Wallet", account.get()), HttpStatus.OK);
		} catch (Exception ex) {
			log.error("An error getAccountDetails: {}",ex.getLocalizedMessage());
			String customException;
			if(ex instanceof CustomException){
				customException =  objectMapper.convertValue(ex.getLocalizedMessage(),String.class);
				return new ResponseEntity<>(new ErrorResponse(customException),HttpStatus.BAD_REQUEST);
			}else {
				return new ResponseEntity<>(new ErrorResponse("Unable to fetch your default account, try again later"),HttpStatus.BAD_REQUEST);
			}
		}
	}

	@Override
	public ResponseEntity<?> getAccountDefault(Long user_id) {
		try {
			securityCheck(user_id);
			WalletUser user = walletUserRepository.findByUserId(user_id);
			if (user == null) {
				return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
			}

			Optional<WalletAccount> account = walletAccountRepository.findByDefaultAccount(user);
			if (!account.isPresent()) {
				return new ResponseEntity<>(new ErrorResponse("Unable to fetch default account"), HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<>(new SuccessResponse("Wallet Default", account.get()), HttpStatus.OK);
		}catch (Exception ex) {
			log.error("An error getAccountDefault: {}",ex.getLocalizedMessage());
			String customException;
			if(ex instanceof CustomException){
				 customException =  objectMapper.convertValue(ex.getLocalizedMessage(),String.class);
				return new ResponseEntity<>(new ErrorResponse(customException),HttpStatus.BAD_REQUEST);
			}else {
				return new ResponseEntity<>(new ErrorResponse("Unable to fetch your default account, try again later"),HttpStatus.BAD_REQUEST);
			}
		}
	}

	public ResponseEntity<?> searchAccount(String search) {
		List<WalletUser> user = walletUserRepository.findAll();
		List<WalletUser> matchingAcct;
		List<WalletUser> matchingAcct2 = new ArrayList<>();
		List<WalletAccount> account = new ArrayList<WalletAccount>();
		Collection<WalletAccount> accountColl = new ArrayList<WalletAccount>();
		for (WalletUser col : user) {
			if (col.getUserId() == Long.valueOf(search)) {
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
		securityWtihAccountNo(acctNo);
		List<AccountStatementDTO> account = tempwallet.fetchTransaction(acctNo);
		if (account.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO TRANSACTION RECORD", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESSFUL TRANSACTION STATEMENT", account);
	}

	@Override
	public ApiResponse<?> fetchFilterTransaction(String acctNo, Date fromdate, Date todate) {
		securityWtihAccountNo(acctNo);
		LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		List<AccountStatementDTO> account = tempwallet.fetchFilterTransaction(acctNo, fromDate, toDate);
		if (account.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO TRANSACTION RECORD", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESSFUL TRANSACTION STATEMENT", account);
	}

	@Override
	public ApiResponse<?> fetchRecentTransaction(Long user_id) {
		securityCheck(user_id);
		WalletUser user = walletUserRepository.findByUserId(user_id);
		if (user == null) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "USER ID DOES NOT EXIST", null);
		}
		List<WalletAccount> accountList = walletAccountRepository.findByUser(user);
		if (accountList.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO ACCOUNT FOR USER ID", null);
		}
		List<AccountStatementDTO> statement = new ArrayList<>();
		for (WalletAccount acct : accountList) {
			List<AccountStatementDTO> account = tempwallet.recentTransaction(acct.getAccountNo());
			statement.addAll(account);
		}
		if (statement.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO TRANSACTION RECORD", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "RECENT TRANSACTION", statement);
	}

	@Override
	public ResponseEntity<?> getListWayaAccount() {
		List<WalletAccount> account = walletAccountRepository.findByWayaAccount();
		if (account.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("NO WAYA ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("LIST WAYA ACCOUNT", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> getListWalletAccount() {

		WalletUser user = Util.checkOwner();
		List<WalletAccount> findByUser = walletAccountRepository.findByUser(user);

		if (findByUser.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("NO WAYA ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("LIST WAYA ACCOUNT", findByUser), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createOfficialAccount(OfficialAccountDTO accountPojo) {
		boolean validate2 = paramValidation.validateDefaultCode(accountPojo.getCrncyCode(), "Currency");
		if (!validate2) {
			return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
		}

		WalletProduct product = walletProductRepository.findByProductCode(accountPojo.getProductCode(),
				accountPojo.getProductGL());
		if ((!product.getProduct_type().equals("OAB"))) {
			return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
		}

		WalletProductCode code = walletProductCodeRepository.findByProductGLCode(accountPojo.getProductCode(),
				accountPojo.getProductGL());
		if ((!code.getProductType().equals("OAB"))) {
			return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
		}


		WalletUser walletUser = walletUserRepository.findByEmailAddress(systemUserEmail);
		if(ObjectUtils.isEmpty(walletUser)){
			return new ResponseEntity<>(new ErrorResponse("Wallet not properly setup"), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Integer rand = reqUtil.getAccountNo();
		if (rand == 0) {
			return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"), HttpStatus.BAD_REQUEST);
		}
		// NGN000011108001
		// NGN008017725071
		String acctNo = "801" + rand;
		if (acctNo.length() < 10) {
			acctNo = StringUtils.rightPad(acctNo, 10, "0");
		}
		acctNo = product.getCrncy_code() + "00" + acctNo;
		String acct_ownership = "O";

		if (accountPojo.getDescription().isEmpty()) {
			accountPojo.setDescription("SAVINGS ACCOUNT");
		}

		try {
			String hashed_no = reqUtil.WayaEncrypt(
					0L + "|" + acctNo + "|" + accountPojo.getProductCode() + "|" + product.getCrncy_code());

			String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, "ledger");
			WalletAccount account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, accountPojo.getAccountName(), null,
					code.getGlSubHeadCode(),
					product.getProductCode(), acct_ownership, hashed_no, product.isInt_paid_flg(),
					product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
					product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
					product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(), false,
					accountPojo.getAccountType(), accountPojo.getDescription());
			
			coreBankingService.createAccount(walletUser, account);
			
			return new ResponseEntity<>(new SuccessResponse("Office Account created successfully.", account),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ArrayList<Object> createOfficialAccount(List<OfficialAccountDTO> account) {
		ResponseEntity<?> responseEntity = null;
		ArrayList<Object> objectArrayList = new ArrayList<>();
		try {
			for (OfficialAccountDTO data : account) {

				responseEntity = createOfficialAccount(data);
				objectArrayList.add(responseEntity.getBody());
			}
			return objectArrayList;

		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> AccountAccessDelete(UserAccountDelete user) {
		try {

			WalletUser userDelete = walletUserRepository.findByUserId(user.getUserId());
			if (userDelete == null) {
				return new ResponseEntity<>(new ErrorResponse("Wallet User Account does not exists"),
						HttpStatus.NOT_FOUND);
			}

			List<WalletAccount> accountList = walletAccountRepository.findByUser(userDelete);
			if (!accountList.isEmpty()) {
				for (WalletAccount acct : accountList) {
					if (acct.isAcct_cls_flg() && acct.getClr_bal_amt() != 0) {
						return new ResponseEntity<>(
								new ErrorResponse(
										"All User Accounts balance must be equal to 0 before it can be closure"),
								HttpStatus.NOT_FOUND);
					}
				}
			}

			for (WalletAccount accountDet : accountList) {
				accountDet.setAcct_cls_date(LocalDate.now());
				accountDet.setAcct_cls_flg(true);
				accountDet.setAccountNo(accountDet.getAccountNo());
				walletAccountRepository.save(accountDet);
			}

			String email = userDelete.getEmailAddress() + userDelete.getId();
			String phone = userDelete.getMobileNo() + userDelete.getId();
			Long userId = 1000000000L + userDelete.getUserId() + userDelete.getId();
			userDelete.setEmailAddress(email);
			userDelete.setMobileNo(phone);
			userDelete.setUserId(userId);
			userDelete.setDel_flg(true);
			walletUserRepository.save(userDelete);

			return new ResponseEntity<>(new SuccessResponse("User Account Deleted successfully.", userDelete),
					HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> AccountAccessPause(AccountFreezeDTO user) {
		try {
			switch (user.getFreezCode()) {
				case "D":
					break;
				case "C":
					break;
				case "T":
					break;
				default:
					return new ResponseEntity<>(new ErrorResponse("Unknown freeze code"), HttpStatus.NOT_FOUND);
			}

			WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNo());
			if (account == null) {
				return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"), HttpStatus.NOT_FOUND);
			}

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
			walletAccountRepository.save(account);
			return new ResponseEntity<>(new SuccessResponse("Account Freeze successfully.", account), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> AccountAccessBlockAndUnblock(AccountBlockDTO user, HttpServletRequest request) {
		try {

			WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNo());
			if (account == null) {
				return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"), HttpStatus.NOT_FOUND);
			}

			if (user.isBlock()) {
				account.setAcct_cls_date(LocalDate.now());
				account.setAcct_cls_flg(true);
				walletAccountRepository.save(account);
				CompletableFuture.runAsync(() -> blockAccount(account, request, true));
				return new ResponseEntity<>(new SuccessResponse("Account blocked successfully.", account),
						HttpStatus.OK);

			} else {
				account.setAcct_cls_date(LocalDate.now());
				account.setAcct_cls_flg(false);
				walletAccountRepository.save(account);
				CompletableFuture.runAsync(() -> blockAccount(account, request, false));
				return new ResponseEntity<>(new SuccessResponse("Account Unblock successfully.", account),
						HttpStatus.OK);

			}

		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	private void blockAccount(WalletAccount account, HttpServletRequest request, boolean isBlock) {
		log.info("request : " + request);
		try {
			MifosBlockAccount mifosBlockAccount = new MifosBlockAccount();

			mifosBlockAccount.setAccountNumber(account.getNubanAccountNo());

			if (isBlock) {
				mifosBlockAccount.setNarration("block account");

			} else {
				mifosBlockAccount.setNarration("unblock account");
			}

		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> AccountAccessClosure(AccountCloseDTO user) {
		try {

			WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNo());
			if (account == null) {
				return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"), HttpStatus.NOT_FOUND);
			}

			if (account.isAcct_cls_flg() && account.getClr_bal_amt() != 0) {
				return new ResponseEntity<>(
						new ErrorResponse("Account balance must be equal to zero before it can be closed"),
						HttpStatus.NOT_FOUND);
			} else {
				if (account.isAcct_cls_flg())
					return new ResponseEntity<>(new ErrorResponse("Account already closed"), HttpStatus.NOT_FOUND);
			}

			if (account.getClr_bal_amt() == 0) {
				account.setAcct_cls_date(LocalDate.now());
				account.setAcct_cls_flg(true);
			} else {
				return new ResponseEntity<>(
						new ErrorResponse("Account balance must be equal to 0 before it can be closed"),
						HttpStatus.NOT_FOUND);
			}
			walletAccountRepository.save(account);
			return new ResponseEntity<>(new SuccessResponse("Account closed successfully.", account), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<?> AccountAccessClosureMultiple(List<AccountCloseDTO> user) {
		int count = 0;
		for (AccountCloseDTO data : user) {
			ResponseEntity<?> dd = AccountAccessClosure(data);
			log.info("dd : " + dd);
			count++;
		}
		return new ResponseEntity<>(new SuccessResponse(count + "accounts closed successfully.", user), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> AccountAccessLien(AccountLienDTO user) {
		log.info(" ##### AccountAccessLien #### ");
		try {
			if (user.getLienAmount().compareTo(BigDecimal.ZERO) == 0) {
				return new ResponseEntity<>(new ErrorResponse("Lien Amount should not be 0"), HttpStatus.NOT_FOUND);
			}

			WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNo());
			if (account == null) {
				return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"), HttpStatus.NOT_FOUND);
			}
			if (user.isLien()) {
				double acctAmt = account.getLien_amt() + user.getLienAmount().doubleValue();
				account.setLien_amt(acctAmt);
				account.setLien_reason(user.getLienReason());
			} else {
				double acctAmt = account.getLien_amt() - user.getLienAmount().doubleValue();
				log.info("###################### account.getLien_amt() ########### " + account.getLien_amt());
				log.info("###################### user.getLienAmount() ########### " + user.getLienAmount());
				account.setLien_amt(acctAmt);
				account.setLien_reason(user.getLienReason());
			}

			WalletAccount account1 = walletAccountRepository.save(account);
			log.info("Actual Value " + account1);
			return new ResponseEntity<>(new SuccessResponse("Account Lien successfully.", account), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
	}

	public ResponseEntity<?> getAccountSimulated(Long user_id) {
		WalletUser user = walletUserRepository.findBySimulated(user_id);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Invalid User ID OR Not Simulated"), HttpStatus.BAD_REQUEST);
		}
		Optional<WalletAccount> account = walletAccountRepository.findByDefaultAccount(user);
		if (!account.isPresent()) {
			return new ResponseEntity<>(new ErrorResponse("Unable to fetch simulated account"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("Simulated Account", account), HttpStatus.OK);
	}

	public ResponseEntity<?> getListSimulatedAccount() {
		List<WalletUser> user = walletUserRepository.findBySimulated();
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("Not Simulated User"), HttpStatus.BAD_REQUEST);
		}
		List<WalletAccount> account = walletAccountRepository.findBySimulatedAccount();
		if (account.isEmpty()) {
			return new ResponseEntity<>(new ErrorResponse("NO SIMULATED ACCOUNT"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("LIST SIMULATED ACCOUNT", account), HttpStatus.OK);
	}

	public ResponseEntity<?> getUserAccountCount(Long userId) {
		WalletUser user = walletUserRepository.findByUserId(userId);
		if (user == null) {
			return new ResponseEntity<>(new ErrorResponse("User Doesn't Exist"), HttpStatus.BAD_REQUEST);
		}
		List<WalletAccount> account = walletAccountRepository.findByUser(user);
		if (account.isEmpty() || account == null) {
			return new ResponseEntity<>(new ErrorResponse("NO ACCOUNT CREATED"), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(new SuccessResponse("LIST ACCOUNT", account), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> AccountLookUp(String account, SecureDTO secureKey) {

		if (!secureKey.getKey()
				.equals("yYSowX0uQVUZpNnkY28fREx0ayq+WsbEfm2s7ukn4+RHw1yxGODamMcLPH3R7lBD+Tmyw/FvCPG6yLPfuvbJVA==")) {
			return new ResponseEntity<>(new ErrorResponse("INVALID KEY"), HttpStatus.BAD_REQUEST);
		}

		com.wayapaychat.temporalwallet.dto.AccountLookUp mAccount = tempwallet.GetAccountLookUp(account);
		if (mAccount == null) {
			return new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT"), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(new SuccessResponse("ACCOUNT SEARCH", mAccount), HttpStatus.OK);
	}

	public ResponseEntity<?> getTotalActiveAccount() {
		BigDecimal count = walletAccountRepository.totalActiveAccount();
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?> countActiveAccount() {
		long count = walletAccountRepository.countActiveAccount();
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	public ResponseEntity<?> countInActiveAccount() {
		long count = walletAccountRepository.countInActiveAccount();
		return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> createDefaultWallet(MyData tokenData) {
		WalletUserDTO createAccount = new WalletUserDTO();
		// Default Debit Limit SetUp
		createAccount.setCustDebitLimit(0.0);
		// Default Account Expiration Date
		LocalDateTime time = LocalDateTime.of(2099, Month.DECEMBER, 30, 0, 0);
		createAccount.setCustExpIssueDate(Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
		createAccount.setUserId(tokenData.getId());
		createAccount.setCustIssueId(generateRandomNumber(9));
		createAccount.setFirstName(tokenData.getFirstName());
		createAccount.setLastName(tokenData.getSurname());
		createAccount.setEmailId(tokenData.getEmail());
		createAccount.setMobileNo(tokenData.getPhoneNumber());

		createAccount.setCustSex("N"); // Set to default
		createAccount.setCustTitleCode(""); // Set to default
		createAccount.setDob(new Date()); // Set to default
		// Default Branch SOL ID
		createAccount.setSolId("0000");
		createAccount.setAccountType("saving");
		createAccount.setCorporate(tokenData.isCorporate());
		log.info("retrying to create wallet for {}", createAccount.getEmailId());
		return createUserAccount(createAccount);
	}

	public ResponseEntity<?> updateCustomerDebitLimit(String userId, BigDecimal amount) {
		System.out.println("updateCustomerDebitLimit :: " + amount);
		log.info("updateCustomerDebitLimit userId :: " + userId);
		try {
			Optional<WalletUser> walletUser = walletUserRepository.findUserId(Long.parseLong(userId));
			if (walletUser.isPresent()) {
				WalletUser walletUser1 = walletUser.get();
				walletUser1.setCust_debit_limit(amount.doubleValue());
				walletUserRepository.save(walletUser1);
			}
			return new ResponseEntity<>(
					new SuccessResponse("SUCCESS", walletUserRepository.findUserId(Long.parseLong(userId))),
					HttpStatus.OK);
		} catch (CustomException ex) {
			throw new CustomException("error", HttpStatus.EXPECTATION_FAILED);
		}

	}

	@Override
	public ResponseEntity<?> createExternalAccount(String accountNumber) {
		WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
		if (account == null) {
			return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
					HttpStatus.NOT_FOUND);
		}

		Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
			return new ResponseEntity<>(new ErrorResponse(ResponseCodes.NO_PROVIDER.getValue()),
					HttpStatus.NOT_FOUND);
        }

		return coreBankingService.externalCBACreateAccount(account.getUser(), account, null);
	}


    public ResponseEntity<?> updateNotificationEmail(String accountNumber, String email){

		Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNumber);

		if(account.isEmpty()){
			return new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT"), HttpStatus.BAD_REQUEST);
		}

		ResponseEntity<?> result = securityCheckOwner(accountNumber);
		if(!result.getStatusCode().is2xxSuccessful()){
			return result;
		}
		
		account.get().setNotify_email(email);
		walletAccountRepository.save(account.get());

		return new ResponseEntity<>(new SuccessResponse("SUCCESS", null), HttpStatus.OK);

	}
 
    public ResponseEntity<?> securityCheckOwner(String accountNumber) {
        log.info("securityCheck Ownership:: " + accountNumber);
        MyData userToken = (MyData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userToken == null) {
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()),
                    HttpStatus.BAD_REQUEST);
        }

        AccountSumary account = tempwallet.getAccountSumaryLookUp(accountNumber);
        log.info("AccountSumary :: " + account);
        if (account == null) {
            return new ResponseEntity<>(
                    new ErrorResponse(
                            String.format("%s  %s", ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber)),
                    HttpStatus.BAD_REQUEST);
        }

        boolean isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_OWNER"::equalsIgnoreCase);
        isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_APP"::equalsIgnoreCase) ? true : isWriteAdmin;
        boolean isOwner = Long.compare(account.getUId(), userToken.getId()) == 0;

        if (!isOwner && !isWriteAdmin) {
            log.error("owner check {} {}", isOwner, isWriteAdmin);
            return new ResponseEntity<>(new ErrorResponse(String.format("%s %s %s %s",
                    ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber, isOwner, isWriteAdmin)),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(account, HttpStatus.ACCEPTED);

    }

	@Override
	public void setupExternalCBA(){
		List<WalletAccount> walletAccounts = walletAccountRepository.findByWayaAccount();

		for (WalletAccount wallet : walletAccounts) {
			if("0".equals(wallet.getNubanAccountNo()))
			{
				wallet.setNubanAccountNo(Util.generateNuban(financialInstitutionCode, ("O".equalsIgnoreCase(wallet.getAcct_ownership())?"ledger":"savings")));
				walletAccountRepository.save(wallet);
			}
			Provider provider = switchWalletService.getActiveProvider();
			coreBankingService.externalCBACreateAccount(wallet.getUser(), wallet, provider);
		}

	}

	@Override
	public ApiResponse<?> getAllAccounts(int page, int size, String filter, LocalDate fromdate, LocalDate todate){
		
		Pageable pagable = PageRequest.of(page, size);
		Map<String, Object> response = new HashMap<>();
		Page<WalletAccount> accountsPage = null;

		if (filter != null) {
			// LocalDate fromtranDate, LocalDate totranDate
			accountsPage = walletAccountRepository.findByAllWalletAccountWithDateRangeAndTranTypeOR(pagable,
					filter, fromdate, todate);

			System.out.println("walletTransactionPage2 " + accountsPage.getContent());
		} else {
			accountsPage = walletAccountRepository.findByAllWalletAccountWithDateRange(pagable, fromdate,
					todate);
		}

		List<WalletAccount> account = accountsPage.getContent();
		response.put("account", account);
		response.put("currentPage", accountsPage.getNumber());
		response.put("totalItems", accountsPage.getTotalElements());
		response.put("totalPages", accountsPage.getTotalPages());

		if (account.isEmpty()) {
			return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO RECORD FOUND", null);
		}
		return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "ACCOUNT LIST SUCCESSFULLY", response);

	}
 
}