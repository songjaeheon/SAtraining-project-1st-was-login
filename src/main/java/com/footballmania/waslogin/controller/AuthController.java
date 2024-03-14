package com.footballmania.waslogin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.footballmania.waslogin.dto.LoginRequestDto;
import com.footballmania.waslogin.service.CustomUserDetailsService;
import com.footballmania.waslogin.service.RedisService;
import com.footballmania.waslogin.util.JwtTokenUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://footballmania.com", "https://footballmania.com"})
public class AuthController {
    
    private static final String accessTokenCookieName = "YL_access_token";
    private static final String accessTokenRedisPrefix = "REDIS_JWT_";

    @Autowired
    private RedisService redisService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(path="/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        UserDetails userDetails = null;

        try {
            // find user by email.
            userDetails = customUserDetailsService.loadUserByUsername(loginRequestDto.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        if(passwordEncoder.matches(loginRequestDto.getPassword(), userDetails.getPassword())) {
            // generate access token
            Cookie accessTokenCookie = new Cookie(accessTokenCookieName, jwtTokenUtil.generateAccessToken(userDetails));
            accessTokenCookie.setPath("/");
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setMaxAge((int)jwtTokenUtil.getJwtAccessExpirationTime());
            response.addCookie(accessTokenCookie);

            return ResponseEntity.ok().build();
        } else {
            // login failed
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path="/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // cookie 확인 (http-only refresh token)
        Cookie[] cookies = request.getCookies();
        // refresh token으로 access token 재발급
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(accessTokenCookieName)) {
                    try {
                        // Redis에 (key, value)로 (Prefix + access token, username(email)) 저장
                        redisService.setAccessToken(accessTokenRedisPrefix + cookie.getValue(), "logout");
                        // redisService.setAccessToken(accessTokenRedisPrefix + cookie.getValue(), ((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
                        // access token 수명 0으로 설정
                        Cookie accessTokenCookie = new Cookie(accessTokenCookieName, null);
                        accessTokenCookie.setHttpOnly(true);
                        accessTokenCookie.setMaxAge(0);
                        accessTokenCookie.setPath("/");
                        response.addCookie(accessTokenCookie);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.badRequest().build();
                    }
                }
            }
        }
        return ResponseEntity.ok().build();
    }


}
