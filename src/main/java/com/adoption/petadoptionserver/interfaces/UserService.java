package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(UserDto user);
    Optional<UserDto> findById(Long id);
    Optional<UserDto> findByUsername(String username);
    List<UserDto> findAll();
    boolean deleteUser(Long id);
}