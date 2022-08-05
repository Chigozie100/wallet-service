package com.wayapaychat.temporalwallet.util;

import com.wayapaychat.temporalwallet.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.util.Random;

public class Util {
    public static String generateWayaVirtualAccount(){
        Random rand = new Random();
        int num = rand.nextInt(9000000) + 1000;
        return Constant.WAYABANK_PREFIX.concat(String.valueOf(num));
    }



    public static String generateNuban(String financialInstitutionCode, String accountType){
        String nuban = "";
        try {

            // String currentPrefix = "2" String loanPrefix = "7" String fixedDeposit = "8"
            // String officePrefix = "9";

            // Defines our Financial Institution Code
//            String financialInstitutionCode = "901037";

//            String accountType = "savings";

            String nineDigits;

            // It will generate 8 digit random Number from 0 to 99999999 And format as
            // String
            Random rnd = new Random();
            switch (accountType) {
                case "fixed":
                    accountType = "9999999";
                    nineDigits = accountType + String.format("%02d", rnd.nextInt(99));
                    break;
                case "ledger":
                    accountType = "999";
                    nineDigits = accountType + String.format("%04d", rnd.nextInt(9999)) + "99";
                    break;
                case "loan":
                    accountType = "9";
                    nineDigits = accountType + String.format("%02d", rnd.nextInt(99)) +"999999";
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
                default:
                    accountType = "1";
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

            String  checkDigit;
            if(checkDigitSum % 10 == 0) {
                checkDigit = String.valueOf(0);
            } else {
                checkDigit = String.valueOf(10 - (checkDigitSum % 10));
            }

            nuban = nineDigits + checkDigit;

            System.out.println("Nuban Account = " + nuban);

        } catch (Exception e) {
            throw new CustomException("Inside create nuban account" + e.getMessage(),HttpStatus.EXPECTATION_FAILED);
        }
        return nuban;

    }
}
