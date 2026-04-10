package com.sjcapstone.domain.user.service;

import com.sjcapstone.domain.user.dto.UserCreateRequest;
import com.sjcapstone.domain.user.dto.UserListResponse;
import com.sjcapstone.domain.user.dto.UserResponse;
import com.sjcapstone.domain.user.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUser(Long userId);

    List<UserListResponse> getUsers();

    UserResponse updateUser(Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);
}