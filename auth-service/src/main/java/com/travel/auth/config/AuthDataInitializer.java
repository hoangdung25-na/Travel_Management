package com.travel.auth.config;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.constant.RoleCode;
import com.travel.auth.entity.RoleEntity;
import com.travel.auth.entity.UserEntity;
import com.travel.auth.entity.UserProfileEntity;
import com.travel.auth.repository.RoleRepository;
import com.travel.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        log.info("Khởi tạo vai trò và tài khoản hệ thống mặc định...");

        RoleEntity roleAdmin = roleRepository.findByCode(RoleCode.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .code(RoleCode.ROLE_ADMIN)
                        .description("System Administrator")
                        .build()));

        RoleEntity roleTourist = roleRepository.findByCode(RoleCode.ROLE_TOURIST)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .code(RoleCode.ROLE_TOURIST)
                        .description("Standard Tourist")
                        .build()));

        RoleEntity roleGuide = roleRepository.findByCode(RoleCode.ROLE_GUIDE)
                .orElseGet(() -> roleRepository.save(RoleEntity.builder()
                        .code(RoleCode.ROLE_GUIDE)
                        .description("Tour Guide")
                        .build()));

        // Create Admin Account
        if (!userRepository.existsByEmail("admin@example.com")) {
            UserEntity adminUser = UserEntity.builder()
                    .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .status(AccountStatus.ACTIVE)
                    .roles(Set.of(roleAdmin))
                    .build();

            UserProfileEntity profile = UserProfileEntity.builder()
                    .user(adminUser)
                    .fullName("Quản Trị Viên Hệ Thống")
                    .phoneNumber("0988888888")
                    .build();

            adminUser.setProfile(profile);
            userRepository.save(adminUser);
            log.info("Đã khởi tạo tài khoản Admin mặc định: admin@example.com / 12345678");
        }

        // Create Tourist Account
        if (!userRepository.existsByEmail("tourist@example.com")) {
            UserEntity touristUser = UserEntity.builder()
                    .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                    .email("tourist@example.com")
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .status(AccountStatus.ACTIVE)
                    .roles(Set.of(roleTourist))
                    .build();

            UserProfileEntity profile = UserProfileEntity.builder()
                    .user(touristUser)
                    .fullName("Nguyễn Văn Tourist")
                    .phoneNumber("0901234567")
                    .build();

            touristUser.setProfile(profile);
            userRepository.save(touristUser);
            log.info("Đã khởi tạo tài khoản Tourist mặc định: tourist@example.com / 12345678");
        }

        // Create Guide Account
        if (!userRepository.existsByEmail("guide@example.com")) {
            UserEntity guideUser = UserEntity.builder()
                    .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                    .email("guide@example.com")
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .status(AccountStatus.ACTIVE)
                    .roles(Set.of(roleGuide))
                    .build();

            UserProfileEntity profile = UserProfileEntity.builder()
                    .user(guideUser)
                    .fullName("Trần Văn Hướng Dẫn")
                    .phoneNumber("0912345678")
                    .build();

            guideUser.setProfile(profile);
            userRepository.save(guideUser);
            log.info("Đã khởi tạo tài khoản Guide mặc định: guide@example.com / 12345678");
        }
    }
}
