package com.fiuni.mytube_users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
/*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Permitir acceso sin restricciones a todas las rutas
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()  // Permitir todas las solicitudes sin autenticaciÃ³n
                )
                .csrf(AbstractHttpConfigurer::disable);  // Deshabilitar CSRF de manera segura

        return http.build();
    }*/

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF (si es necesario)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/users/**").authenticated()  // Requiere autenticación para rutas de usuarios
                        .anyRequest().permitAll()  // Permitir todas las demás solicitudes sin autenticación
                );

        return http.build();
    }
}
