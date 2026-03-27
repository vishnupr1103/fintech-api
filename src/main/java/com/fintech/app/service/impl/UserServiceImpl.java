package com.fintech.app.service.impl;

import com.fintech.app.dto.request.LoginRequest;
import com.fintech.app.dto.request.RegisterRequest;
import com.fintech.app.dto.request.VerifyOtpRequest;
import com.fintech.app.dto.response.ApiResponse;
import com.fintech.app.dto.response.LoginResponse;
import com.fintech.app.dto.response.RegisterResponse;
import com.fintech.app.entity.Account;
import com.fintech.app.entity.User;
import com.fintech.app.exception.BadRequestException;
import com.fintech.app.exception.ResourceNotFoundException;
import com.fintech.app.exception.UnauthorizedException;
import com.fintech.app.model.UserStatus;
import com.fintech.app.repository.AccountRepository;
import com.fintech.app.repository.UserRepository;
import com.fintech.app.security.JwtUtil;
import com.fintech.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal("1000.00");

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new BadRequestException("Mobile number is already registered");
        }

        String otp = generateOtp();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .status(UserStatus.PENDING)
                .otp(otp)
                .otpGeneratedAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        log.info("User registered with PENDING status. userId={}", user.getId());

        return RegisterResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .status(user.getStatus().name())
                .otp(otp)
                .message("OTP sent successfully (For demo, OTP is returned in response)")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<String> verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for : {}", request.getIdentifier());

        User user = userRepository.findByEmail(request.getIdentifier())
                .or(() -> userRepository.findByMobile(request.getIdentifier()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("User is already verified and active");
        }

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (user.getOtpGeneratedAt().plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new OTP");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setOtp(null);
        user.setOtpGeneratedAt(null);
        userRepository.save(user);

        // Auto-create account with default balance
        Account account = Account.builder()
                .user(user)
                .balance(DEFAULT_BALANCE)
                .build();
        accountRepository.save(account);

        log.info("User activated and account created with balance={}. userId={}", DEFAULT_BALANCE, user.getId());

        return ApiResponse.success("OTP verified successfully. Account created with default balance of ₹1000.", null);
    }

    @Override
    public RegisterResponse resendOtp(LoginRequest request) {

        User user = userRepository.findByEmail(request.getIdentifier())
                .or(() -> userRepository.findByMobile(request.getIdentifier()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("User already verified");
        }

        String newOtp = generateOtp();

        user.setOtp(newOtp);
        user.setOtpGeneratedAt(LocalDateTime.now());

        userRepository.save(user);

        return RegisterResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .status(user.getStatus().name())
                .otp(newOtp)
                .message("OTP sent successfully (For demo, OTP is returned in response)")
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for identifier: {}", request.getIdentifier());

        User user = userRepository.findByMobile(request.getIdentifier())
                .or(()-> userRepository.findByEmail(request.getIdentifier()))
                .orElseThrow(() ->
                        new ResourceNotFoundException("No user found with provided mobile/email"));;

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active. Please verify your OTP first.");
        }

        String token = jwtUtil.generateToken(request.getIdentifier(), user.getId());
        log.info("JWT token generated for userId={}", user.getId());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
