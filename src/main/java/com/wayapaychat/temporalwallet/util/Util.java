package com.wayapaychat.temporalwallet.util;

import java.util.Random;

public class Util {
    public static String generateWayaVirtualAccount(){
        Random rand = new Random();
        int num = rand.nextInt(9000) + 1000;
        return Constant.WAYABANK_PREFIX.concat(String.valueOf(num));
    }
}
