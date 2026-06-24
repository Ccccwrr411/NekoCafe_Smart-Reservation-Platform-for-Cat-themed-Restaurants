package cn.edu.bjfu.nekocafe.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 基于 Redis 的轻量分布式锁（SET key token NX PX + Lua 比较删除）。
 *
 * 用途：序列化「同一资源」的并发临界区（如同一桌位的预约创建检查-插入），
 * 不同资源的请求互不阻塞，既保证强一致又保持高并发吞吐。
 */
@Component
public class DistributedLock {

    /** 释放锁：仅当 value 等于自己持有的 token 时才删除，避免误删他人锁。 */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public DistributedLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在分布式锁保护下执行临界区。获取不到锁会自旋重试到 waitMs 超时。
     *
     * @param key      锁键，如 "lock:table:123"
     * @param leaseMs  锁自动过期时间（防止持有者宕机死锁）
     * @param waitMs   最长等待获取时间
     * @param action   临界区逻辑
     * @return action 的返回值
     * @throws IllegalStateException 等待超时仍未获取到锁
     */
    public <T> T executeWithLock(String key, long leaseMs, long waitMs, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + waitMs * 1_000_000L;
        boolean acquired = false;
        try {
            while (true) {
                Boolean ok = redisTemplate.opsForValue()
                        .setIfAbsent(key, token, Duration.ofMillis(leaseMs));
                if (Boolean.TRUE.equals(ok)) {
                    acquired = true;
                    break;
                }
                if (System.nanoTime() >= deadline) {
                    throw new IllegalStateException("获取分布式锁超时: " + key);
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("获取分布式锁被中断: " + key, ie);
                }
            }
            return action.get();
        } finally {
            if (acquired) {
                redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
            }
        }
    }
}
