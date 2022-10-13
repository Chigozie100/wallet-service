package com.wayapaychat.temporalwallet.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillsUtil {
    
    private String biller;
    private String category;




    public List<BillsUtil> getAirtimeList() {
        List<BillsUtil> data = new ArrayList<>();
        data.add(new BillsUtil("mtn", "AIRTIME"));
        data.add(new BillsUtil("glo", "AIRTIME"));
        data.add(new BillsUtil("airtel", "AIRTIME"));
        data.add(new BillsUtil("9mobile", "AIRTIME"));
        return data;
    }

    public List<BillsUtil> getDataList() {
        List<BillsUtil> data = new ArrayList<>();
        data.add(new BillsUtil("mtn", "DATA"));
        data.add(new BillsUtil("glo", "DATA"));
        data.add(new BillsUtil("airtel", "DATA"));
        data.add(new BillsUtil("9mobile", "DATA"));
        return data;
    }

    public List<BillsUtil> getCableTV() {
        List<BillsUtil> data = new ArrayList<>();
        data.add(new BillsUtil("startimes", "CABLE"));
        data.add(new BillsUtil("dstv", "CABLE"));
        data.add(new BillsUtil("gotv", "CABLE"));
        return data;
    }

    public List<BillsUtil> getUtility() {
        List<BillsUtil> data = new ArrayList<>();

        data.add(new BillsUtil("AEDC POSTPAID", "Utility"));
        data.add(new BillsUtil("Benin Electricity Distribution Company Postpaid", "Utility"));
        data.add(new BillsUtil("Benin Electricity Distribution Company Prepaid", "Utility"));
        data.add(new BillsUtil("Bonny Utility", "Utility"));
        data.add(new BillsUtil("Cofred Online", "Utility"));
        data.add(new BillsUtil("Cross River Water Board", "Utility"));
        data.add(new BillsUtil("Disco Eko Electricity Prepaid", "Utility"));
        data.add(new BillsUtil("EDO STATE MOTOR VEHICLE ADMINISTRATION", "Utility"));
        data.add(new BillsUtil("Edo Waste Management", "Utility"));
        data.add(new BillsUtil("Eko Electricity Distribution Company Plc Postpaid", "Utility"));
        data.add(new BillsUtil("Eko Electricity Distribution Company Plc Prepaid", "Utility"));
        data.add(new BillsUtil("Enugu Electricity Distribution Company Postpaid", "Utility"));
        data.add(new BillsUtil("Enugu Electricity Distribution Company Prepaid", "Utility"));
        data.add(new BillsUtil("Enugu State Water Co", "Utility"));
        data.add(new BillsUtil("ETICKET APP", "Utility"));
        data.add(new BillsUtil("FCT Water Board", "Utility"));
        data.add(new BillsUtil("Gardner Integrated Services", "Utility"));
        data.add(new BillsUtil("Ibadan Electricity Distribution Company Postpaid", "Utility"));
        data.add(new BillsUtil("Utility - Ibadan Electricity Distribution Company Prepaid", "Utility"));
        data.add(new BillsUtil("IBEDC MAP", "Utility"));
        data.add(new BillsUtil("ICE Commercial Power", "Utility"));
        data.add(new BillsUtil("Ikeja Electric (Postpaid)", "Utility"));
        data.add(new BillsUtil("Ikeja Electric Non-Energy Payments", "Utility"));
        data.add(new BillsUtil("Ikeja Electric Prepaid", "Utility"));
        data.add(new BillsUtil("Jos Electricity Distribution Company Postpaid", "Utility"));
        data.add(new BillsUtil("Jos Electricity Distribution Company Prepaid", "Utility"));
        data.add(new BillsUtil("Kaduna Electricity Distribution Company Postpaid", "Utility"));
        data.add(new BillsUtil("Kaduna Electricity Distribution Company Prepaid", "Utility"));
        data.add(new BillsUtil("KEDCO PostPaid", "Utility"));
        data.add(new BillsUtil("Kisumu Water", "Utility"));
        data.add(new BillsUtil("KPLC Postpaid", "Utility"));
        data.add(new BillsUtil("KPLC Prepaid", "Utility"));
        data.add(new BillsUtil("Lagos Water Corporation", "Utility"));
        data.add(new BillsUtil("LASG Public Utilities Levy", "Utility"));
        data.add(new BillsUtil("LUMOS SOLAR", "Utility"));
        data.add(new BillsUtil("M-KOPA", "Utility"));
        data.add(new BillsUtil("Madan", "Utility"));
        data.add(new BillsUtil("NAWEC CASHPOWER", "Utility"));
        data.add(new BillsUtil("Next Big Thing", "Utility"));
        data.add(new BillsUtil("Ogun State Water Corporation", "Utility"));
        data.add(new BillsUtil("PHCN Postpaid (ALL ZONES)", "Utility"));
        data.add(new BillsUtil("PHED Postpaid", "Utility"));
        data.add(new BillsUtil("PHED Prepaid", "Utility"));
        data.add(new BillsUtil("SOLAR ENERGY BY DLIGHT LTD", "Utility"));
        data.add(new BillsUtil("TAWASCO COLLECTIONS", "Utility"));
        data.add(new BillsUtil("Waste Management Billing System", "Utility"));
        data.add(new BillsUtil("Yola Electricity Distribution Company", "Utility"));
        data.add(new BillsUtil("ZLGA - Kaduna Waste Levy", "Utility"));
        data.add(new BillsUtil("ZLGA-ONDO STATE WASTE LEVY", "Utility"));
        return data;
    }


}
