    package com.fiuni.mytube_users.config;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.filter.CorsFilter;

    @Configuration
    public class GlobalCorsConfig {

        @Bean
        public CorsFilter corsFilter() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.addAllowedOriginPattern("*");  // Permitir todos los orígenes
            config.addAllowedHeader("*");  // Permitir todos los encabezados
            config.addAllowedMethod("*");  // Permitir todos los métodos HTTP
            source.registerCorsConfiguration("/**", config);
            return new CorsFilter(source);
        }
    }
