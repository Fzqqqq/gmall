package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsFeign;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.utils.DistributedLock;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author fzqqq
 * @create 2020-10-09 15:21
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GmallPmsFeign pmsFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private DistributedLock distributedLock;

    public static final String KEY_PREFIX = "index:category:";

    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsFeign.queryCategoryByParentId(0l);
        return listResponseVo.getData();
    }

    @GmallCache(prefix = "index:category:", timeout = 14400, random = 3600, lock = "lock" )
    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
        // 从缓存中获取
//        String cacheCategories = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (StringUtils.isNotBlank(cacheCategories)) {
//            // 如果缓存中有，直接返回
//            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
//            return categoryEntities;
//        }

        // 加了注解之后 只需要关注业务的实现
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsFeign.queryCategoriesWithSub(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
//        if (CollectionUtils.isEmpty(categoryEntities)) {
//            // 把查询结果放入缓存
//            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 5, TimeUnit.MINUTES);
//
//        }else {
//            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 90 + new Random().nextInt(5), TimeUnit.MINUTES);
//
//        }
        return categoryEntities;
    }

//    @Override
//    public synchronized void testLock() {
//        // 查询redis中的num值
//        String num = this.redisTemplate.opsForValue().get("num");
//        if (StringUtils.isBlank(num)) {
//            return;
//        }
//        // 有值， 转化成int执行加一操作
//        int numInt = Integer.parseInt(num);
//        this.redisTemplate.opsForValue().set("num",String.valueOf(++numInt));
//    }

    // redis分布式锁  防误删防，并设置过期时间（原子性）
//    @Override
//    public void testLock() {
//        String uuid = UUID.randomUUID().toString();
//        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);
//        if (lock) {
//            // 查询redis中的num值
//            String num = this.redisTemplate.opsForValue().get("num");
//            if (StringUtils.isBlank(num)) {
//                return;
//            }
//            // 有值， 转化成int执行加一操作
//            int numInt = Integer.parseInt(num);
//            this.redisTemplate.opsForValue().set("num",String.valueOf(++numInt));
//            // 2. 释放锁 del
//            if (StringUtils.equals(redisTemplate.opsForValue().get("lock"),uuid)){
//                this.redisTemplate.delete("lock");
//            }
//        }else {
//            // 3. 每隔0.1秒钟回调一次，再次尝试获取锁
//            try {
//                Thread.sleep(100);
//                testLock();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
    // 优化之LUA脚本保证删除的原子性
//    @Override
//    public void testLock() {
//        String uuid = UUID.randomUUID().toString();
//        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);
//        if (lock) {
//            // 查询redis中的num值
//            String num = this.redisTemplate.opsForValue().get("num");
//            if (StringUtils.isBlank(num)) {
//                return;
//            }
//            // 有值， 转化成int执行加一操作
//            int numInt = Integer.parseInt(num);
//            this.redisTemplate.opsForValue().set("num",String.valueOf(++numInt));
//            // 2. 释放锁 del
//            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
//            this.redisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"), uuid);
//        }else {
//            // 3. 每隔0.1秒钟回调一次，再次尝试获取锁
//            try {
//                Thread.sleep(100);
//                testLock();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    // 优化可重入
//    @Override
//    public void testLock() {
//        String uuid = UUID.randomUUID().toString();
//        // 加锁
//        Boolean lock = distributedLock.tryLock("lock", uuid, 9l);
//
//        if (lock) {
//
//            // 查询redis中的num值
//            String num = this.redisTemplate.opsForValue().get("num");
//
//            if (StringUtils.isBlank(num)) {
//                return;
//            }
//            // 有值， 转化成int执行加一操作
//            int numInt = Integer.parseInt(num);
//            this.redisTemplate.opsForValue().set("num",String.valueOf(++numInt));
//
//            this.testSubLock(uuid);
//
//            try {
//                TimeUnit.SECONDS.sleep(60);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//
//            //释放锁
//            distributedLock.unlock("lock",uuid);
//        }
//    }
//    // 测试可重入性
//    private void testSubLock(String uuid){
//        // 加锁
//        Boolean lock = distributedLock.tryLock("lock", uuid, 30l);
//
//        if (lock) {
//            System.out.println("分布式可重入锁。。。");
//
//            distributedLock.unlock("lock", uuid);
//        }
//    }

    // 使用redisson
    @Override
    public void testLock() {
        RLock lock = this.redissonClient.getLock("lock");//只要锁的名称相同就是同一把锁
//        lock.lock();// 加锁
        lock.lock(10,TimeUnit.SECONDS);// 改良：10s后自动释放锁
        // 查询redis中的num值
        String num = this.redisTemplate.opsForValue().get("num");

        if (StringUtils.isBlank(num)) {
            return;
        }
        // 有值， 转化成int执行加一操作
        int numInt = Integer.parseInt(num);
        this.redisTemplate.opsForValue().set("num",String.valueOf(++numInt));

        //释放锁
        lock.unlock();//改良后会自动释放

    }
}
