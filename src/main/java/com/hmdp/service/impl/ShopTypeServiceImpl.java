package com.hmdp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;

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

    @Override
    public Result queryList() {
        //1: 从redis中查询是否有店铺列表信息
        String key = CACHE_SHOPTYPE_KEY;
        String cacheShop = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotEmpty(cacheShop)) {
            //2: 有直接返回
            List<ShopType> shopTypes = JSONObject.parseArray(cacheShop,ShopType.class);
            return Result.ok(shopTypes);
        }
        //3：没有、去数据库中查询
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if (CollUtil.isEmpty(typeList)) {
            //4：没有
            return Result.fail("没有商品类别");
        }
        //5：有、将店铺数据写入redis中
        stringRedisTemplate.opsForValue().set(key , JSON.toJSONString(typeList));
        //6：返回店铺列表数据
        return Result.ok(typeList);
    }
}
