package com.digitalsignage.admin.user.controller;

import com.digitalsignage.admin.common.enums.SysUserStatus;
import com.digitalsignage.admin.common.enums.UserRole;
import com.digitalsignage.admin.common.exception.GlobalExceptionHandler;
import com.digitalsignage.admin.security.JwtService;
import com.digitalsignage.admin.user.dto.CreateUserRequest;
import com.digitalsignage.admin.user.dto.UpdateUserRequest;
import com.digitalsignage.admin.user.dto.UserResponse;
import com.digitalsignage.admin.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    private static UserResponse sampleUser() {
        return UserResponse.builder()
                .id(1L)
                .username("admin")
                .email("a@example.com")
                .role(UserRole.ADMIN)
                .status(SysUserStatus.ACTIVE)
                .organizationId(10L)
                .createdAt(LocalDateTime.parse("2026-01-01T12:00:00"))
                .build();
    }

    @Test
    void me_returnsOk() throws Exception {
        when(userService.getMe()).thenReturn(sampleUser());

        mockMvc.perform(get("/api/admin/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.organizationId").value(10));
    }

    @Test
    void list_returnsOk() throws Exception {
        when(userService.listUsers()).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(userService.getUser(5L)).thenReturn(sampleUser());

        mockMvc.perform(get("/api/admin/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
        verify(userService).getUser(5L);
    }

    @Test
    void create_returnsOk() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(sampleUser());

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setPassword("secret12");
        req.setEmail("n@example.com");
        req.setRole(UserRole.VIEWER);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void create_shortPassword_returnsBadRequest() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setPassword("short");
        req.setRole(UserRole.VIEWER);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void update_returnsOk() throws Exception {
        when(userService.updateUser(eq(3L), any(UpdateUserRequest.class))).thenReturn(sampleUser());

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("x@example.com");

        mockMvc.perform(put("/api/admin/users/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(userService).updateUser(eq(3L), any(UpdateUserRequest.class));
    }

    @Test
    void delete_returnsOk() throws Exception {
        doNothing().when(userService).deleteUser(7L);

        mockMvc.perform(delete("/api/admin/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        verify(userService).deleteUser(7L);
    }
}
