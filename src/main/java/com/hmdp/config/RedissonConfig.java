package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Program: hm-dianping
 * @Package: com.hmdp.config
 * @Class: RedissonConfig
 * @Description: 配置Redisson
 * @Author: cwp0
 * @CreatedTime: 2024/07/28 16:04
 * @Version: 1.0
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.3.186:6379").setPassword("123456");
        // 创建RedissonClient对象
        return Redisson.create(config);
    }

}

