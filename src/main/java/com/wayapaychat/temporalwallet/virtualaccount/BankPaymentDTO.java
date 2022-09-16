package com.wayapaychat.temporalwallet.virtualaccount;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "CreditAccount",
        "DebitAccount",
        "Narration",
        "PaymentReference",
        "DebitAccountName",
        "CreditAccountName",
        "ChannelCode",
        "Amount",
        "SourceBankCode",
        "OriginatorAccountNo",
        "OriginatorAccountName",
        "BVN",
        "Currency"
})
public class BankPaymentDTO {

    @JsonProperty("CreditAccount")
    private String creditAccount;

    @JsonProperty("DebitAccount")
    private String debitAccount;

    @JsonProperty("Narration")
    private String narration;

    @JsonProperty("PaymentReference")
    private String paymentReference;

    @JsonProperty("DebitAccountName")
    private String debitAccountName;

    @JsonProperty("CreditAccountName")
    private String creditAccountName;

    @JsonProperty("ChannelCode")
    private String channelCode;

    @JsonProperty("Amount")
    private BigDecimal amount;

    @JsonProperty("SourceBankCode")
    private String sourceBankCode;

    @JsonProperty("OriginatorAccountNo")
    private String originatorAccountNo;

    @JsonProperty("OriginatorAccountName")
    private String originatorAccountName;

    @JsonProperty("BVN")
    private String bvn;

    @JsonProperty("Currency")
    private String currency;

}
