package com.hmdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Shop;
import com.hmdp.entity.User;
import com.hmdp.rebbitmq.MQSender;
import com.hmdp.service.IShopService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.service.impl.UserServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import lombok.Cleanup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.*;

@SpringBootTest
class HmDianPingApplicationTests {
    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ShopServiceImpl shopService;
    @Resource
    private MQSender mqSender;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    UserServiceImpl userService;

    private ExecutorService es = Executors.newFixedThreadPool(500);
    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for(int i = 0;i<100;i++){
                long id = redisIdWorker.nextId("order");
                System.out.println("id = "+id);
            }
        };
        long begin = System.currentTimeMillis();
        for (int i = 0;i<300 ;i++){
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();

        System.out.println("time = "+(end-begin));
    }

    /**
     * 千人秒杀高并发测试生成token
     * @throws IOException
     */
    @Test
    void testMultiLogin() throws IOException {
        List<User> userList = userService.lambdaQuery().last("limit 1000").list();
        for(User user : userList){
            String token = UUID.randomUUID().toString(true);
            UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);
            Map<String ,Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
            String tokenKey = LOGIN_USER_KEY+token;
            stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);

            stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.MINUTES);
        }
        Set<String> keys = stringRedisTemplate.keys(LOGIN_USER_KEY+"*");
        @Cleanup FileWriter fileWriter = new FileWriter(System.getProperty("user.dir")+"\\tokens.txt");
        @Cleanup BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        assert keys != null;
        for (String key : keys){
            String token = key.substring(LOGIN_USER_KEY.length());
            String text = token + "\n";
            bufferedWriter.write(text);
        }
    }

    /**
     * 测试mq
     */
    @Test
    public void mq(){
//        mqSender.send("abcd666");
//        mqSender.send01("red");
//        mqSender.send02("flowwer");
        //mqSender.send03("aaaaaaaa");
    }

    /**
     * redisGEO导入商户数据
     */
    @Test
    void loadShopData(){
        //1.查询店铺信息
        List<Shop> list = shopService.list();
        //2.把店铺分组,按type-id分组
        Map<Long,List<Shop>> map = list.stream().collect(Collectors.groupingBy(shop -> shop.getTypeId()));
        //3.分批存储
        for(Map.Entry<Long,List<Shop>> entry : map.entrySet()){
            //3.1获取类型id
            Long typeId = entry.getKey();
            String key = SHOP_GEO_KEY+typeId;
            //3.2获取同类型的店铺集合
            List<Shop> shops = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(shops.size());
            //3.3写入redis GEOADD
            for(Shop shop:shops){
               locations.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(),new Point(shop.getX(),shop.getY())));
            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }

}
