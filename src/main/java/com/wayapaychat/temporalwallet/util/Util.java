package com.wayapaychat.temporalwallet.util;

import com.waya.security.auth.pojo.UserIdentityData;
import com.wayapaychat.temporalwallet.SpringApplicationContext;
import com.wayapaychat.temporalwallet.dto.ServiceResponse;
import com.wayapaychat.temporalwallet.entity.WalletUser;
import com.wayapaychat.temporalwallet.exception.CustomException;
import com.wayapaychat.temporalwallet.notification.EmailEvent;
import com.wayapaychat.temporalwallet.notification.EmailPayload;
import com.wayapaychat.temporalwallet.notification.EmailRecipient;
import com.wayapaychat.temporalwallet.pojo.MyData;
import com.wayapaychat.temporalwallet.proxy.NotificationProxy;
import com.wayapaychat.temporalwallet.repository.WalletUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;

@Slf4j
@Component
public class Util {

    private final NotificationProxy notificationProxy;

    private final String SECRET_KEY = "PEAL33550099GOEScatriiendKETTLE001UNITED";
    private final String SALT = "stdsxcitymanjoehhhhh!!!!waya";

    public Util(NotificationProxy notificationProxy) {
        this.notificationProxy = notificationProxy;
    }

    public static String generateWayaVirtualAccount() {
        Random rand = new Random();
        int num = rand.nextInt(9000000) + 1000;
        return Constant.WAYABANK_PREFIX.concat(String.valueOf(num));
    }

    public static String generateNuban(String financialInstitutionCode, String accountType) {
        String nuban = "";
        try {

            // It will generate 8 digit random Number from 0 to 99999999 And format as
            String nineDigits;
            // String prefixType;

            // It will generate 8 digit random Number from 0 to 99999999 And format as
            // String
            SecureRandom rnd = new SecureRandom();
            switch (accountType) {
                case "ledger":
                    accountType = "9999999";
                    nineDigits = accountType + String.format("%02d", rnd.nextInt(99));
                    break;
                case "fixed":
                    accountType = "999";
                    nineDigits = accountType + String.format("%04d", rnd.nextInt(9999)) + "99";
                    break;
                case "loan":
                    accountType = "9";
                    nineDigits = accountType + String.format("%02d", rnd.nextInt(99)) + "999999";
                    break;
                case "current 2":
                    accountType = "8";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                case "current 1":
                    accountType = "7";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                case "savings 6":
                    accountType = "6";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                case "savings 5":
                    accountType = "5";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                case "savings 4":
                    accountType = "4";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                case "savings 3":
                    accountType = "3";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                case "savings 2":
                    accountType = "2";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                case "savings 1":
                    accountType = "1";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
                default:
                    accountType = "0";
                    nineDigits = accountType + String.format("%08d", rnd.nextInt(99999999));
                    break;
            }
            // Concatenate Financial Institution Code with 9 digits generated
            String accountString = financialInstitutionCode + nineDigits;

            // Apply the NUBAN algorithm to get check digits
            int[] numbers = new int[accountString.length()];

            for (int i = 0; i < accountString.length(); i++) {
                numbers[i] = accountString.charAt(i) - '0';
            }

            int checkDigitSum = numbers[0] * 3 + numbers[1] * 7 + numbers[2] * 3 + numbers[3] * 3 + numbers[4] * 7
                    + numbers[5] * 3 + numbers[6] * 3 + numbers[7] * 7 + numbers[8] * 3 + numbers[9] * 3
                    + numbers[10] * 7 + numbers[11] * 3 + numbers[12] * 3 + numbers[13] * 7 + numbers[14] * 3;

            String checkDigit;
            if (checkDigitSum % 10 == 0) {
                checkDigit = String.valueOf(0);
            } else {
                checkDigit = String.valueOf(10 - (checkDigitSum % 10));
            }

            nuban = nineDigits + checkDigit;

            System.out.println("Nuban Account = " + nuban);

        } catch (Exception e) {
            throw new CustomException("Inside create nuban account" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
        return nuban;

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

    public static String WayaEncrypt(String pText) throws Exception {
        String authHash = Base64Utils.encodeToString(pText.getBytes(StandardCharsets.UTF_8));
        return authHash;
    }

    public static String WayaDecrypt(String encryptText) throws Exception {
        byte[] asBytes = Base64Utils.decodeFromString(encryptText);
        String output = new String(asBytes);
        return output;
    }

    public String encrypt(String strToEncrypt) {
        try {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public String decrypt(String strToDecrypt) {
        try {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static BigDecimal computePercentage(BigDecimal amount, BigDecimal percentageValue) {
        BigDecimal per = BigDecimal.valueOf(percentageValue.doubleValue() / 100);
        return BigDecimal.valueOf(per.doubleValue() * amount.doubleValue());
    }

    public static ArrayList<Map<String, String>> products() {

        ArrayList<Map<String, String>> list = new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("Virtual Account Issuance", "VIRTUALACCOUNTISS");
        map.put("Funding via card", "PAYSTACK");
        map.put("Funding via bank account", "PAYSTK");
        map.put("Funding via Bank Transfer", "PAYSTK");
        map.put("Internal Bank Transfer", "WAYATRAN");
        map.put("External Bank Transfer", "BANKPMT");
        map.put("Sms Alert", "SMSCHG");
        map.put("Bills Payment", "AITCOL");

        list.add(map);
        return list;
    }

    public static WalletUser checkOwner() {

        WalletUserRepository walletUserRepo = ((WalletUserRepository) SpringApplicationContext
                .getBean("walletUserRepository"));

        UserIdentityData _userToken = (UserIdentityData) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        MyData jwtUser = MyData.newInstance(_userToken);

        WalletUser user = walletUserRepo.findByEmailAddress(jwtUser.getEmail());

        return user;

    }

    public void pushEMAIL(String subject, String token, String name, String email, String message, Long userId) {

        log.info("INSIDEE EMAIL {}", userId);
        EmailEvent emailEvent = new EmailEvent();

        emailEvent.setEventType(Constant.EMAIL);
        emailEvent.setEventCategory("WELCOME");
        emailEvent.setProductType(Constant.PRODUCT);
        emailEvent.setSubject(subject);
        EmailPayload data = new EmailPayload();

        data.setMessage(message);

        EmailRecipient emailRecipient = new EmailRecipient();
        emailRecipient.setFullName(name);
        emailRecipient.setEmail(email);

        List<EmailRecipient> addUserId = new ArrayList<>();
        addUserId.add(emailRecipient);
        data.setNames(addUserId);

        emailEvent.setData(data);
        emailEvent.setInitiator(userId.toString());
        log.debug("REQUEST EMAIL WAYABANK: " + emailEvent.toString());

        try {
            sendEmailNotification(emailEvent, token);
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    public void sendEmailNotification(EmailEvent emailEvent, String token) {
        try {
            ServiceResponse responseEntity = notificationProxy.sendEmail(emailEvent, token);
            log.info(" email sent status :: " + responseEntity);
        } catch (Exception ex) {
            log.error("Unable to generate transaction Id", ex);
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }
}
