package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    /**
     * @Description: 根据id查询商铺信息
     * @Param: id      {java.lang.Long}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 12:57
     */
    Result queryById(Long id);

    /**
     * @Description: 更新商铺信息
     * @Param: shop      {com.hmdp.entity.Shop}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 13:55
     */
    Result update(Shop shop);

}
