package com.wayapaychat.temporalwallet.notification;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.wayapaychat.temporalwallet.enumm.EventCategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TransEmailEvent extends EventBase {
	
	@NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(EMAIL)", message = "must match 'EMAIL'")
    private String eventType;

    private EventCategory eventCategory;
    private String subject;
    private String availableBalance;
    private String accountName;
    private String transactionCurrency;
    private String accountNumber;
    private String transactionType;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private EmailPayload data;
    
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private String transactionId;
    
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private String amount;
    
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private String transactionDate;
    private String narration;
    private String productType;
    private String initiator;

    public TransEmailEvent(EmailPayload data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }

}
