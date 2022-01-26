package com.wayapaychat.temporalwallet.notification;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InAppRecipient {

    @NotBlank(message = "value must not be blank, also enter the right key *userId*")
    private String userId;
}
