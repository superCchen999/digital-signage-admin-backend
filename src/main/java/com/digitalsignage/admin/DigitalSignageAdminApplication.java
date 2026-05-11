package com.digitalsignage.admin;

import com.digitalsignage.admin.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class DigitalSignageAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalSignageAdminApplication.class, args);
    }
}
