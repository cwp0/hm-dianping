package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IShopTypeService typeService;

    /**
     * @Description: 查询商铺类型列表
     * @Param:       {}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 13:17
     */
    public Result queryShopTypeList() {
        String key = RedisConstants.CACHE_SHOP_LIST_KEY;
        // 1. 从Redis中查询缓存
        String shopListJson = stringRedisTemplate.opsForValue().get(key);
        // 2. 判断是否存在
        if (!StrUtil.isBlank(shopListJson)) {
            // 3. 如果存在，直接返回
            List<ShopType> shopTypeList = JSONUtil.toList(JSONUtil.parseArray(shopListJson), ShopType.class);
            return Result.ok(shopTypeList);
        }
        // 4. 如果不存在，查询数据库
        List<ShopType> shopTypeList = typeService.query().orderByAsc("sort").list();
        // 5. 数据库中也不存在，返回错误
        if (shopTypeList == null) {
            return Result.fail("商铺类型不存在");
        }
        // 6. 数据库中存在，将数据写入Redis缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypeList), RedisConstants.CACHE_SHOP_LIST_TTL, TimeUnit.MINUTES);
        // 7. 返回数据
        return Result.ok(shopTypeList);
    }
}
