package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;


/**
 * @Program: hm-dianping
 * @Package: com.hmdp.utils
 * @Class: SimpleRedisLock
 * @Description: ILock的实现类
 * @Author: cwp0
 * @CreatedTime: 2024/07/28 14:27
 * @Version: 1.0
 */
public class SimpleRedisLock implements ILock{

    /* 锁的名字 */
    private String name;

    /* 锁的前缀 */
    private static final String LOCK_PREFIX = "lock:";

    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * @Description: 尝试获取锁
     * @Param: timeSec   锁持有的超时时间，过期后自动释放   {long}
     * @Return: boolean
     * @Author: cwp0
     * @CreatedTime: 2024/7/28 14:28
     */
    @Override
    public boolean tryLock(long timeSec) {
        // 获取当前线程的id
        String threadId = String.valueOf(Thread.currentThread().getId());
        // 获取锁
        Boolean isSuccess = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + name, threadId, timeSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(isSuccess);
    }

    /**
     * @Description: 释放锁
     * @Param:       {}
     * @Return: void
     * @Author: cwp0
     * @CreatedTime: 2024/7/28 14:28
     */
    @Override
    public void unlock() {
        stringRedisTemplate.delete(LOCK_PREFIX + name);
    }
}

