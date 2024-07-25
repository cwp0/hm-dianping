package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Program: hm-dianping
 * @Package: com.hmdp.utils
 * @Class: CacheClient
 * @Description: 基于StringRedisTemplate封装缓存工具类
 * @Author: cwp0
 * @CreatedTime: 2024/07/25 19:01
 * @Version: 1.0
 */
@Component
public class CacheClient {

    @Resource
    private final StringRedisTemplate stringRedisTemplate;

    /** 缓存重建线程池 */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, timeUnit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit timeUnit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));

        // 写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        // 1. 从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3. 如果存在，直接返回
            return JSONUtil.toBean(json, type);
        }
        // 判断命中的是否是空值
        if (json != null) {
            return null;
        }
        // 4. 如果不存在，根据id查询数据库
        R r = dbFallback.apply(id);
        // 5. 数据库中也不存在，返回错误
        if (r == null) {
            // 将空值写入redis，防止缓存穿透
            this.set(key, "", time, timeUnit);

            return null;
        }
        // 6. 数据库中存在，将数据写入redis缓存, 给过期时间添加一个随机值，防止缓存雪崩
        this.set(key, r, time, timeUnit);
        // 7. 返回数据
        return r;
    }

    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        // 1. 从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isBlank(shopJson)) {
            // 3. 如果不存在，直接返回
            return null;
        }
        // 4. 如果存在，需要先把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(data, type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5. 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 如果未过期，直接返回商铺信息
            return r;
        }
        // 5.2 如果过期，缓存重建
        // 6. 缓存重建
        // 6.1 获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2 判断是否获取成功
        if (isLock) {
            // 6.3 成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                // 重建缓存
                try {
                    // 查询数据库
                    R r1 = dbFallback.apply(id);
                    // 写入Redis
                    this.setWithLogicalExpire(key, r1, time, timeUnit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 释放锁
                    unlock(lockKey);
                }
            });
        }
        // 6.4 获取失败，返回过期的商铺信息
        return r;
    }

    /**
     * @Description: 使用SETNX自定义互斥锁，获取锁
     * @Param: key      {java.lang.String}
     * @Return: boolean
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 15:34
     */
    private boolean tryLock(String key) {
        // SETNX(setIfAbsent)方法是原子的，只有一个线程能够获取到锁，这里给key设置过期时间10秒，防止程序发生意外导致锁一直存在
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    /**
     * @Description: 释放锁
     * @Param: key      {java.lang.String}
     * @Return: void
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 15:34
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

}

