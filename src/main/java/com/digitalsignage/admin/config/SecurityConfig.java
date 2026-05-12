package com.digitalsignage.admin.config;

import com.digitalsignage.admin.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/auth/login", "/api/admin/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/admin/users").hasAnyRole("ADMIN", "EDITOR", "VIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/*").hasAnyRole("ADMIN", "EDITOR", "VIEWER")
                        .requestMatchers(HttpMethod.POST, "/api/admin/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/media/*").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/admin/media").hasAnyRole("ADMIN", "EDITOR", "VIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/media/*").hasAnyRole("ADMIN", "EDITOR", "VIEWER")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/media/*").hasAnyRole("ADMIN", "EDITOR")
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
