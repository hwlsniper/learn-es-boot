package com.learn.es.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.config.RedisSessionConfig
 * @description session会话
 * @date 2020/8/12 15:06
 */
@Configuration
//@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400)
public class RedisSessionConfig {

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
		return new StringRedisTemplate(factory);
	}
}
