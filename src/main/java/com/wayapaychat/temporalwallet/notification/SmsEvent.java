package com.wayapaychat.temporalwallet.notification;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
public class SmsEvent {
	
	@NotNull(message = "make sure you entered the right key *eventType* , and the value must not be null")
    @Pattern(regexp = "(SMS)", message = "must match 'SMS'")
    private String eventType;
	
	@Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
	private SmsPayload data;
	
	@NotNull(message = "make sure you entered the right key *initiator* , and the value must not be null")
    @NotBlank(message = "initiator field must not be blank, and make sure you use the right key *initiator*")
    private String initiator;

	public SmsEvent(SmsPayload data,
			String eventType) {
		super();
		this.eventType = eventType;
		this.data = data;
	}
	

}
