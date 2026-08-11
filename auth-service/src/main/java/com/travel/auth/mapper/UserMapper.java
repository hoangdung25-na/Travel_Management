package com.travel.auth.mapper;

import com.travel.auth.constant.RoleCode;
import com.travel.auth.entity.RoleEntity;
import com.travel.auth.entity.UserEntity;
import com.travel.auth.entity.UserProfileEntity;
import com.travel.auth.viewmodel.UserProfileVm;
import com.travel.auth.viewmodel.UserVm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "fullName", source = "profile.fullName")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoleEntitiesToRoleCodes")
    UserVm toUserVm(UserEntity user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "roles", source = "user.roles", qualifiedByName = "mapRoleEntitiesToRoleCodes")
    UserProfileVm toUserProfileVm(UserProfileEntity profile);

    @Named("mapRoleEntitiesToRoleCodes")
    default Set<RoleCode> mapRoleEntitiesToRoleCodes(Set<RoleEntity> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toSet());
    }
}
