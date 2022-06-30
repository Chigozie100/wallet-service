package com.wayapaychat.temporalwallet.notification;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
public class PaymentResponse {
    List<ParamNameValue> data = new ArrayList<>();
}
