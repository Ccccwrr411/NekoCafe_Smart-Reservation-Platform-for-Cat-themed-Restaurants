package cn.edu.bjfu.nekocafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 解决默认 RedisTemplate key 乱码问题，统一使用 String 序列化
 *
 * 使用方式（注入）：
 *   @Autowired
 *   private RedisTemplate<String, Object> redisTemplate;
 *
 *   redisTemplate.opsForValue().set("key", value, 1, TimeUnit.HOURS);
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        // Value 使用 JSON 序列化（可换成 Jackson2JsonRedisSerializer）
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
