package com.digitalsignage.admin.user.service;

import com.digitalsignage.admin.user.dto.CreateUserRequest;
import com.digitalsignage.admin.user.dto.UpdateUserRequest;
import com.digitalsignage.admin.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getMe();

    List<UserResponse> listUsers();

    UserResponse getUser(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}
