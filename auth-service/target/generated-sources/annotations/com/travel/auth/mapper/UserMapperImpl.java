package com.travel.auth.mapper;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.entity.RoleEntity;
import com.travel.auth.entity.UserEntity;
import com.travel.auth.entity.UserProfileEntity;
import com.travel.auth.viewmodel.UserProfileVm;
import com.travel.auth.viewmodel.UserVm;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-12T15:27:58+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserVm toUserVm(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        UserVm.UserVmBuilder userVm = UserVm.builder();

        userVm.userId( user.getId() );
        userVm.fullName( userProfileFullName( user ) );
        userVm.roles( mapRoleEntitiesToRoleCodes( user.getRoles() ) );
        userVm.email( user.getEmail() );
        userVm.status( user.getStatus() );

        return userVm.build();
    }

    @Override
    public UserProfileVm toUserProfileVm(UserProfileEntity profile) {
        if ( profile == null ) {
            return null;
        }

        UserProfileVm.UserProfileVmBuilder userProfileVm = UserProfileVm.builder();

        userProfileVm.userId( profileUserId( profile ) );
        userProfileVm.email( profileUserEmail( profile ) );
        userProfileVm.status( profileUserStatus( profile ) );
        Set<RoleEntity> roles = profileUserRoles( profile );
        userProfileVm.roles( mapRoleEntitiesToRoleCodes( roles ) );
        userProfileVm.avatarUrl( profile.getAvatarUrl() );
        userProfileVm.dateOfBirth( profile.getDateOfBirth() );
        userProfileVm.emergencyContact( profile.getEmergencyContact() );
        userProfileVm.fullName( profile.getFullName() );
        userProfileVm.phoneNumber( profile.getPhoneNumber() );

        return userProfileVm.build();
    }

    private String userProfileFullName(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }
        UserProfileEntity profile = userEntity.getProfile();
        if ( profile == null ) {
            return null;
        }
        String fullName = profile.getFullName();
        if ( fullName == null ) {
            return null;
        }
        return fullName;
    }

    private UUID profileUserId(UserProfileEntity userProfileEntity) {
        if ( userProfileEntity == null ) {
            return null;
        }
        UserEntity user = userProfileEntity.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String profileUserEmail(UserProfileEntity userProfileEntity) {
        if ( userProfileEntity == null ) {
            return null;
        }
        UserEntity user = userProfileEntity.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }

    private AccountStatus profileUserStatus(UserProfileEntity userProfileEntity) {
        if ( userProfileEntity == null ) {
            return null;
        }
        UserEntity user = userProfileEntity.getUser();
        if ( user == null ) {
            return null;
        }
        AccountStatus status = user.getStatus();
        if ( status == null ) {
            return null;
        }
        return status;
    }

    private Set<RoleEntity> profileUserRoles(UserProfileEntity userProfileEntity) {
        if ( userProfileEntity == null ) {
            return null;
        }
        UserEntity user = userProfileEntity.getUser();
        if ( user == null ) {
            return null;
        }
        Set<RoleEntity> roles = user.getRoles();
        if ( roles == null ) {
            return null;
        }
        return roles;
    }
}
