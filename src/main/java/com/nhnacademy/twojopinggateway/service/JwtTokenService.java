package com.nhnacademy.twojopinggateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    private Key key;

    @PostConstruct
    private void setKey() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public List<String> resolveToken(ServerWebExchange exchange) {
        // 쿠키에서 JWT 추출
        // 쿠키에서 토큰 추출, index 0: accessToken, 1: refreshToken
        String accessToken = exchange.getRequest().getCookies().getFirst("accessToken").getValue();
        String refreshToken = exchange.getRequest().getCookies().getFirst("refreshToken").getValue();

        return List.of(accessToken, refreshToken);
    }

    // 토큰에서 jti 추출
    public String getJti(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getId();
    }

    // 토큰검증
    // 비밀키, 만료기간 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
