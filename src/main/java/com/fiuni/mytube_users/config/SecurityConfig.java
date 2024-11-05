package com.fiuni.mytube_users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Permitir acceso sin restricciones a todas las rutas y deshabilitar CORS y CSRF
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()  // Permitir todas las solicitudes sin autenticación
                )
                .csrf(AbstractHttpConfigurer::disable);  // Deshabilitar CSRF de manera segura

        return http.build();
    }
}
