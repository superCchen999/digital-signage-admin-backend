package com.digitalsignage.admin.auth.service;

import com.digitalsignage.admin.auth.dto.LoginRequest;
import com.digitalsignage.admin.auth.dto.LoginResponse;
import com.digitalsignage.admin.auth.dto.RefreshTokenRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);
}
