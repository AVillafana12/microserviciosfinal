package com.clinic.eureka.eureka_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para permitir registro de microservicios
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/eureka/**").permitAll() // Permitir acceso a endpoints de Eureka
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}); // Habilitar autenticación HTTP Basic
        
        return http.build();
    }
}
