package com.footballmania.waslogin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    
    private final int strength = 12;

    @Autowired
    private JwtFilter jwtFilter;

    // Password encoder, used by Spring to encode and verify user passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(strength);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // JWT 사용 위해 세션 비활성화
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);


        // XSS Protection, Content Security Policy 설정
        http.headers()
            .xssProtection()
            .and()
            // every contents source must be derived from the server
            .contentSecurityPolicy("default-src 'self'");

        http.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest()
            .permitAll())
            .csrf(AbstractHttpConfigurer::disable);
        
        // Add custom JWT security filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
