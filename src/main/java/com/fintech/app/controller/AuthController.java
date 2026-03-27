package com.fintech.app.controller;

import com.fintech.app.dto.request.LoginRequest;
import com.fintech.app.dto.request.RegisterRequest;
import com.fintech.app.dto.request.VerifyOtpRequest;
import com.fintech.app.dto.response.ApiResponse;
import com.fintech.app.dto.response.LoginResponse;
import com.fintech.app.dto.response.RegisterResponse;
import com.fintech.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User registration, OTP verification and login APIs")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers user with name, email, mobile and generates OTP")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/register - email={}", request.getEmail());
        RegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. OTP sent successfully.", response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verifies OTP and activates user account with default balance")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("POST /api/verify-otp - mobile/email={}", request.getIdentifier());
        ApiResponse<String> response = userService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "On expiry resend a new otp")
    public ResponseEntity<ApiResponse<RegisterResponse>> resendOtp(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/resend-otp - mobile/email={}", request.getIdentifier());
        RegisterResponse response = userService.resendOtp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("OTP sent successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Login using mobile or email to receive JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/login - identifier={}", request.getIdentifier());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
