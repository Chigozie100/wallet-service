package com.wayapaychat.temporalwallet.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)@ToString
@AllArgsConstructor @NoArgsConstructor
public class UserDtoResponse {
    private Long timeStamp;
    private boolean status;
    private String message;
    private UserDetailPojo data;
}
