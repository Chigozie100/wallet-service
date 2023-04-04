package com.wayapaychat.temporalwallet.notification;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InAppRecipient {

    @NotBlank(message = "value must not be blank, also enter the right key *userId*")
    private String userId;
}
