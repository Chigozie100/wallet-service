package com.wayapaychat.temporalwallet.pojo.signupKafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

@Data
public class SignUpDto {
    private UserDataDto user;
    private ProfileDataDto profile;
    private String token;

    public SignUpDto(String jsonString) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SignUpDto dto = mapper.readValue(jsonString, SignUpDto.class);
        this.user = dto.user;
        this.profile = dto.profile;
        this.token = dto.token;
    }
}
