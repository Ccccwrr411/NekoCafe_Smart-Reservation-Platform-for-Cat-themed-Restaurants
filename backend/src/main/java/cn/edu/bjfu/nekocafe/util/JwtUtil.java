package cn.edu.bjfu.nekocafe.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 负责生成、解析、校验 JWT Token
 *
 * 使用方式：
 *   String token = JwtUtil.generateToken(userId);
 *   Long   uid   = JwtUtil.getUserIdFromToken(token);
 *   boolean ok   = JwtUtil.validateToken(token);
 */
public class JwtUtil {

    /** 签名密钥（上线前务必改成随机长字符串并放进环境变量） */
    private static final String SECRET = "nekocafe-secret-key-change-me-before-deploy-32chars!";

    /** Token 有效期：7 天（毫秒） */
    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    /** 懒加载的密钥对象 */
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 根据 userId 生成 JWT Token
     * @param userId 用户 ID
     * @return JWT 字符串
     */
    public static String generateToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(userId.toString())           // 把 userId 存进 sub 字段
                .issuedAt(now)                        // 签发时间
                .expiration(expiration)               // 过期时间
                .signWith(KEY)                        // 签名
                .compact();
    }

    /**
     * 从 Token 中解析 userId
     * @param token JWT 字符串（不含 "Bearer " 前缀）
     * @return userId
     * @throws JwtException Token 非法或已过期
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 校验 Token 是否有效
     * @param token JWT 字符串
     * @return true = 有效，false = 过期或签名不匹配
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
