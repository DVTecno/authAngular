package com.auth.service.interfaces;

import com.auth.model.User;

import java.util.UUID;

public interface IProfileService {
    void createProfileWithUser(RequestCreateProfileDTO requestCreateProfileDTO, User user);
    ResponseProfileDTO createProfile(String userIdOrDni, RequestCreateProfileDTO profileDto);
    ResponseProfileDTO findProfileByUserIdOrDni(String userIdOrDni);
    ResponseProfileDTO findProfileByUserIdOrThrowIfNotFound(UUID userId);
    ResponseProfileDTO findProfileByDniOrThrowIfNotFound(String dni);
    ResponseProfileDTO updateProfile(String userIdOrDni, UUID profileId, RequestCreateProfileDTO profileDto);
    ResponseProfileDTO updateProfile(UUID userIdOrDni, RequestCreateProfileDTO profileDto);
    void deleteProfile(String userIdOrDni, UUID profileId);
}

