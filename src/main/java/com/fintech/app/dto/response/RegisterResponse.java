package com.fintech.app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponse {
    private Long userId;
    private String name;
    private String email;
    private String mobile;
    private String status;
    private String otp;
    private String message;
}
