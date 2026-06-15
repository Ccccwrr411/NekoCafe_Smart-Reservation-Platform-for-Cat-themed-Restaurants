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
 *   String  token   = JwtUtil.generateToken(userId, roleId, storeId);
 *   Long    uid      = JwtUtil.getUserIdFromToken(token);
 *   Integer roleId   = JwtUtil.getRoleIdFromToken(token);
 *   Integer storeId  = JwtUtil.getStoreIdFromToken(token);
 *   boolean ok        = JwtUtil.validateToken(token);
 */
public class JwtUtil {

    /** 签名密钥（上线前务必改成随机长字符串并放进环境变量） */
    private static final String SECRET = "nekocafe-secret-key-change-me-before-deploy-32chars!";

    /** Token 有效期：7 天（毫秒） */
    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    /** 懒加载的密钥对象 */
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * 兼容旧调用：仅传入 userId
     * @param userId 用户 ID
     * @return JWT 字符串
     */
    public static String generateToken(Long userId) {
        return generateToken(userId, null, null);
    }

    /**
     * 完整版本：将 userId / roleId / storeId 写入 Token 的自定义 claims
     * @param userId  用户 ID
     * @param roleId  角色 ID（可为 null）
     * @param storeId 门店 ID（可为 null）
     * @return JWT 字符串
     */
    public static String generateToken(Long userId, Integer roleId, Integer storeId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_MS);

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiration);

        if (roleId != null) {
            builder.claim("roleId", roleId);
        }
        if (storeId != null) {
            builder.claim("storeId", storeId);
        }

        return builder.signWith(KEY).compact();
    }

    /**
     * 从 Token 中解析 userId
     * @param token JWT 字符串（不含 "Bearer " 前缀）
     * @return userId
     * @throws JwtException Token 非法或已过期
     */
    public static Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * 从 Token 中解析 roleId（若 Token 中未存储则返回 null）
     */
    public static Integer getRoleIdFromToken(String token) {
        Object val = parseClaims(token).get("roleId");
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Long) return ((Long) val).intValue();
        return null;
    }

    /**
     * 从 Token 中解析 storeId（若 Token 中未存储则返回 null）
     */
    public static Integer getStoreIdFromToken(String token) {
        Object val = parseClaims(token).get("storeId");
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Long) return ((Long) val).intValue();
        return null;
    }

    /**
     * 校验 Token 是否有效
     * @param token JWT 字符串
     * @return true = 有效，false = 过期或签名不匹配
     */
    public static boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 内部复用：解析 Claims */
    private static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
