package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
public class SimpleRedisLock implements ILock{
    //锁名字
    private String lockName;
    private StringRedisTemplate stringRedisTemplate;
    //锁前缀
    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true)+"-";


    public SimpleRedisLock(String lockName,StringRedisTemplate stringRedisTemplate){
        this.lockName = lockName;
        this.stringRedisTemplate  = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        //获取线程id
        String threadId = ID_PREFIX+Thread.currentThread().getId();
        //获取锁
        Boolean success = stringRedisTemplate
                .opsForValue().setIfAbsent(KEY_PREFIX+lockName,threadId,timeoutSec , TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        //获取线程id
        String threadId = ID_PREFIX+Thread.currentThread().getId();
        //获取去锁中标识
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX+lockName);
        //获取线程表示
        if(threadId.equals(id)){
            stringRedisTemplate.delete(KEY_PREFIX+lockName);
        }
    }
}
