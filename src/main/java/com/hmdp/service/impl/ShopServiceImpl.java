package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @Description: 根据id查询商铺信息
     * @Param: id      {java.lang.Long}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 12:58
     */
    @Override
    public Result queryById(Long id) {
        // 缓存穿透和缓存雪崩
        // Shop shop = queryWithPassThrough(id);

        // 使用互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }

    /**
     * @Description: 互斥锁解决缓存击穿问题
     * @Param: id      {java.lang.Long}
     * @Return: com.hmdp.entity.Shop
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 15:39
     */
    public Shop queryWithMutex(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        // 1. 从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 如果存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 判断命中的是否是空值
        if (shopJson != null) {
            return null;
        }
        // 4. 实现缓存重建
        // 4.1 获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 判断是否获取成功
            if (!isLock) {
                // 4.3 失败，则休眠并重试
                Thread.sleep(50);
                // 重试
                return queryWithMutex(id);
            }
            // 4.4 成功，根据id查询数据库
            shop = getById(id);
            // 模拟重建缓存的耗时
            Thread.sleep(200);
            // 5. 数据库中也不存在，返回错误
            if (shop == null) {
                // 将空值写入redis，防止缓存穿透
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 6. 数据库中存在，将数据写入redis缓存, 给过期时间添加一个随机值，防止缓存雪崩
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL + (long) (Math.random() * 10), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7. 释放互斥锁
            unlock(lockKey);
        }
        // 8. 返回数据
        return shop;
    }

    /**
     * @Description: 封装缓存穿透&缓存雪崩代码
     * @Param: id      {java.lang.Long}
     * @Return: com.hmdp.entity.Shop
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 15:37
     */
    public Shop queryWithPassThrough(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        // 1. 从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 如果存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        // 判断命中的是否是空值
        if (shopJson != null) {
            return null;
        }
        // 4. 如果不存在，根据id查询数据库
        Shop shop = getById(id);
        // 5. 数据库中也不存在，返回错误
        if (shop == null) {
            // 将空值写入redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);

            return null;
        }
        // 6. 数据库中存在，将数据写入redis缓存, 给过期时间添加一个随机值，防止缓存雪崩
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL + (long) (Math.random() * 10), TimeUnit.MINUTES);
        // 7. 返回数据
        return shop;
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

    /**
     * @Description: 更新商铺信息
     * @Param: shop      {com.hmdp.entity.Shop}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 13:56
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("商铺id不能为空");
        }
        // 1. 更新数据库
        updateById(shop);
        // 2. 删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);

        return Result.ok();
    }
}
