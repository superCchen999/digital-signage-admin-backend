package com.digitalsignage.admin.user.dto;

import com.digitalsignage.admin.common.enums.SysUserStatus;
import com.digitalsignage.admin.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Email(message = "invalid email")
    @Size(max = 255)
    private String email;

    private UserRole role;

    private SysUserStatus status;

    @Size(min = 6, max = 128)
    private String password;
}
