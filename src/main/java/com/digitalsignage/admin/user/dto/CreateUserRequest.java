package com.digitalsignage.admin.user.dto;

import com.digitalsignage.admin.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "username is required")
    @Size(max = 64)
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 128)
    private String password;

    @Email(message = "invalid email")
    @Size(max = 255)
    private String email;

    @NotNull(message = "role is required")
    private UserRole role;
}
