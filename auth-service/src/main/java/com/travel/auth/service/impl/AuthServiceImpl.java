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
import com.travel.common.core.exception.BadRequestException;
import com.travel.common.core.exception.DuplicatedException;
import com.travel.common.core.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserVm register(RegisterRequest request) {
        log.info("Processing user registration for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicatedException("Email đã được sử dụng bởi tài khoản khác: " + request.getEmail());
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
                throw new BadRequestException("Tài khoản đang bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            if (user.getStatus() == AccountStatus.PENDING_APPROVAL) {
                throw new BadRequestException("Tài khoản đang chờ quản trị viên phê duyệt.");
            }
        }

        return keycloakService.authenticate(request.getUsername(), request.getPassword());
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
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng cho ID: " + userId));

        return userMapper.toUserProfileVm(profile);
    }

    @Override
    @Transactional
    public UserProfileVm updateProfile(UUID userId, UpdateProfileRequest request, MultipartFile avatarFile) {
        log.info("Updating profile for user ID: {}", userId);

        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hồ sơ người dùng cho ID: " + userId));

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
            // Future extension: Store avatar to MinIO using StorageService
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
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng cho ID: " + userId));

        user.setStatus(status);
        UserEntity updatedUser = userRepository.save(user);

        return userMapper.toUserVm(updatedUser);
    }
}
