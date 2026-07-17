package com.fruitlink.auth.service;

import com.fruitlink.auth.dto.AuthResponse;
import com.fruitlink.auth.dto.LoginRequest;
import com.fruitlink.auth.dto.RegisterRequest;
import com.fruitlink.auth.entity.User;
import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.common.BusinessException;
import com.fruitlink.config.JwtTokenProvider;
import com.fruitlink.rbac.entity.Role;
import com.fruitlink.rbac.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.mail.javamail.JavaMailSender;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new BusinessException("Phone number already registered");
        }

        Role role = roleRepository.findByName(req.getRoleName())
                .orElseThrow(() -> new BusinessException("Role not found: " + req.getRoleName()));

        User user = new User();
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setAuthProvider("password");
        user.setActive(true);

        userRepository.save(user);
        return buildTokens(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new BadCredentialsException("Invalid phone or password"));

        if (!user.isActive()) {
            throw new BusinessException("Account is inactive");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid phone or password");
        }

        return buildTokens(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }
        String phone = jwtTokenProvider.getSubject(refreshToken);
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found"));
        return buildTokens(user);
    }

    private final JavaMailSender mailSender;

    public void forgotPassword(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found with phone: " + phone));
        
        // Generate 6 digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setResetOtp(otp);
        user.setResetOtpExpiry(java.time.Instant.now().plus(java.time.Duration.ofMinutes(10)));
        userRepository.save(user);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            try {
                org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("FruitLink Password Reset OTP");
                message.setText("Your password reset OTP is: " + otp + "\nIt is valid for 10 minutes.");
                mailSender.send(message);
                System.out.println("OTP emailed to " + user.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send email to " + user.getEmail() + ". Check SMTP config. OTP is: " + otp);
            }
        } else {
            System.out.println("No email registered for " + phone + ". OTP is: " + otp);
        }
    }

    @Transactional
    public void resetPassword(com.fruitlink.auth.dto.ResetPasswordRequest req) {
        User user = userRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (user.getResetOtp() == null || user.getResetOtpExpiry() == null) {
            throw new BusinessException("No active reset request found");
        }

        if (user.getResetOtpExpiry().isBefore(java.time.Instant.now())) {
            throw new BusinessException("OTP has expired");
        }

        if (!user.getResetOtp().equals(req.getOtp())) {
            throw new BusinessException("Invalid OTP");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);
    }

    private AuthResponse buildTokens(User user) {
        Map<String, Object> claims = Map.of(
                "userId", user.getId().toString(),
                "role", user.getRole().getName()
        );
        String accessToken = jwtTokenProvider.generateAccessToken(user.getPhone(), claims);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getPhone());
        return new AuthResponse(accessToken, refreshToken, user.getId().toString(),
                user.getFullName(), user.getRole().getName(), user.getPhone());
    }
}
