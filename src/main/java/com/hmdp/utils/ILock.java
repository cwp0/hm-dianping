package com.hmdp.utils;

/**
 * @Program: hm-dianping
 * @Package: com.hmdp.utils
 * @Interface: ILock
 * @Description: 使用Redis自定义非阻塞式锁，也就是Redis实现分布式锁，保证多个JVM之间线程的互斥
 * @Author: cwp0
 * @CreatedTime: 2024/07/28 14:25
 * @Version: 1.0
 */
public interface ILock {

    /**
     * @Description: 尝试获取锁
     * @Param: timeSec   锁持有的超时时间，过期后自动释放   {long}
     * @Return: boolean
     * @Author: cwp0
     * @CreatedTime: 2024/7/28 14:26
     */
    boolean tryLock(long timeSec);


    /**
     * @Description: 释放锁
     * @Param:       {}
     * @Return: void
     * @Author: cwp0
     * @CreatedTime: 2024/7/28 14:26
     */
    void unlock();
}
