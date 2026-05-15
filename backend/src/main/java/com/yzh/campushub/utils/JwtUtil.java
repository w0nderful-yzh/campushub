package com.yzh.campushub.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {
    // 密钥 - 必须足够长以满足HS256算法的要求 (至少256位/32字节)
    // 使用Keys.secretKeyFor(SignatureAlgorithm.HS256)生成的安全密钥的Base64编码，或者直接使用Keys.hmacShaKeyFor
    private static final String SECRET_STRING = "campushubSecretKeyMustBeLongEnoughToMeetTheRequirementOfHS256Algorithm";
    
    // 使用Keys.hmacShaKeyFor将字符串转换为Key对象
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    // 过期时间（7天）
    private static final long EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    /**
     * 生成token
     */
    public static String generateToken(Long userId, String username) {

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析token
     */
    public static Claims parseToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取 userId
     */
    public static Long getUserId(String token) {

        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 获取 username
     */
    public static String getUsername(String token) {

        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

}
