package com.travel.auth.service.impl;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.constant.AccountType;
import com.travel.auth.constant.RoleCode;
import com.travel.auth.dto.LoginRequest;
import com.travel.auth.dto.RefreshTokenRequest;
import com.travel.auth.dto.RegisterRequest;
import com.travel.auth.dto.UpdateProfileRequest;
import com.travel.auth.entity.RoleEntity;
import com.travel.auth.entity.UserEntity;
import com.travel.auth.entity.UserProfileEntity;
import com.travel.auth.mapper.UserMapper;
import com.travel.auth.repository.RoleRepository;
import com.travel.auth.repository.UserProfileRepository;
import com.travel.auth.repository.UserRepository;
import com.travel.auth.service.AuthService;
import com.travel.auth.service.KeycloakService;
import com.travel.auth.viewmodel.AuthTokenVm;
import com.travel.auth.viewmodel.UserProfileVm;
import com.travel.auth.viewmodel.UserVm;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;
    private final com.travel.auth.service.EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserVm register(RegisterRequest request) {
        log.info("Processing user registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.of(ErrorCode.AUTH_EMAIL_EXISTS, "Email đã được sử dụng bởi tài khoản khác: " + request.getEmail());
        }

        RoleCode targetRoleCode = (request.getAccountType() == AccountType.GUIDE) 
                ? RoleCode.ROLE_GUIDE 
                : RoleCode.ROLE_TOURIST;

        AccountStatus initialStatus = (request.getAccountType() == AccountType.GUIDE) 
                ? AccountStatus.PENDING_APPROVAL 
                : AccountStatus.ACTIVE;

        RoleEntity role = roleRepository.findByCode(targetRoleCode)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .code(targetRoleCode)
                        .description("Default system role for " + targetRoleCode)
                        .build()));

        // Create user identity in Keycloak
        keycloakService.createKeycloakUser(request.getEmail(), request.getPassword(), request.getFullName());

        Set<RoleEntity> roles = new HashSet<>();
        roles.add(role);

        UserEntity userEntity = UserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(initialStatus)
                .roles(roles)
                .build();

        UserProfileEntity profileEntity = UserProfileEntity.builder()
                .user(userEntity)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        userEntity.setProfile(profileEntity);

        UserEntity savedUser = userRepository.save(userEntity);
        log.info("Successfully registered user with ID: {}, status: {}", savedUser.getId(), savedUser.getStatus());

        return userMapper.toUserVm(savedUser);
    }

    @Override
    public AuthTokenVm login(LoginRequest request) {
        log.info("Processing login for username: {}", request.getUsername());

        Optional<UserEntity> userOpt = userRepository.findByEmail(request.getUsername());
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (user.getStatus() == AccountStatus.BLOCKED) {
                throw BusinessException.badRequest("Tài khoản đang bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            if (user.getStatus() == AccountStatus.PENDING_APPROVAL) {
                throw BusinessException.badRequest("Tài khoản đang chờ quản trị viên phê duyệt.");
            }

            if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                log.info("Xác thực người dùng thành công qua local database: {}", request.getUsername());
                String token = "jwt-access-token-" + user.getId() + "-" + System.currentTimeMillis();
                return AuthTokenVm.builder()
                        .accessToken(token)
                        .refreshToken("jwt-refresh-token-" + user.getId())
                        .tokenType("Bearer")
                        .expiresIn(3600L)
                        .build();
            } else {
                throw BusinessException.of(ErrorCode.AUTH_INVALID_CREDENTIALS, "Email hoặc mật khẩu không chính xác");
            }
        }

        try {
            return keycloakService.authenticate(request.getUsername(), request.getPassword());
        } catch (Exception e) {
            log.warn("Xác thực người dùng thất bại: {}", e.getMessage());
            throw BusinessException.of(ErrorCode.AUTH_INVALID_CREDENTIALS, "Email hoặc mật khẩu không chính xác");
        }
    }

    @Override
    public AuthTokenVm refreshToken(RefreshTokenRequest request) {
        log.info("Processing refresh token request");
        return keycloakService.refreshToken(request.getRefreshToken());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileVm getProfile(UUID userId) {
        log.info("Fetching profile for user ID: {}", userId);
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_USER_NOT_FOUND, "Không tìm thấy hồ sơ người dùng cho ID: " + userId));

        return userMapper.toUserProfileVm(profile);
    }

    @Override
    @Transactional
    public UserProfileVm updateProfile(UUID userId, UpdateProfileRequest request, MultipartFile avatarFile) {
        log.info("Updating profile for user ID: {}", userId);

        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_USER_NOT_FOUND, "Không tìm thấy hồ sơ người dùng cho ID: " + userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getEmergencyContact() != null) {
            profile.setEmergencyContact(request.getEmergencyContact());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = "/uploads/avatars/" + userId + "_" + avatarFile.getOriginalFilename();
            profile.setAvatarUrl(avatarUrl);
        }

        UserProfileEntity updatedProfile = userProfileRepository.save(profile);
        return userMapper.toUserProfileVm(updatedProfile);
    }

    @Override
    @Transactional
    public UserVm updateUserStatus(UUID userId, AccountStatus status) {
        log.info("Updating status for user ID: {} to {}", userId, status);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_USER_NOT_FOUND, "Không tìm thấy người dùng cho ID: " + userId));

        user.setStatus(status);
        UserEntity updatedUser = userRepository.save(user);

        return userMapper.toUserVm(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVm> getAllUsers() {
        log.info("Admin fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toUserVm)
                .toList();
    }

    @Override
    @Transactional
    public UserVm adminUpdateUser(UUID userId, com.travel.auth.dto.AdminUpdateUserRequest request) {
        log.info("Admin updating user details for user ID: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_USER_NOT_FOUND, "Không tìm thấy người dùng cho ID: " + userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            if (user.getProfile() == null) {
                user.setProfile(UserProfileEntity.builder().user(user).fullName(request.getFullName().trim()).build());
            } else {
                user.getProfile().setFullName(request.getFullName().trim());
            }
        }

        if (request.getPhoneNumber() != null && user.getProfile() != null) {
            user.getProfile().setPhoneNumber(request.getPhoneNumber().trim());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<RoleEntity> newRoles = new HashSet<>();
            for (RoleCode roleCode : request.getRoles()) {
                roleRepository.findByCode(roleCode).ifPresent(newRoles::add);
            }
            if (!newRoles.isEmpty()) {
                user.setRoles(newRoles);
            }
        }

        UserEntity savedUser = userRepository.save(user);
        return userMapper.toUserVm(savedUser);
    }

    @Override
    @Transactional
    public void adminDeleteUser(UUID userId) {
        log.info("Admin deleting user ID: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.AUTH_USER_NOT_FOUND, "Không tìm thấy người dùng cho ID: " + userId));

        if ("admin@example.com".equalsIgnoreCase(user.getEmail())) {
            throw BusinessException.badRequest("Không được phép xóa tài khoản Quản Trị Viên mặc định");
        }

        userRepository.delete(user);
        log.info("Đã xóa người dùng thành công ID: {}", userId);
    }

    @Override
    @Transactional
    public AuthTokenVm loginWithGoogle(com.travel.auth.dto.GoogleLoginRequest request) {
        log.info("Processing Google login for email: {}", request.getEmail());
        String email = request.getEmail().trim().toLowerCase();

        RoleEntity role = roleRepository.findByCode(RoleCode.ROLE_TOURIST)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .code(RoleCode.ROLE_TOURIST)
                        .description("Default system role for ROLE_TOURIST")
                        .build()));

        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            Set<RoleEntity> roles = new HashSet<>();
            roles.add(role);

            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .status(AccountStatus.ACTIVE)
                    .roles(roles)
                    .build();

            UserProfileEntity profile = UserProfileEntity.builder()
                    .user(newUser)
                    .fullName(request.getFullName() != null && !request.getFullName().isBlank() ? request.getFullName() : email.split("@")[0])
                    .avatarUrl(request.getAvatarUrl())
                    .phoneNumber("0901234567")
                    .build();

            newUser.setProfile(profile);
            return userRepository.save(newUser);
        });

        // Đồng bộ avatar & tên mới nhất từ Google
        if (user.getProfile() != null) {
            boolean updated = false;
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
                user.getProfile().setAvatarUrl(request.getAvatarUrl());
                updated = true;
            }
            if (request.getFullName() != null && !request.getFullName().isBlank()) {
                user.getProfile().setFullName(request.getFullName());
                updated = true;
            }
            if (updated) {
                userProfileRepository.save(user.getProfile());
            }
        }

        String token = "jwt-access-token-" + user.getId() + "-" + System.currentTimeMillis();
        return AuthTokenVm.builder()
                .accessToken(token)
                .refreshToken("jwt-refresh-token-" + user.getId())
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();
    }

    private final java.util.concurrent.ConcurrentHashMap<String, OtpEntry> otpCache = new java.util.concurrent.ConcurrentHashMap<>();

    private record OtpEntry(String otp, long expiryTimeMillis) {}

    @Override
    public void sendForgotPasswordOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        log.info("Sending OTP for forgot password to email: {}", normalizedEmail);

        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw BusinessException.badRequest("Email không tồn tại trong hệ thống: " + normalizedEmail);
        }

        String otp = String.format("%06d", new java.security.SecureRandom().nextInt(1_000_000));
        long expiry = System.currentTimeMillis() + (5 * 60 * 1000);
        otpCache.put(normalizedEmail, new OtpEntry(otp, expiry));

        log.info("====================================================================");
        log.info(">> MA OTP KHOI PHUC MAT KHAU CHO {}: {}", normalizedEmail, otp);
        log.info("====================================================================");

        emailService.sendOtpEmail(normalizedEmail, otp);
    }

    @Override
    public boolean verifyForgotPasswordOtp(String email, String otp) {
        String normalizedEmail = email.trim().toLowerCase();
        OtpEntry entry = otpCache.get(normalizedEmail);

        if (entry == null || System.currentTimeMillis() > entry.expiryTimeMillis()) {
            otpCache.remove(normalizedEmail);
            throw BusinessException.badRequest("Mã OTP đã hết hạn hoặc không tồn tại. Vui lòng lấy mã mới.");
        }

        if (!entry.otp().equals(otp.trim())) {
            throw BusinessException.badRequest("Mã OTP không chính xác.");
        }

        return true;
    }

    @Override
    @Transactional
    public void resetPassword(com.travel.auth.dto.ResetPasswordRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        log.info("Resetting password for email: {}", normalizedEmail);

        verifyForgotPasswordOtp(normalizedEmail, request.getOtp());

        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> BusinessException.badRequest("Email không tồn tại: " + normalizedEmail));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpCache.remove(normalizedEmail);
        log.info("Cập nhật mật khẩu mới thành công cho user: {}", normalizedEmail);
    }
}
