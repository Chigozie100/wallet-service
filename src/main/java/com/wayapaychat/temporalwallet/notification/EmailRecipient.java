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
public class EmailRecipient {

    @NotBlank(message = "value must not be blank, also enter the right key *fullName*")
    private String fullName;

    @NotBlank(message = "value must not be blank, also enter the right key *email*")
    private String email;
}
