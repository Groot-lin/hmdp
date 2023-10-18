package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherService extends IService<Voucher> {

    /**
     * 查询商户的优惠卷
     * @param shopId
     * @return
     */
    Result queryVoucherOfShop(Long shopId);

    /**
     * 添加秒杀优惠卷
     * @param voucher
     */
    void addSeckillVoucher(Voucher voucher);
}
