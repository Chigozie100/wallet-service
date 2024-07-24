package com.wayapaychat.temporalwallet.security;


import com.wayapaychat.temporalwallet.config.UserDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthResponse {
    private String timeStamp;
    private Boolean status;
    private String message;
    private UserDetail data;

    public AuthResponse(Boolean status, String message, UserDetail data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
