package org.csu.creditbank.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class TokenUtil {

    private static final String PREFIX = "token:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public TokenUtil(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void storeToken(String token, Long userId, String role) {
        redis.opsForHash().put(PREFIX + token, "userId", String.valueOf(userId));
        redis.opsForHash().put(PREFIX + token, "role", role);
        redis.expire(PREFIX + token, TTL);
    }

    public Long getUserId(String token) {
        Object val = redis.opsForHash().get(PREFIX + token, "userId");
        return val != null ? Long.valueOf(val.toString()) : null;
    }

    public String getRole(String token) {
        Object val = redis.opsForHash().get(PREFIX + token, "role");
        return val != null ? val.toString() : null;
    }

    public void removeToken(String token) {
        redis.delete(PREFIX + token);
    }

    /** 刷新 token 过期时间 */
    public void refreshToken(String token) {
        redis.expire(PREFIX + token, TTL);
    }
}
