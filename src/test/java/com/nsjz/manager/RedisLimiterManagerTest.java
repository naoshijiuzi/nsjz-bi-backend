package com.nsjz.manager;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 郭春燕
 */
@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Test
    void doRateLimite() {
        String userId="1";
        for (int i = 0; i < 2; i++) {
            redisLimiterManager.doRateLimite(userId);
            System.out.println("成功");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 5; i++) {
            redisLimiterManager.doRateLimite(userId);
            System.out.println("成功1");
        }
    }
}