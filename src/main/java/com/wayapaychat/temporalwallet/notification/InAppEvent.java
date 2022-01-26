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
public class InAppEvent extends EventBase {
    @NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(IN_APP)", message = "must match 'IN_APP'")
    private String eventType;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private InAppPayload data;

    public InAppEvent(InAppPayload data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }
}
