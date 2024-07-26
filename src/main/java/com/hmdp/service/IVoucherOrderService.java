package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * @Description: 秒杀代金券
     * @Param: voucherId      {java.lang.Long}
     * @Return: com.hmdp.dto.Result
     * @Author: cwp0
     * @CreatedTime: 2024/7/26 14:38
     */
    Result seckillVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId);
}
