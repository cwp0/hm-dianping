package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    /**
     * @Description: 查询商铺类型列表
     * @Param:       {}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/25 13:16
     */
    Result queryShopTypeList();
}
