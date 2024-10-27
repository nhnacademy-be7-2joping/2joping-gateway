package com.nhnacademy.twojopinggateway.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    // 쿠키에서 토큰 추출, index 0: accessToken, 1: refreshToken
    public List<String> resolveToken(ServerWebExchange exchange) {
        // 쿠키에서 JWT 추출
        String accessToken = exchange.getRequest().getCookies().getFirst("accessToken").getValue();
        String refreshToken = exchange.getRequest().getCookies().getFirst("refreshToken").getValue();

        return List.of(accessToken, refreshToken);
    }

    // 토큰에서 사용자 id 추출
    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication getAuthentication(String token) {
        // 토큰에서 사용자 이름을 추출
        String username = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        // 사용자 권한(roles) 추출
        List<GrantedAuthority> authorities = Arrays.stream(Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token)
                        .getBody()
                        .get("role", String.class)
                        .split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 사용자 인증 정보를 바탕으로 UsernamePasswordAuthenticationToken 생성
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    // 토큰검증
    // 비밀키, 만료기간 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
