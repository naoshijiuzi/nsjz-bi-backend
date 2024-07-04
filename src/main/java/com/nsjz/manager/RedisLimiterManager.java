package com.nsjz.manager;

import com.nsjz.common.ErrorCode;
import com.nsjz.exception.BusinessException;
import jakarta.annotation.Resource;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * @author 郭春燕
 *
 * 提供RedisLimiter限流基础服务
 */
@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     * @param key 区分不同的限流器，比如不同的用户id应分别统计
     */
    public void doRateLimite(String key){
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //(每秒2个请求，连续的请求最多只能有1个被允许通过)
        //OVERALL表示速率限制作用于整个令牌桶，即限制所有请求的速率
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);

        //每当一个操作来了后，请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        //如果没有令牌，还想执行操作，就抛出异常
        if(!canOp){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
