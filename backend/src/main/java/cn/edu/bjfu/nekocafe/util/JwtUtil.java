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
 *   String token = JwtUtil.generateToken(userId, roleId, storeId);
 *   Long   uid   = JwtUtil.getUserIdFromToken(token);
 *   Integer rid  = JwtUtil.getRoleIdFromToken(token);
 *   Integer sid  = JwtUtil.getStoreIdFromToken(token);
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
     * 根据 userId + roleId + storeId 生成 JWT Token
     * @param userId  用户 ID
     * @param roleId  当前角色 ID（可为 null）
     * @param storeId 所属门店 ID（可为 null）
     * @return JWT 字符串
     */
    public static String generateToken(Long userId, Integer roleId, Integer storeId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        Claims claims = Jwts.claims()
                .subject(userId.toString())
                .add("roleId", roleId)
                .add("storeId", storeId)
                .build();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(KEY)
                .compact();
    }

    /**
     * 从 Token 中解析所有 claims
     */
    private static Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中解析 userId
     * @param token JWT 字符串（不含 "Bearer " 前缀）
     * @return userId
     * @throws JwtException Token 非法或已过期
     */
    public static Long getUserIdFromToken(String token) {
        return Long.valueOf(getClaims(token).getSubject());
    }

    /**
     * 从 Token 中解析 roleId（可能为 null）
     */
    public static Integer getRoleIdFromToken(String token) {
        Object val = getClaims(token).get("roleId");
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        return null;
    }

    /**
     * 从 Token 中解析 storeId（可能为 null）
     */
    public static Integer getStoreIdFromToken(String token) {
        Object val = getClaims(token).get("storeId");
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        return null;
    }

    /**
     * 校验 Token 是否有效
     * @param token JWT 字符串
     * @return true = 有效，false = 过期或签名不匹配
     */
    public static boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
