package com.footballmania.waslogin.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.footballmania.waslogin.service.CustomUserDetailsService;
import com.footballmania.waslogin.util.JwtTokenUtil;

@Component
public class JwtFilter extends OncePerRequestFilter {
    
    private String accessTokenCookieName = "SC_access_token";

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            // Get JWT from cookie
            String accessToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(accessTokenCookieName)) {
                        accessToken = cookie.getValue();
                        break;
                    }
                }
            }

            // If JWT is found, validate it and set authentication
            if (accessToken != null && jwtTokenUtil.validateAccessToken(accessToken)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(jwtTokenUtil.getUsernameFromAccessToken(accessToken));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        chain.doFilter(request, response);
    }
}
