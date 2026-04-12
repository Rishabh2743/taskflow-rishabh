package com.taskflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())   // ✅ disable CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()  // ✅ allow auth APIs
                .anyRequest().permitAll()                 // ✅ TEMP allow all (we secure later)
            )
            .formLogin(form -> form.disable())   // ❗ disable default login
            .httpBasic(basic -> basic.disable()); // ❗ disable basic auth

        return http.build();
    }
}