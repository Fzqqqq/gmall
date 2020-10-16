package com.atguigu.gmall.index.utils;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author fzqqq
 * @create 2020-10-11 20:38
 */
@Component
public class DistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Thread thread;

    // 枷锁
    public Boolean tryLock(String lockName, String uuid, Long expire){
        String script = "if(redis.call('exists', KEYS[1])==0 or redis.call('hexists', KEYS[1], ARGV[1])==1) then redis.call('hincrby', KEYS[1], ARGV[1], 1) redis.call('expire', KEYS[1], ARGV[2]) return 1;  else return 0; end";
        if (!this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString())) {
            try {
                //没获取到锁  重试
                Thread.sleep(30);
                tryLock(lockName, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.renewExpire(lockName, uuid, expire);
        return true;
    }

    // 解锁
    public void unlock(String lockName, String uuid){
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 0) then return nil elseif(redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) then return 0 else redis.call('del', KEYS[1]) return 1 end";
        // 这里之所以没有跟加锁一样使用 Boolean ,这是因为解锁 lua 脚本中，三个返回值含义如下：
        // 1 代表解锁成功，锁被释放
        // 0 代表可重入次数被减 1
        // null 代表其他线程尝试解锁，解锁失败
        Long result  = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Lists.newArrayList(lockName), uuid);
        thread.interrupt();
        if (result == null) {
            throw new IllegalMonitorStateException("attempt to unlock lock, not locked by lockName: "
                    + lockName + " with request: "  + uuid);
        }
    }

    // 自动续期
    private void renewExpire(String lockName, String uuid, Long expireTime) {
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 1) then redis.call('expire', KEYS[1], ARGV[2]) return 1 else return 0 end";
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(expireTime * 1000 * 2 / 3);
                        redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expireTime.toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "input you threadname");
        thread.start();
    }
}
