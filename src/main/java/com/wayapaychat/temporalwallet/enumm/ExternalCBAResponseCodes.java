
package com.wayapaychat.temporalwallet.enumm;

import lombok.Getter;

/**
 *
 * @author dynamo
 */
public enum ExternalCBAResponseCodes {

    R_00("00", "Success", "yes", GlobalProperties.SUCCESS),
    R_01("01", "Authentication failed", "yes", GlobalProperties.FAILED),
    R_02("02", "Fineract related error, Failed to update account, identity mismatch", "yes", GlobalProperties.FAILED),
    R_03("03", "Fineract related error, Failed to update account, account failed to update", "yes", GlobalProperties.FAILED),
    R_04("04", "Fineract related error, account updated but approval failedt,", "yes", GlobalProperties.FAILED),
    R_05("05", "Fineract related error, account updated but activation failedt,", "yes", GlobalProperties.FAILED),
    R_91("91", "Invalid Arguments passed, review the folowing: ", "yes", GlobalProperties.FAILED),
    R_96("96", "Invalid Request", "Time stamp mismacth", GlobalProperties.FAILED),
    R_92("92", "No Signature Present", "yes", GlobalProperties.FAILED),
    R_99("99", "Failed", "yes", GlobalProperties.FAILED),
    R_94("94", "Failed, Error Creating User, user must either be an admin, app-user or fi-admin", "yes", GlobalProperties.FAILED),
    R_95("95", "Failed, Email Address cannot be empty", "yes", GlobalProperties.FAILED),
    R_97("97", "Empty Result", "yes", GlobalProperties.FAILED),
    R_93("93", "Wrong reference or data key value or empty result", "yes", GlobalProperties.FAILED),
    R_98("98", "No Account Number Found", "yes", GlobalProperties.FAILED),
    R_66("66", "Error Saving, make sure you are passing in unique values", "yes", GlobalProperties.FAILED),
    R_67("67", "Error during tansaction operation, constraint on object has being identified ", "yes", GlobalProperties.FAILED),
    R_33("33", "Request Error, validate one or more of the request fields passed", "yes", GlobalProperties.FAILED),
    R_34("34", "Error retrieving Data", "yes", GlobalProperties.FAILED),
    R_22("22", "Invalid Account Number passed", "yes", GlobalProperties.FAILED),
    R_23("23", "Invalid OTP OR OTP Expired", "yes", GlobalProperties.FAILED),
    R_25("25", "Unable to block amount", "yes", GlobalProperties.FAILED),
    R_26("26", "Unable to unblock amount, unblock amount mismatch ", "yes", GlobalProperties.FAILED),
    R_27("27", "Unable to unblock amount", "yes", GlobalProperties.FAILED),
    R_24("24", "Unable to block account", "yes", GlobalProperties.FAILED),
    R_44("44", "Transaction not found", "yes", GlobalProperties.FAILED),
    R_11("11", "Transaction Failed", "yes", GlobalProperties.SUCCESS),
    R_15("15", "End date can not be before start date", "yes", GlobalProperties.SUCCESS),
    R_16("16", "Transaction has not been procced", "yes", GlobalProperties.SUCCESS),
    R_17("17", "Transaction is pending", "yes", GlobalProperties.SUCCESS),
    R_18("18", "Invalid Retrival Pin", "yes", GlobalProperties.SUCCESS),
    R_19("19", "Invalid Redemtion Amount", "yes", GlobalProperties.SUCCESS),
    R_12("12", "Wallet Creation Failed, Account Number and Phone Number Mismatch", "yes", GlobalProperties.SUCCESS),
    R_77("77", "Access Unauthorized", "yes", GlobalProperties.SUCCESS),
    R_78("78", "Debit Card Failed", "yes", GlobalProperties.SUCCESS),
    R_76("76", "Tokenization Declined", "yes", GlobalProperties.SUCCESS),
    R_79("79", "Insufficient Funds Or Problem debiting from account contact administrator", "yes", GlobalProperties.SUCCESS),
    R_101("101", "Email does not exist", "yes", GlobalProperties.FAILED),
    R_103("103", "Could not complete payment process", "yes", GlobalProperties.FAILED),
    R_104("104", "Name Enquiry was not successful, Account not found", "yes", GlobalProperties.FAILED),
    R_105("105", "Could not complete process, You can not disable role of higher precedence", "yes", GlobalProperties.FAILED),
    R_55("55", "Duplicate Payment Request", "yes", GlobalProperties.FAILED),
    R_57("57", "Incorrect Parameters Sent", "yes", GlobalProperties.FAILED),
    R_54("54", "Old Password can not be the same as current password", "yes", GlobalProperties.FAILED),
    R_53("53", "Error, Creating account at Core", "yes", GlobalProperties.FAILED),
    R_56("56", "Duplicate Data Request", "yes", GlobalProperties.FAILED),
    R_87("87", "Access Denied, Invalid Upload Format", "yes", GlobalProperties.FAILED),
    R_88("88", "Access Denied", "yes", GlobalProperties.FAILED),
    R_89("89", "Beneficiary phone and email can not be empty", "yes", GlobalProperties.FAILED),
    R_85("85", "Sender phone and email can not be empty", "yes", GlobalProperties.FAILED),
    R_82("82", "Beneficiary email can not be empty and transport type is email", "yes", GlobalProperties.FAILED),
    R_84("84", "Beneficiary phone number can not be empty and transport type is sms", "yes", GlobalProperties.FAILED),
    R_72("72", "Sender email can not be empty and transport type is email", "yes", GlobalProperties.FAILED),
    R_74("74", "No Response From Core", "yes", GlobalProperties.FAILED),
    R_86("86", "Error sending OTP", "yes", GlobalProperties.FAILED),
    R_83("83", "User Account is Suspended or has been Deleted", "yes", GlobalProperties.FAILED),
    R_81("81", "Could not complete process, You can not disable user from another institution", "yes", GlobalProperties.FAILED),
    R_100("100", "Due to a high Number of requests we can't create a group for you right now, we will let you know once we are able to meet your request", "yes", GlobalProperties.SUCCESS),
    R_106("106", "User already have a wallet", "yes", GlobalProperties.FAILED);

    @Getter
    private final String respCode;

    @Getter
    private final String respDescription;

    private final String definite;

    private final String tranStatus;

    private ExternalCBAResponseCodes(String respCode, String respDescription, String definite, String tranStatus) {

        this.respCode = respCode;
        this.respDescription = respDescription;
        this.definite = definite;
        this.tranStatus = tranStatus;

    }
}
