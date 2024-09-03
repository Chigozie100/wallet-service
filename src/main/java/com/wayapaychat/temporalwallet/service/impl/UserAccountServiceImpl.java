package com.wayapaychat.temporalwallet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waya.security.auth.pojo.UserIdentityData;
import com.wayapaychat.temporalwallet.config.SecurityConstants;
import com.wayapaychat.temporalwallet.dao.AuthUserServiceDAO;
import com.wayapaychat.temporalwallet.dao.TemporalWalletDAO;
import com.wayapaychat.temporalwallet.dto.*;
import com.wayapaychat.temporalwallet.entity.*;
import com.wayapaychat.temporalwallet.enumm.CategoryType;
import com.wayapaychat.temporalwallet.enumm.ResponseCodes;
import com.wayapaychat.temporalwallet.enumm.TransactionChannel;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.interceptor.TokenImpl;
import com.wayapaychat.temporalwallet.pojo.*;
import com.wayapaychat.temporalwallet.pojo.signupKafka.InWardDataDto;
import com.wayapaychat.temporalwallet.proxy.AuthProxy;
import com.wayapaychat.temporalwallet.repository.*;
import com.wayapaychat.temporalwallet.response.ApiResponse;
import com.wayapaychat.temporalwallet.service.*;
import com.wayapaychat.temporalwallet.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
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
    private final WalletTransactionRepository walletTransactionRepository;
    private final MessageQueueProducer messageQueueProducer;
    //    @Autowired
//    private KafkaMessageConsumer kafkaMessageConsumer;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    TransactionPropertyRepository transactionPropertyRepo;
    @Autowired
    AuthProxy authProxy;
    @Autowired
    WalletTransAccountRepository walletTransAccountRepo;
    @Autowired
    WalletTransactionRepository walletTransRepo;

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
                                  CoreBankingService coreBankingService, SwitchWalletService switchWalletService,
                                  WalletTransactionRepository walletTransactionRepository, MessageQueueProducer messageQueueProducer) {
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
        this.walletTransactionRepository = walletTransactionRepository;
        this.messageQueueProducer = messageQueueProducer;
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

    private ResponseEntity<?> createClient(String clientId,String clientType,Long userid, String token,String profileId,String profileType) {
        log.info("Request to create a client  ---->> {}", profileType);
        WalletUser existingUser = walletUserRepository.findByUserIdAndProfileId(userid,profileId);
        log.info("Found user --->> {}", existingUser);
        if (!ObjectUtils.isEmpty(existingUser)) {
            return new ResponseEntity<>(existingUser, HttpStatus.ACCEPTED);
        }

        log.info("::Id, ProfileId, ",userid+ "/"+profileId);
        UserProfileResponse userDetailsResponse = authProxy.getProfileByIdAndUserId(userid,profileId, token,clientId,clientType);
        log.info("userDetailsResponse1 {} ", userDetailsResponse);
        if (ObjectUtils.isEmpty(userDetailsResponse)) {
            return new ResponseEntity<>(new ErrorResponse("User does not exists"), HttpStatus.BAD_REQUEST);
        }

        log.info("userDetailsResponse2 {} ", userDetailsResponse);
        if (!userDetailsResponse.isStatus() || ObjectUtils.isEmpty(userDetailsResponse.getData())) {
            return new ResponseEntity<>(new ErrorResponse("User does not exists"), HttpStatus.BAD_REQUEST);
        }

        try {
            String acct_name = userDetailsResponse.getData().isCorporate()
                    ? userDetailsResponse.getData().getOtherDetails().getOrganisationName()
                    : userDetailsResponse.getData().getFirstName().toUpperCase() + " "
                    + userDetailsResponse.getData().getSurname().toUpperCase();
            String phoneNumber = userDetailsResponse.getData().isCorporate()
                    ? userDetailsResponse.getData().getOtherDetails().getOrganisationPhone()
                    : userDetailsResponse.getData().getPhoneNumber();
            String emailAddress = userDetailsResponse.getData().isCorporate()
                    ? userDetailsResponse.getData().getOtherDetails().getOrganisationEmail()
                    : userDetailsResponse.getData().getEmail();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date dob = formatter.parse(userDetailsResponse.getData().getDateOfBirth());
            String custSex = userDetailsResponse.getData().getGender().substring(0,
                    Math.min(userDetailsResponse.getData().getGender().length(), 1));
            String custTitle = custSex.equalsIgnoreCase("M") ? "MR" : "MRS";
            String code = generateRandomNumber(9);

            WalletUser userInfo = new WalletUser("0000", userid,
                    userDetailsResponse.getData().getFirstName().toUpperCase(),
                    userDetailsResponse.getData().getSurname().toUpperCase(),
                    emailAddress, phoneNumber, acct_name,
                    custTitle, custSex, dob, code, new Date(), LocalDate.now(), 0);


            userInfo.setProfileId(profileId);
            userInfo.setAccountType(profileType);
            userInfo.setCorporate(userDetailsResponse.getData().isCorporate());
            WalletUser newUserDetails = walletUserRepository.save(userInfo);

            log.info("New user ---->> {}", userInfo);
            return new ResponseEntity<>(newUserDetails, HttpStatus.ACCEPTED);

        } catch (Exception e) {
            log.error("Error creating client {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse("Error creating client"), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<?> createClientAccount(WalletUser walletUser, String accountType, String description) {
        log.info("Processing createClientAccount {}, {}, {} ", walletUser, accountType, description);
        Optional<WalletAccount> defaultAccount = walletAccountRepository.findByDefaultAccount(walletUser);
        WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
        WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);
        String acctNo = null;
        Integer rand = reqUtil.getAccountNo();
        if (rand == 0) {
            log.error("Unable to generate Wallet Account");
            return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"), HttpStatus.BAD_REQUEST);
        }
        String acct_ownership = null;
        if (!walletUser.getCust_sex().equals("S")) {
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
            log.info("Processing createClientAccount2 {}, {}, {} ", walletUser, accountType, description);
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
            log.info("Processing createClientAccount2 {}, {}, {} ", walletUser, accountType, description);
        }

        String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, accountType);
        log.info("Generated account number -----> {}", nubanAccountNumber);
        try {
            String hashed_no = reqUtil
                    .WayaEncrypt(
                            walletUser.getUserId() + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());
            accountType = accountType == null ? "SAVINGS" : accountType;
            description = description == null ? accountType.concat(" ACCOUNT") : description;
            WalletAccount account = new WalletAccount();
            if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
                    || product.getProduct_type().equals("ODA"))) {
                account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, walletUser.getCust_name(),
                        walletUser,
                        code.getGlSubHeadCode(), wayaProduct,
                        acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
                        LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
                        product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
                        product.getXfer_cr_limit(), !defaultAccount.isPresent(), accountType, description);
            }

            coreBankingService.createAccount(walletUser, account);
            log.info("Created account in cor banking ");

            WalletAccount caccount = new WalletAccount();
            // Commission Wallet
            log.info("Create commission account in progress:{}", walletUser);
            if (walletUser.isCorporate() && !defaultAccount.isPresent()) {
                String commisionName = "COMMISSION ACCOUNT";
                Optional<WalletAccount> acct = walletAccountRepository.findFirstByProduct_codeAndUserAndAcct_nameLike(wayaProductCommission, walletUser);
                if (!acct.isPresent()) {
                    code = walletProductCodeRepository.findByProductGLCode(wayaProductCommission, wayaCommGLCode);
                    product = walletProductRepository.findByProductCode(wayaProductCommission, wayaCommGLCode);
                    if (!walletUser.getCust_sex().equals("S")) {
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
                    log.info("Comission Account::{}", acctNo);
                    hashed_no = reqUtil.WayaEncrypt(
                            walletUser.getUserId() + "|" + acctNo + "|" + wayaProductCommission + "|"
                                    + product.getCrncy_code());
                    String acct_name = walletUser.getCust_name() + " " + commisionName;
                    if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
                            || product.getProduct_type().equals("ODA"))) {
                        caccount = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, walletUser,
                                code.getGlSubHeadCode(),
                                wayaProductCommission, acct_ownership, hashed_no, product.isInt_paid_flg(),
                                product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
                                product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
                                product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(),
                                false, accountType, description);
                        log.info("Wallet commission account: {}", caccount);
                    } else {
                        log.error("Commission account not created");
                    }

                    coreBankingService.createAccount(walletUser, caccount);
                    log.info("Commission account created: {}", caccount.getAccountNo());
                }

            }

            if (!defaultAccount.isPresent()) {
                CompletableFuture.runAsync(() -> userPricingService.createUserPricing(walletUser));
            }
            log.info("Client account created successfully");
            return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
                    HttpStatus.CREATED);
        } catch (Exception e) {

            log.error("Error creating ClientAccount", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }

    }


    // This method was constructed out of the previous one so we can have a fall back // Tue May 21:: at 7:42 in lagos
    private ResponseEntity<?> createClientVirtualAccount(WalletUser walletUser, String accountType, String description) {
        log.info("Processing createClientAccount {}, {}, {} ", walletUser, accountType, description);
        Optional<WalletAccount> defaultAccount = walletAccountRepository.findByDefaultAccount(walletUser);
        WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
        WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);
        String acctNo = null;
        Integer rand = reqUtil.getAccountNo();
        if (rand == 0) {
            log.error("Unable to generate Wallet Account");
            return new ResponseEntity<>(new ErrorResponse("Unable to generate Wallet Account"), HttpStatus.BAD_REQUEST);
        }
        String acct_ownership = null;
        if (!walletUser.getCust_sex().equals("S")) {
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
            log.info("Processing createClientAccount2 {}, {}, {} ", walletUser, accountType, description);
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
            log.info("Processing createClientAccount2 {}, {}, {} ", walletUser, accountType, description);
        }

        String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, accountType);
        log.info("Generated account number -----> {}", nubanAccountNumber);
        try {
            String hashed_no = reqUtil
                    .WayaEncrypt(
                            walletUser.getUserId() + "|" + acctNo + "|" + wayaProduct + "|" + product.getCrncy_code());
            accountType = accountType == null ? "SAVINGS" : accountType;
            description = description == null ? accountType.concat(" ACCOUNT") : description;
            WalletAccount account = new WalletAccount();
            if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
                    || product.getProduct_type().equals("ODA"))) {
                account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, walletUser.getCust_name(),
                        walletUser,
                        code.getGlSubHeadCode(), wayaProduct,
                        acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
                        LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
                        product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
                        product.getXfer_cr_limit(), !defaultAccount.isPresent(), accountType, description);
            }

            coreBankingService.createAccount(walletUser, account);
            log.info("Created account in cor banking ");

            WalletAccount caccount = new WalletAccount();
            // Commission Wallet
            log.info("Create commission account in progress:{}", walletUser);
            if (walletUser.isCorporate() && !defaultAccount.isPresent()) {
                String commisionName = "COMMISSION ACCOUNT";
                Optional<WalletAccount> acct = walletAccountRepository.findFirstByProduct_codeAndUserAndAcct_nameLike(wayaProductCommission, walletUser);
                if (!acct.isPresent()) {
                    code = walletProductCodeRepository.findByProductGLCode(wayaProductCommission, wayaCommGLCode);
                    product = walletProductRepository.findByProductCode(wayaProductCommission, wayaCommGLCode);
                    if (!walletUser.getCust_sex().equals("S")) {
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
                    log.info("Comission Account::{}", acctNo);
                    hashed_no = reqUtil.WayaEncrypt(
                            walletUser.getUserId() + "|" + acctNo + "|" + wayaProductCommission + "|"
                                    + product.getCrncy_code());
                    String acct_name = walletUser.getCust_name() + " " + commisionName;
                    if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
                            || product.getProduct_type().equals("ODA"))) {
                        caccount = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, walletUser,
                                code.getGlSubHeadCode(),
                                wayaProductCommission, acct_ownership, hashed_no, product.isInt_paid_flg(),
                                product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
                                product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
                                product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(),
                                false, accountType, description);
                        log.info("Wallet commission account: {}", caccount);
                    } else {
                        log.error("Commission account not created");
                    }

                    coreBankingService.createAccount(walletUser, caccount);
                    log.info("Commission account created: {}", caccount.getAccountNo());
                }

            }

            if (!defaultAccount.isPresent()) {
                CompletableFuture.runAsync(() -> userPricingService.createUserPricing(walletUser));
            }
            log.info("Client account created successfully");
            return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
                    HttpStatus.CREATED);
        } catch (Exception e) {

            log.error("Error creating ClientAccount", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }

    }
    public ResponseEntity<?> createUser(HttpServletRequest request, UserDTO user, String token) {
        log.info("Received request to create user: {}", user);

        String profileId = null;
        if (user.getProfileId() != null) {
            profileId = user.getProfileId();
        }

        String profileAccountType;
        if (user.isCorporate()) {
            profileAccountType = "BUSINESS";
        } else {
            profileAccountType = "PERSONAL";
        }
        log.info("Profile account type determined as: {}", profileAccountType);

        ResponseEntity<?> response = createClient(request.getHeader(SecurityConstants.CLIENT_ID),
                request.getHeader(SecurityConstants.CLIENT_TYPE), user.getUserId(), token, profileId, profileAccountType);
        log.info("Received response from createClient: {}", response);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("createClient request failed with status code: {}", response.getStatusCodeValue());
            return response;
        }

        log.info("Successfully created client, proceeding to create client account");
        return createClientAccount((WalletUser) response.getBody(), user.getAccountType(), user.getDescription());
    }


    @Override
    public WalletAccount createNubanAccount(WalletUserDTO user) {
        WalletUser existingUser = walletUserRepository.findByUserIdAndProfileId(user.getUserId(),user.getProfileId());
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
            log.info("Exiting createNubanAccount method");

            return account;
        } catch (Exception e) {
            throw new CustomException(e.getLocalizedMessage(), HttpStatus.EXPECTATION_FAILED);

        }
    }


    public WalletAccount createNubanAccountVersion2(WalletUser wayaGramUser, WalletUser businessObj, VirtualAccountRequest accountRequest) {

        String acct_name = wayaGramUser.getCust_name().toUpperCase()+" / "+accountRequest.getAccountName().toUpperCase();

        WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
        WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);

        String acct_ownership = null;
        String acctNo = null;
        Integer rand = reqUtil.getAccountNo();
        if (rand == 0) {
            log.info("Unable to generate Wallet Account");
            return null;
        }

        if (!wayaGramUser.getCust_sex().equals("S")) {
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

        String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, wayaGramUser.getAccountType());
        try {
            String hashed_no = reqUtil
                    .WayaEncrypt(accountRequest.getUserId() + "|" + nubanAccountNumber + "|" + wayaProduct + "|" + product.getCrncy_code());

            WalletAccount account = new WalletAccount();
            if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
                    || product.getProduct_type().equals("ODA"))) {
                account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, wayaGramUser,
                        code.getGlSubHeadCode(), wayaProduct,
                        acct_ownership, hashed_no, product.isInt_paid_flg(), product.isInt_coll_flg(), "WAYADMIN",
                        LocalDate.now(), product.getCrncy_code(), product.getProduct_type(), product.isChq_book_flg(),
                        product.getCash_dr_limit(), product.getXfer_dr_limit(), product.getCash_cr_limit(),
                        product.getXfer_cr_limit(), false, wayaGramUser.getAccountType(), "Virtual Account", true);
            }


            WalletAccount response = coreBankingService.createAccountExtension(wayaGramUser, account);
            log.info("Exiting: {}", response);

            return response;
        } catch (Exception e) {
            throw new CustomException(e.getLocalizedMessage(), HttpStatus.EXPECTATION_FAILED);

        }
    }

    @Override
    public void setupSystemUser() {
        log.info("Entering setupSystemUser method");
        WalletUser walletUser = walletUserRepository.findByEmailAddress(systemUserEmail);
        if (ObjectUtils.isNotEmpty(walletUser)) {
            log.info("System user already exists");
            return;
        }

        WalletUser userInfo = new WalletUser("0000", 0L, "SYSTEM",
                "ACCOUNT", systemUserEmail, systemUserMobileNumber, "SYSTEM ACCOUNT",
                null, null, null,
                null, null, LocalDate.now(), 100000000.0);

        walletUserRepository.save(userInfo);
        log.info("Exiting setupSystemUser method");
    }

    public WalletUser creatUserAccountUtil(UserDetailPojo userDetailPojo) {
        log.info("Entering creatUserAccountUtil method");
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
        log.info("Exiting creatUserAccountUtil method");
        return userx;
    }

    // Call by Aut-service and others
    public ResponseEntity<?> createUserAccount(String clientId,String clientType,WalletUserDTO user, String token) {
        log.info("Entering createUserAccount method");
        String profileId = null;
        if(user.getProfileId() != null)
            profileId = user.getProfileId();

        String profileAccountType = null;
        if(user.getProfileType() != null)
            profileAccountType = user.getProfileType();

        log.info("::WalletUserDTO {}",user);
        ResponseEntity<?> response = createClient(clientId,clientType,user.getUserId(), token,profileId,profileAccountType);
        log.info(":::CreateClient Response {} ", response);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("ERROR CreateUserAccount::: {}", response);
            return response;
        }
        log.info("Exiting createUserAccount method");
        return createClientAccount((WalletUser) response.getBody(), user.getAccountType(), user.getDescription());
    }


    public ResponseEntity<?> createUserAccount2(String clientId,String clientType,WalletUserDTO user, String token) {
        log.info("Entering createUserAccount method");
        String profileId = null;
        if(user.getProfileId() != null)
            profileId = user.getProfileId();

        String profileAccountType = null;
        if(user.getProfileType() != null)
            profileAccountType = user.getProfileType();


        // Creating a wayagram Account will not require creating a wallet user
        // as we are using the Wayagram user to add merchants account under it
        // at this point we will find the Wayagram user using the wayagram account number

        //WalletAccount wayaGramAccount = this.findUserAccount(user.);
        log.info("::WalletUserDTO {}",user);
        ResponseEntity<?> response = createClient(clientId,clientType,user.getUserId(), token,profileId,profileAccountType);
        log.info(":::CreateClient Response {} ", response);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("ERROR CreateUserAccount::: {}", response);
            return response;
        }
        // no Creation of user
        log.info("Exiting createUserAccount method");
        return createClientVirtualAccount((WalletUser) response.getBody(), user.getAccountType(), user.getDescription());
    }

    public ResponseEntity<?> createNubbanAccountAuto() {
        log.info("Entering createNubbanAccountAuto method");
        log.info("Exiting createNubbanAccountAuto method");
        return new ResponseEntity<>(new SuccessResponse("Successfully createAccountOnMIFOS"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> modifyUserAccount(HttpServletRequest request,UserAccountDTO user) {
        log.info("Entering modifyUserAccount method");
        WalletUser existingUser = walletUserRepository.findByUserIdAndProfileId(user.getUserId(),user.getProfileId());
        if (existingUser == null) {
            log.error("Wallet User does not exist");
            return new ResponseEntity<>(new ErrorResponse("Wallet User does not exists"), HttpStatus.NOT_FOUND);
        }
        int userId = user.getUserId().intValue();
        UserDetailPojo wallet = authService.AuthUser(request,userId);
        if (wallet.isAccountDeleted()) {
            log.error("Auth User has been deleted");
            return new ResponseEntity<>(new ErrorResponse("Auth User has been deleted"), HttpStatus.BAD_REQUEST);
        }
        if (!user.getNewEmailId().isBlank() && !user.getNewEmailId().isEmpty()) {
            WalletUser existingEmail = walletUserRepository.findByEmailAddress(user.getNewEmailId());
            if (existingEmail != null) {
                log.error("Email already used on Wallet User Account");
                return new ResponseEntity<>(new ErrorResponse("Email already used on Wallet User Account"),
                        HttpStatus.NOT_FOUND);
            }
            existingUser.setEmailAddress(user.getNewEmailId());
        }
        if (!user.getNewMobileNo().isBlank() && !user.getNewMobileNo().isEmpty()) {
            WalletUser existingPhone = walletUserRepository.findByMobileNo(user.getNewMobileNo());
            if (existingPhone != null) {
                log.error("PhoneNo already used on Wallet User Account");
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
                    log.error("Wallet Account does not exist");
                    return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
                            HttpStatus.NOT_FOUND);
                }
                account.setWalletDefault(false);
                walletAccountRepository.save(account);
                WalletAccount caccount = walletAccountRepository.findByAccountNo(user.getNewDefaultAcctNo());
                if (caccount == null) {
                    log.error("Wallet Account does not exist");
                    return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
                            HttpStatus.NOT_FOUND);
                }
                caccount.setWalletDefault(true);
                walletAccountRepository.save(caccount);
                log.info("Account created successfully.");
                return new ResponseEntity<>(new SuccessResponse("Account created successfully.", account),
                        HttpStatus.CREATED);
            } catch (Exception e) {
                log.error("Exception occurred: {}", e.getMessage());
                return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
            }
        }
        log.info("Successfully Update Without No Account Affected");
        return new ResponseEntity<>(new SuccessResponse("Successfully Update Without No Account Affected"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> ToggleAccount(HttpServletRequest request, AccountToggleDTO user) {
        log.info("Entering ToggleAccount method");
        WalletUser existingUser = walletUserRepository.findByUserIdAndProfileId(user.getUserId(), user.getProfileId());
        if (existingUser == null) {
            log.error("Wallet User does not exist");
            return new ResponseEntity<>(new ErrorResponse("Wallet User does not exists"), HttpStatus.NOT_FOUND);
        }
        int userId = user.getUserId().intValue();
        UserDetailPojo wallet = authService.AuthUser(request, userId);
        if (wallet.isAccountDeleted()) {
            log.error("Auth User has been deleted");
            return new ResponseEntity<>(new ErrorResponse("Auth User has been deleted"), HttpStatus.BAD_REQUEST);
        }
        // Default Wallet
        walletUserRepository.save(existingUser);
        if (!user.getNewDefaultAcctNo().isEmpty() && !user.getNewDefaultAcctNo().isBlank()) {
            try {
                Optional<WalletAccount> accountDef = walletAccountRepository.findByDefaultAccount(existingUser);
                if (!accountDef.isPresent()) {
                    log.error("Wallet Account does not exist");
                    return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"),
                            HttpStatus.NOT_FOUND);
                }
                WalletAccount account = accountDef.get();
                account.setWalletDefault(false);
                walletAccountRepository.save(account);
                WalletAccount caccount = walletAccountRepository.findByAccountNo(user.getNewDefaultAcctNo());
                if (caccount == null) {
                    log.error("Wallet Account does not exist");
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
                log.info("Account set as default successfully.");
                return new ResponseEntity<>(new SuccessResponse("Account set as default successfully.", account),
                        HttpStatus.CREATED);
            } catch (Exception e) {
                log.error("Exception occurred: {}", e.getMessage());
                return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
            }
        }
        log.info("Successfully Update Without No Account Affected");
        return new ResponseEntity<>(new SuccessResponse("Successfully Update Without No Account Affected"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> UserAccountAccess(AdminAccountRestrictionDTO user) {
        log.info("Entering UserAccountAccess method");
        if (!user.isAcctClosed()) {

        } else if (!user.isAcctfreez()) {
            if (!user.getFreezCode().isBlank() && !user.getFreezCode().isEmpty()) {
                log.error("Freeze Code should not be entered");
                return new ResponseEntity<>(new ErrorResponse("Freeze Code should not be entered"),
                        HttpStatus.NOT_FOUND);
            }
            if (!user.getFreezReason().isBlank() && !user.getFreezReason().isEmpty()) {
                log.error("Freeze Reason should not be entered");
                return new ResponseEntity<>(new ErrorResponse("Freeze Reason should not be entered"),
                        HttpStatus.NOT_FOUND);
            }
        } else if (!user.isAmountRestrict()) {
            if (!user.getLienReason().isBlank() && !user.getLienReason().isEmpty()) {
                log.error("Lien Reason should not be entered");
                return new ResponseEntity<>(new ErrorResponse("Lien Reason should not be entered"),
                        HttpStatus.NOT_FOUND);
            }
            if (user.getLienAmount().compareTo(BigDecimal.ZERO) != 0
                    && user.getLienAmount().compareTo(BigDecimal.ZERO) != 0) {
                log.error("Lien Amount should not be entered");
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
                log.error("Wallet Account does not exists");
                return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"), HttpStatus.NOT_FOUND);
            }

            userDelete = walletUserRepository.findByAccount(account);
            if (account.isAcct_cls_flg() && userDelete.isDel_flg()) {
                log.info("Wallet Account Deleted Successfully");
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
                    log.error("Enter Correct Code");
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
                    log.error("Account balance must be equal to zero before it can be closed");
                    return new ResponseEntity<>(new ErrorResponse("Account balance must be equal to zero before it can be closed"),
                            HttpStatus.NOT_FOUND);
                }
            } else {
                log.error("All User Accounts balance must be equal to zero before it can be closed");
                return new ResponseEntity<>(new ErrorResponse("All User Accounts balance must be equal to zero before it can be closed"),
                        HttpStatus.NOT_FOUND);
            }
            if (user.isAmountRestrict()) {
                double acctAmt = account.getLien_amt() + user.getLienAmount().doubleValue();
                account.setLien_amt(acctAmt);
                account.setLien_reason(user.getLienReason());
            }
            walletAccountRepository.save(account);
            walletUserRepository.save(userDelete);
            log.info("Account updated successfully.");
            return new ResponseEntity<>(new SuccessResponse("Account updated successfully.", account),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> createCashAccount(HttpServletRequest request, WalletCashAccountDTO user) {
        log.info("Entering createCashAccount method");
        boolean validate = paramValidation.validateDefaultCode(user.getCashAccountCode(), "Batch Account");
        if (!validate) {
            log.error("Batch Account Validation Failed");
            return new ResponseEntity<>(new ErrorResponse("Batch Account Validation Failed"), HttpStatus.BAD_REQUEST);
        }

        boolean validate2 = paramValidation.validateDefaultCode(user.getCrncyCode(), "Currency");
        if (!validate2) {
            log.error("Currency Code Validation Failed");
            return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
        }

        UserDetailPojo userd = authService.AuthUser(request, (user.getUserId().intValue()));
        if (!userd.isAdmin()) {
            log.error("User Not Admin");
            return new ResponseEntity<>(new ErrorResponse("User Not Admin"), HttpStatus.BAD_REQUEST);
        }

        WalletProduct product = walletProductRepository.findByProductCode(user.getProductCode(), user.getProductGL());
        if ((!product.getProduct_type().equals("OAB"))) {
            log.error("Product Type Does Not Match");
            return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
        }

        WalletProductCode code = walletProductCodeRepository.findByProductGLCode(user.getProductCode(),
                user.getProductGL());
        if ((!code.getProductType().equals("OAB"))) {
            log.error("Product Type Does Not Match");
            return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
        }

        Optional<WalletTeller> tellerx = walletTellerRepository.findByUserCashAcct(user.getUserId(),
                user.getCrncyCode(), user.getCashAccountCode());
        if (!tellerx.isPresent()) {
            log.error("User Cash Account Does Not Exist");
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
            log.info("Office Account created successfully.");
            return new ResponseEntity<>(new SuccessResponse("Office Account created successfully.", account),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getLocalizedMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> createEventAccount(WalletEventAccountDTO user) {
        log.info("Entering createEventAccount method");
        boolean validate = paramValidation.validateDefaultCode(user.getPlaceholderCode(), "Batch Account");
        if (!validate) {
            log.error("Batch Account Validation Failed");
            return new ResponseEntity<>(new ErrorResponse("Batch Account Validation Failed"), HttpStatus.BAD_REQUEST);
        }

        boolean validate2 = paramValidation.validateDefaultCode(user.getCrncyCode(), "Currency");
        if (!validate2) {
            log.error("Currency Code Validation Failed");
            return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
        }

        WalletEventCharges event = walletEventRepo.findByEventCurrency(user.getEventId(), user.getCrncyCode())
                .orElse(null);
        if (event == null) {
            log.error("No Event created");
            return new ResponseEntity<>(new ErrorResponse("No Event created"), HttpStatus.BAD_REQUEST);
        }

        WalletProduct product = walletProductRepository.findByProductCode(user.getProductCode(), user.getProductGL());
        if ((!product.getProduct_type().equals("OAB"))) {
            log.error("Product Type Does Not Match");
            return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
        }

        WalletProductCode code = walletProductCodeRepository.findByProductGLCode(user.getProductCode(),
                user.getProductGL());
        if ((!code.getProductType().equals("OAB"))) {
            log.error("Product Type Does Not Match");
            return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
        }

        WalletUser walletUser = walletUserRepository.findByEmailAddress(systemUserEmail);
        if (ObjectUtils.isEmpty(walletUser)) {
            log.error("System config not completed");
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
            log.info("Office Account created successfully.");
            return new ResponseEntity<>(new SuccessResponse("Office Account created successfully.", _account),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getLocalizedMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> createAccount(HttpServletRequest request, AccountPojo2 accountPojo, String token) {
        // Logging token validation
        log.info("Validating token...");
        MyData tokenData = tokenService.getUserInformation(request);
        if (tokenData == null) {
            log.error("Token validation failed: Token data is null");
            return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
        }

        // Logging user authorization
        log.info("Authorizing user...");
        if (!isAllowed(tokenData, accountPojo.getUserId())) {
            log.error("User authorization failed: User is not allowed");
            return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
        }

        int userId = accountPojo.getUserId().intValue();
        // Logging user authentication
        log.info("Authenticating user...");
        UserDetailPojo user = authService.AuthUser(request, userId);
        if (user == null) {
            log.error("User authentication failed: Auth user ID does not exist");
            return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exist"), HttpStatus.BAD_REQUEST);
        }

        // Logging wallet retrieval
        log.info("Retrieving wallet user details...");
        WalletUser y = walletUserRepository.findByUserIdAndProfileId(accountPojo.getUserId(), accountPojo.getProfileId());
        WalletUser x = walletUserRepository.findByEmailAddressAndProfileId(user.getEmail(), accountPojo.getProfileId());

        if (x == null && y == null && Long.compare(accountPojo.getUserId(), tokenData.getId()) == 0) {
            // Logging default wallet creation
            log.info("Creating default wallet...");
            return createDefaultWallet(request, tokenData, token, accountPojo.getProfileId());
        } else if (x == null && y == null) {
            log.error("Default wallet not created: No user found");
            return new ResponseEntity<>(new ErrorResponse("Default Wallet Not Created"), HttpStatus.BAD_REQUEST);
        }

        // Logging data integrity check
        log.info("Checking data integrity...");
        if (!y.getEmailAddress().equals(x.getEmailAddress())) {
            log.error("Data integrity check failed: Wallet data integrity issue");
            return new ResponseEntity<>(new ErrorResponse("Wallet Data Integrity.please contact Admin"), HttpStatus.BAD_REQUEST);
        } else if (y.getEmailAddress().equals(x.getEmailAddress())) {
            // Logging product and code retrieval
            log.info("Retrieving product and code details...");
            WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProduct, wayaGLCode);
            WalletProduct product = walletProductRepository.findByProductCode(wayaProduct, wayaGLCode);
            String acctNo = null;

            Integer rand = reqUtil.getAccountNo();
            if (rand == 0) {
                log.error("Account number generation failed: Unable to generate account number");
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

            // Logging account name generation
            log.info("Generating account name...");
            String acct_name;
            if (user.isCorporate()) {
                String newAccountName = y.getCust_name() + " " + accountPojo.getDescription();
                acct_name = newAccountName;
            } else {
                String oldName = y.getFirstName().toUpperCase() + " " + y.getLastName().toUpperCase();
                if (!y.getCust_name().toUpperCase().contains(oldName)) {
                    String newAccountName = y.getCust_name() + " " + accountPojo.getDescription();
                    acct_name = newAccountName;
                } else {
                    acct_name = oldName + " " + accountPojo.getDescription();
                }
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

                    // Logging account creation
                    log.info("Creating account...");
                    coreBankingService.createAccount(y, _account);

                    return new ResponseEntity<>(new SuccessResponse("Account Created Successfully.", _account),
                            HttpStatus.CREATED);

                }
            } catch (Exception e) {
                // Logging exception
                log.error("Exception occurred: " + e.getMessage());
                return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
            }
        }

        // Logging default wallet not created
        log.error("Default Wallet has not been created: Please contact Admin");
        return new ResponseEntity<>(new ErrorResponse("Default Wallet has not been created. Please contact Admin"),
                HttpStatus.NOT_FOUND);
    }


    public ResponseEntity<?> createAccountProduct(HttpServletRequest request, AccountProductDTO accountPojo) {
        int userId = (int) accountPojo.getUserId();
        // Logging user authentication
        log.info("Authenticating user...");
        UserDetailPojo user = authService.AuthUser(request, userId);
        if (user == null) {
            log.error("User authentication failed: Auth user ID does not exist");
            return new ResponseEntity<>(new ErrorResponse("Auth User ID does not exist"), HttpStatus.BAD_REQUEST);
        }

        // Logging product retrieval
        log.info("Retrieving product details...");
        WalletProductCode fProduct = walletProductCodeRepository.findByProduct(accountPojo.getProductCode());
        if (fProduct == null) {
            log.error("Product retrieval failed: Product Code does not exist");
            return new ResponseEntity<>(new ErrorResponse("Product Code does not exist"), HttpStatus.BAD_REQUEST);
        }

        // Logging wallet retrieval
        log.info("Retrieving wallet user details...");
        WalletUser y = walletUserRepository.findByUserIdAndProfileId(accountPojo.getUserId(), accountPojo.getProfileId());
        WalletUser x = walletUserRepository.findByEmailAddressAndProfileId(user.getEmail(), accountPojo.getProfileId());

        if (x == null && y == null) {
            log.error("Default wallet not created: No user found");
            return new ResponseEntity<>(new ErrorResponse("Default Wallet Not Created"), HttpStatus.BAD_REQUEST);
        }

        // Logging data integrity check
        log.info("Checking data integrity...");
        if (!y.getEmailAddress().equals(x.getEmailAddress())) {
            log.error("Data integrity check failed: Wallet data integrity issue");
            return new ResponseEntity<>(new ErrorResponse("Wallet Data Integity.please contact Admin"),
                    HttpStatus.BAD_REQUEST);
        } else if (y.getEmailAddress().equals(x.getEmailAddress())) {
            // Logging product and code retrieval
            log.info("Retrieving product and code details...");
            WalletProductCode code = walletProductCodeRepository.findByProductGLCode(fProduct.getProductCode(),
                    fProduct.getGlSubHeadCode());
            WalletProduct product = walletProductRepository.findByProductCode(fProduct.getProductCode(),
                    fProduct.getGlSubHeadCode());

            // Logging account number generation
            log.info("Generating account number...");
            String acctNo = null;
            String acct_name = y.getFirstName().toUpperCase() + " " + y.getLastName().toUpperCase();
            Integer rand = reqUtil.getAccountNo();
            if (rand == 0) {
                log.error("Account number generation failed: Unable to generate account number");
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
            } else if ((product.getProduct_type().equals("OAB"))) {
                acct_ownership = "O";
                acctNo = product.getCrncy_code() + "0000" + rand;
            }

            // Logging account creation
            log.info("Creating account...");
            try {
                String hashed_no = reqUtil.WayaEncrypt(
                        userId + "|" + acctNo + "|" + fProduct.getProductCode() + "|" + product.getCrncy_code());

                WalletAccount account = new WalletAccount();
                String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, accountPojo.getAccountType());
                if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
                        || product.getProduct_type().equals("ODA"))) {
                    account = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, y,
                            code.getGlSubHeadCode(),
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
                // Logging exception
                log.error("Exception occurred: " + e.getMessage());
                return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
            }
        } else {
            log.error("Default Wallet has not been created: Please contact Admin");
            return new ResponseEntity<>(new ErrorResponse("Default Wallet has not been created. Please contact Admin"),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ApiResponse<?> findCustWalletById(Long walletId) {
        // Logging wallet retrieval by ID
        log.info("Retrieving wallet by ID...");
        Optional<WalletUser> wallet = walletUserRepository.findById(walletId);
        if (!wallet.isPresent()) {
            log.error("Wallet retrieval failed: Wallet ID not found");
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Failed", null);
        }
        ApiResponse<?> resp = new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "Success", wallet.get());
        return resp;
    }

    @Override
    public ApiResponse<?> findAcctWalletById(Long walletId) {
        // Logging wallet retrieval by ID
        log.info("Retrieving wallet by ID...");
        Optional<WalletUser> wallet = walletUserRepository.findById(walletId);
        if (!wallet.isPresent()) {
            log.error("Wallet retrieval failed: Wallet ID not found");
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
                log.error("Invalid User: User ID not found");
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
        // Logging account information retrieval
        log.info("Retrieving account information...");
        securityWtihAccountNo(accountNo);
        WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
        if (account == null) {
            log.error("Invalid Account: Account number not found");
            return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAccountInfoWithUserInfo(String accountNo) {
        // Logging account information retrieval with user info
        log.info("Retrieving account information with user info... --->>> {}", accountNo);
        // securityWtihAccountNo(accountNo);
        WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
        if (account == null) {
            log.error("Invalid Account: Account number not found");
            return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.BAD_REQUEST);
        }
        WalletUser user = walletUserRepository.findByAccount(account);
        return new ResponseEntity<>(new SuccessResponse("Success", user), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> nameEnquiry(String accountNo) {
        // Logging name enquiry
        log.info("Performing name enquiry on account ---->> {}", accountNo);
        WalletAccount acct = walletAccountRepository.findByAccountNo(accountNo);
        if (acct == null) {
            log.error("Invalid Account: Account number not found");
            return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.NOT_FOUND);
        }

        AccountDetailDTO account = new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getAccountNo(),
                acct.getAcct_name(), acct.getProduct_code(), BigDecimal.valueOf(0.0),
                acct.getAcct_crncy_code(), acct.isWalletDefault());

        return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
    }

    public ResponseEntity<?> fetchAccountDetail(String accountNo, Boolean isAdmin) {
        if (!isAdmin) {
            // Logging account detail fetching
            log.info("Fetching account detail... --->> {}", accountNo);
            securityWtihAccountNo(accountNo);
        }

        WalletAccount acct = walletAccountRepository.findByAccountNo(accountNo);
        if (acct == null) {
            log.error("Invalid Account: Account number not found");
            return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.NOT_FOUND);
        }
        AccountDetailDTO account = new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getAccountNo(),
                acct.getAcct_name(), acct.getProduct_code(), BigDecimal.valueOf(acct.getClr_bal_amt()),
                acct.getAcct_crncy_code(), acct.isWalletDefault(), acct.isAcct_cls_flg(), acct.isDel_flg(),
                acct.getNubanAccountNo());
        WalletUser xUser = walletUserRepository.findByAccount(acct);
        if (xUser != null) {
            account.setEmail(xUser.getEmailAddress());
            account.setPhoneNumber(xUser.getMobileNo());
            account.setProfileId(xUser.getProfileId());
            account.setUserId(xUser.getUserId());
            account.setFirstName(xUser.getFirstName());
            account.setLastName(xUser.getLastName());
        }
        return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
    }


    public ResponseEntity<?> fetchUserByAccountNo(String accountNo) {
        log.info("Fetching user by account number: {}", accountNo);
        WalletAccount acct = walletAccountRepository.findByAccountNo(accountNo);
        if (acct == null) {
            log.error("Invalid Account: {}", accountNo);
            return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.NOT_FOUND);
        }
        AccountUserDetail account = new AccountUserDetail();
        account.setAccountNo(acct.getAccountNo());
        account.setAccountName(acct.getAcct_name());
        account.setAccountDefault(acct.isWalletDefault());
        account.setNubanAccountNo(acct.getNubanAccountNo());
        account.setCurrencyCode(acct.getAcct_crncy_code());
        WalletUser xUser = walletUserRepository.findByAccount(acct);
        if (xUser != null) {
            account.setEmail(xUser.getEmailAddress());
            account.setPhoneNumber(xUser.getMobileNo());
            account.setProfileId(xUser.getProfileId());
            account.setUserId(xUser.getUserId());
        }
        log.info("User fetched successfully by account number: {}", accountNo);
        return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> fetchVirtualAccountDetail(String accountNo) {
        log.info("Fetching virtual account detail for account number: {}", accountNo);
        WalletAccount acct = walletAccountRepository.findByNubanAccountNo(accountNo);
        if (acct == null) {
            log.error("Invalid Account: {}", accountNo);
            return new ResponseEntity<>(new ErrorResponse("Invalid Account"), HttpStatus.NOT_FOUND);
        }
        AccountDetailDTO account = new AccountDetailDTO(acct.getId(), acct.getSol_id(), acct.getNubanAccountNo(),
                acct.getAcct_name(), acct.getProduct_code(), BigDecimal.valueOf(acct.getClr_bal_amt()),
                acct.getAcct_crncy_code());
        log.info("Virtual account detail fetched successfully for account number: {}", accountNo);
        return new ResponseEntity<>(new SuccessResponse("Success", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserAccountList(HttpServletRequest request,long userId,String profileId, String token) {
        log.info("Getting user account list for userId={}, profileId={}, token={}", userId, profileId, "********");
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        MyData tokenData = MyData.newInstance(_userToken);
        if (tokenData == null) {
            log.error("Failed to retrieve token data");
            return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
        }

        if (!isAllowed(tokenData, userId)) {
            log.error("User is not allowed to perform this action");
            return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
        }
        updateExistingWalletUser(userId, profileId, tokenData.isCorporate());

        Optional<WalletUser> walletUser = walletUserRepository.findUserIdAndProfileId(userId, profileId);
        if (!walletUser.isPresent() && userId == tokenData.getId()) {
            return createDefaultWallet(request, tokenData, token, profileId);
        } else if (!walletUser.isPresent()) {
            log.error("Wallet user not found for userId={}, profileId={}", userId, profileId);
            return new ResponseEntity<>(new ErrorResponse("FAILED"), HttpStatus.BAD_REQUEST);
        }

        List<WalletAccount> accounts = walletAccountRepository.findByUser(walletUser.get());
        if (ObjectUtils.isEmpty(accounts)) {
            return createDefaultWallet(request, tokenData, token, profileId);
        }

        log.info("User account list fetched successfully for userId={}, profileId={}", userId, profileId);
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", accounts), HttpStatus.OK);
    }

    private boolean isAllowed(MyData tokenData, long userId) {
        log.info("Checking if user is allowed to perform the action: userId={}, tokenData={}", userId, tokenData);
        boolean isWriteAdmin = tokenData.getRoles().stream().anyMatch("ROLE_ADMIN_OWNER"::equalsIgnoreCase);
        isWriteAdmin = tokenData.getRoles().stream().anyMatch("ROLE_ADMIN_APP"::equalsIgnoreCase) ? true : isWriteAdmin;
        boolean isOwner = Long.compare(userId, tokenData.getId()) == 0;

        if (isOwner || isWriteAdmin) {
            log.error("Owner check: isOwner={}, isWriteAdmin={}", isOwner, isWriteAdmin);
            return true;
        }

        return false;
    }

    public ResponseEntity<?> ListUserAccount(HttpServletRequest request, long userId) {
        log.info("Listing user accounts for userId={}", userId);
        try {

            WalletUser x = walletUserRepository.findFirstByUserId(userId);
            if (x == null)
                return new ResponseEntity<>(new ErrorResponse("Wallet User does not exist"), HttpStatus.NOT_FOUND);

            List<NewWalletAccount> accounts = new ArrayList<>();
            List<WalletAccount> listAcct = walletAccountRepository.findByUser(x);
            if (listAcct == null) {
                return new ResponseEntity<>(new ErrorResponse("Account List Does Not Exist"), HttpStatus.NOT_FOUND);
            }
            for (WalletAccount wAcct : listAcct) {
                NewWalletAccount mAcct = new NewWalletAccount(wAcct, userId);
                accounts.add(mAcct);
            }

            log.info("User accounts listed successfully for userId={}", userId);
            return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("Exception occurred while listing user accounts: {}", ex.getMessage());
            return new ResponseEntity<>(new ErrorResponse("Unable able to fetch account"), HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> getAllAccount() {
        log.info("Getting all accounts");
        List<WalletAccount> pagedResult = walletAccountRepository.findByWalletAccount();
        log.info("All accounts fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("Success.", pagedResult), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getUserCommissionList(long userId, Boolean isAdmin, String profileId) {
        log.info("Getting user commission list for userId={}, isAdmin={}, profileId={}", userId, isAdmin, profileId);
        String userProfileId = null;
        if (profileId != null)
            userProfileId = profileId;

        WalletUser userx;
        if (!isAdmin) {
            if (userProfileId == null)
                return new ResponseEntity<>(new ErrorResponse("Invalid Profile ID"), HttpStatus.BAD_REQUEST);
            securityCheck(userId, userProfileId);

            userx = walletUserRepository.findByUserIdAndProfileId(userId, userProfileId);
            if (userx == null)
                return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
        } else {
            if (userProfileId == null) {
                List<WalletUser> walletUserList = walletUserRepository.findAllByUserId(userId);
                if (walletUserList.size() == 1) {
                    userx = walletUserList.get(0);
                } else {
                    return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
                }
            } else {
                userx = walletUserRepository.findByUserIdAndProfileId(userId, userProfileId);
                if (userx == null)
                    return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
            }
        }

        Optional<WalletAccount> accounts = walletAccountRepository.findByAccountUser(userx);
        if (!accounts.isPresent()) {
            return new ResponseEntity<>(new ErrorResponse("No Commission Account"), HttpStatus.BAD_REQUEST);
        }
        log.info("User commission list fetched successfully for userId={}, profileId={}", userId, profileId);
        return new ResponseEntity<>(new SuccessResponse("Success.", accounts), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> makeDefaultWallet(String accountNo) {
        log.info("Making default wallet for accountNo={}", accountNo);
        securityWtihAccountNo(accountNo);
        WalletAccount account = walletAccountRepository.findByAccountNo(accountNo);
        if (account == null) {
            log.error("Invalid Account No: {}", accountNo);
            return new ResponseEntity<>(new ErrorResponse("Invalid Account No"), HttpStatus.BAD_REQUEST);
        }
        log.info("Default wallet set successfully for accountNo={}", accountNo);
        return new ResponseEntity<>(new SuccessResponse("Default wallet set", account), HttpStatus.OK);
    }

    private MyData getEmailFromToken(long userId) {
        log.info("Getting email from token for userId={}", userId);
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        MyData myData = MyData.newInstance(_userToken);
        log.info("Email retrieved successfully from token for userId={}", userId);
        return myData;
    }

    private void securityWtihAccountNo(String accountNo) {
        log.info("Performing security check for accountNo={}", accountNo);
        try {
            WalletAccount walletAccount = walletAccountRepository.findByAccountNo(accountNo);
            WalletUser xUser = walletUserRepository.findByAccount(walletAccount);
            securityCheck(Long.valueOf(xUser.getUserId()), xUser.getProfileId());
        } catch (CustomException ex) {
            log.error("Security check failed: {}", ex.getMessage());
            throw new CustomException("You Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
        }
    }

    public void securityCheck(long userId, String profileId) {
        log.info("Performing security check for userId={}, profileId={}", userId, profileId);
        WalletUser user = walletUserRepository.findByUserIdAndProfileId(userId, profileId);
        if (Objects.isNull(user)) {
            log.error("Security check failed: User not found for userId={}, profileId={}", userId, profileId);
            throw new CustomException("You Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
        }

        if (!user.getUserId().equals(userId)) {
            log.error("Security check failed: UserId does not match for userId={}, profileId={}", userId, profileId);
            throw new CustomException("You Lack credentials to perform this action", HttpStatus.BAD_REQUEST);
        }
        log.info("Security check passed successfully for userId={}, profileId={}", userId, profileId);
    }

    @Override
    public ResponseEntity<?> UserWalletLimit(long userId, String profileId) {
        log.info("Getting user wallet limit for userId={}, profileId={}", userId, profileId);
        // security check
        securityCheck(userId, profileId);

        WalletUser user = walletUserRepository.findByUserIdAndProfileId(userId, profileId);
        if (user == null) {
            log.error("Invalid User ID: userId={}, profileId={}", userId, profileId);
            return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
        }
        log.info("User wallet limit fetched successfully for userId={}, profileId={}", userId, profileId);
        return new ResponseEntity<>(new SuccessResponse("User Wallet Info", user), HttpStatus.OK);
    }


    @Override
    public ResponseEntity<?> getALLCommissionAccount() {
        log.info("Fetching all commission accounts");
        List<WalletAccount> account = walletAccountRepository.findByProductList(wayaProductCommission);
        if (account == null || account.isEmpty()) {
            log.error("Unable to fetch commission accounts");
            return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
        }
        log.info("Commission accounts fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("Wallet Commissions", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAccountCommission(String accountNo) {
        log.info("Fetching commission account for account number: {}", accountNo);
        securityWtihAccountNo(accountNo);
        Optional<WalletAccount> account = walletAccountRepository.findByAccountProductCode(wayaProductCommission,
                accountNo);
        if (!account.isPresent()) {
            log.error("Unable to fetch commission account for account number: {}", accountNo);
            return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
        }
        log.info("Commission account fetched successfully for account number: {}", accountNo);
        return new ResponseEntity<>(new SuccessResponse("Wallet Commissions", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getAccountDetails(String accountNo, Boolean isAdmin) {
        log.info("Fetching account details for account number: {}", accountNo);
        if (!isAdmin) {
            securityWtihAccountNo(accountNo);
        }

        try {
            Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNo);
            if (!account.isPresent()) {
                log.error("Unable to fetch account details for account number: {}", accountNo);
                return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
            }
            log.info("Account details fetched successfully for account number: {}", accountNo);
            return new ResponseEntity<>(new SuccessResponse("Wallet", account.get()), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An error occurred while fetching account details for account number {}: {}", accountNo, ex.getLocalizedMessage());
            String customException;
            if (ex instanceof CustomException) {
                customException = objectMapper.convertValue(ex.getLocalizedMessage(), String.class);
                return new ResponseEntity<>(new ErrorResponse(customException), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(new ErrorResponse("Unable to fetch your default account, try again later"), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Override
    public ResponseEntity<?> getAccountDefault(Long user_id, String profileId) {
        log.info("Fetching default account for user ID: {}, profile ID: {}", user_id, profileId);
        try {
            securityCheck(user_id, profileId);
            WalletUser user = walletUserRepository.findByUserIdAndProfileId(user_id, profileId);
            if (user == null) {
                log.error("Invalid User ID: {}", user_id);
                return new ResponseEntity<>(new ErrorResponse("Invalid User ID"), HttpStatus.BAD_REQUEST);
            }

            Optional<WalletAccount> account = walletAccountRepository.findByDefaultAccount(user);
            if (!account.isPresent()) {
                log.error("Unable to fetch default account for user ID: {}, profile ID: {}", user_id, profileId);
                return new ResponseEntity<>(new ErrorResponse("Unable to fetch default account"), HttpStatus.BAD_REQUEST);
            }
            log.info("Default account fetched successfully for user ID: {}, profile ID: {}", user_id, profileId);
            return new ResponseEntity<>(new SuccessResponse("Wallet Default", account.get()), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("An error occurred while fetching default account for user ID: {}, profile ID {}: {}", user_id, profileId, ex.getLocalizedMessage());
            String customException;
            if (ex instanceof CustomException) {
                customException = objectMapper.convertValue(ex.getLocalizedMessage(), String.class);
                return new ResponseEntity<>(new ErrorResponse(customException), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(new ErrorResponse("Unable to fetch your default account, try again later"), HttpStatus.BAD_REQUEST);
            }
        }
    }

    public ResponseEntity<?> searchAccount(String search) {
        log.info("Searching account with search term: {}", search);
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
            log.info("Account search successful with search term: {}", search);
            return new ResponseEntity<>(new SuccessResponse("Account Wallet Search", account), HttpStatus.OK);
        }

        matchingAcct = user.stream().filter(str -> str.getMobileNo().trim().equalsIgnoreCase(search))
                .collect(Collectors.toList());
        if (!matchingAcct.isEmpty()) {
            for (WalletUser x : matchingAcct) {
                account = walletAccountRepository.findByUser(x);
                accountColl.addAll(account);
            }
            log.info("Account search successful with search term: {}", search);
            return new ResponseEntity<>(new SuccessResponse("Account Wallet Search", accountColl), HttpStatus.OK);
        }

        matchingAcct = user.stream().filter(str -> str.getEmailAddress().trim().equalsIgnoreCase(search))
                .collect(Collectors.toList());
        if (!matchingAcct.isEmpty()) {
            for (WalletUser x : matchingAcct) {
                account = walletAccountRepository.findByUser(x);
                accountColl.addAll(account);
            }
            log.info("Account search successful with search term: {}", search);
            return new ResponseEntity<>(new SuccessResponse("Account Wallet Search", accountColl), HttpStatus.OK);
        }
        log.error("Unable to fetch account with search term: {}", search);
        return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.NOT_FOUND);
    }

    @Override
    public ApiResponse<?> fetchTransaction(String acctNo) {
        log.info("Fetching transactions for account number: {}", acctNo);
        securityWtihAccountNo(acctNo);
        List<AccountStatementDTO> account = tempwallet.fetchTransaction(acctNo);
        if (account.isEmpty()) {
            log.error("No transaction record found for account number: {}", acctNo);
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO TRANSACTION RECORD", null);
        }
        log.info("Transactions fetched successfully for account number: {}", acctNo);
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESSFUL TRANSACTION STATEMENT", account);
    }

    @Override
    public ApiResponse<?> fetchFilterTransaction(String acctNo, Date fromdate, Date todate) {
        log.info("Fetching filtered transactions for account number: {}", acctNo);
        securityWtihAccountNo(acctNo);
        LocalDate fromDate = fromdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toDate = todate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<AccountStatementDTO> account = tempwallet.fetchFilterTransaction(acctNo, fromDate, toDate);
        if (account.isEmpty()) {
            log.error("No transaction record found for account number: {}", acctNo);
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO TRANSACTION RECORD", null);
        }
        log.info("Filtered transactions fetched successfully for account number: {}", acctNo);
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESSFUL TRANSACTION STATEMENT", account);
    }

    @Override
    public ApiResponse<?> fetchRecentTransaction(Long user_id, String profileId) {
        log.info("Fetching recent transactions for user ID: {}, profile ID: {}", user_id, profileId);
        securityCheck(user_id, profileId);
        WalletUser user = walletUserRepository.findByUserIdAndProfileId(user_id, profileId);
        if (user == null) {
            log.error("User ID does not exist: {}", user_id);
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "USER ID DOES NOT EXIST", null);
        }
        List<WalletAccount> accountList = walletAccountRepository.findByUser(user);
        if (accountList.isEmpty()) {
            log.error("No account found for user ID: {}", user_id);
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO ACCOUNT FOR USER ID", null);
        }
        List<AccountStatementDTO> statement = new ArrayList<>();
        for (WalletAccount acct : accountList) {
            List<AccountStatementDTO> account = tempwallet.recentTransaction(acct.getAccountNo());
            statement.addAll(account);
        }
        if (statement.isEmpty()) {
            log.error("No recent transaction found for user ID: {}", user_id);
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO TRANSACTION RECORD", null);
        }
        log.info("Recent transactions fetched successfully for user ID: {}", user_id);
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "RECENT TRANSACTION", statement);
    }

    @Override
    public ResponseEntity<?> getListWayaAccount() {
        log.info("Fetching list of WAYA accounts");
        List<WalletAccount> account = walletAccountRepository.findByWayaAccount();
        if (account.isEmpty()) {
            log.error("No WAYA accounts found");
            return new ResponseEntity<>(new ErrorResponse("NO WAYA ACCOUNT"), HttpStatus.BAD_REQUEST);
        }
        log.info("List of WAYA accounts fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("LIST WAYA ACCOUNT", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getListWalletAccount() {
        log.info("Fetching list of wallet accounts");
        WalletUser user = Util.checkOwner();
        List<WalletAccount> findByUser = walletAccountRepository.findByUser(user);
        if (findByUser.isEmpty()) {
            log.error("No wallet accounts found");
            return new ResponseEntity<>(new ErrorResponse("NO WAYA ACCOUNT"), HttpStatus.BAD_REQUEST);
        }
        log.info("List of wallet accounts fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("LIST WAYA ACCOUNT", findByUser), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> createOfficialAccount(OfficialAccountDTO accountPojo) {
        log.info("Creating official account ---->> {}", accountPojo);
        boolean validate2 = paramValidation.validateDefaultCode(accountPojo.getCrncyCode(), "Currency");
        if (!validate2) {
            log.error("Currency code validation failed");
            return new ResponseEntity<>(new ErrorResponse("Currency Code Validation Failed"), HttpStatus.BAD_REQUEST);
        }

        WalletProduct product = walletProductRepository.findByProductCode(accountPojo.getProductCode(),
                accountPojo.getProductGL());
        if ((!product.getProduct_type().equals("OAB"))) {
            log.error("Product type does not match");
            return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
        }

        WalletProductCode code = walletProductCodeRepository.findByProductGLCode(accountPojo.getProductCode(),
                accountPojo.getProductGL());
        if ((!code.getProductType().equals("OAB"))) {
            log.error("Product type does not match");
            return new ResponseEntity<>(new ErrorResponse("Product Type Does Not Match"), HttpStatus.BAD_REQUEST);
        }

        WalletUser walletUser = walletUserRepository.findByEmailAddress(systemUserEmail);
        if (ObjectUtils.isEmpty(walletUser)) {
            log.error("Wallet not properly setup");
            return new ResponseEntity<>(new ErrorResponse("Wallet not properly setup"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Integer rand = reqUtil.getAccountNo();
        if (rand == 0) {
            log.error("Unable to generate Wallet Account");
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
            WalletAccount account = new WalletAccount("0000", "", acctNo, nubanAccountNumber,
                    accountPojo.getAccountName(), null,
                    code.getGlSubHeadCode(),
                    product.getProductCode(), acct_ownership, hashed_no, product.isInt_paid_flg(),
                    product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
                    product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
                    product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(), false,
                    accountPojo.getAccountType(), accountPojo.getDescription());

            coreBankingService.createAccount(walletUser, account);
            log.info("Official account created successfully");
            return new ResponseEntity<>(new SuccessResponse("Office Account created successfully.", account),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("An error occurred while creating official account: {}", e.getLocalizedMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ArrayList<Object> createOfficialAccount(List<OfficialAccountDTO> account) {
        log.info("Creating multiple official accounts --->>> {}", account);
        ResponseEntity<?> responseEntity = null;
        ArrayList<Object> objectArrayList = new ArrayList<>();
        try {
            for (OfficialAccountDTO data : account) {
                responseEntity = createOfficialAccount(data);
                objectArrayList.add(responseEntity.getBody());
            }
            log.info("Accounts ---->> {}", account);
            return objectArrayList;
        } catch (Exception e) {
            log.error("An error occurred while creating multiple official accounts: {}", e.getMessage());
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> AccountAccessDelete(UserAccountDelete user) {
        log.info("Deleting user account: {}", user);
        try {
            WalletUser userDelete = walletUserRepository.findByUserIdAndProfileId(user.getUserId(), user.getProfileId());
            if (userDelete == null) {
                log.error("Wallet User Account does not exist");
                return new ResponseEntity<>(new ErrorResponse("Wallet User Account does not exist"), HttpStatus.NOT_FOUND);
            }

            List<WalletAccount> accountList = walletAccountRepository.findByUser(userDelete);
            if (!accountList.isEmpty()) {
                for (WalletAccount acct : accountList) {
                    if (acct.isAcct_cls_flg() && acct.getClr_bal_amt() != 0) {
                        return new ResponseEntity<>(new ErrorResponse("All User Accounts balance must be equal to 0 before they can be closed"),
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
            log.error("An error occurred while deleting user account: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> AccountAccessPause(AccountFreezeDTO user) {
        log.info("Pausing account: {}", user);
        try {
            switch (user.getFreezCode()) {
                case "D":
                    break;
                case "C":
                    break;
                case "T":
                    break;
                case "U":
                    break;
                default:
                    return new ResponseEntity<>(new ErrorResponse("Unknown freeze code"), HttpStatus.NOT_FOUND);
            }

            WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNo());
            if (account == null) {
                log.error("Wallet Account does not exist");
                return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exist"), HttpStatus.NOT_FOUND);
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
            } else if (user.getFreezCode().equalsIgnoreCase("U")) {
                account.setFrez_code("");
                account.setFrez_reason_code("");
            } else {
                return new ResponseEntity<>(new ErrorResponse("Enter Correct Code"), HttpStatus.NOT_FOUND);
            }
            walletAccountRepository.save(account);
            return new ResponseEntity<>(new SuccessResponse("Account Freeze successfully.", account), HttpStatus.OK);
        } catch (Exception e) {
            log.error("An error occurred while pausing account: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }
    @Override
    public ResponseEntity<?> AccountAccessBlockAndUnblock(AccountBlockDTO user, HttpServletRequest request) {
        log.info("Request to Account Access Block And Unblock ---->> {}", user);
        try {

            WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNo());
            if (account == null) {
                log.error("wallet account not found");
                return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exists"), HttpStatus.NOT_FOUND);
            }

            if (user.isBlock()) {
                account.setAcct_cls_date(LocalDate.now());
                account.setAcct_cls_flg(true);
                walletAccountRepository.save(account);
                //Push to NIP-INWARD FOR ACCT UPDATE
                pushAcctClosureOrBlockToInWardService(account,true);
                CompletableFuture.runAsync(() -> blockAccount(account, request, true));
                log.info("Account blocked successfully");
                return new ResponseEntity<>(new SuccessResponse("Account blocked successfully.", account),
                        HttpStatus.OK);
            } else {
                account.setAcct_cls_date(LocalDate.now());
                account.setAcct_cls_flg(false);
                walletAccountRepository.save(account);
                //Push to NIP-INWARD FOR ACCT UPDATE
                pushAcctClosureOrBlockToInWardService(account,false);
                log.info("Account Unblocked successfully");
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
            log.info("Successful");

        } catch (Exception e) {
            throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> AccountAccessClosure(AccountCloseDTO user) {
        try {
            log.info("Closing user account with account number: {}", user.getCustomerAccountNo());
            WalletAccount account = walletAccountRepository.findByAccountNo(user.getCustomerAccountNo());
            if (account == null) {
                log.error("Wallet Account with account number {} does not exist", user.getCustomerAccountNo());
                return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exist"), HttpStatus.NOT_FOUND);
            }

            if (account.isAcct_cls_flg() && account.getClr_bal_amt() != 0) {
                log.error("Account balance must be equal to zero before it can be closed");
                return new ResponseEntity<>(new ErrorResponse("Account balance must be equal to zero before it can be closed"),
                        HttpStatus.NOT_FOUND);
            } else {
                if (account.isAcct_cls_flg()) {
                    log.error("Account already closed");
                    return new ResponseEntity<>(new ErrorResponse("Account already closed"), HttpStatus.NOT_FOUND);
                }
            }

            if (account.getClr_bal_amt() == 0) {
                account.setAcct_cls_date(LocalDate.now());
                account.setAcct_cls_flg(true);
            } else {
                log.error("Account balance must be equal to 0 before it can be closed");
                return new ResponseEntity<>(new ErrorResponse("Account balance must be equal to 0 before it can be closed"),
                        HttpStatus.NOT_FOUND);
            }
            walletAccountRepository.save(account);
            // Push to NIP-INWARD FOR ACCT UPDATE
            pushAcctClosureOrBlockToInWardService(account, true);
            return new ResponseEntity<>(new SuccessResponse("Account closed successfully.", account), HttpStatus.OK);
        } catch (Exception e) {
            log.error("An error occurred while closing account: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getLocalizedMessage() + " : " + e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> AccountAccessClosureMultiple(List<AccountCloseDTO> user) {
        int count = 0;
        for (AccountCloseDTO data : user) {
            log.info("Closing user account with account number: {}", data.getCustomerAccountNo());
            ResponseEntity<?> dd = AccountAccessClosure(data);
            log.info("Response: {}", dd);
            count++;
        }
        return new ResponseEntity<>(new SuccessResponse(count + " accounts closed successfully.", user), HttpStatus.OK);
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
                account.setClr_bal_amt(account.getClr_bal_amt() - user.getLienAmount().doubleValue());
            } else {
                double acctAmt = account.getLien_amt() - user.getLienAmount().doubleValue();
                log.info("###################### account.getLien_amt() ########### " + account.getLien_amt());
                log.info("###################### user.getLienAmount() ########### " + user.getLienAmount());

                account.setLien_amt(acctAmt);
                account.setLien_reason(user.getLienReason());
                account.setClr_bal_amt(account.getClr_bal_amt() + user.getLienAmount().doubleValue());

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
        log.info("Fetching simulated account for user with ID: {}", user_id);
        WalletUser user = walletUserRepository.findBySimulated(user_id);
        if (user == null) {
            log.error("Invalid User ID OR Not Simulated");
            return new ResponseEntity<>(new ErrorResponse("Invalid User ID OR Not Simulated"), HttpStatus.BAD_REQUEST);
        }
        Optional<WalletAccount> account = walletAccountRepository.findByDefaultAccount(user);
        if (!account.isPresent()) {
            log.error("Unable to fetch simulated account");
            return new ResponseEntity<>(new ErrorResponse("Unable to fetch simulated account"), HttpStatus.BAD_REQUEST);
        }
        log.info("Simulated account fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("Simulated Account", account), HttpStatus.OK);
    }

    public ResponseEntity<?> getListSimulatedAccount() {
        log.info("Fetching list of simulated accounts");
        List<WalletUser> user = walletUserRepository.findBySimulated();
        if (user == null) {
            log.error("Not Simulated User");
            return new ResponseEntity<>(new ErrorResponse("Not Simulated User"), HttpStatus.BAD_REQUEST);
        }
        List<WalletAccount> account = walletAccountRepository.findBySimulatedAccount();
        if (account.isEmpty()) {
            log.error("No simulated account found");
            return new ResponseEntity<>(new ErrorResponse("NO SIMULATED ACCOUNT"), HttpStatus.BAD_REQUEST);
        }
        log.info("List of simulated accounts fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("LIST SIMULATED ACCOUNT", account), HttpStatus.OK);
    }

    public ResponseEntity<?> getUserAccountCount(Long userId, String profileId) {
        log.info("Fetching account count for user with ID: {} and profile ID: {}", userId, profileId);
        WalletUser user = walletUserRepository.findByUserIdAndProfileId(userId, profileId);
        if (user == null) {
            log.error("User Doesn't Exist");
            return new ResponseEntity<>(new ErrorResponse("User Doesn't Exist"), HttpStatus.BAD_REQUEST);
        }
        List<WalletAccount> account = walletAccountRepository.findByUser(user);
        if (account.isEmpty() || account == null) {
            log.error("No account created");
            return new ResponseEntity<>(new ErrorResponse("NO ACCOUNT CREATED"), HttpStatus.BAD_REQUEST);
        }
        log.info("Account count fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("LIST ACCOUNT", account), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> AccountLookUp(String account, SecureDTO secureKey) {
        log.info("Performing account lookup for account: {}", account);
        if (!secureKey.getKey().equals("yYSowX0uQVUZpNnkY28fREx0ayq+WsbEfm2s7ukn4+RHw1yxGODamMcLPH3R7lBD+Tmyw/FvCPG6yLPfuvbJVA==")) {
            log.error("Invalid Key");
            return new ResponseEntity<>(new ErrorResponse("INVALID KEY"), HttpStatus.BAD_REQUEST);
        }

        com.wayapaychat.temporalwallet.dto.AccountLookUp mAccount = tempwallet.GetAccountLookUp(account);
        if (mAccount == null) {
            log.error("Invalid Account");
            return new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT"), HttpStatus.BAD_REQUEST);
        }

        log.info("Account lookup successful");
        return new ResponseEntity<>(new SuccessResponse("ACCOUNT SEARCH", mAccount), HttpStatus.OK);
    }

    public ResponseEntity<?> getTotalActiveAccount() {
        log.info("Fetching total active account count");
        BigDecimal count = walletAccountRepository.totalActiveAccount();
        log.info("Total active account count fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    public ResponseEntity<?> countActiveAccount() {
        log.info("Counting active accounts");
        long count = walletAccountRepository.countActiveAccount();
        log.info("Active account count fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }

    public ResponseEntity<?> countInActiveAccount() {
        log.info("Counting inactive accounts");
        long count = walletAccountRepository.countInActiveAccount();
        log.info("Inactive account count fetched successfully");
        return new ResponseEntity<>(new SuccessResponse("SUCCESS", count), HttpStatus.OK);
    }


    @Override
    public ResponseEntity<?> createDefaultWallet(HttpServletRequest request,MyData tokenData, String token, String profileId) {
        log.info("Create default wallet request ---->> {}", profileId);
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
        createAccount.setProfileId(profileId);
        if(tokenData.isCorporate()){
            createAccount.setProfileType("BUSINESS");
        }else {
            createAccount.setProfileType("PERSONAL");
        }
        log.info("retrying to create wallet for {}", createAccount.getEmailId());
        return createUserAccount(request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE),createAccount, token);
    }

    public ResponseEntity<?> updateCustomerDebitLimit(String userId, BigDecimal amount,String profileId) {
        log.info("update Customer Debit Limit method called ");
        log.info("updateCustomerDebitLimit :: " + amount);
        log.info("updateCustomerDebitLimit userId :: " + userId);
        try {
            Optional<WalletUser> walletUser = walletUserRepository.findUserIdAndProfileId(Long.parseLong(userId),profileId);
            if (walletUser.isPresent()) {
                WalletUser walletUser1 = walletUser.get();
                walletUser1.setCust_debit_limit(amount.doubleValue());
                walletUserRepository.save(walletUser1);
            }
            return new ResponseEntity<>(
                    new SuccessResponse("SUCCESS", walletUserRepository.findUserIdAndProfileId(Long.parseLong(userId),profileId)),
                    HttpStatus.OK);
        } catch (CustomException ex) {
            throw new CustomException("error", HttpStatus.EXPECTATION_FAILED);
        }

    }

    public ResponseEntity<?> getCustomerDebitLimit(String userId,String profileId) {
        log.info("getCustomerDebitLimit userId :: " + userId);
        try {
            Optional<WalletUser> walletUser = walletUserRepository.findUserIdAndProfileId(Long.parseLong(userId),profileId);
            BigDecimal totalTransactionToday = new BigDecimal(0);
            HashMap<String,String> response = new HashMap<>();
            if (walletUser.isPresent()) {
                response.put("debitLimit", String.valueOf(walletUser.get().getCust_debit_limit()));
                for (WalletAccount walletAccount : walletUser.get().getAccount()) {
                    BigDecimal _totalTransactionToday = walletTransactionRepository
                            .totalTransactionAmountToday(walletAccount.getAccountNo(), LocalDate.now());
                    totalTransactionToday = _totalTransactionToday == null ? totalTransactionToday
                            : totalTransactionToday.add(_totalTransactionToday);
                }
            }
            response.put("totalTransactionToday", String.valueOf(totalTransactionToday.doubleValue()));
            log.info("Response ---->> {}", response);
            return new ResponseEntity<>(
                    new SuccessResponse("SUCCESS", response),
                    HttpStatus.OK);
        } catch (CustomException ex) {
            throw new CustomException("error", HttpStatus.EXPECTATION_FAILED);
        }

    }
    @Override
    public ResponseEntity<?> createExternalAccount(String accountNumber) {
        log.info("Creating external account for account number: {}", accountNumber);
        WalletAccount account = walletAccountRepository.findByAccountNo(accountNumber);
        if (account == null) {
            log.error("Wallet Account does not exist");
            return new ResponseEntity<>(new ErrorResponse("Wallet Account does not exist"), HttpStatus.NOT_FOUND);
        }

        Provider provider = switchWalletService.getActiveProvider();
        if (provider == null) {
            log.error("No active provider found");
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.NO_PROVIDER.getValue()), HttpStatus.NOT_FOUND);
        }

        return coreBankingService.externalCBACreateAccount(account.getUser(), account, null);
    }

    public ResponseEntity<?> updateNotificationEmail(String accountNumber, String email) {
        log.info("Updating notification email for account number: {}", accountNumber);
        Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNumber);

        if (account.isEmpty()) {
            log.error("Invalid Account");
            return new ResponseEntity<>(new ErrorResponse("INVALID ACCOUNT"), HttpStatus.BAD_REQUEST);
        }

        ResponseEntity<?> result = securityCheckOwner(accountNumber);
        if (!result.getStatusCode().is2xxSuccessful()) {
            return result;
        }

        account.get().setNotify_email(email);
        walletAccountRepository.save(account.get());

        return new ResponseEntity<>(new SuccessResponse("SUCCESS", null), HttpStatus.OK);
    }

    public ResponseEntity<?> securityCheckOwner(String accountNumber) {
        log.info("Performing security check for account ownership: {}", accountNumber);
        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        MyData userToken = MyData.newInstance(_userToken);
        if (userToken == null) {
            log.error("Invalid Token");
            return new ResponseEntity<>(new ErrorResponse(ResponseCodes.INVALID_TOKEN.getValue()), HttpStatus.BAD_REQUEST);
        }

        AccountSumary account = tempwallet.getAccountSumaryLookUp(accountNumber);
        log.info("Account Summary: {}", account);
        if (account == null) {
            return new ResponseEntity<>(new ErrorResponse(String.format("%s  %s",
                    ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber)), HttpStatus.BAD_REQUEST);
        }

        boolean isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_OWNER"::equalsIgnoreCase);
        isWriteAdmin = userToken.getRoles().stream().anyMatch("ROLE_ADMIN_APP"::equalsIgnoreCase) || isWriteAdmin;
        boolean isOwner = Long.compare(account.getUId(), userToken.getId()) == 0;

        if (!isOwner && !isWriteAdmin) {
            log.error("Owner check failed: {} {}", isOwner, isWriteAdmin);
            return new ResponseEntity<>(new ErrorResponse(String.format("%s %s %s %s",
                    ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accountNumber, isOwner, isWriteAdmin)),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(account, HttpStatus.ACCEPTED);
    }

    @Override
    public void setupExternalCBA() {
        log.info("Setting up external CBA");
        List<WalletAccount> walletAccounts = walletAccountRepository.findByWayaAccount();

        for (WalletAccount wallet : walletAccounts) {
            if ("0".equals(wallet.getNubanAccountNo())) {
                wallet.setNubanAccountNo(Util.generateNuban(financialInstitutionCode,
                        ("O".equalsIgnoreCase(wallet.getAcct_ownership()) ? "ledger" : "savings")));
                walletAccountRepository.save(wallet);
            }
            Provider provider = switchWalletService.getActiveProvider();
            coreBankingService.externalCBACreateAccount(wallet.getUser(), wallet, provider);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<?> setupAccountOnExternalCBA(String accounNumber) {
        log.info("Setting up account on external CBA for account number: {}", accounNumber);
        WalletAccount walletAccount = walletAccountRepository.findByAccountNo(accounNumber);

        if (ObjectUtils.isEmpty(walletAccount)) {
            log.error("Invalid Source Account: {}", accounNumber);
            return new ResponseEntity<>(new ErrorResponse(String.format("%s %s",
                    ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accounNumber)), HttpStatus.BAD_REQUEST);
        }

        WalletUser walletUser = walletAccount.getUser();
        if (ObjectUtils.isEmpty(walletUser)) {
            log.error("Invalid Source Account: {}", accounNumber);
            return new ResponseEntity<>(new ErrorResponse(String.format("%s %s",
                    ResponseCodes.INVALID_SOURCE_ACCOUNT.getValue(), accounNumber)), HttpStatus.BAD_REQUEST);
        }
        log.info(walletUser.getCust_name());
        Provider provider = switchWalletService.getActiveProvider();
        return coreBankingService.externalCBACreateAccount(walletUser, walletAccount, provider);
    }

    @Override
    public ApiResponse<?> getAllAccounts(int page, int size, String filter, LocalDate fromdate, LocalDate todate) {
        log.info("Fetching all accounts with pagination. Page: {}, Size: {}, Filter: {}, From Date: {}, To Date: {}",
                page, size, filter, fromdate, todate);
        Pageable pagable = PageRequest.of(page, size);
        Map<String, Object> response = new HashMap<>();
        Page<WalletAccount> accountsPage = null;

        if (filter != null) {
            // LocalDate fromtranDate, LocalDate totranDate
            accountsPage = walletAccountRepository.findByAllWalletAccountWithDateRangeAndTranTypeOR(pagable,
                    filter, fromdate, todate);
            log.info("Filtered accounts: {}", accountsPage.getContent());
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
            log.error("No record found");
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO RECORD FOUND", null);
        }
        log.info("Accounts fetched successfully");
        return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "ACCOUNT LIST SUCCESSFULLY", response);
    }


    @Override
    public ApiResponse<?> toggleTransactionType(HttpServletRequest request,long userId, String type, String token) {
        log.info("method to toggle Transaction Type request ---->>> {}", userId);
        try {
            TranasctionPropertie toogle = new TranasctionPropertie();
            UserDtoResponse user = authProxy.getUserById(userId, token,request.getHeader(SecurityConstants.CLIENT_ID),request.getHeader(SecurityConstants.CLIENT_TYPE));
            if (!user.isStatus()) {
                log.error("Auth User ID does not exists");
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "Auth User ID does not exists", null);
            }
            TranasctionPropertie getUSer = transactionPropertyRepo.findByUserId(userId);
            if (getUSer != null) {
                getUSer.setBillsPayment(type.contains("Bills") ? !getUSer.isBillsPayment()
                        : getUSer.isBillsPayment());
                getUSer.setDeposit(type.contains("Deposit") ? !getUSer.isDeposit()
                        : getUSer.isDeposit());
                getUSer.setWithdrawal(type.contains("Withdrawal") ? !getUSer.isWithdrawal()
                        : getUSer.isWithdrawal());
                transactionPropertyRepo.save(getUSer);
                return new ApiResponse<>(true, ApiResponse.Code.SUCCESS,
                        "User transaction property toggled off successfully", getUSer);
            }
            toogle.setUserId(userId);
            toogle.setBillsPayment(type.contains("Bills") ? !toogle.isBillsPayment()
                    : toogle.isBillsPayment());
            toogle.setDeposit(type.contains("Deposit") ? !toogle.isDeposit()
                    : toogle.isDeposit());
            toogle.setWithdrawal(type.contains("Withdrawal") ? !toogle.isWithdrawal()
                    : toogle.isWithdrawal());
            transactionPropertyRepo.save(toogle);
            log.info("Response from oggle Transaction Type ---->> {}", toogle);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS,
                    "User transaction property toggled off successfully", toogle);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "Error occurred. Try again", null);
        }
    }

    @Override
    public ApiResponse<?> transTypeStatus(long userId) {
        try {
            log.info("Fetching transaction property for user: {}", userId);
            TranasctionPropertie toogle = new TranasctionPropertie();
            TranasctionPropertie getUSer = transactionPropertyRepo.findByUserId(userId);
            if (getUSer != null) {
                log.info("Transaction property found for user: {}", userId);
                return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "User transaction property", getUSer);
            }
            log.error("No transaction property set for user: {}", userId);
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "No transaction property set for user", getUSer);
        } catch (Exception ex) {
            log.error("Error occurred while fetching transaction property for user: {}", userId, ex);
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "Error occurred. Try again", null);
        }
    }

    @Override
    public ApiResponse<?> totalTransactionByUserId(Long user_id, boolean filter, LocalDate fromdate, LocalDate todate, String profileId) {
        try {
            log.info("Fetching total transactions for user with ID: {}", user_id);
            WalletUser user = walletUserRepository.findByUserIdAndProfileId(user_id, profileId);
            if (user == null) {
                log.error("User ID does not exist: {}", user_id);
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "USER ID DOES NOT EXIST", null);
            }
            List<WalletAccount> accountList = walletAccountRepository.findByUser(user);
            if (accountList.isEmpty()) {
                log.error("No account for user ID: {}", user_id);
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO ACCOUNT FOR USER ID", null);
            }
            Map<String, BigDecimal> response = new HashMap<>();
            BigDecimal totalTrans = BigDecimal.ZERO;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalIncoming = BigDecimal.ZERO;
            BigDecimal totalOutgoing = BigDecimal.ZERO;
            log.info("Account list: {}", accountList);
            for (WalletAccount acct : accountList) {
                BigDecimal revenue = walletTransAccountRepo.totalRevenueAmountByUser(acct.getAccountNo());
                totalRevenue = totalRevenue.add(revenue == null ? BigDecimal.ZERO : revenue);
                // log.info("total customer revenue:: {}", totalrevenue);

                // total outgoing
                BigDecimal outgoing = walletTransRepo.totalWithdrawalByCustomer(acct.getAccountNo());
                totalOutgoing = totalOutgoing.add(outgoing == null ? BigDecimal.ZERO : outgoing);
                // total incoming
                BigDecimal incoming = walletTransRepo.totalDepositByCustomer(acct.getAccountNo());
                totalIncoming = totalIncoming.add(incoming == null ? BigDecimal.ZERO : incoming);
                // total balance
                BigDecimal totalBalance = walletAccountRepository.totalBalanceByUser(acct.getAccountNo());
                totalTrans = totalTrans.add(totalBalance == null ? BigDecimal.ZERO : totalBalance);
            }
            response.put("totalBalance", totalTrans);
            response.put("totalRevenue", totalRevenue);
            response.put("totalDeposit", totalIncoming);
            response.put("totalWithdrawal", totalOutgoing);
            log.info("Total transactions fetched successfully for user with ID: {}", user_id);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "User TRANSACTION", response);
        } catch (Exception ex) {
            log.error("Error occurred while fetching total transactions for user with ID: {}", user_id, ex);
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<?> fetchAllUsersTransactionAnalysis() {
        log.info("Method to fetch users transaction analysis called !!!!");
        try {
            long countTotalAccountUser = walletUserRepository.countByUserIdIsNotNull();
            log.info("countTotalAccountUser ---->>> {}", countTotalAccountUser);
            if (countTotalAccountUser < 1) {
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "No records found!", null);
            }

            int maxPerPage = 1000;
            double result = (double) countTotalAccountUser / (double) maxPerPage;
            int numberOfPage = (int) Math.ceil(result);
            log.info("CountTotalAccountUser {}", countTotalAccountUser);
            log.info("NUMBER OF PAGES {}", numberOfPage);
            log.info("DATA SIZE PER PAGE {}", maxPerPage);
            List<UserAccountStatsDto> userAccountStatsDtoList = new ArrayList<>();
            for (int i = 0; i < numberOfPage; i++) {
                Sort sort = Sort.by(Sort.Direction.DESC, "id");
                Pageable pageable = PageRequest.of(i, maxPerPage, sort);
                List<WalletUser> userAccountList = walletUserRepository.findAllByOrderByIdDesc(pageable);
                log.info("UserAccountList Size {}", userAccountList.size());
                if (userAccountList.size() < 1) {
                    continue;
                }

                for (WalletUser user : userAccountList) {
                    List<WalletAccount> accountList = walletAccountRepository.findByUser(user);
                    if (accountList.size() < 1) {
                        continue;
                    }

                    UserAccountStatsDto userAccountStatsDto = new UserAccountStatsDto();
                    userAccountStatsDto.setUserId(String.valueOf(user.getUserId()));
                    BigDecimal totalTrans = BigDecimal.ZERO;
                    BigDecimal totalRevenue = BigDecimal.ZERO;
                    BigDecimal totalIncoming = BigDecimal.ZERO;
                    BigDecimal totalOutgoing = BigDecimal.ZERO;
                    String acctNumber = "";
                    String acctType = "";
                    for (WalletAccount acct : accountList) {

                        if (acct.isWalletDefault()) {
                            acctNumber = acct.getAccountNo();
                            acctType = acct.getAccountType();
                        }
                        BigDecimal revenue = walletTransAccountRepo.totalRevenueAmountByUser(acct.getAccountNo());
                        totalRevenue = totalRevenue.add(revenue == null ? BigDecimal.ZERO : revenue);
                        // total outgoing
                        BigDecimal outgoing = walletTransRepo.totalWithdrawalByCustomer(acct.getAccountNo());
                        totalOutgoing = totalOutgoing.add(outgoing == null ? BigDecimal.ZERO : outgoing);
                        // total incoming
                        BigDecimal incoming = walletTransRepo.totalDepositByCustomer(acct.getAccountNo());
                        totalIncoming = totalIncoming.add(incoming == null ? BigDecimal.ZERO : incoming);
                        // total balance
                        BigDecimal totalBalance = walletAccountRepository.totalBalanceByUser(acct.getAccountNo());
                        totalTrans = totalTrans.add(totalBalance == null ? BigDecimal.ZERO : totalBalance);

                    }
                    userAccountStatsDto.setTotalIncoming(totalIncoming);
                    userAccountStatsDto.setTotalTrans(totalTrans);
                    userAccountStatsDto.setTotalOutgoing(totalOutgoing);
                    userAccountStatsDto.setTotalRevenue(totalRevenue);
                    userAccountStatsDto.setAccountNumber(acctNumber);
                    userAccountStatsDto.setAccountType(acctType);
                    userAccountStatsDtoList.add(userAccountStatsDto);

                }
            }
            log.info("UserAccountStatsDtoList {}", userAccountStatsDtoList);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "Record fetched....", userAccountStatsDtoList);
        } catch (Exception ex) {
            log.error("Error FetchAllUsersTransactionAnalysis {}", ex.getLocalizedMessage());
            ex.printStackTrace();
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<?> fetchUserTransactionStatForReferral(String user_id, String accountNo, String profileId) {
        log.info("fetch User Transaction Stat For Referral method called ----->>>> {}", accountNo);
        try {
            String profileIdData = null;
            if (profileId != null && !profileId.isEmpty())
                profileIdData = profileId;

            WalletUser user = walletUserRepository.findByUserIdAndProfileId(Long.valueOf(user_id), profileIdData);
            if (user == null) {
                List<WalletUser> walletUserList = walletUserRepository.findAllByUserId(Long.valueOf(user_id));
                if (walletUserList.size() == 1) {
                    user = walletUserList.get(0);
                } else {
                    log.error("USER ID DOES NOT EXIST");
                    return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "USER ID DOES NOT EXIST", null);
                }
            }
            List<WalletAccount> accountList = walletAccountRepository.findByUser(user);
            if (accountList.isEmpty()) {
                log.error("NO ACCOUNT FOR USER ID");
                return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, "NO ACCOUNT FOR USER ID", null);
            }
            BigDecimal totalTrans = BigDecimal.ZERO;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalIncoming = BigDecimal.ZERO;
            BigDecimal totalOutgoing = BigDecimal.ZERO;
            BigDecimal overAllWithdrawal = BigDecimal.ZERO;
            BigDecimal overAllTransfer = BigDecimal.ZERO;

            BigDecimal totalAirTimeTopUp = BigDecimal.ZERO;
            BigDecimal totalBetting = BigDecimal.ZERO;
            BigDecimal totalUtility = BigDecimal.ZERO;
            BigDecimal totalDataTopUp = BigDecimal.ZERO;
            BigDecimal totalCableCount = BigDecimal.ZERO;

            BigDecimal posTrans = BigDecimal.ZERO;
            BigDecimal posTransCommission = BigDecimal.ZERO;

            BigDecimal webTrans = BigDecimal.ZERO;
            BigDecimal webTransCommission = BigDecimal.ZERO;

            BigDecimal webTotalCount = BigDecimal.ZERO;
            BigDecimal posTotalCount = BigDecimal.ZERO;

            for (WalletAccount acct : accountList) {

                BigDecimal airTimeTopUp = BigDecimal.valueOf(walletTransactionRepository
                        .countByAcctNumAndTranCategory(accountNo, CategoryType.AIRTIME_TOPUP));
                totalAirTimeTopUp = totalAirTimeTopUp.add(airTimeTopUp == null ? BigDecimal.ZERO : airTimeTopUp);

                BigDecimal betting = BigDecimal.valueOf(
                        walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.BETTING));
                totalBetting = totalBetting.add(betting == null ? BigDecimal.ZERO : betting);

                BigDecimal utility = BigDecimal.valueOf(
                        walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.UTILITY));
                totalUtility = totalUtility.add(utility == null ? BigDecimal.ZERO : utility);

                BigDecimal dataTopUp = BigDecimal.valueOf(
                        walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.DATA_TOPUP));
                totalDataTopUp = totalDataTopUp.add(dataTopUp == null ? BigDecimal.ZERO : dataTopUp);

                BigDecimal cableCount = BigDecimal.valueOf(
                        walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.CABLE));
                totalCableCount = totalCableCount.add(cableCount == null ? BigDecimal.ZERO : cableCount);

                // over all credit/transfer
                BigDecimal overTransfer = BigDecimal.valueOf(walletTransactionRepository
                        .countByAcctNumAndTranCategory(acct.getAccountNo(), CategoryType.TRANSFER));
                overAllTransfer = overAllTransfer.add(overTransfer == null ? BigDecimal.ZERO : overTransfer);
                // over all debit/withdrawal
                BigDecimal overAllWith = BigDecimal.valueOf(walletTransactionRepository
                        .countByAcctNumAndTranCategory(acct.getAccountNo(), CategoryType.WITHDRAW));
                overAllWithdrawal = overAllWithdrawal.add(overAllWith == null ? BigDecimal.ZERO : overAllWith);
                // total revenue
                BigDecimal revenue = walletTransAccountRepo.totalRevenueAmountByUser(acct.getAccountNo());
                totalRevenue = totalRevenue.add(revenue == null ? BigDecimal.ZERO : revenue);
                // total Withdrawal
                BigDecimal outgoing = walletTransRepo.totalWithdrawalByCustomer(acct.getAccountNo());
                totalOutgoing = totalOutgoing.add(outgoing == null ? BigDecimal.ZERO : outgoing);
                // total transfer
                BigDecimal incoming = walletTransRepo.totalDepositByCustomer(acct.getAccountNo());
                totalIncoming = totalIncoming.add(incoming == null ? BigDecimal.ZERO : incoming);
                // total balance
                BigDecimal totalBalance = walletAccountRepository.totalBalanceByUser(acct.getAccountNo());
                totalTrans = totalTrans.add(totalBalance == null ? BigDecimal.ZERO : totalBalance);

                // POS , WEB Collection amount, fee
                BigDecimal posAmountSum = walletTransRepo.posOrWebCollectionAmountSum(acct.getAccountNo(), TransactionChannel.POS_TERMINAL.name());
                posTrans = posTrans.add(posAmountSum == null ? BigDecimal.ZERO : posAmountSum);

                BigDecimal webAmountSum = walletTransRepo.posOrWebCollectionAmountSum(acct.getAccountNo(), TransactionChannel.WEB_TERMINAL.name());
                webTrans = webTrans.add(webAmountSum == null ? BigDecimal.ZERO : webAmountSum);

                BigDecimal webCount = BigDecimal.valueOf(walletTransRepo.countByAcctNumAndTransChannel(acct.getAccountNo(), TransactionChannel.WEB_TERMINAL.name()));
                webTotalCount = webTotalCount.add(webCount == null ? BigDecimal.ZERO : webCount);
                BigDecimal posCount = BigDecimal.valueOf(walletTransRepo.countByAcctNumAndTransChannel(acct.getAccountNo(), TransactionChannel.POS_TERMINAL.name()));
                posTotalCount = posTotalCount.add(posCount == null ? BigDecimal.ZERO : posCount);
            }
            BigDecimal singleAccountWithdrawal = BigDecimal.valueOf(
                    walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.WITHDRAW));
            BigDecimal singleAccountTransfer = BigDecimal.valueOf(
                    walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.TRANSFER));
            BigDecimal singleAirTimeTopUp = BigDecimal.valueOf(
                    walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.AIRTIME_TOPUP));
            BigDecimal singleBetting = BigDecimal.valueOf(
                    walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.BETTING));
            BigDecimal singleUtility = BigDecimal.valueOf(
                    walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.UTILITY));
            BigDecimal singleDataTopUp = BigDecimal.valueOf(
                    walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.DATA_TOPUP));
            BigDecimal singleCableCount = BigDecimal
                    .valueOf(walletTransactionRepository.countByAcctNumAndTranCategory(accountNo, CategoryType.CABLE));

            UserTransactionStatsDataDto transactionStats = new UserTransactionStatsDataDto();
            transactionStats.setTotalBalance(totalTrans);
            transactionStats.setTotalRevenue(totalRevenue);
            transactionStats.setTotalDeposit(totalIncoming);
            transactionStats.setTotalWithdrawal(totalOutgoing);
            transactionStats.setOverAllWithdrawalCount(overAllWithdrawal);
            transactionStats.setOverAllTransferCount(overAllTransfer);
            transactionStats.setSingleAccountTransferCount(singleAccountTransfer);
            transactionStats.setSingleAccountWithdrawalCount(singleAccountWithdrawal);
            transactionStats.setSingleAirTimeTopUpCount(singleAirTimeTopUp);
            transactionStats.setSingleDataTopUpCount(singleDataTopUp);
            transactionStats.setSingleCableCount(singleCableCount);
            transactionStats.setSingleBettingCount(singleBetting);
            transactionStats.setSingleUtilityCount(singleUtility);
            transactionStats.setTotalAirTimeTopUpCount(totalAirTimeTopUp);
            transactionStats.setTotalDataTopUpCount(totalDataTopUp);
            transactionStats.setTotalCableCount(totalCableCount);
            transactionStats.setTotalBettingCount(totalBetting);
            transactionStats.setTotalUtilityCount(totalUtility);
            transactionStats.setTotalPosAmount(posTrans);
            transactionStats.setTotalPosCommission(posTransCommission);
            transactionStats.setTotalWebAmount(webTrans);
            transactionStats.setTotalWebCommission(webTransCommission);
            transactionStats.setTotalPosCount(posTotalCount);
            transactionStats.setTotalWebCount(webTotalCount);

            log.info("Transaction status ----->> {}", transactionStats);
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "USER TRANSACTION STATS FETCHED....",
                    transactionStats);
        } catch (Exception ex) {
            log.error("Exception: {}", ex.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.BAD_REQUEST, ex.getMessage(), null);
        }
    }


    @Override
    public ResponseEntity<?> updateAccountDescription(String accountNo, String token, String description) {
        log.info("Update acct name description for acct no ---->> {}", accountNo);
        try {

            Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNo);
            if (!account.isPresent()) {
                return new ResponseEntity<>(new ErrorResponse("Unable to fetch account"), HttpStatus.BAD_REQUEST);
            }

            WalletAccount update = account.get();
            update.setDescription(description);
            walletAccountRepository.save(update);
            log.info("Updated successfully!!!");
            return new ResponseEntity<>(new SuccessResponse("SUCCESS", update), HttpStatus.OK);

        } catch (Exception e) {
            log.error("Exception::{}", e.getMessage());
            return new ResponseEntity<>("An Error occured. Try again", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Override
    public ApiResponse<?> updateAccountName(String accountNo, String token, String name) {
        log.info("Update acct name request for acct no ---->> {}", accountNo);
        try {

            Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNo);
            if (account.isEmpty()) {
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unable to fetch account", null);
            }
            WalletAccount update = account.get();
            update.setAcct_name(name);
            walletAccountRepository.save(update);
            log.info("Updated successfully!!!");
            return new ApiResponse<>(false, ApiResponse.Code.SUCCESS, "SUCCESS", update);
        } catch (Exception e) {
            log.error("Exception::{}", e.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "An Error occured. Try again", null);
        }
    }

    @Override
    public ApiResponse<?> createCommisionAccount(long userId, String token,String profileId) {
        log.info("Method request to create Commission Account ---->>> {}", profileId);
        try {

            String nubanAccountNumber = Util.generateNuban(financialInstitutionCode, "");

            String acctNo = null;
            Integer rand = reqUtil.getAccountNo();
            String hashed_no = reqUtil.WayaEncrypt(
                    userId + "|" + acctNo + "|" + wayaProductCommission + "|"
                            + "NGN");

            // check if user is corporate
            WalletUser walletUser = walletUserRepository.findByUserIdAndProfileId(userId,profileId);

            if (walletUser.isCorporate()) {
                String commisionName = "COMMISSION ACCOUNT";
                Optional<WalletAccount> acct = walletAccountRepository.findFirstByProduct_codeAndUserAndAcct_nameLike(wayaProductCommission, walletUser);
                if (!acct.isPresent()) {
                    WalletProductCode code = walletProductCodeRepository.findByProductGLCode(wayaProductCommission, wayaCommGLCode);
                    WalletProduct product = walletProductRepository.findByProductCode(wayaProductCommission, wayaCommGLCode);
                    if (!walletUser.getCust_sex().equals("S")) {
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
                    log.info("Comission Account::{}", acctNo);
                    String acct_name = walletUser.getCust_name() + " " + "COMMISSION ACCOUNT";
                    WalletAccount caccount = new WalletAccount();
                    if ((product.getProduct_type().equals("SBA") || product.getProduct_type().equals("CAA")
                            || product.getProduct_type().equals("ODA"))) {
                        caccount = new WalletAccount("0000", "", acctNo, nubanAccountNumber, acct_name, walletUser,
                                code.getGlSubHeadCode(),
                                wayaProductCommission, "C", hashed_no, product.isInt_paid_flg(),
                                product.isInt_coll_flg(), "WAYADMIN", LocalDate.now(), product.getCrncy_code(),
                                product.getProduct_type(), product.isChq_book_flg(), product.getCash_dr_limit(),
                                product.getXfer_dr_limit(), product.getCash_cr_limit(), product.getXfer_cr_limit(),
                                false, "", "COMMISSION ACCOUNT");
                        log.info("Wallet commission account: {}", caccount);
                    } else {
                        log.error("Commission account not created");
                    }

                    coreBankingService.createAccount(walletUser, caccount);
                    log.info("Commission account created: {}", caccount.getAccountNo());
                    return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "Successful", caccount);

                }
                log.error("Commission account exist for user");
                return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "Commission account exist for user", null);

            }
            log.error("Not a corporate user");
            return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Not a corporate user", null);

        } catch (Exception e) {
            log.error("Exception:: {}", e.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "An Error occured. Try again", null);
        }
    }

    private void updateExistingWalletUser(Long userId, String profileId,boolean corporate){
        log.info("Update existing wallet user request ---->> {}", userId);
        try {
            List<WalletUser> walletUserList = walletUserRepository.findAllByUserIdAndProfileIdIsNull(userId);
            if(walletUserList.size() > 0 && walletUserList.size() == 1){
                for (WalletUser user: walletUserList){
                    if(user.getProfileId() == null || user.getProfileId().isEmpty()){
                        user.setProfileId(profileId);
                        if(corporate){
                            user.setAccountType("BUSINESS");
                        }else {
                            user.setAccountType("PERSONAL");
                        }
                        walletUserRepository.save(user);
                        log.info("::DEFAULT WALLET USER SUCCESSFULLY UPDATED {}",userId);
                    }
                }
            }
        }catch (Exception ex){
            log.error("::Error updateExistingWalletUser {}",ex.getLocalizedMessage());
            ex.printStackTrace();
            return;
        }
    }

    private void pushAcctClosureOrBlockToInWardService(WalletAccount account,boolean closure){
        log.info("push Acct Closure Or Block To InWard Service method request ---->>> {}", account);
        try {
            InWardDataDto dataDto = new InWardDataDto();
            dataDto.setAccountName(account.getAcct_name());
            dataDto.setNubanAccountNumber(account.getNubanAccountNo());
            dataDto.setAccountNumber(account.getAccountNo());
            dataDto.setDeleteFlag(account.isDel_flg());
            dataDto.setWalletDefault(account.isWalletDefault());
            dataDto.setCloseFlag(closure);
            if(account.getUser() != null){
                dataDto.setUserId(account.getUser().getUserId());
                dataDto.setWalletUserId(account.getUser().getId());
                dataDto.setEmailAddress(account.getUser().getEmailAddress());
                dataDto.setMobileNo(account.getUser().getMobileNo());
            }
            dataDto.setWalletAccountId(account.getId());
            if(account.getAccountType() != null)
                dataDto.setAccountType(account.getAccountType());

            log.info("::ABOUT TO PROCESS ACCT ACTION OF TYPE CLOSURE/BLOCK/DELETE {}",dataDto);
            CompletableFuture.runAsync(() -> {
                messageQueueProducer.send(Constant.IN_WARD_SERVICE, dataDto);
                log.info("::::ACCT UPDATE DTO SUCCESSFULLY PUBLISHED TO KAFKA MESSAGE QUEUE {}",dataDto);
            });
        }catch (Exception ex){
            log.error("::Error pushAcctClosureOrBlockToInWardService {}",ex.getLocalizedMessage());
            log.error("::FAIL TO PUBLISH DATA TO KAFKA MESSAGE");
        }
    }

    @Override
    public ApiResponse<?> updateBulkAmount(String accountNumber, String token, double limit) {
        log.info("Update debit limit ---->>> {}", accountNumber);
        try {
            Optional<WalletAccount> account = walletAccountRepository.findByAccount(accountNumber);
            if (account.isEmpty()) {
                log.error("Unable to fetch account");
                return new ApiResponse<>(false, ApiResponse.Code.NOT_FOUND, "Unable to fetch account", null);
            }

            WalletAccount update = account.get();
            update.setBlockAmount(BigDecimal.valueOf(limit));
            walletAccountRepository.save(update);
            log.info("Block amount set successfully");
            return new ApiResponse<>(true, ApiResponse.Code.SUCCESS, "SUCCESS", update);
        } catch (Exception e) {
            log.error("Exception::{}", e.getMessage());
            return new ApiResponse<>(false, ApiResponse.Code.UNKNOWN_ERROR, "An Error occurred. Try again", null);
        }
    }

    public WalletUser findUserWalletByEmailAndPhone(String email) {
        // Logging wallet retrieval by ID
        log.info("Retrieving wallet by Email...");
        Optional<WalletUser> wallet = walletUserRepository.findByEmailOrPhoneNumberOrUserId(email);
        if (wallet.isPresent()) {
            return wallet.get();
        }
        return new WalletUser();
    }

    @Override
    public WalletUser findById(Long id) {
        return walletUserRepository.findById(id).orElse(new WalletUser());
    }


    public WalletAccount findUserAccount(String accountNo){
        //findByAccount
        Optional<WalletAccount> wallet = walletAccountRepository.findByAccount(accountNo);
        if (wallet.isPresent()) {
            return wallet.get();
        }
        return new WalletAccount();
    }
}