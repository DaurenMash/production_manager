package com.prodman.userservice.mapper;

import com.prodman.userservice.dto.response.UserResponse;
import com.prodman.userservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
    
    // Если нужен маппинг с явным указанием роли VISITOR
    @Mapping(target = "role", source = "role")
    UserResponse toResponseWithRole(User user);
}

//напиши unit тесты