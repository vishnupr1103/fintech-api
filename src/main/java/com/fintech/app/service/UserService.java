package com.fintech.app.service;

import com.fintech.app.dto.request.LoginRequest;
import com.fintech.app.dto.request.RegisterRequest;
import com.fintech.app.dto.request.VerifyOtpRequest;
import com.fintech.app.dto.response.ApiResponse;
import com.fintech.app.dto.response.LoginResponse;
import com.fintech.app.dto.response.RegisterResponse;

public interface UserService {
    RegisterResponse register(RegisterRequest request);

    ApiResponse<String> verifyOtp(VerifyOtpRequest request);

    RegisterResponse resendOtp(LoginRequest request);

    LoginResponse login(LoginRequest request);
}
