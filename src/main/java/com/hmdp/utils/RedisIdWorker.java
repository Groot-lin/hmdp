package com.hmdp.utils;

import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    //设置初始时间
    private static final long BEGIN_TIMESRAMP = 1640995200L;

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     *Redis自增id策略
     * @param keyPrefix 前缀区分业务
     * @return
     */
    public long nextId(String keyPrefix){
        //1.生成时间戳
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESRAMP;
        //2.生成序列号

        //获取当前日期--天
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr" + keyPrefix + date);

        //3.拼接并返回
        long a = timestamp << 32  | count;
        return  a;
    }

    public static void main(String[] args) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        RedisIdWorker redisIdWorker = new RedisIdWorker(stringRedisTemplate);
        redisIdWorker.nextId("a");
    }
}
