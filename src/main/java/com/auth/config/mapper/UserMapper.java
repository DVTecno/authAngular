package com.auth.config.mapper;

import com.auth.dto.response.auth.UserResponseDto;
import com.auth.model.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDTO(User user);

    List<UserResponseDto> toUserResponseDTO(List<User> user);
}
