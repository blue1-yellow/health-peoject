package org.health.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = System.getenv().getOrDefault("JWT_SECRET", "local-development-only-change-before-deployment");
    // 有效期：12小时
    private static final long EXPIRATION = 12 * 60 * 60 * 1000L;

    // 根据userId生成token
    public static String generateToken(Integer userId){
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 解析token，拿到userId
    public static Integer parseToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        String userIdStr = claims.getSubject();
        return Integer.parseInt(userIdStr);
    }
}
