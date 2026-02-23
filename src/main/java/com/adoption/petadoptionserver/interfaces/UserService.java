package com.adoption.petadoptionserver.interfaces;

import com.adoption.petadoptionserver.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> findAll();
    Optional<UserDto> findById(Long id);
    Optional<UserDto> findByUsername(String username);
    UserDto create(UserDto dto);
    Optional<UserDto> update(Long id, UserDto dto);
    boolean delete(Long id);
}