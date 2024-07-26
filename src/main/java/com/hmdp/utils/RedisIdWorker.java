package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @Program: hm-dianping
 * @Package: com.hmdp.utils
 * @Class: RedisIdWorker
 * @Description: 使用redis生成分布式全局唯一ID
 * @Author: cwp0
 * @CreatedTime: 2024/07/25 20:04
 * @Version: 1.0
 */
@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP = 1640995200L;
    public static final int COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix) {
        // 1. 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        // 2. 生成序列号
        // 2.1 获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2 自增
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date, 1L);
        // 3. 拼接并返回
        return timestamp << COUNT_BITS | count;
    }

    public static void main(String[] args) {
        long epochSecond = LocalDateTime.of(2022, 1, 1, 0, 0, 0).toEpochSecond(ZoneOffset.UTC);
        System.out.println(epochSecond);
    }

}

