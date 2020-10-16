package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author fzqqq
 * @create 2020-10-13 19:40
 */
@Configuration
public class ThreadConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${threadPool.coreSize}")Integer coreSize,
            @Value("${threadPool.maxSize}")Integer maxSize,
            @Value("${threadPool.keepAlive}")Integer keepAlive,
            @Value("${threadPool.blockingSize}")Integer blockingSize

    ){
        return  new ThreadPoolExecutor(coreSize,maxSize,keepAlive, TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockingSize), Executors.defaultThreadFactory(),
                (Runnable r,ThreadPoolExecutor executor)->{
                    // 记录被拒绝的请求
                    System.out.println("您的请求被拒绝了");
                });
    }

}
