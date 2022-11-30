package com.wayapaychat.temporalwallet.util;

import java.math.BigDecimal;

public class MessageHelper {

    public static String formatDebitMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
                                     String narration) {

        String message = "" + "\n";
        message = message + "" + "Message :" + "A debit transaction has occurred"
                + "  on your account see details below" + "\n";
        message = message + "" + "Amount :" + amount + "\n";
        message = message + "" + "tranId :" + tranId + "\n";
        message = message + "" + "tranDate :" + tranDate + "\n";
        message = message + "" + "Currency :" + tranCrncy + "\n";
        message = message + "" + "Narration :" + narration + "\n";
        return message;
    }

    public static String formatNewMessage(BigDecimal amount, String tranId, String tranDate, String tranCrncy,
                                   String narration) {

        String message = "" + "\n";
        message = message + "" + "Message :" + "A credit transaction has occurred"
                + "  on your account see details below" + "\n";
        message = message + "" + "Amount :" + amount + "\n";
        message = message + "" + "tranId :" + tranId + "\n";
        message = message + "" + "tranDate :" + tranDate + "\n";
        message = message + "" + "Currency :" + tranCrncy + "\n";
        message = message + "" + "Narration :" + narration + "\n";
        return message;
    }
}
