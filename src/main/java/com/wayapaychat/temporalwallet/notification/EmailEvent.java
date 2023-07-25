package com.wayapaychat.temporalwallet.notification;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EmailEvent extends EventBase {

    @NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(EMAIL)", message = "must match 'EMAIL'")
    private String eventType;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private EmailPayload data;

    private String eventCategory;
    private String subject;

    private String productType;
    private String htmlCode;
    private String attachment;

  
}
