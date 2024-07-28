package com.hmdp.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import cn.hutool.core.lang.UUID;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
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

    /* 线程id的前缀 */
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    private StringRedisTemplate stringRedisTemplate;

    /* 释放锁的Lua脚本 */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * @Description: 尝试获取锁
     * @Param: timeSec   锁持有的超时时间，过期后自动释放，避免长时间不释放导致死锁问题   {long}
     * @Return: boolean
     * @Author: cwp0
     * @CreatedTime: 2024/7/28 14:28
     */
    @Override
    public boolean tryLock(long timeSec) {
        // 获取当前线程的标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
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
        // 基于Lua脚本释放锁，保证获取锁标识和释放锁的原子性
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(LOCK_PREFIX + name), ID_PREFIX + Thread.currentThread().getId());

        /*// 获取当前线程的标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁中的标识
        String id = stringRedisTemplate.opsForValue().get(LOCK_PREFIX + name);
        // 判断标识是否一致
        if (threadId.equals(id)) {
            // 释放锁
            stringRedisTemplate.delete(LOCK_PREFIX + name);
        }*/
    }
}

