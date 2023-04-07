package com.wayapaychat.temporalwallet.notification;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class EmailRecipient {

    @NotBlank(message = "value must not be blank, also enter the right key *fullName*")
    private String fullName;

    @NotBlank(message = "value must not be blank, also enter the right key *email*")
    private String email;
}
